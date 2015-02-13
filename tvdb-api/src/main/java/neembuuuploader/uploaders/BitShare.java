/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.BitShareAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.CommonUploaderTasks;
import neembuuuploader.uploaders.common.FormBodyPartUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class BitShare extends AbstractUploader{
    
    BitShareAccount bitShareAccount = (BitShareAccount) AccountsManager.getAccount("BitShare.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String progressKey = "";
    private String userGroupKey = "";
    private String uploadIdentifier = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public BitShare(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "BitShare.com";
        if (bitShareAccount.loginsuccessful) {
            host = bitShareAccount.username + " | BitShare.com";
        }
        maxFileSizeLimit = 1073741824l; //1024 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://bitshare.com", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("#uploadform").first().attr("action");
        uploadURL += "?X-Progress-ID=undefined" + CommonUploaderTasks.createRandomString(32);
        
        progressKey = doc.select("#progress_key").first().val();
        userGroupKey = doc.select("#usergroup_key").first().val();
        uploadIdentifier = doc.select("input[name=UPLOAD_IDENTIFIER]").first().val();
    }

    @Override
    public void run() {
        try {
            if (bitShareAccount.loginsuccessful) {
                httpContext = bitShareAccount.getHttpContext();
                
                if(bitShareAccount.isPremium()){
                    maxFileSizeLimit = 1073741824l; //1024 MB
                }
                else{
                    maxFileSizeLimit = 2147483648l; //2048 MB
                }
                
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 1073741824l; //1024 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("APC_UPLOAD_PROGRESS", new StringBody(progressKey));
            mpEntity.addPart("APC_UPLOAD_USERGROUP", new StringBody(userGroupKey));
            mpEntity.addPart("UPLOAD_IDENTIFIER", new StringBody(uploadIdentifier));
            FormBodyPart customBodyPart = FormBodyPartUtils.createEmptyFileFormBodyPart("file[]", new StringBody(""));
            mpEntity.addPart(customBodyPart);
            mpEntity.addPart("file[]", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into BitShare.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            final String location = httpResponse.getFirstHeader("Location").getValue();
            responseString = EntityUtils.toString(httpResponse.getEntity());
            responseString = NUHttpClientUtils.getData(location, httpContext);
 
            //Read the links
            gettingLink();
            //FileUtils.saveInFile("BitShare.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("#filedetails input[type=text]").eq(1).val();
            deletelink = doc.select("#filedetails input[type=text]").eq(4).val();
            
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
