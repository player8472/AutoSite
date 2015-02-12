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
 * @author Dinesh
 */
public class LetitbitAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;

    private static String phpsessioncookie;
    private static String debugcookie;
    private static String logcookie;
    private static String pascookie;
    private static String hostcookie;

    public LetitbitAccount() {
        KEY_USERNAME = "libusername";
        KEY_PASSWORD = "libpassword";
        HOSTNAME = "Letitbit.net";
    }

    public String getDebugcookie() {
        return debugcookie;
    }

    public String getHostcookie() {
        return hostcookie;
    }

    public String getLogcookie() {
        return logcookie;
    }

    public String getPascookie() {
        return pascookie;
    }

    public String getPhpsessioncookie() {
        return phpsessioncookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().letitbitCheckBox.setEnabled(false);
        HostsPanel.getInstance().letitbitCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            initialize();
            loginLetitbit();
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in Letitbit Login: {0}", ex.getStackTrace());
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from letitbit.net");
        httpGet = new NUHttpGet("http://www.letitbit.net/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        //CookieUtils.printCookie(httpContext);
        
        phpsessioncookie = CookieUtils.getCookieValue(httpContext, "PHPSESSID");
        debugcookie = "";
        NULogger.getLogger().log(Level.INFO, "phpsessioncookie: {0}", phpsessioncookie);
        NULogger.getLogger().log(Level.INFO, "debugcookie : {0}", debugcookie);
    }

    public void loginLetitbit() throws Exception {
        loginsuccessful = false;

        NULogger.getLogger().info("Trying to log in to letitbit.com");
        httpPost = new NUHttpPost("http://letitbit.net/");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("act", "login"));
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        
        if(CookieUtils.existCookie(httpContext, "log")){
            logcookie = CookieUtils.getCookieNameValue(httpContext, "log");
            pascookie = CookieUtils.getCookieNameValue(httpContext, "pas");
            hostcookie = CookieUtils.getCookieNameValue(httpContext, "host");
            loginsuccessful = true;
            NULogger.getLogger().info(logcookie);
        }
        
        if (loginsuccessful) {
            loginsuccessful = true;
            username = getUsername();
            password = getPassword();
            HostsPanel.getInstance().letitbitCheckBox.setEnabled(true);
            NULogger.getLogger().info("Letitbit.net Login success :)");
        } else {
            //Handle errors
            //FileUtils.saveInFile("LetitbitAccount.html", stringResponse);
            NULogger.getLogger().info("Letitbit.net Login failed :(");
            throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
        }

    }
    
    private void resetLogin(){
        loginsuccessful = false;
        debugcookie = "";
        hostcookie = "";
        logcookie = "";
        pascookie = "";
        phpsessioncookie = "";
        username = "";
        password = "";
    }
}
