package com.stream_pi.server.window.dashboard.actiongridpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.action_api.otheractions.FolderAction;
import com.stream_pi.server.client.ClientProfile;

public interface ActionGridPaneListener
{
    void addActionToCurrentClientProfile(Action newAction);

    void renderFolder(FolderAction action);

    String getCurrentParent();

    ClientProfile getCurrentProfile();

    void setCurrentParent(String currentParent);

    ExternalPlugin createNewActionFromExternalPlugin(String module) throws Exception;
    Action createNewOtherAction(ActionType actionType) throws Exception;
}
