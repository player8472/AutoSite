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

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class FileCloudAccount extends AbstractAccount {
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private JSONObject jsonObject;
    private String status;
    private String message;

    private String fileCloudAPIKey; 

    public FileCloudAccount() {
        KEY_USERNAME = "fcusername";
        KEY_PASSWORD = "fcpassword";
        HOSTNAME = "FileCloud.io";

    }

    public String getFileCloudAPIKey() {
        return fileCloudAPIKey;
    }

    @Override
    public void disableLogin() {
        resetLogin();
        //These code are necessary for account only sites.
        HostsPanel.getInstance().fileCloudCheckBox.setEnabled(false);
        HostsPanel.getInstance().fileCloudCheckBox.setSelected(false);
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
            httpPost = new NUHttpPost("https://secure.filecloud.io/api-fetch_apikey.api?response=json");
            NULogger.getLogger().info("Getting api key for the user from filecloud.....");
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("username", getUsername()));
            formparams.add(new BasicNameValuePair("password", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            //NULogger.getLogger().log(Level.INFO, "Response: {0}", stringResponse);
            
            jsonObject = new JSONObject(stringResponse);
            message = jsonObject.getString("message");
            status = jsonObject.getString("status");
            
            if("ok".equals(status)){
                fileCloudAPIKey = jsonObject.getString("akey");
                loginsuccessful = true;
                HostsPanel.getInstance().fileCloudCheckBox.setEnabled(true);
                
                username = getUsername();
                password = getPassword();
                
                //Get details
                httpPost = new NUHttpPost("http://api.filecloud.io/api-fetch_account_details.api?response=json");
                NULogger.getLogger().info("Getting details for the user from filecloud.....");

                formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("akey", fileCloudAPIKey));
                entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                stringResponse = EntityUtils.toString(httpResponse.getEntity());
                jsonObject = new JSONObject(stringResponse);
                premium = jsonObject.getInt("is_premium") == 1;
                
                if(premium){
                    NULogger.getLogger().info("Premium user.");
                }
                else{
                    NULogger.getLogger().info("Normal user.");
                }
                
                NULogger.getLogger().log(Level.INFO, "FileCloud API Key : {0}", fileCloudAPIKey);
                NULogger.getLogger().info("FileCloud Login success :)");
            }
            
            if("error".equals(status)){
                //NULogger.getLogger().log(Level.INFO, "Message: {0}", message);
                
                if("no such user or wrong password".equals(message)){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                //Handle other errors
                throw new Exception("Login doesn't work: " + message);
            }
        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
            resetLogin();
            NULogger.getLogger().log(Level.SEVERE, "{0}: Error in FileCloud Login \n Exception: {1}", new Object[]{getClass().getName(), e});
        }
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
