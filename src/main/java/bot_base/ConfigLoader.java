package bot_base;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader
{
    private static final String CONFIG_FILE = "src/main/resources/config.properties";
    private static final Properties properties;

    // static variables initializer
    static
    {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE))
        {
            properties.load(fis);
        }
        catch (IOException e)
        {
            System.err.println("Failed to load config.properties: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     *
     * @param key string that contains requested property
     * @return the property in String
     */
    public static String get (String key)
    {
        return properties.getProperty(key);
    }
}
