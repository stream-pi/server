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

package com.stream_pi.server.window.windowmenubar;


import com.stream_pi.server.window.windowmenubar.filemenu.FileMenu;
import com.stream_pi.server.window.windowmenubar.helpmenu.HelpMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class WindowMenuBar extends MenuBar
{
    private FileMenu fileMenu;
    private HelpMenu helpMenu;


    public WindowMenuBar()
    {
        getStyleClass().add("base_menu_bar");


        fileMenu = new FileMenu();

        helpMenu = new HelpMenu();


        getMenus().addAll(fileMenu, helpMenu);
    }


    public FileMenu getFileMenu()
    {
        return fileMenu;
    }

    public HelpMenu getHelpMenu()
    {
        return helpMenu;
    }
}
