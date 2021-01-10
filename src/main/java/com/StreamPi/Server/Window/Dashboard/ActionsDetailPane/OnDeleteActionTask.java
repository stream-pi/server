package com.StreamPi.Server.Window.Dashboard.ActionsDetailPane;

import java.util.logging.Logger;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ClientConnection;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionBox;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class OnDeleteActionTask extends Task<Void>
{
    private Logger logger;

    public OnDeleteActionTask(ClientConnection connection, Action action, boolean isCombineChild,
        CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, ActionBox actionBox,
        ActionDetailsPane actionDetailsPane,
        ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.connection = connection;
        this.action = action;
        this.isCombineChild = isCombineChild;
        this.combineActionPropertiesPane = combineActionPropertiesPane;
        this.clientProfile = clientProfile;
        this.actionBox = actionBox;
        this.actionDetailsPane = actionDetailsPane;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        logger = Logger.getLogger(getClass().getName());
    }

    private ClientConnection connection;
    private Action action;
    private ActionBox actionBox;
    private ActionDetailsPane actionDetailsPane;
    private boolean isCombineChild;
    private ClientProfile clientProfile;
    private CombineActionPropertiesPane combineActionPropertiesPane;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    private void runTask()
    {
        try {

            connection.deleteAction(clientProfile.getID(), action.getID());
            clientProfile.removeActionByID(action.getID());

            if(isCombineChild)
            {
                System.out.println("ACTION ID TO BE REMOVED : "+action.getID());
                combineActionPropertiesPane.getCombineAction().removeChild(action.getID());

                System.out.println("222155  "+combineActionPropertiesPane.getCombineAction().getClientProperties().getSize());

                combineActionPropertiesPane.renderProps();

                try {

                    actionDetailsPane.onActionClicked(
                            combineActionPropertiesPane.getCombineAction(),
                            actionBox
                    );

                    actionDetailsPane.saveAction(combineActionPropertiesPane.getCombineAction());

                } catch (MinorException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Platform.runLater(()->{
                    actionBox.clear();
                    actionDetailsPane.clear();
                });
            }


            // Platform.runLater(()->{  
            //     saveButton.setDisable(false);
            //     deleteButton.setDisable(false);
            // });

        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }


    @Override
    protected Void call() throws Exception
    {
        runTask();
        return null;
    }
    
}
