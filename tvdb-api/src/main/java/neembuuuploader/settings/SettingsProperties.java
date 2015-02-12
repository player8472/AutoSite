/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import neembuuuploader.utils.NULogger;
import neembuuuploader.versioning.UserImpl;

/** This class is used for storing settings properties separately from .nuproperties
 *
 * @author vigneshwaran
 */
public class SettingsProperties {
    
    //This is the property file (settings/settings.dat) for storing settings
    //This is separate from .nuproperties file which is stored separately in each user's home directory.
    //settings.dat is one file common for all users.
    private static File settingsFile = new File(System.getProperty("user.home")
            + File.separator + "nu_settings.dat");
            /*new File(AppLocation.getPath(),"settings"
            + File.separator + "settings.dat");*/
    
    //properties object.
    private static Properties properties = new Properties();

    //Non instantiable
    private SettingsProperties() {
    }
    
    //Static constructor. Must load properties from file into the properties object
    static {
        //Just in case
        // Giving issues of scrapped this code
    }
    
    public static void init(){
        settingsFile =
            new File(System.getProperty("user.home")
            + File.separator + "nu_settings.dat");
        
        try{
            if(!settingsFile.exists()){
                initializeFirstTimeUser();
            }
        }catch(Exception s){
            s.printStackTrace();
        }
        
        UserImpl.init(getUserId());
        UserImpl.I().keepChecking();
        
        //Load Properties
        loadProperties();
        
    }
    
    public static long getUserId(){
        String p = SettingsProperties.getProperty("user_id");
        long user_id;
        if(p==null){
            user_id = generateNewId();
            SettingsProperties.setProperty("user_id",Long.toString(user_id));
        }else  {
            try{
                user_id = Long.parseLong(p);
            }catch(Exception a){
                user_id = generateNewId();
                SettingsProperties.setProperty("user_id",Long.toString(user_id));
            }
        }
        return user_id;
    }
    
    private static long generateNewId(){
        return (long)(Math.random()*Long.MAX_VALUE);
    }
    
    private static void initializeFirstTimeUser()throws IOException{
        if(settingsFile.exists())return;
        settingsFile.createNewFile();
        properties.put("savecontrolstate", "TRUE");
        properties.put("savequeuedlinks", "TRUE");
        properties.put("showoverallprogress", "TRUE");
        properties.put("firstlaunch","TRUE");
        storeProperties();
    }
    
    /**
     * Write the properties to property file
     */
    public static void storeProperties() {
        NULogger.getLogger().info("Storing Properties");
        try {
            properties.store(new FileOutputStream(settingsFile), "Neembuu Uploader Settings... Do not modify manually..");
        } catch (FileNotFoundException ex) {
            NULogger.getLogger().log(Level.INFO, "Properties file not found: {0}", ex);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.INFO, "IOException while writing property file {0}", ex);
        }
        NULogger.getLogger().info("Properties stored successfully");
    }
    
    
    /**
     * Load the properties from property file
     */
    public static void loadProperties() {
        NULogger.getLogger().info("Loading Properties");
        try {
            properties.load(new FileInputStream(settingsFile));
        } catch (FileNotFoundException ex) {
            NULogger.getLogger().log(Level.INFO, "Properties file not found: {0}", ex);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.INFO, "IOException while reading property file {0}", ex);
        }

        NULogger.getLogger().info("Properties loaded successfully");

    }
    
    
    /**
     * Set the property with the specified key and value
     * @param key
     * @param value 
     */
    public static void setProperty(String key, String value) {
        //Always remember to load the properties before storing
        loadProperties();
        properties.setProperty(key, value);
        storeProperties();
    }
    
    /**
     * Get the value for a specified key. Returns "" if no value is present
     * @param key The property key
     * @return The value of the given property if exists
     */
    public static String getProperty(String key) {
        return properties.getProperty(key, "");
    }
    
    /**
     * Get the value for a specified key. Returns the specified defaultValue if no value present
     * @param key The property key
     * @param defaultValue A default value to return if none exists
     * @return The value of the given property if exists.. Or the specified default value if no value exists
     */
    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    
    
    /**
     * Whether the value for a given key is true or not.
     * @param key The Key of the property
     * @return A boolean value that indicates whether the property has a true
     * value or not.. If no value present, false is returned.
     */
    public static boolean isPropertyTrue(String key) {
        return Boolean.valueOf(properties.getProperty(key, "false"));
    }
}

