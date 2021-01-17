package com.StreamPi.Server.Window.Dashboard.ActionsDetailPane;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.Location;
import com.StreamPi.ActionAPI.OtherActions.FolderAction;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionBox;
import com.StreamPi.Util.Exception.MinorException;
import javafx.stage.Window;

public interface ActionDetailsPaneListener {
    void onActionClicked(Action action, ActionBox actionBox) throws MinorException;

    void saveAction();

    void saveAction(Action action, boolean runAsync);

    void clear();

    void setSendIcon(boolean sendIcon);

    void onOpenFolderButtonClicked();

    Window getCurrentWindow();

}
