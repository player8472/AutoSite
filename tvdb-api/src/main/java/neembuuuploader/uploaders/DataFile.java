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
import neembuuuploader.accounts.DataFileAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author davidepastore
 */
public class DataFile extends AbstractUploader{
    
    DataFileAccount dataFileAccount = (DataFileAccount) AccountsManager.getAccount("DataFile.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private CookieStore cookieStore;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String afterUploadUrl = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public DataFile(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "DataFile.com";
        if (dataFileAccount.loginsuccessful) {
            host = dataFileAccount.username + " | DataFile.com";
        }
        maxFileSizeLimit = 2147483648l; //2 GB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://www.datafile.com", httpContext);
        
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "upload_url: ", ",");
        uploadURL = StringUtils.removeFirstChar(uploadURL);
        uploadURL = StringUtils.removeLastChar(uploadURL);
        
        afterUploadUrl = StringUtils.stringBetweenTwoStrings(responseString, "window.location = ", ";");
        afterUploadUrl = StringUtils.removeFirstChar(afterUploadUrl);
        afterUploadUrl = StringUtils.removeLastChar(afterUploadUrl);
        afterUploadUrl = "http://www.datafile.com" + afterUploadUrl;
    }

    @Override
    public void run() {
        try {
            if (dataFileAccount.loginsuccessful) {
                userType = "reg";
                httpContext = dataFileAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                
                if(dataFileAccount.isPremium()){
                    maxFileSizeLimit = 10737418240l; //10 GB
                }
                else{
                    maxFileSizeLimit = 2147483648l; //2 GB
                }
                
            } else {
                userType = "anon";
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
            mpEntity.addPart("Filename", new StringBody(file.getName()));
            mpEntity.addPart("folder_id", new StringBody("0"));
            mpEntity.addPart("Filedata", createMonitoredFileBody());
            mpEntity.addPart("Upload", new StringBody("Submit Query"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into DataFile.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            responseString = NUHttpClientUtils.getData(afterUploadUrl, httpContext);
            
            //Read the links
            gettingLink();
            //FileUtils.saveInFile("DataFile.html", responseString);
            
            doc = Jsoup.parse(responseString);
            downloadlink = doc.select(".links.first .text-edit").first().text();
            deletelink = doc.select(".links.del .text-edit").first().text();
            
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
