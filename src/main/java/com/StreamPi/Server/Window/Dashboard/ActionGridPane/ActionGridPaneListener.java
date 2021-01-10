package com.StreamPi.Server.Window.Dashboard.ActionGridPane;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.OtherActions.FolderAction;

public interface ActionGridPaneListener {
    void addActionToCurrentClientProfile(Action newAction);

    void renderFolder(FolderAction action);
}
