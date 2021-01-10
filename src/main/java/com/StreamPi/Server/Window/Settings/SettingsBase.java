package com.StreamPi.Server.Window.Settings;

import com.StreamPi.Server.Connection.ServerListener;
import com.StreamPi.Server.Window.ExceptionAndAlertHandler;
import com.StreamPi.ThemeAPI.Theme;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsBase extends VBox {


    private TabPane tabPane;

    private GeneralSettings generalSettings;
    private PluginsSettings pluginsSettings;
    private ThemesSettings themesSettings;
    private ClientsSettings clientsSettings;

    private Button closeButton;

    private HostServices hostServices;
    private ExceptionAndAlertHandler exceptionAndAlertHandler;

    public SettingsBase(HostServices hostServices, ExceptionAndAlertHandler exceptionAndAlertHandler,
                        ServerListener serverListener)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;
        this.hostServices = hostServices;

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab generalSettingsTab = new Tab("General");
        generalSettings = new GeneralSettings(exceptionAndAlertHandler, serverListener, hostServices);
        generalSettingsTab.setContent(generalSettings);

        Tab pluginsSettingsTab = new Tab("Plugins");
        pluginsSettings = new PluginsSettings(exceptionAndAlertHandler, hostServices);
        pluginsSettingsTab.setContent(pluginsSettings);

        Tab themesSettingsTab = new Tab("Themes");
        themesSettings = new ThemesSettings();
        themesSettingsTab.setContent(themesSettings);

        Tab clientsSettingsTab = new Tab("Clients");
        clientsSettings = new ClientsSettings(exceptionAndAlertHandler, serverListener);
        clientsSettingsTab.setContent(clientsSettings);


        Tab aboutTab = new Tab("About");
        aboutTab.setContent(new About(hostServices));

        tabPane.getTabs().addAll(generalSettingsTab, pluginsSettingsTab, themesSettingsTab, clientsSettingsTab, aboutTab);

        setAlignment(Pos.TOP_RIGHT);

        closeButton = new Button("Close");
        VBox.setMargin(closeButton, new Insets(10.0));

        getChildren().addAll(tabPane, closeButton);

        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    public void setDefaultTabToGeneral()
    {
        tabPane.getSelectionModel().selectFirst();
    }

    public Button getCloseButton()
    {
        return closeButton;
    }

    public GeneralSettings getGeneralSettings()
    {
        return generalSettings;
    }

    public PluginsSettings getPluginsSettings()
    {
        return pluginsSettings;
    }

    public ThemesSettings getThemesSettings()
    {
        return themesSettings;
    }

    public ClientsSettings getClientsSettings()
    {
        return clientsSettings;
    }

}
