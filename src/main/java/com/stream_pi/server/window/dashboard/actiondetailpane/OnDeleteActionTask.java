package com.stream_pi.server.window.dashboard.actiondetailpane;

import java.util.logging.Logger;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;

import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class OnDeleteActionTask extends Task<Void>
{
    private Logger logger;

    public OnDeleteActionTask(ClientConnection connection, Action action, boolean isCombineChild,
                              CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, ActionBox actionBox,
                              ActionDetailsPane actionDetailsPane,
                              ExceptionAndAlertHandler exceptionAndAlertHandler, boolean runAsync)
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

        if(runAsync)
            new Thread(this).start();
        else
            runTask();
    }

    private ClientConnection connection;
    private Action action;
    private ActionBox actionBox;
    private ActionDetailsPaneListener actionDetailsPane;
    private boolean isCombineChild;
    private ClientProfile clientProfile;
    private CombineActionPropertiesPane combineActionPropertiesPane;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;


    private void runTask()
    {
        try {

            if(action instanceof ExternalPlugin)
            {
                try
                {
                    ((ExternalPlugin) action).onActionDeleted();
                }
                catch (MinorException e)
                {
                    e.setTitle("Unable to run onActionDeleted for "+action.getModuleName());
                    exceptionAndAlertHandler.handleMinorException("Display Text: "+action.getDisplayText()+"\nDetailed message : \n\n"+e.getMessage(), e);
                }
            }

            Action a = clientProfile.getActionByID(action.getID());
            if(a instanceof ExternalPlugin)
            {
                try
                {
                    ((ExternalPlugin) a).onActionDeleted();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleMinorException(
                            new MinorException("failed at onActionDeleted for "+a.getModuleName(),
                                    "Detailed Message : "+e.getMessage())
                    );
                }
            }


            connection.deleteAction(clientProfile.getID(), action.getID());
            clientProfile.removeActionByID(action.getID());

            if(isCombineChild)
            {
                System.out.println("ACTION ID TO BE REMOVED : "+action.getID());
                combineActionPropertiesPane.getCombineAction().removeChild(action.getID());

                System.out.println("222155  "+combineActionPropertiesPane.getCombineAction().getClientProperties().getSize());

                combineActionPropertiesPane.renderProps();

                try {


                    System.out.println(combineActionPropertiesPane.getCombineAction().getDisplayText());

                    connection.saveActionDetails(clientProfile.getID(), combineActionPropertiesPane.getCombineAction());

                    actionDetailsPane.onActionClicked(
                            combineActionPropertiesPane.getCombineAction(),
                            actionBox
                    );



                } catch (MinorException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Platform.runLater(()->{
                    actionDetailsPane.clearActionBox(action.getLocation().getCol(), action.getLocation().getRow(),
                            action.getColSpan(), action.getRowSpan());
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
