/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.NowDownloadAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author MNidhal
 */
public class NowDownload extends AbstractUploader{
    
    NowDownloadAccount nowDownloadAccount = (NowDownloadAccount) AccountsManager.getAccount("NowDownload.ch");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String upload_key;
    private String uid;
    private String upload_server;
    
    private String downloadlink = "";
    private String deletelink = "";

    public NowDownload(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "NowDownload.ch";
        if (nowDownloadAccount.loginsuccessful) {
            host = nowDownloadAccount.username + " | NowDownload.ch";
        }
        maxFileSizeLimit = 2147483648l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.nowdownload.ch", httpContext);
        
        upload_key = StringUtils.stringBetweenTwoStrings(responseString, "var upload_key = '", "';");
        uid = StringUtils.stringBetweenTwoStrings(responseString, "var uid = ", ";");
        upload_server = StringUtils.stringBetweenTwoStrings(responseString, "var upload_server = '", "';");
        uploadURL = "http://"+upload_server+"/cgi-bin/upload.cgi";
    }

    @Override
    public void run() {
        try {
            if (nowDownloadAccount.loginsuccessful) {
                httpContext = nowDownloadAccount.getHttpContext();
                maxFileSizeLimit = 2147483648l; //2 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2147483648l; //2 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_key", new StringBody(upload_key));
            mpEntity.addPart("uid", new StringBody(uid));
            mpEntity.addPart("upload_server", new StringBody(upload_server));
            mpEntity.addPart("fileselect", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into NowDownload.ch");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            String up_session = StringUtils.stringBetweenTwoStrings(responseString, "\"session\":\"", "\"}");          
            String vid_url = "http://" + upload_server + "/upload.php?s=nowdownload&dd=nowdownload.ch&upload_key=" + upload_key + "&uid=" + uid + "&session_id="+up_session;
            responseString = NUHttpClientUtils.getData(vid_url, httpContext);
            
            //Read the links
            gettingLink();
            downloadlink = "http://www.nowdownload.ch/dl/"+StringUtils.stringBetweenTwoStrings(responseString, "video_id\":\"", "\"}");
            deletelink = UploadStatus.NA.getLocaleSpecificString();
            
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
