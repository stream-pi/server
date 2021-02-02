package com.stream_pi.server.window.settings;

import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.client.ClientTheme;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.connection.ServerListener;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.SpaceFiller;
import com.stream_pi.util.platform.ReleaseStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientsSettings extends VBox {
    private VBox clientsSettingsVBox;
    private Button saveButton;

    private ServerListener serverListener;

    private Logger logger;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public ClientsSettings(ExceptionAndAlertHandler exceptionAndAlertHandler, ServerListener serverListener)
    {   
        getStyleClass().add("clients_settings");
        this.serverListener = serverListener;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        clientSettingsVBoxArrayList = new ArrayList<>();

        setPadding(new Insets(10.0));

        logger = Logger.getLogger(ClientsSettings.class.getName());

        clientsSettingsVBox = new VBox();
        clientsSettingsVBox.setSpacing(20.0);
        clientsSettingsVBox.setAlignment(Pos.TOP_CENTER);

        setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("clients_settings_scroll_pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));

        clientsSettingsVBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));
        scrollPane.setContent(clientsSettingsVBox);

        saveButton = new Button("Save");
        saveButton.setOnAction(event -> onSaveButtonClicked());

        HBox hBox = new HBox(saveButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(scrollPane, hBox);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void onSaveButtonClicked()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    Platform.runLater(()->saveButton.setDisable(true));
                    StringBuilder finalErrors = new StringBuilder();

                    for(ClientSettingsVBox clientSettingsVBox : clientSettingsVBoxArrayList)
                    {
                        StringBuilder errors = new StringBuilder();

                        if(clientSettingsVBox.getNickname().isBlank())
                            errors.append("    Cannot have blank nickname. \n");

                        try {
                            Double.parseDouble(clientSettingsVBox.getStartupWindowHeight());
                        }
                        catch (NumberFormatException e)
                        {
                            errors.append("    Must have integer display height. \n");
                        }

                        try {
                            Double.parseDouble(clientSettingsVBox.getStartupWindowWidth());
                        }
                        catch (NumberFormatException e)
                        {
                            errors.append("    Must have integer display width. \n");
                        }

                        for(ClientProfileVBox clientProfileVBox : clientSettingsVBox.getClientProfileVBoxes())
                        {
                            StringBuilder errors2 = new StringBuilder();

                            if(clientProfileVBox.getName().isBlank())
                                errors2.append("        cannot have blank nickname. \n");

                            try {
                                Integer.parseInt(clientProfileVBox.getActionSize());
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer action Size. \n");
                            }


                            try {
                                Integer.parseInt(clientProfileVBox.getActionGap());
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer action Gap. \n");
                            }


                            try {
                                int rows = Integer.parseInt(clientProfileVBox.getRows());

                                int actionsSize = Integer.parseInt(clientProfileVBox.getActionSize());
                                double startupWidth = Double.parseDouble(clientSettingsVBox.getStartupWindowWidth());


                                if((rows*actionsSize) > (startupWidth - 25) && clientSettingsVBox.getPlatform()!= com.stream_pi.util.platform.Platform.ANDROID)
                                {
                                    errors2.append("        Rows out of bounds of screen size. \n"+startupWidth);
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer Rows. \n");
                            }


                            try {
                                int cols = Integer.parseInt(clientProfileVBox.getCols());

                                int actionsSize = Integer.parseInt(clientProfileVBox.getActionSize());
                                double startupHeight = Double.parseDouble(clientSettingsVBox.getStartupWindowHeight());

                                if((cols*actionsSize) > (startupHeight - 25) && clientSettingsVBox.getPlatform()!= com.stream_pi.util.platform.Platform.ANDROID)
                                {
                                    errors2.append("        Cols out of bounds of screen size. \n"+startupHeight);
                                }
                            }
                            catch (NumberFormatException e)
                            {
                                errors2.append("        Must have integer Columns. \n");
                            }


                            if(!errors2.toString().isEmpty())
                            {
                                errors.append("    ")
                                        .append(clientProfileVBox.getRealName())
                                        .append("\n")
                                        .append(errors2.toString())
                                        .append("\n");
                            }
                        }


                        if(!errors.toString().isEmpty())
                        {
                            finalErrors.append("* ")
                                    .append(clientSettingsVBox.getRealNickName())
                                    .append("\n")
                                    .append(errors.toString())
                                    .append("\n");
                        }



                    }

                    if(!finalErrors.toString().isEmpty())
                        throw new MinorException("You made form mistakes",
                                "Please fix the following issues : \n"+finalErrors.toString());



                    //save details and values
                    for(ClientSettingsVBox clientSettingsVBox : clientSettingsVBoxArrayList)
                    {
                        clientSettingsVBox.saveClientAndProfileDetails();
                    }

                    loadData();
                    serverListener.clearTemp();
                }
                catch (MinorException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleMinorException(e);
                }
                catch (SevereException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleSevereException(e);
                }
                catch (CloneNotSupportedException e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleSevereException(new SevereException(
                            e.getMessage()
                    ));
                }
                finally
                {
                    Platform.runLater(()->saveButton.setDisable(false));
                }
                return null;
            }
        }).start();
    }

    private ArrayList<ClientSettingsVBox> clientSettingsVBoxArrayList;

    public void loadData()
    {
        logger.info("Loading client data into ClientsSettings ...");

        Platform.runLater(()-> clientsSettingsVBox.getChildren().clear());
        clientSettingsVBoxArrayList.clear();

        List<ClientConnection> clientConnections = ClientConnections.getInstance().getConnections();

        if(clientConnections.size() == 0)
        {
            Platform.runLater(()->{
                clientsSettingsVBox.getChildren().add(new Label("No Clients Connected."));
                saveButton.setVisible(false);
            });
        }
        else
        {
            Platform.runLater(()->saveButton.setVisible(true));
            for (ClientConnection clientConnection : clientConnections) {
                ClientSettingsVBox clientSettingsVBox = new ClientSettingsVBox(clientConnection);

                clientSettingsVBoxArrayList.add(clientSettingsVBox);
                Platform.runLater(()->clientsSettingsVBox.getChildren().add(clientSettingsVBox));
            }
        }

        logger.info("... Done!");
    }

    public class ClientSettingsVBox extends VBox
    {
        private ComboBox<ClientProfile> profilesComboBox;

        private ComboBox<ClientTheme> themesComboBox;

        private TextField startupWindowHeightTextField;

        public String getStartupWindowHeight() {
            return startupWindowHeightTextField.getText();
        }

        private TextField startupWindowWidthTextField;

        public String getStartupWindowWidth() {
            return startupWindowWidthTextField.getText();
        }

        private TextField nicknameTextField;

        public String getNickname() {
            return nicknameTextField.getText();
        }

        private Label nickNameLabel;

        private Label versionLabel;

        public String getRealNickName()
        {
            return nickNameLabel.getText();
        }

        private com.stream_pi.util.platform.Platform platform;

        public com.stream_pi.util.platform.Platform getPlatform() {
            return platform;
        }

        private Label socketConnectionLabel;

        private ClientConnection connection;

        private Accordion profilesAccordion;

        private ArrayList<ClientProfileVBox> clientProfileVBoxes;

        private Label platformLabel;

        private HBoxInputBox startupWindowHeightInputBox, startupWindowWidthInputBox;

        public ArrayList<ClientProfileVBox> getClientProfileVBoxes() {
            return clientProfileVBoxes;
        }

        public ClientSettingsVBox(ClientConnection connection)
        {
            this.connection = connection;
            this.platform = connection.getClient().getPlatform();

            clientProfileVBoxes = new ArrayList<>();

            initUI();
            loadValues();
        }

        public ClientConnection getConnection()
        {
            return connection;
        }

        public void saveClientAndProfileDetails() throws SevereException, CloneNotSupportedException, MinorException {
            System.out.println("IIN");
            getConnection().saveClientDetails(
                    nicknameTextField.getText(),
                    startupWindowWidthTextField.getText(),
                    startupWindowHeightTextField.getText(),
                    profilesComboBox.getSelectionModel().getSelectedItem().getID(),
                    themesComboBox.getSelectionModel().getSelectedItem().getThemeFullName()
            );

            System.out.println("OUT");
            
            logger.info("Profiles : ");
            for(ClientProfileVBox clientProfileVBox : clientProfileVBoxes)
            {
                logger.info("Name : "+clientProfileVBox.getClientProfile().getName());
                getConnection().saveProfileDetails(clientProfileVBox.getClientProfile());
            }


            //remove deleted client profiles
            for(ClientProfile clientProfile : connection.getClient().getAllClientProfiles())
            {
                boolean found = false;
                for(ClientProfileVBox clientProfileVBox : clientProfileVBoxes)
                {
                    if(clientProfileVBox.getClientProfile().getID().equals(clientProfile.getID()))
                    {
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    connection.getClient().removeProfileFromID(clientProfile.getID());
                    connection.deleteProfile(clientProfile.getID());
                }
            }


        }

        public void initUI()
        {
            profilesComboBox = new ComboBox<>();
            Callback<ListView<ClientProfile>, ListCell<ClientProfile>> profilesComboBoxFactory = new Callback<>() {
                @Override
                public ListCell<ClientProfile> call(ListView<ClientProfile> clientConnectionListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ClientProfile clientProfile, boolean b) {
                            super.updateItem(clientProfile, b);

                            if(clientProfile == null)
                            {
                                setText(null);
                            }
                            else
                            {
                                setText(clientProfile.getName());
                            }
                        }
                    };
                }
            };
            profilesComboBox.setCellFactory(profilesComboBoxFactory);
            profilesComboBox.setButtonCell(profilesComboBoxFactory.call(null));

            themesComboBox = new ComboBox<>();
            Callback<ListView<ClientTheme>, ListCell<ClientTheme>> themesComboBoxFactory = new Callback<>() {
                @Override
                public ListCell<ClientTheme> call(ListView<ClientTheme> clientConnectionListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(ClientTheme clientTheme, boolean b) {
                            super.updateItem(clientTheme, b);

                            if(clientTheme == null)
                            {
                                setText(null);
                            }
                            else
                            {
                                setText(clientTheme.getShortName());
                            }
                        }
                    };
                }
            };
            themesComboBox.setCellFactory(themesComboBoxFactory);
            themesComboBox.setButtonCell(themesComboBoxFactory.call(null));

            startupWindowHeightTextField = new TextField();
            startupWindowWidthTextField = new TextField();

            platformLabel = new Label();
            platformLabel.getStyleClass().add("settings_client_platform_label");

            socketConnectionLabel = new Label();
            socketConnectionLabel.getStyleClass().add("settings_client_socket_connection_label");

            nicknameTextField = new TextField();

            nickNameLabel = new Label();
            nickNameLabel.getStyleClass().add("settings_client_nick_name_label");

            versionLabel = new Label();
            versionLabel.getStyleClass().add("settings_client_version_label");

            profilesAccordion = new Accordion();
            VBox.setMargin(profilesAccordion, new Insets(0,0,20,0));


            Button addNewProfileButton = new Button("Add new Profile");
            addNewProfileButton.setOnAction(event -> onNewProfileButtonClicked());

            setSpacing(10.0);

            getStyleClass().add("settings_clients_each_client");


            startupWindowHeightInputBox = new HBoxInputBox("Startup window Height", startupWindowHeightTextField);
            startupWindowHeightInputBox.managedProperty().bind(startupWindowHeightInputBox.visibleProperty());

            startupWindowWidthInputBox = new HBoxInputBox("Startup window Width", startupWindowWidthTextField);
            startupWindowWidthInputBox.managedProperty().bind(startupWindowWidthInputBox.visibleProperty());


            this.getChildren().addAll(
                    nickNameLabel,
                    socketConnectionLabel,
                    platformLabel,
                    versionLabel,
                    new HBoxInputBox("Nickname",nicknameTextField),
                    new HBox(
                        new Label("Theme"),
                        new SpaceFiller(SpaceFiller.FillerType.HBox),
                        themesComboBox
                    ),

                    startupWindowHeightInputBox,

                    startupWindowWidthInputBox,

                    new HBox(new Label("Startup Profile"),
                            new SpaceFiller(SpaceFiller.FillerType.HBox),
                            profilesComboBox),

                    addNewProfileButton,

                    profilesAccordion);
        }

        public void loadValues()
        {
            Client client = connection.getClient();

            profilesComboBox.setItems(FXCollections.observableList(client.getAllClientProfiles()));
            profilesComboBox.getSelectionModel().select(
                    client.getProfileByID(client.getDefaultProfileID())
            );


            themesComboBox.setItems(FXCollections.observableList(client.getThemes()));
            themesComboBox.getSelectionModel().select(
                    client.getThemeByFullName(
                            client.getDefaultThemeFullName()
                    )
            );

            nicknameTextField.setText(client.getNickName());

            if(client.getPlatform() == com.stream_pi.util.platform.Platform.ANDROID)
            {
                startupWindowHeightInputBox.setVisible(false);
                startupWindowWidthInputBox.setVisible(false);
            }

            platformLabel.setText("Platform : "+client.getPlatform().getUIName());

            startupWindowWidthTextField.setText(client.getStartupDisplayWidth()+"");
            startupWindowHeightTextField.setText(client.getStartupDisplayHeight()+"");

            socketConnectionLabel.setText(client.getRemoteSocketAddress().toString().substring(1)); //substring removes the `/`

            nickNameLabel.setText(client.getNickName());

            versionLabel.setText(client.getReleaseStatus().getUIName()+" "+client.getVersion().getText());

            //add profiles
            for(ClientProfile clientProfile : client.getAllClientProfiles())
            {
                TitledPane titledPane = new TitledPane();
                titledPane.setText(clientProfile.getName());

                ClientProfileVBox clientProfileVBox = new ClientProfileVBox(clientProfile);

                clientProfileVBox.getRemoveButton().setOnAction(event -> onProfileDeleteButtonClicked(clientProfileVBox, titledPane));

                titledPane.setContent(clientProfileVBox);

                clientProfileVBoxes.add(clientProfileVBox);

                profilesAccordion.getPanes().add(titledPane);
            }
        }

        public void onNewProfileButtonClicked()
        {
            ClientProfile clientProfile = new ClientProfile(
                    "Untitled Profile",
                    3,
                    3,
                    100,
                    5
            );


            ClientProfileVBox clientProfileVBox = new ClientProfileVBox(clientProfile);
            TitledPane titledPane = new TitledPane();
            titledPane.setContent(clientProfileVBox);
            titledPane.setText(clientProfile.getName());

            clientProfileVBox.getRemoveButton().setOnAction(event -> onProfileDeleteButtonClicked(clientProfileVBox, titledPane));

            clientProfileVBoxes.add(clientProfileVBox);

            profilesAccordion.getPanes().add(titledPane);
        }

        public void onProfileDeleteButtonClicked(ClientProfileVBox clientProfileVBox, TitledPane titledPane)
        {
            if(clientProfileVBoxes.size() == 1)
            {
                exceptionAndAlertHandler.handleMinorException(new MinorException("Only one",
                        "You cannot delete all profiles"));
            }
            else
            {
                if(profilesComboBox.getSelectionModel().getSelectedItem().getID().equals(clientProfileVBox.getClientProfile().getID()))
                {
                    exceptionAndAlertHandler.handleMinorException(new MinorException("Default",
                            "You cannot delete default profile. Change to another one to delete this."));
                }
                else
                {
                    clientProfileVBoxes.remove(clientProfileVBox);
                    profilesComboBox.getItems().remove(clientProfileVBox.getClientProfile());

                    profilesAccordion.getPanes().remove(titledPane);
                }
            }
        }
    }

    public class ClientProfileVBox extends VBox
    {
        private TextField nameTextField;

        public String getName()
        {
            return nameTextField.getText();
        }

        private TextField rowsTextField;

        public String getRows()
        {
            return rowsTextField.getText();
        }

        private TextField colsTextField;

        public String getCols()
        {
            return colsTextField.getText();
        }

        private TextField actionSizeTextField;

        public String getActionSize()
        {
            return actionSizeTextField.getText();
        }

        private TextField actionGapTextField;

        public String getActionGap()
        {
            return actionGapTextField.getText();
        }

        private Button removeButton;

        private ClientProfile clientProfile;

        public String getRealName()
        {
            return clientProfile.getName();
        }

        public ClientProfileVBox(ClientProfile clientProfile)
        {
            this.clientProfile = clientProfile;

            initUI();
            loadValues(clientProfile);
        }

        public void initUI()
        {
            setPadding(new Insets(5.0));
            setSpacing(10.0);

            nameTextField = new TextField();
            rowsTextField = new TextField();
            colsTextField = new TextField();
            actionSizeTextField = new TextField();
            actionGapTextField = new TextField();

            removeButton = new Button("Remove");

            HBox hBox = new HBox(removeButton);
            hBox.setAlignment(Pos.CENTER_RIGHT);


            getChildren().addAll(
                    new HBoxInputBox("Name ", nameTextField),
                    new HBoxInputBox("Columns", rowsTextField),
                    new HBoxInputBox("Rows", colsTextField),
                    new HBoxInputBox("action Size", actionSizeTextField),
                    new HBoxInputBox("action Gap", actionGapTextField),
                    hBox
            );
        }

        public Button getRemoveButton()
        {
            return removeButton;
        }

        public void loadValues(ClientProfile clientProfile)
        {
            nameTextField.setText(clientProfile.getName());

            rowsTextField.setText(clientProfile.getRows()+"");
            colsTextField.setText(clientProfile.getCols()+"");

            actionSizeTextField.setText(clientProfile.getActionSize()+"");
            actionGapTextField.setText(clientProfile.getActionGap()+"");
        }

        public ClientProfile getClientProfile()
        {
            clientProfile.setActionGap(Integer.parseInt(actionGapTextField.getText()));
            clientProfile.setActionSize(Integer.parseInt(actionSizeTextField.getText()));
            clientProfile.setRows(Integer.parseInt(rowsTextField.getText()));
            clientProfile.setCols(Integer.parseInt(colsTextField.getText()));
            clientProfile.setName(nameTextField.getText());

            return clientProfile;
        }
    }


}
