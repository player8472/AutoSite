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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author dinesh
 */
public class OneFichierAccount extends AbstractAccount {

    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private static String sidcookie;

    public OneFichierAccount() {
        KEY_USERNAME = "ofusername";
        KEY_PASSWORD = "ofpassword";
        HOSTNAME = "1fichier.com";
    }

    public static String getSidcookie() {
        return sidcookie;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        try {
            NULogger.getLogger().info("Trying to log in to 1fichier.com");
            httpPost = new NUHttpPost("https://1fichier.com/login.pl");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();

            formparams.add(new BasicNameValuePair("mail", getUsername()));
            formparams.add(new BasicNameValuePair("pass", getPassword()));
            formparams.add(new BasicNameValuePair("valider", "Send"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().info("Getting cookies........");
            if(CookieUtils.existCookie(httpContext, "SID")){
                sidcookie = CookieUtils.getCookieNameValue(httpContext, "SID");
                loginsuccessful = true;
                NULogger.getLogger().info(sidcookie);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("1fichier login successful :)");
            }
            else{
                //Handle errors
                String error;
                Document doc = Jsoup.parse(stringResponse);
                error = doc.select("div#masterdiv form div span").first().text();
                
                if(error.contains("Invalid username or password.")){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                throw new Exception("OneFichier login failed with error: "+error);
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
            Logger.getLogger(OneFichierAccount.class.getName()).log(Level.SEVERE, null, e);
        }

    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        sidcookie = "";
    }
}
