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
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.action.Location;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.client.Client;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiondetailspane.ActionDetailsPaneListener;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.logging.Logger;

public class ActionGridPane extends ScrollPane implements ActionGridPaneListener
{

    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private ActionDetailsPaneListener actionDetailsPaneListener;

    public ActionGridPane(ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        logger = Logger.getLogger(ActionGridPane.class.getName());

        actionBoxHashMap = new HashMap<>();

        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        getStyleClass().add("action_grid_pane_parent");

        actionsGridPane = new GridPane();
        actionsGridPane.setPadding(new Insets(5.0));
        actionsGridPane.getStyleClass().add("action_grid_pane");

        actionsGridPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        setContent(actionsGridPane);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    private HashMap<String, ActionBox> actionBoxHashMap;

    public ActionBox getActionBoxByProfileAndID(String profileID, String actionID)
    {
        // Returns null when there is no such action available

        if(getClientProfile() == null)
        {
            return null;
        }

        if(!getClientProfile().getID().equals(profileID))
        {
            return null;
        }

        return actionBoxHashMap.getOrDefault(actionID, null);
    }

    @Override
    public ActionBox getActionBox(int col, int row)
    {
        return actionBoxes[col][row];
    }

    @Override
    public void clearActionBox(int col, int row, int colSpan, int rowSpan)
    {
        showNonUsedBoxes(col, row, colSpan, rowSpan);
        actionBoxes[col][row].clear();
    }


    public void setActionDetailsPaneListener(ActionDetailsPaneListener actionDetailsPaneListener) {
        this.actionDetailsPaneListener = actionDetailsPaneListener;
    }

    private String currentParent = null;

    @Override
    public ExternalPlugin createNewActionFromExternalPlugin(String uniqueID) throws CloneNotSupportedException
    {
        ExternalPlugin newAction = ExternalPlugins.getInstance().getPluginByUniqueID(uniqueID).clone();

        if(newAction.getActionType() == ActionType.TOGGLE)
        {
            newAction.setCurrentIconState("false__false");
        }

        newAction.setIDRandom();


        newAction.setShowDisplayText(true);
        newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);

        newAction.setBgColourHex("");
        newAction.setDisplayTextFontColourHex("");

        return newAction;
    }

    @Override
    public Action createNewOtherAction(ActionType actionType) throws Exception
    {
        Action newAction;

        if(actionType == ActionType.FOLDER)
        {
            newAction = new FolderAction();
        }
        else if(actionType == ActionType.COMBINE)
        {
            newAction = new CombineAction();
        }
        else
        {
            throw new IllegalArgumentException(I18N.getString("window.dashboard.actiongridpane.ActionGridPane.externalPluginsAreNotSupportedHere"));
        }

        newAction.setIDRandom();


        newAction.setShowDisplayText(true);
        newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);

        newAction.setBgColourHex("");
        newAction.setDisplayTextFontColourHex("");

        return newAction;
    }

    @Override
    public void setCurrentParent(String currentParent) {
        this.currentParent = currentParent;
    }

    @Override
    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    private ClientConnection clientConnection;


    public void setClientConnection(ClientConnection clientConnection)
    {
        this.clientConnection = clientConnection;
    }

    public Client getClient() {
        return getClientConnection().getClient();
    }

    private int rows, cols;

    private GridPane actionsGridPane;

    private ClientProfile clientProfile;

    public void setClientProfile(ClientProfile clientProfile)
    {
        this.clientProfile = clientProfile;

        setCurrentParent("root");
        setRows(clientProfile.getRows());
        setCols(clientProfile.getCols());
    }


    @Override
    public String getCurrentParent() {
        return currentParent;
    }

    @Override
    public ClientProfile getCurrentProfile() {
        return clientProfile;
    }

    public StackPane getFolderBackButton()
    {
        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().add("action_box");
        stackPane.getStyleClass().add("action_box_valid");


        stackPane.setPrefSize(
                actionSize,
                actionSize
        );

        FontIcon fontIcon = new FontIcon("fas-chevron-left");
        fontIcon.getStyleClass().add("folder_action_back_button_icon");
        fontIcon.setIconSize((int) (actionSize * 0.8));

        stackPane.setAlignment(Pos.CENTER);
        stackPane.getChildren().add(fontIcon);

        stackPane.setOnMouseClicked(e->returnToPreviousParent());

        return stackPane;
    }

    private ActionBox[][] actionBoxes;
    private boolean isFreshRender = true;
    private Node folderBackButton = null;

    private double actionSize;
    private double actionGap;

    private enum RenderSelector
    {
        ROW, COLUMN
    }

    public void renderGrid() throws SevereException
    {
        if (Config.getInstance().getActionGridUseSameActionSizeAsProfile())
        {
            actionSize = getClientProfile().getActionSize();
            actionGap = getClientProfile().getActionGap();
        }
        else
        {
            actionSize = Config.getInstance().getActionGridActionSize();
            actionGap = Config.getInstance().getActionGridActionGap();
        }


        if(Config.getInstance().getActionGridUseSameActionGapAsProfile())
        {
            actionsGridPane.setHgap(clientProfile.getActionGap());
            actionsGridPane.setVgap(clientProfile.getActionGap());
        }
        else
        {
            actionsGridPane.setHgap(Config.getInstance().getActionGridActionGap());
            actionsGridPane.setVgap(Config.getInstance().getActionGridActionGap());
        }

        RenderSelector renderSelector = null;

        if(isFreshRender)
        {
            clear();
            actionBoxes = new ActionBox[cols][rows];

            if (rows>cols)
            {
                renderSelector = RenderSelector.COLUMN;
            }
            else
            {
                renderSelector = RenderSelector.ROW;
            }
        }

        boolean isFolder = false;

        if(getCurrentParent().equals("root"))
        {
            if(folderBackButton != null)
            {
                actionsGridPane.getChildren().remove(folderBackButton);
                folderBackButton = null;

                actionBoxes[0][0] = generateBlankActionBox(0,0);
                actionsGridPane.add(actionBoxes[0][0], 0, 0);
            }
        }
        else
        {
            isFolder = true;

            if(folderBackButton != null)
            {
                actionsGridPane.getChildren().remove(folderBackButton);
                folderBackButton = null;
            }
            else
            {
                actionsGridPane.getChildren().remove(actionBoxes[0][0]);
            }

            folderBackButton = getFolderBackButton();
            actionsGridPane.add(folderBackButton, 0,0);
        }

        ActionBox[][] actionBoxesToBeAdded = null;

        if(isFreshRender)
        {
            actionBoxesToBeAdded = new ActionBox[(renderSelector == RenderSelector.ROW ? rows : cols)][(renderSelector != RenderSelector.ROW ? rows : cols)];
        }

        for(int row = 0; row<rows; row++)
        {
            for(int col = 0; col<cols; col++)
            {
                if(row == 0 && col == 0 && isFolder)
                    continue;

                if(isFreshRender)
                {
                    actionBoxes[col][row] = generateBlankActionBox(col, row);

                    actionBoxesToBeAdded[renderSelector == RenderSelector.ROW ? row : col][renderSelector != RenderSelector.ROW ? row : col] = actionBoxes[col][row];
                }
                else
                {
                    if(actionBoxes[col][row].getAction() != null)
                    {
                        actionBoxes[col][row].clear();
                    }
                }

                actionBoxes[col][row].setVisible(true);
            }
        }

        if (isFreshRender)
        {
            boolean inverse = getClient().getOrientation() == Orientation.VERTICAL;


            if (renderSelector == RenderSelector.ROW)
            {
                for(int i = 0; i<rows; i++)
                {
                    if (inverse)
                    {
                        actionsGridPane.addColumn(i, actionBoxesToBeAdded[i]);
                    }
                    else
                    {
                        actionsGridPane.addRow(i, actionBoxesToBeAdded[i]);
                    }
                }
            }
            else
            {
                for(int i = 0; i<cols; i++)
                {
                    if (inverse)
                    {
                        actionsGridPane.addRow(i, actionBoxesToBeAdded[i]);
                    }
                    else
                    {
                        actionsGridPane.addColumn(i, actionBoxesToBeAdded[i]);
                    }
                }
            }
        }

        isFreshRender = false;
    }

    public void setFreshRender(boolean freshRender)
    {
        isFreshRender = freshRender;
    }

    public ActionBox generateBlankActionBox(int col, int row) throws SevereException
    {
        return new ActionBox(actionSize, actionDetailsPaneListener, this,
                col, row,
                Config.getInstance().getActionGridActionDisplayTextFontSize(),
                clientProfile.getActionDefaultDisplayTextFontSize(),
                Config.getInstance().getActionGridUseSameActionDisplayTextFontSizeAsProfile(), exceptionAndAlertHandler);
    }

    public void renderActions()
    {
        StringBuilder errors = new StringBuilder();
        for(String action1x : getClientProfile().getActionsKeySet())
        {
            Action eachAction = getClientProfile().getActionByID(action1x);
            logger.info("action ID : "+eachAction.getID()+
                    "\nInvalid : "+eachAction.isInvalid());

            try
            {
                renderAction(eachAction);
            }
            catch (MinorException e)
            {
                errors.append("*").append(e.getMessage()).append("\n");
            }
        }

        if(!errors.toString().isEmpty())
        {
            exceptionAndAlertHandler.handleMinorException(new MinorException(I18N.getString("window.dashboard.actiongridpane.ActionGridPane.errorWhileRenderingFollowingActions", errors)));
        }
    }

    public void clear()
    {
        actionBoxHashMap.clear();
        actionsGridPane.getChildren().clear();
    }

    private Logger logger;

    @Override
    public void renderAction(Action action) throws MinorException
    {
        if(!action.getParent().equals(currentParent))
        {
            logger.info("Skipping action "+action.getID()+", not current parent!");
            return;
        }

        if(action.getLocation().getRow()==-1)
        {
            logger.info("action has -1 rowIndex. Probably Combine action. Skipping ...");
            return;
        }

        if(action.getLocation().getRow() >= rows || action.getLocation().getCol() >= cols)
        {
            throw new MinorException(I18N.getString("window.dashboard.actiongridpane.ActionGridPane.actionOutsideBounds", action.getDisplayText(), action.getID()));
        }


        Location location = action.getLocation();

        ActionBox actionBox = actionBoxes[location.getCol()][location.getRow()];

        if(actionBox.getAction() != null)
        {
            if((GridPane.getColumnSpan(actionBox) != action.getLocation().getColSpan()) || (GridPane.getRowSpan(actionBox) != action.getLocation().getRowSpan()))
            {
                showNonUsedBoxes(action.getLocation().getCol(), action.getLocation().getRow(), GridPane.getColumnSpan(actionBox),  GridPane.getRowSpan(actionBox));
            }
            actionBox.clear();
        }

        actionBox.setAction(action);

        actionBoxHashMap.put(action.getID(), actionBox);


        GridPane.setRowSpan(actionBox, location.getRowSpan());
        GridPane.setColumnSpan(actionBox, location.getColSpan());

        hideOverlappingBoxes(location.getCol(), location.getRow(), location.getColSpan(), location.getRowSpan());


        double actionWidth = (actionSize * location.getColSpan()) + (actionGap * (location.getColSpan()-1));
        double actionHeight = (actionSize * location.getRowSpan()) + (actionGap * (location.getRowSpan()-1));

        actionBox.configureSize(actionWidth, actionHeight);
        actionBox.init();
        actionBox.setVisible(true);
    }

    private void showNonUsedBoxes(int col, int row, int colSpan, int rowSpan)
    {
        showHideOverlappingBoxes(col, row, colSpan, rowSpan, true);
    }

    private void hideOverlappingBoxes(int col, int row, int colSpan, int rowSpan)
    {
        showHideOverlappingBoxes(col, row, colSpan, rowSpan, false);
    }

    private void showHideOverlappingBoxes(int col, int row, int colSpan, int rowSpan, boolean visibility)
    {
        for (int i = row; i< (row+rowSpan); i++)
        {
            for (int j = col; j < (col+colSpan);j++)
            {
                if (! (i==row && j==col))
                {
                    if (visibility)
                    {
                        actionBoxes[j][i].setVisible(true);
                        GridPane.setColumnSpan(actionBoxes[j][i], 1);
                        GridPane.setRowSpan(actionBoxes[j][i], 1);
                    }
                    else
                    {
                        actionBoxes[j][i].setVisible(false);
                    }
                }
            }
        }
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public int getRows()
    {
        return rows;
    }

    public int getCols()
    {
        return cols;
    }


    @Override
    public void addActionToCurrentClientProfile(Action newAction)
    {
        try {
            getClientProfile().addAction(newAction);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private String previousParent;

    public void setPreviousParent(String previousParent) {
        this.previousParent = previousParent;
    }

    public String getPreviousParent() {
        return previousParent;
    }

    @Override
    public void renderFolder(FolderAction action)
    {
        setCurrentParent(action.getID());
        setPreviousParent(action.getParent());
        try
        {
            renderGrid();
            renderActions();
        }
        catch (SevereException e)
        {
            e.printStackTrace();
        }
    }

    public void returnToPreviousParent()
    {
        setCurrentParent(getPreviousParent());

        if(!getPreviousParent().equals("root"))
        {
            setPreviousParent(getClientProfile().getActionByID(
                    getPreviousParent()
            ).getParent());
        }

        try {
            renderGrid();
            renderActions();
        } catch (SevereException e) {
            e.printStackTrace();
        }
    }
}
