package com.StreamPi.Server.Action;

import com.StreamPi.ActionAPI.Action.Action;
import com.StreamPi.ActionAPI.Action.ActionType;
import com.StreamPi.ActionAPI.Action.ServerConnection;
import com.StreamPi.ActionAPI.Action.PropertySaver;
import com.StreamPi.ActionAPI.ActionProperty.ClientProperties;
import com.StreamPi.ActionAPI.ActionProperty.Property.ControlType;
import com.StreamPi.ActionAPI.ActionProperty.Property.Property;
import com.StreamPi.ActionAPI.ActionProperty.Property.Type;
import com.StreamPi.ActionAPI.ActionProperty.ServerProperties;
import com.StreamPi.ActionAPI.NormalAction.NormalAction;
import com.StreamPi.Server.Controller.Controller;
import com.StreamPi.Util.Exception.MinorException;
import com.StreamPi.Util.Exception.SevereException;
import com.StreamPi.Util.Exception.StreamPiException;
import com.StreamPi.Util.Version.Version;
import com.StreamPi.Util.XMLConfigHelper.XMLConfigHelper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.scene.image.ImageView;
import org.w3c.dom.NodeList;
import org.kordamp.ikonli.javafx.FontIcon;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;

public class NormalActionPlugins
{
    private static NormalActionPlugins instance = null;
    private final Logger logger;

    private File configFile;
    private Document document;

    private static String pluginsLocation = null;


    public static synchronized NormalActionPlugins getInstance()
    {
        if(instance == null)
        {
            instance = new NormalActionPlugins();
        }

        return instance;
    }

    public static void setPluginsLocation(String location)
    {
        pluginsLocation = location;
    }


    private NormalActionPlugins()
    {
        logger = Logger.getLogger(NormalActionPlugins.class.getName());
        normalPluginsHashmap = new HashMap<>();
    }

    public void init() throws SevereException, MinorException
    {
        registerPlugins();
        initPlugins();
    }

    public List<NormalAction> getPlugins()
    {
        return normalPlugins;
    }

    public NormalAction getPluginByModuleName(String name)
    {
        logger.info("Plugin being requested : "+name);
        Integer index = normalPluginsHashmap.getOrDefault(name, -1);
        if(index != -1)
        {
            return normalPlugins.get(index);
        }

        return null;
    }

    private List<NormalAction> normalPlugins;
    HashMap<String, Integer> normalPluginsHashmap;

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

        ArrayList<NormalAction> errorModules = new ArrayList<>();

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
 


            String name="";
            Version version;
            try
            {
                name = XMLConfigHelper.getStringProperty(eachActionElement, "module-name");
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

            Action action = new Action(ActionType.NORMAL);

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
            normalPlugins = ServiceLoader
                    .load(layer, NormalAction.class).stream()
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

        for (NormalAction eachPlugin : normalPlugins) {
            try {
                eachPlugin.setPropertySaver(propertySaver);
                eachPlugin.setServerConnection(serverConnection);
                eachPlugin.initProperties();

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
                    ArrayList<NormalAction> actions = new ArrayList<>();

                    sortedPlugins.put(eachPlugin.getCategory(), actions);
                }

                sortedPlugins.get(eachPlugin.getCategory()).add(eachPlugin);

                /*logger.debug("-----Custom Plugin Debug-----" +
                        "\nAction Type : " + eachPlugin.getActionType() +
                        "\nName : " + eachPlugin.getName() +
                        "\nFull Module Name : " + eachPlugin.getModuleName() +
                        "\nAuthor : " + eachPlugin.getAuthor() +
                        "\nCategory : " + eachPlugin.getCategory() +
                        "\nRepo : " + eachPlugin.getRepo() +
                        "\nVersion : " + eachPlugin.getVersion().getText() +
                        "\n---------------------------");*/


            } catch (Exception e) {
                e.printStackTrace();
                errorModules.add(eachPlugin);
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
            for(NormalAction e : errorModules)
            {
                normalPlugins.remove(e);
                errors.append("\n * ").append(e);
            }

            throw new MinorException("Plugins", errors.toString());
        }


        for(int i = 0;i<normalPlugins.size();i++)
        {
            normalPluginsHashmap.put(normalPlugins.get(i).getModuleName(), i);
        }
    }

    public void initPlugins() throws MinorException
    {
        StringBuilder errors = new StringBuilder("There were errors registering the following plugins. As a result, they have been omitted : ");
        boolean isError = false;

        for(NormalAction eachPlugin : normalPlugins)
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

    HashMap<String, ArrayList<NormalAction>> sortedPlugins;

    public HashMap<String, ArrayList<NormalAction>> getSortedPlugins()
    {
        return sortedPlugins;
    }

    private Element getActionsElement()
    {
        return (Element) document.getElementsByTagName("actions").item(0);
    }

    public void saveServerSettings() throws MinorException 
    {
        XMLConfigHelper.removeChilds(getActionsElement());

        for(NormalAction normalAction : normalPlugins)
        {
            Element actionElement = document.createElement("action");
            getActionsElement().appendChild(actionElement);

            Element moduleNameElement = document.createElement("module-name");
            moduleNameElement.setTextContent(normalAction.getModuleName());
            actionElement.appendChild(moduleNameElement);

            
            Element versionElement = document.createElement("version");
            versionElement.setTextContent(normalAction.getVersion().getText());
            actionElement.appendChild(versionElement);

            Element propertiesElement = document.createElement("properties");
            actionElement.appendChild(propertiesElement);

            for(String key : normalAction.getServerProperties().getNames())
            {
                for(Property eachProperty : normalAction.getServerProperties().getMultipleProperties(key))
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

    public void setPropertySaver(PropertySaver propertySaver)
    {
        this.propertySaver = propertySaver;
    }

    private ServerConnection serverConnection = null;

    public void setServerConnection(ServerConnection serverConnection)
    {
        this.serverConnection = serverConnection;
    }


    public NormalAction getActionFromIndex(int index)
    {
        return normalPlugins.get(index);
    }

    public void shutDownActions()
    {
        if(normalPlugins != null)
        {
            for(NormalAction eachPlugin : normalPlugins)
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
    
            normalPlugins.clear();
        }
    }

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
