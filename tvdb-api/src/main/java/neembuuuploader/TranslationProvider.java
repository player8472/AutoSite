package neembuuuploader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.settings.SettingsManager;
import neembuuuploader.utils.NULogger;

/**
 *
 * @author Shashank Tulsyan
 */
public final class TranslationProvider {

    private final Properties properties;
    private final String language;
    //guarded by this class
    private static TranslationProvider singleton = null;
    private static TranslationProvider defaultprovider = null;

    /**
     * 
     * @return a translation provider based on default locale. If none exist, get TranslationProvider for English
     */
    public static synchronized TranslationProvider getTranslationProvider() {
        if (singleton == null) {
//            singleton = new TranslationProvider(Locale.getDefault().getLanguage());
//            defaultprovider = new TranslationProvider("en");
//            if (singleton.properties == null) {
//                singleton = defaultprovider;
//            }
            singleton = new TranslationProvider("en");
        }
        return singleton;
    }

    /**
     * 
     * @return TranslationProvider with English language as default
     */
    public static synchronized TranslationProvider getDefaultTranslationProvider() {
        if (defaultprovider == null) {
            defaultprovider = new TranslationProvider("en");
        }
        return defaultprovider;
    }

    /**
     * 
     * @return language currently in use
     */
    public final String getLanguage() {
        return language;
    }

    /**
     * Private constructor..
     * @param language 
     */
    private TranslationProvider(final String language) {
        this.language = language;
        Properties p = new Properties();
        try {
            InputStream prof = null;
            String urlpth = "translations/NeembuuUploader_"
                    + language
                    + ".properties";
            prof = new FileInputStream(new File(AppLocation.getPath(), urlpth));
            p.load(prof);


        } catch (Exception a) {
            Logger.getLogger(TranslationProvider.class.getName()).log(Level.SEVERE, "Could not set language for language " + language, a);;
            TranslationProvider.changeLanguage("en");
        }

        properties = p;
    }

    /**
     * Use this method to get the locale specific value for a key
     * @param property
     * @return locale specific value for a key
     */
    public static String get(String property) {
        Properties p = TranslationProvider.getTranslationProvider().properties;
        if (p == null) {
            return property;
        }
        String localvalue = p.getProperty(property,"");
        if (!localvalue.isEmpty()) {
            return localvalue;
        } else {
            return TranslationProvider.getDefaultTranslationProvider().properties.getProperty(property);
        }
    }

    /**
     * 
     * @param property
     * @return value for a key in default ENGLISH
     */
    public static String getDefault(String property) {
        return TranslationProvider.getDefaultTranslationProvider().properties.getProperty(property, "");
    }

    /**This is the method used to initialize TranslationProvider and also to change language.
     * It also updates the GUI of NeembuuUploader and Table automatically.
     * @param locale 
     * @throws SecurityException The program is designed to allow
     * changes from only NeembuuUploader.class and TranslationProvider.class .
     * This is to prevent, malicious behavior.
     */
    public static synchronized void changeLanguage(String languagecode) { /*package private*/
        // getStackTrace()[0] will be getStackTrace()
        // getStackTrace()[1] will be changeLanguage()
        // getStackTrace()[2] will be the calling class which should be one of the three allowed classes.
        String name = Thread.currentThread().getStackTrace()[2].getClassName(); // takes around 228.3 microsec
        //name = sun.reflect.Reflection.getCallerClass(2).getName(); // takes around 40 microsecs*/
        if (!name.equals(NeembuuUploader.class.getName())
                && !name.equals(TranslationProvider.class.getName())
                && !name.equals(SettingsManager.class.getName())) {
            throw new SecurityException(name + " does not have rights to call this function");
        }
        singleton = new TranslationProvider(languagecode);

        NULogger.getLogger().log(Level.INFO, "{0}Language Changed", TranslationProvider.class.getName());

        //Update GUI on NU
        NeembuuUploader.getInstance().languageChanged_UpdateGUI();

    }
//    public static void main(String[] args) {
//        TranslationProvider.changeLanguage("hi");
//        System.out.println(TranslationProvider.get("neembuuuploader.LocaleManager.testString"));       
//        JOptionPane.showMessageDialog(null, TranslationProvider.get("neembuuuploader.LocaleManager.unicodeString"), "Language",JOptionPane.PLAIN_MESSAGE );
//        
//        TranslationProvider.changeLanguage(Locale.ENGLISH.getLanguage());
//        System.out.println(TranslationProvider.get("neembuuuploader.LocaleManager.testString"));       
//        JOptionPane.showMessageDialog(null, TranslationProvider.get("neembuuuploader.LocaleManager.unicodeString"), "Language",JOptionPane.PLAIN_MESSAGE );
//        
//        System.exit(0);
//    }
}
