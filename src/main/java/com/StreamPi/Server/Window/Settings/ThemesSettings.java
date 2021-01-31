package com.StreamPi.Server.Window.Settings;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.ActionProperty.Property.ControlType;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.Server.Action.NormalActionPlugins;
import com.StreamPi.Server.Connection.ServerListener;
import com.StreamPi.Server.Controller.Controller;
import com.StreamPi.Server.IO.Config;
import com.StreamPi.ThemeAPI.Theme;
import com.StreamPi.ThemeAPI.Themes;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.FormHelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ThemesSettings extends VBox
{
    private VBox themesSettingsVBox;

    private Controller controller;

    private Logger logger;
    private HostServices hostServices;

    public ThemesSettings(HostServices hostServices)
    {
        this.hostServices = hostServices;
        getStyleClass().add("themes_settings");
        logger = Logger.getLogger(ThemesSettings.class.getName());

        setPadding(new Insets(10));

        themesSettingsVBox = new VBox();
        themesSettingsVBox.setSpacing(10.0);
        themesSettingsVBox.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("themes_settings_scroll_pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.maxWidthProperty().bind(widthProperty().multiply(0.8));

        themesSettingsVBox.prefWidthProperty().bind(scrollPane.widthProperty().subtract(10));
        scrollPane.setContent(themesSettingsVBox);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        setAlignment(Pos.TOP_CENTER);

        getChildren().addAll(scrollPane);

        toggleButtons = new ArrayList<>();

    }

    public void setController(Controller controller)
    {
        this.controller = controller;
    }

    private Themes themes;
    private String currentThemeFullName;

    public void setThemes(Themes themes)
    {
        this.themes = themes;
    }

    public void setCurrentThemeFullName(String currentThemeFullName)
    {
        this.currentThemeFullName = currentThemeFullName;
    }

    private ArrayList<ToggleButton> toggleButtons;

    public void loadThemes()
    {
        toggleButtons.clear();

        Platform.runLater(()-> themesSettingsVBox.getChildren().clear());


        for(int i = 0; i<themes.getThemeList().size(); i++)
        {
            Theme theme = themes.getThemeList().get(i);


            Label shortNameLabel = new Label(theme.getShortName());
            shortNameLabel.getStyleClass().add("settings_themes_each_theme_heading");

            Label authorLabel = new Label(theme.getAuthor());

            Label fullNameLabel = new Label(theme.getFullName());


            HBox topRowHBox = new HBox(shortNameLabel);

            if(theme.getWebsite() != null)
            {
                Button helpButton = new Button();
                FontIcon questionIcon = new FontIcon("fas-question");
                questionIcon.getStyleClass().add("settings_themes_theme_help_icon");
                helpButton.setGraphic(questionIcon);
                helpButton.setOnAction(event -> hostServices.showDocument(theme.getWebsite()));

                topRowHBox.getChildren().addAll(new SpaceFiller(SpaceFiller.FillerType.HBox), helpButton);
            }



            Label versionLabel = new Label("Version : "+theme.getVersion().getText());

            ToggleButton toggleButton = new ToggleButton();

            toggleButton.setSelected(theme.getFullName().equals(currentThemeFullName));
            toggleButton.setId(theme.getFullName());


            if(theme.getFullName().equals(currentThemeFullName))
            {
                toggleButton.setText("ON");
                toggleButton.setSelected(true);
                toggleButton.setDisable(true);
            }
            else
            {
                toggleButton.setText("OFF");
            }

            toggleButton.setOnAction(event -> {
                ToggleButton toggleButton1 = (ToggleButton) event.getSource();


                toggleButton.setText("ON");

                try {
                    Config.getInstance().setCurrentThemeFullName(toggleButton1.getId());
                    Config.getInstance().save();


                    for(ToggleButton toggleButton2 : toggleButtons)
                    {
                        if(toggleButton2.getId().equals(Config.getInstance().getCurrentThemeFullName()))
                        {
                            toggleButton2.setDisable(true);
                            toggleButton2.setText("ON");
                            toggleButton2.setSelected(true);
                        }
                        else
                        {
                            toggleButton2.setDisable(false);
                            toggleButton2.setText("OFF");
                            toggleButton2.setSelected(false);
                        }
                    }

                    controller.initThemes();
                }
                catch (SevereException e)
                {
                    controller.handleSevereException(e);
                }
            });

            HBox hBox = new HBox(toggleButton);

            Region region1 = new Region();
            region1.setPrefHeight(5);

            hBox.setAlignment(Pos.TOP_RIGHT);

            VBox vBox = new VBox(topRowHBox, authorLabel, versionLabel, fullNameLabel, hBox, region1);
            vBox.setSpacing(5.0);


            vBox.getStyleClass().add("settings_themes_each_theme");

            Platform.runLater(()->themesSettingsVBox.getChildren().add(vBox));


            toggleButtons.add(toggleButton);
        }

    }
}
