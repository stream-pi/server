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

import com.stream_pi.server.info.StartupFlags;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URISyntaxException;

public class Main extends Application
{

    /**
     * First method to be called
     * This method first parses all the available command line arguments passed.
     * Then a new instance of controller is created, and then initialised.
     */
    public void start(Stage stage) {
        for(String eachArg : getParameters().getRaw())
        {
            if(!eachArg.startsWith("Stream-Pi"))
                continue;

            String[] r = eachArg.split("=");
            String arg = r[0];
            String val = r[1];

            switch (arg)
            {
                case "Stream-Pi.startupRunnerFileName":
                    StartupFlags.RUNNER_FILE_NAME = val;
                    break;
                case "Stream-Pi.startMinimised":
                    StartupFlags.START_MINIMISED = val.equals("true");
                    break;
                case "Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation":
                    StartupFlags.APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = val.equals("true");
                    break;
            }
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
