package com.StreamPi.Server.Window.Dashboard.ActionsDetailPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.DisplayTextAlignment;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.ActionProperty.Property.Type;
import com.StreamPi.Server.Client.ClientProfile;
import com.StreamPi.Server.Connection.ClientConnection;
import com.StreamPi.Server.UIPropertyBox.UIPropertyBox;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Server.Window.Dashboard.ActionGridPane.ActionBox;
import com.StreamPi.Util.Exception.SevereException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Button;

public class OnSaveActionTask extends Task<Void> 
{

    private Logger logger;

    public OnSaveActionTask(ClientConnection connection, Action action, String displayNameText, boolean isCombineChild,
        boolean isShowDisplayText, boolean isDefaultDisplayTextColour, String displayTextFontColour, boolean isClearIcon,
        boolean isHideIcon, DisplayTextAlignment displayTextAlignment, boolean isTransparentBackground, String backgroundColour,
        CombineActionPropertiesPane combineActionPropertiesPane, ClientProfile clientProfile, boolean sendIcon, ActionBox actionBox,
        ArrayList<UIPropertyBox> actionClientProperties, ExceptionAndAlertHandler exceptionAndAlertHandler, Button saveButton, Button deleteButton)
    {
        this.saveButton = saveButton;
        this.deleteButton = deleteButton;

        this.connection = connection;
        this.action = action;
        this.displayNameText = displayNameText;
        this.isCombineChild = isCombineChild;
        this.isShowDisplayText = isShowDisplayText;
        this.isDefaultDisplayTextColour = isDefaultDisplayTextColour;
        this.displayTextFontColour = displayTextFontColour;
        this.isClearIcon = isClearIcon;
        this.isHideIcon = isHideIcon;
        this.displayTextAlignment = displayTextAlignment;
        this.isTransparentBackground = isTransparentBackground;
        this.combineActionPropertiesPane = combineActionPropertiesPane;
        this.clientProfile = clientProfile;
        this.sendIcon = sendIcon;
        this.actionBox = actionBox;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.backgroundColour = backgroundColour;
        this.actionClientProperties = actionClientProperties;


        logger = Logger.getLogger(getClass().getName());
    }

    private Button saveButton;
    private Button deleteButton;
    private boolean isShowDisplayText;
    private boolean isCombineChild;
    private String displayNameText;
    private boolean isDefaultDisplayTextColour;
    private ArrayList<UIPropertyBox> actionClientProperties;
    private String displayTextFontColour;
    private boolean isClearIcon;
    private boolean isHideIcon;
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
                action.setIcon(null);
                action.setHasIcon(false);
                action.setShowIcon(false);
            }

            if(action.isHasIcon())
                action.setShowIcon(isHideIcon);


            action.setDisplayTextAlignment(displayTextAlignment);


            logger.info("BBBGGG : "+backgroundColour);
            if(isTransparentBackground)
                action.setBgColourHex("");
            else
            {
                //String bgColour = "#" + actionBackgroundColourPicker.getValue().toString().substring(2);
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
            //properties
            for (UIPropertyBox clientProperty : actionClientProperties) {
                action.getClientProperties().get().get(clientProperty.getIndex()).setRawValue(clientProperty.getRawValue());
            }
        }


        try 
        {
            logger.info("Saving action ... "+action.isHasIcon()+"+"+sendIcon);

            if(action.isHasIcon())
            {
                if(clientProfile.getActionByID(action.getID()).getIconAsByteArray() == null)
                {
                    sendIcon = true;
                }
                else
                {
                    if(!Arrays.equals(action.getIconAsByteArray(), clientProfile.getActionByID(action.getID()).getIconAsByteArray()))
                    {
                       logger.info("Sending ...");
                       sendIcon = true;
                   }
               }
            }

            connection.saveActionDetails(clientProfile.getID(), action);

            if(sendIcon)
            {   
                connection.sendIcon(clientProfile.getID(), action.getID(), action.getIconAsByteArray());
            }

            if(!isCombineChild)
            {
                Platform.runLater(()->{
                    actionBox.clear();
                    actionBox.setAction(action);
                    actionBox.baseInit();
                    actionBox.init();
                });

                setSaveDeleteButtonState(false);
            }

            clientProfile.removeActionByID(action.getID());
            clientProfile.addAction(action);

        }
        catch (SevereException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleSevereException(e);
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }
        
    }

    @Override
    protected Void call() throws Exception
    {
        runTask();
        return null;
    }
}