/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.exceptions.accounts;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.exceptions.NUException;

/**
 * Too many tries: try to login after.
 * @author davidepastore
 */
public class NUTooManyTries extends NUAccountException{
    
    /**
     * Constructs an instance of
     * <code>NUTooManyTries</code> with the username and the hostname.
     *
     * @param userName the username.
     * @param hostName the hostname.
     */
    public NUTooManyTries(String userName, String hostName) {
        super(NUException.TOO_MANY_TRIES);
        this.hostName = hostName;
        this.userName = userName;
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + userName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
    
}
