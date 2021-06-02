package com.stream_pi.server.connection;

import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.server.window.dashboard.ClientAndProfileSelectorPane;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.settings.SettingsBase;
import com.stream_pi.util.exception.SevereException;
import javafx.stage.Stage;

import java.net.SocketAddress;

public interface ServerListener
{
    void onNormalActionClicked(NormalAction action, String profileID, SocketAddress socketAddress);
    void onToggleActionClicked(ToggleAction action, boolean toggle, String profileID, SocketAddress socketAddress);

    void clearTemp();

    void init();

    void restart();

    void othInit();

    Stage getStage();

    DashboardBase getDashboardBase();
    SettingsBase getSettingsBase();

    void initLogger() throws SevereException;
}
