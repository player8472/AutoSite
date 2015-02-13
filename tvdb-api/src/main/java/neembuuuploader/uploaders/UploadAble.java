/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.UploadAbleAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import neembuuuploader.uploaders.common.StringUtils;
import org.apache.http.client.methods.HttpPut;
import neembuuuploader.uploaders.common.MonitoredFileEntity;

/**
 *
 * @author Paralytic
 */
public class UploadAble extends AbstractUploader{
    
    UploadAbleAccount uploadAbleAccount = (UploadAbleAccount) AccountsManager.getAccount("UploadAble.ch");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private CookieStore cookieStore;
    private String responseString;
    private String uploadURL;
    private String upload_code = "";
    private String delete_code = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public UploadAble(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UploadAble.ch";
        if (uploadAbleAccount.loginsuccessful) {
            host = uploadAbleAccount.username + " | UploadAble.ch";
        }
        maxFileSizeLimit = 2147483648L; // 2 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://www.uploadable.ch/upload.php", httpContext);
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "var uploadUrl = '", "';");
    }

    @Override
    public void run() {
        try {
            if (uploadAbleAccount.loginsuccessful) {
                httpContext = uploadAbleAccount.getHttpContext();
                maxFileSizeLimit = 2147483648L; // 2 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2147483648L; // 2 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            // http://up.uploadable.ch/u/username/e/sid
            HttpPut httpPut = new HttpPut(uploadURL);
            MonitoredFileEntity fileEntity = createMonitoredFileEntity();
            httpPut.setEntity(fileEntity);
            
            //Set the headers
            httpPut.setHeader("Origin", "http://www.uploadable.ch");
            httpPut.setHeader("X-File-Name", file.getName());
            httpPut.setHeader("X-File-Size", Long.toString(file.length()));
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPut.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into UploadAble.ch");
            uploading();
            httpResponse = httpclient.execute(httpPut, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            upload_code = StringUtils.stringBetweenTwoStrings(responseString, "\"shortenCode\":\"", "\"");
            delete_code = StringUtils.stringBetweenTwoStrings(responseString, "\"deleteCode\":\"", "\"");
            
            downloadlink = "http://www.uploadable.ch/file/" + upload_code + "/";
            deletelink = "http://www.uploadable.ch/file/" + upload_code + "/delete/" + delete_code;
            
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
