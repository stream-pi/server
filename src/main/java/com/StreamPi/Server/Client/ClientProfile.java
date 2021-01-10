package com.StreamPi.Server.Client;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.DisplayTextAlignment;
import com.StreamPi.ActionAPI.Action.Location;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.Server.Action.NormalActionPlugins;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionBox;
import com.StreamPi.Util.Exception.MinorException;
import javafx.geometry.Dimension2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ClientProfile implements Cloneable {

    private String name, ID;

    private int rows, cols, actionSize, actionGap;

    private final HashMap<String, Action> actions;

    public ClientProfile(String name, String ID, int rows, int cols, int actionSize, int actionGap)
    {
        this.actions = new HashMap<>();
        this.ID = ID;
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.actionGap = actionGap;
        this.actionSize = actionSize;
    }

    public ClientProfile(String name, int rows, int cols, int actionSize, int actionGap)
    {
        this(name, UUID.randomUUID().toString(), rows, cols, actionSize, actionGap);
    }

    public Action getActionByID(String ID)
    {
        return actions.get(ID);
    }

    public void removeActionByID(String ID)
    {
        actions.remove(ID);
    }


    public Set<String> getActionsKeySet() {
        return actions.keySet();
    }

    public synchronized void addAction(Action action) throws CloneNotSupportedException {
        actions.put(action.getID(), action.clone());
    }

    public String getID()
    {
        return ID;
    }

    public String getName()
    {
        return name;
    }

    public int getRows()
    {
        return rows;
    }

    public int getCols()
    {
        return cols;
    }

    public int getActionSize()
    {
        return actionSize;
    }

    public int getActionGap()
    {
        return actionGap;
    }

    public void setRows(int rows)
    {
        this.rows = rows;
    }

    public void setCols(int cols)
    {
        this.cols = cols;
    }

    public void setID(String ID)
    {
        this.ID = ID;
    }

    public void setActionSize(int actionSize)
    {
        this.actionSize = actionSize;
    }

    public void setActionGap(int actionGap)
    {
        this.actionGap = actionGap;
    }

    public void setName(String name)
    {
        this.name = name;
    }


    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
