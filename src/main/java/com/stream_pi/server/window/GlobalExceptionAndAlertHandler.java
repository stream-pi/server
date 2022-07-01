package com.stream_pi.server.window;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.config.Config;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertButton;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalExceptionAndAlertHandler
{
    private static GlobalExceptionAndAlertHandler instance = null;

    public static GlobalExceptionAndAlertHandler getInstance() {
        return instance;
    }

    private final ServerListener serverListener;
    public GlobalExceptionAndAlertHandler(ServerListener serverListener)
    {
        this.serverListener = serverListener;
    }

    public static void nullify()
    {
        instance = null;
    }

    public static void initialise(ServerListener serverListener) throws SevereException
    {
        if(instance != null)
        {
            Logger.getLogger(Config.class.getName()).warning("ExceptionAndAlertHandler was already loaded! Re-loading ...");
        }

        instance = new GlobalExceptionAndAlertHandler(serverListener);
    }

    public StreamPiAlert handleMinorException(MinorException e)
    {
        return handleMinorException(e.getMessage(), e);
    }

    public StreamPiAlert handleMinorException(String message, MinorException e)
    {
        Logger.getLogger(GlobalExceptionAndAlertHandler.class.getName()).log(Level.SEVERE, message, e);
        e.printStackTrace();
        StreamPiAlert alert = new StreamPiAlert(e.getTitle(), message, StreamPiAlertType.WARNING);
        alert.show();
        return alert;
    }

    public StreamPiAlert handleSevereException(SevereException e)
    {
        return handleSevereException(e.getMessage(), e);
    }

    public StreamPiAlert handleSevereException(String message, SevereException e)
    {
        Logger.getLogger(GlobalExceptionAndAlertHandler.class.getName()).log(Level.SEVERE, message, e);
        e.printStackTrace();

        StreamPiAlert alert = new StreamPiAlert(e.getTitle(), message +"\n" + I18N.getString("controller.Controller.willNowExit"), StreamPiAlertType.ERROR);

        alert.setOnClicked(new StreamPiAlertListener()
        {
            @Override
            public void onClick(StreamPiAlertButton s)
            {
                serverListener.fullExit();
            }
        });

        alert.show();

        return alert;
    }
}
