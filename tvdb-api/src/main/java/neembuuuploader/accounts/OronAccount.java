/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
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
 * @author dinesh
 */
public class OronAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String logincookie;
    private static String xfsscookie;

    public OronAccount() {
        KEY_USERNAME = "orusername";
        KEY_PASSWORD = "orpassword";
        HOSTNAME = "Oron.com";
    }

    public static String getLogincookie() {
        return logincookie;
    }

    public static String getXfsscookie() {
        return xfsscookie;
    }

    public void disableLogin() {
        loginsuccessful = false;
        logincookie = "";
        xfsscookie = "";
        username = "";
        password = "";
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public void login() {
        loginsuccessful = false;
        
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {

            NULogger.getLogger().info("Trying to log in to oron.com");
            httpPost = new NUHttpPost("http://oron.com/login");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("login", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("op", "login"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().info("Getting cookies........");
            if(CookieUtils.existCookie(httpContext, "xfss")){
                logincookie = CookieUtils.getCookieNameValue(httpContext, "login");
                xfsscookie = CookieUtils.getCookieNameValue(httpContext, "xfss");
                loginsuccessful = true;
            }

            if (loginsuccessful) {
                NULogger.getLogger().info("Oron Login successful :)");
                username = getUsername();
                password = getPassword();
            } else {
                NULogger.getLogger().info("Oron Login failed :(");
                loginsuccessful = false;
                username = "";
                password = "";
                JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
                AccountsManager.getInstance().setVisible(true);
            }



        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e.toString()});
            System.err.println(e);
        }
    }
}
