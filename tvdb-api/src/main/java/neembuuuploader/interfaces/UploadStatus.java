/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.interfaces;

import java.util.logging.Level;
import neembuuuploader.TranslationProvider;
import neembuuuploader.utils.NULogger;

/**Enum for Uploader's status
 *
 * @author Shashaank Tulsyan
 */
public enum UploadStatus {
    QUEUED,
    INITIALISING,
    GETTINGCOOKIE,
    UPLOADING,
    GETTINGLINK,
    UPLOADFINISHED,
    UPLOADFAILED,
    UPLOADSTOPPED,
    UPLOADINVALID,
    GETTINGERRORS,
    PLEASEWAIT,
    NA,
    
    TORETRY,
    RETRYING,
    REUPLOADING,
    RETRYFAILED,

    LOGGINGIN,
    LOGGEDIN,
    LOGGINGFAILED;
    
    /**
     * 
     * @return the locale specific text for a particular Enum value
     */
    public String getLocaleSpecificString(){
        
        try{
            return TranslationProvider.get(UploadStatus.class.getName()+"."+this.toString());
        }catch(Exception a){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), a});
            return "Error";
        }
 
    }
    
    /**
     * 
     * @return the default English text for a particular Enum value
     */
    public String getDefaultLocaleSpecificString(){
        
        try{
            return TranslationProvider.getDefault(UploadStatus.class.getName()+"."+this.toString());
        }catch(Exception a){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), a});
            return "Error";
        }
 
    }
}
