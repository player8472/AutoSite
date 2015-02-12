/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import neembuuuploader.settings.SettingsProperties;

/**This class is used for Language/Locale related operations.
 * It is closely dependant on operations of SettingsProperties class. So that class will be instantiated if not already
 * @author vigneshwaran
 */
public class NeembuuUploaderLanguages {

    //This is a mapping of language code with it's full English name
    public static final Map<String, String> languagemap = new LinkedHashMap<String, String>();
    
    //This is a file object that refers to the location of the fallback font..
    //This is used only for Tamil language..
    private static final File fallbackfont = new File(System.getProperty("java.home") + File.separator
            + "lib" + File.separator + "fonts" + File.separator + "fallback"
            + File.separator + "LATHA.TTF");
    

    //List of short language codes
    //When a new language is added, add the code here and the appropriate mapping in further code.
    public static final String BRAZILIAN_PORTUGESE = "pt_BR";
    public static final String CATALAN = "ca";
    public static final String CHINESE_SIMPLIFIED = "zh_CN";
    public static final String CHINESE_TRADITIONAL_TAIWAN = "zh_TW";
    public static final String DUTCH = "nl";
    public static final String ENGLISH = "en";
    public static final String FRENCH = "fr";
    public static final String GERMAN = "de";
    public static final String GREEK = "el";
    public static final String HEBREW = "iw";
    public static final String HINDI = "hi";
    public static final String HUNGARIAN = "hu";
    public static final String ITALIAN = "it";
    public static final String MALAY = "ms";
    public static final String RUSSIAN = "ru";
    public static final String SOURASHTRA = "saz";
    public static final String SPANISH = "es";
    public static final String TAMIL = "ta";
    public static final String TURKISH = "tr";
    public static final String VIETNAMESE = "vi";
    
    
    //Static initializer maps the language shortcodes with their full names.
    //When you add a new language, add the short code above and map with the full name here
    static {
        languagemap.put(BRAZILIAN_PORTUGESE, "Brazilian Portugese");
        languagemap.put(CATALAN, "Catalan");
        languagemap.put(CHINESE_SIMPLIFIED, "Chinese (Simplified)");
        languagemap.put(CHINESE_TRADITIONAL_TAIWAN, "Chinese (Traditional)");
        languagemap.put(DUTCH, "Dutch");
        languagemap.put(ENGLISH, "English");
        languagemap.put(FRENCH, "French");
        languagemap.put(GERMAN, "German");
        languagemap.put(GREEK, "Greek");
        languagemap.put(HEBREW, "Hebrew");
        languagemap.put(HINDI, "Hindi");
        languagemap.put(HUNGARIAN, "Hungarian");
        languagemap.put(ITALIAN, "Italian");
        languagemap.put(MALAY, "Malay (Incomplete)");
        languagemap.put(RUSSIAN, "Russian");
        languagemap.put(SOURASHTRA, "Sourashtra");
        languagemap.put(SPANISH, "Spanish");
        
        //Since Tamil is not supported by Java, do not display it unless the workaround fallback font exists..
        if (fallbackfont.exists()) {
            NULogger.getLogger().log(Level.INFO, "{0}: Tamil available..", NeembuuUploaderLanguages.class.getName());
            languagemap.put(TAMIL, "Tamil");
        }
        languagemap.put(TURKISH, "Turkish");
        languagemap.put(VIETNAMESE, "Vietnamese (Incomplete)");
    }
    
    
    /**
     * 
     * @return the list of all language short codes as a String array[]
     */
    public static String[] getLanguageCodes() {
        return languagemap.keySet().toArray(new String[0]);
    }
    
    /**
     * 
     * @return the list of all full language names in English as a String array[]
     */
    public static String[] getLanguageNames() {
        return languagemap.values().toArray(new String[0]);
    }
    
    /**
     * Get the language full name for the specified code
     * @param code
     * @return 
     */    
    public static String getLanguageNameByCode(String code) {
        return languagemap.get(code);
    }

    //Non instantiable.. Use getInstance()..
    private NeembuuUploaderLanguages() {
    }

    /**
     * 
     * @return the language code for the language set by the user. 
     * If none available, return "en"
     */
    public static String getUserLanguageCode() {
        return SettingsProperties.getProperty("userlang", ENGLISH);
    }
    
    /**
     * 
     * @return the full language name for the language set by the user
     */
    public static String getUserLanguageName() {
        return getLanguageNameByCode(getUserLanguageCode());
    }

    /**
     * Set the user language with the specified code.
     * @param langcode 
     */
    public static void setUserLanguageCode(String langcode) {
        SettingsProperties.setProperty("userlang", langcode);
    }
    
    /**To be used by the SettingsManager class. 
     * Set the user language by the index of the languages combobox.
     * 
     * @param i 
     */
    public static void setUserLanguageByIndex(int i) {
        setUserLanguageCode(getLanguageCodes()[i]);
    }
    
    /**To be used by the SettingsManager class. 
     * Set the user language by the index of the languages combobox.
     * 
     * @param i 
     */
    public static void setUserLanguageByName(String selectedlanguage) {
        int i = 0;
        for(String language : getLanguageNames()) {
            if(language.equals(selectedlanguage)) {
                setUserLanguageCode(getLanguageCodes()[i]);
                break;
            }
            i++;
        }
    }
}
