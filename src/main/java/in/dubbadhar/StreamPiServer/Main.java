package in.dubbadhar.StreamPiServer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public void start(Stage stage) {
        Scene s = new Scene(new dash());
        stage.setScene(s);
        stage.show();
        stage.setTitle("StreamPi Server - 0.7 - ALPHA");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
