/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.LuckyShareAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.CookieUtils;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class LuckyShare extends AbstractUploader{
    
    LuckyShareAccount luckyShareAccount = (LuckyShareAccount) AccountsManager.getAccount("LuckyShare.net");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    
    private String downloadlink = "";

    public LuckyShare(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "LuckyShare.net";
        if (luckyShareAccount.loginsuccessful) {
            host = luckyShareAccount.username + " | LuckyShare.net";
        }
        maxFileSizeLimit = 4294967296l; //4 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://luckyshare.net", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("#form-upload-form").first().attr("action");
    }

    @Override
    public void run() {
        try {
            if (luckyShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = luckyShareAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "glo_session_eac2");
                maxFileSizeLimit = 4294967296l; //4 GB
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 4294967296l; //4 GB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if(luckyShareAccount.loginsuccessful){
                mpEntity.addPart("session", new StringBody(sessionID));
            }
            mpEntity.addPart("uploader", new StringBody("iframe"));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into LuckyShare.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select("a").first().attr("href");
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
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
    
    /**
     * Implementation of returnUnique() of <a href="http://luckyshare.net/">http://luckyshare.net/</a>
     * @return Return the unique id.
     */
    private String returnUnique() {
        String text = "";
        String possible = "abcdef0123456789";
        for(int i = 0; i < 32; i++){
            text += possible.charAt((int)(Math.random() * possible.length()));
        }
        return text;
    }
    
}
 