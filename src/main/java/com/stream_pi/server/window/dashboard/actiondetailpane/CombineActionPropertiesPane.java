package com.stream_pi.server.window.dashboard.actiondetailpane;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.otheractions.CombineAction;
import com.stream_pi.server.client.ClientProfile;
import com.stream_pi.util.exception.MinorException;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

public class CombineActionPropertiesPane extends VBox {
    private CombineAction combineAction;
    private ClientProfile clientProfile;
    private ActionDetailsPane actionDetailsPane;

    public CombineAction getCombineAction() {
        return combineAction;
    }

    public CombineActionPropertiesPane(CombineAction combineAction, ClientProfile clientProfile, ActionDetailsPane actionDetailsPane) throws MinorException {
        this.combineAction = combineAction;
        this.clientProfile = clientProfile;
        this.actionDetailsPane = actionDetailsPane;

        setSpacing(10.0);

        setPadding(new Insets(0,0,10,0));

        renderProps();
    }


    public void renderProps() throws MinorException {

        getChildren().clear();

        int i = 0;

        for(String actionID : combineAction.getChildrenIDSequential())
        {
            Action action = clientProfile.getActionByID(actionID);

            System.out.println("232323xxxxxxxxxxxx : "+action.getID());
            Button settingsButton = new Button();

            FontIcon settingsFontIcon = new FontIcon("fas-cog");
            settingsButton.setGraphic(settingsFontIcon);


            settingsButton.setOnAction(event -> onSettingsButtonClicked(action));

            Button upButton = new Button();
            FontIcon upButtonFontIcon = new FontIcon("fas-chevron-up");
            upButton.setGraphic(upButtonFontIcon);


            Button downButton = new Button();
            FontIcon downButtonFontIcon = new FontIcon("fas-chevron-down");
            downButton.setGraphic(downButtonFontIcon);

            Label displayTextLabel = new Label(action.getDisplayText());
            displayTextLabel.setId(action.getID());

            Region r = new Region();
            HBox.setHgrow(r, Priority.ALWAYS);

            HBox actionHBox = new HBox(displayTextLabel, r, settingsButton, upButton, downButton);
            actionHBox.setSpacing(5.0);

            upButton.setUserData(i);
            downButton.setUserData(i);


            upButton.setOnAction(this::onUpButtonClicked);

            downButton.setOnAction(this::onDownButtonClicked);
            getChildren().add(actionHBox);
            i++;
        }
    }

    public void onSettingsButtonClicked(Action action)
    {
        actionDetailsPane.clear();
        actionDetailsPane.setAction(action);
        try {
            actionDetailsPane.renderActionProperties();
        } catch (MinorException e) {
            e.printStackTrace();
        }
    }

    public void onUpButtonClicked(ActionEvent event) {
        try {
            Node node = (Node) event.getSource();

            int currentIndex = (int) node.getUserData();

            if(currentIndex > 0)
            {

                Property current = combineAction.getClientProperties().getSingleProperty(currentIndex+"");
                Property aboveOne = combineAction.getClientProperties().getSingleProperty((currentIndex-1)+"");

                combineAction.addChild(current.getRawValue(), currentIndex-1);
                combineAction.addChild(aboveOne.getRawValue(), currentIndex);

                actionDetailsPane.saveAction(true, true);
                renderProps();
            }
        }
        catch (MinorException e)
        {
            e.printStackTrace();
        }
    }


    public void onDownButtonClicked(ActionEvent event)
    {
        try {
            Node node = (Node) event.getSource();

            int currentIndex = (int) node.getUserData();

            if(currentIndex < getChildren().size() - 1)
            {
                Property current = combineAction.getClientProperties().getSingleProperty(currentIndex+"");
                Property belowOne = combineAction.getClientProperties().getSingleProperty((currentIndex+1)+"");

                combineAction.addChild(current.getRawValue(), currentIndex+1);
                combineAction.addChild(belowOne.getRawValue(), currentIndex);

                actionDetailsPane.saveAction(true, true);
                renderProps();
            }
        }
        catch (MinorException e)
        {
            e.printStackTrace();
        }
    }


    public List<String> getFinalChildren()
    {
        ArrayList<String> children = new ArrayList<>();

        for(int i = 0;i<getChildren().size();i++)
        {
            HBox hBox = (HBox) getChildren().get(i);
            Label label = (Label) hBox.getChildren().get(0);
            children.add(label.getId());
        }

        return children;
    }
}
