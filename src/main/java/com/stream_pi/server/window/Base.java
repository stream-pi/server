package com.stream_pi.server.window;

import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.Main;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.firsttimeuse.FirstTimeUse;
import com.stream_pi.server.window.settings.SettingsBase;
import com.stream_pi.theme_api.Theme;
import com.stream_pi.theme_api.Themes;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.loggerhelper.StreamPiLogFallbackHandler;
import com.stream_pi.util.loggerhelper.StreamPiLogFileHandler;

import com.stream_pi.util.platform.Platform;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class Base extends StackPane implements ExceptionAndAlertHandler, ServerListener
{

    private Config config;
    private ServerInfo serverInfo;
    private Stage stage;
    private HostServices hostServices;
    private SettingsBase settingsBase;
    private DashboardBase dashboardBase;
    private StackPane alertStackPane;
    private StreamPiLogFileHandler logFileHandler = null;
    private StreamPiLogFallbackHandler logFallbackHandler = null;

    public FirstTimeUse firstTimeUse;

    private Logger logger = null;
    public Logger getLogger(){
        return logger;
    }

    public void setHostServices(HostServices hostServices)
    {
        this.hostServices = hostServices;
    }

    public HostServices getHostServices()
    {
        return hostServices;
    }

    @Override
    public void initLogger()
    {
        try
        {
            if(logFileHandler != null)
                return;

            closeLogger();
            logger = Logger.getLogger("com.stream_pi");

            if(new File(ServerInfo.getInstance().getPrePath()).getAbsoluteFile().getParentFile().canWrite())
            {
                String path = ServerInfo.getInstance().getPrePath()+"../stream-pi-server.log";

                logFileHandler = new StreamPiLogFileHandler(path);
                logger.addHandler(logFileHandler);
            }
            else
            {
                logFallbackHandler = new StreamPiLogFallbackHandler();
                logger.addHandler(logFallbackHandler);
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();

            logFallbackHandler = new StreamPiLogFallbackHandler();
            logger.addHandler(logFallbackHandler);
        }
    }

    public void closeLogger()
    {
        if(logFileHandler != null)
            logFileHandler.close();
        else if(logFallbackHandler != null)
            logFallbackHandler.close();
    }
    
    public void initBase() throws SevereException
    {
        stage = (Stage) getScene().getWindow();

        getStage().getIcons().clear();
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon256x256.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon48x48.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon32x32.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon24x24.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icon16x16.png"))));
        
        getStage().setMinWidth(720);
        getStage().setMinHeight(530);

        serverInfo = ServerInfo.getInstance();


        settingsBase = new SettingsBase(getHostServices(), this, this);
        settingsBase.prefWidthProperty().bind(widthProperty());
        settingsBase.prefHeightProperty().bind(heightProperty());

        dashboardBase = new DashboardBase(this, getHostServices());
        dashboardBase.prefWidthProperty().bind(widthProperty());
        dashboardBase.prefHeightProperty().bind(heightProperty());

        alertStackPane = new StackPane();
        alertStackPane.setOpacity(0);



        StreamPiAlert.setParent(alertStackPane);

        getChildren().clear();
        getChildren().addAll(alertStackPane);

        initLogger();

        checkPrePathDirectory();

        getChildren().addAll(settingsBase, dashboardBase);

        config = Config.getInstance();

        initThemes();

        stage.setWidth(config.getStartupWindowWidth());
        stage.setHeight(config.getStartupWindowHeight());

        dashboardBase.setDividerPositions(config.getRightDividerPositions());
        dashboardBase.getLeftSplitPane().setDividerPositions(config.getLeftDividerPositions());

        dashboardBase.toFront();
    }

    private void checkPrePathDirectory() throws SevereException
    {
        try
        {
            File filex = new File(ServerInfo.getInstance().getPrePath());

            if(!filex.exists())
            {
                boolean result = filex.mkdirs();
                if(result)
                {
                    Config.unzipToDefaultPrePath();

                    initLogger();
                }
                else
                {
                    setPrefSize(300,300);
                    clearStylesheets();
                    applyDefaultStylesheet();
                    applyDefaultIconsStylesheet();
                    applyGlobalDefaultStylesheet();
                    getStage().show();
                    throw new SevereException("No storage permission. Give it!");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(e.getMessage());
        }
    }

    public void initThemes() throws SevereException 
    {
        clearStylesheets();
        if(themes==null)
            registerThemes();
        applyDefaultStylesheet();
        applyDefaultTheme();
        applyDefaultIconsStylesheet();
        applyGlobalDefaultStylesheet();
    }

    public void applyGlobalDefaultStylesheet()
    {
        File globalCSSFile = new File(getConfig().getDefaultThemesPath()+"/global.css");
        if(globalCSSFile.exists())
        {
            getLogger().info("Found global default style sheet. Adding ...");
            getStylesheets().add(globalCSSFile.toURI().toString());
        }
    }

    @Override
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

    public void applyDefaultIconsStylesheet()
    {
        Font.loadFont(Main.class.getResourceAsStream("Roboto.ttf"), 13);
        getStylesheets().add(Main.class.getResource("default_icons.css").toExternalForm());
    }

    public DashboardBase getDashboardBase()
    {
        return dashboardBase;
    }

    public SettingsBase getSettingsBase()
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

        logger.info("... Theme applied successfully!");
    }

    public void clearStylesheets()
    {
        getStylesheets().clear();
    }

    Themes themes = null;
    public void registerThemes() throws SevereException
    {
        logger.info("Loading themes ...");

        themes = new Themes(getConfig().getDefaultThemesPath(), getConfig().getThemesPath(), getConfig().getCurrentThemeFullName(), serverInfo.getMinThemeSupportVersion());
        
        if(!themes.getErrors().isEmpty())
        {
            StringBuilder themeErrors = new StringBuilder();

            for(MinorException eachException : themes.getErrors())
            {
                themeErrors.append("\n * ").append(eachException.getMessage());
            }

            if(themes.getIsBadThemeTheCurrentOne())
            {
                if(getConfig().getCurrentThemeFullName().equals(getConfig().getDefaultCurrentThemeFullName()))
                {
                    throw new SevereException("Unable to get default theme ("+getConfig().getDefaultCurrentThemeFullName()+")\n" +
                            "Please restore the theme or reinstall.");
                }

                themeErrors.append("\n\nReverted to default theme! (").append(getConfig().getDefaultCurrentThemeFullName()).append(")");

                getConfig().setCurrentThemeFullName(getConfig().getDefaultCurrentThemeFullName());
                getConfig().save();
            }

            handleMinorException(new MinorException("Theme Loading issues", themeErrors.toString()));
        }
        logger.info("...Themes loaded successfully !");
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

        if(!foundTheme)
        {
            logger.info("Theme not found. reverting to light theme ...");
            try 
            {
                Config.getInstance().setCurrentThemeFullName("com.stream_pi.defaultlight");
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
