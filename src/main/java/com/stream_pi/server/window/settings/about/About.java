/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macropad
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
 *
 */

package com.stream_pi.server.window.settings.about;

import com.stream_pi.action_api.ActionAPI;
import com.stream_pi.theme_api.ThemeAPI;
import com.stream_pi.util.Util;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.Main;
import com.stream_pi.util.links.Links;
import javafx.application.HostServices;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class About extends VBox
{

    private final HostServices hostServices;

    public About(HostServices hostServices)
    {
        getStyleClass().add("about");
        this.hostServices = hostServices;

        setAlignment(Pos.TOP_CENTER);

        Image appIcon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/256x256.png")));
        ImageView appIconImageView = new ImageView(appIcon);
        VBox.setMargin(appIconImageView, new Insets(10, 0, 10, 0));
        appIconImageView.setFitHeight(128);
        appIconImageView.setFitWidth(128);

        TabPane tabPane = new TabPane();
        tabPane.addEventFilter(SwipeEvent.ANY, Event::consume);
        tabPane.getStyleClass().add("settings_about_tab_internal");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(600);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab licenseTab = new Tab(I18N.getString("window.settings.about.About.licenseTabHeading"));
        licenseTab.setContent(new LicenseTab());


        Tab contributorsTab = new Tab(I18N.getString("window.settings.about.About.contributorsTabHeading"));
        contributorsTab.setContent(new ContributorsTab(hostServices));

        Tab contactTab = new Tab(I18N.getString("window.settings.about.About.contactTabHeading"));
        contactTab.setContent(new ContactTab(hostServices));

        tabPane.getTabs().addAll(licenseTab, contributorsTab, contactTab);


        Hyperlink donateButton = new Hyperlink(I18N.getString("donate"));
        donateButton.setOnAction(event -> openWebpage(Links.getDonateLink()));
        donateButton.getStyleClass().add("about_donate_hyperlink");

        ServerInfo serverInfo = ServerInfo.getInstance();

        Label versionLabel = new Label(serverInfo.getVersion().getText() + " - "+ serverInfo.getPlatform().getUIName() + " - "+ serverInfo.getReleaseStatus().getUIName());
        versionLabel.getStyleClass().add("about_version_label");

        Label commStandardLabel = new Label(I18N.getString("window.settings.about.About.serverCommunicationProtocolVersion", serverInfo.getCommunicationProtocolVersion().getText()));
        commStandardLabel.getStyleClass().add("about_comm_standard_label");

        Label minThemeAPILabel = new Label(I18N.getString("window.settings.about.About.minThemeAPI", ThemeAPI.MIN_VERSION_SUPPORTED.getText()));
        minThemeAPILabel.getStyleClass().add("about_min_theme_api_label");

        Label minActionAPILabel = new Label(I18N.getString("window.settings.about.About.minActionAPI", ActionAPI.MIN_VERSION_SUPPORTED.getText()));
        minActionAPILabel.getStyleClass().add("about_min_action_api_label");

        Label currentActionAPILabel = new Label(I18N.getString("window.settings.about.About.currentActionAPI", ActionAPI.VERSION.getText()));
        currentActionAPILabel.getStyleClass().add("about_current_action_api_label");

        Label currentUtilLabel = new Label(I18N.getString("window.settings.about.About.currentUtil", Util.VERSION.getText()));
        currentUtilLabel.getStyleClass().add("about_current_util_label");

        HBox hBox1 = new HBox(commStandardLabel, getSep(),
                minThemeAPILabel, getSep(),
                minActionAPILabel, getSep(),
                currentActionAPILabel, getSep(),
                currentUtilLabel);

        hBox1.setAlignment(Pos.CENTER);
        hBox1.setSpacing(10);

        Label javaVersionLabel = new Label(I18N.getString("window.settings.about.About.java", System.getProperty("java.version")));
        javaVersionLabel.getStyleClass().add("about_java_version");

        Label javafxVersionLabel = new Label(I18N.getString("window.settings.about.About.javafx", System.getProperty("javafx.version")));
        javafxVersionLabel.getStyleClass().add("about_javafx_version");

        Label javaGCLabel = new Label(I18N.getString("window.settings.about.About.gc", ManagementFactory.getGarbageCollectorMXBeans().get(0).getName()));
        javaGCLabel.getStyleClass().add("about_java_gc");

        HBox hBox2 = new HBox(versionLabel, getSep(),
                javaVersionLabel, getSep(),
                javafxVersionLabel, getSep(),
                javaGCLabel);

        hBox2.setAlignment(Pos.CENTER);
        hBox2.setSpacing(10);


        getChildren().addAll(appIconImageView, tabPane, donateButton, hBox1, hBox2);

        try
        {
            InputStream inputStream = Main.class.getResourceAsStream("build.properties");
            if (inputStream != null)
            {
                Properties properties = new Properties();
                properties.load(inputStream);
                inputStream.close();

                Label buildDateLabel = new Label(I18N.getString("window.settings.about.About.buildDate", properties.getProperty("build.date")));
                buildDateLabel.getStyleClass().add("about_build_date_label");
                hBox2.getChildren().addAll(getSep(), buildDateLabel);
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(getClass().getName()).warning("build.properties not present");
        }
    }

    public void openWebpage(String url)
    {
        hostServices.showDocument(url);
    }

    private Label getSep()
    {
        Label label = new Label("|");
        label.getStyleClass().add("separator_ui_label");
        return label;
    }
}
