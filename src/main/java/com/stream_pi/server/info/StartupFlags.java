/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
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

import javafx.application.Application;

import java.util.ArrayList;
import java.util.List;

public class StartupFlags
{
    public static String RUNNER_FILE_NAME = null;
    public static boolean START_MINIMISED = false;
    public static boolean APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = false;
    public static boolean ALLOW_ROOT = false;

    public static final String RUNNER_FILE_NAME_ARG = "Stream-Pi.startupRunnerFileName";
    public static final String START_MINIMISED_ARG = "Stream-Pi.startMinimised";
    public static final String APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION_ARG = "Stream-Pi.appendPathBeforeRunnerFileToOvercomeJPackageLimitation";
    public static final String ALLOW_ROOT_ARG = "Stream-Pi.allowRoot";

    public static void init(Application.Parameters parameters)
    {
        for (String arg : parameters.getRaw())
        {
            String[] arr = arg.split("=");

            if (arr.length == 2)
            {
                String val = arr[1].strip();
                switch(arr[0])
                {
                    case RUNNER_FILE_NAME_ARG: RUNNER_FILE_NAME = val; break;
                    case START_MINIMISED_ARG: START_MINIMISED = val.equals("true"); break;
                    case APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION_ARG: APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION = val.equals("true"); break;
                    case ALLOW_ROOT_ARG: ALLOW_ROOT = val.equals("true"); break;
                }
            }
        }
    }

    public static String[] generateRuntimeArgumentsForStartOnBoot()
    {
        List<String> arrayList = new ArrayList<>();

        if (RUNNER_FILE_NAME!=null)
        {
            arrayList.add(RUNNER_FILE_NAME_ARG+"='"+RUNNER_FILE_NAME+"'");
        }

        arrayList.add(START_MINIMISED_ARG+"=true");
        arrayList.add(APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION_ARG+"="+APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION);
        arrayList.add(ALLOW_ROOT_ARG+"="+ALLOW_ROOT);

        return arrayList.toArray(new String[0]);
    }
}
