package com.stream_pi.server.controller;

import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.window.dashboard.ClientAndProfileSelectorPane;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.settings.SettingsBase;
import com.stream_pi.util.exception.SevereException;
import javafx.stage.Stage;

import java.net.SocketAddress;

public interface ServerListener
{
    void onActionClicked(Client client, String profileID, String actionID, boolean toggle);

    void clearTemp();

    void init();

    void restart();

    void othInit();

    Stage getStage();

    DashboardBase getDashboardBase();
    SettingsBase getSettingsBase();

    void initLogger() throws SevereException;

    void factoryReset();

    void initSoundOnActionClicked();
    void onServerStartFailure();

    void showUserChooseIPDialog();
}
