package com.stream_pi.server.window.dashboard.actiondetailpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionGridPaneListener;
import com.stream_pi.util.exception.MinorException;
import javafx.stage.Window;

public interface ActionDetailsPaneListener {
    void onActionClicked(Action action, ActionBox actionBox) throws MinorException;

    void saveAction(boolean runAsync, boolean runOnActionSavedFromServer);

    void saveAction(Action action, boolean runAsync, boolean runOnActionSavedFromServer);

    void clear();

    void setSendIcon(boolean sendIcon);

    void setAction(Action action);

    void onOpenFolderButtonClicked();

    Window getCurrentWindow();


}
