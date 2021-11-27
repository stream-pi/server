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
    }

    public static String[] generateRuntimeArgumentsForStartOnBoot()
    {
        List<String> arrayList = new ArrayList<>();

        if (StartupFlags.RUNNER_FILE_NAME!=null)
        {
            arrayList.add("Stream-Pi.startupRunnerFileName='"+StartupFlags.RUNNER_FILE_NAME+"'");
        }

        arrayList.add("Stream-Pi.startMinimised=true");
        arrayList.add("Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation="+StartupFlags.APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION);
        arrayList.add("Stream-Pi.allowRoot="+StartupFlags.ALLOW_ROOT);

        return arrayList.toArray(new String[0]);
    }
}
