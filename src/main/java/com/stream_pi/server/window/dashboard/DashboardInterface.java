package com.stream_pi.server.window.dashboard;

import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;

public interface DashboardInterface {
    void newSelectedClientConnection(ClientConnection clientConnection);
    void newSelectedClientProfile(ClientProfile clientProfile);

}
