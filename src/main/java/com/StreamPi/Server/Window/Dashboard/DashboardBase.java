package com.StreamPi.Server.Window.Dashboard;

import java.util.logging.Logger;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ClientConnection;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionGridPane;
import com.StreamPi.Server.Window.Dashboard.ActionsDetailPane.ActionDetailsPane;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Util.Exception.SevereException;
import javafx.application.HostServices;
import javafx.scene.CacheHint;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardBase extends HBox implements DashboardInterface {

    private final VBox leftPane;

    private Logger logger;

    public ClientProfile currentClientProfile;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    public DashboardBase(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        logger = Logger.getLogger(DashboardBase.class.getName());
        leftPane = new VBox();

        HBox.setHgrow(leftPane, Priority.ALWAYS);
        getChildren().add(leftPane);

        setPluginsPane(new PluginsPane(hostServices));

        setClientDetailsPane(new ClientDetailsPane(this));

        setActionGridPane(new ActionGridPane(exceptionAndAlertHandler));

        setActionDetailsPane(new ActionDetailsPane(exceptionAndAlertHandler, hostServices));

        getActionGridPane().setActionDetailsPaneListener(getActionDetailsPane());
    }



    private PluginsPane pluginsPane;
    private void setPluginsPane(PluginsPane pluginsPane)
    {
        this.pluginsPane = pluginsPane;
        getChildren().add(this.pluginsPane);
    }
    public PluginsPane getPluginsPane()
    {
        return pluginsPane;
    }

    private ClientDetailsPane clientDetailsPane;
    private void setClientDetailsPane(ClientDetailsPane clientDetailsPane)
    {
        this.clientDetailsPane = clientDetailsPane;
        leftPane.getChildren().add(this.clientDetailsPane);
    }
    public ClientDetailsPane getClientDetailsPane()
    {
        return clientDetailsPane;
    }

    private ActionGridPane actionGridPane;
    private void setActionGridPane(ActionGridPane actionGridPane)
    {
        this.actionGridPane = actionGridPane;
        leftPane.getChildren().add(this.actionGridPane);
    }
    public ActionGridPane getActionGridPane()
    {
        return actionGridPane;
    }

    private ActionDetailsPane actionDetailsPane;
    private void setActionDetailsPane(ActionDetailsPane actionDetailsPane)
    {
        this.actionDetailsPane = actionDetailsPane;
        leftPane.getChildren().add(this.actionDetailsPane);
    }
    public ActionDetailsPane getActionDetailsPane()
    {
        return actionDetailsPane;
    }

    public void newSelectedClientConnection(ClientConnection clientConnection)
    {
        if(clientConnection == null)
        {
            logger.info("Remove action grid");
        }
        else
        {
            getActionDetailsPane().setClient(clientConnection.getClient());
            getActionGridPane().setClient(clientConnection.getClient());
        }
    }

    public void newSelectedClientProfile(ClientProfile clientProfile)
    {
        this.currentClientProfile = clientProfile;

        getActionDetailsPane().setClientProfile(clientProfile);

        drawProfile(this.currentClientProfile);
    }

    public void drawProfile(ClientProfile clientProfile)
    {
        logger.info("Drawing ...");

        getActionGridPane().setClientProfile(clientProfile);

        try {
            getActionGridPane().renderGrid();
            getActionGridPane().renderActions();
        }
        catch (SevereException e)
        {
            exceptionAndAlertHandler.handleSevereException(e);
        }


    }
}
