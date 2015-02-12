/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.accounts.NUInvalidLoginException;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.api._crocko.CrockoApi;
import neembuuuploader.utils.NULogger;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
public class CrockoAccount extends AbstractAccount {

    private String apikey;

    public CrockoAccount() {
        KEY_USERNAME = "crusername";
        KEY_PASSWORD = "crpassword";
        HOSTNAME = "Crocko.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    
    @Override
    public void login() {

        loginsuccessful = false;
        try{
            apikey = CrockoApi.getAPIkey(getUsername(), getPassword());
            if(apikey != null){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                
                NULogger.getLogger().log(Level.INFO, "Crocko api key is: {0}", apikey);
                NULogger.getLogger().info("Crocko login successful :)");
            }
            else{
                throw new NUInvalidLoginException(getUsername(), HOSTNAME);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
            
        } catch (Exception e){
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e.toString()});
            resetLogin();
            
            NULogger.getLogger().info("Crocko.com Login failed :(");
                
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "<br/>"+CrockoApi.getError()+": "+CrockoApi.getErrorMessage()+"</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
            }
        }
    
    /**
     * Return the apikey of this account.
     * @return The apikey. 
     */
    public String getAPIkey() {
        return apikey;
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
