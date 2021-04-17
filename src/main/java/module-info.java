module com.stream_pi.server
{

    uses com.stream_pi.action_api.action.Action;
    uses com.stream_pi.action_api.externalplugin.NormalAction;
    uses com.stream_pi.action_api.externalplugin.ExternalPlugin;

    requires com.stream_pi.action_api;
    requires com.stream_pi.util;
    requires com.stream_pi.theme_api;

    requires org.kordamp.ikonli.javafx;

    requires java.xml;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    requires java.desktop;

    requires java.sql;

    opens com.stream_pi.server.window.settings;

    exports com.stream_pi.server;
    opens com.stream_pi.server.window.settings.About;
}