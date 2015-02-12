/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.math.BigInteger;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
import neembuuuploader.utils.NULogger;
//import org.apache.http.Header;
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
import neembuuuploader.uploaders.common.StringUtils;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;

/**
 *
 * @author MNidhal
 */
public class OboomAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    public String api_key = "";
    

    public OboomAccount() {
        KEY_USERNAME = "obousername";
        KEY_PASSWORD = "obopassword";
        HOSTNAME = "Oboom.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().obOomCheckBox.setEnabled(false);
        HostsPanel.getInstance().obOomCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();
            NULogger.getLogger().info("Trying to log in to Oboom.com");
            httpPost = new NUHttpPost("https://www.oboom.com/1.0/login");
            String pass = new StringBuffer(getPassword()).reverse().toString();
            
            byte[] salt = pass.getBytes();
            KeySpec spec = new PBEKeySpec(getPassword().toCharArray(), salt, 1000, 128);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("auth", getUsername()));
            formparams.add(new BasicNameValuePair("pass", new BigInteger(1, hash).toString(16)));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            //FileUtils.saveInFile("OboomAccount.html", responseString);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());

            if (responseString != null && responseString.contains("cookie")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                api_key = StringUtils.stringBetweenTwoStrings(responseString, "api_key\":\"", "\",");
                HostsPanel.getInstance().obOomCheckBox.setEnabled(true);
                NULogger.getLogger().info("Oboom.com login successful!");
                

            } else {
                //Get error message
                //FileUtils.saveInFile("OboomAccount.html", responseString);
                
                if(responseString.contains("Invalid Login Credentials")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " );
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        //NULogger.getLogger().info("Getting startup cookies & link from Oboom.com");
        //responseString = NUHttpClientUtils.getData("http://www.oboom.com/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
