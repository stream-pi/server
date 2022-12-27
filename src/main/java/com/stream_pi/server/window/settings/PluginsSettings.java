/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package com.stream_pi.server.window.settings;

import com.stream_pi.action_api.actionproperty.ServerProperties;
import com.stream_pi.action_api.actionproperty.property.*;
import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.helper.ControlNodePair;
import com.stream_pi.server.window.helper.Helper;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.uihelper.HBoxInputBoxWithFileChooser;
import com.stream_pi.util.uihelper.HBoxWithSpaceBetween;
import com.stream_pi.util.uihelper.SpaceFiller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PluginsSettings extends VBox
{

    private final VBox pluginsSettingsVBox;

    private ServerListener serverListener;

    private Logger logger;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private HostServices hostServices;

    private javafx.beans.property.IntegerProperty unsavedChanges;

    public PluginsSettings(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices)
    {
        this.hostServices = hostServices;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        pluginProperties = new HashMap<>();
        logger = Logger.getLogger(PluginsSettings.class.getName());

        pluginsSettingsVBox = new VBox();
        pluginsSettingsVBox.getStyleClass().add("plugins_settings_vbox");
        pluginsSettingsVBox.setAlignment(Pos.TOP_CENTER);

        unsavedChanges = new SimpleIntegerProperty(0);
        Label unsavedLabel = new Label(I18N.getString("window.settings.PluginsSettings.unsavedChanges"));
        unsavedLabel.visibleProperty().bind(unsavedChanges.greaterThan(0));
        unsavedLabel.getStyleClass().add("plugins_settings_unsaved_changes_label");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("plugins_settings_scroll_pane");
        scrollPane.setFitToWidth(true);
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));

        scrollPane.setContent(pluginsSettingsVBox);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        saveButton = new Button(I18N.getString("save"));
        saveButton.disableProperty().bind(unsavedChanges.isEqualTo(0));
        HBox.setMargin(saveButton, new Insets(0,10, 0, 0));
        saveButton.setOnAction(event -> onSaveButtonClicked());


        HBox hBox = new HBox(saveButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(unsavedLabel, scrollPane, hBox);
        getStyleClass().add("plugins_settings");
    }

    private final Button saveButton;

    public void onSaveButtonClicked()
    {
        try
        {
            //form validation
            StringBuilder finalErrors = new StringBuilder();

            for (PluginProperties properties : pluginProperties.values())
            {
                StringBuilder errors = validatePluginProperties(properties);

                if(!errors.toString().isBlank())
                {
                    finalErrors.append("    * ").append(properties.getName()).append("\n").append(errors).append("\n");
                }
            }

            if(!finalErrors.toString().isEmpty())
            {
                throw new MinorException(I18N.getString("validationError", finalErrors));
            }

            //save
            for (PluginProperties pp : pluginProperties.values())
            {
                for (UIPropertyBox propertyBox : pp.getUIPropertyBoxes())
                {
                    propertyBox.saveValue();
                }
            }

            ExternalPlugins.getInstance().saveServerSettings();

            ExternalPlugins.getInstance().runOnServerPropertiesSavedByUser();
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }

    public void saveServerPropertiesFromFields(String uniqueID) throws MinorException
    {
        StringBuilder errors = validatePluginProperties(uniqueID);

        if(!errors.toString().isEmpty())
        {
            throw new MinorException(I18N.getString("validationError", errors));
        }

        PluginProperties pp = pluginProperties.get(uniqueID);
        for (UIPropertyBox propertyBox : pp.getUIPropertyBoxes())
        {
            propertyBox.saveValue();
        }


        ExternalPlugins.getInstance().saveServerSettings();
        reloadPlugin(uniqueID);
    }

    public StringBuilder validatePluginProperties(PluginProperties pluginProperties)
    {
        StringBuilder errors = new StringBuilder();
        for(int j = 0; j < pluginProperties.getUIPropertyBoxes().size(); j++)
        {
            UIPropertyBox serverProperty = pluginProperties.getUIPropertyBoxes().get(j);

            String error = serverProperty.validateProperty();

            if (error != null)
            {
                errors.append("        -> ").append(error).append(("\n"));
            }
        }

        return errors;
    }

    public StringBuilder validatePluginProperties(String uniqueID)
    {
        return validatePluginProperties(pluginProperties.get(uniqueID));
    }

    private final HashMap<String, PluginProperties> pluginProperties;

    public void showPluginInitError()
    {
        Platform.runLater(()->{
            pluginsSettingsVBox.getChildren().add(new Label(I18N.getString("window.settings.PluginsSettings.pluginInitError")));
            saveButton.setVisible(false);
        });
    }

    public void loadPlugins() throws MinorException
    {
        pluginProperties.clear();

        Collection<ExternalPlugin> actions = ExternalPlugins.getInstance().getPlugins();

     //   unsavedChanges.setValue(0);

        Platform.runLater(()-> pluginsSettingsVBox.getChildren().clear());

        if(actions.size() == 0)
        {
            Platform.runLater(()->{
                Label l = new Label(I18N.getString("window.settings.PluginsSettings.noPluginsInstalled"));
                l.getStyleClass().add("plugins_pane_no_plugins_installed_label");
                pluginsSettingsVBox.getChildren().add(l);
                saveButton.setVisible(false);
            });
            return;
        }
        else
        {
            Platform.runLater(()->saveButton.setVisible(true));
        }


        for(ExternalPlugin action : actions)
        {
            if(!action.isVisibleInServerSettingsPane())
                continue;


            Label headingLabel = new Label(action.getName());
            headingLabel.getStyleClass().add("plugins_settings_each_plugin_heading_label");

            HBox headerHBox = new HBox(headingLabel);
            headerHBox.getStyleClass().add("plugins_settings_each_plugin_header");


            if (action.getHelpLink()!=null)
            {
                Button helpButton = new Button();
                helpButton.getStyleClass().add("plugins_settings_each_plugin_help_button");
                FontIcon questionIcon = new FontIcon("fas-question");
                questionIcon.getStyleClass().add("plugins_settings_each_plugin_help_icon");
                helpButton.setGraphic(questionIcon);
                helpButton.setOnAction(event -> hostServices.showDocument(action.getHelpLink()));

                headerHBox.getChildren().addAll(SpaceFiller.horizontal() ,helpButton);
            }



            Label authorLabel = new Label(action.getAuthor());
            authorLabel.getStyleClass().add("plugins_settings_each_plugin_author_label");

            Label moduleLabel = new Label(action.getUniqueID());
            moduleLabel.getStyleClass().add("plugins_settings_each_plugin_unique_ID_label");

            Label versionLabel = new Label(I18N.getString("version", action.getVersion().getText()));
            versionLabel.getStyleClass().add("plugins_settings_each_plugin_version_label");

            VBox serverPropertiesVBox = new VBox();
            serverPropertiesVBox.getStyleClass().add("plugins_settings_each_plugin_server_properties_box");
            serverPropertiesVBox.setSpacing(10.0);

            List<Property> serverProperties = action.getServerProperties().get();

            ArrayList<UIPropertyBox> serverPropertyArrayList = new ArrayList<>();


            for(int j =0; j<serverProperties.size(); j++)
            {
                Property eachProperty = serverProperties.get(j);

                if(!eachProperty.isVisible())
                    continue;

                UIPropertyBox serverProperty = new UIPropertyBox(j, eachProperty, unsavedChanges);
                serverPropertyArrayList.add(serverProperty);
                serverPropertiesVBox.getChildren().add(serverProperty.getUINode());
            }

            PluginProperties pp = new PluginProperties(action.getUniqueID(), action.getName(), serverPropertyArrayList);

            pluginProperties.put(action.getUniqueID(), pp);


            Platform.runLater(()->
            {
                VBox vBox = new VBox();
                vBox.getStyleClass().add("plugins_settings_each_plugin_box");
                vBox.setSpacing(5.0);
                vBox.getChildren().addAll(headerHBox, authorLabel, moduleLabel, versionLabel, serverPropertiesVBox);

                if(action.getServerSettingsNodes()!=null)
                {
                    vBox.getChildren().addAll(action.getServerSettingsNodes());
                }

                if(action.getServerSettingsButtonBar()!=null)
                {
                    action.getServerSettingsButtonBar().getStyleClass().add("plugins_settings_each_plugin_button_bar");
                    HBox buttonBarHBox = new HBox(SpaceFiller.horizontal(), action.getServerSettingsButtonBar());
                    buttonBarHBox.getStyleClass().add("plugins_settings_each_plugin_button_bar_hbox");
                    vBox.getChildren().add(buttonBarHBox);
                }

                pluginsSettingsVBox.getChildren().add(vBox);

            });
        }
    }

    public void reloadPlugins() throws MinorException
    {
        for (PluginProperties pp : pluginProperties.values())
        {
            reloadPlugin(pp);
        }
    }

    public void reloadPlugin(String uniqueID) throws MinorException
    {
        reloadPlugin(pluginProperties.get(uniqueID));
    }

    public void reloadPlugin(PluginProperties pp)
    {
        pp.getUIPropertyBoxes().forEach(UIPropertyBox::reloadValue);
    }

    public class PluginProperties
    {
        private ArrayList<UIPropertyBox> UIPropertyBoxes;
        private String name;

        private String uniqueID;

        public PluginProperties(String uniqueID, String name, ArrayList<UIPropertyBox> UIPropertyBoxes)
        {
            this.uniqueID = uniqueID;
            this.name = name;
            this.UIPropertyBoxes = UIPropertyBoxes;
        }

        public String getUniqueID() {
            return uniqueID;
        }

        public String getName()
        {
            return name;
        }

        public ArrayList<UIPropertyBox> getUIPropertyBoxes() {
            return UIPropertyBoxes;
        }
    }
}
