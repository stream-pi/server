/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macro Pad
 * Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
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

package com.stream_pi.server.window.settings;

import com.stream_pi.server.Main;
import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.server.combobox.LanguageChooserComboBox;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.StartupFlags;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertButton;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.checkforupdates.CheckForUpdates;
import com.stream_pi.util.checkforupdates.UpdateHyperlinkOnClick;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.platform.PlatformType;
import com.stream_pi.util.rootchecker.RootChecker;
import com.stream_pi.util.startonboot.StartOnBoot;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxInputBoxWithDirectoryChooser;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;

import java.awt.SystemTray;
import java.io.File;
import java.util.logging.Logger;

public class GeneralSettings extends VBox
{
    private final TextField serverNameTextField;
    private final TextField defaultActionLabelFontSizeTextField;
    private final TextField portTextField;
    private final IPChooserComboBox ipChooserComboBox;
    private final TextField pluginsPathTextField;
    private final TextField themesPathTextField;
    private final TextField actionGridPaneActionBoxSize;
    private final CheckBox actionGridPaneActionBoxSizeIsDefaultCheckBox;
    private final TextField actionGridPaneActionBoxGap;
    private final CheckBox actionGridPaneActionBoxGapIsDefaultCheckBox;
    private final CheckBox actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox;
    private final ToggleSwitch startOnBootToggleSwitch;
    private final HBoxWithSpaceBetween startOnBootHBox;
    private final TextField soundOnActionClickedFilePathTextField;
    private final ToggleSwitch soundOnActionClickedToggleSwitch;
    private final HBoxWithSpaceBetween soundOnActionClickedToggleSwitchHBox;
    private final ToggleSwitch minimizeToSystemTrayOnCloseToggleSwitch;
    private final HBoxWithSpaceBetween minimizeToSystemTrayOnCloseHBox;
    private final ToggleSwitch showAlertsPopupToggleSwitch;
    private final HBoxWithSpaceBetween showAlertsPopupHBox;
    private final Button saveButton;
    private final Button checkForUpdatesButton;
    private final Button factoryResetButton;
    private final Button restartButton;
    private final LanguageChooserComboBox languageChooserComboBox;

    private Logger logger;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private ServerListener serverListener;

    private HostServices hostServices;

    public GeneralSettings(ExceptionAndAlertHandler exceptionAndAlertHandler, ServerListener serverListener, HostServices hostServices)
    {
        this.hostServices = hostServices;


        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.serverListener = serverListener;

        logger = Logger.getLogger(GeneralSettings.class.getName());

        serverNameTextField = new TextField();


        portTextField = new TextField();

        ipChooserComboBox = new IPChooserComboBox(exceptionAndAlertHandler);

        languageChooserComboBox = new LanguageChooserComboBox();

        pluginsPathTextField = new TextField();

        themesPathTextField = new TextField();

        actionGridPaneActionBoxSize = new TextField();
        actionGridPaneActionBoxSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridPaneActionBoxGap = new TextField();
        actionGridPaneActionBoxGapIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        defaultActionLabelFontSizeTextField = new TextField();
        actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        startOnBootToggleSwitch = new ToggleSwitch();
        startOnBootHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.startOnBoot"), startOnBootToggleSwitch);
        startOnBootHBox.managedProperty().bind(startOnBootHBox.visibleProperty());

        soundOnActionClickedToggleSwitch = new ToggleSwitch();
        soundOnActionClickedToggleSwitchHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked"), soundOnActionClickedToggleSwitch);


        soundOnActionClickedFilePathTextField = new TextField();

        HBoxInputBoxWithFileChooser soundHBoxInputBoxWithFileChooser =  new HBoxInputBoxWithFileChooser(I18N.getString("window.settings.GeneralSettings.soundOnActionClicked"), soundOnActionClickedFilePathTextField,
                new FileChooser.ExtensionFilter("Sounds","*.mp3","*.mp4", "*.m4a", "*.m4v","*.wav","*.aif", "*.aiff","*.fxm","*.flv","*.m3u8"));

        soundHBoxInputBoxWithFileChooser.setUseLast(false);
        soundHBoxInputBoxWithFileChooser.setRememberThis(false);

        soundHBoxInputBoxWithFileChooser.getFileChooseButton().disableProperty().bind(soundOnActionClickedToggleSwitch.selectedProperty().not());

        minimizeToSystemTrayOnCloseToggleSwitch = new ToggleSwitch();
        minimizeToSystemTrayOnCloseHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.minimiseToTrayOnClose"), minimizeToSystemTrayOnCloseToggleSwitch);

        showAlertsPopupToggleSwitch = new ToggleSwitch();
        showAlertsPopupHBox = new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.showPopupOnAlert"), showAlertsPopupToggleSwitch);

        checkForUpdatesButton = new Button(I18N.getString("window.settings.GeneralSettings.checkForUpdates"));
        checkForUpdatesButton.setOnAction(event->checkForUpdates());

        factoryResetButton = new Button(I18N.getString("window.settings.GeneralSettings.factoryReset"));
        factoryResetButton.setOnAction(actionEvent -> onFactoryResetButtonClicked());
        
        restartButton = new Button(I18N.getString("window.settings.GeneralSettings.restart"));
        restartButton.setOnAction(event->{
            if (ClientConnections.getInstance().getConnections().size() > 0)
            {
                showRestartPrompt(I18N.getString("window.settings.GeneralSettings.restartPromptWarning"));
            }
            else
            {
                serverListener.restart();
            }
        });

        serverNameTextField.setPrefWidth(200);


        saveButton = new Button(I18N.getString("save"));
        VBox.setMargin(saveButton, new Insets(0,10, 0, 0));

        saveButton.setOnAction(event->save());

        VBox vbox = new VBox(
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.connection")),
                new HBoxInputBox(I18N.getString("serverName"), serverNameTextField),
                new HBoxInputBox(I18N.getString("serverPort"), portTextField),
                new HBoxWithSpaceBetween(I18N.getString("serverIPBinding"), ipChooserComboBox),
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.actionGrid")),
                new HBoxInputBox(I18N.getString("actionBoxSize"), actionGridPaneActionBoxSize, actionGridPaneActionBoxSizeIsDefaultCheckBox),
                new HBoxInputBox(I18N.getString("actionBoxGap"), actionGridPaneActionBoxGap, actionGridPaneActionBoxGapIsDefaultCheckBox),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.actionBoxDisplayTextFontSize"), defaultActionLabelFontSizeTextField, actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox),
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.locations")),
                new HBoxInputBoxWithDirectoryChooser(I18N.getString("window.settings.GeneralSettings.plugins"), pluginsPathTextField),
                new HBoxInputBoxWithDirectoryChooser(I18N.getString("window.settings.GeneralSettings.themes"), themesPathTextField),
                soundHBoxInputBoxWithFileChooser,
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.others")),
                new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.language"), languageChooserComboBox),
                soundOnActionClickedToggleSwitchHBox,
                minimizeToSystemTrayOnCloseHBox,
                startOnBootHBox,
                showAlertsPopupHBox,
                factoryResetButton,
                restartButton
        );

        // checkForUpdatesButton removed until Update API is finalised


        vbox.getStyleClass().add("general_settings_vbox");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("general_settings_scroll_pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setContent(vbox);

        getStyleClass().add("general_settings");

        getChildren().addAll(scrollPane, saveButton);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }



    private Label generateSubHeading(String text)
    {
        Label label = new Label(text);
        label.getStyleClass().add("general_settings_sub_heading");
        return label;
    }

    private void checkForUpdates()
    {
        new CheckForUpdates(checkForUpdatesButton,
                PlatformType.SERVER, ServerInfo.getInstance().getVersion(), new UpdateHyperlinkOnClick() {
            @Override
            public void handle(ActionEvent actionEvent) {
                hostServices.showDocument(getURL());
            }
        });
    }

    public void loadData() throws SevereException
    {
        Config config = Config.getInstance();

        javafx.application.Platform.runLater(()->
        {
            serverNameTextField.setText(config.getServerName());
            portTextField.setText(config.getPort()+"");
            defaultActionLabelFontSizeTextField.setText(config.getActionGridActionDisplayTextFontSize()+"");
            pluginsPathTextField.setText(config.getPluginsPath());
            themesPathTextField.setText(config.getThemesPath());
            actionGridPaneActionBoxSize.setText(config.getActionGridActionSize()+"");
            actionGridPaneActionBoxSizeIsDefaultCheckBox.setSelected(config.getActionGridUseSameActionSizeAsProfile());
            actionGridPaneActionBoxGapIsDefaultCheckBox.setSelected(config.getActionGridUseSameActionGapAsProfile());
            actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.setSelected(config.getActionGridUseSameActionDisplayTextFontSizeAsProfile());
            actionGridPaneActionBoxGap.setText(config.getActionGridActionGap()+"");

            minimizeToSystemTrayOnCloseToggleSwitch.setSelected(config.getMinimiseToSystemTrayOnClose());
            showAlertsPopupToggleSwitch.setSelected(config.isShowAlertsPopup());
            startOnBootToggleSwitch.setSelected(config.isStartOnBoot());

            soundOnActionClickedToggleSwitch.setSelected(config.getSoundOnActionClickedStatus());
            soundOnActionClickedFilePathTextField.setText(config.getSoundOnActionClickedFilePath());

            ipChooserComboBox.configureOptions(config.getIP());

            languageChooserComboBox.getSelectionModel().select(I18N.getLanguage(config.getCurrentLanguageLocale()));
        });
    }

    public void save()
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try {
                    boolean toBeReloaded = false;
                    boolean dashToBeReRendered = false;

                    javafx.application.Platform.runLater(()-> saveButton.setDisable(true));

                    String serverNameStr = serverNameTextField.getText();
                    String serverPortStr = portTextField.getText();
                    String defaultActionLabelFontSizeStr = defaultActionLabelFontSizeTextField.getText();
                    String pluginsPathStr = pluginsPathTextField.getText();
                    String themesPathStr = themesPathTextField.getText();

                    String actionGridActionBoxSize = actionGridPaneActionBoxSize.getText();
                    String actionGridActionBoxGap = actionGridPaneActionBoxGap.getText();

                    boolean minimizeToSystemTrayOnClose = minimizeToSystemTrayOnCloseToggleSwitch.isSelected();
                    boolean showAlertsPopup = showAlertsPopupToggleSwitch.isSelected();
                    boolean startOnBoot = startOnBootToggleSwitch.isSelected();

                    boolean soundOnActionClicked = soundOnActionClickedToggleSwitch.isSelected();

                    String soundOnActionClickedFilePath = soundOnActionClickedFilePathTextField.getText();

                    Config config = Config.getInstance();

                    StringBuilder errors = new StringBuilder();


                    if(serverNameStr.isBlank())
                    {
                        errors.append("* ").append(I18N.getString("serverNameCannotBeBlank")).append("\n");
                    }
                    else
                    {
                        if(!config.getServerName().equals(serverNameStr))
                        {
                            toBeReloaded = true;
                        }
                    }


                    int serverPort=-1;
                    try {
                        serverPort = Integer.parseInt(serverPortStr);

                        if (serverPort < 1024 && !RootChecker.isRoot(ServerInfo.getInstance().getPlatform()))
                            errors.append("* ").append(I18N.getString("serverPortMustBeGreaterThan1024")).append("\n");
                        else if(serverPort > 65535)
                            errors.append("* ").append(I18N.getString("serverPortMustBeLesserThan65535")).append("\n");

                        if(config.getPort()!=serverPort)
                        {
                            toBeReloaded = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("serverPortMustBeInteger")).append("\n");
                    }

                    double defaultActionLabelFontSize=-1;
                    try
                    {
                        defaultActionLabelFontSize = Double.parseDouble(defaultActionLabelFontSizeStr);

                        if (defaultActionLabelFontSize < 1)
                            errors.append("* ").append(I18N.getString("actionDisplayTextFontSizeTooSmall")).append("\n");
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("actionDisplayTextFontSizeMustBeNumeric")).append("\n");
                    }


                    double actionSize=-1;
                    try
                    {
                        actionSize = Double.parseDouble(actionGridActionBoxSize);

                        if(config.getActionGridActionSize() != actionSize)
                        {
                            dashToBeReRendered = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("actionSizeMustBeNumeric")).append("\n");
                    }

                    if(actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected() != config.getActionGridUseSameActionSizeAsProfile())
                    {
                        dashToBeReRendered = true;
                    }


                    double actionGap=-1;
                    try
                    {
                        actionGap = Double.parseDouble(actionGridActionBoxGap);

                        if(config.getActionGridActionGap() != actionGap)
                        {
                            dashToBeReRendered = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("actionGapMustBeNumeric")).append("\n");
                    }

                    if(actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected() != config.getActionGridUseSameActionGapAsProfile())
                    {
                        dashToBeReRendered = true;
                    }

                    if (actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.isSelected() != config.getActionGridUseSameActionDisplayTextFontSizeAsProfile())
                    {
                        dashToBeReRendered = true;
                    }

                    if(pluginsPathStr.isBlank())
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.pluginsPathMustNotBeBlank")).append("\n");
                    }
                    else
                    {
                        if(!config.getPluginsPath().equals(pluginsPathStr))
                        {
                            toBeReloaded = true;
                        }
                    }

                    if(themesPathStr.isBlank())
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.themesPathMustNotBeBlank")).append("\n");
                    }
                    else
                    {
                        if(!config.getThemesPath().equals(themesPathStr))
                        {
                            toBeReloaded = true;
                        }
                    }

                    if(!errors.toString().isEmpty())
                    {
                        throw new MinorException(I18N.getString("validationError", errors));
                    }



                    if (!ipChooserComboBox.getSelectedIP().equals(config.getIP()))
                    {
                        config.setIP(ipChooserComboBox.getSelectedIP());

                        toBeReloaded = true;
                    }

                    if (!languageChooserComboBox.getSelectedLocale().equals(config.getCurrentLanguageLocale()))
                    {
                        config.setCurrentLanguageLocale(languageChooserComboBox.getSelectedLocale());

                        toBeReloaded = true;
                    }


                    if(config.isStartOnBoot() != startOnBoot)
                    {
                        StartOnBoot startAtBoot = new StartOnBoot(PlatformType.SERVER, ServerInfo.getInstance().getPlatform(),
                                Main.class.getProtectionDomain().getCodeSource().getLocation(),
                                StartupFlags.APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION);

                        if(startOnBoot)
                        {
                            try
                            {
                                startAtBoot.create(StartupFlags.RUNNER_FILE_NAME, StartupFlags.generateRuntimeArgumentsForStartOnBoot());
                            }
                            catch (MinorException e)
                            {
                                exceptionAndAlertHandler.handleMinorException(e);
                                startOnBoot = false;
                            }
                        }
                        else
                        {
                            boolean result = startAtBoot.delete();
                            if(!result)
                                new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.unableToDeleteStarterFile"), StreamPiAlertType.ERROR).show();
                        }
                    }

                    if(minimizeToSystemTrayOnClose)
                    {
                        if(!SystemTray.isSupported()) 
                        {
                            StreamPiAlert alert = new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.traySystemNotSupported"), StreamPiAlertType.ERROR);
                            alert.show();

                            minimizeToSystemTrayOnClose = false;
                        }
                    }

                    if(soundOnActionClicked)
                    {
                        if(soundOnActionClickedFilePath.isBlank())
                        {
                            StreamPiAlert alert = new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.soundFileCannotBeEmpty"), StreamPiAlertType.ERROR);
                            alert.show();

                            soundOnActionClicked = false;
                        }
                        else
                        {
                            File soundFile = new File(soundOnActionClickedFilePath);
                            if(!soundFile.exists() || !soundFile.isFile())
                            {

                                StreamPiAlert alert = new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.soundFileNotFound"), StreamPiAlertType.ERROR);
                                alert.show();

                                soundOnActionClicked = false;
                            }
                        }
                    }

                    config.setServerName(serverNameStr);
                    config.setPort(serverPort);
                    config.setActionGridActionGap(actionGap);
                    config.setActionGridActionSize(actionSize);
                    config.setActionGridActionDisplayTextFontSize(defaultActionLabelFontSize);
                    config.setPluginsPath(pluginsPathStr);
                    config.setThemesPath(themesPathStr);

                    config.setActionGridUseSameActionGapAsProfile(actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected());
                    config.setActionGridUseSameActionSizeAsProfile(actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected());
                    config.setActionGridUseSameActionDisplayTextFontSizeAsProfile(actionGridPaneActionDisplayTextFontSizeIsDefaultCheckBox.isSelected());


                    config.setMinimiseToSystemTrayOnClose(minimizeToSystemTrayOnClose);
                    StreamPiAlert.setIsShowPopup(showAlertsPopup);
                    config.setShowAlertsPopup(showAlertsPopup);
                    config.setStartupOnBoot(startOnBoot);

                    if(soundOnActionClicked)
                    {
                        serverListener.initSoundOnActionClicked();
                    }

                    config.setSoundOnActionClickedStatus(soundOnActionClicked);



                    config.setSoundOnActionClickedFilePath(soundOnActionClickedFilePath);

                    config.save();

                    loadData();

                    if(toBeReloaded)
                    {
                        showRestartPrompt(I18N.getString("window.settings.GeneralSettings.needsToBeRestartedToApplySettings") + "\n" + I18N.getString("window.settings.GeneralSettings.restartPromptWarning"));
                    }

                    if(dashToBeReRendered)
                    {
                        serverListener.clearTemp();
                    }
                }
                catch (MinorException e)
                {
                    exceptionAndAlertHandler.handleMinorException(e);
                }
                catch (SevereException e)
                {
                    exceptionAndAlertHandler.handleSevereException(e);
                }
                finally
                {
                    javafx.application.Platform.runLater(()-> saveButton.setDisable(false));
                }
                return null;
            }
        }).start();
    }

    private void showRestartPrompt(String promptText)
    {
        StreamPiAlert restartPrompt = new StreamPiAlert(promptText,
                StreamPiAlertType.WARNING, StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        restartPrompt.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if(s.equals(StreamPiAlertButton.YES))
                {
                    serverListener.restart();
                }
            }
        });

        restartPrompt.show();
    }

    private void onFactoryResetButtonClicked()
    {
        StreamPiAlert confirmation = new StreamPiAlert( I18N.getString("window.settings.GeneralSettings.resetAreYouSure"),
                StreamPiAlertType.WARNING, StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        confirmation.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if (s.equals(StreamPiAlertButton.YES)) {
                    serverListener.factoryReset();
                }
            }
        });

        confirmation.show();
    }
}