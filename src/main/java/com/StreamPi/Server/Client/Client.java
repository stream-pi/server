package com.StreamPi.Server.Client;

import com.StreamPi.Server.Connection.ClientConnection;
import com.StreamPi.ThemeAPI.Theme;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Platform.Platform;
import com.StreamPi.Util.Platform.ReleaseStatus;
import com.StreamPi.Util.Version.Version;
import javafx.geometry.Dimension2D;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Client {
    private String nickName;
    private final SocketAddress remoteSocketAddress;
    private final Platform platform;
    private ClientConnection connectionHandler;
    private final Version version;
    private final Version commAPIVersion;
    private final Version themeAPIVersion;
    private final ReleaseStatus releaseStatus;

    private double startupDisplayHeight, startupDisplayWidth;

    private final HashMap<String,ClientProfile> profiles;

    private final HashMap<String,ClientTheme> themes;

    private String defaultProfileID;
    private String defaultThemeFullName;

    private int totalNoOfProfiles;

    public Client(Version version, ReleaseStatus releaseStatus, Version commAPIVersion, Version themeAPIVersion, String nickName, Platform platform, SocketAddress remoteSocketAddress)
    {
        this.version = version;
        this.releaseStatus = releaseStatus;
        this.commAPIVersion = commAPIVersion;
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

    public void setTotalNoOfProfiles(int totalNoOfProfiles)
    {
        this.totalNoOfProfiles = totalNoOfProfiles;
    }

    public int getTotalNoOfProfiles()
    {
        return totalNoOfProfiles;
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

    public void setConnectionHandler(ClientConnection connectionHandler)
    {
        this.connectionHandler = connectionHandler;
    }

    public ClientConnection getConnectionHandler()
    {
        return connectionHandler;
    }

    //Client Profiles

    /*public ArrayList<ClientProfile> getProfiles()
    {
        return profiles;
    }*/

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

    public void removeProfileFromID(String ID) throws MinorException {
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

    public Version getCommAPIVersion()
    {
        return commAPIVersion;
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

    public void debugPrint()
    {
        System.out.println("Client Info : "+
                "\nNickname : "+nickName+
                "\nRemote address : "+remoteSocketAddress+
                "\nPlatform : "+platform.getUIName()+
                "\nVersion : "+version.getText()+
                "\nComm API Version : "+commAPIVersion.getText()+
                "\nTheme API Version : "+themeAPIVersion.getText()+
                "\nDisplay Width : "+startupDisplayWidth+
                "\nDisplay Height : "+startupDisplayHeight+
                "\nDefault Profile ID : "+defaultProfileID);
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
