package com.stream_pi.server.connection;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MainServer extends Thread
{
    private ServerListener serverListener;

    private Logger logger = Logger.getLogger(MainServer.class.getName());
    private int port;
    private String ip = null;
    private ServerSocket serverSocket = null;

    private AtomicBoolean stop = new AtomicBoolean(false);

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    public MainServer(ServerListener serverListener, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.serverListener = serverListener;
    }


    public void setPort(int port)
    {
        this.port = port;
    }

    public void setIP(String ip)
    {
        this.ip = ip;
    }

    @Override
    public synchronized void start() {
        stop.set(false);
        super.start();
    }

    public void stopListeningForConnections()
    {
        try
        {
            logger.info("Stopping listening for connections ...");
            if(serverSocket!=null)
            {
                if(!serverSocket.isClosed())
                {
                    stop.set(true);
                    serverSocket.close();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally {
            logger.info("... Done!");
        }
    }

    @Override
    public void run()
    {
        Platform.runLater(()-> serverListener.getStage().setTitle("Stream-Pi Server - Starting Server ... "));

        try
        {
            logger.info("Starting server on port "+port+" ...");

            if (ip.isBlank())
            {
                logger.info("No preferred IP to bind to! Binding on all IPs ...");
                serverSocket = new ServerSocket(port);
            }
            else
            {
                InetAddress address = InetAddress.getByName(ip);
                logger.info("Binding to '"+ip+"' ...");
                serverSocket = new ServerSocket(port, 0, address);
            }

            setupStageTitle();
            while(!stop.get())
            {
                Socket s = serverSocket.accept();
                ClientConnections.getInstance().addConnection(new ClientConnection(s, serverListener, exceptionAndAlertHandler));

                logger.info("New client connected ("+s.getRemoteSocketAddress()+") !");
            }
        }
        catch (BindException | UnknownHostException e)
        {
            logger.warning("Main Server stopped accepting calls ...");
            serverListener.onServerStartFailure();


            serverListener.showUserChooseIPDialog();
        }
        catch (SocketException e)
        {
            if(!e.getMessage().contains("Socket closed") && !e.getMessage().contains("Interrupted function call: accept failed"))
            {

                logger.warning("Main Server stopped accepting calls ...");
                serverListener.onServerStartFailure();

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

    private void setupStageTitle()
    {
        try
        {
            if (ip.isBlank())
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

                Platform.runLater(()-> serverListener.getStage().setTitle("Stream-Pi Server - IP(s): "+ips+" | Port: "+ port));
            }
            else
            {
                Platform.runLater(()-> serverListener.getStage().setTitle("Stream-Pi Server - IP: "+ip+" | Port: "+ port));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(new MinorException("Error",e.getMessage()));
        }
    }
}
