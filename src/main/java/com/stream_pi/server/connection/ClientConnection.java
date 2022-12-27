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

package com.stream_pi.server.connection;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.gaugeproperties.GaugeProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.StringProperty;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.externalplugin.GaugeAction;
import com.stream_pi.action_api.externalplugin.inputevent.StreamPiInputEvent;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.client.ClientTheme;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.comms.DisconnectReason;
import com.stream_pi.util.comms.Message;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.exception.StreamPiException;
import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.ReleaseStatus;
import com.stream_pi.util.version.Version;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection extends Thread
{
    private Socket socket;
    private ServerListener serverListener;
    private AtomicBoolean stop = new AtomicBoolean(false);

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private Logger logger;

    private Client client = null;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public ClientConnection(Socket socket, ServerListener serverListener, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.socket = socket;

        this.serverListener = serverListener;

        logger = Logger.getLogger(ClientConnection.class.getName());

        try
        {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException(I18N.getString("connection.ClientConnection.failedToSetUpIOStreams", e)));
        }

        start();
    }

    public synchronized void exit()
    {
        exit(true);
    }

    private boolean isAutoRemove = true;
    public synchronized void exit(boolean isAutoRemove)
    {
        if(stop.get())
            return;

        this.isAutoRemove = isAutoRemove;
        callOnClientDisconnectOnAllActions();

        logger.info("Stopping ...");

        try
        {
            if(socket !=null)
            {
                logger.info("Stopping connection "+getRemoteSocketAddress());
                disconnect();
            }
        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }

    public synchronized void exitAndRemove()
    {
        exit();

        if(isAutoRemove)
        {
            removeConnection();
        }
    }

    public void callOnClientDisconnectOnAllActions()
    {
        for(ClientProfile profile : getClient().getAllClientProfiles())
        {
            for (String actionID : profile.getActionsKeySet())
            {
                Action action = profile.getActionByID(actionID);
                if(action instanceof ExternalPlugin)
                {
                    try
                    {
                        ExternalPlugin externalPlugin = (ExternalPlugin) action;
                        externalPlugin.onClientDisconnected();
                    }
                    catch (MinorException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "callOnClientDisconnectOnAllActions()", action.getUniqueID(), e.getMessage()), e);
                    }
                }
            }
        }
    }

    public void removeConnection()
    {
        ClientConnections.getInstance().removeConnection(this);
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void sendIcon(String profileID, String actionID, String state, byte[] icon) throws SevereException
    {
        getLogger().info("Sending icon "+state+" len "+icon.length+"; profile:"+profileID+"; ID:"+actionID+"\n\n\n\n");
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        Message message = new Message("action_icon");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        message.setValue("icon_state", state);
        message.setValue("icon", icon);
        sendMessage(message);
    }

    public void initAfterConnectionQueryReceive(Message message) throws StreamPiException
    {
        logger.info("Setting up client object ...");

        Version clientVersion;
        Version communicationProtocol;
        Version themeAPIVersion;

        ReleaseStatus releaseStatus;

        clientVersion = (Version) message.getValue("version");
        releaseStatus = (ReleaseStatus) message.getValue("release_status");
        communicationProtocol = (Version) message.getValue("communication_protocol_version");
        themeAPIVersion = (Version) message.getValue("theme_api_version");

        if (communicationProtocol.getMajor() != ServerInfo.getInstance().getCommunicationProtocolVersion().getMajor())
        {
            disconnect(DisconnectReason.COMMUNICATION_PROTOCOL_MISMATCH);
            throw new MinorException(DisconnectReason.COMMUNICATION_PROTOCOL_MISMATCH.getMessage()+"\n"+
                    I18N.getString("connection.ClientConnection.serverClientCommunicationProtocolVersions",
                            ServerInfo.getInstance().getCommunicationProtocolVersion().getText(), communicationProtocol.getText()
                    )
            );
        }

        Orientation orientation = (Orientation) message.getValue("orientation");

        client = new Client(clientVersion, releaseStatus, communicationProtocol, themeAPIVersion, (String) message.getValue("name"),
                (Platform) message.getValue("platform"), socket.getRemoteSocketAddress(), orientation);

        client.setDisplayWidth((Double) message.getValue("display_width"));
        client.setDisplayHeight((Double) message.getValue("display_height"));
        client.setDefaultProfileID((String) message.getValue("default_profile_ID"));
        client.setDefaultThemeFullName((String) message.getValue("default_theme_full_name"));
        
        //call get profiles command.
        serverListener.clearTemp();
    }

    public void updateClientDetails(Message message)
    {
        logger.info("Setting up client object ...");

        client.setName((String) message.getValue("name"));
        client.setDisplayWidth((Double) message.getValue("display_width"));
        client.setDisplayHeight((Double) message.getValue("display_height"));
        client.setDefaultProfileID((String) message.getValue("default_profile_ID"));
        client.setDefaultThemeFullName((String) message.getValue("default_theme_full_name"));

        serverListener.getSettingsBase().getClientsSettings().loadData();
    }

    public synchronized Client getClient()
    {
        return client;
    }

    @Override
    public void run()
    {
        try
        {
            initAfterConnectionQuerySend();
        }
        catch (SevereException e)
        {
            e.printStackTrace();

            exceptionAndAlertHandler.handleSevereException(e);

            exitAndRemove();
            return;
        }

        try
        {
            while(!stop.get())
            {
                try
                {
                    Message message = (Message) ois.readObject();

                    String header = message.getHeader();

                    switch (header)
                    {
                        case "disconnect" :                 clientDisconnected(message);
                            break;

                        case "register_client_details" :    initAfterConnectionQueryReceive(message);
                            getProfilesFromClient();
                            getThemesFromClient();
                            break;

                        case "update_client_details" :      updateClientDetails(message);
                            break;

                        case "client_screen_details" :      onClientScreenDetailsReceived(message);
                            break;

                        case "profiles" :                   registerProfilesFromClient(message);
                            break;

                        case "profile_details" :            registerProfileDetailsFromClient(message);
                            break;

                        case "action_details" :             registerActionToProfile(message);
                            break;

                        case "themes":                      registerThemes(message);
                            break;

                        case "client_orientation":          updateClientOrientation(message);
                            break;

                        case "refresh_all_gauges":          refreshAllGauges();
                            break;

                        case "input_event_in_action":       onInputEventInAction(message);
                            break;

                        case "set_toggle_status":           onSetToggleStatus(message);
                            break;

                        default:                    logger.warning("Command '"+header+"' does not match records. Make sure client and server versions are equal.");


                    }
                }
                catch (IOException | ClassNotFoundException e)
                {
                    logger.log(Level.SEVERE, e.getMessage());
                    e.printStackTrace();


                    if(!stop.get())
                    {
                        removeConnection();
                        serverListener.clearTemp();
                        throw new MinorException(I18N.getString("connection.ClientConnection.accidentallyDisconnectedFromClient", getClient().getName()));
                    }

                    exitAndRemove();

                    serverListener.clearTemp();

                    return;
                }
            }
        }
        catch (StreamPiException e)
        {
            e.printStackTrace();


            if(e instanceof MinorException)
                exceptionAndAlertHandler.handleMinorException((MinorException) e);
            else if (e instanceof SevereException)
                exceptionAndAlertHandler.handleSevereException((SevereException) e);

        }
    }

    // commands


    private void onSetToggleStatus(Message message) throws MinorException
    {
        String profileID = (String) message.getValue("profile_ID");
        String actionID = (String) message.getValue("ID");
        boolean newStatus = (boolean) message.getValue("toggle_status");

        serverListener.onSetToggleStatus(getClient(), profileID, actionID, newStatus);
    }

    private void updateClientOrientation(Message message) throws MinorException
    {
        getClient().setOrientation((Orientation) message.getValue("orientation"));
        if(serverListener.getDashboardBase().getActionGridPane().getClientProfile() != null)
        {
            javafx.application.Platform.runLater(()-> serverListener.getDashboardBase().reDrawProfile());
        }
    }

    public void initAfterConnectionQuerySend() throws SevereException
    {
        logger.info("Asking client details ...");
        sendMessage(new Message("get_client_details"));

        Message message = new Message("server_details");
        message.setValue("name", Config.getInstance().getServerName());
        sendMessage(message);
    }

    public void disconnect() throws SevereException
    {
        disconnect(null);
    }

    public void disconnect(DisconnectReason disconnectReason) throws SevereException
    {
        if(stop.get())
            return;

        stop.set(true);

        logger.info("Sending client disconnect message ...");

        Message m = new Message("disconnect");
        m.setValue("reason", disconnectReason);
        sendMessage(m);

        try
        {
            if(!socket.isClosed())
                socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException(I18N.getString("connection.ClientConnection.failedToCloseSocket", e.getLocalizedMessage()));
        }
    }

    public synchronized void sendMessage(Message message) throws SevereException
    {
        try
        {
            logger.info("Sending message with heading "+message.getHeader()+" ...");
            oos.writeObject(message);
            oos.flush();
            logger.info("... Done!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException(I18N.getString("connection.ClientConnection.failedToWriteToIOStream", e.getLocalizedMessage()));
        }
    }

    public void clientDisconnected(Message message)
    {
        stop.set(true);

        if (message.getValue("reason") == null)
        {
            new StreamPiAlert(I18N.getString("connection.ClientConnection.disconnectedFromClient", getClient().getName()), StreamPiAlertType.WARNING).show();
        }
        else
        {
            new StreamPiAlert(I18N.getString("connection.ClientConnection.disconnectedFromClient", getClient().getName()), ((DisconnectReason) message.getValue("reason")).getMessage(), StreamPiAlertType.WARNING).show();
        }

        exitAndRemove();
        serverListener.clearTemp();
    }

    public void getProfilesFromClient() throws StreamPiException
    {
        logger.info("Asking client to send profiles ...");
        sendMessage(new Message("get_profiles"));
    }

    public void getThemesFromClient() throws StreamPiException
    {
        logger.info("Asking clients to send themes ...");
        sendMessage(new Message("get_themes"));
    }

    public void registerThemes(Message message)
    {
        int size = (int) message.getValue("size");

        for (int i = 0; i<size; i++)
        {
            ClientTheme clientTheme = new ClientTheme(
                    (String) message.getValue("theme_"+i+"_full_name"),
                    (String) message.getValue("theme_"+i+"_short_name"),
                    (String) message.getValue("theme_"+i+"_author"),
                    (Version) message.getValue("theme_"+i+"_version")
            );

            try
            {
                getClient().addTheme(clientTheme);
            }
            catch (CloneNotSupportedException e)
            {
                logger.log(Level.SEVERE, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private HashMap<String, Integer> temporaryProfilesCheck = null;


    public void registerProfilesFromClient(Message message) throws StreamPiException
    {
        logger.info("Registering profiles ...");

        int size = (int) message.getValue("size");

        temporaryProfilesCheck = new HashMap<>();

        for (int i = 0; i< size; i++)
        {
            temporaryProfilesCheck.put((String) message.getValue("profile_"+i+"_ID"), (Integer) message.getValue("profile_"+i+"_actions_size"));
            getProfileDetailsFromClient((String) message.getValue("profile_"+i+"_ID"));
        }
    }

    public void getProfileDetailsFromClient(String ID) throws StreamPiException
    {
        logger.info("Asking client to send details of profile : "+ID);
        Message message = new Message("get_profile_details");
        message.setValue("ID", ID);
        sendMessage(message);
    }


    public void registerProfileDetailsFromClient(Message message)
    {
        String ID = (String) message.getValue("ID");
        logger.info("Registering details for profile : "+ID);

        String name = (String) message.getValue("name");
        int rows = (int) message.getValue("rows");
        int cols = (int) message.getValue("cols");
        double actionSize = (double) message.getValue("action_size");
        double actionGap = (double) message.getValue("action_gap");
        double actionDefaultDisplayTextFontSize = (double) message.getValue("action_default_display_text_font_size");

        ClientProfile clientProfile = new ClientProfile(name, ID, rows, cols, actionSize, actionGap, actionDefaultDisplayTextFontSize);

        logger.info("Added client profile "+clientProfile.getName());
        try
        {
            getClient().addProfile(clientProfile);
        }
        catch (CloneNotSupportedException e)
        {
            logger.severe(e.getMessage());
            e.printStackTrace();
        }
        serverListener.clearTemp();
    }

    public synchronized void registerActionToProfile(Message message) throws StreamPiException
    {
        String profileID = (String) message.getValue("profile_ID");

        String ID = (String) message.getValue("ID");
        ActionType actionType = (ActionType) message.getValue("type");

        //display
        String bgColourHex = (String) message.getValue("bg_colour_hex");

        int allIconStateNamesSize = (int) message.getValue("icon_state_names_size");



        String currentIconState = (String) message.getValue("current_icon_state");

        //text
        boolean isShowDisplayText = (boolean) message.getValue("is_show_display_text");
        String displayTextFontColourHex = (String) message.getValue("display_text_font_colour_hex");
        String displayText = (String) message.getValue("display_text");
        double displayTextFontSize = (Double) message.getValue("display_text_font_size");
        DisplayTextAlignment displayTextAlignment = (DisplayTextAlignment) message.getValue("display_text_alignment");


        Location location = (Location) message.getValue("location");

        String parent = (String) message.getValue("parent");

        int delayBeforeExecuting = (int) message.getValue("delay_before_executing");


        //client properties

        boolean isAnimatedGauge = (boolean) message.getValue("is_gauge_animated");

        int clientPropertiesSize = (int) message.getValue("client_properties_size");

        ClientProperties clientProperties = new ClientProperties();

        if(actionType == ActionType.FOLDER)
            clientProperties.setDuplicatePropertyAllowed(true);

        for(int i = 0;i<clientPropertiesSize; i++)
        {
            StringProperty property = new StringProperty((String) message.getValue("client_property_"+i+"_name"));
            property.setRawValue((String) message.getValue("client_property_"+i+"_raw_value"));

            clientProperties.addProperty(property);
        }

        //set up action

        temporaryProfilesCheck.replace(
                profileID,
                (temporaryProfilesCheck.get(profileID) - 1)
        );

        if(temporaryProfilesCheck.get(profileID) == 0)
        {
            temporaryProfilesCheck.remove(profileID);
        }


        //action toBeAdded = null;

        String uniqueID = (String) message.getValue("unique_ID");
        boolean isInvalidAction = false;

        if(actionType == ActionType.NORMAL || actionType == ActionType.TOGGLE || actionType == ActionType.GAUGE)
        {
            Version version = (Version) message.getValue("version");

            ExternalPlugin originalAction = ExternalPlugins.getInstance().getPluginByUniqueID(uniqueID);

            if(originalAction == null)
            {
                isInvalidAction = true;
            }
            else
            {
                if(originalAction.getVersion().getMajor() != version.getMajor())
                {
                    isInvalidAction = true;
                }
                else
                {
                    try
                    {
                        ExternalPlugin newPlugin = originalAction.clone();


                        for(int i = 0;i<allIconStateNamesSize; i++)
                        {
                            newPlugin.addIcon((String) message.getValue("icon_state_"+i), (byte[]) message.getValue("icon_"+i));
                        }

                        newPlugin.setID(ID);
                        newPlugin.setProfileID(profileID);
                        newPlugin.setSocketAddressForClient(socket.getRemoteSocketAddress());

                        newPlugin.setBgColourHex(bgColourHex);
                        newPlugin.setCurrentIconState(currentIconState);

                        newPlugin.setShowDisplayText(isShowDisplayText);
                        newPlugin.setDisplayTextFontColourHex(displayTextFontColourHex);
                        newPlugin.setDisplayText(displayText);
                        newPlugin.setDisplayTextAlignment(displayTextAlignment);
                        newPlugin.setDisplayTextFontSize(displayTextFontSize);

                        newPlugin.setLocation(location);

                        newPlugin.setParent(parent);

                        newPlugin.setDelayBeforeExecuting(delayBeforeExecuting);

                        if (actionType == ActionType.GAUGE)
                        {
                            newPlugin.setGaugeAnimated(isAnimatedGauge);
                        }

                        ClientProperties finalClientProperties = new ClientProperties();


                        for(Property property : originalAction.getClientProperties().get())
                        {
                            for(int i = 0;i<clientProperties.getSize();i++)
                            {
                                Property property1 = clientProperties.get().get(i);
                                if (property.getName().equals(property1.getName()))
                                {
                                    property.setRawValue(property1.getRawValue());

                                    finalClientProperties.addProperty(property);
                                }
                            }
                        }

                        newPlugin.setClientProperties(finalClientProperties);

                        getClient().getProfileByID(profileID).addAction(newPlugin);

                        new Thread(new Task<Void>() {
                            @Override
                            protected Void call()
                            {
                                try
                                {
                                    newPlugin.onClientConnected();
                                }
                                catch (MinorException e)
                                {
                                    exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "onClientConnected()", uniqueID, e.getMessage()), e);
                                }

                                try
                                {
                                    if (newPlugin instanceof GaugeAction)
                                    {
                                        updateActionGaugeProperties(newPlugin.getGaugeProperties(), newPlugin.getProfileID(), newPlugin.getID());

                                        GaugeAction gaugeAction = (GaugeAction) newPlugin;
                                        gaugeAction.cancelGaugeUpdaterFuture();
                                        gaugeAction.onGaugeInit();
                                    }
                                }
                                catch (MinorException e)
                                {
                                    exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "onGaugeInit()", uniqueID, e.getMessage()), e);
                                }
                                catch (SevereException e)
                                {
                                    exceptionAndAlertHandler.handleSevereException(I18N.getString("methodCallFailed", "updateActionGaugeProperties", uniqueID, e.getMessage()), e);
                                }
                                return null;
                            }
                        }).start();


                    }
                    catch (CloneNotSupportedException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(new MinorException(I18N.getString("connection.ClientConnection.failedToCloneAction", uniqueID)));
                    }

                    checkIfReady();
                    return;
                }
            }
        }


        Action action = null;

        if(isInvalidAction)
        {
            Version version = (Version) message.getValue("version");

            action = new Action(uniqueID);
            action.setInvalid(true);
            action.setVersion(version);
        }
        else
        {
            if(actionType == ActionType.COMBINE)
            {
                action = new CombineAction();
            }
            else if(actionType == ActionType.FOLDER)
            {
                action = new FolderAction();
            }
        }


        action.setID(ID);
        action.setProfileID(profileID);
        action.setSocketAddressForClient(socket.getRemoteSocketAddress());

        for(int i = 0;i<allIconStateNamesSize; i++)
        {
            action.addIcon((String) message.getValue("icon_state_"+i), (byte[]) message.getValue("icon_"+i));
        }

        action.setBgColourHex(bgColourHex);
        action.setCurrentIconState(currentIconState);

        action.setDelayBeforeExecuting(delayBeforeExecuting);

        action.setShowDisplayText(isShowDisplayText);
        action.setDisplayTextFontColourHex(displayTextFontColourHex);
        action.setDisplayText(displayText);
        action.setDisplayTextAlignment(displayTextAlignment);
        action.setDisplayTextFontSize(displayTextFontSize);

        action.setLocation(location);

        action.setParent(parent);


        action.setClientProperties(clientProperties);


        try
        {
            getClient().getProfileByID(profileID).addAction(action);
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException("action", "Unable to clone"));
        }

        checkIfReady();
    }

    public void checkIfReady() throws SevereException
    {
        if(temporaryProfilesCheck.size() == 0)
        {
            temporaryProfilesCheck = null;
            sendMessage(new Message("ready"));
        }
    }

    public synchronized void updateActionTemporaryDisplayText(String profileID, Action action, String displayText) throws SevereException
    {
        Message message = new Message("update_action_temporary_display_text");

        message.setValue("profile_ID", profileID);
        message.setValue("ID", action.getID());
        message.setValue("display_text", displayText);

        sendMessage(message);
    }

    public synchronized void saveActionDetails(String profileID, Action action) throws SevereException
    {
        Message message = new Message("save_action_details");

        message.setValue("profile_ID", profileID);
        message.setValue("ID", action.getID());
        message.setValue("type", action.getActionType());

        if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE || action.getActionType() == ActionType.GAUGE)
        {
            message.setValue("unique_ID", action.getUniqueID());
            message.setValue("version", action.getVersion());
        }

        //display

        message.setValue("bg_colour_hex", action.getBgColourHex());

        //icon
        message.setValue("icon_state_names_size", action.getIcons().size());

        int i = 0;
        for(String eachState : action.getIcons().keySet())
        {
            message.setValue("icon_state_"+i, eachState);
            i+=1;
        }

        message.setValue("current_icon_state", action.getCurrentIconState());

        //text
        message.setValue("is_show_display_text", action.isShowDisplayText());
        message.setValue("display_text_font_colour_hex", action.getDisplayTextFontColourHex());
        message.setValue("display_text", action.getDisplayText());
        message.setValue("display_text_font_size", action.getDisplayTextFontSize());
        message.setValue("display_text_alignment", action.getDisplayTextAlignment());

        //location

        if (action.getLocation() == null)
        {
            message.setValue("location", null);
        }
        else
        {
            message.setValue("location", new Location(action.getLocation().getRow(), action.getLocation().getCol(), action.getLocation().getRowSpan(), action.getLocation().getColSpan()));
        }

        message.setValue("parent", action.getParent());

        message.setValue("delay_before_executing", action.getDelayBeforeExecuting());

        //client properties

        message.setValue("is_gauge_animated", action.isGaugeAnimated());


        ClientProperties clientProperties = action.getClientProperties();

        message.setValue("client_properties_size", clientProperties.getSize());

        for (int x = 0;x< clientProperties.getSize(); x++)
        {
            message.setValue("client_property_"+x+"_name", clientProperties.get().get(x).getName());
            message.setValue("client_property_"+x+"_raw_value", clientProperties.get().get(x).getRawValue());
        }

        sendMessage(message);

        if (action.getActionType() == ActionType.GAUGE)
        {
            updateActionGaugeProperties(action.getGaugeProperties(), profileID, action.getID());
        }
    }

    public void onClientScreenDetailsReceived(Message message)
    {
        getClient().setDisplayWidth((Double) message.getValue("display_width"));
        getClient().setDisplayHeight((Double) message.getValue("display_height"));
    }

    public void deleteAction(String profileID, String actionID) throws SevereException
    {
        Message message = new Message("delete_action");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        sendMessage(message);
    }

    public void saveClientDetails(String clientName, String defaultProfileID,
                                  String defaultThemeFullName) throws SevereException
    {
        Message message = new Message("save_client_details");
        message.setValue("name", clientName);
        message.setValue("default_profile_ID", defaultProfileID);
        message.setValue("default_theme_full_name", defaultThemeFullName);

        sendMessage(message);

        client.setName(clientName);
        client.setDefaultProfileID(defaultProfileID);
        client.setDefaultThemeFullName(defaultThemeFullName);
    }

    public void saveProfileDetails(ClientProfile clientProfile) throws SevereException, CloneNotSupportedException
    {
        if(client.getProfileByID(clientProfile.getID()) !=null)
        {
            client.getProfileByID(clientProfile.getID()).setName(clientProfile.getName());
            client.getProfileByID(clientProfile.getID()).setRows(clientProfile.getRows());
            client.getProfileByID(clientProfile.getID()).setCols(clientProfile.getCols());
            client.getProfileByID(clientProfile.getID()).setActionSize(clientProfile.getActionSize());
            client.getProfileByID(clientProfile.getID()).setActionGap(clientProfile.getActionGap());
        }
        else
            client.addProfile(clientProfile);

        Message message = new Message("save_client_profile");
        message.setValue("ID", clientProfile.getID());
        message.setValue("name", clientProfile.getName());
        message.setValue("rows", clientProfile.getRows());
        message.setValue("cols", clientProfile.getCols());
        message.setValue("action_size", clientProfile.getActionSize());
        message.setValue("action_gap", clientProfile.getActionGap());
        message.setValue("action_default_display_text_font_size", clientProfile.getActionDefaultDisplayTextFontSize());

        sendMessage(message);
    }

    public void deleteProfile(String ID) throws SevereException
    {
        Message message = new Message("delete_profile");
        message.setValue("ID", ID);
        sendMessage(message);
    }

    public void onInputEventInAction(Message message)
    {
        String profileID = (String) message.getValue("profile_ID");
        String actionID = (String) message.getValue("ID");
        StreamPiInputEvent streamPiInputEvent = (StreamPiInputEvent) message.getValue("event");

        serverListener.onInputEventInAction(getClient(), profileID, actionID, streamPiInputEvent);
    }

    public void setToggleStatus(boolean status, String profileID, String actionID) throws SevereException
    {
        Message message = new Message("set_toggle_status");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        message.setValue("toggle_status", status);

        sendMessage(message);
    }

    public void updateActionGaugeProperties(GaugeProperties gaugeProperties, String profileID, String actionID) throws SevereException
    {
        Message message = new Message("set_action_gauge_properties");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        message.setValue("gauge_properties", gaugeProperties);

        sendMessage(message);
    }

    public void setActionGaugeValue(double value, String profileID, String actionID) throws SevereException
    {
        Message message = new Message("set_action_gauge_value");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        message.setValue("gauge_value", value);

        sendMessage(message);
    }

    public void sendActionFailed(String profileID, String actionID) throws SevereException
    {
        logger.info("Sending failed status ...");
        Message message = new Message("action_failed");
        message.setValue("profile_ID", profileID);
        message.setValue("ID", actionID);
        sendMessage(message);
    }

    public void refreshAllGauges() throws SevereException
    {
        for(ClientProfile clientProfile : client.getAllClientProfiles())
        {
            for(Action action : clientProfile.getActions())
            {
                if(action.getActionType() == ActionType.GAUGE)
                {
                    updateActionGaugeProperties(action.getGaugeProperties(), action.getProfileID(), action.getID());
                }
            }
        }
    }
}
