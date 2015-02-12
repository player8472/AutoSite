package neembuuuploader.utils;

import java.io.File;
import java.io.Serializable;
import neembuuuploader.interfaces.Uploader;

/** Use this class instead of map.
 * If there was more than one rows with same file, then map will eliminate them.
 * So using this class.
 * 
 * This class is immutable and is marked as Serializable..
 * 
 * @author vigneshwaran
 */
public class FileClassPair implements Serializable {
    //The file
    private final File file;
    //The class for that file.. Any class that implements Uploader interface.
    private final Class<? extends Uploader> hostclass;

    /**
     * Pass in each file and its associated class
     * @param file
     * @param hostclass 
     */
    public FileClassPair(File file, Class<? extends Uploader> hostclass) {
        this.file = file;
        this.hostclass = hostclass;
    }

    /**
     * 
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * 
     * @return the hostclass for that file
     */
    public Class<? extends Uploader> getHostclass() {
        return hostclass;
    }
    
    
}
