/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author vigneshwaran
 */
public class TwoSharedAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private static StringBuilder cookies = null;

    public static String getCookies() {
        return cookies.toString();
    }

    
    public TwoSharedAccount() {
        KEY_USERNAME = "tsusername";
        KEY_PASSWORD = "tspassword";
        HOSTNAME = "2Shared.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        //These code are necessary for account only sites.
        HostsPanel.getInstance().twoSharedCheckBox.setEnabled(false);
        HostsPanel.getInstance().twoSharedCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {

        loginsuccessful = false;
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            NULogger.getLogger().info("Trying to log in to 2shared.com");
            httpPost = new NUHttpPost("http://www.2shared.com/login.jsp");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info("Getting cookies........");
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            cookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));

            if (CookieUtils.existCookie(httpContext, "Login")) {
                NULogger.getLogger().info("2Shared login success :)");
                NULogger.getLogger().log(Level.INFO, "Cookies : {0}", cookies);

                HostsPanel.getInstance().twoSharedCheckBox.setEnabled(true);
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
            } else {
                //Handle errors
                //FileUtils.saveInFile("TwoSharedAccount.html", stringResponse);
                //NULogger.getLogger().log(Level.INFO, "Cookies : {0}", cookies);
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
            Logger.getLogger(TwoSharedAccount.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        cookies = null;
    }
}
