/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed), Samuel Quiñones (SamuelQuinones)
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

import java.util.ArrayList;
import java.util.logging.Logger;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.externalplugin.GaugeAction;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;

import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;

public class OnSaveActionTask extends Task<Void> 
{

    private Logger logger;


    public OnSaveActionTask(ClientConnection connection, Action action, String delayBeforeRunningString, String displayNameText, String displayNameLabelSize, boolean isUseDefaultFontSizeForDisplayLabel, boolean isCombineChild,
                            boolean isShowDisplayText, boolean isDefaultDisplayTextColour, String displayTextFontColour, boolean isClearIcon,
                            boolean isHideDefaultIcon, boolean isHideToggleOffIcon, boolean isHideToggleOnIcon, DisplayTextAlignment displayTextAlignment, boolean isTransparentBackground, String backgroundColour,
                            CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, boolean sendIcon, ActionBox actionBox,
                            ArrayList<UIPropertyBox> actionClientProperties, ExceptionAndAlertHandler exceptionAndAlertHandler, Button saveButton, Button deleteButton, Button resetButton,
                            boolean runOnActionSavedFromServer, boolean runAsync, boolean isGaugeAnimated, ActionDetailsPaneListener actionDetailsPaneListener,
                            String rowSpanStr, String colSpanStr)
    {
        this.saveButton = saveButton;
        this.deleteButton = deleteButton;
        this.resetButton = resetButton;

        this.delayBeforeRunningString = delayBeforeRunningString;
        this.connection = connection;
        this.action = action;
        this.displayNameText = displayNameText;
        this.displayNameLabelSize = displayNameLabelSize;
        this.isUseDefaultFontSizeForDisplayLabel = isUseDefaultFontSizeForDisplayLabel;
        this.isCombineChild = isCombineChild;
        this.isShowDisplayText = isShowDisplayText;
        this.isDefaultDisplayTextColour = isDefaultDisplayTextColour;
        this.displayTextFontColour = displayTextFontColour;
        this.isClearIcon = isClearIcon;
        this.isHideDefaultIcon = isHideDefaultIcon;
        this.isHideToggleOffIcon = isHideToggleOffIcon;
        this.isHideToggleOnIcon = isHideToggleOnIcon;
        this.displayTextAlignment = displayTextAlignment;
        this.isTransparentBackground = isTransparentBackground;
        this.combineActionPropertiesPane = combineActionPropertiesPane;
        this.clientProfile = clientProfile;
        this.sendIcon = sendIcon;
        this.actionBox = actionBox;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.backgroundColour = backgroundColour;
        this.actionClientProperties = actionClientProperties;
        this.runOnActionSavedFromServer = runOnActionSavedFromServer;
        this.isGaugeAnimated = isGaugeAnimated;
        this.actionDetailsPaneListener = actionDetailsPaneListener;
        this.rowSpanStr = rowSpanStr;
        this.colSpanStr = colSpanStr;

        logger = Logger.getLogger(getClass().getName());


        if(runAsync)
            new Thread(this).start();
        else
            runTask();
    }

    private boolean runOnActionSavedFromServer;

    private ActionDetailsPaneListener actionDetailsPaneListener;

    private Button saveButton;
    private Button deleteButton;
    private Button resetButton;
    private String delayBeforeRunningString;
    private boolean isShowDisplayText;
    private boolean isCombineChild;
    private String displayNameText;
    private String displayNameLabelSize;
    private boolean isUseDefaultFontSizeForDisplayLabel;
    private boolean isDefaultDisplayTextColour;
    private ArrayList<UIPropertyBox> actionClientProperties;
    private String displayTextFontColour;
    private boolean isClearIcon;
    private boolean isHideDefaultIcon;
    private boolean isHideToggleOffIcon;
    private boolean isHideToggleOnIcon;
    private DisplayTextAlignment displayTextAlignment;
    private boolean isTransparentBackground;
    private String backgroundColour;
    private CombineActionPropertiesPane combineActionPropertiesPane;
    private ClientProfile clientProfile;
    private boolean sendIcon;
    private ActionBox actionBox;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private Action action;
    private ClientConnection connection;
    private boolean isGaugeAnimated;
    private String rowSpanStr;
    private String colSpanStr;

    private void setSaveDeleteResetButtonState(boolean state)
    {
        Platform.runLater(()->{
            resetButton.setDisable(state);
            saveButton.setDisable(state);
            deleteButton.setDisable(state);
        });
    }

    
    private void runTask()
    {
        action.setDisplayText(displayNameText);

        if(isUseDefaultFontSizeForDisplayLabel)
        {
            action.setDisplayTextFontSize(-1);
        }
        else
        {
            action.setDisplayTextFontSize(Double.parseDouble(displayNameLabelSize));
        }

        if(!isCombineChild)
        {
            setSaveDeleteResetButtonState(true);

            action.setShowDisplayText(isShowDisplayText);

            if(isDefaultDisplayTextColour)
                action.setDisplayTextFontColourHex("");
            else
            {
                action.setDisplayTextFontColourHex(displayTextFontColour);
            }


            if(isClearIcon)
            {
                action.getIcons().clear();
                action.setCurrentIconState("");
            }


            if(action.getActionType() == ActionType.NORMAL ||
            action.getActionType() == ActionType.FOLDER ||
            action.getActionType() == ActionType.COMBINE ||
            action.getActionType() == ActionType.GAUGE)
            {
                if(isHideDefaultIcon)
                {
                    action.setCurrentIconState("");
                }
                else
                {
                    if(action.getIcon("default") != null)
                    {
                        action.setCurrentIconState("default");
                    }
                }
            }
            else if (action.getActionType() == ActionType.TOGGLE)
            {
                action.setCurrentIconState(isHideToggleOffIcon+"__"+isHideToggleOnIcon);
            }

            action.setDisplayTextAlignment(displayTextAlignment);


            if(isTransparentBackground)
                action.setBgColourHex("");
            else
            {
                action.setBgColourHex(backgroundColour);
            }

            if(action.getActionType() == ActionType.GAUGE)
            {
                action.setGaugeAnimated(isGaugeAnimated);
            }


            if(action.getLocation() != null)
            {
                action.getLocation().setRowSpan(Integer.parseInt(rowSpanStr));
                action.getLocation().setColSpan(Integer.parseInt(colSpanStr));
            }
        }


        if(action.getActionType() != ActionType.COMBINE)
        {
            if(action.getActionType() != ActionType.FOLDER)
                action.setDelayBeforeExecuting(Integer.parseInt(delayBeforeRunningString));

            //properties
            for (UIPropertyBox clientProperty : actionClientProperties) {
                action.getClientProperties().get().get(clientProperty.getIndex()).setRawValue(clientProperty.getControlNodeRawValue());
            }
        }


        try 
        {
            logger.info("Saving action ... "+action.isHasIcon()+"+"+sendIcon);

            connection.saveActionDetails(clientProfile.getID(), action);

            if(runOnActionSavedFromServer)
            {
                try
                {
                    if(action instanceof ExternalPlugin)
                    {
                        ((ExternalPlugin) action).onActionSavedFromServer();
                    }
                }
                catch (MinorException e)
                {
                    exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "onActionSavedFromServer()", action.getUniqueID(), e.getMessage()), e);
                }

                try
                {
                    if (action instanceof GaugeAction)
                    {
                        GaugeAction gaugeAction = (GaugeAction) action;
                        gaugeAction.cancelGaugeUpdaterFuture();
                        gaugeAction.onGaugeInit();
                    }
                }
                catch (MinorException e)
                {
                    exceptionAndAlertHandler.handleMinorException(I18N.getString("methodCallFailed", "onGaugeInit()", action.getUniqueID(), e.getMessage()), e);
                }
            }


            if(sendIcon)
            {
                sendAllIcons(clientProfile, action);
            }

            if(!isCombineChild)
            {
                Platform.runLater(()->{
                    try
                    {
                        actionDetailsPaneListener.renderAction(action);
                        actionBox.setSelected(true);
                    }
                    catch (MinorException e)
                    {
                        exceptionAndAlertHandler.handleMinorException(e);
                    }
                });

                setSaveDeleteResetButtonState(false);
            }

            clientProfile.removeActionByID(action.getID());
            clientProfile.addAction(action);
        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }

    private void sendAllIcons(ClientProfile clientProfile, Action action) throws SevereException
    {
        for(String state : action.getIcons().keySet())
        {
            connection.sendIcon(clientProfile.getID(), action.getID(), state, action.getIcon(state));
        }
    }

    @Override
    protected Void call() throws Exception
    {
        runTask();
        return null;
    }
}
