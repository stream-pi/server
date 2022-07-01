package com.stream_pi.server.config.record;

import java.util.Locale;

public record OthersSettings(Locale currentLanguageLocale, boolean minimiseToSystemTrayOnClose, boolean startOnBoot, boolean showAlertsPopup) {
}
