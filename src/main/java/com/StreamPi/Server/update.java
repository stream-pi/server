package com.StreamPi.Server;

//The Java NIO package offers the possibility to transfer bytes between 2 Channels
//without buffering them into the application memory.

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class update {
    String Link;
    String OutputDir;

    public update(String link, String outputDir) {
        this.Link = link;
        this.OutputDir = outputDir;
    }

    public void download() throws IOException {
        try {
            URL url = new URL(this.Link);
            //Create a byte channel from URL stream data
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(this.OutputDir);
            //use the file output stream to write it to a file
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String check() throws IOException {
        try {
            //Create HttpURLConnection
            HttpURLConnection HTTP_CON = (HttpURLConnection)
                    new URL("https://api.github.com/repositories/199320695/releases").openConnection();
            HTTP_CON.addRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(HTTP_CON.getInputStream()));

            //Read line by line
            StringBuilder response_SB = new StringBuilder();
            String line;
            while ( ( line = in.readLine() ) != null) {
                response_SB.append("\n" + line);
            }
            in.close();

            List<String> myList = new ArrayList<String>();

            Arrays.stream(response_SB.toString()
                    .split("\"tag_name\":")).skip(1)
                    .map(l -> l.split(",")[0]).forEach(myList::add);

            return myList.get(0);

        } catch(IOException e) {
            e.printStackTrace();
            return "null";
        }

    }
}
