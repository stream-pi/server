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
package com.stream_pi.server;

import com.stream_pi.server.controller.Controller;
import com.stream_pi.server.info.ServerInfo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    /**
     * First method to be called
     * This method first parses all the available command line arguments passed.
     * Then a new instance of controller is created, and then initialised.
     */
    public void start(Stage stage) {
        for(String eachArg : getParameters().getRaw())
        {
            if(!eachArg.startsWith("-DStream-Pi"))
                continue;

            String[] r = eachArg.split("=");
            String arg = r[0];
            String val = r[1];

            if(arg.equals("-DStream-Pi.startupRunnerFileName"))
                ServerInfo.getInstance().setRunnerFileName(val);
            else if(arg.equals("-DStream-Pi.startupMode"))
                ServerInfo.getInstance().setStartMinimised(val.equals("min"));
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
