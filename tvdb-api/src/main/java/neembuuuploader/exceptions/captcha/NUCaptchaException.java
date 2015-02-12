/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.exceptions.captcha;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.exceptions.NUException;

/**
 * If the captcha is incorrect.
 * @author davidepastore
 */
public class NUCaptchaException extends NUException{
    
    /**
     * Constructs an instance of
     * <code>NUCaptchaException</code> with the host name.
     *
     * @param hostName the host name.
     */
    public NUCaptchaException(String hostName) {
        super(NUException.CAPTCHA_ERROR, hostName);
    }
    
    @Override
    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+"</html>", this.hostName, JOptionPane.WARNING_MESSAGE);
    }
    
}
