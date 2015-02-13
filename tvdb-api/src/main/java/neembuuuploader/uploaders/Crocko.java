/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.CrockoAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.NUHTTP403Exception;
import neembuuuploader.exceptions.uploaders.NUDailyUploadLimitException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.exceptions.uploaders.NUMinFileSizeException;
import neembuuuploader.exceptions.uploaders.NUUploadFailedException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.api._crocko.CrockoApi;
import neembuuuploader.uploaders.common.CommonUploaderTasks;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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
import org.jsoup.nodes.Element;

/**
 *
 * @author dinesh
 * @author davidepastore
 */
public class Crocko extends AbstractUploader {

    CrockoAccount crockoAccount = (CrockoAccount) AccountsManager.getAccount("Crocko.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    
    private Document doc;
    
    private String postURL;
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private long maxFileSizeLimit = CrockoApi.MAX_FILE_SIZE;
    private long minFileSizeLimit = CrockoApi.MIN_FILE_SIZE;
    private final short dailyUploadAllowance = 8;

    public Crocko(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Crocko.com";

        if (crockoAccount.loginsuccessful) {
            //  login = true;
            host = crockoAccount.username + " | Crocko.com";
        }

    }

    @Override
    public void run() {
        try {

            if (crockoAccount.loginsuccessful) {
                host = crockoAccount.username + " | Crocko.com";
            } else {
                host = "Crocko.com";
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), crockoAccount.getHOSTNAME());
            }

            if (file.length() < minFileSizeLimit) {
                throw new NUMinFileSizeException(minFileSizeLimit, file.getName(), crockoAccount.getHOSTNAME());
            }

            uploadInitialising();

            if(crockoAccount.loginsuccessful){
                apiUpload();
            }
            else{
                initialize();
                normalUpload();
            }

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch(Exception e){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
            uploadFailed();
        } finally {
            postURL = null;
            uploadresponse = null;
            downloadlink = null;
            deletelink = null;
        }
    }

    /**
     * Initialize the uploader. It reads the upload_url variable from a .js file.
     * @throws Exception 
     */
    private void initialize() throws Exception {
        cookieStore = new BasicCookieStore();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        NULogger.getLogger().info("Getting upload url of crocko........");
        
        httpGet = new NUHttpGet("http://crocko.com/");
        httpResponse = httpclient.execute(httpGet, httpContext);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());

        postURL = StringUtils.stringBetweenTwoStrings(stringResponse, "upload_url : \"", "\"");
        NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
    }
    
    
    /**
     * Upload a file using the normal upload method (without account).
     */
    private void normalUpload() throws Exception{
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into crocko");
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();

        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        } else {
            throw new Exception("There might be a problem with your internet connectivity or server error. Please try again later :(");
        }
//  
        gettingLink();
        //NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
        //FileUtils.saveInFile("Crocko.html", uploadresponse);

        doc = Jsoup.parse(uploadresponse);
        Element element = doc.select("div.msg-ok dl dd input").first();
        if(element != null){
            downloadlink = element.val();
            deletelink = doc.select("div.msg-ok dl dd a.del").first().text();
        }
        else{
            //Handle errors
            status = UploadStatus.GETTINGERRORS;
            String error = doc.select("div.msg-err h4").first().text();
            
            if(error.contains("You exceed upload limit for anonymous user")){
                throw new NUDailyUploadLimitException(dailyUploadAllowance, file.getName(), crockoAccount.getHOSTNAME());
            }
            
            throw new Exception("Error is: "+error);
            // [#136] Use only HttpClient for connections in Crocko.
        }

        if("".equals(downloadlink)){
            status = UploadStatus.GETTINGERRORS;
            throw new Exception("Upload response: "+uploadresponse);
        }

        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);

        downURL = downloadlink;
        delURL = deletelink;
        
        uploadFinished();
    }


    /**
     * Upload a file using the upload method of CrockoApi (with account).
     */
    private void apiUpload() throws Exception{
        String body;
        int httpCode;

        //Setting connection parameters
        httpPost = new NUHttpPost(CrockoApi.CROCKO_API_URL+"/files");
        httpPost.addHeader("Accept", "application/atom+xml");
        httpPost.addHeader("Authorization", crockoAccount.getAPIkey());

        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("upload", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);

        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        
        HttpEntity resEntity = httpResponse.getEntity();
        body = CommonUploaderTasks.readAllFromInputStream(resEntity.getContent());
        StatusLine statusLine = httpResponse.getStatusLine();

        //Getting HTTP code response
        httpCode = statusLine.getStatusCode();
        if (httpCode == 403) {
            throw new NUHTTP403Exception(crockoAccount.getHOSTNAME());
        }
        if ("".equals(body)) {
            //Handle errors
            
            //Here it can be: you exceed the number of upload daily..
            throw new NUUploadFailedException(file.getName(), crockoAccount.getHOSTNAME());
        }

        gettingLink();
        
        if( CrockoApi.uploadController(body, httpCode, file.getName()) ){
            downURL = CrockoApi.getDownloadURL();
            delURL = CrockoApi.getDeleteURL();

            uploadFinished();
            }
        else{
            status = UploadStatus.GETTINGERRORS;
            throw new Exception("Error in the controller: "+CrockoApi.getError());
        }
    }
}
