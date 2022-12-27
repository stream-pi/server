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

package com.stream_pi.server.controller;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.ServerConnection;
import com.stream_pi.action_api.actionproperty.gaugeproperties.GaugeProperties;
import com.stream_pi.action_api.externalplugin.*;
import com.stream_pi.action_api.externalplugin.inputevent.StreamPiInputEvent;
import com.stream_pi.server.Main;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.connection.MainServer;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.info.StartupFlags;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.window.Base;
import com.stream_pi.server.window.GlobalExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.firsttimeuse.FirstTimeUse;
import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.server.window.windowmenubar.WindowMenuBar;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertButton;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.*;
import com.stream_pi.util.iohelper.IOHelper;
import com.stream_pi.util.links.Links;
import com.stream_pi.util.rootchecker.RootChecker;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.stage.WindowEvent;
import javafx.util.Duration;



import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.File;
import java.net.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;


public class Controller extends Base implements ServerConnection, ToggleExtras, GaugeExtras
{
    private MainServer mainServer;
    private Animation openSettingsAnimation;
    private Animation closeSettingsAnimation;

    public Controller()
    {
        mainServer = null;
    }

    public void setupDashWindow() throws SevereException
    {
        try
        {
            getStage().setTitle(I18N.getString("windowTitle"));
            getStage().setOnCloseRequest(this::onCloseRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(e.getMessage());
        }
    }

    public MainServer getMainServer()
    {
        return mainServer;
    }

    @Override
    public void init()
    {
        try
        {
            //TODO: Refactor some stuff

            GlobalExceptionAndAlertHandler.initialise(this);
            initBase();
            setupDashWindow();

            setupSettingsWindowsAnimations();

            getSettingsBase().getCloseButton().setOnAction(event -> closeSettingsAnimation.play());

            ExternalPlugins.getInstance().setToggleExtras(this);
            ExternalPlugins.getInstance().setGaugeExtras(this);

            ExternalPlugins.getInstance().setServerConnection(this);

            getSettingsBase().getThemesSettings().setController(this);


            mainServer = new MainServer(this, this);

            registerMenuBarButtons();

            if (RootChecker.isRoot(getServerInfo().getPlatform()))
            {
                if(StartupFlags.ALLOW_ROOT)
                {
                    getLogger().warning("Stream-Pi has been started as root due to allowRoot flag. This may be unsafe and is strictly not recommended!");
                }
                else
                {
                    throw new SevereException(RootChecker.getRootNotAllowedI18NString());
                }
            }

            if(getConfig().isFirstTimeUse())
            {
                firstTimeUse = new FirstTimeUse(this, this);

                getChildren().add(firstTimeUse);

                firstTimeUse.toFront();
                getStage().show();
            }
            else
            {
                try
                {
                    if(StartupFlags.START_MINIMISED && SystemTray.isSupported())
                    {
                        minimiseApp();
                    }
                    else
                    {
                        getStage().show();
                    }



                    StreamPiAlert.setIsShowPopup(getConfig().isShowAlertsPopup());
                }
                catch(MinorException e)
                {
                    handleMinorException(e);
                }

                if(getConfig().getSoundOnActionClickedStatus())
                {
                    initSoundOnActionClicked();
                }

                ServerExecutorService.getExecutorService().execute(new Task<Void>() {
                    @Override
                    protected Void call()
                    {
                        try
                        {
                            getSettingsBase().getGeneralSettings().load();

                            //themes
                            getSettingsBase().getThemesSettings().setThemes(getThemes());
                            getSettingsBase().getThemesSettings().setCurrentThemeFullName(getCurrentTheme().getFullName());
                            getSettingsBase().getThemesSettings().loadThemes();

                            //clients
                            getSettingsBase().getClientsSettings().loadData();

                            try
                            {
                                //Plugins
                                Platform.runLater(()->{
                                    getDashboardBase().getPluginsPane().clearData();
                                    getDashboardBase().getPluginsPane().loadOtherActions();
                                });

                                ExternalPlugins.setPluginsLocation(getConfig().getPluginsPath());
                                ExternalPlugins.getInstance().init();

                                Platform.runLater(()->getDashboardBase().getPluginsPane().loadData());

                                getSettingsBase().getPluginsSettings().loadPlugins();
                            }
                            catch (MinorException e)
                            {
                                getSettingsBase().getPluginsSettings().showPluginInitError();
                                handleMinorException(e);
                            }

                            //Server
                            mainServer.setPort(getConfig().getPort());
                            mainServer.setIP(getConfig().getIP());
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
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
    }

    private void registerMenuBarButtons()
    {
        WindowMenuBar windowMenuBar = getDashboardBase().getWindowMenuBar();

        windowMenuBar.getFileMenu().getSettingsMenu().setOnAction(event -> {
            windowMenuBar.getFileMenu().hide();
            openSettingsAnimation.play();
        });


        windowMenuBar.getFileMenu().getSettingsMenu().getGeneralSettingsMenuItem().setOnAction(event -> {
            getSettingsBase().getTabPane().getSelectionModel().select(getSettingsBase().getGeneralSettingsTab());
            openSettingsAnimation.play();
        });

        windowMenuBar.getFileMenu().getSettingsMenu().getPluginsSettingsMenuItem().setOnAction(event -> {
            getSettingsBase().getTabPane().getSelectionModel().select(getSettingsBase().getPluginsSettingsTab());
            openSettingsAnimation.play();
        });

        windowMenuBar.getFileMenu().getSettingsMenu().getThemesSettingsMenuItem().setOnAction(event -> {
            getSettingsBase().getTabPane().getSelectionModel().select(getSettingsBase().getThemesSettingsTab());
            openSettingsAnimation.play();
        });

        windowMenuBar.getFileMenu().getSettingsMenu().getClientSettingsMenuItem().setOnAction(event -> {
            getSettingsBase().getTabPane().getSelectionModel().select(getSettingsBase().getClientsSettingsTab());
            openSettingsAnimation.play();
        });

        windowMenuBar.getFileMenu().getDisconnectFromAllClients().disableProperty().bind(ClientConnections.getInstance().getSizeProperty().isEqualTo(0));
        windowMenuBar.getFileMenu().getDisconnectFromAllClients().setOnAction(event -> ServerExecutorService.getExecutorService().submit(()->{
            ClientConnections.getInstance().disconnectAll();
            clearTemp();
        }));

        windowMenuBar.getFileMenu().getExitMenuItem().setOnAction(event -> fullExit());




        windowMenuBar.getShowIPPortConfigurationMenuLabel().addEventHandler(MouseEvent.MOUSE_CLICKED, event -> showIPPortConfiguration());


        windowMenuBar.getHelpMenu().getWebsiteMenuItem().setOnAction(event -> getHostServices().showDocument(Links.getWebsite()));

        windowMenuBar.getHelpMenu().getDonateMenuItem().setOnAction(event -> getHostServices().showDocument(Links.getDonateLink()));

        windowMenuBar.getHelpMenu().getAboutMenuItem().setOnAction(event -> {
            getSettingsBase().getTabPane().getSelectionModel().select(getSettingsBase().getAboutTab());
            openSettingsAnimation.play();
        });

    }

    private void showIPPortConfiguration()
    {
        try
        {
            StringBuilder content = new StringBuilder(I18N.getString("controller.Controller.port", getConfig().getPort()));

            content.append("\n").append(I18N.getString("controller.Controller.IPs"));

            if (getConfig().getIP().isBlank())
            {
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements())
                {
                    NetworkInterface n = e.nextElement();
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements())
                    {
                        InetAddress i = ee.nextElement();
                        String hostAddress = i.getHostAddress();
                        if(i instanceof Inet4Address)
                        {
                            content.append("\n").append("•").append(hostAddress);
                        }
                    }
                }
            }
            else
            {
                content.append("\n").append("•").append(getConfig().getIP());
            }

            new StreamPiAlert(I18N.getString("controller.Controller.IPPortConfiguration"),
                    content.toString(),
                    StreamPiAlertType.INFORMATION).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            handleMinorException(new MinorException(e.getMessage()));
        }


    }


    @Override
    public void showUserChooseIPAndPortDialog()
    {
        VBox vBox = new VBox();
        vBox.getStyleClass().add("user_choose_ip_and_port_dialog");
        vBox.setAlignment(Pos.CENTER);

        IPChooserComboBox ipChooserComboBox = new IPChooserComboBox();
        ipChooserComboBox.configureOptions();

        TextField portTextField = new TextField(getConfig().getPort()+"");

        String ip = getConfig().getIP();
        if (ip.isBlank())
        {
            ip = I18N.getString("combobox.IPChooserComboBox.allAddresses");
        }

        Label headLabel = new Label(I18N.getString("controller.Controller.IPAndPortNotAvailable", ip));
        headLabel.getStyleClass().add("invalid_ip_prompt_label");
        headLabel.setWrapText(true);

        vBox.getChildren().addAll(headLabel,
                new HBoxWithSpaceBetween(I18N.getString("serverIPBinding"), ipChooserComboBox),
                new HBoxInputBox(I18N.getString("serverPort"), portTextField));

        StreamPiAlert streamPiAlert = new StreamPiAlert(StreamPiAlertType.WARNING, vBox, new StreamPiAlertButton(I18N.getString("controller.Controller.proceed")));
        streamPiAlert.setDestroyAfterButtonClick(false);
        streamPiAlert.setOnClicked(new StreamPiAlertListener()
        {
            @Override
            public void onClick(StreamPiAlertButton s)
            {
                try
                {
                    String error = null;
                    int serverPort=-1;
                    try
                    {
                        serverPort = Integer.parseInt(portTextField.getText());

                        if (serverPort < 1024 && !RootChecker.isRoot(ServerInfo.getInstance().getPlatform()))
                        {
                            error = I18N.getString("serverPortMustBeGreaterThan1024");
                        }
                        else if(serverPort > 65535)
                        {
                            error = I18N.getString("serverPortMustBeLesserThan65535");
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        error = I18N.getString("serverPortMustBeInteger");
                    }

                    if (error!=null)
                    {
                        new StreamPiAlert(error).show();
                    }
                    else
                    {
                        streamPiAlert.destroy();
                        getConfig().setIP(ipChooserComboBox.getSelectedIP());
                        getConfig().setPort(serverPort);
                        getConfig().save();
                        restart();
                    }
                }
                catch (SevereException e)
                {
                    handleSevereException(e);
                    e.printStackTrace();
                }
            }
        });

        streamPiAlert.show();

        System.out.println("SHOW@!!!!!");
    }

    @Override
    public void factoryReset()
    {
        getLogger().info("Reset to factory ...");

        onQuitApp();

        IOHelper.deleteFile(getServerInfo().getPrePath(), true);

        StreamPiAlert streamPiAlert = new StreamPiAlert("Stream-Pi Server has been successfully reset. The application shall now quit.", StreamPiAlertType.INFORMATION, StreamPiAlertButton.OK);

        streamPiAlert.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton streamPiAlertButton) {
                exit();
            }
        });

        streamPiAlert.show();
    }

    @Override
    public void fullExit()
    {
        onQuitApp();
        exit();
    }

    private void setupSettingsWindowsAnimations()
    {
        Node settingsNode = getSettingsBase();
        Node dashboardNode = getDashboardBase();

        openSettingsAnimation = createOpenSettingsAnimation(settingsNode, dashboardNode);
        closeSettingsAnimation = createCloseSettingsAnimation(settingsNode, dashboardNode);
    }

    public void onCloseRequest(WindowEvent event)
    {
        try
        {
            if(getConfig().getMinimiseToSystemTrayOnClose() &&
                    SystemTray.isSupported() && !getMainServer().isFailedToStart().get() &&
                    !getConfig().isFirstTimeUse())
            {
                minimiseApp();

                if(event != null)
                {
                    event.consume();
                }
            }
            else
            {
                fullExit();
            }
        }
        catch (MinorException e)
        {
            handleMinorException(e);
        }
    }

    public void onQuitApp()
    {
        getLogger().info("Shutting down ...");

        try
        {
            if(getConfig() != null)
            {
                getConfig().setStartupWindowSize(getWidth(), getHeight());
                getConfig().setRightDividerPositions(getDashboardBase().getSplitPane().getDividerPositions());
                getConfig().setLeftDividerPositions(getDashboardBase().getLeftSplitPane().getDividerPositions());
                getConfig().save();
            }
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }

        stopServerAndAllConnections();
        ExternalPlugins.getInstance().shutDownActions();
        closeLogger();
        Config.nullify();
    }

    public void exit()
    {
        ServerExecutorService.getExecutorService().shutdown();
        Platform.exit();
    }

    private void stopServerAndAllConnections()
    {
        if(mainServer!=null)
            mainServer.stopListeningForConnections();

        ClientConnections.getInstance().disconnectAll();
    }

    @Override
    public void restart()
    {
        getLogger().info("Restarting ...");

        onQuitApp();
        Platform.runLater(this::init);
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

            getStage().setOnShown(windowEvent -> systemTray.remove(getTrayIcon()));
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

        MenuItem exitItem = new MenuItem(I18N.getString("exit"));
        exitItem.addActionListener(l->{
            systemTray.remove(getTrayIcon());
            fullExit();
        });

        MenuItem openItem = new MenuItem(I18N.getString("controller.Controller.systemTrayOpen"));
        openItem.addActionListener(l-> unMinimizeApp());

        popup.add(openItem);
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(Main.class.getResource("icons/24x24.png")),
                I18N.getString("windowTitle"),
                popup
        );

        trayIcon.addActionListener(l-> unMinimizeApp());

        trayIcon.setImageAutoSize(true);

        this.trayIcon = trayIcon;
    }

    private TrayIcon trayIcon = null;

    public TrayIcon getTrayIcon()
    {
        return trayIcon;
    }


    private void unMinimizeApp()
    {
        Platform.runLater(()->{
            getStage().show();
            getStage().setAlwaysOnTop(true);
            getStage().setAlwaysOnTop(false);
        });
    }

    @Override
    public StreamPiAlert handleMinorException(MinorException e)
    {
       return handleMinorException(e.getMessage(), e);
    }

    @Override
    public StreamPiAlert handleMinorException(String message, MinorException e)
    {
        getLogger().log(Level.SEVERE, message, e);
        e.printStackTrace();
        StreamPiAlert alert = new StreamPiAlert(e.getTitle(), message, StreamPiAlertType.WARNING);
        alert.show();
        return alert;
    }

    @Override
    public StreamPiAlert handleSevereException(SevereException e)
    {
        return handleSevereException(e.getMessage(), e);
    }

    @Override
    public StreamPiAlert handleSevereException(String message, SevereException e)
    {
        getLogger().log(Level.SEVERE, message, e);
        e.printStackTrace();

        StreamPiAlert alert = new StreamPiAlert(e.getTitle(), message +"\n" + I18N.getString("controller.Controller.willNowExit"), StreamPiAlertType.ERROR);

        alert.setOnClicked(new StreamPiAlertListener()
        {
            @Override
            public void onClick(StreamPiAlertButton s)
            {
                fullExit();
            }
        });

        alert.show();

        return alert;
    }

    private AudioClip audioClip = null;

    private void playSound()
    {
        if(audioClip.isPlaying())
        {
            Platform.runLater(audioClip::stop);
            return;
        }

        Platform.runLater(audioClip::play);
    }

    @Override
    public void initSoundOnActionClicked()
    {
        File file = new File(getConfig().getSoundOnActionClickedFilePath());

        if(!file.exists())
        {
            audioClip = null;
            getConfig().setSoundOnActionClickedStatus(false);
            getConfig().setSoundOnActionClickedFilePath("");
            handleMinorException(new MinorException(I18N.getString("controller.Controller.actionClickSoundFileMissing")));
            return;
        }

        audioClip = new AudioClip(file.toURI().toString());
    }

    @Override
    public synchronized void onSetToggleStatus(Client client, String profileID, String actionID, boolean toggleStatus)
    {
        Action action = client.getProfileByID(profileID).getActionByID(actionID);

        if(action.isInvalid())
        {
            handleMinorException(new MinorException(I18N.getString("controller.Controller.pluginNotInstalledOnServer", action.getUniqueID())));
            return;
        }

        ToggleAction toggleAction = (ToggleAction) action;

        ServerExecutorService.getExecutorService().submit(()->{
            try
            {
                runToggleOnOffMethod(toggleAction, toggleStatus);
            }
            catch (MinorException e)
            {
                sendActionFailed(e, client.getRemoteSocketAddress(), profileID, action);
            }
        });
    }

    public void runToggleOnOffMethod(ToggleAction toggleAction, boolean toggleStatus) throws MinorException
    {
        toggleAction.setCurrentStatus(toggleStatus);

        if(toggleStatus)
        {
            toggleAction.onToggleOn();
        }
        else
        {
            toggleAction.onToggleOff();
        }
    }


    private String invalidActionUniqueID = null;

    @Override
    public synchronized void onInputEventInAction(Client client, String profileID, String actionID, StreamPiInputEvent streamPiInputEvent)
    {
        ClientProfile profile = client.getProfileByID(profileID);

        Action action = profile.getActionByID(actionID);

        if(action.isInvalid())
        {
            if (!action.getUniqueID().equals(invalidActionUniqueID))
            {
                invalidActionUniqueID = action.getUniqueID();
                handleMinorException(new MinorException(I18N.getString("controller.Controller.pluginNotInstalledOnServer", action.getUniqueID())));
            }

            return;
        }

        invalidActionUniqueID = null;


        ServerExecutorService.getExecutorService().submit(()->{
            try
            {
                if(streamPiInputEvent.getEventType() == MouseEvent.MOUSE_CLICKED)
                {
                    if(action.getActionType() == ActionType.NORMAL)
                    {
                        ((NormalAction) action).onActionClicked();
                    }
                    else if(action.getActionType() == ActionType.COMBINE)
                    {
                        for(int i = 0;i<action.getClientProperties().getSize(); i++)
                        {
                            try
                            {
                                Action childAction = profile.getActionByID(
                                        action.getClientProperties().getSingleProperty(i+"").getRawValue()
                                );

                                Thread.sleep(childAction.getDelayBeforeExecuting());

                                if (childAction.getActionType() == ActionType.NORMAL)
                                {
                                    ((NormalAction) childAction).onActionClicked();
                                }
                                else if (childAction.getActionType() == ActionType.TOGGLE)
                                {
                                    ToggleAction toggleAction = (ToggleAction) childAction;

                                    runToggleOnOffMethod(toggleAction, !toggleAction.getCurrentStatus());

                                    setToggleStatus(
                                            toggleAction.getCurrentStatus(),
                                            profileID,
                                            childAction.getID(),
                                            client.getRemoteSocketAddress()
                                    );
                                }
                            }
                            catch (MinorException e)
                            {
                                handleMinorException(e);
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    playSound();
                }

                ((ExternalPlugin) action).onInputEventReceived(streamPiInputEvent);
            }
            catch (MinorException e)
            {
                sendActionFailed(e, client.getRemoteSocketAddress(), profileID, action);
            }
        });
    }


    @Override
    public void clearTemp() {
        Platform.runLater(() -> {
            getDashboardBase().getClientAndProfileSelectorPane().refresh();
            getDashboardBase().getActionGridPane().clear();
            getDashboardBase().getActionGridPane().setFreshRender(true);
            getDashboardBase().getActionDetailsPane().clear();
            getSettingsBase().getClientsSettings().loadData();
        });
    }

    @Override
    public void saveServerProperties(String uniqueID)
    {
        try
        {
            ExternalPlugins.getInstance().saveServerSettings();
            getSettingsBase().getPluginsSettings().reloadPlugin(uniqueID);
        }
        catch (MinorException e)
        {
            handleMinorException(e);
        }
    }

    @Override
    public boolean saveServerPropertiesProvidedByUser(String uniqueID)
    {
        try
        {
            StringBuilder errors = getSettingsBase().getPluginsSettings().validatePluginProperties(uniqueID);

            if(!errors.toString().isEmpty())
            {
                throw new MinorException(I18N.getString("validationError", errors));
            }

            getSettingsBase().getPluginsSettings().saveServerPropertiesFromFields(uniqueID);

            return true;
        }
        catch (MinorException e)
        {
            handleMinorException(e);
            return false;
        }
    }

    private void saveClientActionMain(String profileID, String actionID, SocketAddress socketAddress, boolean sendIcons)
    {
        try
        {
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

                    ActionBox actionBox = getDashboardBase().getActionGridPane().getActionBoxByProfileAndID(
                            profileID,
                            action.getID()
                    );

                    if(actionBox != null)
                    {
                        Platform.runLater(actionBox::init);
                    }

                    if(getDashboardBase().getActionDetailsPane().getAction() != null)
                    {
                        // This block is executed when no Action is selected.
                        if(getDashboardBase().getActionDetailsPane().getAction().getID().equals(actionID))
                        {
                            getDashboardBase().getActionDetailsPane().setAction(action);
                            getDashboardBase().getActionDetailsPane().refresh();
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
            ServerExecutorService.getExecutorService().execute(new Task<Void>() {
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
            ServerExecutorService.getExecutorService().execute(new Task<Void>() {
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
        try
        {
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
    public void saveIcon(String state, String profileID, String actionID, SocketAddress socketAddress)
    {
        ServerExecutorService.getExecutorService().execute(new Task<Void>() {
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
    public void updateTemporaryDisplayText(String profileID, String actionID, SocketAddress socketAddress, String displayText) throws MinorException
    {
        try
        {
            ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress);

            if (clientConnection == null)
            {
                throw new MinorException("Client does not exist");
            }

            ClientProfile clientProfile = clientConnection.getClient().getProfileByID(profileID);

            Action action = clientProfile.getActionByID(actionID);
            clientConnection.updateActionTemporaryDisplayText(profileID, action, displayText);

            action.setTemporaryDisplayText(displayText);
        }
        catch (SevereException e)
        {
            handleSevereException(e);
        }
    }

    @Override
    public boolean isConnected(SocketAddress socketAddress)
    {
        return ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress) != null;
    }

    @Override
    public ExecutorService getExecutorService() {
        return ServerExecutorService.getExecutorService();
    }

    @Override
    public com.stream_pi.util.platform.Platform getPlatform() {
        return ServerInfo.getInstance().getPlatform();
    }

    @Override
    public void sendActionFailed(MinorException exception, SocketAddress socketAddress, String profileID, Action action)
    {
        exception.setTitle(
                I18N.getString("controller.Controller.errorWhileRunningAction",
                (action.getDisplayText() == null) ? action.getName() : action.getDisplayText())
        );

        handleMinorException(exception);

        if(profileID==null || action.getID() == null)
            return;

        ServerExecutorService.getExecutorService().execute(new Task<Void>() {
            @Override
            protected Void call()
            {
                try {
                    ClientConnections.getInstance().getClientConnectionBySocketAddress(socketAddress)
                            .sendActionFailed(profileID, action.getID());
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
    public Locale getCurrentLanguageLocale()
    {
        return getConfig().getCurrentLanguageLocale();
    }

    private Animation createOpenSettingsAnimation(Node settingsNode, Node dashboardNode)
    {
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

    private Animation createCloseSettingsAnimation(Node settingsNode, Node dashboardNode)
    {
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
            ServerExecutorService.getExecutorService().execute(new Task<Void>() {
                @Override
                protected Void call()  {
                    try {
                        getSettingsBase().getClientsSettings().loadData();

                        getSettingsBase().getGeneralSettings().load();
                        getSettingsBase().getPluginsSettings().reloadPlugins();

                        getSettingsBase().getThemesSettings().setThemes(getThemes());
                        getSettingsBase().getThemesSettings().setCurrentThemeFullName(getCurrentTheme().getFullName());
                        getSettingsBase().getThemesSettings().loadThemes();

                        getSettingsBase().setDefaultTabToGeneral();
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

    @Override
    public void setToggleStatus(boolean currentStatus, String profileID, String actionID, SocketAddress clientSocketAddress)
    {
        ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(
                clientSocketAddress
        );

        if(clientConnection == null)
        {
            getLogger().warning("setToggleStatus failed because no client found with given socket address");
            return;
        }

        ServerExecutorService.getExecutorService().submit(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    clientConnection.setToggleStatus(currentStatus, profileID, actionID);
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
    public void updateGauge(GaugeProperties gaugeProperties, String profileID, String actionID, SocketAddress clientSocketAddress)
    {
        ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(
                clientSocketAddress
        );

        if(clientConnection == null)
        {
            getLogger().warning("updateGauge failed because no client found with given socket address");
            return;
        }


        ActionBox actionBox = getDashboardBase().getActionGridPane().getActionBoxByProfileAndID(profileID, actionID);

        if (actionBox != null)
        {
            Platform.runLater(()-> actionBox.updateGauge(gaugeProperties));
        }

        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    clientConnection.updateActionGaugeProperties(gaugeProperties, profileID, actionID);
                }
                catch (SevereException e)
                {
                    handleSevereException(e);
                }
                return null;
            }
        }).start();

    }

    @Override
    public void updateGaugeValue(double value, String profileID, String actionID, SocketAddress clientSocketAddress)
    {

        ClientConnection clientConnection = ClientConnections.getInstance().getClientConnectionBySocketAddress(
                clientSocketAddress
        );

        if(clientConnection == null)
        {
            getLogger().warning("updateGaugeValue failed because no client found with given socket address");
            return;
        }


        ActionBox actionBox = getDashboardBase().getActionGridPane().getActionBoxByProfileAndID(profileID, actionID);

        if (actionBox != null && actionBox.getAction() != null)
        {
            Platform.runLater(()-> actionBox.updateGaugeValue(value));
        }

        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    clientConnection.setActionGaugeValue(value, profileID, actionID);
                }
                catch (SevereException e)
                {
                    handleSevereException(e);
                }
                return null;
            }
        }).start();
    }
}
