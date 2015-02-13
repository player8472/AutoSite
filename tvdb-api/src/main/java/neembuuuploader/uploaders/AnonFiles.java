/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUFileExtensionException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.FileUtils;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
public class AnonFiles extends AbstractUploader{
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private JSONObject jSonObject;
    private String uploadURL = "https://anonfiles.com/api";
    
    private ArrayList<String> allowedExtensions = new ArrayList<String>();
    
    private String downloadlink = "";

    public AnonFiles(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "AnonFiles.com";
        maxFileSizeLimit = 524288000l; // 500 MB
    }
    
    /**
     * Add all the allowed extensions
     */
    private void addExtensions(){
        allowedExtensions.add("jpg");
        allowedExtensions.add("jpeg");
        allowedExtensions.add("gif");
        allowedExtensions.add("png");
        allowedExtensions.add("pdf");
        allowedExtensions.add("css");
        allowedExtensions.add("txt");
        allowedExtensions.add("avi");
        allowedExtensions.add("mpeg");
        allowedExtensions.add("mpg");
        allowedExtensions.add("mp3");
        allowedExtensions.add("doc");
        allowedExtensions.add("docx");
        allowedExtensions.add("odt");
        allowedExtensions.add("apk");
        allowedExtensions.add("7z");
        allowedExtensions.add("rmvb");
        allowedExtensions.add("zip");
        allowedExtensions.add("rar");
        allowedExtensions.add("mkv");
        allowedExtensions.add("xls");
    }

    @Override
    public void run() {
        try {

            //Check size
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            
            addExtensions();
            
            //Check extension
            if(!FileUtils.checkFileExtension(allowedExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            uploadInitialising();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into AnonFiles.com");
            uploading();
            httpResponse = httpclient.execute(httpPost);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            jSonObject = new JSONObject(responseString);
            if(jSonObject.getString("status").equals("success")){
                downloadlink = jSonObject.getString("url");
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            }
            else{
                throw new Exception("Upload failed: " + jSonObject.getString("msg"));
            }
            
            downURL = downloadlink;
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
}
 