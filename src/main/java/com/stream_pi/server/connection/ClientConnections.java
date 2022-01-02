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


import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.net.SocketAddress;
import java.util.ArrayList;

public class ClientConnections
{
    private ArrayList<ClientConnection> connections;

    private static ClientConnections instance = null;

    private IntegerProperty sizeProperty;


    private ClientConnections()
    {
        connections = new ArrayList<>();
        sizeProperty = new SimpleIntegerProperty(0);
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

    public void addConnection(ClientConnection connection)
    {
        sizeProperty.set(sizeProperty.get()+1);
        connections.add(connection);
    }

    public void removeConnection(ClientConnection clientConnection)
    {
        sizeProperty.set(sizeProperty.get()-1);
        connections.remove(clientConnection);
    }

    public void disconnectAll()
    {
        for(ClientConnection clientConnection : connections)
        {
            clientConnection.exit(false);
        }

        sizeProperty.set(0);
        connections.clear();
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

    public IntegerProperty getSizeProperty()
    {
        return sizeProperty;
    }
}