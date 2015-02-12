/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.MixtureCloudAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
public class MixtureCloud extends AbstractUploader implements UploaderAccountNecessary {
    
    MixtureCloudAccount mixtureCloudAccount = (MixtureCloudAccount) AccountsManager.getAccount("MixtureCloud.com");
    //Necessary variables
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    
    private String downloadUrl = "https://www.mixturecloud.com/media/download/";
    
    private String uploadUrl;

    public MixtureCloud(File file) {
        super(file);
        host = "MixtureCloud.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();

        if (mixtureCloudAccount.loginsuccessful) {
            host = mixtureCloudAccount.username + " | MixtureCloud.com";
        }
        maxFileSizeLimit = Long.MAX_VALUE; //Unknow size

    }

    @Override
    public void run() {

        //Checking once again as user may disable account while this upload thread is waiting in queue
        if (mixtureCloudAccount.loginsuccessful) {
            host = mixtureCloudAccount.username + " | MixtureCloud.com";
            httpContext = mixtureCloudAccount.getHttpContext();
        } else {
            host = "MixtureCloud.com";
            uploadInvalid();
            return;
        }
        try {
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), getHost());
            }
            
            uploadMixtureCloud();
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            Logger.getLogger(MixtureCloud.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private void uploadMixtureCloud() throws Exception {
        uploadInitialising();
        responseString = NUHttpClientUtils.getData("https://www.mixturecloud.com/files", httpContext);
        uploadUrl = "http:" + StringUtils.stringBetweenTwoStrings(responseString, "urlUpload   : '", "',");
        NULogger.getLogger().log(Level.INFO, "uploadUrl is {0}", uploadUrl);
        
        httpPost = new NUHttpPost(uploadUrl);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("cmd", new StringBody("upload"));
        mpEntity.addPart("target", new StringBody("mcm1_MA"));
        mpEntity.addPart("upload[]", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        responseString = EntityUtils.toString(resEntity);
        //NULogger.getLogger().log(Level.INFO, "stringResponse : {0}", responseString);
        
        JSONObject jSonObject = new JSONObject(responseString);
        String webAccess = jSonObject.getJSONArray("file_data").getJSONObject(0).getString("web_access");
        downloadUrl += webAccess;
        
        downURL = downloadUrl;
    }
    
}
