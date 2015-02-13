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
 * @author davidepastore
 */
public class CloudZerAccount extends AbstractAccount{
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private JSONObject jSonObject;

    public CloudZerAccount() {
        KEY_USERNAME = "czusername";
        KEY_PASSWORD = "czpassword";
        HOSTNAME = "CloudZer.net";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().cloudZerCheckBox.setEnabled(false);
        HostsPanel.getInstance().cloudZerCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();
        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            initialize();

            NULogger.getLogger().info("Trying to log in to cloudzer.net");
            httpPost = new NUHttpPost("http://cloudzer.net/io/login");

            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("id", getUsername()));
            formparams.add(new BasicNameValuePair("pw", getPassword()));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            
            jSonObject = new JSONObject(stringResponse);

            if (jSonObject.has("loc") && jSonObject.getString("loc").equals("me")) {
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                HostsPanel.getInstance().cloudZerCheckBox.setEnabled(true);
                NULogger.getLogger().info("CloudZer.net login successful!");

            } else {
                //Get error message
                //FileUtils.saveInFile("CloudZer.html", stringResponse);
                if(jSonObject.has("err")){
                    String error = jSonObject.getString("err");
                    
                    if(error.equals("User and password do not match!")){
                        throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                    }
                }

                //Generic exception
                throw new Exception("Generic login error: " + stringResponse);
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

        //NULogger.getLogger().info("Getting startup cookies & link from share-online.biz");
        //stringResponse = NUHttpClientUtils.getData("https://www.cloudzer.net/", httpContext);
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
    
}
