/*
Config.java

Contributor(s) : Debayan Sutradhar (@rnayabed)

handler for config.xml
 */

package com.stream_pi.server.io;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
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
import com.stream_pi.server.info.ServerInfo;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.iohelper.IOHelper;
import com.stream_pi.util.xmlconfighelper.XMLConfigHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Config
{

    private static Config instance = null;
  
    private final File configFile;

    private Document document;

    private Config() throws SevereException {
        try {
            configFile = new File(ServerInfo.getInstance().getPrePath()+"config.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            document = docBuilder.parse(configFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SevereException("Config", "Unable to read config.xml\n"+e.getMessage());
        }
    }

    public static synchronized Config getInstance() throws SevereException
    {
        if(instance == null)
            instance = new Config();

        return instance;
    }

    Logger logger = Logger.getLogger(Config.class.getName());

    public void save() throws SevereException {
        try {
            logger.info("Saving config ...");
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(configFile);
            Source input = new DOMSource(document);

            transformer.transform(input, output);
            logger.info("... Done!");
        } catch (Exception e) {
            throw new SevereException("Config", "unable to save config.xml");
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

    //default getters
    public String getDefaultServerName()
    {
        return "Stream-Pi Server";
    }

    public int getDefaultPort()
    {
        return 2004;
    }

    private Element getServerElement()
    {
        return (Element) document.getElementsByTagName("server").item(0);
    }



    //server
    private Element getDividerPositionsElement()
    {
        return (Element) getServerElement().getElementsByTagName("divider-positions").item(0);
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
        return (Element) getServerElement().getElementsByTagName("action-grid").item(0);
    }

    public int getActionGridActionGap()
    {
        return XMLConfigHelper.getIntProperty(getActionGridElement(), "gap",
                getDefaultActionGridActionGap(), false, true, document, configFile);
    }

    public int getActionGridActionSize()
    {
        return XMLConfigHelper.getIntProperty(getActionGridElement(), "size",
                getDefaultActionGridSize(), false, true, document, configFile);
    }


    public String getCurrentThemeFullName()
    {
        return XMLConfigHelper.getStringProperty(getServerElement(), "current-theme-full-name",
                getDefaultCurrentThemeFullName(), false, true, document, configFile);
    }

    public String getThemesPath()
    {
        return XMLConfigHelper.getStringProperty(getServerElement(), "themes-path",
                getDefaultThemesPath(), false, true, document, configFile);
    }


    public String getPluginsPath()
    {
        return XMLConfigHelper.getStringProperty(getServerElement(), "plugins-path",
                getDefaultPluginsPath(), false, true, document, configFile);
    }

    //default getters
    public String getDefaultCurrentThemeFullName()
    {
        return "com.stream_pi.defaultlight";
    }

    public String getDefaultThemesPath()
    {
        return ServerInfo.getInstance().getPrePath()+"Themes/";
    }

    public String getDefaultPluginsPath()
    {
        return ServerInfo.getInstance().getPrePath()+"Plugins/";
    }


    //server > startup-window-size
    
    private Element getStartupWindowSizeElement()
    {
        return (Element) getServerElement().getElementsByTagName("startup-window-size").item(0);
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


    //others
    private Element getOthersElement()
    {
        return (Element) document.getElementsByTagName("others").item(0);
    }

    public boolean getStartOnBoot()
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

    public boolean isAllowDonatePopup()
    {
        return XMLConfigHelper.getBooleanProperty(getOthersElement(), "allow-donate-popup", true, false, true, document, configFile);
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

    public void setServerPort(int port)
    {
        getCommsElement().getElementsByTagName("port").item(0).setTextContent(port+"");
    }

    //server

    public int getDefaultActionGridActionGap()
    {
        return 5;
    }

    public int getDefaultActionGridSize()
    {
        return 100;
    }

    public void setActionGridSize(int size)
    {
        getActionGridElement().getElementsByTagName("size").item(0).setTextContent(size+"");
    }

    public void setActionGridGap(int size)
    {
        getActionGridElement().getElementsByTagName("gap").item(0).setTextContent(size+"");
    }

    public void setPluginsPath(String path)
    {
        getServerElement().getElementsByTagName("plugins-path").item(0).setTextContent(path);
    }

    public void setThemesPath(String path)
    {
        getServerElement().getElementsByTagName("themes-path").item(0).setTextContent(path);
    }

    public void setCurrentThemeFullName(String themeName)
    {
        getServerElement().getElementsByTagName("current-theme-full-name").item(0).setTextContent(themeName);
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

    public void setAllowDonatePopup(boolean value)
    {
        getOthersElement().getElementsByTagName("allow-donate-popup").item(0).setTextContent(value+"");
    }

    public static void unzipToDefaultPrePath() throws Exception
    {
        IOHelper.unzip(Objects.requireNonNull(Main.class.getResourceAsStream("Default.zip")), ServerInfo.getInstance().getPrePath());

        Config config = Config.getInstance();

        config.setThemesPath(config.getDefaultThemesPath());
        config.setPluginsPath(config.getDefaultPluginsPath());

        if(SystemTray.isSupported())
        {
            config.setMinimiseToSystemTrayOnClose(true);
        }

        config.save();
    }
}
