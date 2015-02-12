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

/**
 * If the upload failed for upload limit exceeded.
 * I think it refers to upload speed.
 * Take a look <a href="http://crocko.com/it/developers.html">here</a>.
 * @author davidepastore
 */
public class NUUploadLimitExceededException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUUploadLimitExceededException</code> with the file name
     * and the hostname.
     * 
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUUploadLimitExceededException(String fileName, String hostName) {
        super(NUException.UPLOAD_LIMIT_EXCEEDED);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" +fileName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
