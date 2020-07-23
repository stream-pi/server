package com.StreamPi.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.HashMap;

public class io {

    static Logger logger = LoggerFactory.getLogger(io.class);

    public static String readFileRaw(String fileName) throws Exception
    {
        logger.info("Reading file "+fileName+" ...");
        StringBuilder toBeReturned = new StringBuilder();
        FileReader fileReader = new FileReader(fileName);
        int c;
        while((c=fileReader.read())>-1)
        {
            toBeReturned.append((char) c);
        }
        fileReader.close();
        logger.info("... File Read Done!");
        return toBeReturned.toString();
    }

    public static HashMap<String,String> readConfig() throws Exception
    {
        logger.info("Reading config file : config.properties ...");
        HashMap<String,String> toReturn = new HashMap<>();

        String content = readFileRaw("config.properties");
        String splitChar = "\n";
        if(content.contains("\r\n")) splitChar = "\r\n";
        for(String eachLine : content.split(splitChar))
        {
            String[] confPart = eachLine.split(" = ");
            toReturn.put(confPart[0],confPart[1]);
        }
        logger.info("... Config Read Done!");
        return toReturn;
    }
}
