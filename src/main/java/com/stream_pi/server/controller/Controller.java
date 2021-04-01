package com.stream_pi.server.controller;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.PropertySaver;
import com.stream_pi.action_api.action.ServerConnection;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.server.Main;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.connection.MainServer;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.Base;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.dashboard.DonatePopupContent;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.firsttimeuse.FirstTimeUse;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class Controller extends Base implements PropertySaver, ServerConnection
{
    private ExecutorService executor = Executors.newCachedThreadPool();
    private MainServer mainServer;
    private Animation openSettingsAnimation;
    private Animation closeSettingsAnimation;

    public Controller(){
        mainServer = null;
    }

    public void setupDashWindow() throws SevereException
    {
        try
        {
            getStage().setOnCloseRequest(this::onCloseRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(e.getMessage());
        }
    }

    @Override
    public void init()
    {
        try {

            initBase();
            setupDashWindow();

            setupSettingsWindowsAnimations();

            ExternalPlugins.getInstance().setPropertySaver(this);
            ExternalPlugins.getInstance().setServerConnection(this);


            getDashboardPane().getPluginsPane().getSettingsButton().setOnAction(event -> {
                openSettingsAnimation.play();
            });

            getSettingsPane().getCloseButton().setOnAction(event -> {
                closeSettingsAnimation.play();
            });

            getSettingsPane().getThemesSettings().setController(this);

            mainServer = new MainServer(this, this);

            if(getConfig().isFirstTimeUse())
            {
                Stage stage = new Stage();
                Scene s = new Scene(new FirstTimeUse(this, this),
                        getConfig().getStartupWindowWidth(), getConfig().getStartupWindowHeight());
                stage.setResizable(false);
                stage.setScene(s); 
                stage.setTitle("Stream-Pi Server Setup");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setOnCloseRequest(event->Platform.exit());
                stage.show();    
            }
            else
            {
                if(getConfig().isAllowDonatePopup())
                {
                    if(new Random().nextInt(5) == 3)
                        new DonatePopupContent(getHostServices(), this).show();
                }

                othInit();
            }

        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
    }

    @Override
    public void othInit()
    {
        try
        {
            if(ServerInfo.getInstance().isStartMinimised() && SystemTray.isSupported())
                minimiseApp();
            else
                getStage().show();
        }
        catch(MinorException e)
        {
            handleMinorException(e);
        }

        executor.execute(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    getSettingsPane().getGeneralSettings().loadDataFromConfig();

                    //themes
                    getSettingsPane().getThemesSettings().setThemes(getThemes());
                    getSettingsPane().getThemesSettings().setCurrentThemeFullName(getCurrentTheme().getFullName());
                    getSettingsPane().getThemesSettings().loadThemes();

                    //clients
                    getSettingsPane().getClientsSettings().loadData();

                    try
                    {
                        //Plugins 
                        Platform.runLater(()->{
                            getDashboardPane().getPluginsPane().clearData();
                            getDashboardPane().getPluginsPane().loadOtherActions();
                        });

                        ExternalPlugins.setPluginsLocation(getConfig().getPluginsPath());
                        ExternalPlugins.getInstance().init();

                        Platform.runLater(()->getDashboardPane().getPluginsPane().loadData());

                        getSettingsPane().getPluginsSettings().loadPlugins();
                    }
                    catch (MinorException e)
                    {
                        getSettingsPane().getPluginsSettings().showPluginInitError();
                        handleMinorException(e);
                    }

                    //Server
                    mainServer.setPort(getConfig().getPort());
                    mainServer.start();
                }
                catch (SevereException e)
                {
                    handleSevereException(e);
                }
                return null;
            }
        });
    }

    @Override
    public DashboardBase getDashboardBase() {
        return getDashboardPane();
    }

    private void setupSettingsWindowsAnimations()
    {
        Node settingsNode = getSettingsPane();
        Node dashboardNode = getDashboardPane();

        openSettingsAnimation = createOpenSettingsAnimation(settingsNode, dashboardNode);
        closeSettingsAnimation = createCloseSettingsAnimation(settingsNode, dashboardNode);
    }

    public void onCloseRequest(WindowEvent event)
    {
        try
        {
            if(Config.getInstance().getMinimiseToSystemTrayOnClose() &&
                    SystemTray.isSupported())
            {
                minimiseApp();
                event.consume();
                return;
            }

            getConfig().setStartupWindowSize(getWidth(), getHeight());
            getConfig().save();
            onQuitApp();
            ExternalPlugins.getInstance().shutDownActions();
            Platform.exit();
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
        catch (MinorException e)
        {
            handleMinorException(e);
        }
        finally
        {
            closeLogger();
        }
    }

    public void onQuitApp()
    {
        if(mainServer!=null)
            mainServer.stopListeningForConnections();

        ClientConnections.getInstance().disconnectAll();
        executor.shutdown();
        getLogger().info("Shutting down ...");
    }

    public void minimiseApp() throws MinorException
    {
        try
        {
            SystemTray systemTray = SystemTray.getSystemTray();

            if(getTrayIcon() == null)
                initIconTray(systemTray);

            systemTray.add(getTrayIcon());
            getStage().hide();

            getStage().setOnShown(windowEvent -> {
                systemTray.remove(getTrayIcon());
            });
        }
        catch(Exception e)
        {
            throw new MinorException(e.getMessage());
        }
    }

    public void initIconTray(SystemTray systemTray)
    {

        Platform.setImplicitExit(false);
        
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(l->{
            systemTray.remove(getTrayIcon());
            onQuitApp();
            Platform.exit();
        });

        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(
            Toolkit.getDefaultToolkit().getImage(Main.class.getResource("app_icon.png")),
            "Stream-Pi Server",
            popup
        );

        trayIcon.addActionListener(l-> Platform.runLater(()-> getStage().show()));

        trayIcon.setImageAutoSize(true);

        this.trayIcon = trayIcon;
    }

    private TrayIcon trayIcon = null;

    public TrayIcon getTrayIcon()
    {
        return trayIcon;
    }

    @Override
    public void handleMinorException(MinorException e)
    {
        getLogger().log(Level.SEVERE, e.getMessage(), e);
        e.printStackTrace();


        Platform.runLater(()-> new StreamPiAlert(e.getTitle(), e.getShortMessage(), StreamPiAlertType.WARNING).show());
    }

    @Override
    public void handleSevereException(SevereException e) {
        getLogger().log(Level.SEVERE, e.getMessage(), e);
        e.printStackTrace();
    
        Platform.runLater(()->{
            StreamPiAlert alert = new StreamPiAlert(e.getTitle(), e.getShortMessage(), StreamPiAlertType.ERROR);

            alert.setOnClicked(new StreamPiAlertListener()
            {
                @Override
                public void onClick(String txt)
                {    
                    onQuitApp();
                    Platform.exit();
                }
            });

            alert.show();
        });
    }

    @Override
    public synchronized boolean onNormalActionClicked(NormalAction action, String profileID)
    {
        try{
            getLogger().info("action "+action.getID()+" clicked!");
            action.onActionClicked();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //check if its windows UAC related
            if(e.getMessage().contains("operation requires elevation"))
            {
                handleMinorException(new MinorException(
                        "Action Execution Failed!",
                        "Error running action at ["+action.getLocation().getRow()+","+action.getLocation().getCol()+"] ("+action.getDisplayText()+")\n"+
                                "This action requires higher UAC privileges. Re-launch Stream-Pi Server with 'Administrator Privileges' in order to run this command.")
                );
            }
            else
            {
                handleMinorException(new MinorException(
                        "Action Execution Failed!",
                        "Error running action at ["+action.getLocation().getRow()+","+action.getLocation().getCol()+"] ("+action.getDisplayText()+")\n"+
                                "Check stacktrace/log to know what exactly happened\n\nMessage : \n"+e.getMessage() )
                );
            }
            return false;
        }
    }

    @Override
    public boolean onToggleActionClicked(ToggleAction action, boolean toggle, String profileID)
    {
        try{
            getLogger().info("action "+action.getID()+" clicked!");



            if(toggle)
            {
                action.onToggleOn();
            }
            else
            {
                action.onToggleOff();
            }

//            ActionBox actionBox = getDashboardBase().getActionGridPane().getActionBoxByIDAndProfileID(
//                    action.getID(),
//                    profileID
//            );
//
//            if(actionBox != null)
//            {
//                Platform.runLater(()->actionBox.init(toggle));
//            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            //check if its windows UAC related
            if(e.getMessage().contains("operation requires elevation"))
            {
                handleMinorException(new MinorException(
                        "Action Execution Failed!",
                        "Error running action at ["+action.getLocation().getRow()+","+action.getLocation().getCol()+"] ("+action.getDisplayText()+")\n"+
                                "This action requires higher UAC privileges. Re-launch Stream-Pi Server with 'Administrator Privileges' in order to run this command.")
                );
            }
            else
            {
                handleMinorException(new MinorException(
                        "Action Execution Failed!",
                        "Error running action at ["+action.getLocation().getRow()+","+action.getLocation().getCol()+"] ("+action.getDisplayText()+")\n"+
                                "Check stacktrace/log to know what exactly happened\n\nMessage : \n"+e.getMessage() )
                );
            }
            return false;
        }
    }

    @Override
    public void clearTemp() {
        Platform.runLater(() -> {
            getDashboardPane().getClientAndProfileSelectorPane().refresh();
            getDashboardPane().getActionGridPane().clear();
            getDashboardPane().getActionGridPane().setFreshRender(true);
            getDashboardPane().getActionDetailsPane().clear();
            getSettingsPane().getClientsSettings().loadData();
        });
    }

    @Override
    public void saveServerProperties()
    {
        try
        {
            ExternalPlugins.getInstance().saveServerSettings();
            getSettingsPane().getPluginsSettings().loadPlugins();
        } catch (MinorException e) {
            e.printStackTrace();
            handleMinorException(e);
        }
    }

    private void saveClientActionMain(String profileID, String actionID, SocketAddress socketAddress, boolean sendIcons)
    {
        try {
            ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress);

            ClientProfile clientProfile = clientConnection.getClient().getProfileByID(profileID);

            Action action = clientProfile.getActionByID(actionID);
            clientConnection.saveActionDetails(profileID, action);

            if(sendIcons && action.isHasIcon())
            {
                saveAllIcons(profileID, actionID, socketAddress, false);
            }


            Platform.runLater(()->{
                try {

                    ActionBox actionBox = getDashboardBase().getActionGridPane().getActionBoxByIDAndProfileID(
                            action.getID(),
                            profileID
                    );

                    if(actionBox != null)
                    {
                        Platform.runLater(actionBox::init);
                    }

                    if(getDashboardBase().getActionDetailsPane().getAction() != null)
                    {
                        // This block is executed when no Action is selected.
                        if(getDashboardPane().getActionDetailsPane().getAction().getID().equals(actionID))
                        {
                            getDashboardPane().getActionDetailsPane().setAction(action);
                            getDashboardPane().getActionDetailsPane().refresh();
                        }
                    }


                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
    }

    @Override
    public void saveClientAction(String profileID, String actionID, SocketAddress socketAddress, boolean sendIcons, boolean runAsync)
    {
        if(runAsync)
        {
            executor.execute(new Task<Void>() {
                @Override
                protected Void call()
                {
                    saveClientActionMain(profileID, actionID, socketAddress, sendIcons);
                    return null;
                }
            });
        }
        else
        {
            saveClientActionMain(profileID, actionID, socketAddress, sendIcons);
        }
    }

    @Override
    public void saveAllIcons(String profileID, String actionID, SocketAddress socketAddress)
    {
        saveAllIcons(profileID, actionID, socketAddress, true);
    }


    public void saveAllIcons(String profileID, String actionID, SocketAddress socketAddress, boolean async)
    {
        if(async)
        {
            executor.execute(new Task<Void>() {
                @Override
                protected Void call()
                {
                    saveAllIconsMain(profileID, actionID, socketAddress);
                    return null;
                }
            });
        }
        else
        {
            saveAllIconsMain(profileID, actionID, socketAddress);
        }
    }

    private void saveAllIconsMain(String profileID, String actionID, SocketAddress socketAddress)
    {
        try {
            ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress);

            ClientProfile clientProfile = clientConnection.getClient().getProfileByID(profileID);
            Action action = clientProfile.getActionByID(actionID);

            for(String eachState : action.getIcons().keySet())
            {
                clientConnection.sendIcon(profileID, actionID, eachState,
                        action.getIcon(eachState));
            }
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
    }

    @Override
    public void saveIcon(String state, String profileID, String actionID, SocketAddress socketAddress) {
        executor.execute(new Task<Void>() {
            @Override
            protected Void call()
            {
                try {
                    ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress);

                    ClientProfile clientProfile = clientConnection.getClient().getProfileByID(profileID);
                    Action action = clientProfile.getActionByID(actionID);

                    clientConnection.sendIcon(profileID, actionID, state, action.getIcon(state));
                }
                catch (SevereException e)
                {
                    handleSevereException(e);
                }
                return null;
            }
        });
    }

    @Override
    public com.stream_pi.util.platform.Platform getPlatform() {
        return ServerInfo.getInstance().getPlatform();
    }

    private Animation createOpenSettingsAnimation(Node settingsNode, Node dashboardNode) {
        Timeline openSettingsTimeline = new Timeline();
        openSettingsTimeline.setCycleCount(1);

        openSettingsTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(0.0D),
                        new KeyValue(settingsNode.opacityProperty(),
                                0.0D, Interpolator.EASE_IN),
                        new KeyValue(settingsNode.scaleXProperty(),
                                1.1D, Interpolator.EASE_IN),
                        new KeyValue(settingsNode.scaleYProperty(),
                                1.1D, Interpolator.EASE_IN),
                        new KeyValue(settingsNode.scaleZProperty(),
                                1.1D, Interpolator.EASE_IN)),
                new KeyFrame(Duration.millis(90.0D),
                        new KeyValue(settingsNode.opacityProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleXProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleYProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleZProperty(),
                                1.0D, Interpolator.LINEAR)),

                new KeyFrame(Duration.millis(0.0D),
                        new KeyValue(dashboardNode.opacityProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleXProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleYProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleZProperty(),
                                1.0D, Interpolator.LINEAR)),
                new KeyFrame(Duration.millis(90.0D),
                        new KeyValue(dashboardNode.opacityProperty(),
                                0.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleXProperty(),
                                0.9D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleYProperty(),
                                0.9D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleZProperty(),
                                0.9D, Interpolator.LINEAR))
        );

        openSettingsTimeline.setOnFinished(e -> settingsNode.toFront());
        return openSettingsTimeline;
    }

    private Animation createCloseSettingsAnimation(Node settingsNode, Node dashboardNode) {
        Timeline closeSettingsTimeline = new Timeline();
        closeSettingsTimeline.setCycleCount(1);

        closeSettingsTimeline.getKeyFrames().addAll(

                new KeyFrame(Duration.millis(0.0D),
                        new KeyValue(settingsNode.opacityProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleXProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleYProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleZProperty(),
                                1.0D, Interpolator.LINEAR)),
                new KeyFrame(Duration.millis(90.0D),
                        new KeyValue(settingsNode.opacityProperty(),
                                0.0D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleXProperty(),
                                1.1D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleYProperty(),
                                1.1D, Interpolator.LINEAR),
                        new KeyValue(settingsNode.scaleZProperty(),
                                1.1D, Interpolator.LINEAR)),

                new KeyFrame(Duration.millis(0.0D),
                        new KeyValue(dashboardNode.opacityProperty(),
                                0.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleXProperty(),
                                0.9D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleYProperty(),
                                0.9D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleZProperty(),
                                0.9D, Interpolator.LINEAR)),
                new KeyFrame(Duration.millis(90.0D),
                        new KeyValue(dashboardNode.opacityProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleXProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleYProperty(),
                                1.0D, Interpolator.LINEAR),
                        new KeyValue(dashboardNode.scaleZProperty(),
                                1.0D, Interpolator.LINEAR))

        );

        closeSettingsTimeline.setOnFinished(event1 -> {
            dashboardNode.toFront();
            executor.execute(new Task<Void>() {
                @Override
                protected Void call()  {
                    try {
                        getSettingsPane().getClientsSettings().loadData();

                        getSettingsPane().getGeneralSettings().loadDataFromConfig();
                        getSettingsPane().getPluginsSettings().loadPlugins();

                        getSettingsPane().getThemesSettings().setThemes(getThemes());
                        getSettingsPane().getThemesSettings().setCurrentThemeFullName(getCurrentTheme().getFullName());
                        getSettingsPane().getThemesSettings().loadThemes();

                        getSettingsPane().setDefaultTabToGeneral();
                    }
                    catch (SevereException e)
                    {
                        handleSevereException(e);
                    }
                    catch (MinorException e)
                    {
                        handleMinorException(e);
                    }
                    return null;
                }
            });
        });
        return closeSettingsTimeline;
    }
}
