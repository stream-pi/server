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

package com.stream_pi.server.client;

import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.ReleaseStatus;
import com.stream_pi.util.version.Version;
import javafx.geometry.Orientation;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Client
{
    private String name;
    private final SocketAddress remoteSocketAddress;
    private final Platform platform;
    private final Version version;
    private final Version communicationProtocolVersion;
    private final Version themeAPIVersion;
    private final ReleaseStatus releaseStatus;

    private double displayHeight, displayWidth;

    private final HashMap<String,ClientProfile> profiles;

    private final HashMap<String,ClientTheme> themes;

    private String defaultProfileID;
    private String defaultThemeFullName;

    private Orientation orientation;

    public Client(Version version, ReleaseStatus releaseStatus, Version communicationProtocolVersion,
                  Version themeAPIVersion, String name, Platform platform, SocketAddress remoteSocketAddress,
                  Orientation orientation)
    {
        this.version = version;
        this.releaseStatus = releaseStatus;
        this.communicationProtocolVersion = communicationProtocolVersion;
        this.themeAPIVersion = themeAPIVersion;
        this.name = name;
        this.remoteSocketAddress = remoteSocketAddress;
        this.platform = platform;
        this.profiles = new HashMap<>();
        this.themes = new HashMap<>();
        this.orientation = orientation;
    }

    public ReleaseStatus getReleaseStatus() {
        return releaseStatus;
    }

    public void setDefaultThemeFullName(String defaultThemeFullName) {
        this.defaultThemeFullName = defaultThemeFullName;
    }

    public String getDefaultThemeFullName() {
        return defaultThemeFullName;
    }

    public void setDefaultProfileID(String ID)
    {
        defaultProfileID = ID;
    }

    public void addTheme(ClientTheme clientTheme) throws CloneNotSupportedException
    {
        themes.put(clientTheme.getFullName(), (ClientTheme) clientTheme.clone());
    }

    public ArrayList<ClientTheme> getThemes()
    {
        ArrayList<ClientTheme> clientThemes = new ArrayList<>();
        for(String clientTheme : themes.keySet())
        {
            clientThemes.add(themes.get(clientTheme));
        }
        return clientThemes;
    }

    public ClientTheme getThemeByFullName(String fullName)
    {
        return themes.getOrDefault(fullName, null);
    }

    public String getDefaultProfileID()
    {
        return defaultProfileID;
    }

    //client Profiles

    public void setName(String name)
    {
        this.name = name;
    }

    public List<ClientProfile> getAllClientProfiles()
    {
        ArrayList<ClientProfile> clientProfiles = new ArrayList<>();
        for(String profile : profiles.keySet())
            clientProfiles.add(profiles.get(profile));
        return clientProfiles;
    }

    public void removeProfileFromID(String ID)
    {
        profiles.remove(ID);
    }

    public void addProfile(ClientProfile clientProfile) throws CloneNotSupportedException {
        profiles.put(clientProfile.getID(), (ClientProfile) clientProfile.clone());
    }

    public synchronized ClientProfile getProfileByID(String ID) {
        return profiles.getOrDefault(ID, null);
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return remoteSocketAddress;
    }

    public Platform getPlatform()
    {
        return platform;
    }

    public String getName()
    {
        return name;
    }

    public Version getVersion()
    {
        return version;
    }

    public Version getCommunicationProtocolVersion()
    {
        return communicationProtocolVersion;
    }

    public Version getThemeAPIVersion()
    {
        return themeAPIVersion;
    }

    public double getDisplayHeight()
    {
        return displayHeight;
    }

    public double getDisplayWidth()
    {
        return displayWidth;
    }

    public void setDisplayHeight(double height)
    {
        displayHeight = height;
    }

    public void setDisplayWidth(double width)
    {
        displayWidth = width;
    }
    
    private int getMaxRows(int eachActionSize)
    {
        return (int) (displayHeight / eachActionSize);
    }

    public int getMaxCols(int eachActionSize) 
    {
        return (int) (displayWidth / eachActionSize);
    }

    public void setOrientation(Orientation orientation)
    {
        this.orientation = orientation;
    }

    public Orientation getOrientation()
    {
        return orientation;
    }
}
