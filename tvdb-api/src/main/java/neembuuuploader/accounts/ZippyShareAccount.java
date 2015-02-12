/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.HostsPanel;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.accounts.NUInvalidLoginException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author davidepastore
 */
public class ZippyShareAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    private CookieStore cookieStore;
    
    private static String sessioncookie = "";
    
    public ZippyShareAccount() {
        KEY_USERNAME = "zipsusername";
        KEY_PASSWORD = "zipspassword";
        HOSTNAME = "Zippyshare.com";
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        sessioncookie = "";
        
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
    
    /**
     *
     * @return The session cookie of the ZippyShare account
     */
    public String getSessioncookie() {
        return sessioncookie;
    }

    @Override
    public void login() {
        
        loginsuccessful = false;
        sessioncookie = "";
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try{
            
            NULogger.getLogger().info("Trying to log in to ZippyShare");
            httpPost = new NUHttpPost("http://www.zippyshare.com/services/login");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("pass", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            //NULogger.getLogger().log(Level.INFO, "Sending: {0}", httppost.getAllHeaders());
            NULogger.getLogger().info("Getting cookies........");
            sessioncookie = CookieUtils.getAllCookies(httpContext);
            
            
            if (CookieUtils.existCookie(httpContext, "zipname")) {
                NULogger.getLogger().info("ZippyShare Login Success");
                HostsPanel.getInstance().zippyShareCheckBox.setEnabled(true);
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
            } else {
                //Handle errors
                //FileUtils.saveInFile("ZippyShareAccount.html", stringResponse);
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in ZippyShare Login {0}", e);
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
        
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
