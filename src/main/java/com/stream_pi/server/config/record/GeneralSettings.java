package com.stream_pi.server.config.record;

// TODO: This should be moved to Config and not only just GeneralSettings
/*public record GeneralSettings(String serverName, int port, double actionGridActionDisplayTextFontSize,
                              String pluginsPath, String themesPath,
                              double actionGridActionSize,
                              boolean actionGridUseSameActionSizeAsProfile, boolean actionGridUseSameActionGapAsProfile,
                              boolean actionGridUseSameActionDisplayTextFontSizeAsProfile,
                              double actionGridActionGap,
                              boolean minimiseToSystemTrayOnClose, boolean showAlertsPopup, boolean startOnBoot,
                              boolean soundOnActionClickedStatus, String soundOnActionClickedFilePath,
                              String IP, Locale currentLanguageLocale)
{

}*/

public record GeneralSettings(ConnectionSettings connection, ActionGridSettings actionGrid, LocationsSettings locations, SoundOnActionClickedSettings soundOnActionClicked, OthersSettings others)
{

}

