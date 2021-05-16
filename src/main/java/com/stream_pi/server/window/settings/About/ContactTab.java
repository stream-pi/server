package com.stream_pi.server.window.settings.About;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;


public class ContactTab extends ScrollPane
{
    private final HostServices hostServices;


    public ContactTab(HostServices hostServices)
    {
        this.hostServices = hostServices;

        getStyleClass().add("about_contact_tab_scroll_pane");

        Hyperlink github = new Hyperlink("GitHub");
        github.setOnAction(event -> openWebpage("https://github.com/stream-pi"));

        Hyperlink discord = new Hyperlink("Discord");
        discord.setOnAction(event -> openWebpage("https://discord.gg/BExqGmk"));

        Hyperlink website = new Hyperlink("Website");
        website.setOnAction(event -> openWebpage("https://stream-pi.com"));

        Hyperlink twitter = new Hyperlink("Twitter");
        twitter.setOnAction(event -> openWebpage("https://twitter.com/stream_pi"));

        Hyperlink matrix = new Hyperlink("Matrix");
        matrix.setOnAction(event -> openWebpage("https://matrix.to/#/+stream-pi-official:matrix.org"));


        VBox vBox = new VBox(github, discord, website, twitter, matrix);
        vBox.setSpacing(10.0);

        setContent(vBox);
    }


    public void openWebpage(String url)
    {
        hostServices.showDocument(url);
    }

}
