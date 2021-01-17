package com.StreamPi.Server.Window.Dashboard.ActionsDetailPane;

import com.StreamPi.Server.UIPropertyBox.UIPropertyBox;
import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.DisplayTextAlignment;
import com.StreamPi.ActionAPI.Action.Location;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.ActionProperty.Property.ControlType;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.ActionProperty.Property.Type;
import com.StreamPi.ActionAPI.OtherActions.CombineAction;
import com.StreamPi.ActionAPI.OtherActions.FolderAction;
import com.StreamPi.Server.Client.Client;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ClientConnection;
import com.StreamPi.Server.Connection.ClientConnections;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionBox;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.FormHelper.HBoxInputBox;
import com.StreamPi.Util.FormHelper.HBoxInputBoxWithFileChooser;
import com.StreamPi.Util.FormHelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ActionDetailsPane extends VBox implements ActionDetailsPaneListener {

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

    public ActionDetailsPane(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices) {
        this.hostServices = hostServices;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        logger = Logger.getLogger(ActionDetailsPane.class.getName());

        setSpacing(10.0);

        // VBox.setVgrow(this, Priority.SOMETIMES);

        clientPropertiesVBox = new VBox();
        clientPropertiesVBox.setSpacing(10.0);

        vbox = new VBox();
        vbox.setPadding(new Insets(0, 25, 0, 5));
        vbox.getStyleClass().add("action_details_pane_vbox");

        vbox.setSpacing(10.0);

        getStyleClass().add("action_details_pane");
        // setPadding(new Insets(5,50,5,50));
        // setMinHeight(245);

        scrollPane = new ScrollPane();
        VBox.setMargin(scrollPane, new Insets(0, 0, 0, 10));

        scrollPane.getStyleClass().add("action_details_pane_scroll_pane");

        setMinHeight(310);
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

        deleteButton = new Button("Delete Action");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.setIconColor(Paint.valueOf("#FF0000"));
        deleteButton.setTextFill(Paint.valueOf("#FF0000"));
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
        buttonBar.setPadding(new Insets(10, 10, 10, 0));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setVisible(false);
        buttonBar.setSpacing(10.0);

        actionHeadingLabel = new Label();

        actionHeadingLabel.getStyleClass().add("action_details_pane_action_heading_label");

        HBox headingHBox = new HBox(actionHeadingLabel);

        headingHBox.setPadding(new Insets(5, 10, 0, 10));

        getChildren().addAll(headingHBox, scrollPane, buttonBar);

        displayTextAlignmentComboBox = new ComboBox<>(FXCollections.observableArrayList(DisplayTextAlignment.TOP,
                DisplayTextAlignment.CENTER, DisplayTextAlignment.BOTTOM));

        displayTextAlignmentComboBox.managedProperty().bind(displayTextAlignmentComboBox.visibleProperty());

        actionClientProperties = new ArrayList<>();

        displayNameTextField = new TextField();
        displayNameTextField.managedProperty().bind(displayNameTextField.visibleProperty());

        iconFileTextField = new TextField();
        iconFileTextField.managedProperty().bind(iconFileTextField.visibleProperty());
        iconFileTextField.textProperty().addListener((observableValue, s, t1) -> {
            try {
                if (!s.equals(t1) && t1.length() > 0) {
                    byte[] iconFileByteArray = Files.readAllBytes(new File(t1).toPath());

                    hideIconCheckBox.setDisable(false);
                    hideIconCheckBox.setSelected(false);
                    clearIconButton.setDisable(false);

                    System.out.println("ABABABABABBABABBABABABCCCCCCCCCCCCCCCCCC");

                    action.setIcon(iconFileByteArray);
                    setSendIcon(true);

                    System.out.println(action.getIconAsByteArray().length);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
            }
        });

        clearIconButton = new Button("Clear Icon");
        clearIconButton.managedProperty().bind(clearIconButton.visibleProperty());
        clearIconButton.setOnAction(event -> {

            hideIconCheckBox.setDisable(true);
            hideIconCheckBox.setSelected(false);

            clearIconButton.setDisable(true);
            iconFileTextField.clear();
        });

        hideDisplayTextCheckBox = new CheckBox("Hide");
        hideDisplayTextCheckBox.managedProperty().bind(hideDisplayTextCheckBox.visibleProperty());

        hideIconCheckBox = new CheckBox("Hide");
        hideIconCheckBox.managedProperty().bind(hideIconCheckBox.visibleProperty());

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

        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);

        HBox displayTextColourHBox = new HBox(new Label("Display Text Colour"), r1, displayTextColourPicker,
                displayTextColourDefaultCheckBox);
        displayTextColourHBox.setAlignment(Pos.CENTER);
        displayTextColourHBox.setSpacing(5.0);

        HBox bgColourHBox = new HBox(new Label("Background Colour"), r, actionBackgroundColourPicker,
                actionBackgroundColourTransparentCheckBox);
        bgColourHBox.setAlignment(Pos.CENTER);
        bgColourHBox.setSpacing(5.0);

        HBox clearIconHBox = new HBox(clearIconButton);
        clearIconHBox.setAlignment(Pos.CENTER_RIGHT);

        displayTextFieldHBox = new HBoxInputBox("Display Name", displayNameTextField, hideDisplayTextCheckBox);

        normalActionsPropsVBox = new VBox(displayTextColourHBox,

                new HBox(new Label("Alignment"), new SpaceFiller(SpaceFiller.FillerType.HBox),
                        displayTextAlignmentComboBox),

                new HBoxInputBoxWithFileChooser("Icon", iconFileTextField, hideIconCheckBox,
                        new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png", "*.gif")),

                clearIconHBox, bgColourHBox);
        normalActionsPropsVBox.managedProperty().bind(normalActionsPropsVBox.visibleProperty());
        normalActionsPropsVBox.setSpacing(10.0);

        vbox.getChildren().addAll(displayTextFieldHBox, normalActionsPropsVBox, clientPropertiesVBox);

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

        System.out.println(action.getActionType());


        logger.info("Action Display text : "+action.getDisplayText());
        clear();

        renderActionProperties();
    }

    private TextField displayNameTextField;
    private CheckBox hideDisplayTextCheckBox;
    private CheckBox hideIconCheckBox;
    private TextField iconFileTextField;
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
        iconFileTextField.clear();
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

        if(action.getLocation().getCol() == -1) //Combine Child action
            isCombineChild = true;
        else
            isCombineChild = false;           

        displayNameTextField.setText(action.getDisplayText());

        System.out.println(action.getDisplayText()+"@@@@::::"+isCombineChild);

        if(isCombineChild)
        {
            setReturnButtonForCombineActionChildVisible(true);
            normalActionsPropsVBox.setVisible(false);
            hideDisplayTextCheckBox.setSelected(false);
            hideDisplayTextCheckBox.setVisible(false);
        }
        else
        {
            normalActionsPropsVBox.setVisible(true);
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


            hideIconCheckBox.setDisable(!action.isHasIcon());

            hideIconCheckBox.setSelected(!action.isShowIcon());

            if(!action.isHasIcon())
            {
                hideIconCheckBox.setSelected(false);
            }

            clearIconButton.setDisable(!action.isHasIcon());
        }



        buttonBar.setVisible(true);
        vbox.setVisible(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        if(action.getActionType() == ActionType.NORMAL)
        {
            if(action.isInvalid())
                setActionHeadingLabelText("Invalid Action ("+action.getModuleName()+")");
            else
                setActionHeadingLabelText(action.getName());
        }
        else if(action.getActionType() == ActionType.COMBINE)
            setActionHeadingLabelText("Combine Action");
        else if(action.getActionType() == ActionType.FOLDER)
            setActionHeadingLabelText("Folder Action");


        if(!action.isInvalid())
        {
            if(action.getActionType() == ActionType.NORMAL)
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
        try {
            logger.info("@@@@@ : "+action.getClientProperties().getSize());
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
        folderAction.setShowIcon(action.isShowIcon());
        folderAction.setHasIcon(action.isHasIcon());
        if(folderAction.isHasIcon())
            folderAction.setIcon(action.getIconAsByteArray());
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
        combineAction.setShowIcon(action.isShowIcon());
        combineAction.setHasIcon(action.isHasIcon());
        if(combineAction.isHasIcon())
            combineAction.setIcon(action.getIconAsByteArray());
        combineAction.setDisplayTextFontColourHex(action.getDisplayTextFontColourHex());

        for(Property prop : combineAction.getClientProperties().get())
        {
            System.out.println("PROP : "+prop.getName()+","+prop.getRawValue());
        }
        return combineAction;
    }

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

    public void renderClientProperties() throws MinorException
    {
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
            }

            hBox.getChildren().add(new SpaceFiller(SpaceFiller.FillerType.HBox));

            if(eachProperty.getControlType() == ControlType.COMBO_BOX)
            {
                ComboBox<String> comboBox = new ComboBox<>();
                comboBox.getItems().addAll(eachProperty.getListValue());
                comboBox.getSelectionModel().select(eachProperty.getSelectedIndex());


                controlNode = comboBox;
            }
            else if(eachProperty.getControlType() == ControlType.TEXT_FIELD)
            {
                TextField textField = new TextField(eachProperty.getRawValue());

                controlNode= textField;
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
            }
            else if(eachProperty.getControlType() == ControlType.SLIDER_DOUBLE)
            {
                Slider slider = new Slider();
                slider.setValue(eachProperty.getDoubleValue());
                slider.setMax(eachProperty.getMaxDoubleValue());
                slider.setMin(eachProperty.getMinDoubleValue());

                controlNode = slider;
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
            }


            hBox.getChildren().add(controlNode);

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
            displayNameTextField.getText(),
            isCombineChild(),
            !hideDisplayTextCheckBox.isSelected(),
            displayTextColourDefaultCheckBox.isSelected(), 
            "#" + displayTextColourPicker.getValue().toString().substring(2),
            clearIconButton.isDisable(),
            !hideIconCheckBox.isSelected(),
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
            if(action.isHasIcon())
            {
                if(hideDisplayTextCheckBox.isSelected() && hideIconCheckBox.isSelected())
                    finalErrors.append(" * Both Icon and display text check box cannot be hidden.\n");
            }
            else
            {
                if(hideDisplayTextCheckBox.isSelected())
                    finalErrors.append(" * Display Text cannot be hidden, since there is also no icon.\n");
            }

        }



        for (UIPropertyBox clientProperty : actionClientProperties) {

            Node controlNode = clientProperty.getControlNode();

            if (clientProperty.getControlType() == ControlType.TEXT_FIELD)
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