package com.stream_pi.server.connection;

import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.ClientAndProfileSelectorPane;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MainServer extends Thread{
    private ServerListener serverListener;

    private Logger logger = Logger.getLogger(MainServer.class.getName());
    private int port;
    private ServerSocket serverSocket = null;
    //private Server server;

    private AtomicBoolean stop = new AtomicBoolean(false);

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    public MainServer(ServerListener serverListener, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.serverListener = serverListener;
    }


    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public synchronized void start() {
        stop.set(false);
        super.start();
    }

    public void stopListeningForConnections()
    {

        /*if(server !=null)
        {
            if(!server.isShutdown())
                server.shutdown();
        }*/

        try
        {
            logger.info("Stopping listening for connections ...");
            if(serverSocket!=null)
                if(!serverSocket.isClosed())
                {
                    stop.set(true);
                    serverSocket.close();
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            logger.info("... Done!");
        }
    }

    @Override
    public void run() {
        logger.warning("Starting main server on port "+port+" ...");

        try {

            logger.info("Starting server on port "+port+" ...");

            serverSocket = new ServerSocket(port);

            setupStageTitle(true);

            while(!stop.get())
            {
                Socket s = serverSocket.accept();
                ClientConnections.getInstance().addConnection(new ClientConnection(s, serverListener, exceptionAndAlertHandler));

                logger.info("New client connected ("+s.getRemoteSocketAddress()+") !");
            }

        }
        catch (SocketException e)
        {
            if(!e.getMessage().contains("Socket closed"))
            {
                logger.info("Main Server stopped accepting calls ...");

                setupStageTitle(false);

                exceptionAndAlertHandler.handleMinorException(new MinorException("Sorry!","Server could not be started at "+port+".\n" +
                        "This could be due to another process or another instance of Stream-Pi Server using the same port. \n\n" +
                        "If another Server Instance probably running, close it. If not, try changing the port in settings and restart Stream-Pi Server." +
                        "If the problem still persists, consider contacting us. \n\nFull Message : "+e.getMessage()));
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            exceptionAndAlertHandler.handleSevereException(new SevereException("MainServer io Exception occurred!"));
            e.printStackTrace();
        }
    }

    private void setupStageTitle(boolean isSuccess)
    {
        try
        {
            if(isSuccess)
            {
                StringBuilder ips = new StringBuilder();

                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                while(e.hasMoreElements())
                {
                    NetworkInterface n = e.nextElement();
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements())
                    {
                        InetAddress i = ee.nextElement();
                        String hostAddress = i.getHostAddress();
                        if(i instanceof Inet4Address)
                        {
                            ips.append(hostAddress);
                            if(e.hasMoreElements())
                                ips.append(" / ");
                        }
                    }
                }

                Platform.runLater(()-> serverListener.getStage().setTitle("Stream-Pi Server - IP(s): "+ips.toString()+" | Port: "+ port));
            }
            else
            {
                Platform.runLater(()-> serverListener.getStage().setTitle("Stream-Pi Server - Offline"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException("Error",e.getMessage()));
        }
    }


}
