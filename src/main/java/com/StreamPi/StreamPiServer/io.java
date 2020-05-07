package com.StreamPi.StreamPiServer;

import java.io.FileReader;
import java.util.HashMap;

public class io {

    public static void pln(String txt)
    {
        System.out.println(Thread.currentThread().getName()+": "+txt);
    }

    public static String readFileRaw(String fileName) throws Exception
    {
        StringBuilder toBeReturned = new StringBuilder();
        FileReader fileReader = new FileReader(fileName);
        int c;
        while((c=fileReader.read())>-1)
        {
            toBeReturned.append((char) c);
        }
        fileReader.close();
        return toBeReturned.toString();
    }

    public static HashMap<String,String> readConfig() throws Exception
    {
        HashMap<String,String> toReturn = new HashMap<>();

        String content = readFileRaw(io.class.getResource("config.properties").toExternalForm().substring(5));
        String splitChar = "\n";
        if(content.contains("\r\n")) splitChar = "\r\n";
        for(String eachLine : content.split(splitChar))
        {
            String[] confPart = eachLine.split(" = ");
            toReturn.put(confPart[0],confPart[1]);
        }

        return toReturn;
    }
}
