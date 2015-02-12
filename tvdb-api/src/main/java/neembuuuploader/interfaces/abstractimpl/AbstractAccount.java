/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.interfaces.abstractimpl;

import neembuuuploader.interfaces.Account;
import neembuuuploader.utils.NeembuuUploaderProperties;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author vigneshwaran
 */
public abstract class AbstractAccount implements Account {

    //These 3 variables must be overridden specifically by the plugin developer.
    protected String KEY_USERNAME = "";
    protected String KEY_PASSWORD = "";
    protected String HOSTNAME = "";
    
    //This account is premium?
    protected boolean premium = false;
    
    protected HttpContext httpContext;
    
    public String username = "";
    public String password = "";
    public boolean loginsuccessful = false;

    public String getKeyUsername() {
        return KEY_USERNAME;
    }

    public String getKeyPassword() {
        return KEY_PASSWORD;
    }

    public String getHOSTNAME() {
        return HOSTNAME;
    }
    
    public HttpContext getHttpContext() {
        return httpContext;
    }

    public String getUsername() {
        return NeembuuUploaderProperties.getProperty(KEY_USERNAME);
    }

    public String getPassword() {
        return NeembuuUploaderProperties.getEncryptedProperty(KEY_PASSWORD);
    }

    public boolean isLoginSuccessful() {
        return loginsuccessful;
    }
    
    public boolean isPremium() {
        return premium;
    }
    
    /**This is to prevent logging in when the credentials are changed */
    public boolean canLogin() {
        if(username.equals(getUsername()) && password.equals(getPassword()))
            return false;
        return true;
    }

    public abstract void disableLogin();

    public abstract void login();
   
}
