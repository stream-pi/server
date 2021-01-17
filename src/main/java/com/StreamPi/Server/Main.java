/*
Main.java

First class started when the app runs.

Written by Debayan Sutradhar (@rnayabed)
 */


package com.StreamPi.Server;

import com.StreamPi.Server.Controller.Controller;
import com.StreamPi.Server.Info.ServerInfo;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage stage) {
        Controller d = new Controller();                                                        //Starts new dash instance

        Scene s = new Scene(d);                                                     //Starts new scene instance from dash
        stage.setScene(s);                                                          //Init Scene
        d.setHostServices(getHostServices());
        d.init();
    }

    public static void main(String[] args) 
    {
        for(String eachArg : args)
        {
            String[] r = eachArg.split("=");
            if(r[0].equals("-DStreamPi.startupRunnerFileName"))
                ServerInfo.getInstance().setRunnerFileName(r[1]);
            else if(r[0].equals("-DStreamPi.startupMode"))
                ServerInfo.getInstance().setStartMinimised(r[1].equals("min"));
        }
        
        
        launch(args);
    }
}
