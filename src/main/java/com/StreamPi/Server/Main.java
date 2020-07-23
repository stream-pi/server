package com.StreamPi.Server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage stage) {
        dash d = new dash();
        Scene s = new Scene(d);
        stage.setScene(s);
        stage.show();
        stage.setTitle("StreamPi Server - "+ServerInfo.VERSION);
        stage.setOnCloseRequest(event -> d.closeServer());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
