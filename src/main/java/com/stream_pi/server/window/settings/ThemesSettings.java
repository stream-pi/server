package com.stream_pi.server.window.settings;

import com.stream_pi.server.controller.Controller;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.io.Config;
import com.stream_pi.theme_api.Theme;
import com.stream_pi.theme_api.Themes;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.uihelper.SpaceFiller;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
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

        themesSettingsVBox = new VBox();
        themesSettingsVBox.getStyleClass().add("themes_settings_vbox");
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

        toggleSwitches = new ArrayList<>();

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

    private ArrayList<ToggleSwitch> toggleSwitches;

    public void loadThemes()
    {
        toggleSwitches.clear();

        Platform.runLater(()-> themesSettingsVBox.getChildren().clear());


        for(int i = 0; i<themes.getThemeList().size(); i++)
        {
            Theme theme = themes.getThemeList().get(i);

            Label shortNameLabel = new Label(theme.getShortName());
            shortNameLabel.getStyleClass().add("themes_settings_each_theme_heading");

            Label authorLabel = new Label(theme.getAuthor());
            authorLabel.getStyleClass().add("themes_settings_each_theme_author_label");

            Label fullNameLabel = new Label(theme.getFullName());
            fullNameLabel.getStyleClass().add("themes_settings_each_theme_full_name_label");

            HBox topRowHBox = new HBox(shortNameLabel);
            topRowHBox.getStyleClass().add("themes_settings_each_theme_header");

            Label versionLabel = new Label(I18N.getString("window.settings.ThemesSettings.version", theme.getVersion().getText()));
            versionLabel.getStyleClass().add("themes_settings_each_theme_version_label");

            if(theme.getWebsite() != null)
            {
                Button helpButton = new Button();
                helpButton.getStyleClass().add("themes_settings_each_theme_help_button");
                FontIcon questionIcon = new FontIcon("fas-question");
                questionIcon.getStyleClass().add("themes_settings_each_theme_help_icon");
                helpButton.setGraphic(questionIcon);
                helpButton.setOnAction(event -> hostServices.showDocument(theme.getWebsite()));

                topRowHBox.getChildren().addAll(SpaceFiller.horizontal(), helpButton);
            }

            ToggleSwitch toggleSwitch = new ToggleSwitch();
            toggleSwitch.getStyleClass().add("themes_settings_each_theme_toggle_switch");

            toggleSwitch.setSelected(theme.getFullName().equals(currentThemeFullName));
            toggleSwitch.setId(theme.getFullName());


            if(theme.getFullName().equals(currentThemeFullName))
            {
                toggleSwitch.setSelected(true);
                toggleSwitch.setDisable(true);
            }


            toggleSwitch.setOnMouseClicked(event -> {
                ToggleSwitch toggleSwitch1 = (ToggleSwitch) event.getSource();

                try
                {
                    Config.getInstance().setCurrentThemeFullName(toggleSwitch1.getId());
                    Config.getInstance().save();

                    for(ToggleSwitch toggleSwitch2 : toggleSwitches)
                    {
                        if(toggleSwitch2.getId().equals(Config.getInstance().getCurrentThemeFullName()))
                        {
                            toggleSwitch2.setDisable(true);
                            toggleSwitch2.setSelected(true);
                        }
                        else
                        {
                            toggleSwitch2.setDisable(false);
                            toggleSwitch2.setSelected(false);
                        }
                    }

                    controller.initThemes();
                }
                catch (SevereException e)
                {
                    controller.handleSevereException(e);
                }
            });

            HBox hBox = new HBox(toggleSwitch);
            hBox.getStyleClass().add("themes_settings_each_theme_toggle_button_parent");

            hBox.setAlignment(Pos.TOP_RIGHT);

            VBox vBox = new VBox(topRowHBox, authorLabel, fullNameLabel, versionLabel, hBox);


            vBox.getStyleClass().add("theme_settings_each_theme_box");

            Platform.runLater(()->themesSettingsVBox.getChildren().add(vBox));


            toggleSwitches.add(toggleSwitch);
        }

    }
}
