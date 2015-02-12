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
 * This handles all max file size exceptions within the classes of NU.
 * @author davidepastore
 */
public class NUMaxFileSizeException extends NUFileException {
    
    private String maxFileSize;
    
    /**
     * Constructs an instance of
     * <code>NUMaxFileSizeException</code> with the file name, max file size
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param maxFileSize the max file size.
     * @param hostName the hostname.
     */
    public NUMaxFileSizeException(long maxFileSize, String fileName, String hostName) {
        super(NUException.MAX_FILE_SIZE);
        this.maxFileSize = CommonUploaderTasks.getSize(maxFileSize);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + fileName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+" "+this.maxFileSize+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
