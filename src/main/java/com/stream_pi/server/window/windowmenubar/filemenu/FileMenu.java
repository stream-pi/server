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

public class FileMenu extends Menu
{
    private SettingsMenu settingsMenu;
    private MenuItem exitMenuItem;
    private MenuItem disconnectFromAllClients;

    public FileMenu()
    {
        setText(I18N.getString("window.windowmenubar.filemenu.FileMenu.file"));
        getStyleClass().add("menu_bar_file_menu");

        settingsMenu = new SettingsMenu();

        disconnectFromAllClients = new MenuItem(I18N.getString("window.windowmenubar.filemenu.FileMenu.disconnectFromAllClients"));
        exitMenuItem = new MenuItem(I18N.getString("exit"));

        getItems().addAll(settingsMenu, disconnectFromAllClients, exitMenuItem);
    }

    public SettingsMenu getSettingsMenu()
    {
        return settingsMenu;
    }

    public MenuItem getDisconnectFromAllClients()
    {
        return disconnectFromAllClients;
    }

    public MenuItem getExitMenuItem()
    {
        return exitMenuItem;
    }
}