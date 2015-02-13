/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.*;
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
import org.apache.http.HttpEntity;
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
 * @author Dinesh
 * @author davidepastore
 */
public class UploadingDotComAccount extends AbstractAccount {

    private String loginresponse = "";
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String postUrl = "http://uploading.com/general/login_form";

    public UploadingDotComAccount() {
        KEY_USERNAME = "udcusername";
        KEY_PASSWORD = "udcpassword";
        HOSTNAME = "Uploading.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    private void loginUploadingdotcom() throws Exception {
        loginsuccessful = false;
        
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        NULogger.getLogger().info("Trying to log in to uploading.com");
        
        httpPost = new NUHttpPost(postUrl);

        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair("remember", "on"));
        formparams.add(new BasicNameValuePair("recaptcha_response_field", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity en = httpResponse.getEntity();
        loginresponse = EntityUtils.toString(en);
        //NULogger.getLogger().log(Level.INFO, "Login response : {0}", loginresponse);
        NULogger.getLogger().info("Getting cookies........");
        if(CookieUtils.existCookie(httpContext, "u")){
            loginsuccessful = true;
            NULogger.getLogger().info("Cookie exists!");
        }
        else{
            throw new NUInvalidLoginException(this.getUsername(), this.getHOSTNAME());
        }

        if (loginsuccessful) {
            NULogger.getLogger().info("Uploading.com Login successful. :)");
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            HostsPanel.getInstance().uploadingDotComCheckBox.setEnabled(true);
        }
    }

    @Override
    public void login() {
        try {
            loginUploadingdotcom();
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.INFO, "Error in Uploading.com login: {0}", e);
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }

    }
    
    /**
     * Return the HttpContext.
     * @return the HttpContext.
     */
    public HttpContext getHttpContext(){
        return this.httpContext;
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        HostsPanel.getInstance().uploadingDotComCheckBox.setEnabled(false);
        HostsPanel.getInstance().uploadingDotComCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
    }
}
