package com.stream_pi.server.window.dashboard;

import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;

import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DonatePopupContent{
    public DonatePopupContent(HostServices hostServices, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        Label label = new Label("We are a very small team working very hard on this project for best user experience.\n\n" +
            "Something like StreamPi takes time, effort and resources. But we will always keep this 100% opensource and free.\n\n" +
            "If you find this project helpful, and would want to help us, please consider donating :)\n\n"+
            "If you are unable to do so even a small shoutout and share across social media would be very helpful.");
        
        label.setWrapText(true);
        label.getStyleClass().add("donate_request_popup_label");

        Hyperlink patreonLink = new Hyperlink("Our Patreon");
        patreonLink.setOnAction(event ->{
            hostServices.showDocument("https://patreon.com/streampi");
        });
        patreonLink.getStyleClass().add("donate_request_popup_patreon_link");
        
        VBox pane = new VBox(label, patreonLink);
        pane.setSpacing(5.0);

        streamPiAlert = new StreamPiAlert("Hey!", StreamPiAlertType.INFORMATION, pane);

        streamPiAlert.setOnClicked(new StreamPiAlertListener()
        {
            @Override
            public void onClick(String buttonClicked) 
            {
                try
                {
                    Config.getInstance().setAllowDonatePopup(false);
                    Config.getInstance().save();
                }
                catch(SevereException e)
                {
                    exceptionAndAlertHandler.handleSevereException(e);
                }
            }
            
        });
    }

    private StreamPiAlert streamPiAlert;

    public void show()
    {
        streamPiAlert.show();
    }
    
}
