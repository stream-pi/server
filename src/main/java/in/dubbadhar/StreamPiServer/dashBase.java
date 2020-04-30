package in.dubbadhar.StreamPiServer;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

public class dashBase extends StackPane {

    BorderPane alertPane;

    private final Image appIcon = new Image(getClass().getResource("app_icon.png").toExternalForm());

    public dashBase()
    {
        //First add stylesheets and fonts
        Font.loadFont(getClass().getResource("Roboto.ttf").toExternalForm().replace("%20",""), 13);
        getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        //Alert Pane
        alertPane = new BorderPane();
        alertPane.setCache(true);
        alertPane.setCacheHint(CacheHint.SPEED);
        alertTitle = new Label("Title");
        alertMessage = new Label("Message");
        HBox buttonBar = new HBox();
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("flat-btn");
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.getChildren().add(okButton);
        okButton.setOnAction(event->hideAlert());
        VBox alertVBox = new VBox(alertTitle, alertMessage, buttonBar);
        alertVBox.setSpacing(15);
        alertVBox.setPadding(new Insets(15));
        alertVBox.setMaxSize(300,100);
        GridPane.setHgrow(alertVBox,Priority.NEVER);
        GridPane.setVgrow(alertVBox,Priority.NEVER);
        alertPane.getStyleClass().add("alert-pane");
        alertPane.setCenter(alertVBox);

        //Not Connected (Listening For Connections Pane)
        VBox listeningForClientPane = new VBox();
        listeningForClientPane.getStyleClass().add("pane");
        listeningForClientPane.setSpacing(10);
        listeningForClientPane.setAlignment(Pos.CENTER);
        ImageView appIconImgView = new ImageView(appIcon);
        Label listeningLabel = new Label("Listening for StreamPi");
        listeningLabel.getStyleClass().add("h2");
        Label listeningSubHeadingLabel = new Label("Server running at X.X.X.X, Port XXXX");
        listeningSubHeadingLabel.getStyleClass().add("h3");
        listeningForClientPane.getChildren().addAll(appIconImgView, listeningLabel, listeningSubHeadingLabel);

        //Settings Pane


        //Add all the panes
        getChildren().addAll(alertPane, listeningForClientPane);
        alertPane.setOpacity(0);

        setPrefSize(1280,720);
    }



    private final Label alertTitle;
    private final Label alertMessage;
    public void showAlert(String title, String message)
    {
        alertTitle.setText(title);
        alertMessage.setText(message);

        FadeIn f = new FadeIn(alertPane);
        f.setSpeed(3);
        alertPane.setOpacity(0);
        alertPane.toFront();
        f.play();
    }

    public void hideAlert()
    {
        FadeOut f = new FadeOut(alertPane);
        f.setSpeed(3);
        f.play();
        f.setOnFinished(event->alertPane.toBack());
    }
}
