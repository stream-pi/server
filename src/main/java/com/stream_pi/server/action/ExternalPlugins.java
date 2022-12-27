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

package com.stream_pi.server.action;

import com.stream_pi.action_api.action.ServerConnection;
import com.stream_pi.action_api.actionproperty.ServerProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.externalplugin.*;
import com.stream_pi.server.i18n.I18N;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.version.Version;
import com.stream_pi.util.xmlconfighelper.XMLConfigHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExternalPlugins
{
    private static ExternalPlugins instance = null;
    private final Logger logger;

    private File configFile;
    private Document document;

    private static String pluginsLocation = null;


    /**
     * Singleton class instance getter. Creates new one, when asked for the first time
     *
     * @return returns instance of NormalActionPlugins (one and only, always)
     */
    public static synchronized ExternalPlugins getInstance()
    {
        if(instance == null)
        {
            instance = new ExternalPlugins();
        }

        return instance;
    }

    /**
     * Sets the folder location where the plugin JARs and their dependencies are stored
     *
     * @param location Folder location
     */
    public static void setPluginsLocation(String location)
    {
        pluginsLocation = location;
    }

    /**
     * Private constructor
     */
    private ExternalPlugins()
    {
        logger = Logger.getLogger(ExternalPlugins.class.getName());
        externalPlugins = new HashMap<>();
    }

    /**
     * init Method
     */
    public void init() throws SevereException, MinorException
    {
        registerPlugins();
        initPlugins();
    }

    /**
     * Used to fetch list of all external Plugins
     *
     * @return List of plugins
     */
    public Collection<ExternalPlugin> getPlugins()
    {
        return externalPlugins.values();
    }

    /**
     * Returns a plugin by its module name
     *
     * @param uniqueID Module Unique ID
     * @return The plugin. If not found, then null is returned
     */
    public ExternalPlugin getPluginByUniqueID(String uniqueID)
    {
        logger.info("Plugin being requested : "+uniqueID);
        return externalPlugins.getOrDefault(uniqueID, null);
    }

    HashMap<String, ExternalPlugin> externalPlugins;

    /**
     * Used to register plugins from plugin location
     */

    private ModuleLayer moduleLayer;
    public void registerPlugins() throws SevereException, MinorException
    {
        logger.info("Registering external plugins from "+pluginsLocation+" ...");

        try
        {
            configFile = new File(pluginsLocation+"/config.xml");
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            document = docBuilder.parse(configFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SevereException(I18N.getString("action.ExternalPlugins.configXMLParseFailed", configFile.getAbsolutePath()));
        }

      //  ArrayList<ExternalPlugin> pluginErrors = new ArrayList<>();
      //  ArrayList<String> errorModuleError = new ArrayList<>();

        HashMap<String, String> pluginErrors = new HashMap<>();

        HashMap<String, PluginConfig> pluginsConfigs = new HashMap<>();

        NodeList actionsNode = document.getElementsByTagName("actions").item(0).getChildNodes();

        for(int i=0; i<actionsNode.getLength(); i++)
        {
            Node eachActionNode = actionsNode.item(i);

            if(eachActionNode.getNodeType() != Node.ELEMENT_NODE ||
                    !eachActionNode.getNodeName().equals("action"))
                continue;

            Element eachActionElement = (Element) eachActionNode;

            String uniqueID;
            Version version;
            try
            {
                uniqueID = XMLConfigHelper.getStringProperty(eachActionElement, "unique-ID");
                version = new Version(XMLConfigHelper.getStringProperty(eachActionElement, "version"));
            }
            catch (Exception e)
            {
                logger.log(Level.WARNING, "Skipping configuration because invalid ...");
                e.printStackTrace();
                continue;
            }

            ServerProperties serverProperties = new ServerProperties();
         
            NodeList serverPropertiesNodeList = eachActionElement.getElementsByTagName("properties").item(0).getChildNodes();

            for(int j = 0;j<serverPropertiesNodeList.getLength();j++)
            {
                Node eachPropertyNode = serverPropertiesNodeList.item(j);

                if(eachPropertyNode.getNodeType() != Node.ELEMENT_NODE)
                    continue;
            
                if(!eachPropertyNode.getNodeName().equals("property"))
                    continue;


                Element eachPropertyElement = (Element) eachPropertyNode;

                try
                {
                    Property property = new Property(XMLConfigHelper.getStringProperty(eachPropertyElement, "name"), Type.STRING);
                    property.setRawValue(XMLConfigHelper.getStringProperty(eachPropertyElement, "value"));    
                    
                    serverProperties.addProperty(property);
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Skipping property because invalid ...");
                    e.printStackTrace();
                }
            }

            pluginsConfigs.put(uniqueID, new PluginConfig(version, serverProperties));
        }

        logger.info("Size : "+pluginsConfigs.size());

        Path pluginsDir = Paths.get(pluginsLocation);
        try
        {
            ModuleFinder pluginsFinder = ModuleFinder.of(pluginsDir);

            List<String> p = pluginsFinder
                    .findAll()
                    .stream()
                    .map(ModuleReference::descriptor)
                    .map(ModuleDescriptor::name)
                    .collect(Collectors.toList());

            moduleLayer = ModuleLayer
                    .boot()
                    .defineModulesWithOneLoader(ModuleLayer
                            .boot()
                            .configuration()
                            .resolve(pluginsFinder, ModuleFinder.of(), p),

                            ClassLoader.getSystemClassLoader());

            logger.info("Loading plugins from jar ...");

            externalPlugins = new HashMap<>();
            ServiceLoader.load(moduleLayer, ExternalPlugin.class).stream().map(ServiceLoader.Provider::get).forEach(plugin -> {
                externalPlugins.put(plugin.getUniqueID(), plugin);
            });

            logger.info("...Done!");

        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new MinorException(
                    I18N.getString("action.ExternalPlugins.errorLoadingModulesHeading"),
                    I18N.getString("action.ExternalPlugins.errorLoadingModulesBody", e.getLocalizedMessage())
            );
        }


        sortedPlugins = new HashMap<>();

        for (ExternalPlugin eachPlugin : externalPlugins.values())
        {
            try
            {
                eachPlugin.setServerConnection(serverConnection);

                if(eachPlugin instanceof ToggleAction)
                    ((ToggleAction) eachPlugin).setToggleExtras(getToggleExtras());
                else if(eachPlugin instanceof GaugeAction)
                    ((GaugeAction) eachPlugin).setGaugeExtras(getGaugeExtras());

                eachPlugin.initProperties();

                logger.info("Unique ID : "+eachPlugin.getUniqueID());

                PluginConfig pluginConfig = pluginsConfigs.getOrDefault(eachPlugin.getUniqueID(), null);

                if(pluginConfig == null)
                {
                    List<Property> eachPluginStoredProperties = eachPlugin.getServerProperties().get();
                    for(Property property :eachPluginStoredProperties)
                    {
                        if(property.getType() == Type.STRING || property.getType() == Type.INTEGER || property.getType() == Type.DOUBLE)
                            property.setRawValue(property.getDefaultRawValue());
                    }
                }
                else
                {
                    if (pluginConfig.getVersion().isEqual(eachPlugin.getVersion()))
                    {
                        List<Property> eachPluginStoredProperties = pluginConfig.getServerProperties().get();
                        List<Property> eachPluginCodeProperties = eachPlugin.getServerProperties().get();

                        for (int i =0;i< eachPluginCodeProperties.size(); i++)
                        {

                            Property eachPluginCodeProperty = eachPluginCodeProperties.get(i);

                            Property foundProp = null;
                            for (Property eachPluginStoredProperty : eachPluginStoredProperties) {
                                if (eachPluginCodeProperty.getName().equals(eachPluginStoredProperty.getName())) {
                                    eachPluginCodeProperty.setRawValue(eachPluginStoredProperty.getRawValue());
                                    foundProp = eachPluginStoredProperty;
                                }
                            }

                            eachPluginCodeProperties.set(i, eachPluginCodeProperty);

                            if (foundProp != null) {
                                eachPluginStoredProperties.remove(foundProp);
                            }
                        }

                        eachPlugin.getServerProperties().set(eachPluginCodeProperties);
                    }
                }

                if (!sortedPlugins.containsKey(eachPlugin.getCategory())) {
                    sortedPlugins.put(eachPlugin.getCategory(), new ArrayList<>());
                }
                sortedPlugins.get(eachPlugin.getCategory()).add(eachPlugin);
            }
            catch (MinorException e)
            {
                e.printStackTrace();
                pluginErrors.put(eachPlugin.getUniqueID(), "\n * "+eachPlugin.getName()+" ("+e.getMessage()+")");
            }
        }

        try
        {
            saveServerSettings();
        } catch (MinorException e)
        {
            e.printStackTrace();
        }

        logger.log(Level.INFO, "All plugins registered!");

        if(pluginErrors.size() > 0)
        {
            StringBuilder errors = new StringBuilder();
            for (String eachErrorPluginUniqueID: pluginErrors.keySet())
            {
                externalPlugins.remove(eachErrorPluginUniqueID);
                errors.append(pluginErrors.get(eachErrorPluginUniqueID));
            }
            throw new MinorException(I18N.getString("action.ExternalPlugins.theFollowingPluginsCouldNotBeLoaded", errors));
        }
    }

    private class PluginConfig
    {
        private final Version version;
        private final ServerProperties serverProperties;

        public PluginConfig(Version version, ServerProperties serverProperties)
        {
            this.version = version;
            this.serverProperties = serverProperties;
        }

        public Version getVersion()
        {
            return version;
        }

        public ServerProperties getServerProperties()
        {
            return serverProperties;
        }
    }

    /**
     * Used to init plugins
     */
    public void initPlugins() throws MinorException
    {
        StringBuilder errors = new StringBuilder();
        boolean isError = false;

        ArrayList<ExternalPlugin> pluginsToBeRemoved = new ArrayList<>();

        for(ExternalPlugin eachPlugin : externalPlugins.values())
        {
            try
            {
                eachPlugin.initAction();
            }
            catch (MinorException e)
            {
                pluginsToBeRemoved.add(eachPlugin);
                e.printStackTrace();
                isError = true;
                errors.append("\n* ")
                        .append(eachPlugin.getName())
                        .append(" - ")
                        .append(eachPlugin.getUniqueID())
                        .append("\n");

                errors.append(e.getMessage());

                errors.append("\n");
            }
        }

        for (ExternalPlugin plugin : pluginsToBeRemoved)
        {
            externalPlugins.remove(plugin);
        }

        if(isError)
        {
            throw new MinorException(I18N.getString("action.ExternalPlugins.initActionPluginsFailed", errors));
        }
    }

    public void runOnServerPropertiesSavedByUser() throws MinorException
    {
        StringBuilder errors = new StringBuilder();
        boolean isError = false;

        for(ExternalPlugin eachPlugin : externalPlugins.values())
        {
            try
            {
                eachPlugin.onServerPropertiesSavedByUser();
            }
            catch (MinorException e)
            {
                e.printStackTrace();
                isError = true;
                errors.append("\n* ")
                        .append(eachPlugin.getName())
                        .append(" - ")
                        .append(eachPlugin.getUniqueID())
                        .append("\n");

                errors.append(e.getMessage());

                errors.append("\n");
            }
        }

        if(isError)
        {
            throw new MinorException(errors.toString());
        }
    }

    HashMap<String, ArrayList<ExternalPlugin>> sortedPlugins;

    /**
     * Gets list of sorted plugins
     *
     * @return Hashmap with category key, and list of plugins of each category
     */
    public HashMap<String, ArrayList<ExternalPlugin>> getSortedPlugins()
    {
        return sortedPlugins;
    }

    /**
     * @return Gets actions element from the config.xml in plugins folder
     */
    private Element getActionsElement()
    {
        return (Element) document.getElementsByTagName("actions").item(0);
    }

    /**
     * Saves ServerProperties of every plugin in config.xml in plugins folder
     *
     * @throws MinorException Thrown when failed to save settings
     */
    public void saveServerSettings() throws MinorException 
    {
        XMLConfigHelper.removeChilds(getActionsElement());

        for(ExternalPlugin externalPlugin : externalPlugins.values())
        {
            Element actionElement = document.createElement("action");
            getActionsElement().appendChild(actionElement);

            Element uniqueIDElement = document.createElement("unique-ID");
            uniqueIDElement.setTextContent(externalPlugin.getUniqueID());
            actionElement.appendChild(uniqueIDElement);

            
            Element versionElement = document.createElement("version");
            versionElement.setTextContent(externalPlugin.getVersion().getText());
            actionElement.appendChild(versionElement);

            Element propertiesElement = document.createElement("properties");
            actionElement.appendChild(propertiesElement);

            for(String key : externalPlugin.getServerProperties().getNames())
            {
                for(Property eachProperty : externalPlugin.getServerProperties().getMultipleProperties(key))
                {
                    Element propertyElement = document.createElement("property");
                    propertiesElement.appendChild(propertyElement);

                    Element nameElement = document.createElement("name");
                    nameElement.setTextContent(eachProperty.getName());
                    propertyElement.appendChild(nameElement);

                    Element valueElement = document.createElement("value");
                    valueElement.setTextContent(eachProperty.getRawValue());
                    propertyElement.appendChild(valueElement);
                }
            }
        }

        save();
    }

    private ToggleExtras toggleExtras = null;

    /**
     * Set PropertySaver class
     * @param toggleExtras instance of PropertySaver
     */
    public void setToggleExtras(ToggleExtras toggleExtras)
    {
        this.toggleExtras = toggleExtras;
    }

    public ToggleExtras getToggleExtras()
    {
        return toggleExtras;
    }


    private GaugeExtras gaugeExtras = null;

    public void setGaugeExtras(GaugeExtras gaugeExtras)
    {
        this.gaugeExtras = gaugeExtras;
    }

    public GaugeExtras getGaugeExtras()
    {
        return gaugeExtras;
    }

    private ServerConnection serverConnection = null;

    /**
     * Set setServerConnection class
     * @param serverConnection instance of ServerConnection
     */
    public void setServerConnection(ServerConnection serverConnection)
    {
        this.serverConnection = serverConnection;
    }

    /**
     * Get plugin from index from list
     *
     * @param index of plugin
     * @return found plugin
     */
    public ExternalPlugin getActionFromIndex(int index)
    {
        return externalPlugins.get(index);
    }

    /**
     * Calls onShutDown method in every plugin
     */
    public void shutDownActions()
    {
        if(externalPlugins != null)
        {
            for(ExternalPlugin eachPlugin : externalPlugins.values())
            {
                try
                {
                    eachPlugin.onShutDown();
                }
                catch (MinorException e)
                {
                    e.printStackTrace();
                }
            }
            externalPlugins.clear();
            externalPlugins = null;
            moduleLayer = null;
        }
    }

    /**
     * Saves all Server Properties of each Plugin in config.xml in Plugins folder
     * @throws MinorException thrown when failed to save
     */
    public void save() throws MinorException
    {
        try
        {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(configFile);
            Source input = new DOMSource(document);

            transformer.transform(input, output);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new MinorException(I18N.getString("action.ExternalPlugins.savePluginSettingsFailed", e.getLocalizedMessage()));
        }
    }
}
