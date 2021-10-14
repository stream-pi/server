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

    public static String getString(String key)
    {
        System.out.println("LOCALL : "+RESOURCE_BUNDLE.getLocale());
        System.out.println(RESOURCE_BUNDLE.getString(key));
        return RESOURCE_BUNDLE.getString(key);
    }
}
