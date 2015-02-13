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
 * Account locked exception: the account is temporarily locked.
 * @author davidepastore
 */
public class NULockedAccountException extends NUAccountException{
    
    /**
     * Constructs an instance of
     * <code>NULockedAccountException</code> with the username and the hostname.
     *
     * @param userName the username.
     * @param hostName the hostname.
     */
    public NULockedAccountException(String userName, String hostName) {
        super(NUException.LOCKED_ACCOUNT);
        this.hostName = hostName;
        this.userName = userName;
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + userName+ ":<br/>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
}
