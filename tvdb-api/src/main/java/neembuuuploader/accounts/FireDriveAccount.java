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
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.common.StringUtils;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class FireDriveAccount extends AbstractAccount{
    
    private final String getUrlLogin = "http://www.putlocker.com/authenticate.php?login";
    private final String POST_URL_LOGIN = "https://auth.firedrive.com/";
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    private Document doc;
    
    public FireDriveAccount() {
        KEY_USERNAME = "plusername";
        KEY_PASSWORD = "plpassword";
        HOSTNAME = "FireDrive.com";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
        NUHttpClient.deleteCookies("firedrive.com");
    }

    @Override
    public void login() {
        String getResponse;
        String captchaAddress;
        Captcha captcha = new Captcha();
        String captchaString;
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        UrlEncodedFormEntity entity;
        httpGet = new NUHttpGet(getUrlLogin);
        httpPost = new NUHttpPost(POST_URL_LOGIN);
        httpContext = new BasicHttpContext();
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        
        NULogger.getLogger().info("Trying to log in to firedrive.com");
        try {
            //Find the image of the captcha
//            httpResponse = httpclient.execute(httpGet, httpContext);
//            getResponse = EntityUtils.toString(httpResponse.getEntity());
//            captchaAddress = StringUtils.stringBetweenTwoStrings(getResponse, "<img src=\"/include/captcha.php", "\"");
//            captchaAddress = "http://www.putlocker.com/include/captcha.php"+captchaAddress;
//            captchaAddress = captchaAddress.replace("&amp;", "&");
//            NULogger.getLogger().log(Level.INFO, "captchaAddress: {0}", captchaAddress);
//
//            captcha.setFormTitle(TranslationProvider.get("neembuuuploader.accounts.captchacontrol")+" (PutLocker.com)");
//            captcha.setImageURL(captchaAddress);
//            captcha.setHttpContext(httpContext);
//            captchaString = captcha.getCaptchaString(); //Read captcha string
            
            //Now we can send the request
            formparams.add(new BasicNameValuePair("user", getUsername()));
            formparams.add(new BasicNameValuePair("pass", getPassword()));
            formparams.add(new BasicNameValuePair("json", "1"));
//            formparams.add(new BasicNameValuePair("captcha_code", captchaString));
            entity = new UrlEncodedFormEntity(formparams);

            httpPost.setEntity(entity);
            
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            JSONObject jSonObject = new JSONObject(stringResponse);
            
            int status = jSonObject.getInt("status");
            
            switch(status){
                case 0:
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                case 1:
                    loginsuccessful = true;
                    username = getUsername();
                    password = getPassword();
                    
                    //Getting user type
                    stringResponse = NUHttpClientUtils.getData("https://www.firedrive.com/myfiles", httpContext);
                    String userToken = StringUtils.stringBetweenTwoStrings(stringResponse, "user_token", ";");
                    userToken = StringUtils.stringBetweenTwoStrings(userToken, "\"", "\"");
                    
                    String userTypeAddress = "http://www.firedrive.com/my_settings?user_token=" + userToken + "&_=" + System.currentTimeMillis();
                    stringResponse = NUHttpClientUtils.getData(userTypeAddress, httpContext);
                    
                    if(stringResponse.contains("You currently have a Free")){
                        premium = false;
                    }
                    else{
                        premium = true;
                    }

                    NULogger.getLogger().log(Level.INFO, "FireDrive premium: {0}", premium);
                    
                    NULogger.getLogger().info("FireDrive login successful");
                    
                    break;
                default:
                    throw new Exception("Login doesn't work!");
            }
            
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            NULogger.getLogger().log(Level.INFO, "FireDrive login error: {0}", ex);
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }
    
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
