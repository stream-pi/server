package com.stream_pi.server.window.dashboard;
import com.stream_pi.actionapi.action.Action;
import com.stream_pi.actionapi.action.ActionType;
import com.stream_pi.actionapi.action.DisplayTextAlignment;
import com.stream_pi.actionapi.actionproperty.property.Property;
import com.stream_pi.actionapi.actionproperty.property.Type;
import com.stream_pi.actionapi.normalaction.NormalAction;
import com.stream_pi.actionapi.otheractions.CombineAction;
import com.stream_pi.actionapi.otheractions.FolderAction;
import com.stream_pi.server.action.NormalActionPlugins;

import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.HashMap;

public class PluginsPane extends VBox {

    private Button settingsButton;

    public PluginsPane(HostServices hostServices)
    {
        setMinWidth(250);
        getStyleClass().add("plugins_pane");
        setPadding(new Insets(10));

        setSpacing(10.0);

        this.hostServices = hostServices;

        initUI();
    }

    private Accordion pluginsAccordion;

    public void initUI()
    {
        pluginsAccordion = new Accordion();
        pluginsAccordion.setCache(true);

        Region r = new Region();
        VBox.setVgrow(r, Priority.ALWAYS);

        settingsButton = new Button();

        FontIcon cog = new FontIcon("fas-cog");

        settingsButton.setGraphic(cog);

        HBox settingsHBox = new HBox(settingsButton);
        settingsHBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(new Label("Plugins"), pluginsAccordion, r, settingsHBox);
    }

    public Button getSettingsButton()
    {
        return settingsButton;
    }

    public void clearData()
    {
        pluginsAccordion.getPanes().clear();
    }

    public void loadData()
    {
        HashMap<String, ArrayList<NormalAction>> sortedPlugins = NormalActionPlugins.getInstance().getSortedPlugins();

        for(String eachCategory : sortedPlugins.keySet())
        {
            VBox vBox = new VBox();
            vBox.setSpacing(5);


            TitledPane pane = new TitledPane(eachCategory, vBox);
            for(NormalAction eachAction : sortedPlugins.get(eachCategory))
            {
                if(!eachAction.isVisibleInPluginsPane())
                    continue;

                Button eachNormalActionPluginButton = new Button();
                HBox.setHgrow(eachNormalActionPluginButton, Priority.ALWAYS);
                eachNormalActionPluginButton.setMaxWidth(Double.MAX_VALUE);
                eachNormalActionPluginButton.setAlignment(Pos.CENTER_LEFT);

                Node graphic = eachAction.getServerButtonGraphic();

                if(graphic == null)
                {
                    FontIcon cogs = new FontIcon("fas-cogs");
                    cogs.getStyleClass().add("dashboard_plugins_pane_action_icon");
                    eachNormalActionPluginButton.setGraphic(cogs);
                }
                else
                {
                    if(graphic instanceof FontIcon)
                    {
                        FontIcon fi = (FontIcon) graphic;
                        eachNormalActionPluginButton.setGraphic(fi);
                    }
                    else if(graphic instanceof ImageView)
                    {
                        ImageView iv = (ImageView) graphic;
                        iv.getStyleClass().add("dashboard_plugins_pane_action_icon_imageview");
                        iv.setPreserveRatio(false);
                        eachNormalActionPluginButton.setGraphic(iv);
                    }
                }
                eachNormalActionPluginButton.setText(eachAction.getName());


                eachNormalActionPluginButton.setOnDragDetected(mouseEvent -> {
                    Dragboard db = eachNormalActionPluginButton.startDragAndDrop(TransferMode.ANY);

                    ClipboardContent content = new ClipboardContent();

                    content.put(Action.getDataFormat(), createFakeAction(eachAction, "Untitled action"));

                    db.setContent(content);

                    mouseEvent.consume();
                });

            

                HBox hBox = new HBox(eachNormalActionPluginButton);
                hBox.setSpacing(5.0);
                hBox.setAlignment(Pos.TOP_LEFT);

                HBox.setHgrow(eachNormalActionPluginButton, Priority.ALWAYS);

                if(eachAction.getHelpLink() != null) {
                    Button helpButton = new Button();
                    FontIcon questionIcon = new FontIcon("fas-question");
                    questionIcon.getStyleClass().add("dashboard_plugins_pane_action_help_icon");
                    helpButton.setGraphic(questionIcon);
                    helpButton.setOnAction(event -> hostServices.showDocument(eachAction.getHelpLink()));

                    hBox.getChildren().add(helpButton);
                }


                vBox.getChildren().add(hBox);
            }

            if(vBox.getChildren().size() > 0)
                pluginsAccordion.getPanes().add(pane);
        }
    }

    
    private HostServices hostServices;

    public Action createFakeAction(Action action, String displayText)
    {
        Action newAction = new Action(action.getActionType());

        if(action.getActionType() == ActionType.NORMAL)
        {
            newAction.setModuleName(action.getModuleName());
            newAction.setVersion(action.getVersion());
            newAction.setName(action.getName());
        }

        newAction.setClientProperties(action.getClientProperties());

        for(Property property : newAction.getClientProperties().get())
        {
            if(property.getType() == Type.STRING || property.getType() == Type.INTEGER || property.getType() == Type.DOUBLE)
                property.setRawValue(property.getDefaultRawValue());
        }

       // newAction.setLocation(location);

        newAction.setIDRandom();


        newAction.setShowDisplayText(true);
        newAction.setDisplayText(displayText);
        newAction.setDisplayTextAlignment(DisplayTextAlignment.CENTER);
        newAction.setShowIcon(false);
        newAction.setHasIcon(false);

        //action.setParent(root);

        newAction.setBgColourHex("");
        newAction.setDisplayTextFontColourHex("");

        return newAction;
    }

    public void loadOtherActions()
    {
        VBox vBox = new VBox();

        Button folderActionButton = new Button("Folder");
        folderActionButton.setMaxWidth(Double.MAX_VALUE);
        folderActionButton.setAlignment(Pos.CENTER_LEFT);
        FontIcon folder = new FontIcon("fas-folder");
        folderActionButton.setGraphic(folder);

        folderActionButton.setOnDragDetected(mouseEvent -> {
            Dragboard db = folderActionButton.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            content.put(Action.getDataFormat(), createFakeAction(new FolderAction(), "Untitled Folder"));

            db.setContent(content);

            mouseEvent.consume();
        });




        Button combineActionButton = new Button("Combine");
        combineActionButton.setMaxWidth(Double.MAX_VALUE);
        combineActionButton.setAlignment(Pos.CENTER_LEFT);
        FontIcon list = new FontIcon("fas-list");
        combineActionButton.setGraphic(list);

        combineActionButton.setOnDragDetected(mouseEvent -> {
            Dragboard db = combineActionButton.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            content.put(Action.getDataFormat(), createFakeAction(new CombineAction(), "Untitled Combine"));

            db.setContent(content);

            mouseEvent.consume();
        });





        vBox.getChildren().addAll(folderActionButton, combineActionButton);
        vBox.setSpacing(5);

        TitledPane pane = new TitledPane("StreamPi", vBox);

        pluginsAccordion.getPanes().add(pane);
        pluginsAccordion.setCache(true);
        pluginsAccordion.setCacheHint(CacheHint.SPEED);
    }

}
