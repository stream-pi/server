package com.stream_pi.server.window.firsttimeuse;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.HBoxInputBox;

import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class FinalConfigPane extends VBox
{
    private TextField serverNicknameTextField;
    private TextField serverPortTextField;
    private IPChooserComboBox ipChooserComboBox;
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

        Label label = new Label(I18N.getString("window.firsttimeuse.FinalConfigPane.header"));
        label.setWrapText(true);
        label.getStyleClass().add("first_time_use_pane_final_config_label");

        serverNicknameTextField = new TextField();
        serverPortTextField = new TextField(Config.getDefaultPort()+"");
        ipChooserComboBox = new IPChooserComboBox(exceptionAndAlertHandler);
        ipChooserComboBox.configureOptions();

        HBoxInputBox serverNickNameInputBox = new HBoxInputBox(I18N.getString("window.firsttimeuse.FinalConfigPane.serverName"), serverNicknameTextField, 200);
        HBoxInputBox serverPortInputBox = new HBoxInputBox(I18N.getString("window.firsttimeuse.FinalConfigPane.serverPort"), serverPortTextField);
        HBoxWithSpaceBetween ipChooserHBox = new HBoxWithSpaceBetween(I18N.getString("window.firsttimeuse.FinalConfigPane.serverIPBinding"), ipChooserComboBox);

        Label securityWarningLabel = new Label(I18N.getString("window.firsttimeuse.FinalConfigPane.securityWarning"));
        securityWarningLabel.setWrapText(true);
        securityWarningLabel.getStyleClass().add("first_time_use_pane_final_config_security_warning_label");



        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(label, serverNickNameInputBox, serverPortInputBox, ipChooserHBox, securityWarningLabel);

        setSpacing(10.0);

        setVisible(false);
    }

    public void makeChangesToNextButton()
    {
        nextButton.setText(I18N.getString("window.firsttimeuse.FinalConfigPane.confirm"));
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

        if(serverNameStr.isBlank())
        {
            errors.append("* ").append(I18N.getString("window.firsttimeuse.FinalConfigPane.serverNameCannotBeBlank")).append("\n");
        }

        int serverPort=-1;
        try {
            serverPort = Integer.parseInt(serverPortStr);

            if (serverPort < 1024)
            {
                errors.append("* ").append(I18N.getString("window.firsttimeuse.FinalConfigPane.serverPortMustBeGreaterThan1024")).append("\n");
            }
            else if(serverPort > 65535)
            {
                errors.append("* ").append(I18N.getString("window.firsttimeuse.FinalConfigPane.serverPortMustBeLesserThan65535")).append("\n");
            }
        }
        catch (NumberFormatException e)
        {
            errors.append("* ").append(I18N.getString("window.firsttimeuse.FinalConfigPane.serverPortMustBeInteger")).append("\n");
        }

        if(errors.toString().isEmpty())
        {
            try
            {
                Config.getInstance().setServerName(serverNameStr);
                Config.getInstance().setServerPort(serverPort);
                Config.getInstance().setIP(ipChooserComboBox.getSelectedIP());
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
            new StreamPiAlert(I18N.getString("validationError", errors), StreamPiAlertType.ERROR).show();
        }
    }
}
