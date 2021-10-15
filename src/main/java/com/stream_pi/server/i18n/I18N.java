package com.stream_pi.server.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18N
{
    public static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("com.stream_pi.server.i18n.lang");

    public static void init(Locale locale)
    {
        RESOURCE_BUNDLE = ResourceBundle.getBundle("com.stream_pi.server.i18n.lang", locale);
    }

    public static String getString(String key, Object... args)
    {
        if (args.length == 0)
        {
            return RESOURCE_BUNDLE.getString(key);
        }
        else
        {
            return String.format(RESOURCE_BUNDLE.getString(key), args);
        }
    }
}
