package com.StreamPi.Server;

import com.StreamPi.ActionAPI.Action;
import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class dash extends dashBase {

    public dash()
    {
        try {
            setupConfig();
            initNodes();

            printPlugins();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        startServer();
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
        if(serverThread.isAlive())
        {
            s.close();
        }
    }

    List<Action> plugins = new ArrayList<>();
    public void printPlugins() throws Exception {
        ModuleFinder finder = ModuleFinder.of(Paths.get("pluginroot/"));
        ModuleLayer parent = ModuleLayer.boot();

        for(String moduleNameJAR : new File("pluginroot/").list())
        {
            String moduleName = moduleNameJAR.replace(".jar","");
            Configuration cf = parent.configuration().resolve(finder, ModuleFinder.of(), Set.of(moduleName));
            ClassLoader scl = ClassLoader.getSystemClassLoader();
            ModuleLayer layer = parent.defineModulesWithOneLoader(cf, scl);
            Action test = (Action) layer.findLoader(moduleName).loadClass(moduleName+".Action").getDeclaredConstructor().newInstance();
            plugins.add(test);
        }

        for(Action eachActionPlugin : plugins)
        {
            System.out.println("-----Custom Action Info-----" +
                    "\nName : "+eachActionPlugin.getName() +
                    "\nAuthor : "+eachActionPlugin.getAuthor() +
                    "\nRepo : "+eachActionPlugin.getRepo() +
                    "\nDescription : "+eachActionPlugin.getDescription()+
                    "\nVersion : "+eachActionPlugin.getVersion() +
                    "\n---------------------------");

            System.out.println("\nAction on Server :");
            eachActionPlugin.actionOnServer();
        }
    }
}
