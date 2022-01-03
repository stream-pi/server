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

package com.stream_pi.server.window.dashboard.actiondetailspane;

import java.util.logging.Logger;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;

import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class OnDeleteActionTask extends Task<Void>
{
    private Logger logger;

    public OnDeleteActionTask(ClientConnection connection, Action action, boolean isCombineChild,
                              CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, ActionBox actionBox,
                              ActionDetailsPane actionDetailsPane,
                              ExceptionAndAlertHandler exceptionAndAlertHandler, boolean runAsync)
    {
        this.connection = connection;
        this.action = action;
        this.isCombineChild = isCombineChild;
        this.combineActionPropertiesPane = combineActionPropertiesPane;
        this.clientProfile = clientProfile;
        this.actionBox = actionBox;
        this.actionDetailsPane = actionDetailsPane;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        logger = Logger.getLogger(getClass().getName());

        if(runAsync)
            new Thread(this).start();
        else
            runTask();
    }

    private ClientConnection connection;
    private Action action;
    private ActionBox actionBox;
    private ActionDetailsPaneListener actionDetailsPane;
    private boolean isCombineChild;
    private ClientProfile clientProfile;
    private CombineActionPropertiesPane combineActionPropertiesPane;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    private void runTask()
    {
        try
        {

            if(action instanceof ExternalPlugin)
            {
                try
                {
                    ((ExternalPlugin) action).onActionDeleted();
                }
                catch (MinorException e)
                {
                    e.setTitle("Unable to run onActionDeleted for "+action.getUniqueID());
                    exceptionAndAlertHandler.handleMinorException("Display Text: "+action.getDisplayText()+"\nDetailed message : \n\n"+e.getMessage(), e);
                }

            }


            connection.deleteAction(clientProfile.getID(), action.getID());
            clientProfile.removeActionByID(action.getID());

            if(isCombineChild)
            {
                combineActionPropertiesPane.getCombineAction().removeChild(action.getID());

                combineActionPropertiesPane.renderProps();

                try
                {
                    connection.saveActionDetails(clientProfile.getID(), combineActionPropertiesPane.getCombineAction());

                    actionDetailsPane.onActionClicked(
                            combineActionPropertiesPane.getCombineAction(),
                            actionBox
                    );
                }
                catch (MinorException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                Platform.runLater(()->{
                    actionDetailsPane.clearActionBox(action.getLocation().getCol(), action.getLocation().getRow(),
                            action.getLocation().getColSpan(), action.getLocation().getRowSpan());
                    actionDetailsPane.clear();
                });
            }
        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }


    @Override
    protected Void call() throws Exception
    {
        runTask();
        return null;
    }
    
}
