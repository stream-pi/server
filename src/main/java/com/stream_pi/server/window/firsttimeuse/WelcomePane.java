package com.stream_pi.server.window.firsttimeuse;

import com.stream_pi.server.Main;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class WelcomePane extends VBox
{
    public WelcomePane()
    {
        getStyleClass().add("first_time_use_pane_welcome");

        Image appIcon = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/256x256.png")));
        ImageView appIconImageView = new ImageView(appIcon);
        VBox.setMargin(appIconImageView, new Insets(10, 0, 10, 0));
        appIconImageView.setFitHeight(128);
        appIconImageView.setFitWidth(128);

        Label welcomeLabel = new Label("Welcome!");
        welcomeLabel.getStyleClass().add("first_time_use_welcome_pane_welcome_label");

        Label nextToContinue = new Label("Please click \"Next\" to start the Setup process");
        nextToContinue.getStyleClass().add("first_time_use_welcome_pane_next_to_continue_label");


        setAlignment(Pos.CENTER);
        setSpacing(5.0);
        getChildren().addAll(appIconImageView, welcomeLabel, nextToContinue);
    
        setVisible(false);
    }
}
