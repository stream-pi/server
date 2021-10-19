package com.stream_pi.server.combobox;

import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.window.ExceptionAndAlertHandler;
import com.stream_pi.util.exception.SevereException;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IPChooserComboBox extends ComboBox<String>
{
    private final ExceptionAndAlertHandler exceptionAndAlertHandler;

    public IPChooserComboBox(ExceptionAndAlertHandler exceptionAndAlertHandler)
    {
        this.exceptionAndAlertHandler = exceptionAndAlertHandler;

        Callback<ListView<String>, ListCell<String>> callbackFactory = new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> str) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String str, boolean b)
                    {
                        super.updateItem(str, b);

                        if (str != null)
                        {
                            setText(str);
                        }
                    }
                };
            }
        };

        setCellFactory(callbackFactory);
        setButtonCell(callbackFactory.call(null));
    }

    public void configureOptions()
    {
        configureOptions(null);
    }

    public void configureOptions(String ip)
    {
        try
        {
            int si = 0, ci = 0;

            getItems().clear();
            getItems().add(I18N.getString("combobox.IPChooserComboBox.allAddresses"));
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = e.nextElement();
                Enumeration<InetAddress> ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = ee.nextElement();
                    if(i instanceof Inet4Address)
                    {
                        ci+=1;
                        getItems().add(i.getHostAddress());

                        if (ip!=null && i.getHostAddress().equals(ip))
                        {
                            si = ci;
                        }
                    }
                }
            }

            getSelectionModel().select(si);
        }
        catch (SocketException e)
        {
            exceptionAndAlertHandler.handleSevereException(new SevereException("Error", "Unable to retrieve network interfaces!"));
        }
    }

    public String getSelectedIP()
    {
        if (getSelectionModel().getSelectedIndex() == 0)
        {
            return "";
        }
        else
        {
            return getSelectionModel().getSelectedItem();
        }
    }
}
