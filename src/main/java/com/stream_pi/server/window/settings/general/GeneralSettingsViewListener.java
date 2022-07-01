package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.config.record.GeneralSettings;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.control.Button;

public interface GeneralSettingsViewListener
{
    ExceptionAndAlertHandler getExceptionAndAlertHandler();
    void onCheckForUpdatesButtonClicked(Button checkForUpdatesButton);
    void onFactoryResetButtonClicked();

    void onRestartButtonClicked();

    void onSaveButtonClicked(Button saveButton, GeneralSettings newSettingsRecord);

    ObservableBooleanValue isSettingsBeingSaved();

}
