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
            switch(arg)
            {
                case "Stream-Pi.startupRunnerFileName": RUNNER_FILE_NAME = parseStringArg(arg); break;
                case "Stream-Pi.startMinimised": START_MINIMISED = parseBooleanArg(arg); break;
                case "Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation": APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = parseBooleanArg(arg); break;
                case "Stream-Pi.allowRoot": ALLOW_ROOT = parseBooleanArg(arg); break;
            }
        }
    }

    private static String parseStringArg(String arg)
    {
        return arg.substring(arg.indexOf("=")).strip();
    }

    private static boolean parseBooleanArg(String arg)
    {
        return arg.substring(arg.indexOf("=")).trim().equals("true");
    }
}
