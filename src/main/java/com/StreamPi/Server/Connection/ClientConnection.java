package com.StreamPi.Server.Connection;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.DisplayTextAlignment;
import com.StreamPi.ActionAPI.Action.Location;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.ActionProperty.Property.Type;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.ActionAPI.OtherActions.CombineAction;
import com.StreamPi.ActionAPI.OtherActions.FolderAction;
import com.StreamPi.Server.Action.NormalActionPlugins;
import com.StreamPi.Server.Client.Client;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Client.ClientTheme;
import com.StreamPi.Server.Info.ServerInfo;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Util.Alert.StreamPiAlert;
import com.StreamPi.Util.Alert.StreamPiAlertType;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.Exception.StreamPiException;
import com.StreamPi.Util.Platform.Platform;
import com.StreamPi.Util.Platform.ReleaseStatus;
import com.StreamPi.Util.Version.Version;
import javafx.concurrent.Task;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class ClientConnection extends Thread{
    private Socket socket;
    private ServerListener serverListener;
    private AtomicBoolean stop = new AtomicBoolean(false);

    private DataInputStream dis;
    private DataOutputStream dos;

    private Logger logger;

    private Client client = null;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public ClientConnection(Socket socket, ServerListener serverListener, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        //actionIconsToBeSent = new ArrayList__();
        this.socket = socket;

        this.serverListener = serverListener;

        logger = Logger.getLogger(ClientConnection.class.getName());

        try
        {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
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
                logger.info("Stopping connection "+socket.getRemoteSocketAddress());
                disconnect();
            }
        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
    }

    public synchronized void exitAndRemove()
    {
        exit();
        removeConnection();
        serverListener.clearTemp();
    }

    public void removeConnection()
    {
        ClientConnections.getInstance().removeConnection(this);
    }


    public void writeToStream(String text) throws SevereException
    {
        /*try
        {
            logger.debug(text);
            dos.writeUTF(text);
            dos.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException("Unable to write to IO Stream!");
        }*/

        try
        {
            byte[] txtBytes = text.getBytes();

            Thread.sleep(50);
            dos.writeUTF("string:: ::");
            dos.flush();
            dos.writeInt(txtBytes.length);
            dos.flush();
            write(txtBytes);
            dos.flush();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
            throw new SevereException("Unable to write to IO Stream!");
        }

    }

    public void sendIcon(String profileID, String actionID, byte[] icon) throws SevereException
    {
        try
        {
            logger.info("Sending action Icon...");
            //Thread.sleep(50);
            System.out.println("1");
            dos.writeUTF("action_icon::"+profileID+"!!"+actionID+"!!::"+icon.length);
            
            System.out.println("2");
            dos.flush();
            
            System.out.println("3");
            dos.writeInt(icon.length);
            
            System.out.println("4");
            dos.flush();
            
            System.out.println("5");
            write(icon);
            
            System.out.println("6");
            dos.flush();
            
            System.out.println("7");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException("Unable to write to IO Stream!");
        }
    }

    public void write(byte[] array) throws SevereException
    {
        try
        {
            dos.write(array);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new SevereException("Unable to write to IO Stream!");
        }
    }


    public void initAfterConnectionQueryReceive(String[] arr) throws StreamPiException
    {
        logger.info("Setting up Client object ...");

        Version clientVersion;
        Version commsStandard;
        Version themesStandard;

        ReleaseStatus releaseStatus;

        try
        {
            clientVersion = new Version(arr[1]);
            releaseStatus = ReleaseStatus.valueOf(arr[2]);
            commsStandard = new Version(arr[3]);
            themesStandard = new Version(arr[4]);
        }
        catch (MinorException e)
        {
            exitAndRemove();
            throw new MinorException(e.getShortMessage()+" (Client '"+socket.getRemoteSocketAddress()+"' )");
        }

        if(!commsStandard.isEqual(ServerInfo.getInstance().getCommAPIVersion()))
        {
            String errTxt = "Server and Client Communication standards do not match. Make sure you are on the latest version of server and client.\n" +
                    "Server Comms. Standard : "+ServerInfo.getInstance().getCommAPIVersion().getText()+
                    "\nClient Comms. Standard : "+commsStandard.getText();

            disconnect(errTxt);
            throw new MinorException(errTxt);
        }

        client = new Client(clientVersion, releaseStatus, commsStandard, themesStandard, arr[5], Platform.valueOf(arr[8]), socket.getRemoteSocketAddress());

        client.setStartupDisplayWidth(Double.parseDouble(arr[6]));
        client.setStartupDisplayHeight(Double.parseDouble(arr[7]));
        client.setDefaultProfileID(arr[9]);
        client.setDefaultThemeFullName(arr[10]);

        client.debugPrint();

        //call get profiles command.
        serverListener.clearTemp();
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
                String msg = "";

                try
                {
                    String raw = dis.readUTF();

                    int length = dis.readInt();

                    System.out.println("SIZE TO READ : "+length);

                    String[] precursor = raw.split("::");

                    String inputType = precursor[0];
                    String secondArg = precursor[1];


                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    /*int count;
                    int chunkSize = 512;
                    while (length>0)
                    {
                        if(chunkSize > length)
                            chunkSize = length;
                        else
                            chunkSize = 512;

                        byte[] buffer = new byte[chunkSize];
                        count = dis.read(buffer);

                        System.out.println(count);

                        byteArrayOutputStream.write(buffer);

                        length-=count;
                    }*/

                    /*byte[] buffer = new byte[8192];
                    int read;
                    while((read = dis.read(buffer)) != -1){
                        System.out.println("READ : "+read);
                        byteArrayOutputStream.write(buffer, 0, read);
                    }

                    System.out.println("READ : "+byteArrayOutputStream.size());

                    byteArrayOutputStream.close();

                    byte[] bArr = byteArrayOutputStream.toByteArray();*/

                    byte[] bArr = new byte[length];

                    dis.readFully(bArr);

                    if(inputType.equals("string"))
                    {
                        msg = new String(bArr);
                    }
                    else if(inputType.equals("action_icon"))
                    {
                        String[] secondArgSep = secondArg.split("!!");

                        String profileID = secondArgSep[0];
                        String actionID = secondArgSep[1];

                        getClient().getProfileByID(profileID).getActionByID(actionID).setIcon(bArr);

                        //serverListener.clearTemp();
                        continue;
                    }
                }
                catch (IOException e)
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

                logger.info("Received text : '"+msg+"'");

                String[] sep = msg.split("::");

                String command = sep[0];

                switch (command)
                {
                    case "disconnect" :         clientDisconnected(msg);
                                                break;

                    case "client_details" :     initAfterConnectionQueryReceive(sep);
                                                getProfilesFromClient();
                                                getThemesFromClient();
                                                break;

                    case "profiles" :           registerProfilesFromClient(sep);
                                                break;

                    case "profile_details" :    registerProfileDetailsFromClient(sep);
                                                break;

                    case "action_details" :     registerActionToProfile(sep);
                                                break;

                    case "themes":              registerThemes(sep);
                                                break;

                    case "action_clicked":      actionClicked(sep[1], sep[2]);
                                                break;

                    default:                    logger.warning("Command '"+command+"' does not match records. Make sure client and server versions are equal.");


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

    public void initAfterConnectionQuerySend() throws SevereException
    {
        logger.info("Asking client details ...");
        writeToStream("get_client_details::");
    }

    public void disconnect() throws SevereException {
        disconnect("");
    }

    public void disconnect(String message) throws SevereException {
        if(stop.get())
            return;

        stop.set(true);

        logger.info("Sending client disconnect message ...");
        writeToStream("disconnect::"+message+"::");


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

    public void clientDisconnected(String message)
    {
        stop.set(true);
        String txt = "Disconnected!";

        if(!message.equals("disconnect::::"))
            txt = "Message : "+message.split("::")[1];

        new StreamPiAlert("Disconnected from "+getClient().getNickName()+".", txt, StreamPiAlertType.WARNING).show();;
        exitAndRemove();
    }

    public void getProfilesFromClient() throws StreamPiException
    {
        logger.info("Asking client to send profiles ...");
        writeToStream("get_profiles::");
    }

    public void getThemesFromClient() throws StreamPiException
    {
        logger.info("Asking clients to send themes ...");
        writeToStream("get_themes::");
    }

    public void registerThemes(String[] sep)
    {
        for(int i =1; i<sep.length;i++)
        {
            String[] internal = sep[i].split("__");

            ClientTheme clientTheme = new ClientTheme(
                    internal[0],
                    internal[1],
                    internal[2],
                    internal[3]
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

    public void registerProfilesFromClient(String[] sep) throws StreamPiException
    {
        logger.info("Registering profiles ...");

        int noOfProfiles = Integer.parseInt(sep[1]);
        getClient().setTotalNoOfProfiles(noOfProfiles);

        for(int i = 2; i<(noOfProfiles + 2); i++)
        {
            String profileID = sep[i];
            getProfileDetailsFromClient(profileID);
        }
    }

    public void getProfileDetailsFromClient(String ID) throws StreamPiException
    {
        logger.info("Asking client to send details of profile : "+ID);
        writeToStream("get_profile_details::"+ID+"::");
    }


    public void registerProfileDetailsFromClient(String[] sep)
    {
        String ID = sep[1];
        logger.info("Registering details for profile : "+ID);

        String name = sep[2];
        int rows = Integer.parseInt(sep[3]);
        int cols = Integer.parseInt(sep[4]);
        int actionSize = Integer.parseInt(sep[5]);
        int actionGap = Integer.parseInt(sep[6]);


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

    /*public void getActionIcon(String profileID, String actionID) throws StreamPiException
    {
        System.out.println("getting action icon from "+profileID+", "+actionID);
        writeToStream("get_action_icon::"+profileID+"::"+actionID);
    }*/

    public synchronized void registerActionToProfile(String[] sep) throws StreamPiException
    {
        String profileID = sep[1];

        String ID = sep[2];
        ActionType actionType = ActionType.valueOf(sep[3]);

        //4 - Version
        //5 - ModuleName

        //display
        String bgColorHex = sep[6];

        //icon
        boolean isHasIcon = sep[7].equals("true");
        boolean isShowIcon = sep[8].equals("true");

        //text
        boolean isShowDisplayText = sep[9].equals("true");
        String displayFontColor = sep[10];
        String displayText = sep[11];
        DisplayTextAlignment displayTextAlignment = DisplayTextAlignment.valueOf(sep[12]);

        //location
        String row = sep[13];
        String col = sep[14];

        Location location = new Location(Integer.parseInt(row), Integer.parseInt(col));



        Action action = new Action(ID, actionType);

        action.setBgColourHex(bgColorHex);
        action.setShowIcon(isShowIcon);
        action.setHasIcon(isHasIcon);

        action.setShowDisplayText(isShowDisplayText);
        action.setDisplayTextFontColourHex(displayFontColor);
        action.setDisplayText(displayText);
        action.setDisplayTextAlignment(displayTextAlignment);


        action.setLocation(location);


        //client properties

        int clientPropertiesSize = Integer.parseInt(sep[15]);

        String root = sep[17];
        action.setParent(root);

        String[] clientPropertiesRaw = sep[16].split("!!");

        ClientProperties clientProperties = new ClientProperties();

        if(actionType == ActionType.FOLDER)
            clientProperties.setDuplicatePropertyAllowed(true);

        for(int i = 0;i<clientPropertiesSize; i++)
        {
            String[] clientPraw = clientPropertiesRaw[i].split("__");

            Property property = new Property(clientPraw[0], Type.STRING);

            if(clientPraw.length > 1)
                property.setRawValue(clientPraw[1]);

            clientProperties.addProperty(property);
        }

        action.setClientProperties(clientProperties);
        action.setModuleName(sep[5]);

        //set up action

        //Action toBeAdded = null;

        if(actionType == ActionType.NORMAL)
        {
            NormalAction actionCopy = NormalActionPlugins.getInstance().getPluginByModuleName(sep[5]);

            if(actionCopy == null)
            {
                action.setInvalid(true);
            }
            else
            {
                action.setVersion(new Version(sep[4]));

                action.setRepo(actionCopy.getRepo());

                if(actionCopy.getVersion().getMajor() != action.getVersion().getMajor())
                {
                    action.setInvalid(true);
                }
                else
                {
                    action.setName(actionCopy.getName());

                    ClientProperties finalClientProperties = new ClientProperties();


                    for(Property property : actionCopy.getClientProperties().get())
                    {
                        for(int i = 0;i<action.getClientProperties().getSize();i++)
                        {
                            Property property1 = action.getClientProperties().get().get(i);
                            if (property.getName().equals(property1.getName()))
                            {
                                property.setRawValue(property1.getRawValue());


                                finalClientProperties.addProperty(property);
                            }
                        }
                    }

                    action.setClientProperties(finalClientProperties);

                }
            }
        }


        try
        {
            for(Property prop : action.getClientProperties().get())
            {
                logger.info("G@@@@@ : "+prop.getRawValue());
            }


            getClient().getProfileByID(profileID).addAction(action);



            for(String action1x : getClient().getProfileByID(profileID).getActionsKeySet())
            {
                Action action1 = getClient().getProfileByID(profileID).getActionByID(action1x);
                logger.info("231cc : "+action1.getID());
                for(Property prop : action1.getClientProperties().get())
                {
                    logger.info("G@VVVV@@@ : "+prop.getRawValue());
                }
            }

        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException("Action", "Unable to clone"));
        }

    }

    public void saveActionDetails(String profileID, Action action) throws SevereException
    {
        StringBuilder finalQuery = new StringBuilder("save_action_details::");

        finalQuery.append(profileID)
                .append("::")
                .append(action.getID())
                .append("::")
                .append(action.getActionType())
                .append("::");

        if(action.getActionType() == ActionType.NORMAL)
        {
            finalQuery.append(action.getVersion().getText());
            System.out.println("VERSION :sdd "+action.getVersion().getText());
        }

        finalQuery.append("::");

        if(action.getActionType() == ActionType.NORMAL)
            finalQuery.append(action.getModuleName());

        finalQuery.append("::");

        //display

        finalQuery.append(action.getBgColourHex())
                .append("::");

        //icon
        finalQuery.append(action.isHasIcon())
                .append("::")
                .append(action.isShowIcon())
                .append("::");

        //text
        finalQuery.append(action.isShowDisplayText())
                .append("::")
                .append(action.getDisplayTextFontColourHex())
                .append("::")
                .append(action.getDisplayText())
                .append("::")
                .append(action.getDisplayTextAlignment())
                .append("::");

        //location

        if(action.getLocation() == null)
            finalQuery.append("-1::-1::");
        else
            finalQuery.append(action.getLocation().getRow())
                    .append("::")
                    .append(action.getLocation().getCol())
                    .append("::");

        //client properties

        ClientProperties clientProperties = action.getClientProperties();

        finalQuery.append(clientProperties.getSize())
                .append("::");

        for(Property property : clientProperties.get())
        {
            finalQuery.append(property.getName())
                    .append("__")
                    .append(property.getRawValue())
                    .append("__");

            finalQuery.append("!!");
        }

        finalQuery.append("::")
                .append(action.getParent())
                .append("::");

        writeToStream(finalQuery.toString());

    }

    public void deleteAction(String profileID, String actionID) throws SevereException
    {
        writeToStream("delete_action::"+profileID+"::"+actionID);
    }

    public void saveClientDetails(String clientNickname, String screenWidth, String screenHeight, String defaultProfileID,
                                  String defaultThemeFullName) throws SevereException
    {
        writeToStream("save_client_details::"+
                clientNickname+"::"+
                screenWidth+"::"+
                screenHeight+"::"+
                defaultProfileID+"::"+
                defaultThemeFullName+"::");

        client.setNickName(clientNickname);
        client.setStartupDisplayWidth(Double.parseDouble(screenWidth));
        client.setStartupDisplayHeight(Double.parseDouble(screenHeight));
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

        writeToStream("save_client_profile::"+
                clientProfile.getID()+"::"+
                clientProfile.getName()+"::"+
                clientProfile.getRows()+"::"+
                clientProfile.getCols()+"::"+
                clientProfile.getActionSize()+"::"+
                clientProfile.getActionGap()+"::");
    }

    public void deleteProfile(String ID) throws SevereException
    {
        writeToStream("delete_profile::"+ID+"::");
    }

    public void actionClicked(String profileID, String actionID) {

        try
        {
            Action action = client.getProfileByID(profileID).getActionByID(actionID);

            if(action.getActionType() == ActionType.NORMAL)
            {
                NormalAction original = NormalActionPlugins.getInstance().getPluginByModuleName(
                        action.getModuleName()
                );

                if(original == null)
                {
                    throw new MinorException(
                            "The Action isn't installed on the server."
                    );
                }

                NormalAction normalAction = original.clone();



                normalAction.setLocation(action.getLocation());
                normalAction.setDisplayText(action.getDisplayText());
                normalAction.setID(actionID);

                normalAction.setClientProperties(action.getClientProperties());

                new Thread(new Task<Void>() {
                    @Override
                    protected Void call()
                    {
                        try
                        {
                            boolean result = serverListener.onNormalActionClicked(normalAction);
                            if(!result)
                            {
                                sendActionFailed(profileID, actionID);
                            }
                        }
                        catch (SevereException e)
                        {
                            exceptionAndAlertHandler.handleSevereException(e);
                        }
                        return null;
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException(e.getMessage()));
        }
    }

    public void sendActionFailed(String profileID, String actionID) throws SevereException {
        logger.info("Sending failed status ...");
        writeToStream("action_failed::"+
                profileID+"::"+
                actionID+"::");
    }
}
