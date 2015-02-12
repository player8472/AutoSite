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
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class YouTubeAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private HttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String sid;
    

    public YouTubeAccount() {
        KEY_USERNAME = "ytusername";
        KEY_PASSWORD = "ytpassword";
        HOSTNAME = "YouTube.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().youTubeCheckBox.setEnabled(false);
        HostsPanel.getInstance().youTubeCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();
            
            NULogger.getLogger().info("Trying to log in to YouTube.com");
            httpPost = new HttpPost("https://www.google.com/accounts/ClientLogin");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("Email", getUsername()));
            formparams.add(new BasicNameValuePair("Passwd", getPassword()));
            formparams.add(new BasicNameValuePair("service", "youtube"));
            formparams.add(new BasicNameValuePair("source", "NU"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if(responseString.contains("Error=BadAuthentication")){
                throw new NUInvalidLoginException(getUsername(), HOSTNAME);
            }
            else{
                sid = StringUtils.stringBetweenTwoStrings(responseString, "Auth=", "\n");
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                HostsPanel.getInstance().youTubeCheckBox.setEnabled(true);
                NULogger.getLogger().info("YouTube.com login successful!");
            }
            
            //FileUtils.saveInFile("YouTubeUploadLogin.html", responseString);
            
            
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

//        NULogger.getLogger().info("Getting startup cookies & link from YouTube.com");
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        sid = "";
    }
    
    /**
     * Get the sid value.
     * @return Returns the sid value.
     */
    public String getSid(){
        return sid;
    }

}
 