/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author vigneshwaran
 */
public class FileDropper extends AbstractUploader {
    
    //FileDropperAccount fileDenAccount = (FileDropperAccount) AccountsManager.getAccount("FileDropper.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;

    private long fileSizeLimit = 5368709120l; //5 GB

    public FileDropper(File file) {
        super(file);
        downURL=UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "FileDropper.com";
    }

    @Override
    public void run(){
        try {
            if (file.length() > fileSizeLimit) {
                //Change last parameter
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), getHost());
            }
            
            status=UploadStatus.INITIALISING;
            /*
            httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://www.filedropper.com");
            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2 GTBDFff GTB7.0");
            HttpResponse httpresponse = httpclient.execute(httpget);
            httpresponse.getEntity().consumeContent();
            */
            //------------------------------------------------------------
            httpPost = new NUHttpPost("http://www.filedropper.com/index.php?xml=true");
            MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            //requestEntity.addPart("Filename", new StringBody(file.getName()));
            //        requestEntity.addPart("", new StringBody(
            //                "Content-Disposition: form-data; name=\"file\"; filename=\""+f.getName()+"\"\r\n" +
            //                "Content-Type: application/octet-stream"
            //        ));
//            requestEntity.addPart("file", new FileBody(file));

            requestEntity.addPart("file", createMonitoredFileBody());
            
            
            requestEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(requestEntity);
            //-------------------------------------------------------------
            uploading();

            //-------------------------------------------------------------
            httpResponse = httpclient.execute(httpPost);
            String strResponse = EntityUtils.toString(httpResponse.getEntity());
            //-------------------------------------------------------------
            gettingLink();
            downURL = "http://www.filedropper.com/" + strResponse.substring(strResponse.lastIndexOf("=") + 1);
            

            NULogger.getLogger().info(downURL);
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.SEVERE, "FileDropper error: {0}", ex);
            
            uploadFailed();
        }
    }
}
