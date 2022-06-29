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

package com.stream_pi.server.controller;

import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.action_api.externalplugin.ToggleAction;
import com.stream_pi.action_api.externalplugin.inputevent.StreamPiInputEvent;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.window.dashboard.ClientAndProfileSelectorPane;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.settings.SettingsBase;
import com.stream_pi.util.exception.SevereException;
import javafx.event.EventType;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.stage.Stage;

import java.net.SocketAddress;

public interface ServerListener
{
    void onInputEventInAction(Client client, String profileID, String actionID, StreamPiInputEvent streamPiInputEvent);

    void onSetToggleStatus(Client client, String profileID, String actionID, boolean toggleStatus);

    void clearTemp();

    void init();

    void restart();

    Stage getStage();

    DashboardBase getDashboardBase();
    SettingsBase getSettingsBase();

    void initLogger() throws SevereException;

    void factoryReset();

    void initSoundOnActionClicked();

    void showUserChooseIPAndPortDialog();

    void fullExit();
}
