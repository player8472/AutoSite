/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.HostsPanel;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.api._hostr.HostrApi;
import neembuuuploader.uploaders.api._hostr.HostrApiBuilder;
import neembuuuploader.utils.NULogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 * If you have some problems with this plugin, take a look to <a href="https://hostr.co/developer">developers page</a>.
 * @author dinesh
 * @author davidepastore
 */
public class HostrAccount extends AbstractAccount {

    private DefaultHttpClient httpclient = (DefaultHttpClient) NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpGet httpGet;
    private String stringResponse;
    private long maxFileSize;
    private int dailyUploadAllowance;

    public HostrAccount() {
        KEY_USERNAME = "lhrsername";
        KEY_PASSWORD = "lhrpassword";
        HOSTNAME = "Hostr.co";
    }

    @Override
    public void disableLogin() {
        resetLogin();
        HostsPanel.getInstance().hostrCheckBox.setEnabled(false);
        HostsPanel.getInstance().hostrCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    @Override
    public void login() {
        loginsuccessful = false;
        try {
            httpGet = new NUHttpGet("https://api.hostr.co/user");
            
            //Basic Auth
            String credentials = getUsername()+ ":" + getPassword();
            String basicAuth = Base64.encodeBase64String(credentials.getBytes());
            httpGet.setHeader("Authorization", "Basic " + basicAuth);
            httpResponse = httpclient.execute(httpGet);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            //NULogger.getLogger().log(Level.SEVERE, "Response: {0}", stringResponse);
            
            JSONObject jsonObj = new JSONObject(stringResponse);
            if(jsonObj.has("error")){
                //Handle errors
                HostrApiBuilder localhostrApiBuilder = new HostrApiBuilder();
                HostrApi localhostrApi = localhostrApiBuilder
                        .setHostname(HOSTNAME)
                        .setJSONObject(jsonObj)
                        .setUsername(username)
                        .build();
                localhostrApi.handleErrors();
            }
            else{
                //Read info
                maxFileSize = jsonObj.getLong("max_filesize");
                dailyUploadAllowance = jsonObj.getInt("daily_upload_allowance");
                loginsuccessful = true;
                NULogger.getLogger().info("Hostr Login Success");
                NULogger.getLogger().log(Level.INFO, "Maxfilesize: {0}", maxFileSize);
                NULogger.getLogger().log(Level.INFO, "Daily Upload Allowance: {0}", dailyUploadAllowance);
                HostsPanel.getInstance().hostrCheckBox.setEnabled(true);
                username = getUsername();
                password = getPassword();
            }

        } catch(NUException ex){
            resetLogin();
            ex.printError();
            AccountsManager.getInstance().setVisible(true);

        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "Hostr Login Exception: {0}",ex);
            resetLogin();

            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "<br/></html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }
    
    /**
     * Get max file size.
     * @return max file size.
     */
    public long getMaxFileSize(){
        return maxFileSize;
    }
    
    /**
     * Get daily upload allowance.
     * @return daily upload allowance.
     */
    public int getDailyUploadAllowance(){
        return dailyUploadAllowance;
    }
    
    private void resetLogin(){
        loginsuccessful = false;
        username = "";
        password = "";
    }
}
