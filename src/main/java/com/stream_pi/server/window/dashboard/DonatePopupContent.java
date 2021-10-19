package com.stream_pi.server.window.dashboard;

import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.io.Config;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.alert.StreamPiAlert;
import com.stream_pi.util.alert.StreamPiAlertListener;
import com.stream_pi.util.alert.StreamPiAlertType;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;

import com.stream_pi.util.links.Links;
import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DonatePopupContent
{
    public DonatePopupContent(HostServices hostServices, ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        Label label = new Label(I18N.getString("window.dashboard.DonatePopupContent.body"));
        
        label.setWrapText(true);
        label.getStyleClass().add("donate_request_popup_label");

        Hyperlink donateLink = new Hyperlink(I18N.getString("window.dashboard.DonatePopupContent.donate"));
        donateLink.setOnAction(event ->{
            hostServices.showDocument(Links.getDonateLink());
        });
        donateLink.getStyleClass().add("donate_request_popup_patreon_link");
        
        VBox pane = new VBox(label, donateLink);
        pane.setSpacing(5.0);

        streamPiAlert = new StreamPiAlert(I18N.getString("window.dashboard.DonatePopupContent.heading"), StreamPiAlertType.INFORMATION, pane);

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
