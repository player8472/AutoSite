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
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NULogger;
import neembuuuploader.utils.NeembuuUploaderProperties;
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
 * @author dinesh
 */
public class MediaFireAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String ukeycookie = "";
    private static String skeycookie = "";
    private static String usercookie = "";
    private static String sessioncookie = "";

    public MediaFireAccount() {
        KEY_USERNAME = "mfusername";
        KEY_PASSWORD = "mfpassword";
        HOSTNAME = "MediaFire.com";
    }

    @Override
    public void login() {
        try {
            loginsuccessful = false;
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            httpGet = new NUHttpGet("http://mediafire.com/");
            httpResponse = httpclient.execute(httpGet, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            ukeycookie = CookieUtils.getCookieNameValue(httpContext, "ukey");
            NULogger.getLogger().info(ukeycookie);
            
            
            httpPost = new NUHttpPost("http://www.mediafire.com/dynamic/login.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("login_email", getUsername()));
            formparams.add(new BasicNameValuePair("login_pass", getPassword()));
            formparams.add(new BasicNameValuePair("submit_login", "Login+to+MediaFire"));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            NULogger.getLogger().info("Getting skey value........");
            skeycookie = CookieUtils.getCookieNameValue(httpContext, "skey");
            usercookie = CookieUtils.getCookieNameValue(httpContext, "user");
            sessioncookie = CookieUtils.getCookieNameValue(httpContext, "session");
            NULogger.getLogger().info(skeycookie);
            NULogger.getLogger().info(usercookie);
            NULogger.getLogger().info(sessioncookie);

            if (!CookieUtils.existCookie(httpContext, "user")) {
                //Handle errors
                //FileUtils.saveInFile("MediaFireAccount.html", stringResponse);
                //throw new NUInvalidLoginException(NUException.INVALID_LOGIN, getUsername(), getHOSTNAME());
                
                //Generic error
                throw new Exception("Error in Mediafire login.");
            } else {
                loginsuccessful = true;
                HostsPanel.getInstance().mediaFireCheckBox.setEnabled(true);

                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info(usercookie);
                NULogger.getLogger().info("MediaFire login success");
            }
        } /*catch(NUException ex){
            resetLogin();
            NeembuuUploaderProperties.setProperty(KEY_USERNAME, "");
            NeembuuUploaderProperties.setEncryptedProperty(KEY_PASSWORD, "");
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        }*/ catch (Exception ex) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in MediaFire Login:- \n {0}", ex);
            NeembuuUploaderProperties.setProperty(KEY_USERNAME, "");
            NeembuuUploaderProperties.setEncryptedProperty(KEY_PASSWORD, "");
            NULogger.getLogger().info("MediaFire login not successful");
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }

    }

    public String getUserCookie() {
        return usercookie;
    }

    public String getSKeyCookie() {
        return skeycookie;
    }

    public String getUKeyCookie() {
        return ukeycookie;
    }

    public String getSessioncookie() {
        return sessioncookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().mediaFireCheckBox.setEnabled(false);
        HostsPanel.getInstance().mediaFireCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        skeycookie = "";
        usercookie = "";
        sessioncookie = "";
        ukeycookie = "";
        username = "";
        password = "";
    }
}
