package com.stream_pi.server.window.settings;

import com.stream_pi.server.connection.ServerListener;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.info.ServerInfo;

import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.startatboot.SoftwareType;
import com.stream_pi.util.startatboot.StartAtBoot;
import com.stream_pi.util.version.Version;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.SystemTray;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

public class GeneralSettings extends VBox {

    private final TextField serverNameTextField;
    private final TextField portTextField;
    private final TextField pluginsPathTextField;
    private final TextField themesPathTextField;
    private final TextField actionGridPaneActionBoxSize;
    private final TextField actionGridPaneActionBoxGap;
    private final ToggleButton startOnBootToggleButton;
    private final ToggleButton closeOnXToggleButton;
    private final Button saveButton;
    private final Button checkForUpdatesButton;


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

        startOnBootToggleButton = new ToggleButton("Start on Boot");
        closeOnXToggleButton = new ToggleButton("Quit On window Close");

        checkForUpdatesButton = new Button("Check for updates");
        checkForUpdatesButton.setOnAction(event->checkForUpdates());

        getStyleClass().add("general_settings");

        prefWidthProperty().bind(widthProperty());
        setAlignment(Pos.CENTER);
        setSpacing(5);

        getChildren().addAll(
                getUIInputBox("Server Name", serverNameTextField),
                getUIInputBox("Port", portTextField),
                getUIInputBox("Grid Pane - Box Size", actionGridPaneActionBoxSize),
                getUIInputBox("Grid Pane - Box Gap", actionGridPaneActionBoxGap),
                getUIInputBoxWithDirectoryChooser("Plugins Path", pluginsPathTextField),
                getUIInputBoxWithDirectoryChooser("Themes Path", themesPathTextField)
        );

        serverNameTextField.setPrefWidth(200);

        HBox toggleButtons = new HBox(closeOnXToggleButton, startOnBootToggleButton);
        toggleButtons.setSpacing(10.0);
        VBox.setMargin(toggleButtons, new Insets(30, 0 , 0,0));
        toggleButtons.setAlignment(Pos.CENTER);

        saveButton = new Button("Save");
        saveButton.setOnAction(event->save());

        getChildren().addAll(toggleButtons, checkForUpdatesButton, saveButton);

        setPadding(new Insets(10));


    }

    private HBox getUIInputBoxWithDirectoryChooser(String labelText, TextField textField)
    {
       HBox hBox = getUIInputBox(labelText, textField);
       hBox.setSpacing(5.0);

       TextField tf = (TextField) hBox.getChildren().get(2);
       tf.setPrefWidth(300);
       tf.setDisable(true);


       Button button = new Button();
       FontIcon fontIcon = new FontIcon("far-folder");
       button.setGraphic(fontIcon);

       button.setOnAction(event -> {
           DirectoryChooser directoryChooser = new DirectoryChooser();


           try {
               File selectedDirectory = directoryChooser.showDialog(getScene().getWindow());

               textField.setText(selectedDirectory.getAbsolutePath());
           }
           catch (NullPointerException e)
           {
               logger.info("No folder selected");
           }
       });

       hBox.getChildren().add(button);


       return hBox;
    }

    private HBox getUIInputBox(String labelText, TextField textField)
    {
        textField.setPrefWidth(100);

        Label label = new Label(labelText);
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);


        return new HBox(label, region, textField);
    }



    public void loadDataFromConfig() throws SevereException {
        Config config = Config.getInstance();

        Platform.runLater(()->
        {
            serverNameTextField.setText(config.getServerName());
            portTextField.setText(config.getPort()+"");
            pluginsPathTextField.setText(config.getPluginsPath());
            themesPathTextField.setText(config.getThemesPath());
            actionGridPaneActionBoxSize.setText(config.getActionGridActionSize()+"");
            actionGridPaneActionBoxGap.setText(config.getActionGridActionGap()+"");

            closeOnXToggleButton.setSelected(config.getCloseOnX());
            startOnBootToggleButton.setSelected(config.getStartOnBoot());
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

                    Platform.runLater(()->{
                        saveButton.setDisable(true);

                        serverNameTextField.setDisable(true);
                        portTextField.setDisable(true);

                        closeOnXToggleButton.setDisable(true);
                        startOnBootToggleButton.setDisable(true);
                    });

                    String serverNameStr = serverNameTextField.getText();
                    String serverPortStr = portTextField.getText();
                    String pluginsPathStr = pluginsPathTextField.getText();
                    String themesPathStr = themesPathTextField.getText();

                    String actionGridActionBoxSize = actionGridPaneActionBoxSize.getText();
                    String actionGridActionBoxGap = actionGridPaneActionBoxGap.getText();

                    boolean closeOnX = closeOnXToggleButton.isSelected();
                    boolean startOnBoot = startOnBootToggleButton.isSelected();

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
                            errors.append("* Server Port must be more than 1024");

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
                    try {
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
                    try {
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
                        throw new MinorException("settings", "Please rectify the following errors and try again :\n"+errors.toString());
                    }

                    if(config.getStartOnBoot() != startOnBoot)
                    {
                        if(ServerInfo.getInstance().getRunnerFileName() == null)
                        {
                            new StreamPiAlert("Uh Oh", "No Runner File Name Specified as startup arguments. Cant set run at boot.", StreamPiAlertType.ERROR).show();
                            startOnBoot = false;
                        }
                        else
                        {
                            StartAtBoot startAtBoot = new StartAtBoot(SoftwareType.SERVER, ServerInfo.getInstance().getPlatformType());
                            if(startOnBoot)
                            {
                                startAtBoot.create(new File(ServerInfo.getInstance().getRunnerFileName()));
                            }
                            else
                            {
                                boolean result = startAtBoot.delete();
                                if(!result)
                                    new StreamPiAlert("Uh Oh!", "Unable to delete starter file", StreamPiAlertType.ERROR).show();
                            }
                        }
                    }

                    if(!closeOnX)
                    {
                        if(!SystemTray.isSupported()) 
                        {
                            StreamPiAlert alert = new StreamPiAlert("Not Supported", "Tray System not supported on this platform ", StreamPiAlertType.ERROR);
                            alert.show();

                            closeOnX = true;
                        }
                    }

                    config.setServerName(serverNameStr);
                    config.setServerPort(serverPort);
                    config.setActionGridGap(actionGap);
                    config.setActionGridSize(actionSize);
                    config.setPluginsPath(pluginsPathStr);
                    config.setThemesPath(themesPathStr);

                    config.setCloseOnX(closeOnX);
                    config.setStartupOnBoot(startOnBoot);

                    config.save();


                    loadDataFromConfig();

                    if(toBeReloaded)
                    {
                        new StreamPiAlert("Restart","Restart to see changes", StreamPiAlertType.INFORMATION).show();
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
                finally {
                    Platform.runLater(()->{
                        saveButton.setDisable(false);

                        serverNameTextField.setDisable(false);
                        portTextField.setDisable(false);

                        closeOnXToggleButton.setDisable(false);
                        startOnBootToggleButton.setDisable(false);
                    });
                }
                return null;
            }
        }).start();
    }

    public void checkForUpdates()
    {
        new Thread(new Task<Void>()
        {
            @Override
            protected Void call() throws Exception {

                    /*try
                {
                    Platform.runLater(()->checkForUpdatesButton.setDisable(true));


                    String jsonRaw = readUrl("https://stream-pi.com/API/get_latest.php?TYPE=SERVER");

                    System.out.println(jsonRaw);
                    JSONObject jsonObject = new JSONObject(jsonRaw);

                    String latestVersionRaw = jsonObject.getString("Version");
                    String releasePage = jsonObject.getString("Release Page");

                    Version latestVersion = new Version(latestVersionRaw);
                    Version currentVersion = ServerInfo.getInstance().getVersion();
                    
                    if(latestVersion.isBiggerThan(currentVersion))
                    {
                        VBox vBox = new VBox();

                        Hyperlink urlLabel = new Hyperlink(releasePage);
                        urlLabel.setOnAction(event->hostServices.showDocument(releasePage));

                        Label label = new Label(
                            "New Version "+latestVersionRaw+" Available.\n" +
                            "Current Version "+currentVersion.getText()+".\n"+
                            "Changelog and install instructions are included in the release page.\n" +
                            "It is recommended to update to ensure maximum stability and least bugs.");
                        label.setWrapText(true);

                        vBox.setSpacing(5);
                        vBox.getChildren().addAll(
                            urlLabel,
                            label
                        );

                        new StreamPiAlert("New Update Available!", StreamPiAlertType.INFORMATION, vBox).show();;
                    }
                    else
                    {
                        new StreamPiAlert("Up to Date", "Server is upto date. ("+currentVersion.getText()+")", StreamPiAlertType.INFORMATION).show();;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    new StreamPiAlert("Uh Oh", "Update Check Failed. API Error/Network issue.", StreamPiAlertType.WARNING).show();;
                }
                finally
                {
                    Platform.runLater(()->checkForUpdatesButton.setDisable(false));
                }*/
                return null;
            }
        }).start();;
    }

    private String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read); 
    
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
