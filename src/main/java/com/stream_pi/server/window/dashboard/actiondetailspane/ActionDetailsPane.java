/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.stream_pi.server.window.dashboard.actiondetailspane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.property.*;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.externalplugin.GaugeAction;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.controller.ServerExecutorService;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.controller.ActionDataFormats;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionGridPaneListener;
import com.stream_pi.server.window.helper.ControlNodePair;
import com.stream_pi.server.window.helper.Helper;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertButton;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import com.stream_pi.util.uihelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.beans.property.SimpleIntegerProperty;
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

    private HBox displayTextAlignmentComboBoxHBox;

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

        openFolderButton = new Button(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.openFolder"));
        openFolderButton.managedProperty().bind(openFolderButton.visibleProperty());
        FontIcon folderOpenIcon = new FontIcon("far-folder-open");
        openFolderButton.setGraphic(folderOpenIcon);
        openFolderButton.setOnAction(event -> onOpenFolderButtonClicked());

        saveButton = new Button(I18N.getString("save"));
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


        resetToDefaultsButton = new Button(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.reset"));
        resetToDefaultsButton.managedProperty().bind(resetToDefaultsButton.visibleProperty());
        resetToDefaultsButton.getStyleClass().add("action_details_pane_reset_button");
        FontIcon resetToDefaultsIcon = new FontIcon("fas-sync-alt");
        resetToDefaultsIcon.getStyleClass().add("action_details_pane_reset_button_icon");
        resetToDefaultsButton.setGraphic(resetToDefaultsIcon);
        resetToDefaultsButton.setOnAction(event -> onResetToDefaultsButtonClicked());

        returnButtonForCombineActionChild = new Button(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.return"));
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

                    actionBackgroundColourTransparentCheckBox.setSelected(true);
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


        clearIconButton = new Button(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.clearIcon"));
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

        hideDisplayTextCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.hide"));
        hideDisplayTextCheckBox.managedProperty().bind(hideDisplayTextCheckBox.visibleProperty());

        displayNameFontSizeCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.default"));
        displayNameFontSizeCheckBox.managedProperty().bind(displayNameFontSizeCheckBox.visibleProperty());
        displayNameFontSizeTextField.disableProperty().bind(displayNameFontSizeCheckBox.selectedProperty());

        hideDefaultIconCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.hide"));

        hideToggleOnIconCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.hide"));

        hideToggleOffIconCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.hide"));

        actionBackgroundColourPicker = new ColorPicker();
        actionBackgroundColourPicker.managedProperty().bind(actionBackgroundColourPicker.visibleProperty());

        displayTextColourPicker = new ColorPicker();
        displayTextColourPicker.managedProperty().bind(displayTextColourPicker.visibleProperty());

        actionBackgroundColourTransparentCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.default"));
        actionBackgroundColourPicker.disableProperty()
                .bind(actionBackgroundColourTransparentCheckBox.selectedProperty());

        HBox.setMargin(actionBackgroundColourTransparentCheckBox, new Insets(0, 0, 0, 10));


        displayTextColourDefaultCheckBox = new CheckBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.default"));
        displayTextColourPicker.disableProperty()
                .bind(displayTextColourDefaultCheckBox.selectedProperty());

        HBox.setMargin(displayTextColourDefaultCheckBox, new Insets(0, 0, 0, 10));

        HBox displayTextColourHBox = new HBox(new Label(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.displayTextColour")), SpaceFiller.horizontal(), displayTextColourPicker,
                displayTextColourDefaultCheckBox);
        displayTextColourHBox.setAlignment(Pos.CENTER);
        displayTextColourHBox.setSpacing(5.0);

        HBox bgColourHBox = new HBox(new Label(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.backgroundColour")), SpaceFiller.horizontal(), actionBackgroundColourPicker,
                actionBackgroundColourTransparentCheckBox);
        bgColourHBox.setAlignment(Pos.CENTER);
        bgColourHBox.setSpacing(5.0);

        clearIconHBox = new HBox(clearIconButton);
        clearIconHBox.managedProperty().bind(clearIconHBox.visibleProperty());
        clearIconHBox.setAlignment(Pos.CENTER_RIGHT);

        HBox.setMargin(hideDisplayTextCheckBox, new Insets(0, 0, 0, 45));
        HBoxInputBox s = new HBoxInputBox(I18N.getString("name"), displayNameTextField);
        HBox.setHgrow(s, Priority.ALWAYS);
        displayTextFieldHBox = new HBox(s, hideDisplayTextCheckBox);


        HBox.setMargin(displayNameFontSizeCheckBox, new Insets(0,0,0,30));
        HBoxInputBox t = new HBoxInputBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.fontSize"), displayNameFontSizeTextField);
        HBox.setHgrow(t, Priority.ALWAYS);
        displayNameLabelFontSizeTextFieldHBox = new HBox(t, displayNameFontSizeCheckBox);
        displayNameLabelFontSizeTextFieldHBox.managedProperty().bind(displayNameLabelFontSizeTextFieldHBox.visibleProperty());


        displayTextAlignmentComboBoxHBox = new HBox(new Label(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.alignment")), SpaceFiller.horizontal(),
                displayTextAlignmentComboBox);


        displayTextAlignmentComboBoxHBox.managedProperty().bind(displayTextAlignmentComboBoxHBox.visibleProperty());



        normalToggleActionCommonPropsVBox = new VBox(
                displayTextColourHBox,
                displayTextAlignmentComboBoxHBox,
                bgColourHBox
        );

        normalToggleActionCommonPropsVBox.managedProperty().bind(normalToggleActionCommonPropsVBox.visibleProperty());
        normalToggleActionCommonPropsVBox.setSpacing(10.0);

        FileChooser.ExtensionFilter iconExtensions = new FileChooser.ExtensionFilter(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.images"), "*.jpeg", "*.jpg", "*.png", "*.gif");

        colSpanTextField = new TextField();
        rowSpanTextField = new TextField();

        normalActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.icon"), defaultIconFileTextField, hideDefaultIconCheckBox,
                        iconExtensions),
                new HBoxInputBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.columnSpan"), colSpanTextField),
                new HBoxInputBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowSpan"), rowSpanTextField)
        );

        normalActionsPropsVBox.managedProperty().bind(normalActionsPropsVBox.visibleProperty());
        normalActionsPropsVBox.setSpacing(10.0);

        toggleActionsPropsVBox = new VBox(
                new HBoxInputBoxWithFileChooser(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.toggleOffIcon"), toggleOffIconFileTextField, hideToggleOffIconCheckBox,
                        iconExtensions),

                new HBoxInputBoxWithFileChooser(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.toggleOnIcon"), toggleOnIconFileTextField, hideToggleOnIconCheckBox,
                        iconExtensions)
        );

        toggleActionsPropsVBox.managedProperty().bind(toggleActionsPropsVBox.visibleProperty());
        toggleActionsPropsVBox.setSpacing(10.0);


        isAnimatedGaugeCheckBox = new CheckBox();

        gaugeActionsPropsVBox = new VBox(
                new HBoxWithSpaceBetween(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.animated"), isAnimatedGaugeCheckBox)
        );


        gaugeActionsPropsVBox.managedProperty().bind(gaugeActionsPropsVBox.visibleProperty());
        gaugeActionsPropsVBox.setSpacing(10.0);


        vbox.getChildren().addAll(displayTextFieldHBox, displayNameLabelFontSizeTextFieldHBox,
                normalToggleActionCommonPropsVBox,clearIconHBox,
                normalActionsPropsVBox, toggleActionsPropsVBox, gaugeActionsPropsVBox,
                clientPropertiesVBox, pluginExtraButtonBar);

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
            try
            {
                Dragboard db = dragEvent.getDragboard();

                ActionType actionType = (ActionType) db.getContent(ActionDataFormats.ACTION_TYPE);

                if(actionType == ActionType.NORMAL || actionType == ActionType.TOGGLE)
                {
                    ExternalPlugin newAction = actionGridPaneListener.createNewActionFromExternalPlugin(
                            (String) db.getContent(ActionDataFormats.UNIQUE_ID)
                    );

                    boolean isNew = (boolean) db.getContent(ActionDataFormats.IS_NEW);

                    if(isNew)
                    {
                        newAction.setShowDisplayText(true);
                        newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);

                        newAction.setDisplayText(newAction.getName());
                        newAction.getClientProperties().resetToDefaults();

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

                    newAction.setParent(getAction().getID());
                    newAction.setProfileID(actionGridPaneListener.getCurrentProfile().getID());
                    newAction.setSocketAddressForClient(actionGridPaneListener.getClientConnection().getRemoteSocketAddress());


                    combineActionPropertiesPane.getCombineAction().addChild(newAction.getID());

                    action.setClientProperties(combineActionPropertiesPane.getCombineAction().getClientProperties());

                    addActionToCurrentClientProfile(newAction);

                    ClientConnection connection = ClientConnections.getInstance()
                            .getClientConnectionBySocketAddress(getClient().getRemoteSocketAddress());

                    connection.saveActionDetails(getClientProfile().getID(), newAction);

                    combineActionPropertiesPane.renderProps();


                    ServerExecutorService.getExecutorService().execute(()->{
                        saveAction(false, false);

                        try
                        {
                            newAction.onActionCreate();
                        }
                        catch (MinorException e)
                        {
                            exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "onCreateFailed()", getAction().getUniqueID(), e.getMessage()), e);
                        }

                    });

                }

            }
            catch (MinorException e)
            {
                exceptionAndAlertHandler.handleMinorException(e);
                e.printStackTrace();
            }
            catch (SevereException e)
            {
                exceptionAndAlertHandler.handleSevereException(e);
                e.printStackTrace();
            }
            catch (CloneNotSupportedException e)
            {
                e.printStackTrace();
            }
        });
    }

    private void onResetToDefaultsButtonClicked()
    {
        StreamPiAlert streamPiAlert = new StreamPiAlert(
                I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.resetAreYouSure"),
                StreamPiAlertType.WARNING,
                StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        streamPiAlert.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if(s.equals(StreamPiAlertButton.YES))
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
    private VBox gaugeActionsPropsVBox;

    private CheckBox isAnimatedGaugeCheckBox;

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
        if (this.action != null && this.action.getID().equals(action.getID()))
        {
            return;
        }

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

    private TextField colSpanTextField;
    private TextField rowSpanTextField;

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
        isCombineChild = getAction().getLocation() == null;

        displayNameTextField.setText(getAction().getDisplayText());

        if (!isCombineChild)
        {
            colSpanTextField.setText(getAction().getLocation().getColSpan()+"");
            rowSpanTextField.setText(getAction().getLocation().getRowSpan()+"");
        }

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

        if(getAction().getDisplayTextFontSize() == -1)
        {
            displayNameFontSizeCheckBox.setSelected(true);
            displayNameFontSizeTextField.clear();
        }
        else
        {
            displayNameFontSizeCheckBox.setSelected(false);
            displayNameFontSizeTextField.setText(getAction().getDisplayTextFontSize()+"");
        }



        if(getAction().isInvalid())
        {
            setActionHeadingLabelText(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.invalidAction", getAction().getUniqueID()));
            return;
        }


        saveButton.setVisible(!getAction().isInvalid());

        if(isCombineChild)
        {
            setReturnButtonForCombineActionChildVisible(true);
            normalActionsPropsVBox.setVisible(false);
            normalToggleActionCommonPropsVBox.setVisible(false);
            gaugeActionsPropsVBox.setVisible(false);
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

            boolean isGaugeAction = getAction().getActionType() == ActionType.GAUGE;
            displayNameLabelFontSizeTextFieldHBox.setVisible(!isGaugeAction);
            displayTextAlignmentComboBoxHBox.setVisible(!isGaugeAction);
            hideDisplayTextCheckBox.setVisible(!isGaugeAction);

            gaugeActionsPropsVBox.setVisible(isGaugeAction);

            if (isGaugeAction)
            {
                isAnimatedGaugeCheckBox.setSelected(getAction().isGaugeAnimated());
            }

            setReturnButtonForCombineActionChildVisible(false);
            setFolderButtonVisible(getAction().getActionType().equals(ActionType.FOLDER));
            setResetToDefaultsButtonVisible(!(getAction().getActionType().equals(ActionType.FOLDER) || getAction().getActionType().equals(ActionType.COMBINE)));

            clearIconButton.setDisable(!getAction().isHasIcon());
        }



        if(getAction().getActionType() == ActionType.NORMAL || getAction().getActionType() == ActionType.TOGGLE || getAction().getActionType() == ActionType.GAUGE)
        {
            setActionHeadingLabelText(getAction().getName());
        }
        else if(getAction().getActionType() == ActionType.COMBINE)
        {
            setActionHeadingLabelText(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.combineAction"));
        }
        else if(getAction().getActionType() == ActionType.FOLDER)
        {
            setActionHeadingLabelText(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.folderAction"));
        }




        if(getAction().getActionType() == ActionType.NORMAL || getAction().getActionType() == ActionType.TOGGLE || getAction().getActionType() == ActionType.GAUGE)
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
                new HBoxInputBox(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.delayBeforeRunning"), delayBeforeRunningTextField, 100)
        );

        for(int i =0;i< getAction().getClientProperties().getSize(); i++)
        {
            Property eachProperty = getAction().getClientProperties().get().get(i);

            if(!eachProperty.isVisible())
                continue;

            UIPropertyBox clientPropertyBox = new UIPropertyBox(i, eachProperty);
            actionClientProperties.add(clientPropertyBox);
            clientPropertiesVBox.getChildren().add(clientPropertyBox.getUINode());
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

    public void addActionToCurrentClientProfile(Action newAction) throws CloneNotSupportedException
    {
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
            saveButton, deleteButton, resetToDefaultsButton, runOnActionSavedFromServer, runAsync, isAnimatedGaugeCheckBox.isSelected(), this,
            rowSpanTextField.getText(), colSpanTextField.getText()
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
            finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.displayNameCannotBeBlank")).append("\n");
        }

        if(!isCombineChild())
        {
            if(getAction().getActionType() != ActionType.TOGGLE)
            {
                if(getAction().isHasIcon())
                {
                    if(hideDisplayTextCheckBox.isSelected() && hideDefaultIconCheckBox.isSelected())
                    {
                        finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.bothIconAndDisplayTextCannotBeHidden")).append("\n");
                    }
                }
                else
                {
                    if(hideDisplayTextCheckBox.isSelected())
                    {
                        finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.displayTextCannotBeHiddenSinceThereIsNoIcon")).append("\n");
                    }
                }
            }

            if(!displayNameFontSizeCheckBox.isSelected())
            {
                try
                {
                    double r = Double.parseDouble(displayNameFontSizeTextField.getText());
                    if(r < 1)
                    {
                        finalErrors.append(" * ").append(I18N.getString("actionDisplayTextFontSizeTooSmall")).append("\n");
                    }
                }
                catch (NumberFormatException e)
                {
                    finalErrors.append(" * ").append(I18N.getString("actionDisplayTextFontSizeMustBeNumeric")).append("\n");
                }
            }

            try
            {
                int rowSpan = Integer.parseInt(rowSpanTextField.getText());

                int row = getAction().getLocation().getRow();
                int col = getAction().getLocation().getCol();

                if(rowSpan < 1)
                {
                    finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowSpanMustBeGreaterThan0")).append("\n");
                }
                else if((row+rowSpan) > getClientProfile().getRows())
                {
                    finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowSpanCannotBeBiggerThanProfileRowsSize")).append("\n");
                }
                else
                {
                    for (int i = (row+1); i< (row+rowSpan); i++)
                    {
                        if(actionGridPaneListener.getActionBox(col, i).getAction() != null)
                        {
                            finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowColumnSpanLocationConflict", i, col)).append("\n");
                        }
                    }
                }
            }
            catch (NumberFormatException e)
            {
                finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowSpanMustBeInteger")).append("\n");
            }

            try
            {
                int colSpan = Integer.parseInt(colSpanTextField.getText());

                int row = getAction().getLocation().getRow();
                int col = getAction().getLocation().getCol();

                if(colSpan < 1)
                {
                    finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.columnSpanMustBeGreaterThan0")).append("\n");
                }
                else if((col+colSpan) > getClientProfile().getCols())
                {
                    finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.columnSpanCannotBeBiggerThanProfileRowsSize")).append("\n");
                }
                else
                {
                    for (int i = (col+1); i< (col+colSpan); i++)
                    {
                        if(actionGridPaneListener.getActionBox(i, row).getAction() != null)
                        {
                            finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.rowColumnSpanLocationConflict", i, col)).append("\n");
                        }
                    }
                }
            }
            catch (NumberFormatException e)
            {
                finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.columnSpanMustBeInteger")).append("\n");
            }
        }

        if(getAction().getActionType() == ActionType.NORMAL || getAction().getActionType() == ActionType.TOGGLE || getAction().getActionType() == ActionType.GAUGE)
        {
            try
            {
                if (Integer.parseInt(delayBeforeRunningTextField.getText()) < 0)
                {
                    finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.delayBeforeRunningMustBeGreaterThan0")).append("\n");
                }
            }
            catch (NumberFormatException e)
            {
                finalErrors.append(" * ").append(I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.delayBeforeRunningMustBeInteger")).append("\n");
            }
        }

        for (UIPropertyBox clientProperty : actionClientProperties)
        {
            String error = clientProperty.validateProperty();

            if (error != null)
            {
                finalErrors.append("        -> ").append(error).append(("\n"));
            }
        }

        if(!finalErrors.toString().isEmpty())
        {
            throw new MinorException(I18N.getString("validationError", finalErrors));
        }
    }

    @Override
    public void onDeleteButtonClicked()
    {
        StreamPiAlert streamPiAlert = new StreamPiAlert(
                I18N.getString("window.dashboard.actiondetailspane.ActionDetailsPane.deleteAreYouSure"),
                StreamPiAlertType.WARNING,
                StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        ActionDetailsPane actionDetailsPane = this;

        streamPiAlert.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if(s.equals(StreamPiAlertButton.YES))
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

    @Override
    public void renderAction(Action action) throws MinorException
    {
        actionGridPaneListener.renderAction(action);
    }

    @Override
    public void clearActionBox(int col, int row, int colSpan, int rowSpan)
    {
        actionGridPaneListener.clearActionBox(col, row, colSpan, rowSpan);
    }
}