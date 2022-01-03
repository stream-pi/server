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

package com.stream_pi.server.window.windowmenubar.filemenu;

import com.stream_pi.server.i18n.I18N;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class SettingsMenu extends Menu
{
    private MenuItem generalSettingsMenuItem, pluginsSettingsMenuItem, themesSettingsMenuItem, clientSettingsMenuItem;

    public SettingsMenu()
    {
        setText(I18N.getString("window.windowmenubar.filemenu.SettingsMenu.settings"));

        generalSettingsMenuItem = new MenuItem(I18N.getString("general"));
        pluginsSettingsMenuItem = new MenuItem(I18N.getString("plugins"));
        themesSettingsMenuItem = new MenuItem(I18N.getString("themes"));
        clientSettingsMenuItem = new MenuItem(I18N.getString("clients"));

        getItems().addAll(generalSettingsMenuItem, pluginsSettingsMenuItem, themesSettingsMenuItem, clientSettingsMenuItem);
    }

    public MenuItem getGeneralSettingsMenuItem()
    {
        return generalSettingsMenuItem;
    }

    public MenuItem getPluginsSettingsMenuItem()
    {
        return pluginsSettingsMenuItem;
    }

    public MenuItem getThemesSettingsMenuItem()
    {
        return themesSettingsMenuItem;
    }

    public MenuItem getClientSettingsMenuItem()
    {
        return clientSettingsMenuItem;
    }
}
