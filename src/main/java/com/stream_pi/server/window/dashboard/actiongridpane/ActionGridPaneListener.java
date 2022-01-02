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

package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;

public interface ActionGridPaneListener
{
    void addActionToCurrentClientProfile(Action newAction);

    ActionBox getActionBoxByProfileAndID(String profileID, String actionID);

    void renderFolder(FolderAction action);
    void renderAction(Action action) throws MinorException;

    String getCurrentParent();

    ClientProfile getCurrentProfile();

    void setCurrentParent(String currentParent);

    ClientConnection getClientConnection();

    ActionBox getActionBox(int col, int row);

    ExternalPlugin createNewActionFromExternalPlugin(String module) throws CloneNotSupportedException;
    Action createNewOtherAction(ActionType actionType) throws Exception;
    void clearActionBox(int col, int row, int colSpan, int rowSpan);
}
