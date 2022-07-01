/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.stream_pi.server.window.firsttimeuse;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.combobox.IPChooserComboBox;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.rootchecker.RootChecker;
import com.stream_pi.util.uihelper.HBoxInputBox;

import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class FinalConfigPane extends VBox
{
    private TextField serverNameTextField;
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

        Label label = new Label(I18N.getString("window.firsttimeuse.FinalConfigPane.subHeading"));
        label.setWrapText(true);
        label.getStyleClass().add("first_time_use_pane_final_config_label");

        serverNameTextField = new TextField();
        serverPortTextField = new TextField(Config.getDefaultPort()+"");
        ipChooserComboBox = new IPChooserComboBox();
        ipChooserComboBox.configureOptions();

        HBoxInputBox serverNickNameInputBox = new HBoxInputBox(I18N.getString("serverName"), serverNameTextField, 200);
        HBoxInputBox serverPortInputBox = new HBoxInputBox(I18N.getString("serverPort"), serverPortTextField);
        HBoxWithSpaceBetween ipChooserHBox = new HBoxWithSpaceBetween(I18N.getString("serverIPBinding"), ipChooserComboBox);

        Label securityWarningLabel = new Label(I18N.getString("window.firsttimeuse.FinalConfigPane.securityWarning"));
        securityWarningLabel.setWrapText(true);
        securityWarningLabel.prefWidthProperty().bind(widthProperty());
        securityWarningLabel.getStyleClass().add("first_time_use_pane_final_config_security_warning_label");

        getChildren().addAll(label, serverNickNameInputBox, serverPortInputBox, ipChooserHBox, securityWarningLabel);

        setSpacing(10.0);

        setVisible(false);

        try
        {
            serverNameTextField.setText(InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException e)
        {
            Logger.getLogger(getClass().getName()).warning("Hostname lookup failed! Not setting any placeholder for serverNameTextField.");
        }
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

        String serverNameStr = serverNameTextField.getText();
        String serverPortStr = serverPortTextField.getText();

        if(serverNameStr.isBlank())
        {
            errors.append("* ").append(I18N.getString("serverNameCannotBeBlank")).append("\n");
        }

        int serverPort=-1;
        try {
            serverPort = Integer.parseInt(serverPortStr);

            if (serverPort < 1024 && !RootChecker.isRoot(ServerInfo.getInstance().getPlatform()))
            {
                errors.append("* ").append(I18N.getString("serverPortMustBeGreaterThan1024")).append("\n");
            }
            else if(serverPort > 65535)
            {
                errors.append("* ").append(I18N.getString("serverPortMustBeLesserThan65535")).append("\n");
            }
        }
        catch (NumberFormatException e)
        {
            errors.append("* ").append(I18N.getString("serverPortMustBeInteger")).append("\n");
        }

        if(errors.toString().isEmpty())
        {
            try
            {
                Config.getInstance().setServerName(serverNameStr);
                Config.getInstance().setPort(serverPort);
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
