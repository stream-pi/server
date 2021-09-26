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

    ActionBox getActionBoxByIDAndProfileID(String actionID, String profileID);

    void renderFolder(FolderAction action);
    void renderAction(Action action) throws MinorException;

    String getCurrentParent();

    ClientProfile getCurrentProfile();

    void setCurrentParent(String currentParent);

    ClientConnection getClientConnection();

    ActionBox getActionBox(int col, int row);

    ExternalPlugin createNewActionFromExternalPlugin(String module) throws CloneNotSupportedException, SevereException;
    Action createNewOtherAction(ActionType actionType) throws Exception;
    void clearActionBox(int col, int row, int colSpan, int rowSpan);
}
