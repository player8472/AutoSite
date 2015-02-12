/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.logging.Level;
import neembuuuploader.utils.NULogger;

/**
 *
 * @author vigneshwaran
 */
public class AppLocation {

    private static File path = null;

    private AppLocation() {
    }

    public static File getPath() {
        if (path == null) {
            try {
                CodeSource src = AppLocation.class.getProtectionDomain().getCodeSource();
                boolean assume = false;
                String urlpth = null;
                if (src == null) {
                    System.err.println("Assuming because code source is null");
                    NULogger.getLogger().severe("Assuming because code source is null");
                    assume = true;//not used now
                } else {
                    if (src.getLocation().toString().endsWith("classes/")) {
                        urlpth = src.getLocation().toString();
                        urlpth = urlpth.substring(0, urlpth.lastIndexOf('/'));
                        urlpth = urlpth.substring(0, urlpth.lastIndexOf('/'));
                        urlpth = urlpth.substring(0, urlpth.lastIndexOf('/') + 1);
                        
                        NULogger.getLogger().log(Level.INFO, "Running in development mode, using properties = {0}", urlpth);
                    } else if (src.getLocation().toString().endsWith(".jar")) {
                        urlpth = src.getLocation().toString();
                        urlpth = urlpth.substring(0, urlpth.lastIndexOf('/') + 1);
                        
                        NULogger.getLogger().log(Level.INFO, "Running from jar, using properties = {0}", urlpth);
                        
                    }
                }
                path = new File(new URL(urlpth).toURI());
                NULogger.getLogger().info(path.getAbsolutePath());
            } catch (Exception a) {
                NULogger.getLogger().severe("Cannot get AppLocation!");
            }
        }
        return path;
    }
}
