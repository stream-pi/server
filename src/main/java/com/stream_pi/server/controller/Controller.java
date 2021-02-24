package com.stream_pi.server.controller;

import com.stream_pi.action_api.action.ServerConnection;
import com.stream_pi.action_api.action.PropertySaver;
import com.stream_pi.action_api.normalaction.NormalAction;
import com.stream_pi.server.Main;
import com.stream_pi.server.action.NormalActionPlugins;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.connection.MainServer;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.window.Base;
import com.stream_pi.server.window.dashboard.DonatePopupContent;
import com.stream_pi.server.window.firsttimeuse.FirstTimeUse;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.iohelper.IOHelper;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.awt.SystemTray;
import javafx.util.Duration;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.PopupMenu;
import java.awt.MenuItem;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Level;

public class Controller extends Base implements PropertySaver, ServerConnection
{
    MainServer mainServer;

    public void setupDashWindow() throws SevereException
    {
        try
        {
            getStage().setTitle("Stream-Pi Server - "+InetAddress.getLocalHost().getCanonicalHostName()+":"+ Config.getInstance().getPort());                   //Sets title
            getStage().setOnCloseRequest(this::onCloseRequest);
        }
        catch (UnknownHostException e)
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

            NormalActionPlugins.getInstance().setPropertySaver(this);
            NormalActionPlugins.getInstance().setServerConnection(this);


            getDashboardPane().getPluginsPane().getSettingsButton().setOnAction(event -> {
                openSettingsTimeLine.play();
            });

            getSettingsPane().getCloseButton().setOnAction(event -> {
                closeSettingsTimeLine.play();
            });

            getSettingsPane().getThemesSettings().setController(this);


            mainServer = new MainServer(this, this);


            if(getConfig().isFirstTimeUse())
            {
                Stage stage = new Stage();
                Scene s = new Scene(new FirstTimeUse(this, this), 512, 300);       
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
        
        new Thread(new Task<Void>() {
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

                        NormalActionPlugins.setPluginsLocation(getConfig().getPluginsPath());
                        NormalActionPlugins.getInstance().init();

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
        }).start();
    }

    private void setupSettingsWindowsAnimations()
    {
        Node settingsNode = getSettingsPane();
        Node dashboardNode = getDashboardPane();

        openSettingsTimeLine = new Timeline();
        openSettingsTimeLine.setCycleCount(1);


        openSettingsTimeLine.getKeyFrames().addAll(
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

        openSettingsTimeLine.setOnFinished(event1 -> {
            settingsNode.toFront();
        });


        closeSettingsTimeLine = new Timeline();
        closeSettingsTimeLine.setCycleCount(1);

        closeSettingsTimeLine.getKeyFrames().addAll(

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

        closeSettingsTimeLine.setOnFinished(event1 -> {
            dashboardNode.toFront();
            new Thread(new Task<Void>() {
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
            }).start();
        });
    }

    private Timeline openSettingsTimeLine;
    private Timeline closeSettingsTimeLine;


    public Controller(){
        mainServer = null;
    }

    public void onCloseRequest(WindowEvent event)
    {
        try
        {
            if(!Config.getInstance().getCloseOnX())
            {
                if(SystemTray.isSupported())
                {
                    minimiseApp();
                    event.consume();
                    return;
                }
            }

            getConfig().setStartupWindowSize(
                    getWidth(),
                    getHeight()
            );
            getConfig().save();
            onQuitApp();
            NormalActionPlugins.getInstance().shutDownActions();
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
        
        MenuItem showItem = new MenuItem("Show");
        showItem.addActionListener(l->{
            Platform.runLater(()->{
                getStage().show();
            });
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(l->{
            systemTray.remove(getTrayIcon());
            onQuitApp();
            Platform.exit();
        });

        popup.add(showItem);
        popup.addSeparator();
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(
            Toolkit.getDefaultToolkit().getImage(Main.class.getResource("app_icon.png")),
            "Stream-Pi Server",
            popup
        );

        trayIcon.setImageAutoSize(true);

        this.trayIcon = trayIcon;
    }

    private TrayIcon trayIcon = null;

    public TrayIcon getTrayIcon()
    {
        return trayIcon;
    }

    @Override
    public void handleMinorException(MinorException e) {
        getLogger().log(Level.SEVERE, e.getMessage(), e);
        e.printStackTrace();


        Platform.runLater(()->{
            new StreamPiAlert(e.getTitle(), e.getShortMessage(), StreamPiAlertType.WARNING).show();
        });
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
    public synchronized boolean onNormalActionClicked(NormalAction action) {
        try{
            getLogger().info("action "+action.getID()+" clicked!");
            
            action.onActionClicked();
            return true;
        }
        catch (Exception e)
        {
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
            getDashboardPane().getClientDetailsPane().refresh();
            getDashboardPane().getActionGridPane().clear();
            getDashboardPane().getActionGridPane().setFreshRender(true);
            getDashboardPane().getActionDetailsPane().clear();
            getSettingsPane().getClientsSettings().loadData();
        });
    }

    @Override
    public void saveServerProperties() {
        try {
            NormalActionPlugins.getInstance().saveServerSettings();
            getSettingsPane().getPluginsSettings().loadPlugins();
        } catch (MinorException e) {
            e.printStackTrace();
            handleMinorException(e);
        }
    }

    @Override
    public com.stream_pi.util.platform.Platform getPlatform() {
        return ServerInfo.getInstance().getPlatformType();
    }
}
