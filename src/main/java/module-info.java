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

module com.stream_pi.server
{

    uses com.stream_pi.action_api.action.Action;
    uses com.stream_pi.action_api.externalplugin.NormalAction;
    uses com.stream_pi.action_api.externalplugin.ExternalPlugin;

    requires com.stream_pi.action_api;
    requires com.stream_pi.util;
    requires com.stream_pi.theme_api;

    requires org.kordamp.ikonli.javafx;

    requires java.xml;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    requires eu.hansolo.medusa;

    requires java.management;
    requires java.desktop;

    requires java.sql;

    opens com.stream_pi.server.window.settings;

    requires org.controlsfx.controls;

    exports com.stream_pi.server;
    opens com.stream_pi.server.window.settings.about;
    opens com.stream_pi.server.combobox;
}