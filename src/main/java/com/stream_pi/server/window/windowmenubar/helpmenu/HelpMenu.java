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

package com.stream_pi.server.window.windowmenubar.helpmenu;

import com.stream_pi.server.i18n.I18N;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class HelpMenu extends Menu
{
    private MenuItem websiteMenuItem, donateMenuItem, aboutMenuItem;

    public HelpMenu()
    {
        setText(I18N.getString("window.windowmenubar.helpmenu.HelpMenu.help"));
        getStyleClass().add("menu_bar_help_menu");

        websiteMenuItem = new MenuItem(I18N.getString("website"));
        donateMenuItem = new MenuItem(I18N.getString("donate"));
        aboutMenuItem = new MenuItem(I18N.getString("about"));

        getItems().addAll(websiteMenuItem, new SeparatorMenuItem(), donateMenuItem, aboutMenuItem);
    }

    public MenuItem getWebsiteMenuItem()
    {
        return websiteMenuItem;
    }

    public MenuItem getDonateMenuItem()
    {
        return donateMenuItem;
    }

    public MenuItem getAboutMenuItem()
    {
        return aboutMenuItem;
    }
}
