package com.stream_pi.server.window.settings.general;

import java.util.Locale;

// TODO: Refactor args
public record GeneralSettingsRecord(String serverName, int port, double actionGridActionDisplayTextFontSize,
                                   String pluginsPath, String themesPath,
                                   double actionGridActionSize,
                                   boolean actionGridUseSameActionSizeAsProfile, boolean actionGridUseSameActionGapAsProfile,
                                   boolean actionGridUseSameActionDisplayTextFontSizeAsProfile,
                                   double actionGridActionGap,
                                   boolean minimiseToSystemTrayOnClose, boolean showAlertsPopup, boolean startOnBoot,
                                   boolean soundOnActionClickedStatus, String soundOnActionClickedFilePath,
                                   String IP, Locale currentLanguageLocale)
{

}

