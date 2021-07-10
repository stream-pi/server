package com.stream_pi.server.window;

import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;

public interface ExceptionAndAlertHandler {
    void handleMinorException(MinorException e);
    void handleMinorException(String message, MinorException e);
    void handleSevereException(SevereException e);
    void handleSevereException(String message, SevereException e);
}
