package com.StreamPi.Server.Window.Dashboard;

import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ClientConnection;

public interface DashboardInterface {
    void newSelectedClientConnection(ClientConnection clientConnection);
    void newSelectedClientProfile(ClientProfile clientProfile);

}
