package com.StreamPi.Server.Window.Settings;

import com.StreamPi.ActionAPI.ActionAPI;
import com.StreamPi.Server.Info.License;
import com.StreamPi.Server.Info.ServerInfo;
import com.StreamPi.Server.Main;
import com.StreamPi.Util.Platform.Platform;
import com.StreamPi.Util.Platform.ReleaseStatus;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class About extends VBox{

    private HostServices hostServices;

    public About(HostServices hostServices)
    {
        getStyleClass().add("about");
        this.hostServices = hostServices;

        setAlignment(Pos.TOP_CENTER);

        Image appIcon = new Image(Main.class.getResourceAsStream("app_icon.png"));
        ImageView appIconImageView = new ImageView(appIcon);
        appIconImageView.setFitHeight(196);
        appIconImageView.setFitWidth(182);

        Label licenseLabel = new Label("License");
        licenseLabel.getStyleClass().add("settings_about_license_label");

        VBox.setMargin(licenseLabel, new Insets(20, 0 , 10 ,0));

        TextArea licenseTextArea = new TextArea(License.getLicense());
        licenseTextArea.setWrapText(false);
        licenseTextArea.setEditable(false);
        licenseTextArea.setMaxWidth(550);

        VBox.setVgrow(licenseTextArea, Priority.ALWAYS);

        HBox links = new HBox();

        Hyperlink github = new Hyperlink("GitHub");
        github.setOnAction(event -> {
            openWebpage("https://github.com/Stream-Pi");
        });

        Hyperlink discord = new Hyperlink("Discord");
        discord.setOnAction(event -> {
            openWebpage("https://discord.gg/BExqGmk");
        });

        Hyperlink website = new Hyperlink("Website");
        website.setOnAction(event -> {
            openWebpage("https://stream-pi.com");
        });

        Hyperlink twitter = new Hyperlink("Twitter");
        twitter.setOnAction(event -> {
            openWebpage("https://twitter.com/Stream_Pi");
        });

        links.setSpacing(30);
        links.setAlignment(Pos.CENTER);
        links.getChildren().addAll(github, discord, website, twitter);

        Hyperlink donateButton = new Hyperlink("DONATE");
        donateButton.setOnAction(event -> {
            openWebpage("https://www.patreon.com/streampi");
        });
        donateButton.getStyleClass().add("settings_about_donate_hyperlink");


        ServerInfo serverInfo = ServerInfo.getInstance();

        Label versionText = new Label(serverInfo.getVersion().getText() + " - "+ serverInfo.getPlatformType().getUIName() + " - "+ serverInfo.getReleaseStatus().getUIName());
        Label commStandardLabel = new Label("Comm Standard "+serverInfo.getCommStandardVersion().getText());
        Label minThemeAPILabel = new Label("Min ThemeAPI "+serverInfo.getMinThemeSupportVersion().getText());
        Label minActionAPILabel = new Label("Min ActionAPI "+serverInfo.getMinPluginSupportVersion().getText());


        Label currentActionAPILabel = new Label("ActionAPI "+ ActionAPI.API_VERSION.getText());

        setSpacing(3);

        getChildren().addAll(appIconImageView, licenseLabel, licenseTextArea, links, donateButton, versionText, commStandardLabel, minThemeAPILabel, minActionAPILabel, currentActionAPILabel);
    }

    public void openWebpage(String url) {
        hostServices.showDocument(url);
    }
}
