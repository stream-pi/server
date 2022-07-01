package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.connection.ClientConnections;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.config.record.GeneralSettings;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertButton;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.checkforupdates.CheckForUpdates;
import com.stream_pi.util.checkforupdates.UpdateHyperlinkOnClick;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.platform.PlatformType;
import javafx.application.HostServices;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class GeneralSettingsPresenter implements GeneralSettingsViewListener
{
    final GeneralSettingsModel model;
    final GeneralSettingsView view;

    private final ExceptionAndAlertHandler exceptionAndAlertHandler;

    private ServerListener serverListener;

    private HostServices hostServices;

    public GeneralSettingsPresenter(ExceptionAndAlertHandler exceptionAndAlertHandler, ServerListener serverListener, HostServices hostServices)
    {
        this.exceptionAndAlertHandler=exceptionAndAlertHandler;
        this.serverListener = serverListener;
        this.hostServices = hostServices;

        model = new GeneralSettingsModel(serverListener);
        view = new GeneralSettingsView(this);
    }

    public GeneralSettingsView getView()
    {
        return view;
    }

    public void load()
    {
        model.loadSettings();
        view.updateFields(model.getCurrentSettings());
    }

    @Override
    public ExceptionAndAlertHandler getExceptionAndAlertHandler() {
        return exceptionAndAlertHandler;
    }

    @Override
    public void onCheckForUpdatesButtonClicked(Button checkForUpdatesButton)
    {
        new CheckForUpdates(checkForUpdatesButton,
                PlatformType.SERVER, ServerInfo.getInstance().getVersion(), new UpdateHyperlinkOnClick() {
            @Override
            public void handle(ActionEvent actionEvent) {
                hostServices.showDocument(getURL());
            }
        });
    }

    @Override
    public void onRestartButtonClicked()
    {
        if (ClientConnections.getInstance().getConnections().size() > 0)
        {
            showRestartPrompt(I18N.getString("window.settings.GeneralSettings.restartPromptWarning"));
        }
        else
        {
            serverListener.restart();
        }
    }

    private void showRestartPrompt(String promptText)
    {
        StreamPiAlert restartPrompt = new StreamPiAlert(promptText,
                StreamPiAlertType.WARNING, StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        restartPrompt.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if(s.equals(StreamPiAlertButton.YES))
                {
                    serverListener.restart();
                }
            }
        });

        restartPrompt.show();
    }

    @Override
    public void onFactoryResetButtonClicked()
    {
        StreamPiAlert confirmation = new StreamPiAlert( I18N.getString("window.settings.GeneralSettings.resetAreYouSure"),
                StreamPiAlertType.WARNING, StreamPiAlertButton.YES, StreamPiAlertButton.NO
        );

        confirmation.setOnClicked(new StreamPiAlertListener() {
            @Override
            public void onClick(StreamPiAlertButton s) {
                if (s.equals(StreamPiAlertButton.YES)) {
                    serverListener.factoryReset();
                }
            }
        });

        confirmation.show();
    }

    @Override
    public void onSaveButtonClicked(Button saveButton, GeneralSettings newSettingsRecord)
    {
        new Thread(new Task<Void>() {
            @Override
            protected Void call()
            {
                try
                {
                    settingsBeingSaved.set(true);

                    boolean shouldServerBeReloaded = model.shouldServerBeReloaded(newSettingsRecord);
                    boolean shouldServerDashboardBeReloaded = model.shouldServerDashboardBeReloaded(newSettingsRecord);

                    model.saveSettings(newSettingsRecord);
                    view.updateFields(model.getCurrentSettings());

                    if(shouldServerBeReloaded)
                    {
                        showRestartPrompt(I18N.getString("window.settings.GeneralSettings.needsToBeRestartedToApplySettings") + "\n" + I18N.getString("window.settings.GeneralSettings.restartPromptWarning"));
                    }

                    if(shouldServerDashboardBeReloaded)
                    {
                        serverListener.clearTemp();
                    }
                }
                catch (SevereException e)
                {
                    exceptionAndAlertHandler.handleSevereException(e);
                }
                finally
                {
                    settingsBeingSaved.set(false);
                }
                return null;
            }
        }).start();
    }

    private final SimpleBooleanProperty settingsBeingSaved = new SimpleBooleanProperty(false);
    @Override
    public ObservableBooleanValue isSettingsBeingSaved() {
        return settingsBeingSaved;
    }
}
