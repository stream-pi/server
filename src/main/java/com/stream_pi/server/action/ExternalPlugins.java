/*
Stream-Pi - Free & Open-Source Modular Cross-Platform Programmable Macropad
Copyright (C) 2019-2021  Debayan Sutradhar (rnayabed),  Samuel Quiñones (SamuelQuinones)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Written by : Debayan Sutradhar (rnayabed)
*/

package com.stream_pi.server.action;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.stream_pi.action_api.action.Action;
import com.stream_pi.action_api.action.ActionType;
import com.stream_pi.action_api.action.PropertySaver;
import com.stream_pi.action_api.action.ServerConnection;
import com.stream_pi.action_api.actionproperty.ServerProperties;
import com.stream_pi.action_api.actionproperty.property.Property;
import com.stream_pi.action_api.actionproperty.property.Type;
import com.stream_pi.action_api.normalaction.ExternalPlugin;
import com.stream_pi.action_api.normalaction.NormalAction;
import com.stream_pi.util.exception.MinorException;
import com.stream_pi.util.exception.SevereException;
import com.stream_pi.util.exception.StreamPiException;
import com.stream_pi.util.version.Version;
import com.stream_pi.util.xmlconfighelper.XMLConfigHelper;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.lang.module.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

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
        externalPluginsHashmap = new HashMap<>();
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
    public List<ExternalPlugin> getPlugins()
    {
        return externalPlugins;
    }

    /**
     * Returns a plugin by its module name
     *
     * @param name Module Name
     * @return The plugin. If not found, then null is returned
     */
    public ExternalPlugin getPluginByModuleName(String name)
    {
        logger.info("Plugin being requested : "+name);
        Integer index = externalPluginsHashmap.getOrDefault(name, -1);
        if(index != -1)
        {
            return externalPlugins.get(index);
        }

        return null;
    }

    private List<ExternalPlugin> externalPlugins = null;
    HashMap<String, Integer> externalPluginsHashmap;

    /**
     * Used to register plugins from plugin location
     */
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
            throw new SevereException("Plugins","Error reading plugins config.xml. Cannot continue.");
        }

        ArrayList<ExternalPlugin> errorModules = new ArrayList<>();
        ArrayList<String> errorModuleError = new ArrayList<>();

        ArrayList<Action> pluginsConfigs = new ArrayList<>();

        NodeList actionsNode = document.getElementsByTagName("actions").item(0).getChildNodes();

        for(int i =0;i<actionsNode.getLength();i++)
        {
            Node eachActionNode = actionsNode.item(i);

            if(eachActionNode.getNodeType() != Node.ELEMENT_NODE)
                continue;
            
            if(!eachActionNode.getNodeName().equals("action"))
                continue;

            Element eachActionElement = (Element) eachActionNode;
 


            String name;
            Version version;
            ActionType actionType;
            try
            {
                name = XMLConfigHelper.getStringProperty(eachActionElement, "module-name");
                actionType = ActionType.valueOf(XMLConfigHelper.getStringProperty(eachActionElement, "type"));
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

            Action action = new Action(actionType);

            action.setModuleName(name);
            action.setVersion(version);
            action.getServerProperties().set(serverProperties);

            pluginsConfigs.add(action);
        }

        logger.info("Size : "+pluginsConfigs.size());

        Path pluginsDir = Paths.get(pluginsLocation); // Directory with plugins JARs
        try
        {
            // Search for plugins in the plugins directory
            ModuleFinder pluginsFinder = ModuleFinder.of(pluginsDir);

            // Find all names of all found plugin modules
            List<String> p = pluginsFinder
                    .findAll()
                    .stream()
                    .map(ModuleReference::descriptor)
                    .map(ModuleDescriptor::name)
                    .collect(Collectors.toList());

            // Create configuration that will resolve plugin modules
            // (verify that the graph of modules is correct)
            Configuration pluginsConfiguration = ModuleLayer
                    .boot()
                    .configuration()
                    .resolve(pluginsFinder, ModuleFinder.of(), p);

            // Create a module layer for plugins
            ModuleLayer layer = ModuleLayer
                    .boot()
                    .defineModulesWithOneLoader(pluginsConfiguration, ClassLoader.getSystemClassLoader());

            logger.info("Loading plugins from jar ...");
            // Now you can use the new module layer to find service implementations in it
            externalPlugins = ServiceLoader
                    .load(layer, ExternalPlugin.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toList());

            logger.info("...Done!");

        }
        catch (Exception e)
        {
            e.printStackTrace();

            throw new MinorException("Error", "Error loading modules\n"+e.getMessage()+"\nPlease fix the errors. Other plugins wont be loaded.");
        }


        sortedPlugins = new HashMap<>();

        for (ExternalPlugin eachPlugin : externalPlugins)
        {
            try
            {
                eachPlugin.setPropertySaver(propertySaver);
                eachPlugin.setServerConnection(serverConnection);
                eachPlugin.initProperties();


                logger.info("MODULE : "+eachPlugin.getModuleName());
                Action foundAction = null;
                for (Action action : pluginsConfigs) {
                    if (action.getModuleName().equals(eachPlugin.getModuleName())
                            && action.getVersion().isEqual(eachPlugin.getVersion())) {

                        foundAction = action;

                        List<Property> eachPluginStoredProperties = action.getServerProperties().get();
                        List<Property> eachPluginCodeProperties = eachPlugin.getServerProperties().get();


                        for (int i =0;i< eachPluginCodeProperties.size(); i++) {

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

                        break;
                    }
                }

                if (foundAction != null)
                    pluginsConfigs.remove(foundAction);
                else
                {
                    List<Property> eachPluginStoredProperties = eachPlugin.getServerProperties().get();
                    for(Property property :eachPluginStoredProperties)
                    {
                        if(property.getType() == Type.STRING || property.getType() == Type.INTEGER || property.getType() == Type.DOUBLE)
                            property.setRawValue(property.getDefaultRawValue());
                    }
                }



                if (!sortedPlugins.containsKey(eachPlugin.getCategory())) {
                    ArrayList<ExternalPlugin> actions = new ArrayList<>();

                    sortedPlugins.put(eachPlugin.getCategory(), actions);
                }

                sortedPlugins.get(eachPlugin.getCategory()).add(eachPlugin);

            }
            catch (Exception e)
            {
                e.printStackTrace();
                errorModules.add(eachPlugin);
                errorModuleError.add(e.getMessage());
            }
        }

        try {
            saveServerSettings();
        } catch (MinorException e) {
            e.printStackTrace();
        }

        logger.log(Level.INFO, "All plugins registered!");

        if(errorModules.size() > 0)
        {
            StringBuilder errors = new StringBuilder("The following action modules could not be loaded:");
            for(int i = 0; i<errorModules.size(); i++)
            {
                externalPlugins.remove(errorModules.get(i));
                errors.append("\n * ").append(errorModules.get(i).getModuleName()).append("\n(")
                    .append(errorModuleError.get(i)).append(")");
            }

            throw new MinorException("Plugins", errors.toString());
        }


        for(int i = 0;i<externalPlugins.size();i++)
        {
            externalPluginsHashmap.put(externalPlugins.get(i).getModuleName(), i);
        }
    }

    /**
     * Used to init plugins
     */
    public void initPlugins() throws MinorException
    {
        StringBuilder errors = new StringBuilder("There were errors registering the following plugins. As a result, they have been omitted : ");
        boolean isError = false;

        for(ExternalPlugin eachPlugin : externalPlugins)
        {
            try
            {
                eachPlugin.initAction();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                isError = true;
                errors.append("\n* ")
                        .append(eachPlugin.getName())
                        .append(" - ")
                        .append(eachPlugin.getModuleName())
                        .append("\n");

                if(e instanceof StreamPiException)
                    errors.append(((MinorException) e).getShortMessage());

                errors.append("\n");
            }
        }

        if(isError)
        {
            throw new MinorException("Plugin init error", errors.toString());
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

        for(ExternalPlugin externalPlugin : externalPlugins)
        {
            Element actionElement = document.createElement("action");
            getActionsElement().appendChild(actionElement);

            Element moduleNameElement = document.createElement("module-name");
            moduleNameElement.setTextContent(externalPlugin.getModuleName());
            actionElement.appendChild(moduleNameElement);

            
            Element versionElement = document.createElement("version");
            versionElement.setTextContent(externalPlugin.getVersion().getText());
            actionElement.appendChild(versionElement);

            Element actionTypeElement = document.createElement("type");
            actionTypeElement.setTextContent(externalPlugin.getActionType().toString());
            actionElement.appendChild(actionTypeElement);

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

    private PropertySaver propertySaver = null;

    /**
     * Set PropertySaver class
     * @param propertySaver instance of PropertySaver
     */
    public void setPropertySaver(PropertySaver propertySaver)
    {
        this.propertySaver = propertySaver;
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
            for(ExternalPlugin eachPlugin : externalPlugins)
            {
                try
                {
                    eachPlugin.onShutDown();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
    
            externalPlugins.clear();
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
            throw new MinorException("Config", "unable to save server plugins settings");
        }
    }
}
