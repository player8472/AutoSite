/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.FilePostAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class FilePost extends AbstractUploader implements UploaderAccountNecessary{
    
    FilePostAccount filePostAccount = (FilePostAccount) AccountsManager.getAccount("FilePost.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String sessionID = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public FilePost(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "FilePost.com";
        if (filePostAccount.loginsuccessful) {
            host = filePostAccount.username + " | FilePost.com";
        }
        maxFileSizeLimit = 2147483648l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://filepost.com", httpContext);
        
        String jSonString = StringUtils.stringBetweenTwoStrings(responseString, "new Object", ";");
        jSonString = jSonString.substring(1, jSonString.length() - 1);
        final JSONObject jSonObject = new JSONObject(jSonString);
        uploadURL = jSonObject.getString("upload_url");
    }

    @Override
    public void run() {
        try {
            if (filePostAccount.loginsuccessful) {
                httpContext = filePostAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "SID");
                
                if(filePostAccount.isPremium()){
                    maxFileSizeLimit = 5368709120l; //5 GB
                }
                else{
                    maxFileSizeLimit = 2147483648l; //2 GB
                }
                
            } else {
                uploadInvalid();
                return;
            }

            //Check file length
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("SID", new StringBody(sessionID));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into FilePost.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            

            final String answer = StringUtils.stringBetweenTwoStrings(responseString, "answer\":\"", "\"");
            
            //Read the links
            gettingLink();
            responseString = NUHttpClientUtils.getData("http://filepost.com/files/done/" + answer, httpContext);
            
            //FileUtils.saveInFile("FilePost.html", responseString);
            
            doc = Jsoup.parse(responseString);
            //downloadlink = doc.getElementById("down_link").val(); // Long
            downloadlink = doc.getElementById("short_down_link").val(); // Short
            deletelink = doc.getElementById("edit_link").val();
            
            NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            delURL = deletelink;
            
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
