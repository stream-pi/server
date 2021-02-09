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

package com.stream_pi.server.client;

import com.stream_pi.util.platform.Platform;
import com.stream_pi.util.platform.ReleaseStatus;
import com.stream_pi.util.version.Version;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Client
{
    private String nickName;
    private final SocketAddress remoteSocketAddress;
    private final Platform platform;
    private final Version version;
    private final Version commStandardVersion;
    private final Version themeAPIVersion;
    private final ReleaseStatus releaseStatus;

    private double startupDisplayHeight, startupDisplayWidth;

    private final HashMap<String,ClientProfile> profiles;

    private final HashMap<String,ClientTheme> themes;

    private String defaultProfileID;
    private String defaultThemeFullName;

    public Client(Version version, ReleaseStatus releaseStatus, Version commStandardVersion, Version themeAPIVersion, String nickName, Platform platform, SocketAddress remoteSocketAddress)
    {
        this.version = version;
        this.releaseStatus = releaseStatus;
        this.commStandardVersion = commStandardVersion;
        this.themeAPIVersion = themeAPIVersion;
        this.nickName = nickName;
        this.remoteSocketAddress = remoteSocketAddress;
        this.platform = platform;
        this.profiles = new HashMap<>();
        this.themes = new HashMap<>();
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
        themes.put(clientTheme.getThemeFullName(), (ClientTheme) clientTheme.clone());
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

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
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

    public String getNickName()
    {
        return nickName;
    }

    public Version getVersion()
    {
        return version;
    }

    public Version getCommStandardVersion()
    {
        return commStandardVersion;
    }

    public Version getThemeAPIVersion()
    {
        return themeAPIVersion;
    }

    public double getStartupDisplayHeight()
    {
        return startupDisplayHeight;
    }

    public double getStartupDisplayWidth()
    {
        return startupDisplayWidth;
    }

    public void setStartupDisplayHeight(double height)
    {
        startupDisplayHeight = height;
    }

    public void setStartupDisplayWidth(double width)
    {
        startupDisplayWidth = width;
    }
    
    private int getMaxRows(int eachActionSize)
    {
        return (int) (startupDisplayHeight / eachActionSize);
    }

    public int getMaxCols(int eachActionSize) 
    {
        return (int) (startupDisplayWidth / eachActionSize);
    }

}
