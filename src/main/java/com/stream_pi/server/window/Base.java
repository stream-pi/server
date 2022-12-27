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
package com.stream_pi.server.window;

import com.stream_pi.action_api.ActionAPI;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.Main;
import com.stream_pi.server.window.dashboard.DashboardBase;
import com.stream_pi.server.window.firsttimeuse.FirstTimeUse;
import com.stream_pi.server.window.settings.SettingsBase;
import com.stream_pi.theme_api.Theme;
import com.stream_pi.theme_api.ThemeAPI;
import com.stream_pi.theme_api.Themes;
import com.stream_pi.util.Util;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.iohelper.IOHelper;
import com.stream_pi.util.loggerhelper.StreamPiLogFallbackHandler;
import com.stream_pi.util.loggerhelper.StreamPiLogFileHandler;

import javafx.application.HostServices;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.Locale;
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
            logger = Logger.getLogger("");

            if(new File(ServerInfo.getInstance().getPrePath()).getAbsoluteFile().getParentFile().canWrite())
            {
                String path = ServerInfo.getInstance().getPrePath() + File.separator + "stream-pi-server.log";

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

    public boolean isOpenFirstTime = false;

    public void initBase() throws SevereException
    {
        I18N.initAvailableLanguages();

        stage = (Stage) getScene().getWindow();

        getStage().getIcons().clear();
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/256x256.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/48x48.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/32x32.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/24x24.png"))));
        getStage().getIcons().add(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("icons/16x16.png"))));
        
        getStage().setMinWidth(700);
        getStage().setMinHeight(500);

        serverInfo = ServerInfo.getInstance();


        alertStackPane = new StackPane();
        alertStackPane.setCache(true);
        alertStackPane.setCacheHint(CacheHint.SPEED);

        StreamPiAlert.setParent(alertStackPane);

        getChildren().clear();
        getChildren().addAll(alertStackPane);

        initLogger();

        checkPrePathDirectory();

        Config.initialise();

        config = Config.getInstance();

        initI18n();

        settingsBase = new SettingsBase(getHostServices(), this, this);
        settingsBase.prefWidthProperty().bind(widthProperty());
        settingsBase.prefHeightProperty().bind(heightProperty());

        dashboardBase = new DashboardBase(this, getHostServices());
        dashboardBase.prefWidthProperty().bind(widthProperty());
        dashboardBase.prefHeightProperty().bind(heightProperty());

        getChildren().addAll(settingsBase, dashboardBase);

        initThemes();

        if (!isOpenFirstTime)
        {
            stage.setWidth(config.getStartupWindowWidth());
            stage.setHeight(config.getStartupWindowHeight());

            isOpenFirstTime = true;
        }

        dashboardBase.getSplitPane().setDividerPositions(config.getRightDividerPositions());
        dashboardBase.getLeftSplitPane().setDividerPositions(config.getLeftDividerPositions());

        dashboardBase.toFront();
    }

    private void initI18n() throws SevereException
    {
        if (I18N.isLanguageAvailable(config.getCurrentLanguageLocale()))
        {
            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(I18N.BASE_LOCALE);
            // This sets the local to Locale en (fallback locale)
            // This is done because the proper way of removing fallback locales is not available on Java 9+
            // As ResourceBundle.Control is not supported on modular projects.


            Util.initI18n(config.getCurrentLanguageLocale());
            ActionAPI.initI18n(config.getCurrentLanguageLocale());
            ThemeAPI.initI18n(config.getCurrentLanguageLocale());
            I18N.init(config.getCurrentLanguageLocale());

            Locale.setDefault(defaultLocale); // Reset locale back to defaults ...
        }
        else
        {
            getLogger().warning("No translation available for locale : "+config.getCurrentLanguageLocale().toString());
            getLogger().warning("Setting it to base ...");
            getConfig().setCurrentLanguageLocale(I18N.BASE_LOCALE);
            getConfig().save();
            initI18n();
        }
    }

    private void checkPrePathDirectory() throws SevereException
    {
        File serverDataFolder = new File(getServerInfo().getPrePath());

        if (serverDataFolder.exists())
        {
            if (new File(getServerInfo().getPrePath()+"config.xml").exists())
            {
                Config tempConfig = new Config();

                if (tempConfig.getVersion() == null || tempConfig.getVersion().getMajor() != getServerInfo().getVersion().getMajor())
                {
                    IOHelper.deleteFile(getServerInfo().getPrePath(), false);
                }
            }
            else
            {
                IOHelper.deleteFile(getServerInfo().getPrePath(), false);
            }
        }


        if (!serverDataFolder.exists())
        {
            try
            {
                Config.unzipToDefaultPrePath();
                initLogger();
            }
            catch (MinorException e)
            {
                throwStoragePermErrorAlert(e.getMessage());
            }
        }
    }

    private void throwStoragePermErrorAlert(String msg) throws SevereException
    {
        setPrefSize(300,300);
        clearStylesheets();
        applyDefaultStylesheet();
        applyDefaultIconsStylesheet();
        applyGlobalDefaultStylesheet();
        getStage().show();
        throw new SevereException(msg);
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

        getStylesheets().add(Objects.requireNonNull(Main.class.getResource("style.css")).toExternalForm());

        logger.info("... Done!");
    }

    public void applyDefaultIconsStylesheet()
    {
        getStylesheets().add(Objects.requireNonNull(Main.class.getResource("default_icons.css")).toExternalForm());
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

        themes = new Themes(getConfig().getDefaultThemesPath(), getConfig().getThemesPath(), getConfig().getCurrentThemeFullName());
        
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
                    throw new SevereException(I18N.getString("window.Base.defaultThemeCorrupt", getConfig().getDefaultCurrentThemeFullName()));
                }

                themeErrors.append("\n\n").append(I18N.getString("window.Base.revertedToDefaultTheme", getConfig().getDefaultCurrentThemeFullName()));

                getConfig().setCurrentThemeFullName(getConfig().getDefaultCurrentThemeFullName());
                getConfig().save();
            }

            handleMinorException(new MinorException(I18N.getString("window.Base.failedToLoadThemes"), themeErrors.toString()));
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
