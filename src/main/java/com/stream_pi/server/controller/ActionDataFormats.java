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

package com.stream_pi.server.controller;

import javafx.scene.input.DataFormat;

public class ActionDataFormats
{
    public static final DataFormat IS_NEW = new DataFormat("Is New");
    public static final DataFormat ACTION_TYPE = new DataFormat("Action Type");
    public static final DataFormat UNIQUE_ID = new DataFormat("Unique ID");
    public static final DataFormat CLIENT_PROPERTIES = new DataFormat("Client Properties");
    public static final DataFormat ICONS = new DataFormat("Icons");
    public static final DataFormat CURRENT_ICON_STATE = new DataFormat("Current Icon State");
    public static final DataFormat BACKGROUND_COLOUR = new DataFormat("Background Colour");
    public static final DataFormat DISPLAY_TEXT_FONT_COLOUR = new DataFormat("Display Text Font Colour");
    public static final DataFormat DISPLAY_TEXT = new DataFormat("Display Text");
    public static final DataFormat DISPLAY_TEXT_ALIGNMENT = new DataFormat("Display Text Alignment");
    public static final DataFormat DISPLAY_TEXT_SHOW = new DataFormat("Display Text Show");
}
