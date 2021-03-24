package com.stream_pi.server.window.settings;

import com.stream_pi.action_api.externalplugin.ExternalPlugin;
import com.stream_pi.server.uipropertybox.UIPropertyBox;
import com.stream_pi.action_api.actionproperty.property.ControlType;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.NormalAction;
import com.stream_pi.server.action.ExternalPlugins;
import com.stream_pi.server.connection.ServerListener;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.uihelper.SpaceFiller;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginsSettings extends VBox {

    private VBox pluginsSettingsVBox;

    private ServerListener serverListener;

    private Logger logger;

    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    private HostServices hostServices;

    public PluginsSettings(ExceptionAndAlertHandler exceptionAndAlertHandler, HostServices hostServices)
    {
        getStyleClass().add("plugins_settings");

        this.hostServices = hostServices;
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        pluginProperties = new ArrayList<>();
        logger = Logger.getLogger(PluginsSettings.class.getName());

        pluginsSettingsVBox = new VBox();
        pluginsSettingsVBox.getStyleClass().add("plugins_settings_vbox");
        pluginsSettingsVBox.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("plugins_settings_scroll_pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));

        pluginsSettingsVBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(25));
        scrollPane.setContent(pluginsSettingsVBox);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        setAlignment(Pos.TOP_CENTER);


        saveButton = new Button("Save");

        saveButton.setOnAction(event -> onSaveButtonClicked());


        HBox hBox = new HBox(saveButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(scrollPane, hBox);

    }

    private Button saveButton;

    public void onSaveButtonClicked()
    {
        try {
            //form validation
            StringBuilder finalErrors = new StringBuilder();

            for (PluginProperties p : pluginProperties)
            {
                StringBuilder errors = new StringBuilder();
                for(int j = 0; j < p.getServerPropertyUIBox().size(); j++)
                {
                    UIPropertyBox serverProperty = p.getServerPropertyUIBox().get(j);
                    Node controlNode = serverProperty.getControlNode();

                    if (serverProperty.getControlType() == ControlType.TEXT_FIELD)
                    {
                        String value = ((TextField) controlNode).getText();
                        if(serverProperty.getType() == Type.INTEGER)
                        {
                            try
                            {
                                Integer.parseInt(value);
                            }
                            catch (NumberFormatException e)
                            {
                                errors.append("        -> ").append(serverProperty.getDisplayName()).append(" must be integer.\n");
                            }
                        }
                        else
                        {
                            if(value.isBlank() && !serverProperty.isCanBeBlank())
                                errors.append("        -> ").append(serverProperty.getDisplayName()).append(" cannot be blank.\n");
                        }
                    }
                    else if(serverProperty.getControlType() == ControlType.TEXT_FIELD_MASKED)
                    {
                        String value = ((TextField) controlNode).getText();

                        if(value.isBlank() && !serverProperty.isCanBeBlank())
                            errors.append("        -> ").append(serverProperty.getDisplayName()).append(" cannot be blank.\n");
                    }
                }

                if(!errors.toString().isBlank())
                {
                    finalErrors.append("    * ").append(p.getName()).append("\n").append(errors.toString()).append("\n");
                }
            }

            if(!finalErrors.toString().isEmpty())
            {
                throw new MinorException("Form Validation Errors",
                        "Please rectify the following errors and try again \n"+finalErrors.toString());
            }

            //save
            for (PluginProperties pp : pluginProperties) {
                for (int j = 0; j < pp.getServerPropertyUIBox().size(); j++) {


                    UIPropertyBox serverProperty = pp.getServerPropertyUIBox().get(j);

                    String rawValue = serverProperty.getRawValue();

                    ExternalPlugins.getInstance().getActionFromIndex(pp.getIndex())
                            .getServerProperties().get()
                            .get(serverProperty.getIndex()).setRawValue(rawValue);
                }
            }


            ExternalPlugins.getInstance().saveServerSettings();

            ExternalPlugins.getInstance().initPlugins();
        }
        catch (MinorException e)
        {
            e.printStackTrace();
            exceptionAndAlertHandler.handleMinorException(e);
        }
    }

    private ArrayList<PluginProperties> pluginProperties;


    public void showPluginInitError()
    {
        Platform.runLater(()->{
            pluginsSettingsVBox.getChildren().add(new Label("Plugin init error. Resolve issues and restart."));
            saveButton.setVisible(false);
        });
    }

    public void loadPlugins() throws MinorException {

        pluginProperties.clear();

        List<ExternalPlugin> actions = ExternalPlugins.getInstance().getPlugins();

        Platform.runLater(()-> pluginsSettingsVBox.getChildren().clear());

        if(actions.size() == 0)
        {
            Platform.runLater(()->{
                Label l = new Label("No Plugins Installed.");
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


        for(int i = 0; i<actions.size(); i++)
        {
            ExternalPlugin action = actions.get(i);

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

            Label moduleLabel = new Label(action.getModuleName());
            moduleLabel.getStyleClass().add("plugins_settings_each_plugin_module_label");

            Label versionLabel = new Label("Version : "+action.getVersion().getText());
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


                Label label = new Label(eachProperty.getDisplayName());

                Region region = new Region();
                HBox.setHgrow(region, Priority.ALWAYS);

                HBox hBox = new HBox(label, SpaceFiller.horizontal());
                //hBox.setId(j+"");

                Node controlNode = null;

                if(eachProperty.getControlType() == ControlType.COMBO_BOX)
                {
                    ComboBox<String> comboBox = new ComboBox<>();
                    comboBox.getItems().addAll(eachProperty.getListValue());
                    comboBox.getSelectionModel().select(eachProperty.getSelectedIndex());
                    hBox.getChildren().add(comboBox);

                    controlNode = comboBox;
                }
                else if(eachProperty.getControlType() == ControlType.TEXT_FIELD)
                {
                    TextField textField = new TextField(eachProperty.getRawValue());

                    hBox.getChildren().add(textField);

                    controlNode = textField;
                }
                else if(eachProperty.getControlType() == ControlType.TEXT_FIELD_MASKED)
                {
                    PasswordField textField = new PasswordField();
                    textField.setText(eachProperty.getRawValue());

                    controlNode= textField;

                    hBox.getChildren().add(controlNode);
                }
                else if(eachProperty.getControlType() == ControlType.TOGGLE)
                {
                    ToggleButton toggleButton = new ToggleButton();
                    toggleButton.setSelected(eachProperty.getBoolValue());

                    if(eachProperty.getBoolValue())
                        toggleButton.setText("ON");
                    else
                        toggleButton.setText("OFF");

                    toggleButton.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                        if(t1)
                            toggleButton.setText("ON");
                        else
                            toggleButton.setText("OFF");
                    });

                    hBox.getChildren().add(toggleButton);

                    controlNode = toggleButton;
                }
                else if(eachProperty.getControlType() == ControlType.SLIDER_DOUBLE)
                {
                    Slider slider = new Slider();
                    slider.setValue(eachProperty.getDoubleValue());
                    slider.setMax(eachProperty.getMaxDoubleValue());
                    slider.setMin(eachProperty.getMinDoubleValue());

                    hBox.getChildren().add(slider);

                    controlNode = slider;
                }
                else if(eachProperty.getControlType() == ControlType.SLIDER_INTEGER)
                {
                    Slider slider = new Slider();
                    slider.setValue(eachProperty.getIntValue());

                    slider.setMax(eachProperty.getMaxIntValue());
                    slider.setMin(eachProperty.getMinIntValue());
                    slider.setBlockIncrement(1.0);
                    slider.setSnapToTicks(true);

                    hBox.getChildren().add(slider);

                    controlNode = slider;
                }


                UIPropertyBox serverProperty = new UIPropertyBox(j, eachProperty.getDisplayName(), controlNode, eachProperty.getControlType(), eachProperty.getType(), eachProperty.isCanBeBlank());

                serverPropertyArrayList.add(serverProperty);

                serverPropertiesVBox.getChildren().add(hBox);

            }

            PluginProperties pp = new PluginProperties(i, serverPropertyArrayList, action.getName());

            pluginProperties.add(pp);



            Platform.runLater(()->{
                VBox vBox = new VBox();
                vBox.getStyleClass().add("plugins_settings_each_plugin_box");
                vBox.setSpacing(5.0);
                vBox.getChildren().addAll(headerHBox, authorLabel, moduleLabel, versionLabel, serverPropertiesVBox);

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

    public class PluginProperties
    {
        private int index;
        private ArrayList<UIPropertyBox> serverProperty;
        private String name;

        public PluginProperties(int index, ArrayList<UIPropertyBox> serverProperty, String name)
        {
            this.index = index;
            this.serverProperty = serverProperty;
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public ArrayList<UIPropertyBox> getServerPropertyUIBox() {
            return serverProperty;
        }
    }
}
