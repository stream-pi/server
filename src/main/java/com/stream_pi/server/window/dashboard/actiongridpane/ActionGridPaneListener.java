package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.otheractions.FolderAction;

public interface ActionGridPaneListener {
    void addActionToCurrentClientProfile(Action newAction);

    void renderFolder(FolderAction action);
}
