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
 * Invalid user exception: user is incorrect.
 * @author davidepastore
 */
public class NUInvalidUserException extends NUAccountException{
    
    /**
     * Constructs an instance of
     * <code>NUInvalidUserException</code> with the username and the hostname.
     *
     * @param userName the username.
     * @param hostName the hostname.
     */
    public NUInvalidUserException(String userName, String hostName) {
        super(NUException.INVALID_USER);
        this.hostName = hostName;
        this.userName = userName;
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + userName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
