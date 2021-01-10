package com.StreamPi.Server.Window;

import com.StreamPi.Util.Alert.StreamPiAlertType;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import javafx.scene.control.Alert;

public interface ExceptionAndAlertHandler {
    void handleMinorException(MinorException e);
    void handleSevereException(SevereException e);
}
