/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macro Pad
 * Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.stream_pi.server.info;

import java.util.ArrayList;
import java.util.List;

public class StartupFlags
{
    public static String RUNNER_FILE_NAME = null;
    public static boolean START_MINIMISED = false;
    public static boolean APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = false;
    public static boolean ALLOW_ROOT = false;

    public static void init(String[] args)
    {
        for (String arg : args)
        {
            System.out.println("ARG. '"+arg+"'");
            String[] arr = arg.split("=");

            String val = arr[1].strip();
            switch(arr[0])
            {
                case "Stream-Pi.startupRunnerFileName": RUNNER_FILE_NAME = val; break;
                case "Stream-Pi.startMinimised": START_MINIMISED = val.equals("true"); break;
                case "Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation": APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = val.equals("true"); break;
                case "Stream-Pi.allowRoot": ALLOW_ROOT = val.equals("true"); break;
            }
        }


        System.out.println("------PROPERTIES------");
        System.out.println("RUNNER_FILE_NAME: '"+RUNNER_FILE_NAME+"'");
        System.out.println("START_MINIMISED: '"+START_MINIMISED+"'");
        System.out.println("APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION: '"+APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION+"'");
        System.out.println("ALLOW_ROOT: '"+ALLOW_ROOT+"'");
        System.out.println("----------------------");
    }

    public static String[] generateRuntimeArguments(boolean startMinimised,
                                                  boolean appendPathBeforeRunnerFileToOverrideJPackageLimitation,
                                                  boolean allowRoot, String runnerFileName)
    {
        List<String> arrayList = new ArrayList<>();

        arrayList.add("Stream-Pi.startMinimised="+startMinimised);
        arrayList.add("Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation="+appendPathBeforeRunnerFileToOverrideJPackageLimitation);
        arrayList.add("Stream-Pi.allowRoot="+allowRoot);

        if (runnerFileName!=null)
        {
            arrayList.add("Stream-Pi.startupRunnerFileName='"+runnerFileName+"'");
        }

        return arrayList.toArray(new String[0]);
    }
}
