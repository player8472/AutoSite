/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.FileCloudAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.exceptions.uploaders.NUMinFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class FileCloud extends AbstractUploader implements UploaderAccountNecessary {

    FileCloudAccount fileCloudAccount = (FileCloudAccount) AccountsManager.getAccount("FileCloud.io");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    private JSONObject jSonObject;

    private String uploadURL;
    private long minFileSizeLimit = 1024l;

    public FileCloud(File file) {

        super(file);
        host = "FileCloud.io";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (fileCloudAccount.loginsuccessful) {
            host = fileCloudAccount.username + " | FileCloud.io";
        }
        
        maxFileSizeLimit = 2097152000l; //2000 MB
    }

    @Override
    public void run() {

        try {
            if (fileCloudAccount.loginsuccessful) {
                host = fileCloudAccount.username + " | FileCloud.io";
            } else {
                host = "FileCloud.io";

                uploadInvalid();
                return;
            }

            //Check file size (max)
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            
            //Check file size (min)
            if(file.length() < minFileSizeLimit){
                throw new NUMinFileSizeException(minFileSizeLimit, file.getName(), host);
            }

            uploadInitialising();
            NULogger.getLogger().info("Getting upload url from FileCloud.....");
            stringResponse = NUHttpClientUtils.getData("http://api.filecloud.io/api-fetch_upload_url.api?response=json");
            
            //Get JSONObject
            jSonObject = new JSONObject(stringResponse);
            String responseStatus = jSonObject.getString("status");
            
            if("ok".equals(responseStatus)){
                uploadURL =jSonObject.getString("upload_url");
                NULogger.getLogger().log(Level.INFO, "FileCloud Upload URL : {0}", uploadURL);
                fileUpload();
                uploadFinished();
            }
            else{
                //Handle errors
                throw new Exception("Error: "+jSonObject.getString("message"));
            }

        } catch (NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(FileCloud.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }

    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(uploadURL);

        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        mpEntity.addPart("akey", new StringBody(fileCloudAccount.getFileCloudAPIKey()));

        mpEntity.addPart("Filedata", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into filecloud.io .....");
        uploading();
        httpResponse = httpclient.execute(httpPost);
        HttpEntity resEntity = httpResponse.getEntity();
        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {
            stringResponse = EntityUtils.toString(resEntity);
        }

        //Get JSONObject
        jSonObject = new JSONObject(stringResponse);
        String responseStatus = jSonObject.getString("status");
        
        if(!"ok".equals(responseStatus)){
            //Handle errors
            throw new Exception("Error: "+jSonObject.getString("message"));
        }
        
        String downloadURL = "http://filecloud.io/" + jSonObject.getString("ukey");
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadURL);
        downURL = downloadURL;

        status = UploadStatus.UPLOADFINISHED;
    }
}
