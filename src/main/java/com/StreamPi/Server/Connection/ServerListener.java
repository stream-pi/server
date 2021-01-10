package com.StreamPi.Server.Connection;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.Server.Client.Client;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.Exception.StreamPiException;
import javafx.scene.control.Alert;

import java.net.Socket;

public interface ServerListener {
    boolean onNormalActionClicked(NormalAction action);

    void clearTemp();

    void init();

    void othInit();
}
