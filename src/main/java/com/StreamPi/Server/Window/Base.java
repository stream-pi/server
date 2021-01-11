package com.StreamPi.Server.Window;

import com.StreamPi.Server.Connection.ServerListener;
import com.StreamPi.Server.IO.Config;
import com.StreamPi.Server.Info.ServerInfo;
import com.StreamPi.Server.Main;
import com.StreamPi.Server.Window.Dashboard.*;
import com.StreamPi.Server.Window.Settings.SettingsBase;
import com.StreamPi.ThemeAPI.Theme;
import com.StreamPi.ThemeAPI.Themes;
import com.StreamPi.Util.Alert.StreamPiAlert;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.LoggerHelper.StreamPiLogFileHandler;

import javafx.application.HostServices;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.awt.PopupMenu;
import java.awt.MenuItem;

public abstract class Base extends StackPane implements ExceptionAndAlertHandler, ServerListener {

    private Config config;

    private ServerInfo serverInfo;

    private Stage stage;

    private HostServices hostServices;

    public Logger getLogger(){
        return logger;
    }

    private SettingsBase settingsBase;
    private DashboardBase dashboardBase;

    private StackPane alertStackPane;

    public void setHostServices(HostServices hostServices)
    {
        this.hostServices = hostServices;
    }

    public HostServices getHostServices()
    {
        return hostServices;
    }

    private Logger logger = null;
    private StreamPiLogFileHandler logFileHandler = null;
    
    public void initLogger() throws SevereException
    {
        try
        {
            if(logger != null)
                return;

            logger = Logger.getLogger("");
            logFileHandler = new StreamPiLogFileHandler(ServerInfo.getInstance().getPrePath()+"../streampi.log");
            logger.addHandler(logFileHandler);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new SevereException("Cant get logger started!");
        }
    }
    
    public void closeLogger()
    {
        if(logFileHandler != null)
            logFileHandler.close();
    }
    
    public void initBase() throws SevereException {
        initLogger();

        getChildren().clear();

        stage = (Stage) getScene().getWindow();

        getStage().getIcons().add(new Image(Main.class.getResourceAsStream("app_icon.png")));
        
        getStage().setMinWidth(500);
        getStage().setMinHeight(500);

        config = Config.getInstance();

        stage.setWidth(config.getStartupWindowWidth());
        stage.setHeight(config.getStartupWindowHeight());


        serverInfo = ServerInfo.getInstance();


        initThemes();

        settingsBase = new SettingsBase(getHostServices(), this, this);
        settingsBase.prefWidthProperty().bind(widthProperty());
        settingsBase.prefHeightProperty().bind(heightProperty());

        dashboardBase = new DashboardBase(this, getHostServices());
        dashboardBase.prefWidthProperty().bind(widthProperty());
        dashboardBase.prefHeightProperty().bind(heightProperty());

        alertStackPane = new StackPane();
        alertStackPane.setVisible(false);

        StreamPiAlert.setParent(alertStackPane);

        getChildren().addAll(settingsBase, dashboardBase, alertStackPane);

        dashboardBase.toFront();


    }

    public void initThemes() throws SevereException {
        clearStylesheets();

        registerThemes();
        applyDefaultStylesheet();
        applyDefaultTheme();
    }

    public Stage getStage()
    {
        return stage;
    }

    public void applyDefaultStylesheet()
    {
        logger.info("Applying default stylesheet ...");

        Font.loadFont(Main.class.getResourceAsStream("Roboto.ttf"), 13);
        getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

        logger.info("... Done!");
    }

    public DashboardBase getDashboardPane()
    {
        return dashboardBase;
    }

    public SettingsBase getSettingsPane()
    {
        return settingsBase;
    }


    public Config getConfig()
    {
        return config;
    }

    public ServerInfo getServerInfo()
    {
        return serverInfo;
    }

    private Theme currentTheme;
    public Theme getCurrentTheme()
    {
        return currentTheme;
    }

    public void applyTheme(Theme t)
    {
        logger.info("Applying theme '"+t.getFullName()+"' ...");

        if(t.getFonts() != null)
        {
            for(String fontFile : t.getFonts())
            {
                Font.loadFont(fontFile.replace("%20",""), 13);
            }
        }

        currentTheme = t;
        getStylesheets().addAll(t.getStylesheets());

        logger.info("... Done!");
    }

    public void clearStylesheets()
    {
        getStylesheets().clear();
    }

    Themes themes;
    public void registerThemes() throws SevereException
    {
        logger.info("Loading themes ...");
        themes = new Themes(getConfig().getThemesPath(), getConfig().getCurrentThemeFullName(), serverInfo.getMinThemeSupportVersion());

        if(themes.getErrors().size()>0)
        {
            StringBuilder themeErrors = new StringBuilder();

            for(MinorException eachException : themes.getErrors())
            {
                themeErrors.append("\n * ").append(eachException.getShortMessage());
            }

            if(themes.getIsBadThemeTheCurrentOne())
            {
                themeErrors.append("\n\nReverted to default theme! (").append(getConfig().getDefaultCurrentThemeFullName()).append(")");

                getConfig().setCurrentThemeFullName(getConfig().getDefaultCurrentThemeFullName());
                getConfig().save();
            }

            handleMinorException(new MinorException("Theme Loading issues", themeErrors.toString()));
        }


        logger.info("... Done!");
    }

    public Themes getThemes()
    {
        return themes;
    }

    public void applyDefaultTheme()
    {
        logger.info("Applying default theme ...");



        boolean foundTheme = false;
        for(Theme t: themes.getThemeList())
        {
            if(t.getFullName().equals(config.getCurrentThemeFullName()))
            {
                foundTheme = true;
                applyTheme(t);
                break;
            }
        }

        if(foundTheme)
            logger.info("... Done!");
        else
        {
            logger.info("Theme not found. reverting to light theme ...");
            try {
                Config.getInstance().setCurrentThemeFullName("com.StreamPi.DefaultLight");
                Config.getInstance().save();

                applyDefaultTheme();
            }
            catch (SevereException e)
            {
                handleSevereException(e);
            }
        }


    }


}
