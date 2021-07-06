package com.stream_pi.server.window.settings;

import com.stream_pi.action_api.actionproperty.property.FileExtensionFilter;
import com.stream_pi.server.Main;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.info.StartupFlags;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.info.ServerInfo;

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.checkforupdates.CheckForUpdates;
import com.stream_pi.util.checkforupdates.UpdateHyperlinkOnClick;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.PlatformType;
import com.stream_pi.util.startatboot.StartAtBoot;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxInputBoxWithDirectoryChooser;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.javafx.FontIcon;
import org.w3c.dom.Text;

import java.awt.SystemTray;
import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class GeneralSettings extends VBox {

    private final TextField serverNameTextField;
    private final TextField portTextField;
    private final TextField pluginsPathTextField;
    private final TextField themesPathTextField;
    private final TextField actionGridPaneActionBoxSize;
    private final TextField actionGridPaneActionBoxGap;
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

        pluginsPathTextField = new TextField();

        themesPathTextField = new TextField();

        actionGridPaneActionBoxSize = new TextField();
        actionGridPaneActionBoxGap = new TextField();

        startOnBootToggleSwitch = new ToggleSwitch();
        startOnBootHBox = new HBoxWithSpaceBetween("Start on Boot", startOnBootToggleSwitch);
        startOnBootHBox.managedProperty().bind(startOnBootHBox.visibleProperty());

        soundOnActionClickedToggleSwitch = new ToggleSwitch();
        soundOnActionClickedToggleSwitchHBox = new HBoxWithSpaceBetween("Sound on Action Clicked", soundOnActionClickedToggleSwitch);


        soundOnActionClickedFilePathTextField = new TextField();

        HBoxInputBoxWithFileChooser soundHBoxInputBoxWithFileChooser =  new HBoxInputBoxWithFileChooser("Sound File Path", soundOnActionClickedFilePathTextField,
                new FileChooser.ExtensionFilter("Sounds","*.mp3","*.mp4", "*.m4a", "*.m4v","*.wav","*.aif", "*.aiff","*.fxm","*.flv","*.m3u8"));

        soundHBoxInputBoxWithFileChooser.setUseLast(false);
        soundHBoxInputBoxWithFileChooser.setRememberThis(false);

        soundHBoxInputBoxWithFileChooser.getFileChooseButton().disableProperty().bind(soundOnActionClickedToggleSwitch.selectedProperty().not());

        minimizeToSystemTrayOnCloseToggleSwitch = new ToggleSwitch();
        minimizeToSystemTrayOnCloseHBox = new HBoxWithSpaceBetween("Minimise To Tray On Close", minimizeToSystemTrayOnCloseToggleSwitch);

        showAlertsPopupToggleSwitch = new ToggleSwitch();
        showAlertsPopupHBox = new HBoxWithSpaceBetween("Show Popup On Alert", showAlertsPopupToggleSwitch);

        checkForUpdatesButton = new Button("Check for updates");
        checkForUpdatesButton.setOnAction(event->checkForUpdates());

        factoryResetButton = new Button("Factory Reset");
        factoryResetButton.setOnAction(actionEvent -> onFactoryResetButtonClicked());

        getStyleClass().add("general_settings");

        prefWidthProperty().bind(widthProperty());
        setAlignment(Pos.CENTER);
        setSpacing(5);

        getChildren().addAll(
                new HBoxInputBox("Server Name", serverNameTextField),
                new HBoxInputBox("Port", portTextField),
                new HBoxInputBox("Grid Pane - Box Size", actionGridPaneActionBoxSize),
                new HBoxInputBox("Grid Pane - Box Gap", actionGridPaneActionBoxGap),
                new HBoxInputBoxWithDirectoryChooser("Plugins Path", pluginsPathTextField),
                new HBoxInputBoxWithDirectoryChooser("Themes Path", themesPathTextField),
                soundHBoxInputBoxWithFileChooser,
                soundOnActionClickedToggleSwitchHBox,
                minimizeToSystemTrayOnCloseHBox,
                startOnBootHBox,
                showAlertsPopupHBox
        );

        serverNameTextField.setPrefWidth(200);

        saveButton = new Button("Save");
        saveButton.setOnAction(event->save());

        getChildren().addAll(factoryResetButton, checkForUpdatesButton, saveButton);

        setPadding(new Insets(10));


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
            pluginsPathTextField.setText(config.getPluginsPath());
            themesPathTextField.setText(config.getThemesPath());
            actionGridPaneActionBoxSize.setText(config.getActionGridActionSize()+"");
            actionGridPaneActionBoxGap.setText(config.getActionGridActionGap()+"");

            minimizeToSystemTrayOnCloseToggleSwitch.setSelected(config.getMinimiseToSystemTrayOnClose());
            showAlertsPopupToggleSwitch.setSelected(config.isShowAlertsPopup());
            startOnBootToggleSwitch.setSelected(config.getStartOnBoot());

            soundOnActionClickedToggleSwitch.setSelected(config.getSoundOnActionClickedStatus());
            soundOnActionClickedFilePathTextField.setText(config.getSoundOnActionClickedFilePath());
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
                        errors.append("* Server Name cannot be blank.\n");
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
                            errors.append("* Server Port must be more than 1024\n");
                        else if(serverPort > 65535)
                            errors.append("* Server Port must be lesser than 65535\n");

                        if(config.getPort()!=serverPort)
                        {
                            toBeReloaded = true;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        errors.append("* Server Port must be integer.\n");
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
                        errors.append("* action Size must be integer.\n");
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
                        errors.append("* action Gap must be integer.\n");
                    }

                    if(pluginsPathStr.isBlank())
                    {
                        errors.append("* Plugins Path must not be blank.\n");
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
                        errors.append("* Themes Path must not be blank.\n");
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
                        throw new MinorException("Uh Oh!", "Please rectify the following errors and try again :\n"+ errors);
                    }

                    if(config.getStartOnBoot() != startOnBoot)
                    {
                        StartAtBoot startAtBoot = new StartAtBoot(PlatformType.SERVER, ServerInfo.getInstance().getPlatform(),
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
                                new StreamPiAlert("Uh Oh!", "Unable to delete starter file", StreamPiAlertType.ERROR).show();
                        }
                    }

                    if(minimizeToSystemTrayOnClose)
                    {
                        if(!SystemTray.isSupported()) 
                        {
                            StreamPiAlert alert = new StreamPiAlert("Not Supported", "Tray System not supported on this platform ", StreamPiAlertType.ERROR);
                            alert.show();

                            minimizeToSystemTrayOnClose = false;
                        }
                    }

                    if(soundOnActionClicked)
                    {
                        if(soundOnActionClickedFilePath.isBlank())
                        {
                            StreamPiAlert alert = new StreamPiAlert("No sound file specified",
                                    "Sound File cannot be empty", StreamPiAlertType.ERROR);
                            alert.show();

                            soundOnActionClicked = false;
                        }
                        else
                        {
                            File soundFile = new File(soundOnActionClickedFilePath);
                            if(!soundFile.exists() || !soundFile.isFile())
                            {

                                StreamPiAlert alert = new StreamPiAlert("File not found",
                                        "No sound file at \n"+soundOnActionClickedFilePath+"\n" +
                                                "Unable to set sound on action clicked.", StreamPiAlertType.ERROR);
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
                        StreamPiAlert restartPrompt = new StreamPiAlert(
                                "Warning",
                                "Stream-Pi Server needs to be restarted for these changes to take effect. Restart?\n" +
                                        "All your current connections will be disconnected.",
                                StreamPiAlertType.WARNING
                        );

                        String yesOption = "Yes";
                        String noOption = "No";

                        restartPrompt.setButtons(yesOption, noOption);

                        restartPrompt.setOnClicked(new StreamPiAlertListener() {
                            @Override
                            public void onClick(String s) {
                                if(s.equals(yesOption))
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
        StreamPiAlert confirmation = new StreamPiAlert("Warning","Are you sure?\n" +
                "This will erase everything.",StreamPiAlertType.WARNING);

        String yesButton = "Yes";
        String noButton = "No";

        confirmation.setButtons(yesButton, noButton);

        confirmation.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(String s) {
                if(s.equals(yesButton))
                {
                    serverListener.factoryReset();
                }
            }
        });

        confirmation.show();
    }
}
