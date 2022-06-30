package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.Main;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.info.StartupFlags;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.server.window.GlobalExceptionAndAlertHandler;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.platform.PlatformType;
import com.stream_pi.util.startonboot.StartOnBoot;

import java.awt.*;
import java.io.File;
import java.util.Locale;

public class GeneralSettingsModel
{
    private GeneralSettingsRecord currentSettings;
    private final GlobalExceptionAndAlertHandler exceptionAndAlertHandler = GlobalExceptionAndAlertHandler.getInstance();

    private ServerListener serverListener;
    public GeneralSettingsModel(ServerListener serverListener)
    {
        this.serverListener = serverListener;
    }
    public void loadSettings()
    {
        Config config = Config.getInstance();

        currentSettings = new GeneralSettingsRecord(config.getServerName(), config.getPort(), config.getActionGridActionDisplayTextFontSize(),
                config.getPluginsPath(), config.getThemesPath(),
                config.getActionGridActionSize(),
                config.getActionGridUseSameActionSizeAsProfile(), config.getActionGridUseSameActionGapAsProfile(),
                config.getActionGridUseSameActionDisplayTextFontSizeAsProfile(),
                config.getActionGridActionGap(),
                config.getMinimiseToSystemTrayOnClose(), config.isShowAlertsPopup(), config.isStartOnBoot(),
                config.getSoundOnActionClickedStatus(), config.getSoundOnActionClickedFilePath(),
                config.getIP(), config.getCurrentLanguageLocale());
    }

    public GeneralSettingsRecord getCurrentSettings()
    {
        return currentSettings;
    }

    public void saveSettings(GeneralSettingsRecord newSettingsRecord) throws SevereException
    {
        boolean minimiseToSystemTrayOnClose = newSettingsRecord.minimiseToSystemTrayOnClose();
        boolean showAlertsPopup = newSettingsRecord.showAlertsPopup();
        boolean startOnBoot = newSettingsRecord.startOnBoot();

        boolean soundOnActionClicked = newSettingsRecord.soundOnActionClickedStatus();


        Config config = Config.getInstance();

        boolean actionGridUseSameActionSizeAsProfile = newSettingsRecord.actionGridUseSameActionSizeAsProfile();
        boolean actionGridUseSameActionGapAsProfile = newSettingsRecord.actionGridUseSameActionGapAsProfile();
        boolean actionGridUseSameActionDisplayTextFontSizeAsProfile = newSettingsRecord.actionGridUseSameActionDisplayTextFontSizeAsProfile();



        config.setIP(newSettingsRecord.IP());

        config.setCurrentLanguageLocale(newSettingsRecord.currentLanguageLocale());


        if(config.isStartOnBoot() != startOnBoot)
        {
            StartOnBoot startAtBoot = new StartOnBoot(PlatformType.SERVER, ServerInfo.getInstance().getPlatform(),
                    Main.class.getProtectionDomain().getCodeSource().getLocation(),
                    StartupFlags.APPEND_PATH_BEFORE_RUNNER_FILE_TO_OVERCOME_JPACKAGE_LIMITATION);

            if(startOnBoot)
            {
                try
                {
                    startAtBoot.create(StartupFlags.RUNNER_FILE_NAME, StartupFlags.generateRuntimeArgumentsForStartOnBoot());
                }
                catch (MinorException e)
                {
                    exceptionAndAlertHandler.handleMinorException(e);
                    startOnBoot = false;
                }
            }
            else
            {
                try
                {
                    boolean result = startAtBoot.delete();
                    if(!result)
                        new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.unableToDeleteStarterFile"), StreamPiAlertType.ERROR).show();
                }
                catch (MinorException e)
                {
                    exceptionAndAlertHandler.handleMinorException(e);
                }
            }
        }

        if(minimiseToSystemTrayOnClose)
        {
            if(!SystemTray.isSupported())
            {
                StreamPiAlert alert = new StreamPiAlert(I18N.getString("window.settings.GeneralSettings.traySystemNotSupported"), StreamPiAlertType.ERROR);
                alert.show();

                minimiseToSystemTrayOnClose = false;
            }
        }

        config.setServerName(newSettingsRecord.serverName());
        config.setPort(newSettingsRecord.port());
        config.setActionGridActionGap(newSettingsRecord.actionGridActionGap());
        config.setActionGridActionSize(newSettingsRecord.actionGridActionSize());
        config.setActionGridActionDisplayTextFontSize(newSettingsRecord.actionGridActionDisplayTextFontSize());
        config.setPluginsPath(newSettingsRecord.pluginsPath());
        config.setThemesPath(newSettingsRecord.themesPath());

        config.setActionGridUseSameActionGapAsProfile(actionGridUseSameActionGapAsProfile);
        config.setActionGridUseSameActionSizeAsProfile(actionGridUseSameActionSizeAsProfile);
        config.setActionGridUseSameActionDisplayTextFontSizeAsProfile(actionGridUseSameActionDisplayTextFontSizeAsProfile);


        config.setMinimiseToSystemTrayOnClose(minimiseToSystemTrayOnClose);
        StreamPiAlert.setIsShowPopup(showAlertsPopup);
        config.setShowAlertsPopup(showAlertsPopup);
        config.setStartupOnBoot(startOnBoot);


        config.setSoundOnActionClickedStatus(soundOnActionClicked);
        config.setSoundOnActionClickedFilePath(newSettingsRecord.soundOnActionClickedFilePath());

        config.save();

        if(soundOnActionClicked)
        {
            serverListener.initSoundOnActionClicked();
        }

        loadSettings();
    }

    public boolean shouldServerBeReloaded(GeneralSettingsRecord newSettingsRecord)
    {
        boolean reload = false;

        if(!currentSettings.serverName().equals(newSettingsRecord.serverName()))
        {
            reload = true;
        }

        if(currentSettings.port()!=newSettingsRecord.port())
        {
            reload = true;
        }

        if(!currentSettings.pluginsPath().equals(newSettingsRecord.pluginsPath()))
        {
            reload = true;
        }

        if(!currentSettings.themesPath().equals(newSettingsRecord.themesPath()))
        {
            reload = true;
        }

        if (!currentSettings.IP().equals(newSettingsRecord.IP()))
        {
            reload = true;
        }

        if (!currentSettings.currentLanguageLocale().equals(newSettingsRecord.currentLanguageLocale()))
        {
            reload = true;
        }

        return reload;
    }

    public boolean shouldServerDashboardBeReloaded(GeneralSettingsRecord newSettingsRecord)
    {
        boolean reload = false;

        if(currentSettings.actionGridActionSize() != newSettingsRecord.actionGridActionSize())
        {
            reload = true;
        }

        if(currentSettings.actionGridUseSameActionSizeAsProfile() != newSettingsRecord.actionGridUseSameActionSizeAsProfile())
        {
            reload = true;
        }


        if(currentSettings.actionGridActionGap() != newSettingsRecord.actionGridActionGap())
        {
            reload = true;
        }

        if(currentSettings.actionGridUseSameActionGapAsProfile() != newSettingsRecord.actionGridUseSameActionGapAsProfile())
        {
            reload = true;
        }

        if (currentSettings.actionGridUseSameActionDisplayTextFontSizeAsProfile() != newSettingsRecord.actionGridUseSameActionDisplayTextFontSizeAsProfile())
        {
            reload = true;
        }


        return reload;
    }
}
