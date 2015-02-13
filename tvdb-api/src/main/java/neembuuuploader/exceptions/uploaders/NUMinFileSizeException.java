/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.exceptions.uploaders;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.uploaders.common.CommonUploaderTasks;

/**
 * If the file you want to upload has a size less than that provided
 * @author davidepastore
 */
public class NUMinFileSizeException extends NUFileException {
    
    private String minFileSize;
    
    /**
     * Constructs an instance of
     * <code>NUMinFileSizeException</code> with min file size, the file name
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param minFileSize the max file size.
     * @param hostName the hostname.
     */
    public NUMinFileSizeException(long minFileSize, String fileName, String hostName) {
        super(NUException.MIN_FILE_SIZE);
        this.minFileSize = CommonUploaderTasks.getSize(minFileSize);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + fileName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+" "+this.minFileSize+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
    
}
