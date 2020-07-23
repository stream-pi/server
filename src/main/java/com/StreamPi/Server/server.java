package com.StreamPi.Server;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class server implements Runnable{

    ServerSocket serverSocket;
    Socket socket;
    Logger logger;

    int port;
    dash dash;

    public server(int port, dash dash)
    {
        this.port = port;
        this.dash = dash;
    }

    DataInputStream dis;
    InputStream is;
    DataOutputStream dos;
    OutputStream os;

    fileTransferConnection fileTransferConnection;

    @Override
    public void run() {
        try
        {
            logger = LoggerFactory.getLogger(server.class);


            logger.info("Server starting on "+port+" ...");
            serverSocket = new ServerSocket(port);
            logger.info("... Server started!");

            String ip = Inet4Address.getLocalHost().getHostAddress();
            Platform.runLater(()->{
//                dash.listeningSubHeadingLabel.setText("Server running at "+ ip +", Port "+port);
            });

            logger.info("Listening for StreamPis ...");
            socket = serverSocket.accept();
            is = socket.getInputStream();
            os = socket.getOutputStream();

            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);

            logger.info("Connected!");

            writeStr("asd");
            while(socket.isConnected())
            {
                logger.info("Listening for data ...\n");
                String rawData = dis.readUTF();

                logger.debug("Raw Data : "+rawData);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            close();
        }
    }

    public void close()
    {
        try
        {
            serverSocket.close();
            logger.info("Socket closed!\nQuitting Thread");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeStr(String txt) throws Exception
    {
        if(socket.isConnected())
        {
            dos.writeUTF(txt);
        }
    }
}
