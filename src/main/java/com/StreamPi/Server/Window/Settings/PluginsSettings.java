package com.StreamPi.Server.Window.Settings;

import com.StreamPi.Server.UIPropertyBox.UIPropertyBox;
import com.StreamPi.ActionAPI.ActionProperty.Property.ControlType;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.ActionProperty.Property.Type;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.Server.Action.NormalActionPlugins;
import com.StreamPi.Server.Connection.ServerListener;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.FormHelper.SpaceFiller;
import com.StreamPi.Util.FormHelper.SpaceFiller.FillerType;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
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

        setPadding(new Insets(10));

        pluginsSettingsVBox = new VBox();
        pluginsSettingsVBox.setSpacing(10.0);
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

                    NormalActionPlugins.getInstance().getActionFromIndex(pp.getIndex())
                            .getServerProperties().get()
                            .get(serverProperty.getIndex()).setRawValue(rawValue);
                }
            }


            NormalActionPlugins.getInstance().saveServerSettings();

            NormalActionPlugins.getInstance().initPlugins();
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

        List<NormalAction> actions = NormalActionPlugins.getInstance().getPlugins();

        System.out.println("asdasdasdasd"+actions.size());

        Platform.runLater(()-> pluginsSettingsVBox.getChildren().clear());

        if(actions.size() == 0)
        {
            Platform.runLater(()->{
                pluginsSettingsVBox.getChildren().add(new Label("No Plugins Installed."));
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
            NormalAction action = actions.get(i);

            if(!action.isVisibleInServerSettingsPane())
                continue;


            Label headingLabel = new Label(action.getName());
            headingLabel.getStyleClass().add("settings_plugins_each_action_heading");

            HBox headerHBox = new HBox(headingLabel, new SpaceFiller(FillerType.HBox));


            if (action.getRepo()!=null)
            {
                Button helpButton = new Button();
                FontIcon questionIcon = new FontIcon("fas-question");
                questionIcon.getStyleClass().add("dashboard_plugins_pane_action_help_icon");
                helpButton.setGraphic(questionIcon);

                
                helpButton.setOnAction(event -> {
                    hostServices.showDocument(action.getRepo());
                });

                headerHBox.getChildren().add(helpButton);
            }



            Label authorLabel = new Label(action.getAuthor());

            Label moduleLabel = new Label(action.getModuleName());

            Label versionLabel = new Label("Version : "+action.getVersion().getText());

            VBox serverPropertiesVBox = new VBox();
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

                HBox hBox = new HBox(label, new SpaceFiller(SpaceFiller.FillerType.HBox));
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



            Region region1 = new Region();
            region1.setPrefHeight(5);


            Platform.runLater(()->{
                VBox vBox = new VBox();
                vBox.setSpacing(5.0);
                vBox.getChildren().addAll(headerHBox, authorLabel, moduleLabel, versionLabel, serverPropertiesVBox);

                if(action.getButtonBar()!=null)
                    vBox.getChildren().add(new HBox(new SpaceFiller(SpaceFiller.FillerType.HBox), action.getButtonBar()));

                vBox.getChildren().add(region1);
                //vBox.setId(i+"");

                vBox.getStyleClass().add("settings_plugins_each_action");

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
