module com.StreamPi.Server {
    uses com.StreamPi.ActionAPI.Action;
    requires com.StreamPi.ActionAPI;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires AnimateFX;
    requires org.slf4j;

    exports com.StreamPi.Server;
}