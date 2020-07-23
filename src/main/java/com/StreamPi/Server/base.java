package com.StreamPi.Server;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.HashMap;

public class base extends StackPane {

    HashMap<String,String> config;

    Label listeningSubHeadingLabel;

    private final Image appIcon = new Image(getClass().getResource("app_icon.png").toExternalForm());

    public void initNodes()
    {
        //First add stylesheets and fonts
        Font.loadFont(getClass().getResource("Roboto.ttf").toExternalForm().replace("%20",""), 13);
        getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        setStyle("bg-color: "+config.get("bg-color")+";font-color: "+config.get("font-color")+";");

        setPrefSize(1280,720);



    }
}
