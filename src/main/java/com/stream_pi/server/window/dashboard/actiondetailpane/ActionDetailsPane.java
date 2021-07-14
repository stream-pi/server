package com.stream_pi.server.window.dashboard.actiondetailpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.property.*;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.controller.ActionDataFormats;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionGridPaneListener;
import com.stream_pi.server.window.helper.Helper;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import com.stream_pi.util.uihelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class ActionDetailsPane extends VBox implements ActionDetailsPaneListener
{

    private ScrollPane scrollPane;

    private VBox vbox;
    private VBox clientPropertiesVBox;

    private Button saveButton;
    private Button deleteButton;
    private Button openFolderButton;
    private Button resetToDefaultsButton;

    private HBox buttonBar;

    private VBox pluginExtraButtonBar;

    private Label actionHeadingLabel;

    private Logger logger;

    private Button returnButtonForCombineActionChild;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private HostServices hostServices;

    private ActionGridPaneListener actionGridPaneListener;

    private HBox clearIconHBox;

    public ActionDetailsPane(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices,
                             ActionGridPaneListener actionGridPaneListener)
    {
        this.hostServices = hostServices;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.actionGridPaneListener = actionGridPaneListener;

        logger = Logger.getLogger(ActionDetailsPane.class.getName());

        setSpacing(10.0);

        clientPropertiesVBox = new VBox();
        clientPropertiesVBox.setSpacing(10.0);

        vbox = new VBox();
        //vbox.setPadding(new Insets(0, 25, 0, 5));
        vbox.getStyleClass().add("action_details_pane_vbox");

        vbox.setSpacing(10.0);

        pluginExtraButtonBar = new VBox();
        pluginExtraButtonBar.setSpacing(10.0);


        getStyleClass().add("action_details_pane");

        scrollPane = new ScrollPane();
        VBox.setMargin(scrollPane, new Insets(0, 0, 0, 10));

        scrollPane.getStyleClass().add("action_details_pane_scroll_pane");

        setMinHeight(210);
        scrollPane.setContent(vbox);

        vbox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));
        scrollPane.prefWidthProperty().bind(widthProperty());

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        openFolderButton = new Button("Open Folder");
        openFolderButton.managedProperty().bind(openFolderButton.visibleProperty());
        FontIcon folderOpenIcon = new FontIcon("far-folder-open");
        openFolderButton.setGraphic(folderOpenIcon);
        openFolderButton.setOnAction(event -> onOpenFolderButtonClicked());

        saveButton = new Button("Save");
        saveButton.getStyleClass().add("action_details_pane_save_button");
        FontIcon syncIcon = new FontIcon("far-save");
        syncIcon.getStyleClass().add("action_details_save_delete_button_icon");
        saveButton.setGraphic(syncIcon);
        saveButton.setOnAction(event -> onSaveButtonClicked());

        deleteButton = new Button();
        deleteButton.getStyleClass().add("action_details_pane_delete_button");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.getStyleClass().add("action_details_pane_delete_button_icon");
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(event -> onDeleteButtonClicked());


        resetToDefaultsButton = new Button("Reset");
        resetToDefaultsButton.managedProperty().bind(resetToDefaultsButton.visibleProperty());
        resetToDefaultsButton.getStyleClass().add("action_details_pane_reset_button");
        FontIcon resetToDefaultsIcon = new FontIcon("fas-sync-alt");
        resetToDefaultsIcon.getStyleClass().add("action_details_pane_reset_button_icon");
        resetToDefaultsButton.setGraphic(resetToDefaultsIcon);
        resetToDefaultsButton.setOnAction(event -> onResetToDefaultsButtonClicked());

        returnButtonForCombineActionChild = new Button("Return");
        returnButtonForCombineActionChild.setGraphic(new FontIcon("fas-caret-left"));
        returnButtonForCombineActionChild.managedProperty().bind(returnButtonForCombineActionChild.visibleProperty());
        returnButtonForCombineActionChild.setOnAction(event -> {
            try
            {
                onActionClicked(getClientProfile().getActionByID(getAction().getParent()), getActionBox());
            } catch (MinorException e)
            {
                e.printStackTrace();
            }
        });

        buttonBar = new HBox(openFolderButton, resetToDefaultsButton, returnButtonForCombineActionChild, saveButton, deleteButton);
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

        displayNameFontSizeTextField = new TextField();
        displayNameFontSizeTextField.managedProperty().bind(displayNameFontSizeTextField.visibleProperty());

        defaultIconFileTextField = new TextField();
        defaultIconFileTextField.textProperty().addListener((observableValue, s, t1) -> {
            try {
                if (!s.equals(t1) && t1.length() > 0) {
                    byte[] iconFileByteArray = Files.readAllBytes(new File(t1).toPath());

                    hideDefaultIconCheckBox.setDisable(false);
                    hideDefaultIconCheckBox.setSelected(false);
                    clearIconButton.setDisable(false);

                    getAction().addIcon("default", iconFileByteArray);
                    getAction().setCurrentIconState("default");
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

                    getAction().addIcon("toggle_off", iconFileByteArray);
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

                    getAction().addIcon("toggle_on", iconFileByteArray);
                    setSendIcon(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
            }
        });


        clearIconButton = new Button("Clear Icon");
        clearIconButton.managedProperty().bind(clearIconButton.visibleProperty());
        clearIconButton.setOnAction(event ->
        {
            hideDefaultIconCheckBox.setDisable(true);
            hideDefaultIconCheckBox.setSelected(false);
            hideToggleOffIconCheckBox.setDisable(true);
            hideToggleOffIconCheckBox.setSelected(false);

            hideToggleOnIconCheckBox.setDisable(true);
            hideToggleOnIconCheckBox.setSelected(false);

            clearIconButton.setDisable(true);

            hideDisplayTextCheckBox.setSelected(false);

            setSendIcon(false);

            defaultIconFileTextField.clear();
            toggleOffIconFileTextField.clear();
            toggleOnIconFileTextField.clear();
        });

        hideDisplayTextCheckBox = new CheckBox("Hide");
        hideDisplayTextCheckBox.managedProperty().bind(hideDisplayTextCheckBox.visibleProperty());

        displayNameFontSizeCheckBox = new CheckBox("Default");
        displayNameFontSizeCheckBox.managedProperty().bind(displayNameFontSizeCheckBox.visibleProperty());
        displayNameFontSizeTextField.disableProperty().bind(displayNameFontSizeCheckBox.selectedProperty());

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

        clearIconHBox = new HBox(clearIconButton);
        clearIconHBox.managedProperty().bind(clearIconHBox.visibleProperty());
        clearIconHBox.setAlignment(Pos.CENTER_RIGHT);

        HBox.setMargin(hideDisplayTextCheckBox, new Insets(0, 0, 0, 45));
        HBoxInputBox s = new HBoxInputBox("Name", displayNameTextField);
        HBox.setHgrow(s, Priority.ALWAYS);
        displayTextFieldHBox = new HBox(s, hideDisplayTextCheckBox);


        HBox.setMargin(displayNameFontSizeCheckBox, new Insets(0,0,0,30));
        HBoxInputBox t = new HBoxInputBox("Font Size", displayNameFontSizeTextField);
        HBox.setHgrow(t, Priority.ALWAYS);
        displayNameLabelFontSizeTextFieldHBox = new HBox(t, displayNameFontSizeCheckBox);
        displayNameLabelFontSizeTextFieldHBox.managedProperty().bind(displayNameLabelFontSizeTextFieldHBox.visibleProperty());


        HBox alignmentHBox = new HBox(new Label("Alignment"), SpaceFiller.horizontal(),
                displayTextAlignmentComboBox);



        normalToggleActionCommonPropsVBox = new VBox(
                displayTextColourHBox,
                alignmentHBox,
                bgColourHBox
        );

        normalToggleActionCommonPropsVBox.managedProperty().bind(normalToggleActionCommonPropsVBox.visibleProperty());
        normalToggleActionCommonPropsVBox.setSpacing(10.0);

        FileChooser.ExtensionFilter iconExtensions = new FileChooser.ExtensionFilter("Images", "*.jpeg", "*.jpg", "*.png", "*.gif");

        normalActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser("Icon", defaultIconFileTextField, hideDefaultIconCheckBox,
                        iconExtensions)
        );

        normalActionsPropsVBox.managedProperty().bind(normalActionsPropsVBox.visibleProperty());
        normalActionsPropsVBox.setSpacing(10.0);

        toggleActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser("Toggle Off Icon", toggleOffIconFileTextField, hideToggleOffIconCheckBox,
                        iconExtensions),

                new HBoxInputBoxWithFileChooser("Toggle On Icon", toggleOnIconFileTextField, hideToggleOnIconCheckBox,
                        iconExtensions)
        );

        toggleActionsPropsVBox.managedProperty().bind(toggleActionsPropsVBox.visibleProperty());
        toggleActionsPropsVBox.setSpacing(10.0);

        vbox.getChildren().addAll(displayTextFieldHBox, displayNameLabelFontSizeTextFieldHBox,
                normalToggleActionCommonPropsVBox,
                normalActionsPropsVBox, toggleActionsPropsVBox,
                clearIconHBox, clientPropertiesVBox,
                pluginExtraButtonBar);

        vbox.setVisible(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasContent(ActionDataFormats.ACTION_TYPE) && action != null) {
                if (getAction().getActionType() == ActionType.COMBINE) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);

                    dragEvent.consume();
                }
            }
        });

        setOnDragDropped(dragEvent -> {
            try {

                Dragboard db = dragEvent.getDragboard();

                ActionType actionType = (ActionType) db.getContent(ActionDataFormats.ACTION_TYPE);

                if(actionType == ActionType.NORMAL || actionType == ActionType.TOGGLE)
                {
                    ExternalPlugin newAction = actionGridPaneListener.createNewActionFromExternalPlugin(
                            (String) db.getContent(ActionDataFormats.MODULE_NAME)
                    );

                    boolean isNew = (boolean) db.getContent(ActionDataFormats.IS_NEW);

                    if(isNew)
                    {
                        newAction.setDisplayText("Untitled Action");
                        newAction.setShowDisplayText(true);
                        newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);

                        if(actionType == ActionType.TOGGLE)
                            newAction.setCurrentIconState("false__false");
                    }
                    else
                    {
                        newAction.setClientProperties((ClientProperties) db.getContent(ActionDataFormats.CLIENT_PROPERTIES));
                        newAction.setIcons((HashMap<String, byte[]>) db.getContent(ActionDataFormats.ICONS));
                        newAction.setCurrentIconState((String) db.getContent(ActionDataFormats.CURRENT_ICON_STATE));
                        newAction.setBgColourHex((String) db.getContent(ActionDataFormats.BACKGROUND_COLOUR));
                        newAction.setDisplayTextFontColourHex((String) db.getContent(ActionDataFormats.DISPLAY_TEXT_FONT_COLOUR));
                        newAction.setDisplayText((String) db.getContent(ActionDataFormats.DISPLAY_TEXT));
                        newAction.setDisplayTextAlignment((DisplayTextAlignment) db.getContent(ActionDataFormats.DISPLAY_TEXT_ALIGNMENT));
                        newAction.setShowDisplayText((boolean) db.getContent(ActionDataFormats.DISPLAY_TEXT_SHOW));
                    }

                    newAction.setLocation(new Location(-1, -1));

                    newAction.setParent(getAction().getID());
                    newAction.setProfileID(actionGridPaneListener.getCurrentProfile().getID());
                    newAction.setSocketAddressForClient(actionGridPaneListener.getClientConnection().getRemoteSocketAddress());

                    try
                    {
                        newAction.onActionCreate();
                    }
                    catch (MinorException e)
                    {
                        e.setTitle("Error");
                        exceptionAndAlertHandler.handleMinorException("onCreate() failed for "+getAction().getModuleName()+"\n\n"+e.getMessage(), e);
                    }

                    combineActionPropertiesPane.getCombineAction().addChild(newAction.getID());

                    addActionToCurrentClientProfile(newAction);

                    ClientConnection connection = ClientConnections.getInstance()
                            .getClientConnectionBySocketAddress(getClient().getRemoteSocketAddress());

                    connection.saveActionDetails(getClientProfile().getID(), newAction);

                    combineActionPropertiesPane.renderProps();

                    saveAction(true, false);



                }

            } catch (MinorException e) {
                exceptionAndAlertHandler.handleMinorException(e);
                e.printStackTrace();
            } catch (SevereException e) {
                exceptionAndAlertHandler.handleSevereException(e);
                e.printStackTrace();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    private void onResetToDefaultsButtonClicked()
    {
        StreamPiAlert streamPiAlert = new StreamPiAlert(
                "Warning",
                "Are you sure you want to reset the action?",
                StreamPiAlertType.WARNING
        );

        String optionYes = "Yes";
        String optionNo = "No";

        streamPiAlert.setButtons(optionYes, optionNo);

        streamPiAlert.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(String s) {
                if(s.equals(optionYes))
                {
                    getAction().getClientProperties().resetToDefaults();

                    try
                    {
                        onActionClicked(getAction(), getActionBox());
                        saveAction(true, true);
                    }
                    catch (MinorException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(e);
                    }
                }
            }
        });

        streamPiAlert.show();
    }

    private VBox normalActionsPropsVBox;
    private VBox normalToggleActionCommonPropsVBox;
    private VBox toggleActionsPropsVBox;

    private HBox displayTextFieldHBox;
    private HBox displayNameLabelFontSizeTextFieldHBox;

    private ClientConnection clientConnection;
    private ClientProfile clientProfile;

    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public Client getClient()
    {
        return getClientConnection().getClient();
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

    public synchronized Action getAction() {
        return action;
    }

    private ActionBox actionBox;

    public ActionBox getActionBox() {
        return actionBox;
    }

    @Override
    public void onActionClicked(Action action, ActionBox actionBox) throws MinorException
    {
        clear();

        setAction(action);
        this.actionBox = actionBox;
        actionBox.setSelected(true);

        renderActionProperties();
    }



    public void refresh()
    {
        clear(false);
        try
        {
            renderActionProperties();
        }
        catch (MinorException e)
        {
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }

    private TextField displayNameTextField;
    private CheckBox hideDisplayTextCheckBox;

    private TextField displayNameFontSizeTextField;
    private CheckBox displayNameFontSizeCheckBox;

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

    public void clear(boolean actionNull)
    {
        sendIcon = false;
        actionClientProperties.clear();
        displayNameTextField.clear();

        displayNameFontSizeTextField.clear();

        defaultIconFileTextField.clear();
        toggleOffIconFileTextField.clear();
        toggleOnIconFileTextField.clear();


        clientPropertiesVBox.getChildren().clear();
        pluginExtraButtonBar.getChildren().clear();
        vbox.setVisible(false);

        normalActionsPropsVBox.setVisible(false);
        toggleActionsPropsVBox.setVisible(false);
        saveButton.setVisible(false);
        openFolderButton.setVisible(false);
        resetToDefaultsButton.setVisible(false);
        returnButtonForCombineActionChild.setVisible(false);

        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        buttonBar.setVisible(false);
        setActionHeadingLabelText("");

        actionBackgroundColourPicker.setValue(Color.WHITE);
        actionBackgroundColourTransparentCheckBox.setSelected(false);
        displayTextColourPicker.setValue(Color.WHITE);
        displayTextColourDefaultCheckBox.setSelected(false);

        if(actionNull)
        {
            action = null;

            if(actionBox !=null)
            {
                actionBox.setSelected(false);
                actionBox = null;
            }
        }
    }

    @Override
    public void clear()
    {
        clear(true);
    }

    boolean isCombineChild = false;

    public boolean isCombineChild() {
        return isCombineChild;
    }

    public void renderActionProperties() throws MinorException
    {

        //Combine Child action
        isCombineChild = getAction().getLocation().getCol() == -1;

        displayNameTextField.setText(getAction().getDisplayText());

        vbox.setVisible(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        buttonBar.setVisible(true);


        displayTextAlignmentComboBox.getSelectionModel().select(getAction().getDisplayTextAlignment());

        if(!getAction().getBgColourHex().isEmpty())
        {
            actionBackgroundColourPicker.setValue(Color.valueOf(getAction().getBgColourHex()));
            actionBackgroundColourTransparentCheckBox.setSelected(false);
        }
        else
        {
            actionBackgroundColourPicker.setValue(Color.WHITE);
            actionBackgroundColourTransparentCheckBox.setSelected(true);
        }



        if(!getAction().getDisplayTextFontColourHex().isEmpty())
        {
            displayTextColourPicker.setValue(Color.valueOf(getAction().getDisplayTextFontColourHex()));
            displayTextColourDefaultCheckBox.setSelected(false);
        }
        else
        {
            displayTextColourPicker.setValue(Color.WHITE);
            displayTextColourDefaultCheckBox.setSelected(true);
        }


        hideDisplayTextCheckBox.setSelected(!getAction().isShowDisplayText());

        if(getAction().getNameFontSize() == -1)
        {
            displayNameFontSizeCheckBox.setSelected(true);
            displayNameFontSizeTextField.clear();
        }
        else
        {
            displayNameFontSizeCheckBox.setSelected(false);
            displayNameFontSizeTextField.setText(getAction().getNameFontSize()+"");
        }



        if(getAction().isInvalid())
        {
            setActionHeadingLabelText("Invalid action ("+getAction().getModuleName()+")");
            return;
        }


        saveButton.setVisible(!getAction().isInvalid());

        if(isCombineChild)
        {
            setReturnButtonForCombineActionChildVisible(true);
            normalActionsPropsVBox.setVisible(false);
            normalToggleActionCommonPropsVBox.setVisible(false);
            hideDisplayTextCheckBox.setSelected(false);
            hideDisplayTextCheckBox.setVisible(false);

            displayNameLabelFontSizeTextFieldHBox.setVisible(false);

            clearIconHBox.setVisible(false);
        }
        else
        {
            clearIconHBox.setVisible(true);
            displayNameLabelFontSizeTextFieldHBox.setVisible(true);
            normalToggleActionCommonPropsVBox.setVisible(true);

            if(getAction().getActionType() == ActionType.TOGGLE)
            {
                normalActionsPropsVBox.setVisible(false);
                toggleActionsPropsVBox.setVisible(true);


                boolean doesToggleOnExist = getAction().getIcons().containsKey("toggle_on");
                boolean isToggleOnHidden = getAction().getCurrentIconState().contains("toggle_on");


                if(!doesToggleOnExist)
                    isToggleOnHidden = false;

                hideToggleOnIconCheckBox.setDisable(!doesToggleOnExist);
                hideToggleOnIconCheckBox.setSelected(isToggleOnHidden);




                boolean doesToggleOffExist = getAction().getIcons().containsKey("toggle_off");
                boolean isToggleOffHidden = getAction().getCurrentIconState().contains("toggle_off");



                if(!doesToggleOffExist)
                    isToggleOffHidden = false;

                hideToggleOffIconCheckBox.setDisable(!doesToggleOffExist);
                hideToggleOffIconCheckBox.setSelected(isToggleOffHidden);
            }
            else
            {
                normalActionsPropsVBox.setVisible(true);
                toggleActionsPropsVBox.setVisible(false);


                boolean doesDefaultExist = getAction().getIcons().containsKey("default");
                boolean isDefaultHidden = !getAction().getCurrentIconState().equals("default");

                if(!doesDefaultExist)
                    isDefaultHidden = false;

                hideDefaultIconCheckBox.setDisable(!doesDefaultExist);
                hideDefaultIconCheckBox.setSelected(isDefaultHidden);
            }

            setReturnButtonForCombineActionChildVisible(false);
            hideDisplayTextCheckBox.setVisible(true);

            setFolderButtonVisible(getAction().getActionType().equals(ActionType.FOLDER));
            setResetToDefaultsButtonVisible(!(getAction().getActionType().equals(ActionType.FOLDER) || getAction().getActionType().equals(ActionType.COMBINE)));

            clearIconButton.setDisable(!getAction().isHasIcon());
        }



        if(getAction().getActionType() == ActionType.NORMAL || getAction().getActionType() == ActionType.TOGGLE)
        {
            setActionHeadingLabelText(getAction().getName());
        }
        else if(getAction().getActionType() == ActionType.COMBINE)
        {
            setActionHeadingLabelText("Combine action");
        }
        else if(getAction().getActionType() == ActionType.FOLDER)
        {
            setActionHeadingLabelText("Folder action");
        }




        if(getAction().getActionType() == ActionType.NORMAL || getAction().getActionType() == ActionType.TOGGLE)
        {
            renderClientProperties();
            renderPluginExtraButtonBar();
        }
        else if(getAction().getActionType() == ActionType.COMBINE)
            renderCombineActionProperties();

    }

    private void renderPluginExtraButtonBar()
    {
        ExternalPlugin externalPlugin = (ExternalPlugin) getAction();

        externalPlugin.initClientActionSettingsButtonBar();

        if(externalPlugin.getClientActionSettingsButtonBar() != null)
        {
            HBox tba = new HBox(SpaceFiller.horizontal(), externalPlugin.getClientActionSettingsButtonBar());
            pluginExtraButtonBar.getChildren().add(tba);
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
            combineActionPropertiesPane = new CombineActionPropertiesPane((CombineAction) getAction(),
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

    @Override
    public synchronized void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void onOpenFolderButtonClicked()
    {
        actionBox.getActionGridPaneListener().renderFolder((FolderAction) getAction());
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

        for(int i =0;i< getAction().getClientProperties().getSize(); i++)
        {
            Property eachProperty = getAction().getClientProperties().get().get(i);

            if(!eachProperty.isVisible())
                continue;
            Helper.ControlNodePair controlNodePair = new Helper().getControlNode(eachProperty);
            UIPropertyBox clientProperty = new UIPropertyBox(i, eachProperty.getDisplayName(), controlNodePair.getControlNode(),
                    eachProperty.getControlType(), eachProperty.getType(), eachProperty.isCanBeBlank());
            actionClientProperties.add(clientProperty);
            clientPropertiesVBox.getChildren().add(controlNodePair.getUINode());
        }
    }

    public void onSaveButtonClicked()
    {
        try
        {
            validateForm();

            saveAction(true, true);
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
    public synchronized void saveAction(Action action, boolean runAsync, boolean runOnActionSavedFromServer)
    {
        String delayBeforeRunning = "0";
        if(action.getActionType() != ActionType.FOLDER && action.getActionType() !=ActionType.COMBINE)
            delayBeforeRunning = delayBeforeRunningTextField.getText();

        new OnSaveActionTask(
            ClientConnections.getInstance().getClientConnectionBySocketAddress(
                getClient().getRemoteSocketAddress()
            ),
            action, delayBeforeRunning,
            displayNameTextField.getText(),
            displayNameFontSizeTextField.getText(),
            displayNameFontSizeCheckBox.isSelected(),
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
            saveButton, deleteButton, resetToDefaultsButton, runOnActionSavedFromServer, runAsync, this
        );
    }

    @Override
    public void saveAction(boolean runAsync, boolean runOnActionSavedFromServer)
    {
        saveAction(getAction(), runAsync, runOnActionSavedFromServer);
    }

    public void setFolderButtonVisible(boolean visible)
    {
        openFolderButton.setVisible(visible);
    }

    public void setResetToDefaultsButtonVisible(boolean visible)
    {
        resetToDefaultsButton.setVisible(visible);
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
            if(getAction().getActionType() != ActionType.TOGGLE)
            {
                if(getAction().isHasIcon())
                {
                    if(hideDisplayTextCheckBox.isSelected() && hideDefaultIconCheckBox.isSelected())
                    {
                        finalErrors.append(" * Both Icon and display text check box cannot be hidden.\n");
                    }
                }
                else
                {
                    if(hideDisplayTextCheckBox.isSelected())
                        finalErrors.append(" * Display Text cannot be hidden since there is no icon.\n");
                }
            }

            if(!displayNameFontSizeCheckBox.isSelected())
            {
                try
                {
                    double r = Double.parseDouble(displayNameFontSizeTextField.getText());
                    if(r < 1)
                    {
                        finalErrors.append(" * Name Label Font Size too small.\n");
                    }
                    else if(r > getClientProfile().getActionSize())
                    {
                        finalErrors.append(" * Name Label Font Size too large.\n");
                    }
                }
                catch (NumberFormatException e)
                {
                    finalErrors.append(" * Name Label Font Size should be a number.\n");
                }
            }
        }

        if(getAction().getActionType() == ActionType.NORMAL)
        {
            try
            {
                if (Integer.parseInt(delayBeforeRunningTextField.getText()) < 0)
                {
                    finalErrors.append(" * Sleep should be greater than 0.\n");
                }
            }
            catch (NumberFormatException e)
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

    @Override
    public void onDeleteButtonClicked()
    {
        StreamPiAlert streamPiAlert = new StreamPiAlert(
                "Warning",
                "Are you sure you want to delete the action?",
                StreamPiAlertType.WARNING
        );

        String optionYes = "Yes";
        String optionNo = "No";

        streamPiAlert.setButtons(optionYes, optionNo);

        ActionDetailsPane actionDetailsPane = this;

        streamPiAlert.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(String s) {
                if(s.equals(optionYes))
                {
                    new OnDeleteActionTask(
                            ClientConnections.getInstance().getClientConnectionBySocketAddress(
                                    getClient().getRemoteSocketAddress()
                            ),
                            action,
                            isCombineChild(),
                            getCombineActionPropertiesPane(),
                            clientProfile, actionBox, actionDetailsPane, exceptionAndAlertHandler,
                            !isCombineChild
                    );
                }
            }
        });

        streamPiAlert.show();
    }
}