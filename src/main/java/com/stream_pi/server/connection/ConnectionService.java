/*package com.StreamPi.Server.connection;

import com.StreamPi.ActionAPI.action.action;
import com.StreamPi.ActionAPI.action.ActionType;
import com.StreamPi.ActionAPI.action.Location;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.CommAPI.ConnectionGrpc;
import com.StreamPi.CommAPI.ServerGRPC;
import NormalActionPlugins;
import client;
import ClientProfile;
import ServerInfo;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.Exception.StreamPiException;
import com.StreamPi.Util.Platform.Platform;
import com.StreamPi.Util.Version.Version;
import io.grpc.stub.StreamObserver;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionService extends ConnectionGrpc.ConnectionImplBase {

    private client client = null;
    private Logger logger;

    ServerListener serverListener;

    final HashMap<String, Boolean> actionStatuses;

    public ConnectionService(ServerListener serverListener)
    {
        super();

        isDisconnect = false;
        disconnectMessage = "";

        logger = LoggerFactory.getLogger(ConnectionService.class);
        actionStatuses = new HashMap<>();

        this.serverListener = serverListener;
    }


    boolean isDisconnect;
    String disconnectMessage;

    public void disconnect()
    {
        disconnect("");
    }

    public void disconnect(String message)
    {
        if(!isDisconnect)
        {
            this.isDisconnect = true;
            this.disconnectMessage = message;
        }
    }

    @Override
    public void sendClientDetails(ServerGRPC.ClientDetails request, StreamObserver<ServerGRPC.Status> responseObserver) {

        Version clientVersion;
        Version commsAPIVersion;
        Version themeAPIVersion;

        try
        {
            clientVersion = new Version(request.getClientVersion());
            commsAPIVersion = new Version(request.getCommAPIVersion());
            themeAPIVersion = new Version(request.getThemeAPIVersion());
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            serverListener.onRPCError(new MinorException("versions invalid. Check stacktrace"));
            disconnect();
            return;
        }


        String nickName = request.getNickName();
        Platform platform = Platform.valueOf(request.getPlatform().toString());

        client = new client(clientVersion, commsAPIVersion, themeAPIVersion, nickName, platform, null);

        boolean sendActions = true;

        if(!commsAPIVersion.isEqual(ServerInfo.getInstance().getCommAPIVersion()))
        {
            sendActions = false;
            disconnect("client CommAPI and Server CommAPI do not match!");
        }

        responseObserver.onNext(ServerGRPC.Status.newBuilder().setSendActions(sendActions).build());

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ServerGRPC.ClientProfile> sendClientProfiles(StreamObserver<ServerGRPC.Empty> responseObserver) {

        client.getProfiles().clear();

        ArrayList<String> notFoundActions = new ArrayList<>();

        return new StreamObserver<ServerGRPC.ClientProfile>() {
            @Override
            public void onNext(ServerGRPC.ClientProfile clientProfile) {

                String name = clientProfile.getName();
                String id = clientProfile.getId();

                int rows = clientProfile.getRows();
                int cols = clientProfile.getCols();

                int actionSize = clientProfile.getActionSize();
                int actionGap = clientProfile.getActionGap();

                ClientProfile finalClientProfile = new ClientProfile(name, id, rows, cols, actionSize, actionGap);

                ArrayList<action> actions = new ArrayList<>();

                List<ServerGRPC.ClientAction> clientActions = clientProfile.getActionsList();
                for(ServerGRPC.ClientAction clientAction : clientActions)
                {

                    String actionID = clientAction.getId();
                    String actionName = clientAction.getActionName();

                    boolean hasIcon = clientAction.getHasIcon();

                    ActionType actionType = ActionType.valueOf(clientAction.getActionType().toString());

                    action action = new action(actionID, actionType);
                    action.setActionName(actionName);

                    action.setHasIcon(hasIcon);

                    int locationX = clientAction.getLocationX();
                    int locationY = clientAction.getLocationY();

                    action.setLocation(new Location(locationX, locationY));

                    if(actionType == ActionType.NORMAL)
                    {
                        action.setModuleName(clientAction.getModuleName());

                        ClientProperties properties = new ClientProperties();

                        List<ServerGRPC.ClientProperty> clientProperties = clientAction.getClientPropertiesList();

                        for(ServerGRPC.ClientProperty clientProperty : clientProperties)
                        {
                            String propertyName = clientProperty.getName();
                            String propertyValue = clientProperty.getValue();

                            properties.addProperty(propertyName, propertyValue);
                        }

                        action.setClientProperties(properties);

                        boolean isFound = false;
                        for(NormalAction normalAction : NormalActionPlugins.getInstance().getPlugins())
                        {
                            if(normalAction.getModuleName().equals(action.getModuleName()))
                            {
                                isFound = true;

                                normalAction.setClientProperties(action.getClientProperties());
                                normalAction.setActionName(action.getActionName());
                                normalAction.setHasIcon(action.isHasIcon());
                                normalAction.setID(action.getID());
                                normalAction.setLocation(action.getLocation());
                                normalAction.setInvalid(false);

                                actions.add(normalAction);

                                break;
                            }
                        }

                        if(!isFound)
                        {
                            String aName = action.getModuleName();

                            action.setInvalid(true);

                            logger.warn("action "+aName+" not found!");
                            if(!notFoundActions.contains(aName))
                                notFoundActions.add(aName);

                            actions.add(action);
                        }
                    }
                }

                finalClientProfile.setActions(actions);
                client.addProfile(finalClientProfile);


            }

            @Override
            public void onError(Throwable throwable) {
                serverListener.onRPCError(new SevereException(throwable.getMessage()));
            }

            @Override
            public void onCompleted() {

                if(!notFoundActions.isEmpty())
                {
                    StringBuilder all = new StringBuilder("Some actions cannot be edited/used because they are not installed on the server : ");

                    for(String each : notFoundActions)
                    {
                        all.append("\n * ").append(each);
                    }

                    serverListener.onAlert("Warning",all.toString(), Alert.AlertType.WARNING);
                }

                responseObserver.onNext(ServerGRPC.Empty.newBuilder().build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void actionClicked(ServerGRPC.ClickedActionID request, StreamObserver<ServerGRPC.Empty> responseObserver) {
        try
        {
            action actionClicked = client.getProfileByID(request.getProfileID()).getActionByID(request.getId());
            new Thread(new Task<Void>() {
                @Override
                protected Void call()
                {
                    try
                    {
                        synchronized (actionStatuses)
                        {
                            actionStatuses.put(request.getId(), serverListener.onActionClicked(actionClicked));
                        }
                    }
                    catch (MinorException e)
                    {
                        serverListener.onRPCError(e);
                    }
                    return null;
                }
            }).start();
        }
        catch (MinorException e)
        {
            e.printStackTrace();
        }
        finally {
            responseObserver.onNext(ServerGRPC.Empty.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<ServerGRPC.Empty> actionClickedStatus(StreamObserver<ServerGRPC.ActionStatus> responseObserver) {
        return new StreamObserver<ServerGRPC.Empty>() {
            @Override
            public void onNext(ServerGRPC.Empty empty) {

            }

            @Override
            public void onError(Throwable throwable) {
                serverListener.onRPCError(new StreamPiException(throwable.getMessage()));
            }

            @Override
            public void onCompleted() {
                String id = null;
                boolean success = false;
                synchronized (actionStatuses)
                {
                    if(!actionStatuses.isEmpty())
                    {
                        for(String key : actionStatuses.keySet())
                        {
                            id = key;
                            success = actionStatuses.get(id);

                            System.out.println("SDSDSDASDASDXZXCZXCZXC");
                            break;
                        }
                    }
                }

                if(id==null)
                {
                    responseObserver.onNext(ServerGRPC.ActionStatus.newBuilder().build());
                }
                else
                    responseObserver.onNext(ServerGRPC.ActionStatus.newBuilder()
                            .setId(id)
                            .setIsSuccess(success)
                            .build());

                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<ServerGRPC.DisconnectMessage> disconnect(StreamObserver<ServerGRPC.DisconnectMessage> responseObserver) {
        return new StreamObserver<ServerGRPC.DisconnectMessage>() {
            @Override
            public void onNext(ServerGRPC.DisconnectMessage disconnectMessage) {
                if(disconnectMessage.getIsDisconnect())
                    disconnect(disconnectMessage.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                serverListener.onRPCError(new StreamPiException(throwable.getMessage()));
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(ServerGRPC.DisconnectMessage.newBuilder()
                        .setIsDisconnect(isDisconnect)
                        .setMessage(disconnectMessage)
                        .build());
                responseObserver.onCompleted();
            }
        };
    }
}
*/