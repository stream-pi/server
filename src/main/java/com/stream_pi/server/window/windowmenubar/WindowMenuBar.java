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

package com.stream_pi.server.window.windowmenubar;


import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.window.windowmenubar.filemenu.FileMenu;
import com.stream_pi.server.window.windowmenubar.helpmenu.HelpMenu;
import javafx.scene.control.*;

public class WindowMenuBar extends MenuBar
{
    private final FileMenu fileMenu;
    private final HelpMenu helpMenu;
    private final Label showIPPortConfigurationMenuLabel;

    public WindowMenuBar()
    {
        getStyleClass().add("menu_bar");

        fileMenu = new FileMenu();
        helpMenu = new HelpMenu();

        Menu showIPPortConfigurationMenu = new Menu();
        showIPPortConfigurationMenu.getStyleClass().add("menu_bar_show_ip_port_configuration_menu");
        showIPPortConfigurationMenuLabel = new Label(I18N.getString("window.windowmenubar.WindowMenuBar.showIPPortConfiguration"));
        showIPPortConfigurationMenuLabel.getStyleClass().add("menu_bar_show_ip_port_configuration_menu_label");
        showIPPortConfigurationMenu.setGraphic(showIPPortConfigurationMenuLabel);

        getMenus().addAll(fileMenu, helpMenu, showIPPortConfigurationMenu);
    }


    public FileMenu getFileMenu()
    {
        return fileMenu;
    }

    public HelpMenu getHelpMenu()
    {
        return helpMenu;
    }

    public Label getShowIPPortConfigurationMenuLabel()
    {
        return showIPPortConfigurationMenuLabel;
    }
}
