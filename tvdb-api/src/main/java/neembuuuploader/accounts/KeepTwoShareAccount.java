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
import neembuuuploader.captcha.Captcha;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.accounts.NUInvalidLoginException;
import neembuuuploader.exceptions.captcha.NUCaptchaException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.common.FileUtils;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NUHttpClientUtils;
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
 * @author davidepastore
 */
public class KeepTwoShareAccount extends AbstractAccount{
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    
    //Captcha
    private boolean activatedCaptcha = false;
    private String captchaAddress = "";
//    private final String kChallengeURL = "http://www.google.com/recaptcha/api/challenge?k=";
//    private final String kChallengeCode = "6LcYcN0SAAAAABtMlxKj7X0hRxOY8_2U86kI1vbb";

    public KeepTwoShareAccount() {
        KEY_USERNAME = "kptsusername";
        KEY_PASSWORD = "kptspassword";
        HOSTNAME = "Keep2Share.cc";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().keepTwoShareCheckBox.setEnabled(false);
        HostsPanel.getInstance().keepTwoShareCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to Keep2Share.cc");
            httpPost = new NUHttpPost("http://keep2share.cc/login.html");
            
            Document doc = Jsoup.parse(NUHttpClientUtils.getData("http://keep2share.cc/login.html", httpContext));
            
            if(doc.getElementById("captcha_auth0") != null){
                activatedCaptcha = true;
                captchaAddress = "http://keep2share.cc" + doc.getElementById("captcha_auth0").attr("src");
            }
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            
            formparams.add(new BasicNameValuePair("LoginForm[password]", getPassword()));
            formparams.add(new BasicNameValuePair("LoginForm[rememberMe]", "0"));
            formparams.add(new BasicNameValuePair("LoginForm[username]", getUsername()));
            
            if(activatedCaptcha){
                Captcha captcha = new Captcha();
                captcha.setFormTitle(TranslationProvider.get("neembuuuploader.accounts.captchacontrol")+" ("+ getHOSTNAME()  +")");
                captcha.setImageURL(captchaAddress);
                captcha.setHttpContext(httpContext);
                String captchaString = captcha.getCaptchaString();

                formparams.add(new BasicNameValuePair("LoginForm[verifyCode]", captchaString));
//                formparams.add(new BasicNameValuePair("recaptcha_challenge_field", captcha.getCCaptchaUrl()));
//                formparams.add(new BasicNameValuePair("recaptcha_response_field", captchaString));
                formparams.add(new BasicNameValuePair("yt0", ""));
            }
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            if(responseString.contains("Incorrect username or password")){
                throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
            }
            
            if(responseString.contains("The verification code is incorrect.")){
                throw new NUCaptchaException(getHOSTNAME());
            }
            
            if(CookieUtils.existCookie(httpContext, "sessid")){
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                HostsPanel.getInstance().keepTwoShareCheckBox.setEnabled(true);
                NULogger.getLogger().info("Keep2Share.cc login successful!");
            } else {
                //Get error message
                responseString = EntityUtils.toString(httpResponse.getEntity());
                //FileUtils.saveInFile("KeepTwoShareAccount.html", responseString);

                //Generic exception
                throw new Exception("Generic login error");
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

        //NULogger.getLogger().info("Getting startup cookies & link from Keep2Share.cc");
        //responseString = NUHttpClientUtils.getData("http://keep2share.cc/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }

}
 