package com.stream_pi.server.connection;

import com.stream_pi.action_api.normalaction.NormalAction;

public interface ServerListener {
    boolean onNormalActionClicked(NormalAction action);

    void clearTemp();

    void init();

    void othInit();
}
