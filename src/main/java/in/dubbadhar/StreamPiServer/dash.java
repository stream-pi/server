package in.dubbadhar.StreamPiServer;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class dash extends dashBase {
    Configuration config;

    public dash()
    {

    }

    public void setupConfig()
    {
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class).configure(new Parameters().properties().setFileName("config.properties"));
        try {
            builder.setAutoSave(true);
            config = builder.getConfiguration();
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
    }
}
