package com.stream_pi.server.connection;

import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.SevereException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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
        this.port = port;
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
                    serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            logger.info("... Done!");
        }
        stop.set(true);
    }

    @Override
    public void run() {
        logger.warning("Starting main server on port "+port+" ...");

        try {

            logger.info("Starting server on port "+port+" ...");
            /*Server server = ServerBuilder.forPort(port)
                    .addService(new ConnectionService(serverListener))
                    .build();

            server.start();
            logger.info("... Done!");*/

            serverSocket = new ServerSocket(port);

            while(!stop.get())
            {
                Socket s = serverSocket.accept();
                ClientConnections.getInstance().addConnection(new ClientConnection(s, serverListener, exceptionAndAlertHandler));

                logger.info("New client connected ("+s.getRemoteSocketAddress()+") !");
            }

        }
        catch (SocketException e)
        {
            logger.info("Main Server stopped accepting calls ...");
            e.printStackTrace(); //more likely stopped listening;
        } catch (IOException e) {
            exceptionAndAlertHandler.handleSevereException(new SevereException("MainServer io Exception occurred!"));
            e.printStackTrace();
        }
    }


}
