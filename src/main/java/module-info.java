module com.StreamPi.Server {
    uses com.StreamPi.ActionAPI.Action.Action;
    uses com.StreamPi.ActionAPI.NormalAction.NormalAction;

    requires com.StreamPi.ActionAPI;
    requires com.StreamPi.Util;
    requires com.StreamPi.ThemeAPI;

    requires org.kordamp.ikonli.javafx;

    requires java.xml;

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.media;

    requires java.desktop;

    requires java.sql;

    requires org.json;

    exports com.StreamPi.Server;
}