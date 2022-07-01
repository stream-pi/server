package com.stream_pi.server.window.settings.general;

import com.stream_pi.server.Main;
import com.stream_pi.server.controller.ServerListener;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.server.info.StartupFlags;
import com.stream_pi.server.config.Config;
import com.stream_pi.server.window.GlobalExceptionAndAlertHandler;
import com.stream_pi.server.config.record.*;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.platform.PlatformType;
import com.stream_pi.util.startonboot.StartOnBoot;

public class GeneralSettingsModel
{
    private GeneralSettings currentSettings;
    private final GlobalExceptionAndAlertHandler exceptionAndAlertHandler = GlobalExceptionAndAlertHandler.getInstance();

    private ServerListener serverListener;
    public GeneralSettingsModel(ServerListener serverListener)
    {
        this.serverListener = serverListener;
    }
    public void loadSettings()
    {
        Config config = Config.getInstance();

        /*currentSettings = new GeneralSettings(config.getServerName(), config.getPort(), config.getActionGridActionDisplayTextFontSize(),
                config.getPluginsPath(), config.getThemesPath(),
                config.getActionGridActionSize(),
                config.getActionGridUseSameActionSizeAsProfile(), config.getActionGridUseSameActionGapAsProfile(),
                config.getActionGridUseSameActionDisplayTextFontSizeAsProfile(),
                config.getActionGridActionGap(),
                config.getMinimiseToSystemTrayOnClose(), config.isShowAlertsPopup(), config.isStartOnBoot(),
                config.getSoundOnActionClickedStatus(), config.getSoundOnActionClickedFilePath(),
                config.getIP(), config.getCurrentLanguageLocale());*/

        currentSettings = new GeneralSettings(
                new ConnectionSettings(config.getServerName(), config.getPort(), config.getIP()),
                new ActionGridSettings(
                        config.getActionGridActionSize(), config.getActionGridUseSameActionSizeAsProfile(),
                        config.getActionGridActionGap(), config.getActionGridUseSameActionGapAsProfile(),
                        config.getActionGridActionDisplayTextFontSize(),config.getActionGridUseSameActionDisplayTextFontSizeAsProfile()
                ),
                new LocationsSettings(config.getPluginsPath(), config.getThemesPath()),
                new SoundOnActionClickedSettings(config.getSoundOnActionClickedFilePath(), config.getSoundOnActionClickedStatus()),
                new OthersSettings(config.getCurrentLanguageLocale(), config.getMinimiseToSystemTrayOnClose(), config.isStartOnBoot(), config.isShowAlertsPopup())
        );
    }

    public GeneralSettings getCurrentSettings()
    {
        return currentSettings;
    }

    public void saveSettings(GeneralSettings newSettings) throws SevereException
    {
        Config config = Config.getInstance();

        ConnectionSettings connectionSettings = newSettings.connection();
        config.setServerName(connectionSettings.serverName());
        config.setPort(connectionSettings.port());
        config.setIP(connectionSettings.IP());

        ActionGridSettings actionGridSettings = newSettings.actionGrid();
        config.setActionGridActionSize(actionGridSettings.actionGridActionSize());
        config.setActionGridUseSameActionSizeAsProfile(actionGridSettings.actionGridUseSameActionSizeAsProfile());
        config.setActionGridActionGap(actionGridSettings.actionGridActionGap());
        config.setActionGridUseSameActionGapAsProfile(actionGridSettings.actionGridUseSameActionGapAsProfile());
        config.setActionGridActionDisplayTextFontSize(actionGridSettings.actionGridActionDisplayTextFontSize());
        config.setActionGridUseSameActionDisplayTextFontSizeAsProfile(actionGridSettings.actionGridUseSameActionDisplayTextFontSizeAsProfile());

        LocationsSettings locationsSettings = newSettings.locations();
        config.setPluginsPath(locationsSettings.pluginsPath());
        config.setThemesPath(locationsSettings.themesPath());

        SoundOnActionClickedSettings soundOnActionClickedSettings = newSettings.soundOnActionClicked();
        config.setSoundOnActionClickedFilePath(soundOnActionClickedSettings.soundOnActionClickedFilePath());
        boolean soundOnActionClicked = soundOnActionClickedSettings.soundOnActionClickedStatus();
        config.setSoundOnActionClickedStatus(soundOnActionClicked);
        if(soundOnActionClicked)
        {
            serverListener.initSoundOnActionClicked();
        }


        OthersSettings othersSettings = newSettings.others();
        config.setCurrentLanguageLocale(othersSettings.currentLanguageLocale());
        config.setMinimiseToSystemTrayOnClose(othersSettings.minimiseToSystemTrayOnClose());

        boolean startOnBoot = othersSettings.startOnBoot();
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
        config.setStartupOnBoot(startOnBoot);

        boolean showAlertsPopup = othersSettings.showAlertsPopup();
        StreamPiAlert.setIsShowPopup(showAlertsPopup);
        config.setShowAlertsPopup(showAlertsPopup);

        config.save();

        loadSettings();
    }

    public boolean shouldServerBeReloaded(GeneralSettings newSettings)
    {
        boolean reload = false;

        ConnectionSettings currentConnectionSettings = currentSettings.connection();
        ConnectionSettings newConnectionSettings = newSettings.connection();

        if(!currentConnectionSettings.serverName().equals(newConnectionSettings.serverName()))
        {
            reload = true;
        }

        if(currentConnectionSettings.port()!=newConnectionSettings.port())
        {
            reload = true;
        }

        if (!currentConnectionSettings.IP().equals(newConnectionSettings.IP()))
        {
            reload = true;
        }

        LocationsSettings currentLocationsSettings = currentSettings.locations();
        LocationsSettings newLocationsSettings = newSettings.locations();

        if(!currentLocationsSettings.pluginsPath().equals(newLocationsSettings.pluginsPath()))
        {
            reload = true;
        }

        if(!currentLocationsSettings.themesPath().equals(newLocationsSettings.themesPath()))
        {
            reload = true;
        }

        if (!currentSettings.others().currentLanguageLocale().equals(newSettings.others().currentLanguageLocale()))
        {
            reload = true;
        }

        return reload;
    }

    public boolean shouldServerDashboardBeReloaded(GeneralSettings newSettings)
    {
        boolean reload = false;

        ActionGridSettings currentActionGridSettings = currentSettings.actionGrid();
        ActionGridSettings newActionGridSettings = newSettings.actionGrid();

        if(currentActionGridSettings.actionGridActionSize() != newActionGridSettings.actionGridActionSize())
        {
            reload = true;
        }

        if(currentActionGridSettings.actionGridUseSameActionSizeAsProfile() != newActionGridSettings.actionGridUseSameActionSizeAsProfile())
        {
            reload = true;
        }

        if(currentActionGridSettings.actionGridActionGap() != newActionGridSettings.actionGridActionGap())
        {
            reload = true;
        }

        if(currentActionGridSettings.actionGridUseSameActionGapAsProfile() != newActionGridSettings.actionGridUseSameActionGapAsProfile())
        {
            reload = true;
        }

        if (currentActionGridSettings.actionGridUseSameActionDisplayTextFontSizeAsProfile() != newActionGridSettings.actionGridUseSameActionDisplayTextFontSizeAsProfile())
        {
            reload = true;
        }


        return reload;
    }
}
