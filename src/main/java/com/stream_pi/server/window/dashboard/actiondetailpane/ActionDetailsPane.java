package com.stream_pi.server.window.dashboard.actiondetailpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.FileExtensionFilter;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.w3c.dom.Text;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ActionDetailsPane extends VBox implements ActionDetailsPaneListener
{

    private ScrollPane scrollPane;

    private VBox vbox;
    private VBox clientPropertiesVBox;

    private Button saveButton;
    private Button deleteButton;
    private Button openFolderButton;

    private HBox buttonBar;

    private Label actionHeadingLabel;

    private Logger logger;

    private Button returnButtonForCombineActionChild;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private HostServices hostServices;

    public ActionDetailsPane(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices)
    {
        this.hostServices = hostServices;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        logger = Logger.getLogger(ActionDetailsPane.class.getName());

        setSpacing(10.0);

        clientPropertiesVBox = new VBox();
        clientPropertiesVBox.setSpacing(10.0);

        vbox = new VBox();
        vbox.setPadding(new Insets(0, 25, 0, 5));
        vbox.getStyleClass().add("action_details_pane_vbox");

        vbox.setSpacing(10.0);

        getStyleClass().add("action_details_pane");

        scrollPane = new ScrollPane();
        VBox.setMargin(scrollPane, new Insets(0, 0, 0, 10));

        scrollPane.getStyleClass().add("action_details_pane_scroll_pane");

        setMinHeight(210);
        scrollPane.setContent(vbox);

        vbox.prefWidthProperty().bind(scrollPane.widthProperty());
        scrollPane.prefWidthProperty().bind(widthProperty());

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        openFolderButton = new Button("Open Folder");
        FontIcon folderOpenIcon = new FontIcon("far-folder-open");
        openFolderButton.setGraphic(folderOpenIcon);
        openFolderButton.setOnAction(event -> onOpenFolderButtonClicked());

        saveButton = new Button("Apply Changes");
        FontIcon syncIcon = new FontIcon("fas-sync-alt");
        saveButton.setGraphic(syncIcon);
        saveButton.setOnAction(event -> onSaveButtonClicked());

        deleteButton = new Button("Delete action");
        deleteButton.getStyleClass().add("action_details_pane_delete_button");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.getStyleClass().add("action_details_pane_delete_button_icon");
        deleteButton.setGraphic(deleteIcon);

        deleteButton.setOnAction(event -> onDeleteButtonClicked());

        returnButtonForCombineActionChild = new Button("Return");
        returnButtonForCombineActionChild.setGraphic(new FontIcon("fas-caret-left"));
        returnButtonForCombineActionChild.managedProperty().bind(returnButtonForCombineActionChild.visibleProperty());
        returnButtonForCombineActionChild.setOnAction(event -> {
            try {
                logger.info("@@## : " + action.getParent());
                onActionClicked(getClientProfile().getActionByID(action.getParent()), getActionBox());
            } catch (MinorException e) {
                e.printStackTrace();
            }
        });

        buttonBar = new HBox(openFolderButton, returnButtonForCombineActionChild, saveButton, deleteButton);
        buttonBar.getStyleClass().add("action_details_pane_button_bar");
        buttonBar.setPadding(new Insets(10, 10, 10, 0));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setVisible(false);
        buttonBar.setSpacing(10.0);

        actionHeadingLabel = new Label();
        actionHeadingLabel.getStyleClass().add("action_details_pane_heading_label");

        HBox headingHBox = new HBox(actionHeadingLabel);
        headingHBox.getStyleClass().add("action_details_pane_heading_box");
        headingHBox.setPadding(new Insets(5, 10, 0, 10));

        getChildren().addAll(headingHBox, scrollPane, buttonBar);

        displayTextAlignmentComboBox = new ComboBox<>(FXCollections.observableArrayList(DisplayTextAlignment.TOP,
                DisplayTextAlignment.CENTER, DisplayTextAlignment.BOTTOM));

        displayTextAlignmentComboBox.managedProperty().bind(displayTextAlignmentComboBox.visibleProperty());

        Callback<ListView<DisplayTextAlignment>, ListCell<DisplayTextAlignment>> displayTextAlignmentComboBoxFactory = new Callback<>() {
            @Override
            public ListCell<DisplayTextAlignment> call(ListView<DisplayTextAlignment> displayTextAlignment) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(DisplayTextAlignment displayTextAlignment, boolean b) {
                        super.updateItem(displayTextAlignment, b);

                        if (displayTextAlignment != null) {
                            setText(displayTextAlignment.getUIName());
                        }
                    }
                };
            }
        };
        displayTextAlignmentComboBox.setCellFactory(displayTextAlignmentComboBoxFactory);
        displayTextAlignmentComboBox.setButtonCell(displayTextAlignmentComboBoxFactory.call(null));

        actionClientProperties = new ArrayList<>();

        displayNameTextField = new TextField();
        displayNameTextField.managedProperty().bind(displayNameTextField.visibleProperty());

        defaultIconFileTextField = new TextField();
        defaultIconFileTextField.textProperty().addListener((observableValue, s, t1) -> {
            try {
                if (!s.equals(t1) && t1.length() > 0) {
                    byte[] iconFileByteArray = Files.readAllBytes(new File(t1).toPath());

                    hideDefaultIconCheckBox.setDisable(false);
                    hideDefaultIconCheckBox.setSelected(false);
                    clearIconButton.setDisable(false);

                    action.setDefaultIcon(iconFileByteArray);
                    setSendIcon(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
            }
        });

        toggleOffIconFileTextField = new TextField();
        toggleOffIconFileTextField.textProperty().addListener((observableValue, s, t1) -> {
            try {
                if (!s.equals(t1) && t1.length() > 0) {
                    byte[] iconFileByteArray = Files.readAllBytes(new File(t1).toPath());

                    hideToggleOffIconCheckBox.setDisable(false);
                    hideToggleOffIconCheckBox.setSelected(false);
                    clearIconButton.setDisable(false);

                    action.setToggleOffIcon(iconFileByteArray);
                    setSendIcon(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
            }
        });


        toggleOnIconFileTextField = new TextField();
        toggleOnIconFileTextField.textProperty().addListener((observableValue, s, t1) -> {
            try {
                if (!s.equals(t1) && t1.length() > 0) {
                    byte[] iconFileByteArray = Files.readAllBytes(new File(t1).toPath());

                    hideToggleOnIconCheckBox.setDisable(false);
                    hideToggleOnIconCheckBox.setSelected(false);
                    clearIconButton.setDisable(false);

                    action.setToggleOnIcon(iconFileByteArray);
                    setSendIcon(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
            }
        });


        clearIconButton = new Button("Clear Icon");
        clearIconButton.managedProperty().bind(clearIconButton.visibleProperty());
        clearIconButton.setOnAction(event -> {

            hideDefaultIconCheckBox.setDisable(true);
            hideDefaultIconCheckBox.setSelected(false);

            hideToggleOffIconCheckBox.setDisable(true);
            hideToggleOffIconCheckBox.setSelected(false);

            hideToggleOnIconCheckBox.setDisable(true);
            hideToggleOnIconCheckBox.setSelected(false);

            clearIconButton.setDisable(true);

            setSendIcon(false);

            defaultIconFileTextField.clear();
            toggleOffIconFileTextField.clear();
            toggleOnIconFileTextField.clear();
        });

        hideDisplayTextCheckBox = new CheckBox("Hide");
        hideDisplayTextCheckBox.managedProperty().bind(hideDisplayTextCheckBox.visibleProperty());

        hideDefaultIconCheckBox = new CheckBox("Hide");

        hideToggleOnIconCheckBox = new CheckBox("Hide");

        hideToggleOffIconCheckBox = new CheckBox("Hide");

        actionBackgroundColourPicker = new ColorPicker();
        actionBackgroundColourPicker.managedProperty().bind(actionBackgroundColourPicker.visibleProperty());

        displayTextColourPicker = new ColorPicker();
        displayTextColourPicker.managedProperty().bind(displayTextColourPicker.visibleProperty());

        actionBackgroundColourTransparentCheckBox = new CheckBox("Default");
        actionBackgroundColourPicker.disableProperty()
                .bind(actionBackgroundColourTransparentCheckBox.selectedProperty());

        HBox.setMargin(actionBackgroundColourTransparentCheckBox, new Insets(0, 0, 0, 10));    
        

        displayTextColourDefaultCheckBox = new CheckBox("Default");
        displayTextColourPicker.disableProperty()
                .bind(displayTextColourDefaultCheckBox.selectedProperty());

        HBox.setMargin(displayTextColourDefaultCheckBox, new Insets(0, 0, 0, 10));

        HBox displayTextColourHBox = new HBox(new Label("Display Text Colour"), SpaceFiller.horizontal(), displayTextColourPicker,
                displayTextColourDefaultCheckBox);
        displayTextColourHBox.setAlignment(Pos.CENTER);
        displayTextColourHBox.setSpacing(5.0);

        HBox bgColourHBox = new HBox(new Label("Background Colour"), SpaceFiller.horizontal(), actionBackgroundColourPicker,
                actionBackgroundColourTransparentCheckBox);
        bgColourHBox.setAlignment(Pos.CENTER);
        bgColourHBox.setSpacing(5.0);

        HBox clearIconHBox = new HBox(clearIconButton);
        clearIconHBox.setAlignment(Pos.CENTER_RIGHT);

        displayTextFieldHBox = new HBoxInputBox("Display Name", displayNameTextField, hideDisplayTextCheckBox);


        HBox alignmentHBox = new HBox(new Label("Alignment"), SpaceFiller.horizontal(),
                displayTextAlignmentComboBox);



        normalToggleActionCommonPropsVBox = new VBox(
                displayTextColourHBox,
                alignmentHBox,
                bgColourHBox,
                clearIconHBox
        );

        normalToggleActionCommonPropsVBox.managedProperty().bind(normalToggleActionCommonPropsVBox.visibleProperty());
        normalToggleActionCommonPropsVBox.setSpacing(10.0);

        normalActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser("Icon", defaultIconFileTextField, hideDefaultIconCheckBox,
                        new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png", "*.gif"))
        );
        normalActionsPropsVBox.managedProperty().bind(normalActionsPropsVBox.visibleProperty());
        normalActionsPropsVBox.setSpacing(10.0);

        toggleActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser("Toggle Off Icon", toggleOffIconFileTextField, hideToggleOffIconCheckBox,
                        new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png", "*.gif")),

                new HBoxInputBoxWithFileChooser("Toggle On Icon", toggleOnIconFileTextField, hideToggleOnIconCheckBox,
                        new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png", "*.gif"))
        );

        toggleActionsPropsVBox.managedProperty().bind(toggleActionsPropsVBox.visibleProperty());
        toggleActionsPropsVBox.setSpacing(10.0);

        vbox.getChildren().addAll(displayTextFieldHBox,normalToggleActionCommonPropsVBox, normalActionsPropsVBox, toggleActionsPropsVBox, clientPropertiesVBox);

        vbox.setVisible(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasContent(Action.getDataFormat()) && action != null) {
                if (action.getActionType() == ActionType.COMBINE) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);

                    dragEvent.consume();
                }
            }
        });

        setOnDragDropped(dragEvent -> {
            try {
                Action newAction = (Action) dragEvent.getDragboard().getContent(Action.getDataFormat());

                if (newAction.getActionType() == ActionType.NORMAL) {
                    newAction.setLocation(new Location(-1, -1));

                    newAction.setParent(this.action.getID());

                    combineActionPropertiesPane.getCombineAction().addChild(newAction.getID());

                    addActionToCurrentClientProfile(newAction);

                    ClientConnection connection = ClientConnections.getInstance()
                            .getClientConnectionBySocketAddress(getClient().getRemoteSocketAddress());

                    connection.saveActionDetails(getClientProfile().getID(), newAction);

                    combineActionPropertiesPane.renderProps();

                    saveAction();
                }
            } catch (MinorException e) {
                exceptionAndAlertHandler.handleMinorException(e);
                e.printStackTrace();
            } catch (SevereException e) {
                exceptionAndAlertHandler.handleSevereException(e);
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
    }

    private VBox normalActionsPropsVBox;
    private VBox normalToggleActionCommonPropsVBox;
    private VBox toggleActionsPropsVBox;

    private HBox displayTextFieldHBox;

    private Client client;
    private ClientProfile clientProfile;

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    public void setClientProfile(ClientProfile clientProfile) {
        this.clientProfile = clientProfile;
    }

    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    public void setActionHeadingLabelText(String text) {
        actionHeadingLabel.setText(text);
    }

    private Action action;

    public Action getAction() {
        return action;
    }

    private ActionBox actionBox;

    public ActionBox getActionBox() {
        return actionBox;
    }

    @Override
    public void onActionClicked(Action action, ActionBox actionBox) throws MinorException {
        this.action = action;
        this.actionBox = actionBox;

        logger.info("action Display text : "+action.getDisplayText());
        clear();

        renderActionProperties();
    }

    private TextField displayNameTextField;
    private CheckBox hideDisplayTextCheckBox;

    private CheckBox hideDefaultIconCheckBox;
    private TextField defaultIconFileTextField;

    private CheckBox hideToggleOnIconCheckBox;
    private TextField toggleOnIconFileTextField;

    private CheckBox hideToggleOffIconCheckBox;
    private TextField toggleOffIconFileTextField;

    private Button clearIconButton;
    private ColorPicker actionBackgroundColourPicker;
    private ColorPicker displayTextColourPicker;
    private CheckBox actionBackgroundColourTransparentCheckBox;
    private CheckBox displayTextColourDefaultCheckBox;
    private ComboBox<DisplayTextAlignment> displayTextAlignmentComboBox;

    public void clear()
    {
        sendIcon = false;
        actionClientProperties.clear();
        displayNameTextField.clear();


        defaultIconFileTextField.clear();
        toggleOffIconFileTextField.clear();
        toggleOnIconFileTextField.clear();


        clientPropertiesVBox.getChildren().clear();
        vbox.setVisible(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        buttonBar.setVisible(false);
        setActionHeadingLabelText("");

        actionBackgroundColourPicker.setValue(Color.WHITE);
        displayTextColourPicker.setValue(Color.WHITE);
    }

    boolean isCombineChild = false;

    public boolean isCombineChild() {
        return isCombineChild;
    }

    public void renderActionProperties() throws MinorException
    {

        //Combine Child action
        isCombineChild = action.getLocation().getCol() == -1;

        displayNameTextField.setText(action.getDisplayText());

        if(isCombineChild)
        {
            setReturnButtonForCombineActionChildVisible(true);
            normalActionsPropsVBox.setVisible(false);
            normalToggleActionCommonPropsVBox.setVisible(false);
            hideDisplayTextCheckBox.setSelected(false);
            hideDisplayTextCheckBox.setVisible(false);
        }
        else
        {
            normalToggleActionCommonPropsVBox.setVisible(true);

            if(getAction().getActionType() == ActionType.NORMAL)
            {
                normalActionsPropsVBox.setVisible(true);
                toggleActionsPropsVBox.setVisible(false);


                boolean doesDefaultExist = action.getIcons().containsKey("default");
                boolean isDefaultHidden = !action.getCurrentIconState().equals("default");

                if(!doesDefaultExist)
                    isDefaultHidden = false;

                hideDefaultIconCheckBox.setDisable(!doesDefaultExist);
                hideDefaultIconCheckBox.setSelected(isDefaultHidden);
            }
            else
            {
                normalActionsPropsVBox.setVisible(false);
                toggleActionsPropsVBox.setVisible(true);


                boolean doesToggleOnExist = action.getIcons().containsKey("toggle_on");
                boolean isToggleOnHidden = action.getCurrentIconState().contains("toggle_on");


                if(!doesToggleOnExist)
                    isToggleOnHidden = false;

                hideToggleOnIconCheckBox.setDisable(!doesToggleOnExist);
                hideToggleOnIconCheckBox.setSelected(isToggleOnHidden);




                boolean doesToggleOffExist = action.getIcons().containsKey("toggle_off");
                boolean isToggleOffHidden = action.getCurrentIconState().contains("toggle_off");



                if(!doesToggleOffExist)
                    isToggleOffHidden = false;

                hideToggleOffIconCheckBox.setDisable(!doesToggleOffExist);
                hideToggleOffIconCheckBox.setSelected(isToggleOffHidden);
            }


            setReturnButtonForCombineActionChildVisible(false);
            hideDisplayTextCheckBox.setVisible(true);
            setFolderButtonVisible(action.getActionType().equals(ActionType.FOLDER));

            displayTextAlignmentComboBox.getSelectionModel().select(action.getDisplayTextAlignment());

            if(!action.getBgColourHex().isEmpty())
                actionBackgroundColourPicker.setValue(Color.valueOf(action.getBgColourHex()));
            else
                actionBackgroundColourTransparentCheckBox.setSelected(true);
             


            if(!action.getDisplayTextFontColourHex().isEmpty())
                displayTextColourPicker.setValue(Color.valueOf(action.getDisplayTextFontColourHex()));
            else
                displayTextColourDefaultCheckBox.setSelected(true);


            hideDisplayTextCheckBox.setSelected(!action.isShowDisplayText());

            clearIconButton.setDisable(!action.isHasIcon());
        }



        buttonBar.setVisible(true);
        vbox.setVisible(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE)
        {
            if(action.isInvalid())
                setActionHeadingLabelText("Invalid action ("+action.getModuleName()+")");
            else
                setActionHeadingLabelText(action.getName());
        }
        else if(action.getActionType() == ActionType.COMBINE)
            setActionHeadingLabelText("Combine action");
        else if(action.getActionType() == ActionType.FOLDER)
            setActionHeadingLabelText("Folder action");


        if(!action.isInvalid())
        {
            if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE)
                renderClientProperties();
            else if(action.getActionType() == ActionType.COMBINE)
                renderCombineActionProperties();
        }
    }

    private CombineActionPropertiesPane combineActionPropertiesPane;

    public CombineActionPropertiesPane getCombineActionPropertiesPane() {
        return combineActionPropertiesPane;
    }

    public void setReturnButtonForCombineActionChildVisible(boolean visible)
    {
        returnButtonForCombineActionChild.setVisible(visible);
    }

    public void renderCombineActionProperties()
    {
        try
        {
            combineActionPropertiesPane = new CombineActionPropertiesPane(getActionAsCombineAction(action),
                    getClientProfile(),
                    this
            );

            clientPropertiesVBox.getChildren().add(combineActionPropertiesPane);
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public FolderAction getActionAsFolderAction(Action action)
    {
        FolderAction folderAction = new FolderAction();
        folderAction.setDisplayText(action.getDisplayText());
        folderAction.setName(action.getName());
        folderAction.setID(action.getID());
        folderAction.setLocation(action.getLocation());
        folderAction.setBgColourHex(action.getBgColourHex());
        folderAction.setParent(action.getParent());
        folderAction.getClientProperties().set(action.getClientProperties());
        folderAction.setDisplayTextAlignment(action.getDisplayTextAlignment());
        folderAction.setIcons(action.getIcons());
        folderAction.setCurrentIconState(action.getCurrentIconState());
        folderAction.setDisplayTextFontColourHex(action.getDisplayTextFontColourHex());

        return folderAction;
    }

    public CombineAction getActionAsCombineAction(Action action)
    {
        CombineAction combineAction = new CombineAction();
        combineAction.setDisplayText(action.getDisplayText());
        combineAction.setName(action.getName());
        combineAction.setID(action.getID());
        combineAction.setLocation(action.getLocation());
        combineAction.setBgColourHex(action.getBgColourHex());
        combineAction.setParent(action.getParent());
        combineAction.getClientProperties().set(action.getClientProperties());
        combineAction.setDisplayTextAlignment(action.getDisplayTextAlignment());
        combineAction.setIcons(action.getIcons());
        combineAction.setCurrentIconState(action.getCurrentIconState());
        combineAction.setDisplayTextFontColourHex(action.getDisplayTextFontColourHex());

        return combineAction;
    }

    @Override
    public void onOpenFolderButtonClicked()
    {
        FolderAction folderAction = getActionAsFolderAction(action);
        actionBox.getActionGridPaneListener().renderFolder(folderAction);
        clear();
    }

    @Override
    public Window getCurrentWindow() {
        return getScene().getWindow();
    }

    private ArrayList<UIPropertyBox> actionClientProperties;

    private TextField delayBeforeRunningTextField;

    public void renderClientProperties() throws MinorException
    {

        delayBeforeRunningTextField = new TextField();
        delayBeforeRunningTextField.setText(getAction().getDelayBeforeExecuting()+"");

        clientPropertiesVBox.getChildren().add(
                new HBoxInputBox("Delay before running (milli-seconds)", delayBeforeRunningTextField, 100)
        );

        for(int i =0;i< action.getClientProperties().getSize(); i++)
        {
            Property eachProperty = action.getClientProperties().get().get(i);

            if(!eachProperty.isVisible())
                continue;

            Label label = new Label(eachProperty.getDisplayName());

            HBox hBox = new HBox(label);
            hBox.setSpacing(5.0);
            hBox.setAlignment(Pos.CENTER_LEFT);

            Node controlNode = null;

            if(eachProperty.getHelpLink() != null)
            {
                Button helpButton = new Button();
                FontIcon questionIcon = new FontIcon("fas-question");
                helpButton.setGraphic(questionIcon);

                helpButton.setOnAction(event -> {
                    hostServices.showDocument(eachProperty.getHelpLink());
                });

                hBox.getChildren().add(helpButton);

                hBox.getChildren().add(controlNode);
            }

            hBox.getChildren().add(SpaceFiller.horizontal());


            if(eachProperty.getControlType() == ControlType.COMBO_BOX)
            {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.getItems().addAll(eachProperty.getListValue());
                comboBox.getSelectionModel().select(eachProperty.getSelectedIndex());


                controlNode = comboBox;

                hBox.getChildren().add(controlNode);
            }
            else if(eachProperty.getControlType() == ControlType.FILE_PATH)
            {
                TextField textField = new TextField(eachProperty.getRawValue());

                FileExtensionFilter[] fileExtensionFilters = eachProperty.getExtensionFilters();
                FileChooser.ExtensionFilter[] extensionFilters = new FileChooser.ExtensionFilter[fileExtensionFilters.length];

                for(int x = 0;x<fileExtensionFilters.length;x++)
                {
                    extensionFilters[x] = new FileChooser.ExtensionFilter(
                            fileExtensionFilters[x].getDescription(),
                            fileExtensionFilters[x].getExtensions()
                    );
                }

                hBox = new HBoxInputBoxWithFileChooser(eachProperty.getDisplayName(), textField, null,
                        extensionFilters);

                controlNode = textField;
            }
            else if(eachProperty.getControlType() == ControlType.TEXT_FIELD)
            {
                controlNode= new TextField(eachProperty.getRawValue());

                hBox.getChildren().add(controlNode);
            }
            else if(eachProperty.getControlType() == ControlType.TEXT_FIELD_MASKED)
            {
                PasswordField textField = new PasswordField();
                textField.setText(eachProperty.getRawValue());

                controlNode= textField;

                hBox.getChildren().add(controlNode);
            }
            else if(eachProperty.getControlType() == ControlType.TOGGLE)
            {
                ToggleButton toggleButton = new ToggleButton();
                toggleButton.setSelected(eachProperty.getBoolValue());

                if(eachProperty.getBoolValue())
                    toggleButton.setText("ON");
                else
                    toggleButton.setText("OFF");

                toggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                    if(t1)
                        toggleButton.setText("ON");
                    else
                        toggleButton.setText("OFF");
                });

                controlNode = toggleButton;

                hBox.getChildren().add(controlNode);
            }
            else if(eachProperty.getControlType() == ControlType.SLIDER_DOUBLE)
            {
                Slider slider = new Slider();
                slider.setValue(eachProperty.getDoubleValue());
                slider.setMax(eachProperty.getMaxDoubleValue());
                slider.setMin(eachProperty.getMinDoubleValue());

                controlNode = slider;

                hBox.getChildren().add(controlNode);
            }
            else if(eachProperty.getControlType() == ControlType.SLIDER_INTEGER)
            {
                Slider slider = new Slider();
                slider.setValue(eachProperty.getIntValue());

                slider.setMax(eachProperty.getMaxIntValue());
                slider.setMin(eachProperty.getMinIntValue());
                slider.setBlockIncrement(1.0);
                slider.setSnapToTicks(true);

                controlNode = slider;

                hBox.getChildren().add(controlNode);
            }



            UIPropertyBox clientProperty = new UIPropertyBox(i, eachProperty.getDisplayName(), controlNode,
                    eachProperty.getControlType(), eachProperty.getType(), eachProperty.isCanBeBlank());

            actionClientProperties.add(clientProperty);

            clientPropertiesVBox.getChildren().add(hBox);
        }
    }

    public void onSaveButtonClicked()
    {
        try {
            // saveButton.setDisable(true);
            // deleteButton.setDisable(true);

            validateForm();

            saveAction();
        }
        catch (MinorException e)
        {
            exceptionAndAlertHandler.handleMinorException(e);
        }

    }

    private boolean sendIcon = false;

    @Override
    public void setSendIcon(boolean sendIcon)
    {
        this.sendIcon = sendIcon;
    }

    public void addActionToCurrentClientProfile(Action newAction) throws CloneNotSupportedException {
        getClientProfile().addAction(newAction);
    }

    @Override
    public void saveAction(Action action, boolean runAsync)
    {
        new OnSaveActionTask(
            ClientConnections.getInstance().getClientConnectionBySocketAddress(
                getClient().getRemoteSocketAddress()
            ),
            action,
            delayBeforeRunningTextField.getText(),
            displayNameTextField.getText(),
            isCombineChild(),
            !hideDisplayTextCheckBox.isSelected(),
            displayTextColourDefaultCheckBox.isSelected(), 
            "#" + displayTextColourPicker.getValue().toString().substring(2),
            clearIconButton.isDisable(),
            hideDefaultIconCheckBox.isSelected(),
            hideToggleOffIconCheckBox.isSelected(),
            hideToggleOnIconCheckBox.isSelected(),
            displayTextAlignmentComboBox.getSelectionModel().getSelectedItem(),
            actionBackgroundColourTransparentCheckBox.isSelected(),
            "#" + actionBackgroundColourPicker.getValue().toString().substring(2),
            getCombineActionPropertiesPane(), 
            clientProfile, sendIcon, actionBox, actionClientProperties, exceptionAndAlertHandler,
            saveButton, deleteButton, runAsync
        );
    }

    @Override
    public void saveAction()
    {
        saveAction(action, true);
    }

    public void setFolderButtonVisible(boolean visible)
    {
        openFolderButton.setVisible(visible);
    }

    public void validateForm() throws MinorException
    {
        String displayNameStr = displayNameTextField.getText();

        StringBuilder finalErrors = new StringBuilder();

        if(displayNameStr.isBlank())
        {
            finalErrors.append(" * Display Name cannot be blank\n");
        }

        if(!isCombineChild())
        {
            if(action.getActionType() == ActionType.NORMAL)
            {
                if(action.isHasIcon())
                {
                    if(hideDisplayTextCheckBox.isSelected() && hideDefaultIconCheckBox.isSelected())
                    {
                        finalErrors.append(" * Both Icon and display text check box cannot be hidden.\n");
                    }
                }
                else
                {
                    if(hideDisplayTextCheckBox.isSelected())
                        finalErrors.append(" * Display Text cannot be hidden, since there is also no icon.\n");
                }
            }
        }

        if(action.getActionType() == ActionType.NORMAL)
        {
            try
            {
                int n = Integer.parseInt(delayBeforeRunningTextField.getText());

                if (n<0)
                {
                    finalErrors.append(" * Sleep should be greater than 0.\n");
                }
            }
            catch (Exception e)
            {
                finalErrors.append(" * Sleep should be a number.\n");
            }
        }



        for (UIPropertyBox clientProperty : actionClientProperties) {

            Node controlNode = clientProperty.getControlNode();

            if (clientProperty.getControlType() == ControlType.TEXT_FIELD ||
            clientProperty.getControlType() == ControlType.FILE_PATH)
            {
                String value = ((TextField) controlNode).getText();
                if(clientProperty.getType() == Type.INTEGER)
                {
                    try
                    {
                        Integer.parseInt(value);
                    }
                    catch (NumberFormatException e)
                    {
                        finalErrors.append("        -> ").append(clientProperty.getDisplayName()).append(" must be integer.\n");
                    }
                }
                else
                {
                    if(value.isBlank() && !clientProperty.isCanBeBlank())
                        finalErrors.append("        -> ").append(clientProperty.getDisplayName()).append(" cannot be blank.\n");
                }
            }
        }


        if(!finalErrors.toString().isEmpty())
        {
            throw new MinorException("You made mistakes",
                    finalErrors.toString());
        }
    }

    public void onDeleteButtonClicked()
    {
        new OnDeleteActionTask(
            ClientConnections.getInstance().getClientConnectionBySocketAddress(
                getClient().getRemoteSocketAddress()
            ),
            action,
            isCombineChild(),
            getCombineActionPropertiesPane(), 
            clientProfile, actionBox, this, exceptionAndAlertHandler,
            !isCombineChild
        );
    }
}