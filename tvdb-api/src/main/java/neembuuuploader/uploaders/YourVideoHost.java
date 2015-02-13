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
import neembuuuploader.accounts.YourVideoHostAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUFileExtensionException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.FileUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author MNidhal
 */
public class YourVideoHost extends AbstractUploader implements UploaderAccountNecessary{
    
    YourVideoHostAccount yourVideoHostAccount = (YourVideoHostAccount) AccountsManager.getAccount("YourVideoHost.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String disk_id = "";
    private String sv_id = "";
    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();
    
    private String downloadlink = "";
    private String deletelink = "";

    public YourVideoHost(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "YourVideoHost.com";
        if (yourVideoHostAccount.loginsuccessful) {
            host = yourVideoHostAccount.username + " | YourVideoHost.com";
        }
        maxFileSizeLimit = 2520776704l; //2404 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://yourvideohost.com/?op=upload", httpContext);
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[name=file]").first().attr("action");
        sv_id = doc.select("input[name=srv_id]").first().attr("value");
        disk_id = doc.select("input[name=disk_id]").first().attr("value");
        String uploadId = StringUtils.uuid(12, 10);
        uploadURL += uploadId +"&utype="+userType+"&disk_id="+disk_id;
    }

    @Override
    public void run() {
        try {
            if (yourVideoHostAccount.loginsuccessful) {
                userType = "reg";
                httpContext = yourVideoHostAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfsts");
                maxFileSizeLimit = 2520776704l; //2404 MB
            } else {
                host = "yourvideohost.com";
                uploadInvalid();
                return;
            }
            
            addExtensions();
    
            //Check extension
            if(!FileUtils.checkFileExtension(allowedVideoExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_id", new StringBody(sv_id));
            mpEntity.addPart("disk_id", new StringBody(disk_id));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody("Upload!"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into YourVideoHost.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            doc = Jsoup.parse(responseString);
            final String fn = doc.select("textarea[name=fn]").first().text();
            
            //Read the links
            gettingLink();
            httpPost = new NUHttpPost("http://yourvideohost.com");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("fn", fn));
            formparams.add(new BasicNameValuePair("op", "upload_result"));
            formparams.add(new BasicNameValuePair("st", "OK"));
            
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            httpPost.setEntity(entity);
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("textarea").first().val();
            deletelink = doc.select("textarea").eq(3).val();
            
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
    
    private void addExtensions(){
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("mkv");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("vob");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("m2v");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("xvid");
        allowedVideoExtensions.add("3gp");
        allowedVideoExtensions.add("webm");
        allowedVideoExtensions.add("ogv");
        allowedVideoExtensions.add("ogg");
        allowedVideoExtensions.add("rmvb");
        
        //This gives problems
        //allowedVideoExtensions.add("srt");
    }
    
}
