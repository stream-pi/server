package com.StreamPi.Server;

import javafx.application.Platform;

import java.io.*;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class server implements Runnable{

    ServerSocket serverSocket;
    Socket socket;

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
            io.pln("Server starting on "+port+" ...");
            serverSocket = new ServerSocket(port);
            io.pln("... Server started!");

            String ip = Inet4Address.getLocalHost().getHostAddress();
            Platform.runLater(()->{
                dash.listeningSubHeadingLabel.setText("Server running at "+ ip +", Port "+port);
            });

            io.pln("Listening for StreamPis ...");
            socket = serverSocket.accept();
            is = socket.getInputStream();
            os = socket.getOutputStream();

            dis = new DataInputStream(is);
            dos = new DataOutputStream(os);

            io.pln("Connected!");

            while(socket.isConnected())
            {
                io.pln("Listening for data ...\n");
                String header = dis.readUTF();
                io.pln("Data header : "+header);
            }
        }
        catch (IOException e)
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
            io.pln("Socket closed!\nQuitting Thread");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
