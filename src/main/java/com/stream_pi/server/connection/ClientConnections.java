/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macropad
 * Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

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
        connections.remove(clientConnection);
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