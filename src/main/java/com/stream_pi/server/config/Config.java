/*
 * Stream-Pi - Free, Open-Source, Modular, Cross-Platform and Programmable Macro Pad
 * Copyright (C) 2019-2022 Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/*
Config.java

Contributor(s) : Debayan Sutradhar (@rnayabed)

handler for config.xml
 */

package com.stream_pi.server.config;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.stream_pi.server.Main;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.iohelper.IOHelper;
import com.stream_pi.util.version.Version;
import com.stream_pi.util.xmlconfighelper.XMLConfigHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Config
{
    private static Config instance = null;
  
    private final File configFile;

    private final Document document;

    public Config() throws SevereException
    {
        try
        {
            configFile = new File(ServerInfo.getInstance().getPrePath()+"config.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            document = docBuilder.parse(configFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(I18N.getString("io.config.Config.unableToReadConfig", e.getMessage()));
        }
    }

    public static synchronized Config getInstance()
    {
        return instance;
    }

    public static void nullify()
    {
        instance = null;
    }

    public static void initialise() throws SevereException
    {
        if(instance != null)
        {
            Logger.getLogger(Config.class.getName()).warning("Config was already loaded! Re-loading ...");
        }

        instance = new Config();
    }

    Logger logger = Logger.getLogger(Config.class.getName());

    public void save() throws SevereException
    {
        try
        {
            logger.info("Saving config ...");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(configFile);
            Source input = new DOMSource(document);

            transformer.transform(input, output);
            logger.info("... Done!");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(I18N.getString("io.config.Config.unableToSaveConfig", e.getMessage()));
        }
    }

    public Version getVersion()
    {
        try
        {
            Node versionNode = document.getElementsByTagName("version").item(0);

            if (versionNode == null)
            {
                return null;
            }

            return new Version(versionNode.getTextContent());
        }
        catch (MinorException e)
        {
            return null;
        }
    }



    //Getters

    //comms
    private Element getCommsElement()
    {
        return (Element) document.getElementsByTagName("comms").item(0);
    }

    public String getServerName()
    {
        return XMLConfigHelper.getStringProperty(getCommsElement(), "name",
                getDefaultServerName(), false, true, document, configFile);
    }

    public int getPort()
    {
        return XMLConfigHelper.getIntProperty(getCommsElement(), "port",
                getDefaultPort(), false, true, document, configFile);
    }

    public String getIP()
    {
        return XMLConfigHelper.getStringProperty(getCommsElement(), "ip", null, false, true, document, configFile);
    }

    public void setIP(String ip)
    {
        getCommsElement().getElementsByTagName("ip").item(0).setTextContent(ip);
    }

    //default getters
    public String getDefaultServerName()
    {
        return "Stream-Pi Server";
    }

    public static int getDefaultPort()
    {
        return 6504;
    }


    //server
    private Element getDividerPositionsElement()
    {
        return (Element) document.getElementsByTagName("divider-positions").item(0);
    }

    public String getDefaultLeftDividerPositions()
    {
        return "3.0";
    }


    public double[] getLeftDividerPositions()
    {
        String[] strArr =  XMLConfigHelper.getStringProperty(getDividerPositionsElement(), "left",
                getDefaultLeftDividerPositions(), false, true, document, configFile)
                .split(",");

        double[] r = new double[strArr.length];

        for (int i = 0;i<strArr.length;i++)
        {
            r[i] = Double.parseDouble(strArr[i]);
        }

        return r;
    }

    public void setLeftDividerPositions(double[] position)
    {
        String r = Arrays.toString(position);
        getDividerPositionsElement().getElementsByTagName("left").item(0).setTextContent(r.substring(1, r.length()-1));
    }

    public String getDefaultRightDividerPositions()
    {
        return "3.0";
    }


    public double[] getRightDividerPositions()
    {
        String[] strArr =  XMLConfigHelper.getStringProperty(getDividerPositionsElement(), "right",
                getDefaultRightDividerPositions(), false, true, document, configFile)
                .split(",");

        double[] r = new double[strArr.length];

        for (int i = 0;i<strArr.length;i++)
        {
            r[i] = Double.parseDouble(strArr[i]);
        }

        return r;
    }

    public void setRightDividerPositions(double[] position)
    {
        String r = Arrays.toString(position);
        getDividerPositionsElement().getElementsByTagName("right").item(0).setTextContent(r.substring(1, r.length()-1));
    }

    private Element getActionGridElement()
    {
        return (Element) document.getElementsByTagName("action-grid").item(0);
    }

    private Element getActionGridSizeElement()
    {
        return (Element) getActionGridElement().getElementsByTagName("size").item(0);
    }

    private Element getActionGridGapElement()
    {
        return (Element) getActionGridElement().getElementsByTagName("gap").item(0);
    }

    private Element getActionGridDisplayTextFontSizeElement()
    {
        return (Element) getActionGridElement().getElementsByTagName("display-text-font-size").item(0);
    }



    public String getCurrentThemeFullName()
    {
        return XMLConfigHelper.getStringProperty(document, "current-theme-full-name",
                getDefaultCurrentThemeFullName(), false, true, document, configFile);
    }

    public String getThemesPath()
    {
        return XMLConfigHelper.getStringProperty(document, "themes-path",
                getDefaultThemesPath(), false, true, document, configFile);
    }


    public String getPluginsPath()
    {
        return XMLConfigHelper.getStringProperty(document, "plugins-path",
                getDefaultPluginsPath(), false, true, document, configFile);
    }

    //default getters
    public String getDefaultCurrentThemeFullName()
    {
        return "com.stream_pi.defaultlight";
    }

    public String getDefaultThemesPath()
    {
        return ServerInfo.getInstance().getPrePath()+"Themes" + File.separator;
    }

    public String getDefaultPluginsPath()
    {
        return ServerInfo.getInstance().getPrePath()+"Plugins" + File.separator;
    }


    //server > startup-window-size
    
    private Element getStartupWindowSizeElement()
    {
        return (Element) document.getElementsByTagName("startup-window-size").item(0);
    }

    public double getStartupWindowWidth()
    {
        return XMLConfigHelper.getDoubleProperty(getStartupWindowSizeElement(), "width",
                getDefaultStartupWindowWidth(), false, true, document, configFile);
    }

    public double getStartupWindowHeight()
    {
        return XMLConfigHelper.getDoubleProperty(getStartupWindowSizeElement(), "height",
                getDefaultStartupWindowHeight(), false, true, document, configFile);
    }

    //default getters
    public int getDefaultStartupWindowWidth()
    {
        return 1024;
    }

    public int getDefaultStartupWindowHeight()
    {
        return 768;
    }


    //sound on action clicked
    private Element getSoundOnActionClickedElement()
    {
        return (Element) document.getElementsByTagName("sound-on-action-clicked").item(0);
    }

    //others
    private Element getOthersElement()
    {
        return (Element) document.getElementsByTagName("others").item(0);
    }

    public void setCurrentLanguageLocale(Locale locale)
    {
        getOthersElement().getElementsByTagName("language-locale").item(0).setTextContent(locale.toLanguageTag());
    }

    public Locale getCurrentLanguageLocale()
    {
        return Locale.forLanguageTag(XMLConfigHelper.getStringProperty(getOthersElement(), "language-locale",
                getDefaultLanguageLocale().toLanguageTag(), false, true, document, configFile));
    }

    public Locale getDefaultLanguageLocale()
    {
        return Locale.getDefault();
    }





    public boolean isStartOnBoot()
    {
        return XMLConfigHelper.getBooleanProperty(getOthersElement(), "start-on-boot",
                getDefaultStartOnBoot(), false, true, document, configFile);
    }

    public boolean getMinimiseToSystemTrayOnClose()
    {
        return XMLConfigHelper.getBooleanProperty(getOthersElement(), "minimize-to-tray-on-close",
                getDefaultMinimiseToSystemTrayOnClose(), false, true, document, configFile);
    }

    public boolean isFirstTimeUse()
    {
        return XMLConfigHelper.getBooleanProperty(getOthersElement(), "first-time-use", true, false, true, document, configFile);
    }

    //default getters
    public boolean getDefaultStartOnBoot()
    {
        return false;
    }

    public boolean getDefaultMinimiseToSystemTrayOnClose()
    {
        return true;
    }


    //Setters

    //comms
    public void setServerName(String name)
    {
        getCommsElement().getElementsByTagName("name").item(0).setTextContent(name);
    }

    public void setPort(int port)
    {
        getCommsElement().getElementsByTagName("port").item(0).setTextContent(port+"");
    }

    //server


    public int getDefaultActionGridSize()
    {
        return 100;
    }



    // Action grid action size

    // value
    public void setActionGridActionSize(double value)
    {
        getActionGridSizeElement().getElementsByTagName("value").item(0).setTextContent(value+"");
    }

    public double getActionGridActionSize()
    {
        return XMLConfigHelper.getDoubleProperty(getActionGridSizeElement(), "value",
                getDefaultActionGridActionSize(), false, true, document, configFile);
    }

    public double getDefaultActionGridActionSize()
    {
        return 100;
    }

    // profile default
    public void setActionGridUseSameActionSizeAsProfile(boolean value)
    {
        getActionGridSizeElement().getElementsByTagName("use-profile-default").item(0).setTextContent(value+"");
    }

    public boolean getActionGridUseSameActionSizeAsProfile()
    {
        return XMLConfigHelper.getBooleanProperty(getActionGridSizeElement(), "use-profile-default",
                getDefaultActionGridUseSameActionSizeAsProfile(), false, true, document, configFile);
    }

    public boolean getDefaultActionGridUseSameActionSizeAsProfile()
    {
        return false;
    }





    // Action grid action gap

    // value
    public void setActionGridActionGap(double value)
    {
        getActionGridGapElement().getElementsByTagName("value").item(0).setTextContent(value+"");
    }

    public double getActionGridActionGap()
    {
        return XMLConfigHelper.getDoubleProperty(getActionGridGapElement(), "value",
                getDefaultActionGridActionGap(), false, true, document, configFile);
    }

    public double getDefaultActionGridActionGap()
    {
        return 5;
    }

    // profile default
    public void setActionGridUseSameActionGapAsProfile(boolean value)
    {
        getActionGridGapElement().getElementsByTagName("use-profile-default").item(0).setTextContent(value+"");
    }

    public boolean getActionGridUseSameActionGapAsProfile()
    {
        return XMLConfigHelper.getBooleanProperty(getActionGridGapElement(), "use-profile-default",
                getDefaultActionGridUseSameActionGapAsProfile(), false, true, document, configFile);
    }

    public boolean getDefaultActionGridUseSameActionGapAsProfile()
    {
        return false;
    }





    // Action display text font size

    // value
    public void setActionGridActionDisplayTextFontSize(double value)
    {
        getActionGridDisplayTextFontSizeElement().getElementsByTagName("value").item(0).setTextContent(value+"");
    }

    public double getActionGridActionDisplayTextFontSize()
    {
        return XMLConfigHelper.getDoubleProperty(getActionGridDisplayTextFontSizeElement(), "value",
                getDefaultActionGridActionDisplayTextFontSize(), false, true, document, configFile);
    }

    public double getDefaultActionGridActionDisplayTextFontSize()
    {
        return 20;
    }

    // profile default
    public void setActionGridUseSameActionDisplayTextFontSizeAsProfile(boolean value)
    {
        getActionGridDisplayTextFontSizeElement().getElementsByTagName("use-profile-default").item(0).setTextContent(value+"");
    }

    public boolean getActionGridUseSameActionDisplayTextFontSizeAsProfile()
    {
        return XMLConfigHelper.getBooleanProperty(getActionGridDisplayTextFontSizeElement(), "use-profile-default",
                getDefaultActionGridUseSameActionDisplayTextFontSizeAsProfile(), false, true, document, configFile);
    }

    public boolean getDefaultActionGridUseSameActionDisplayTextFontSizeAsProfile()
    {
        return true;
    }


    public void setPluginsPath(String path)
    {
        document.getElementsByTagName("plugins-path").item(0).setTextContent(path);
    }

    public void setThemesPath(String path)
    {
        document.getElementsByTagName("themes-path").item(0).setTextContent(path);
    }

    public void setCurrentThemeFullName(String themeName)
    {
        document.getElementsByTagName("current-theme-full-name").item(0).setTextContent(themeName);
    }

    //server > startup-window-size
    public void setStartupWindowSize(double width, double height)
    {
        setStartupWindowWidth(width);
        setStartupWindowHeight(height);
    }

    public void setStartupWindowWidth(double width)
    {
        getStartupWindowSizeElement().getElementsByTagName("width").item(0).setTextContent(width+"");
    }

    public void setStartupWindowHeight(double height)
    {
        getStartupWindowSizeElement().getElementsByTagName("height").item(0).setTextContent(height+"");
    }

    //others

    public void setStartupOnBoot(boolean value)
    {
        getOthersElement().getElementsByTagName("start-on-boot").item(0).setTextContent(value+"");
    }

    public void setMinimiseToSystemTrayOnClose(boolean value)
    {
        getOthersElement().getElementsByTagName("minimize-to-tray-on-close").item(0).setTextContent(value+"");
    }

    public void setFirstTimeUse(boolean value)
    {
        getOthersElement().getElementsByTagName("first-time-use").item(0).setTextContent(value+"");
    }

    public static void unzipToDefaultPrePath() throws MinorException, SevereException
    {
        IOHelper.unzip(Objects.requireNonNull(Main.class.getResourceAsStream("Default.zip")), ServerInfo.getInstance().getPrePath());

        Config config = new Config();
        config.setThemesPath(config.getDefaultThemesPath());
        config.setPluginsPath(config.getDefaultPluginsPath());
        config.setCurrentLanguageLocale(config.getDefaultLanguageLocale());

        if(SystemTray.isSupported())
        {
            config.setMinimiseToSystemTrayOnClose(true);
        }

        config.save();
    }

    public void setShowAlertsPopup(boolean value)
    {
        getOthersElement().getElementsByTagName("alerts-popup").item(0).setTextContent(value+"");
    }

    public boolean isShowAlertsPopup()
    {
        return XMLConfigHelper.getBooleanProperty(getOthersElement(), "alerts-popup", getDefaultIsShowAlertsPopup(), false, true, document, configFile);
    }

    public boolean getDefaultIsShowAlertsPopup()
    {
        return true;
    }

    public void setSoundOnActionClickedStatus(boolean value)
    {
        getSoundOnActionClickedElement().getElementsByTagName("status").item(0).setTextContent(value+"");
    }

    public boolean getSoundOnActionClickedStatus()
    {
        return XMLConfigHelper.getBooleanProperty(getSoundOnActionClickedElement(), "status", getDefaultSoundOnActionClickedStatus(), false, true, document, configFile);
    }

    public boolean getDefaultSoundOnActionClickedStatus()
    {
        return false;
    }


    public void setSoundOnActionClickedFilePath(String value)
    {
        getSoundOnActionClickedElement().getElementsByTagName("file-path").item(0).setTextContent(value);
    }

    public String getSoundOnActionClickedFilePath()
    {
        return XMLConfigHelper.getStringProperty(getSoundOnActionClickedElement(), "file-path", getDefaultSoundOnActionClickedFilePath(), false, true, document, configFile);
    }

    public String getDefaultSoundOnActionClickedFilePath()
    {
        return null; //To be replaced with a real default beep sound later.
    }
}
