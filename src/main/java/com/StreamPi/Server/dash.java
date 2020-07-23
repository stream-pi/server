package com.StreamPi.Server;

import com.StreamPi.ActionAPI.Action;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class dash extends base {

    Logger logger;

    public dash()
    {
        try {
            //Set up logger
            logger = LoggerFactory.getLogger(dash.class);
            //Initial Setup

            setupConfig();
            initNodes();

            new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    registerPlugins();
                    return null;
                }
            }).start();

            startServer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setupConfig() throws Exception
    {
        config = io.readConfig();
    }


    Thread serverThread;
    server s;
    public void startServer(){
        s = new server(Integer.parseInt(config.get("server-port")), this);
        serverThread = new Thread(s, "Server Thread");
        serverThread.setDaemon(false);
        serverThread.start();
    }

    public void closeServer()
    {
        if(serverThread!=null)
        {
            if(serverThread.isAlive())
            {
                s.close();
            }
        }
    }

    List<Action> plugins;

    synchronized public void registerPlugins() throws Exception{
        logger.info("Registering external plugins ...");
        Path pluginsDir = Paths.get(config.get("plugin-repository")); // Directory with plugins JARs

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

        // Now you can use the new module layer to find service implementations in it
        plugins = ServiceLoader
                .load(layer, Action.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());

        for(Action eachActionPlugin : plugins)
        {
            logger.debug("-----Custom Plugin Debug-----" +
                    "\nAction Type"+eachActionPlugin.getActionType() +
                    "\nName : "+eachActionPlugin.getName() +
                    "\nAuthor : "+eachActionPlugin.getAuthor() +
                    "\nRepo : "+eachActionPlugin.getRepo() +
                    "\nDescription : "+eachActionPlugin.getDescription()+
                    "\nVersion : "+eachActionPlugin.getVersion() +
                    "\nFull Module Name : "+eachActionPlugin.getModuleName() +
                    "\n---------------------------");

            System.out.println("\nAction on Server :");
            eachActionPlugin.actionOnServer();
        }

        logger.debug("All plugins registered!");
    }
}
