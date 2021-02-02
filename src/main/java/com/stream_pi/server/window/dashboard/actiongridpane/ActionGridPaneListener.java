package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.actionapi.action.Action;
import com.stream_pi.actionapi.otheractions.FolderAction;

public interface ActionGridPaneListener {
    void addActionToCurrentClientProfile(Action newAction);

    void renderFolder(FolderAction action);
}
