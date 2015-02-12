/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;

/**
 * If the server has banned your IP.
 * @author davidepastore
 */
public class NUBannedIpException extends NUException {
    
    /**
     * Constructs an instance of
     * <code>NUBannedIpException</code> with the the host name.
     *
     * @param hostName the host name.
     */
    public NUBannedIpException(String hostName) {
        super(NUException.BANNED_IP, hostName);
    }

    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
