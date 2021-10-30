/*
 * Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macropad
 * Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */

package com.stream_pi.server.window.dashboard;

import java.util.logging.Logger;

import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionGridPane;
import com.stream_pi.server.window.dashboard.actiondetailspane.ActionDetailsPane;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.SevereException;
import javafx.application.HostServices;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;

public class DashboardBase extends SplitPane implements DashboardInterface
{

    private final SplitPane leftSplitPane;

    private Logger logger;

    public ClientProfile currentClientProfile;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    public DashboardBase(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        logger = Logger.getLogger(DashboardBase.class.getName());

        leftSplitPane = new SplitPane();
        leftSplitPane.getStyleClass().add("dashboard_left_split_pane");
        leftSplitPane.setOrientation(Orientation.VERTICAL);

        getStyleClass().add("dashboard_right_split_pane");

        getItems().add(leftSplitPane);

        setPluginsPane(new PluginsPane(hostServices));

        setClientDetailsPane(new ClientAndProfileSelectorPane(this));

        setActionGridPane(new ActionGridPane(exceptionAndAlertHandler));

        setActionDetailsPane(new ActionDetailsPane(exceptionAndAlertHandler, hostServices, getActionGridPane()));

        getActionGridPane().setActionDetailsPaneListener(getActionDetailsPane());
    }



    private PluginsPane pluginsPane;
    private void setPluginsPane(PluginsPane pluginsPane)
    {
        this.pluginsPane = pluginsPane;
        getItems().add(this.pluginsPane);
    }
    public PluginsPane getPluginsPane()
    {
        return pluginsPane;
    }

    private ClientAndProfileSelectorPane clientAndProfileSelectorPane;
    private void setClientDetailsPane(ClientAndProfileSelectorPane clientAndProfileSelectorPane)
    {
        this.clientAndProfileSelectorPane = clientAndProfileSelectorPane;
        leftSplitPane.getItems().add(this.clientAndProfileSelectorPane);
    }

    public ClientAndProfileSelectorPane getClientAndProfileSelectorPane()
    {
        return clientAndProfileSelectorPane;
    }

    private ActionGridPane actionGridPane;
    private void setActionGridPane(ActionGridPane actionGridPane)
    {
        this.actionGridPane = actionGridPane;
        leftSplitPane.getItems().add(this.actionGridPane);
    }
    public ActionGridPane getActionGridPane()
    {
        return actionGridPane;
    }

    private ActionDetailsPane actionDetailsPane;
    private void setActionDetailsPane(ActionDetailsPane actionDetailsPane)
    {
        this.actionDetailsPane = actionDetailsPane;
        leftSplitPane.getItems().add(this.actionDetailsPane);
    }
    public ActionDetailsPane getActionDetailsPane()
    {
        return actionDetailsPane;
    }

    public void newSelectedClientConnection(ClientConnection clientConnection)
    {
        if(clientConnection == null)
        {
            logger.info("Remove action grid");
        }
        else
        {
            getActionDetailsPane().setClientConnection(clientConnection);
            getActionGridPane().setClientConnection(clientConnection);
        }
    }

    public void newSelectedClientProfile(ClientProfile clientProfile)
    {
        this.currentClientProfile = clientProfile;

        getActionDetailsPane().setClientProfile(clientProfile);

        drawProfile(this.currentClientProfile);
    }

    public void reDrawProfile()
    {
        drawProfile(getActionGridPane().getCurrentProfile());
    }

    public void drawProfile(ClientProfile clientProfile)
    {
        logger.info("Drawing ...");

        getActionGridPane().setClientProfile(clientProfile);

        try
        {
            getActionGridPane().setFreshRender(true);
            getActionGridPane().renderGrid();
            getActionGridPane().renderActions();
        }
        catch (SevereException e)
        {
            exceptionAndAlertHandler.handleSevereException(e);
        }
    }

    public SplitPane getLeftSplitPane()
    {
        return leftSplitPane;
    }
}
