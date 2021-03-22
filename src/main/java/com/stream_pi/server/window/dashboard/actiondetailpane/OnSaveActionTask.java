package com.stream_pi.server.window.dashboard.actiondetailpane;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.DisplayTextAlignment;
import com.stream_pi.action_api.actionproperty.ClientProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.server.connection.ClientConnection;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.dashboard.actiongridpane.ActionBox;

import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;

public class OnSaveActionTask extends Task<Void> 
{

    private Logger logger;


    public OnSaveActionTask(ClientConnection connection, Action action, String delayBeforeRunningString, String displayNameText, boolean isCombineChild,
                            boolean isShowDisplayText, boolean isDefaultDisplayTextColour, String displayTextFontColour, boolean isClearIcon,
                            boolean isHideDefaultIcon, boolean isHideToggleOffIcon, boolean isHideToggleOnIcon, DisplayTextAlignment displayTextAlignment, boolean isTransparentBackground, String backgroundColour,
                            CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, boolean sendIcon, ActionBox actionBox,
                            ArrayList<UIPropertyBox> actionClientProperties, ExceptionAndAlertHandler exceptionAndAlertHandler, Button saveButton, Button deleteButton,
                            boolean runOnActionSavedFromServer, boolean runAsync)
    {
        this.saveButton = saveButton;
        this.deleteButton = deleteButton;
        this.delayBeforeRunningString = delayBeforeRunningString;

        this.connection = connection;
        this.action = action;
        this.displayNameText = displayNameText;
        this.isCombineChild = isCombineChild;
        this.isShowDisplayText = isShowDisplayText;
        this.isDefaultDisplayTextColour = isDefaultDisplayTextColour;
        this.displayTextFontColour = displayTextFontColour;
        this.isClearIcon = isClearIcon;
        this.isHideDefaultIcon = isHideDefaultIcon;
        this.isHideToggleOffIcon = isHideToggleOffIcon;
        this.isHideToggleOnIcon = isHideToggleOnIcon;
        this.displayTextAlignment = displayTextAlignment;
        this.isTransparentBackground = isTransparentBackground;
        this.combineActionPropertiesPane = combineActionPropertiesPane;
        this.clientProfile = clientProfile;
        this.sendIcon = sendIcon;
        this.actionBox = actionBox;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.backgroundColour = backgroundColour;
        this.actionClientProperties = actionClientProperties;
        this.runOnActionSavedFromServer = runOnActionSavedFromServer;

        logger = Logger.getLogger(getClass().getName());


        if(runAsync)
            new Thread(this).start();
        else
            runTask();
    }

    private boolean runOnActionSavedFromServer;

    private Button saveButton;
    private Button deleteButton;
    private String delayBeforeRunningString;
    private boolean isShowDisplayText;
    private boolean isCombineChild;
    private String displayNameText;
    private boolean isDefaultDisplayTextColour;
    private ArrayList<UIPropertyBox> actionClientProperties;
    private String displayTextFontColour;
    private boolean isClearIcon;
    private boolean isHideDefaultIcon;
    private boolean isHideToggleOffIcon;
    private boolean isHideToggleOnIcon;
    private DisplayTextAlignment displayTextAlignment;
    private boolean isTransparentBackground;
    private String backgroundColour;
    private CombineActionPropertiesPane combineActionPropertiesPane;
    private ClientProfile clientProfile;
    private boolean sendIcon;
    private ActionBox actionBox;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;
    private Action action;
    private ClientConnection connection;

    private void setSaveDeleteButtonState(boolean state)
    {
        Platform.runLater(()->{
            saveButton.setDisable(state);
            deleteButton.setDisable(state);
        });
    }

    
    private void runTask()
    {
        action.setDisplayText(displayNameText);

        if(!isCombineChild)
        {
            setSaveDeleteButtonState(true);

            action.setShowDisplayText(isShowDisplayText);

            if(isDefaultDisplayTextColour)
                action.setDisplayTextFontColourHex("");
            else
            {
                action.setDisplayTextFontColourHex(displayTextFontColour);
                //String fontColour = "#" + displayTextColourPicker.getValue().toString().substring(2);
                //action.setDisplayTextFontColourHex(fontColour);
            }


            if(isClearIcon)
            {
                action.setIcons(null);
                action.setCurrentIconState("");
            }


            if(action.getActionType() == ActionType.NORMAL)
            {
                if(isHideDefaultIcon)
                {
                    if(action.getIcon("default") != null)
                        action.setCurrentIconState("default");
                }
                else
                {
                    action.setCurrentIconState("");
                }
            }
            else if (action.getActionType() == ActionType.TOGGLE)
            {
                action.setCurrentIconState(isHideToggleOffIcon+"__"+isHideToggleOnIcon);
            }

            action.setDisplayTextAlignment(displayTextAlignment);


            if(isTransparentBackground)
                action.setBgColourHex("");
            else
            {
                action.setBgColourHex(backgroundColour);
            }
        }

        System.out.println("parent : "+action.getParent());


        if(action.getActionType() == ActionType.COMBINE)
        {
            List<String> finalChildren = combineActionPropertiesPane.getFinalChildren();
            System.out.println("2334  "+finalChildren.size());

            ClientProperties clientProperties = new ClientProperties();

            for(int i = 0;i<finalChildren.size();i++)
            {
               Property property = new Property(i+"", Type.STRING);
               property.setRawValue(finalChildren.get(i));

                clientProperties.addProperty(property);
            }

            action.getClientProperties().set(clientProperties);
        }
        else
        {
            action.setDelayBeforeExecuting(Integer.parseInt(delayBeforeRunningString));

            //properties
            for (UIPropertyBox clientProperty : actionClientProperties) {
                action.getClientProperties().get().get(clientProperty.getIndex()).setRawValue(clientProperty.getRawValue());
            }
        }


        try 
        {
            logger.info("Saving action ... "+action.isHasIcon()+"+"+sendIcon);

            connection.saveActionDetails(clientProfile.getID(), action);

            if(sendIcon)
            {
                sendAllIcons(clientProfile, action);
            }

            if(!isCombineChild)
            {
                Platform.runLater(()->{
                    actionBox.clear();
                    actionBox.setAction(action);
                    //actionBox.baseInit();
                    actionBox.init();
                });

                setSaveDeleteButtonState(false);
            }

            //clientProfile.removeActionByID(action.getID());
            //clientProfile.addAction(action);


            if(runOnActionSavedFromServer)
            {

                for(Property property : action.getClientProperties().get())
                {
                    System.out.println("SSSSSDDD : "+property.getName());
                    System.out.println("@@@@DDD : "+property.getRawValue());
                }
                try
                {
                    if(action instanceof ExternalPlugin)
                        ((ExternalPlugin) action).onActionSavedFromServer();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    exceptionAndAlertHandler.handleMinorException(new MinorException("Error","onActionSavedFromServer() failed for "+action.getModuleName()+"\n\n"+e.getMessage()));
                }
            }
        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }

    private void sendAllIcons(ClientProfile clientProfile, Action action) throws SevereException
    {
        for(String state : action.getIcons().keySet())
        {
            connection.sendIcon(clientProfile.getID(), action.getID(), state, action.getIcon(state));
        }
    }

    @Override
    protected Void call() throws Exception
    {
        runTask();
        return null;
    }
}
