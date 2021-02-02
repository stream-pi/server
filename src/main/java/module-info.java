module com.stream_pi.server {
    uses com.stream_pi.actionapi.action.Action;
    uses com.stream_pi.actionapi.normalaction.NormalAction;

    requires com.stream_pi.actionapi;
    requires com.stream_pi.util;
    requires com.stream_pi.themeapi;

    requires org.kordamp.ikonli.javafx;

    requires java.xml;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    requires java.desktop;

    requires java.sql;

    requires org.json;

    exports com.stream_pi.server;
}