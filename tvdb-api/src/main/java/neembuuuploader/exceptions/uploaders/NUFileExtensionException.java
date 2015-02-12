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
 * If the file that you want to upload has a not allowed extension.
 * @author davidepastore
 */
public class NUFileExtensionException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUUploadFailedException</code> with the file name
     * and the hostname.
     * @param fileName the file name.
     * @param hostName the hostname.
     */
    public NUFileExtensionException(String fileName, String hostName) {
        super(NUException.FILE_EXTENSION);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + fileName+ ":<br/>"+TranslationProvider.get("neembuuuploader.uploaders.filetypenotsupported")+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
