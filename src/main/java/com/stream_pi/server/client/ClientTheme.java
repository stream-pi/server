package com.stream_pi.server.client;

public class ClientTheme implements Cloneable
{
    public String themeFullName;
    public String shortName;
    public String author;
    public String version;

    public ClientTheme(String themeFullName, String shortName,
                       String author, String version)
    {
        this.themeFullName = themeFullName;
        this.shortName = shortName;
        this.author = author;
        this.version = version;
    }

    public String getThemeFullName()
    {
        return themeFullName;
    }

    public String getShortName()
    {
        return shortName;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getVersion() {
        return version;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}