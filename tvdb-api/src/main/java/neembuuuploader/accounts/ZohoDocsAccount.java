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
 * @author dinesh
 */
public class ZohoDocsAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;

    private static StringBuilder zohodocscookies = new StringBuilder();

    public ZohoDocsAccount() {
        KEY_USERNAME = "zdusername";
        KEY_PASSWORD = "zdpassword";
        HOSTNAME = "ZohoDocs.com";
    }

    public static StringBuilder getZohodocscookies() {
        return zohodocscookies;
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        //These code are necessary for account only sites.
        HostsPanel.getInstance().zohoDocsCheckBox.setEnabled(false);
        HostsPanel.getInstance().zohoDocsCheckBox.setSelected(false);
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

            NULogger.getLogger().info("Trying to log in to Zoho Docs");
            httpPost = new NUHttpPost("https://accounts.zoho.com/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("LOGIN_ID", getUsername()));
            formparams.add(new BasicNameValuePair("PASSWORD", getPassword()));
            formparams.add(new BasicNameValuePair("IS_AJAX", "true"));
            formparams.add(new BasicNameValuePair("remember", "-1"));
            formparams.add(new BasicNameValuePair("servicename", "ZohoPC"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            

            NULogger.getLogger().info("Getting cookies........");
            //CookieUtils.printCookie(httpContext);
            
            zohodocscookies = new StringBuilder(CookieUtils.getAllCookies(httpContext));

            if (CookieUtils.existCookie(httpContext, "IAMAGENTTICKET_un")) {
                NULogger.getLogger().info("Zoho Docs Login Success");
                HostsPanel.getInstance().zohoDocsCheckBox.setEnabled(true);
                
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
            } else {
                //Handle errors
                //FileUtils.saveInFile("ZohoDocsAccount.html", stringResponse);
                if(stringResponse.contains("Invalid username or password")){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //General errors
                throw new Exception("Generic error: "+stringResponse);
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "Error in ZohoDocs Login: {0}", e);
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }


    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        zohodocscookies = new StringBuilder();
    }
}