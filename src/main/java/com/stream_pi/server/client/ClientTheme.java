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

import com.stream_pi.util.version.Version;

public class ClientTheme implements Cloneable
{
    public String fullName;
    public String shortName;
    public String author;
    public Version version;

    public ClientTheme(String fullName, String shortName,
                       String author, Version version)
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

    public Version getVersion() {
        return version;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}