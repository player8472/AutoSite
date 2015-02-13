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
 * If the file that you want to upload is in black list.
 * @author davidepastore
 */
public class NUFileInBlackListException extends NUFileException {
    
    /**
     * Constructs an instance of
     * <code>NUFileInBlackListException</code> with the file name and the host name.
     *
     * @param fileName the file name.
     * @param hostName the host name.
     */
    public NUFileInBlackListException(String fileName, String hostName) {
        super(NUException.FILE_IN_BLACK_LIST);
        this.fileName = fileName;
        this.hostName = hostName;
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + fileName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);

    }
}
