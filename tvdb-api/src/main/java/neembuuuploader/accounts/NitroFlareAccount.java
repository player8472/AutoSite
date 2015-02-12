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
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.accounts.NUInvalidLoginException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
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
import neembuuuploader.uploaders.common.StringUtils;
//import neembuuuploader.captcha.Captcha;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.HostsPanel;

/**
 *
 * @author Paralytic
 */
public class NitroFlareAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
	
    //private String captcha_url = "";
    //private String captchaString = "";
    private String nf_token = "";

    public NitroFlareAccount() {
        KEY_USERNAME = "ntrflareusername";
        KEY_PASSWORD = "ntrflarepassword";
        HOSTNAME = "NitroFlare.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().nitroFlareCheckBox.setEnabled(false);
        HostsPanel.getInstance().nitroFlareCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to NitroFlare.com");
            httpPost = new NUHttpPost("https://www.nitroflare.com/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            //formparams.add(new BasicNameValuePair("captcha", captchaString));
            formparams.add(new BasicNameValuePair("email", getUsername()));
            formparams.add(new BasicNameValuePair("login", ""));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            formparams.add(new BasicNameValuePair("token", nf_token));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            if (httpResponse != null && httpResponse.toString().contains("user=")) {
                EntityUtils.consume(httpResponse.getEntity());
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                HostsPanel.getInstance().nitroFlareCheckBox.setEnabled(true);
                NULogger.getLogger().info("NitroFlare.com login successful!");

            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("NitroFlareAccount.html", responseString);
                Document doc = Jsoup.parse(responseString);
                String error = doc.select(".errors li").first().text();
                
                if(error.contains("Account does not exist")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                else if(error.contains("Login failed")){
                    throw new NUInvalidLoginException(getUsername(), HOSTNAME);
                }
                else if(error.contains("CAPTCHA error")){
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
        NULogger.getLogger().info("Getting startup cookies & link from NitroFlare.com");
        responseString = NUHttpClientUtils.getData("https://www.nitroflare.com/login", httpContext);
        
	nf_token = StringUtils.stringBetweenTwoStrings(responseString, "name=\"token\" value=\"", "\"");
	/*captcha_url = "https://www.nitroflare.com/plugins/captcha/CaptchaSecurityImages.php" + StringUtils.stringBetweenTwoStrings(responseString, "plugins/captcha/CaptchaSecurityImages.php", "\"");
        Captcha captcha = new Captcha();
	captcha.setFormTitle(TranslationProvider.get("neembuuuploader.accounts.captchacontrol")+" (NitroFlare.com)");
        captcha.setImageURL(captcha_url);
        captcha.setHttpContext(httpContext);
        captchaString = captcha.getCaptchaString();*/
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
