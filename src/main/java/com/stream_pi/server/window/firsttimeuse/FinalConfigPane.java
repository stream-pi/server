package com.stream_pi.server.window.firsttimeuse;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FinalConfigPane extends VBox
{
    private TextField serverNicknameTextField;
    private TextField serverPortTextField;
    private Button nextButton;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private ServerListener serverListener;

    public FinalConfigPane(ExceptionAndAlertHandler exceptionAndAlertHandler, ServerListener serverListener,
                           Button nextButton)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.serverListener = serverListener;
        this.nextButton = nextButton;

        getStyleClass().add("first_time_use_pane_final_config");

        Label label = new Label("You're almost finished with setting up your Stream-Pi Server. Now all you need to do is to choose a name for your Stream-Pi Server, and, choose which port you want the Stream-Pi Server to operate on.\n" +
                "We recommend that you give your Stream-Pi Server a short name & that you use the Default port that has already been typed in for you. You can change it from Settings in case it conflicts with other services or applications.");
        label.setWrapText(true);
        label.getStyleClass().add("first_time_use_pane_final_config_label");

        serverNicknameTextField = new TextField();
        serverPortTextField = new TextField(Config.getDefaultPort()+"");

        HBoxInputBox serverNickNameInputBox = new HBoxInputBox("Server Nickname", serverNicknameTextField, 200);
        HBoxInputBox serverPortInputBox = new HBoxInputBox("Server Port", serverPortTextField);

        getChildren().addAll(label, serverNickNameInputBox, serverPortInputBox);

        setSpacing(10.0);

        setVisible(false);
    }

    public void makeChangesToNextButton()
    {
        nextButton.setText("Confirm");
        nextButton.setOnAction(actionEvent -> new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                onConfirmButtonClicked();
                return null;
            }
        }).start());
    }

    private void onConfirmButtonClicked()
    {
        Platform.runLater(()->nextButton.setDisable(true));

        StringBuilder errors = new StringBuilder();

        String serverNameStr = serverNicknameTextField.getText();
        String serverPortStr = serverPortTextField.getText();

        if(serverNameStr.isBlank() || serverNameStr.isEmpty())
        {
            errors.append("* Server Name cannot be blank.\n");
        }

        int serverPort=-1;
        try {
            serverPort = Integer.parseInt(serverPortStr);

            if (serverPort < 1024)
                errors.append("* Server Port must be more than 1024\n");
            else if(serverPort > 65535)
                errors.append("* Server Port must be lesser than 65535\n");
        }
        catch (NumberFormatException e)
        {
            errors.append("* Server Port must be integer.\n");
        }

        if(errors.toString().isEmpty())
        {
            try
            {
                Config.getInstance().setServerName(serverNameStr);
                Config.getInstance().setServerPort(serverPort);
                Config.getInstance().setFirstTimeUse(false);
                Config.getInstance().save();

                Platform.runLater(()->serverListener.restart());
            }
            catch(SevereException e)
            {
                exceptionAndAlertHandler.handleSevereException(e);
            }
        }
        else
        {
            Platform.runLater(()->nextButton.setDisable(false));
            new StreamPiAlert("Uh Oh", "Please rectify the following errors and try again:\n"+errors, StreamPiAlertType.ERROR).show();
        }
    }
}
