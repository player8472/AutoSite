/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.accounts.NUInvalidLoginException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.utils.NULogger;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author davidepastore
 */
public class UploadizAccount extends AbstractAccount{
    
    private HttpClient httpClient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String loginResponse;

    private String loginURL = "http://www.uploadiz.com/";
    
    public UploadizAccount() { 
       KEY_USERNAME = "uiusername"; 
       KEY_PASSWORD = "uipassword"; 
       HOSTNAME = "Uploadiz.com"; 
   }

    @Override
    public void disableLogin() {
        resetLogin();
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            httpPost = new NUHttpPost(loginURL);
            Header locations[];
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("op", "login"));
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpClient.execute(httpPost, httpContext);
            loginResponse = EntityUtils.toString(httpResponse.getEntity());
            locations = httpResponse.getHeaders("Location");
            
            for(int i = 0; i < locations.length; i++){
                if("http://www.uploadiz.com/?op=my_files".equals(locations[i].getValue())){
                    NULogger.getLogger().info("Uploadiz Login successful");
                    loginsuccessful = true;
                    username = this.getUsername();
                    password = getPassword();
                }
                if("login.html&msg=Incorrect Login or Password".equals(locations[i].getValue())){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (IOException ex) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in Uploadiz Login \n Exception: {1}", new Object[]{getClass().getName(), ex.getCause()});
            resetLogin();

            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "<br/></html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }
    
    /**
     * Return the httpContext.
     * @return the httpcontext.
     */
    public HttpContext getHttpContext(){
        return this.httpContext;
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
