package com.stream_pi.server.window.settings;

import com.stream_pi.server.Main;
import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.server.combobox.LanguageChooserComboBox;
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

        defaultActionLabelFontSizeTextField = new TextField();

        portTextField = new TextField();

        ipChooserComboBox = new IPChooserComboBox(exceptionAndAlertHandler);

        languageChooserComboBox = new LanguageChooserComboBox();

        pluginsPathTextField = new TextField();

        themesPathTextField = new TextField();

        actionGridPaneActionBoxSize = new TextField();
        actionGridPaneActionBoxSizeIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

        actionGridPaneActionBoxGap = new TextField();
        actionGridPaneActionBoxGapIsDefaultCheckBox = new CheckBox(I18N.getString("window.settings.GeneralSettings.followProfileDefaults"));

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

        serverNameTextField.setPrefWidth(200);


        saveButton = new Button(I18N.getString("window.settings.GeneralSettings.save"));
        VBox.setMargin(saveButton, new Insets(0,10, 0, 0));

        saveButton.setOnAction(event->save());

        VBox vbox = new VBox(
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.connection")),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.serverName"), serverNameTextField),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.port"), portTextField),
                new HBoxWithSpaceBetween(I18N.getString("window.settings.GeneralSettings.ip"), ipChooserComboBox),
                generateSubHeading(I18N.getString("window.settings.GeneralSettings.actionGrid")),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.actionBoxSize"), actionGridPaneActionBoxSize, actionGridPaneActionBoxSizeIsDefaultCheckBox),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.actionBoxGap"), actionGridPaneActionBoxGap, actionGridPaneActionBoxGapIsDefaultCheckBox),
                new HBoxInputBox(I18N.getString("window.settings.GeneralSettings.actionBoxDefaultTextFontSize"), defaultActionLabelFontSizeTextField),
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
                checkForUpdatesButton
        );

        vbox.prefWidthProperty().bind(widthProperty().subtract(25));


        vbox.getStyleClass().add("general_settings_vbox");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("general_settings_scroll_pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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

    public void loadDataFromConfig() throws SevereException {
        Config config = Config.getInstance();

        javafx.application.Platform.runLater(()->
        {
            serverNameTextField.setText(config.getServerName());
            portTextField.setText(config.getPort()+"");
            defaultActionLabelFontSizeTextField.setText(config.getDefaultActionDisplayTextFontSize()+"");
            pluginsPathTextField.setText(config.getPluginsPath());
            themesPathTextField.setText(config.getThemesPath());
            actionGridPaneActionBoxSize.setText(config.getActionGridActionSize()+"");
            actionGridPaneActionBoxSizeIsDefaultCheckBox.setSelected(config.isUseSameActionSizeAsProfile());
            actionGridPaneActionBoxGapIsDefaultCheckBox.setSelected(config.isUseSameActionGapAsProfile());
            actionGridPaneActionBoxGap.setText(config.getActionGridActionGap()+"");

            minimizeToSystemTrayOnCloseToggleSwitch.setSelected(config.getMinimiseToSystemTrayOnClose());
            showAlertsPopupToggleSwitch.setSelected(config.isShowAlertsPopup());
            startOnBootToggleSwitch.setSelected(config.getStartOnBoot());

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
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.serverNameCannotBeBlank")).append("\n");
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

                        if (serverPort < 1024)
                            errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.serverPortMustBeGreaterThan1024")).append("\n");
                        else if(serverPort > 65535)
                            errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.serverPortMustBeLesserThan65535")).append("\n");

                        if(config.getPort()!=serverPort)
                        {
                            toBeReloaded = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.serverPortMustBeInteger")).append("\n");
                    }

                    double defaultActionLabelFontSize=-1;
                    try
                    {
                        defaultActionLabelFontSize = Double.parseDouble(defaultActionLabelFontSizeStr);

                        if (defaultActionLabelFontSize < 1)
                            errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.actionLabelFontSizeTooSmall")).append("\n");
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.actionLabelFontSizeMustBeInteger")).append("\n");
                    }


                    int actionSize=-1;
                    try
                    {
                        actionSize = Integer.parseInt(actionGridActionBoxSize);

                        if(config.getActionGridActionSize() != actionSize)
                        {
                            dashToBeReRendered = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.actionSizeMustBeInteger")).append("\n");
                    }

                    if(actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected() != config.isUseSameActionSizeAsProfile())
                    {
                        dashToBeReRendered = true;
                    }


                    int actionGap=-1;
                    try
                    {
                        actionGap = Integer.parseInt(actionGridActionBoxGap);

                        if(config.getActionGridActionGap() != actionGap)
                        {
                            dashToBeReRendered = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* ").append(I18N.getString("window.settings.GeneralSettings.actionGapMustBeInteger")).append("\n");
                    }

                    if(actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected() != config.isUseSameActionGapAsProfile())
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
                        throw new MinorException(I18N.getString("window.settings.GeneralSettings.rectifyTheFollowingErrorsAndTryAgain", errors));
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


                    if(config.getStartOnBoot() != startOnBoot)
                    {
                        StartOnBoot startAtBoot = new StartOnBoot(PlatformType.SERVER, ServerInfo.getInstance().getPlatform(),
                                Main.class.getProtectionDomain().getCodeSource().getLocation(),
                                StartupFlags.APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION);

                        if(startOnBoot)
                        {
                            try
                            {
                                startAtBoot.create(StartupFlags.RUNNER_FILE_NAME);
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
                    config.setServerPort(serverPort);
                    config.setActionGridGap(actionGap);
                    config.setActionGridSize(actionSize);
                    config.setPluginsPath(pluginsPathStr);
                    config.setThemesPath(themesPathStr);

                    config.setUseSameActionGapAsProfile(actionGridPaneActionBoxGapIsDefaultCheckBox.isSelected());
                    config.setUseSameActionSizeAsProfile(actionGridPaneActionBoxSizeIsDefaultCheckBox.isSelected());

                    config.setDefaultActionDisplayTextFontSize(defaultActionLabelFontSize);

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


                    loadDataFromConfig();

                    if(toBeReloaded)
                    {
                        StreamPiAlert restartPrompt = new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.restartPromptWarning"),
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