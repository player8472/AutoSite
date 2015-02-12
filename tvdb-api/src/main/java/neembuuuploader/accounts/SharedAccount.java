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
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.Header;
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
import neembuuuploader.uploaders.common.StringUtils;

/**
 *
 * @author Paralytic
 */
public class SharedAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    private String utf8 = "";
    private String authenticity_token = "";
    public String member_upload_url = "";

    public SharedAccount() {
        KEY_USERNAME = "sharedusername";
        KEY_PASSWORD = "sharedpassword";
        HOSTNAME = "Shared.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().sharedCheckBox.setEnabled(false);
        HostsPanel.getInstance().sharedCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            responseString = NUHttpClientUtils.getData("http://shared.com/login", httpContext);
            utf8 = StringUtils.stringBetweenTwoStrings(responseString, "name=\"utf8\" type=\"hidden\" value=\"", "\"");
            authenticity_token = StringUtils.stringBetweenTwoStrings(responseString, "name=\"authenticity_token\" type=\"hidden\" value=\"", "\"");
            
            NULogger.getLogger().info("Trying to log in to Shared.com");
            httpPost = new NUHttpPost("http://shared.com/login");
            httpPost.setHeader("Host", "shared.com");
            httpPost.setHeader("Referer", "http://shared.com/");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("utf8", utf8));
            formparams.add(new BasicNameValuePair("authenticity_token", authenticity_token));
            formparams.add(new BasicNameValuePair("user[email]", getUsername()));
            formparams.add(new BasicNameValuePair("user[password]", getPassword()));
            formparams.add(new BasicNameValuePair("submit", "Log In"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            Header lastHeader = httpResponse.getLastHeader("Location");
            
            member_upload_url = lastHeader.getValue();
            responseString = NUHttpClientUtils.getData(member_upload_url, httpContext);

            if (member_upload_url != null && member_upload_url.contains("http://shared.com/u/") && responseString != null && responseString.contains("logout")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                HostsPanel.getInstance().sharedCheckBox.setEnabled(true);
                NULogger.getLogger().info("Shared.com login successful!");
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("SharedAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select(".alert-error").first().text();
                
                if("Login incorrect".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }

                //Generic exception
                throw new Exception("Login error: " + error);
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

        NULogger.getLogger().info("Getting startup cookies & link from Shared.com");
        responseString = NUHttpClientUtils.getData("http://shared.com/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
