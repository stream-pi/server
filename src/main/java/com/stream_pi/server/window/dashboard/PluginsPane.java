package com.stream_pi.server.window.dashboard;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.action.ExternalPlugins;

import com.stream_pi.server.controller.ActionDataFormats;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.uihelper.SpaceFiller;
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
        setMaxWidth(350);
        getStyleClass().add("plugins_pane");
        setPadding(new Insets(10));

        this.hostServices = hostServices;

        initUI();
    }

    private Accordion pluginsAccordion;

    public void initUI()
    {
        pluginsAccordion = new Accordion();
        pluginsAccordion.getStyleClass().add("plugins_pane_accordion");
        pluginsAccordion.setCache(true);
        settingsButton = new Button();
        settingsButton.getStyleClass().add("plugins_pane_settings_button");

        FontIcon cog = new FontIcon("fas-cog");

        settingsButton.setGraphic(cog);

        HBox settingsHBox = new HBox(settingsButton);
        settingsHBox.getStyleClass().add("plugins_pane_settings_button_parent");
        settingsHBox.setAlignment(Pos.CENTER_RIGHT);

        Label pluginsLabel = new Label(I18N.getString("window.dashboard.PluginsPane.plugins"));
        pluginsLabel.getStyleClass().add("plugins_pane_top_label");

        getChildren().addAll(pluginsLabel, pluginsAccordion, SpaceFiller.vertical(), settingsHBox);
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
        HashMap<String, ArrayList<ExternalPlugin>> sortedPlugins = ExternalPlugins.getInstance().getSortedPlugins();

        for(String eachCategory : sortedPlugins.keySet())
        {
            VBox vBox = new VBox();
            vBox.getStyleClass().add("plugins_pane_each_plugin_box_parent");

            TitledPane pane = new TitledPane(eachCategory, vBox);
            pane.getStyleClass().add("plugins_pane_each_plugin_category_titled_pane");
            for(ExternalPlugin eachAction : sortedPlugins.get(eachCategory))
            {
                if(!eachAction.isVisibleInPluginsPane())
                    continue;

                Button eachNormalActionPluginButton = new Button();
                eachNormalActionPluginButton.getStyleClass().add("plugins_pane_each_plugin_button");
                HBox.setHgrow(eachNormalActionPluginButton, Priority.ALWAYS);
                eachNormalActionPluginButton.setMaxWidth(Double.MAX_VALUE);
                eachNormalActionPluginButton.setAlignment(Pos.CENTER_LEFT);

                Node graphic = eachAction.getServerButtonGraphic();

                if(graphic == null)
                {
                    if(eachAction.getActionType() == ActionType.TOGGLE)
                    {
                        FontIcon toggleIcon = new FontIcon("fas-toggle-on");
                        toggleIcon.getStyleClass().add("plugins_pane_each_plugin_button_icon_toggle");
                        eachNormalActionPluginButton.setGraphic(toggleIcon);
                    }
                    else if(eachAction.getActionType() == ActionType.GAUGE)
                    {
                        FontIcon dynamicIcon = new FontIcon("fas-magic");
                        dynamicIcon.getStyleClass().add("plugins_pane_each_plugin_button_icon_dynamic");
                        eachNormalActionPluginButton.setGraphic(dynamicIcon);
                    }
                    else if(eachAction.getActionType() == ActionType.NORMAL)
                    {
                        FontIcon normalIcon = new FontIcon("fas-cogs");
                        normalIcon.getStyleClass().add("plugins_pane_each_plugin_button_icon_normal");
                        eachNormalActionPluginButton.setGraphic(normalIcon);
                    }
                }
                else
                {
                    if(graphic instanceof FontIcon)
                    {
                        FontIcon fi = (FontIcon) graphic;
                        fi.getStyleClass().add("plugins_pane_each_plugin_button_icon");
                        eachNormalActionPluginButton.setGraphic(fi);
                    }
                    else if(graphic instanceof ImageView)
                    {
                        ImageView iv = (ImageView) graphic;
                        iv.getStyleClass().add("plugins_pane_each_plugin_button_imageview");
                        iv.setPreserveRatio(false);
                        eachNormalActionPluginButton.setGraphic(iv);
                    }
                }
                eachNormalActionPluginButton.setText(eachAction.getName());


                eachNormalActionPluginButton.setOnDragDetected(mouseEvent -> {
                    Dragboard db = eachNormalActionPluginButton.startDragAndDrop(TransferMode.ANY);

                    ClipboardContent content = new ClipboardContent();

                    content.put(ActionDataFormats.ACTION_TYPE, eachAction.getActionType());
                    content.put(ActionDataFormats.MODULE_NAME, eachAction.getModuleName());
                    content.put(ActionDataFormats.IS_NEW, true);

                    db.setContent(content);

                    mouseEvent.consume();
                });

            

                HBox hBox = new HBox(eachNormalActionPluginButton);
                hBox.getStyleClass().add("plugins_pane_each_plugin_box");
                hBox.setAlignment(Pos.TOP_LEFT);

                HBox.setHgrow(eachNormalActionPluginButton, Priority.ALWAYS);

                if(eachAction.getHelpLink() != null) {
                    Button helpButton = new Button();
                    helpButton.getStyleClass().add("plugins_pane_each_plugin_button_help_icon");
                    FontIcon questionIcon = new FontIcon("fas-question");
                    questionIcon.getStyleClass().add("plugins_pane_each_plugin_button_help_button_icon");
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

    public void loadOtherActions()
    {
        VBox vBox = new VBox();
        vBox.getStyleClass().add("plugins_pane_each_plugin_box_parent");

        Button folderActionButton = new Button(I18N.getString("window.dashboard.PluginsPane.folder"));
        folderActionButton.getStyleClass().add("plugins_pane_each_plugin_button");
        folderActionButton.setMaxWidth(Double.MAX_VALUE);
        folderActionButton.setAlignment(Pos.CENTER_LEFT);
        FontIcon folder = new FontIcon("fas-folder");
        folderActionButton.setGraphic(folder);

        folderActionButton.setOnDragDetected(mouseEvent -> {
            Dragboard db = folderActionButton.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            content.put(ActionDataFormats.ACTION_TYPE, ActionType.FOLDER);

            db.setContent(content);

            mouseEvent.consume();
        });




        Button combineActionButton = new Button(I18N.getString("window.dashboard.PluginsPane.combine"));
        combineActionButton.getStyleClass().add("plugins_pane_each_plugin_button");
        combineActionButton.setMaxWidth(Double.MAX_VALUE);
        combineActionButton.setAlignment(Pos.CENTER_LEFT);
        FontIcon list = new FontIcon("fas-list");
        combineActionButton.setGraphic(list);

        combineActionButton.setOnDragDetected(mouseEvent -> {
            Dragboard db = combineActionButton.startDragAndDrop(TransferMode.ANY);

            ClipboardContent content = new ClipboardContent();

            content.put(ActionDataFormats.ACTION_TYPE, ActionType.COMBINE);

            db.setContent(content);

            mouseEvent.consume();
        });

        HBox.setHgrow(folderActionButton, Priority.ALWAYS);
        HBox h1 = new HBox(folderActionButton);
        h1.getStyleClass().add("plugins_pane_each_plugin_box");

        HBox.setHgrow(combineActionButton, Priority.ALWAYS);
        HBox h2 = new HBox(combineActionButton);
        h2.getStyleClass().add("plugins_pane_each_plugin_box");

        vBox.getChildren().addAll(h1, h2);

        TitledPane pane = new TitledPane(I18N.getString("title"), vBox);
        pane.getStyleClass().add("plugins_pane_each_plugin_category_titled_pane");

        pluginsAccordion.getPanes().add(pane);
        pluginsAccordion.setCache(true);
        pluginsAccordion.setCacheHint(CacheHint.SPEED);
    }

}
