/*
Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macropad
Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Written by : Debayan Sutradhar (rnayabed)
*/


package com.StreamPi.Server;

import com.StreamPi.Server.Controller.Controller;
import com.StreamPi.Server.Info.ServerInfo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * First method to be called
     * This method first parses all the available command line arguments passed.
     * Then a new instance of Controller is created, and then initialised.
     */
    public void start(Stage stage) {

        for(String eachArg : getParameters().getRaw())
        {
            String[] r = eachArg.split("=");
            if(r[0].equals("-DStreamPi.startupRunnerFileName"))
                ServerInfo.getInstance().setRunnerFileName(r[1]);
            else if(r[0].equals("-DStreamPi.startupMode"))
                ServerInfo.getInstance().setStartMinimised(r[1].equals("min"));
        }


        Controller d = new Controller();
        Scene s = new Scene(d);
        stage.setScene(s);
        d.setHostServices(getHostServices());
        d.init();
    }

    /**
     * This is a fallback. Called in some JVMs.
     * This method just sends the command line arguments to JavaFX Application
     */
    public static void main(String[] args) 
    {

        launch(args);
    }
}
