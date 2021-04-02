package com.stream_pi.server.window.firsttimeuse;

import com.stream_pi.server.connection.ServerListener;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;
import com.stream_pi.util.uihelper.SpaceFiller;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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

        Label label = new Label("Thats it. Now just name your Stream-Pi Server, and the port where the server will run on!\n" +
            "You can leave the default value of the port as it is.");
        label.setWrapText(true);
        label.getStyleClass().add("first_time_use_pane_final_config_label");

        serverNicknameTextField = new TextField();
        serverPortTextField = new TextField("2004");

        HBoxInputBox serverNickNameInputBox = new HBoxInputBox("Server Nickname", serverNicknameTextField, 200);
        HBoxInputBox serverPortInputBox = new HBoxInputBox("Server Port", serverPortTextField);

        getChildren().addAll(label, serverNickNameInputBox, serverPortInputBox);

        setSpacing(10.0);

        setVisible(false);
    }

    public void makeChangesToNextButton()
    {
        nextButton.setText("Confirm");
        nextButton.setOnAction(event -> onConfirmButtonClicked());
    }

    private void onConfirmButtonClicked()
    {
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

                serverListener.othInit();
                serverListener.initLogger();

                ((Stage) getScene().getWindow()).close();
            }
            catch(SevereException e)
            {
                exceptionAndAlertHandler.handleSevereException(e);
            }
        }
        else
        {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setResizable(false);
            alert.getDialogPane().setPrefHeight(250);
            alert.setContentText("Please rectify the following errors and try again:\n"+errors.toString());
            alert.show();
        }
    }
}
