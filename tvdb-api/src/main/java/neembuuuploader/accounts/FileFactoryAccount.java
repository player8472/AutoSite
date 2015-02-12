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
import neembuuuploader.utils.NeembuuUploaderProperties;
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
 * @author davidepastore
 */
public class FileFactoryAccount extends AbstractAccount {

    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String stringResponse;
    private static String membershipcookie = "";

    public FileFactoryAccount() {
        KEY_USERNAME = "ffusername";
        KEY_PASSWORD = "ffpassword";
        HOSTNAME = "FileFactory.com";
    }

    @Override
    public void login() {
        try {
            httpContext = new BasicHttpContext();
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            loginsuccessful = false;
            
            NULogger.getLogger().info("Trying to log in to filefactory.com");
            httpPost = new NUHttpPost("http://www.filefactory.com/member/signin.php");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("Submit", "Sign In"));
            formparams.add(new BasicNameValuePair("loginEmail", getUsername()));
            formparams.add(new BasicNameValuePair("loginPassword", getPassword()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            NULogger.getLogger().info("Getting cookies........");
            
            if(CookieUtils.existCookie(httpContext, "auth")){
                membershipcookie = CookieUtils.getCookieValue(httpContext, "auth");
                NULogger.getLogger().info(membershipcookie);
                HostsPanel.getInstance().fileFactoryCheckBox.setEnabled(true);
                loginsuccessful = true;
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("FileFactory login success");
            }
            else{
                //Handle errors
                String error;
                //FileUtils.saveInFile("FileFactoryAccount.html", stringResponse);
                Document doc = Jsoup.parse(stringResponse);
                error = doc
                            .select("section#details.container-fluid div.inner form#sign_in fieldset div#input_fields.row-fluid div.span6 span.help-block")
                            .first()
                            .text()
                            .trim();
                
                if("The email address or password you have entered is incorrect. Please try again.".equals(error)){
                    throw new NUInvalidLoginException(getUsername(), getHOSTNAME());
                }
                
                throw new Exception("FileFactory login failed with error: " + error);
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            NeembuuUploaderProperties.setProperty(KEY_USERNAME, "");
            NeembuuUploaderProperties.setEncryptedProperty(KEY_PASSWORD, "");
            AccountsManager.getInstance().setVisible(true);
        } catch (Exception ex) {
            resetLogin();
            NeembuuUploaderProperties.setProperty(KEY_USERNAME, "");
            NeembuuUploaderProperties.setEncryptedProperty(KEY_PASSWORD, "");
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            Logger.getLogger(FileFactoryAccount.class.getName()).log(Level.SEVERE, null, ex);
            AccountsManager.getInstance().setVisible(true);
            
            
        }
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().fileFactoryCheckBox.setEnabled(false);
        HostsPanel.getInstance().fileFactoryCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public String getFileFactoryMembershipcookie() {
        return membershipcookie;
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
        membershipcookie = "";
    }

}
