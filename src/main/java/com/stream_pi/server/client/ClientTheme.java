package com.stream_pi.server.client;

public class ClientTheme implements Cloneable
{
    public String fullName;
    public String shortName;
    public String author;
    public String version;

    public ClientTheme(String fullName, String shortName,
                       String author, String version)
    {
        this.fullName = fullName;
        this.shortName = shortName;
        this.author = author;
        this.version = version;
    }

    public String getFullName()
    {
        return fullName;
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