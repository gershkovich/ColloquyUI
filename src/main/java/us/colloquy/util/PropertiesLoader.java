package us.colloquy.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class PropertiesLoader
{

    /**
     * Loads properties from "properties" directory
     * @param properties
     * @param propertiesFile
     */
    public static void loadProperties(Properties properties, String propertiesFile)
    {

        InputStream propertiesFileInputStream = null;

        try
        {
            propertiesFileInputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream("properties" + File.separator + propertiesFile);

            if ( propertiesFileInputStream != null )
            {

                properties.loadFromXML(propertiesFileInputStream);

                properties.list(System.out);

                propertiesFileInputStream.close();
            }
            else
            {

                System.out.print("Property file not available");

            }

        } catch ( Exception e )
        {
            e.printStackTrace();

        } finally
        {

            if ( propertiesFileInputStream != null )
            {
                try
                {
                    propertiesFileInputStream.close();

                } catch ( IOException e )
                {
                    // Do nothing.
                }
            }
        }
    }


}
