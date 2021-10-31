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

/*
ServerInfo.java

Stores basic information about the server - name, platform type

Contributors: Debayan Sutradhar (@dubbadhar)
 */

package com.stream_pi.server.info;

import com.stream_pi.server.Main;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.ReleaseStatus;
import com.stream_pi.util.version.Version;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerInfo
{
    private Version version;
    private final ReleaseStatus releaseStatus;
    private final Platform platform;
    private String prePath;
    private Version communicationProtocolVersion;
    private String buildDate;
    private String license;

    private static ServerInfo instance = null;


    private ServerInfo()
    {
        version = new Version(1,0,0);
        communicationProtocolVersion = new Version(1,0,0);

        releaseStatus = ReleaseStatus.EA;
        prePath = System.getProperty("user.home")+"/Stream-Pi/Server/";

        String osName = System.getProperty("os.name").toLowerCase();

        if(osName.contains("windows"))
        {
            platform = Platform.WINDOWS;
        }
        else if (osName.contains("linux"))
        {
            platform = Platform.LINUX;
        }
        else if (osName.contains("mac"))
        {
            platform = Platform.MAC;
        }
        else
        {
            platform = Platform.UNKNOWN;
        }

        try
        {
            InputStream inputStream = ServerInfo.class.getResourceAsStream("build.properties");
            if (inputStream != null)
            {
                Properties properties = new Properties();
                properties.load(inputStream);
                inputStream.close();

                buildDate = properties.getProperty("build.date");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to fetch build.properties!", e);
        }

        try
        {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ServerInfo.class.getResourceAsStream("LICENSE"))));

            StringBuilder licenseTxt = new StringBuilder();
            while(true)
            {
                String line = bufferedReader.readLine();

                if(line == null)
                {
                    break;
                }

                licenseTxt.append(line).append("\n");
            }

            license = licenseTxt.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to fetch LICENSE!", e);
        }

    }

    
    public String getPrePath() {
        return prePath;
    }

    public static synchronized ServerInfo getInstance()
    {
        if(instance == null)
        {
            instance = new ServerInfo();
        }

        return instance;
    }


    public Platform getPlatform()
    {
        return platform;
    }

    public Version getVersion() {
        return version;
    }

    public ReleaseStatus getReleaseStatus()
    {
        return releaseStatus;
    }

    public Version getCommunicationProtocolVersion()
    {
        return communicationProtocolVersion;
    }

    public String getBuildDate()
    {
        return buildDate;
    }

    public String getLicense()
    {
        return license;
    }
}
