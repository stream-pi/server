package com.stream_pi.server.connection;


import java.net.SocketAddress;
import java.util.ArrayList;

public class ClientConnections {

    private ArrayList<ClientConnection> connections;

    private static ClientConnections instance = null;

    private ClientConnections()
    {
        connections = new ArrayList<>();
    }

    public static synchronized ClientConnections getInstance()
    {
        if(instance == null)
        {
            instance = new ClientConnections();
        }

        return instance;
    }

    public ArrayList<ClientConnection> getConnections()
    {
        return connections;
    }

    public void clearAllConnections()
    {
        connections.clear(); // NOT RECOMMENDED TO USE CARELESSLY
    }

    public void addConnection(ClientConnection connection)
    {
        connections.add(connection);
    }

    public void removeConnection(ClientConnection clientConnection)
    {
        System.out.println(connections.remove(clientConnection)+" 22222222222222222222222222222222222222222");
    }

    public void disconnectAll()
    {
        new Thread(()->{
            for(ClientConnection clientConnection : connections)
            {
                clientConnection.exit();
            }

            clearAllConnections();
        }).start();
    }

    public ClientConnection getClientConnectionBySocketAddress(SocketAddress socketAddress)
    {
        for(ClientConnection clientConnection : connections)
        {
            if(clientConnection.getClient().getRemoteSocketAddress().equals(socketAddress))
                return clientConnection;
        }

        return null;
    }
}