package com.stream_pi.server.connection;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.client.ClientTheme;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.comms.Message;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.exception.StreamPiException;
import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.ReleaseStatus;
import com.stream_pi.util.version.Version;
import javafx.concurrent.Task;
import javafx.scene.control.Toggle;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
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
        } catch (IOException e) {
            e.printStackTrace();

            exceptionAndAlertHandler.handleMinorException(new MinorException("Unable to start socket streams"));
        }

        start();
    }

    public synchronized void exit()
    {
        if(stop.get())
            return;

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
        callOnClientDisconnectOnAllActions();
        removeConnection();
        serverListener.clearTemp();
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
                        ((ExternalPlugin) action).onClientDisconnected();
                    }
                    catch (MinorException e)
                    {
                        e.setTitle("Unable to run onClientDisconnected for "+action.getModuleName());
                        e.setShortMessage("Detailed message :\n\n"+e.getShortMessage());
                        exceptionAndAlertHandler.handleMinorException(e);
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
        getLogger().info("Sending icon "+state+" len "+icon.length+"; profile:"+profileID+"; actionid:"+actionID+"\n\n\n\n");
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        Message message = new Message("action_icon");
        message.setStringArrValue(profileID, actionID, state);
        message.setByteArrValue(icon);
        sendMessage(message);
    }

    public void initAfterConnectionQueryReceive(Message message) throws StreamPiException
    {
        String[] ar = message.getStringArrValue();

        logger.info("Setting up client object ...");

        Version clientVersion;
        Version commsStandard;
        Version themesStandard;

        ReleaseStatus releaseStatus;

        try
        {
            clientVersion = new Version(ar[0]);
            releaseStatus = ReleaseStatus.valueOf(ar[1]);
            commsStandard = new Version(ar[2]);
            themesStandard = new Version(ar[3]);
        }
        catch (MinorException e)
        {
            exitAndRemove();
            throw new MinorException(e.getShortMessage()+" (client '"+socket.getRemoteSocketAddress()+"' )");
        }

        if(!commsStandard.isEqual(ServerInfo.getInstance().getCommStandardVersion()))
        {
            String errTxt = "Server and client Communication standards do not match. Make sure you are on the latest version of server and client.\n" +
                    "Server Comms. Standard : "+ServerInfo.getInstance().getCommStandardVersion().getText()+
                    "\nclient Comms. Standard : "+commsStandard.getText();

            disconnect(errTxt);
            throw new MinorException(errTxt);
        }

        client = new Client(clientVersion, releaseStatus, commsStandard, themesStandard, ar[4], Platform.valueOf(ar[7]), socket.getRemoteSocketAddress());

        client.setDisplayWidth(Double.parseDouble(ar[5]));
        client.setDisplayHeight(Double.parseDouble(ar[6]));
        client.setDefaultProfileID(ar[8]);
        client.setDefaultThemeFullName(ar[9]);
        
        //call get profiles command.
        serverListener.clearTemp();
    }

    public void updateClientDetails(Message message)
    {
        String[] ar = message.getStringArrValue();

        logger.info("Setting up client object ...");

        client.setNickName(ar[4]);
        client.setDisplayWidth(Double.parseDouble(ar[5]));
        client.setDisplayHeight(Double.parseDouble(ar[6]));
        client.setDefaultProfileID(ar[8]);
        client.setDefaultThemeFullName(ar[9]);

        serverListener.getSettingsBase().getClientsSettings().loadData();
    }

    public synchronized Client getClient()
    {
        return client;
    }

    @Override
    public void run() {

        try {
            initAfterConnectionQuerySend();
        } catch (SevereException e) {
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
                        case "action_icon" :        onActionIconReceived(message);
                            break;

                        case "disconnect" :         clientDisconnected(message);
                            break;

                        case "register_client_details" :     initAfterConnectionQueryReceive(message);
                            getProfilesFromClient();
                            getThemesFromClient();
                            break;

                        case "update_client_details" : updateClientDetails(message);
                            break;

                        case "client_screen_details" : onClientScreenDetailsReceived(message);
                            break;

                        case "profiles" :           registerProfilesFromClient(message);
                            break;

                        case "profile_details" :    registerProfileDetailsFromClient(message);
                            break;

                        case "action_details" :     registerActionToProfile(message);
                            break;

                        case "themes":              registerThemes(message);
                            break;

                        case "action_clicked":      onActionClicked(message);
                            break;

                        default:                    logger.warning("Command '"+header+"' does not match records. Make sure client and server versions are equal.");


                    }
                }
                catch (IOException | ClassNotFoundException e)
                {
                    logger.log(Level.SEVERE, e.getMessage());
                    e.printStackTrace();

                    serverListener.clearTemp();

                    if(!stop.get())
                    {
                        removeConnection();
                        throw new MinorException("Accidentally disconnected from "+getClient().getNickName()+".");
                    }

                    exitAndRemove();

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

    private void onActionIconReceived(Message message) throws MinorException
    {
        String[] s = message.getStringArrValue();

        String profileID = s[0];
        String actionID = s[1];
        String iconState = s[2];

        getClient()
                .getProfileByID(profileID)
                .getActionByID(actionID)
                .addIcon(iconState,message.getByteArrValue());
    }

    public void initAfterConnectionQuerySend() throws SevereException
    {
        logger.info("Asking client details ...");
        sendMessage(new Message("get_client_details"));
    }

    public void disconnect() throws SevereException {
        disconnect("");
    }

    public void disconnect(String message) throws SevereException {
        if(stop.get())
            return;

        stop.set(true);

        logger.info("Sending client disconnect message ...");

        Message m = new Message("disconnect");
        m.setStringValue(message);
        sendMessage(m);

        try
        {
            if(!socket.isClosed())
                socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException("Unable to close socket");
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
            throw new SevereException("Unable to write to io Stream!");
        }
    }

    public void clientDisconnected(Message message)
    {
        stop.set(true);
        String txt = "Disconnected!";

        String msg = message.getStringValue();

        if(!msg.isBlank())
            txt = "Message : "+msg;

        new StreamPiAlert("Disconnected from "+getClient().getNickName()+".", txt, StreamPiAlertType.WARNING).show();;
        exitAndRemove();
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
        String[] r = message.getStringArrValue();

        for(int i =0; i<(r.length);i+=4)
        {
            ClientTheme clientTheme = new ClientTheme(
                    r[i],
                    r[i+1],
                    r[i+2],
                    r[i+3]
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

        String[] profileIds = message.getStringArrValue();

        int[] profilesActionIds = message.getIntArrValue();

        temporaryProfilesCheck = new HashMap<>();

        for (int i = 0;i<profileIds.length;i++)
        {
            temporaryProfilesCheck.put(profileIds[i], profilesActionIds[i]);
            getProfileDetailsFromClient(profileIds[i]);
        }
    }

    public void getProfileDetailsFromClient(String ID) throws StreamPiException
    {
        logger.info("Asking client to send details of profile : "+ID);
        Message message = new Message("get_profile_details");
        message.setStringValue(ID);
        sendMessage(message);
    }


    public void registerProfileDetailsFromClient(Message message)
    {
        String[] r = message.getStringArrValue();

        String ID = r[0];
        logger.info("Registering details for profile : "+ID);

        String name = r[1];
        int rows = Integer.parseInt(r[2]);
        int cols = Integer.parseInt(r[3]);
        int actionSize = Integer.parseInt(r[4]);
        int actionGap = Integer.parseInt(r[5]);


        ClientProfile clientProfile = new ClientProfile(name, ID, rows, cols, actionSize, actionGap);

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


        String[] r = message.getStringArrValue();

        String profileID = r[0];

        String ID = r[1];
        ActionType actionType = ActionType.valueOf(r[2]);

        //3 - Version
        //4 - ModuleName

        //display
        String bgColorHex = r[5];

        //icon
        String[] allIconStateNames = r[6].split("::");
        String defaultIconState = r[7];

        //text
        boolean isShowDisplayText = r[8].equals("true");
        String displayFontColor = r[9];
        String displayText = r[10];
        DisplayTextAlignment displayTextAlignment = DisplayTextAlignment.valueOf(r[11]);

        //location
        String row = r[12];
        String col = r[13];

        Location location = new Location(Integer.parseInt(row), Integer.parseInt(col));

        String root = r[14];

        int delayBeforeRunning = Integer.parseInt(r[15]);


        //client properties

        int clientPropertiesSize = Integer.parseInt(r[16]);

        ClientProperties clientProperties = new ClientProperties();

        if(actionType == ActionType.FOLDER)
            clientProperties.setDuplicatePropertyAllowed(true);

        for(int i = 17;i<((clientPropertiesSize*2) + 17); i+=2)
        {
            Property property = new Property(r[i], Type.STRING);
            property.setRawValue(r[i+1]);

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

        boolean isInvalidAction = false;

        if(actionType == ActionType.NORMAL || actionType == ActionType.TOGGLE)
        {
            String moduleName = r[4];
            Version version = new Version(r[3]);

            ExternalPlugin originalAction = ExternalPlugins.getInstance().getPluginByModuleName(moduleName);

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

                        newPlugin.setID(ID);
                        newPlugin.setProfileID(profileID);
                        newPlugin.setSocketAddressForClient(socket.getRemoteSocketAddress());

                        newPlugin.setBgColourHex(bgColorHex);
                        newPlugin.setCurrentIconState(defaultIconState);

                        newPlugin.setShowDisplayText(isShowDisplayText);
                        newPlugin.setDisplayTextFontColourHex(displayFontColor);
                        newPlugin.setDisplayText(displayText);
                        newPlugin.setDisplayTextAlignment(displayTextAlignment);

                        newPlugin.setLocation(location);

                        newPlugin.setParent(root);

                        newPlugin.setDelayBeforeExecuting(delayBeforeRunning);


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
                                    e.setTitle("Unable to run onClientConnected for "+moduleName);
                                    e.setShortMessage("Detailed message :\n\n"+e.getShortMessage());
                                    exceptionAndAlertHandler.handleMinorException(e);
                                }
                                return null;
                            }
                        }).start();


                    }
                    catch (CloneNotSupportedException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(new MinorException("action", "Unable to clone"));
                    }

                    checkIfReady();
                    return;
                }
            }
        }


        Action action = null;

        if(isInvalidAction)
        {
            String moduleName = r[4];
            Version version = new Version(r[3]);

            action = new Action();
            action.setInvalid(true);
            action.setVersion(version);
            action.setModuleName(moduleName);
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

        action.setBgColourHex(bgColorHex);
        action.setCurrentIconState(defaultIconState);

        action.setShowDisplayText(isShowDisplayText);
        action.setDisplayTextFontColourHex(displayFontColor);
        action.setDisplayText(displayText);
        action.setDisplayTextAlignment(displayTextAlignment);

        action.setLocation(location);

        action.setParent(root);


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

    public synchronized void saveActionDetails(String profileID, Action action) throws SevereException
    {
        ArrayList<String> a = new ArrayList<>();

        a.add(profileID);
        a.add(action.getID());
        a.add(action.getActionType()+"");

        if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE)
        {
            a.add(action.getVersion().getText());
            System.out.println("VERSION :sdd "+action.getVersion().getText());
        }
        else
        {
            a.add("no");
        }


        if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE)
        {
            a.add(action.getModuleName());
        }
        else
        {
            a.add("nut");
        }


        //display

        a.add(action.getBgColourHex());

        //icon

        StringBuilder allIconStatesNames = new StringBuilder();
        for(String eachState : action.getIcons().keySet())
        {
            allIconStatesNames.append(eachState).append("::");
        }
        a.add(allIconStatesNames.toString());


        a.add(action.getCurrentIconState());

        //text
        a.add(action.isShowDisplayText()+"");
        a.add(action.getDisplayTextFontColourHex());
        a.add(action.getDisplayText());
        a.add(action.getDisplayTextAlignment()+"");

        //location

        if(action.getLocation() == null)
        {
            a.add("-1");
            a.add("-1");
        }
        else
        {
            a.add(action.getLocation().getRow()+"");
            a.add(action.getLocation().getCol()+"");
        }

        a.add(action.getParent());

        a.add(action.getDelayBeforeExecuting()+"");

        //client properties

        ClientProperties clientProperties = action.getClientProperties();


        a.add(clientProperties.getSize()+"");

        for(Property property : clientProperties.get())
        {
            a.add(property.getName());
            a.add(property.getRawValue());
        }


        Message message = new Message("save_action_details");
        String[] x = new String[a.size()];
        x = a.toArray(x);

        message.setStringArrValue(x);
        sendMessage(message);
    }

    public void getClientScreenDetails() throws SevereException
    {
        Message message = new Message("get_client_screen_details");
        sendMessage(message);
    }

    public void onClientScreenDetailsReceived(Message message)
    {
        getClient().setDisplayWidth(message.getDoubleArrValue()[0]);
        getClient().setDisplayHeight(message.getDoubleArrValue()[1]);
    }

    public void deleteAction(String profileID, String actionID) throws SevereException
    {
        Message message = new Message("delete_action");
        message.setStringArrValue(profileID, actionID);
        sendMessage(message);
    }

    public void saveClientDetails(String clientNickname, String defaultProfileID,
                                  String defaultThemeFullName) throws SevereException
    {
        Message message = new Message("save_client_details");
        message.setStringArrValue(
                clientNickname,
                defaultProfileID,
                defaultThemeFullName
        );

        sendMessage(message);

        client.setNickName(clientNickname);
        client.setDefaultProfileID(defaultProfileID);
        client.setDefaultThemeFullName(defaultThemeFullName);
    }

    public void saveProfileDetails(ClientProfile clientProfile) throws SevereException, CloneNotSupportedException {
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

        message.setStringArrValue(
                clientProfile.getID(),
                clientProfile.getName(),
                clientProfile.getRows()+"",
                clientProfile.getCols()+"",
                clientProfile.getActionSize()+"",
                clientProfile.getActionGap()+""
        );

        sendMessage(message);
    }

    public void deleteProfile(String ID) throws SevereException
    {
        Message message = new Message("delete_profile");
        message.setStringValue(ID);
        sendMessage(message);
    }

    public void onActionClicked(Message message)
    {
        try
        {
            String[] r = message.getStringArrValue();

            String profileID = r[0];
            String actionID = r[1];
            boolean toggle = r[2].equals("true");

            Action action =  client.getProfileByID(profileID).getActionByID(actionID);

            if(action.getActionType() == ActionType.NORMAL || action.getActionType() == ActionType.TOGGLE)
            {
                if(action.isInvalid())
                {
                    throw new MinorException(
                            "The action isn't installed on the server."
                    );
                }
                else
                {
                    new Thread(new Task<Void>() {
                        @Override
                        protected Void call()
                        {
                            getLogger().info("action "+action.getID()+" clicked!");

                            if(action instanceof ToggleAction)
                            {
                                serverListener.onToggleActionClicked((ToggleAction) action, toggle, profileID,
                                        getRemoteSocketAddress());
                            }
                            else if (action instanceof NormalAction)
                            {
                                serverListener.onNormalActionClicked((NormalAction) action, profileID,
                                        getRemoteSocketAddress());
                            }
                            return null;
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
        }
    }

    public void setToggleStatus(boolean status, String profileID, String actionID) throws SevereException
    {
        Message message = new Message("set_toggle_status");

        message.setStringArrValue(profileID, actionID, status+"");

        sendMessage(message);
    }

    public void sendActionFailed(String profileID, String actionID) throws SevereException
    {
        logger.info("Sending failed status ...");
        Message message = new Message("action_failed");
        message.setStringArrValue(profileID, actionID);
        sendMessage(message);
    }
}
