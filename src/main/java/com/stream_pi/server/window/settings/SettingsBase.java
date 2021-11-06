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

package com.stream_pi.server.window.settings;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.settings.about.About;
import javafx.application.HostServices;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsBase extends VBox
{

    private TabPane tabPane;

    private GeneralSettings generalSettings;
    private PluginsSettings pluginsSettings;
    private ThemesSettings themesSettings;
    private ClientsSettings clientsSettings;

    private Button closeButton;

    private HostServices hostServices;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public SettingsBase(HostServices hostServices, ExceptionAndAlertHandler exceptionAndAlertHandler,
                        ServerListener serverListener)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.hostServices = hostServices;

        tabPane = new TabPane();
        tabPane.addEventFilter(SwipeEvent.ANY, Event::consume);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab generalSettingsTab = new Tab(I18N.getString("window.settings.SettingsBase.general"));
        generalSettings = new GeneralSettings(exceptionAndAlertHandler, serverListener, hostServices);
        generalSettingsTab.setContent(generalSettings);

        Tab pluginsSettingsTab = new Tab(I18N.getString("window.settings.SettingsBase.plugins"));
        pluginsSettings = new PluginsSettings(exceptionAndAlertHandler, hostServices);
        pluginsSettingsTab.setContent(pluginsSettings);

        Tab themesSettingsTab = new Tab(I18N.getString("window.settings.SettingsBase.themes"));
        themesSettings = new ThemesSettings(hostServices);
        themesSettingsTab.setContent(themesSettings);

        Tab clientsSettingsTab = new Tab(I18N.getString("window.settings.SettingsBase.clients"));
        clientsSettings = new ClientsSettings(exceptionAndAlertHandler, serverListener);
        clientsSettingsTab.setContent(clientsSettings);

        Tab aboutTab = new Tab(I18N.getString("window.settings.SettingsBase.about"));
        aboutTab.setContent(new About(hostServices));

        tabPane.getTabs().addAll(generalSettingsTab, pluginsSettingsTab, themesSettingsTab, clientsSettingsTab, aboutTab);


        closeButton = new Button(I18N.getString("window.settings.SettingsBase.close"));
        closeButton.getStyleClass().add("settings_close_button");
        VBox.setMargin(closeButton, new Insets(0,10, 10, 0));

        getChildren().addAll(tabPane, closeButton);

        setCache(true);
        setCacheHint(CacheHint.SCALE);

        getStyleClass().add("settings_base");
    }

    public void setDefaultTabToGeneral()
    {
        tabPane.getSelectionModel().selectFirst();
    }

    public Button getCloseButton()
    {
        return closeButton;
    }

    public GeneralSettings getGeneralSettings()
    {
        return generalSettings;
    }

    public PluginsSettings getPluginsSettings()
    {
        return pluginsSettings;
    }

    public ThemesSettings getThemesSettings()
    {
        return themesSettings;
    }

    public ClientsSettings getClientsSettings()
    {
        return clientsSettings;
    }

}
