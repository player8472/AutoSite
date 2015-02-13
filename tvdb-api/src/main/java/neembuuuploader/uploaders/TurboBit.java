/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.TurboBitAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
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
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class TurboBit extends AbstractUploader{
    
    TurboBitAccount turboBitAccount = (TurboBitAccount) AccountsManager.getAccount("TurboBit.net");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userId = "";
    
    private String downloadlink = "";

    public TurboBit(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "TurboBit.net";
        if (turboBitAccount.loginsuccessful) {
            host = turboBitAccount.username + " | TurboBit.net";
        }
        maxFileSizeLimit = 209715200l; //200 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://turbobit.net/", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("div.html-upload-form form").first().attr("action");
    }

    @Override
    public void run() {
        try {
            if (turboBitAccount.loginsuccessful) {
                httpContext = turboBitAccount.getHttpContext();
                responseString = NUHttpClientUtils.getData("http://turbobit.net/", httpContext);
                doc = Jsoup.parse(responseString);
                userId = doc.select("div.html-upload-form form input:eq(1)").val();
                
                maxFileSizeLimit = 107374182400l; //100 GB
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit =  209715200l; //200 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), turboBitAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();

            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("apptype", new StringBody("fd1"));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            
            if(turboBitAccount.loginsuccessful){
                mpEntity.addPart("user_id", new StringBody(userId));
                mpEntity.addPart("folder_id", new StringBody("0"));
            }
            
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into TurboBit.net");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //Read the links
            gettingLink();
            NULogger.getLogger().log(Level.INFO, "responseString : {0}", responseString);
            JSONObject jSonObject = new JSONObject(responseString);
            if(jSonObject.has("result") && jSonObject.getBoolean("result")){
                String fileId = jSonObject.getString("id");
                downloadlink = String.format("http://turbobit.net/%s.html", fileId);
                
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                
                uploadFinished();
            }
            else{
                throw new Exception("There isn't a result or it's false.");
            }
            
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            NULogger.getLogger().log(Level.SEVERE, "TurboBit.net exception: {0}", e);

            uploadFailed();
        }
    }
    
}
 