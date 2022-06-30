package com.stream_pi.server.window.settings.general;

import java.util.Locale;

public record GeneralSettingsOthersRecord(Locale currentLanguageLocale, boolean minimiseToSystemTrayOnClose, boolean startOnBoot, boolean showAlertsPopup) {
}
