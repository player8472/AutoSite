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
import neembuuuploader.accounts.VShareAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
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
import neembuuuploader.uploaders.common.StringUtils;
import java.util.Random;
import neembuuuploader.uploaders.common.FileUtils;
import neembuuuploader.exceptions.uploaders.NUFileExtensionException;

/**
 *
 * @author Paralytic
 */
public class VShare extends AbstractUploader{
    
    VShareAccount vShareAccount = (VShareAccount) AccountsManager.getAccount("VShare.eu");
    
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
    private String sess_id = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    private String srv_tmp_url = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public VShare(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "VShare.eu";
		
        if (vShareAccount.loginsuccessful) {
            host = vShareAccount.username + " | VShare.eu";
        }
        maxFileSizeLimit = 2122317824L; // 2,024 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://vshare.eu", httpContext);
        doc = Jsoup.parse(responseString);
        uploadURL = StringUtils.stringBetweenTwoStrings(responseString, "name=\"srv_tmp_url\" value=\"", "\"");
    }

    @Override
    public void run() {
        try {
            if (vShareAccount.loginsuccessful) {
                userType = "reg";
                httpContext = vShareAccount.getHttpContext();
                sessionID = CookieUtils.getCookieValue(httpContext, "xfss");
                maxFileSizeLimit = 3196059648L; // 3,048 MB          
            } else {
                userType = "anon";
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 2122317824L; // 2,024 MB
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

            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);
            
            sess_id = StringUtils.stringBetweenTwoStrings(responseString, "name=\"sess_id\" value=\"", "\"");
            srv_tmp_url = uploadURL;
            uploadURL = StringUtils.removeLastChars(uploadURL, 3) + "cgi-bin/upload.cgi?upload_id=" + uploadid_s + "&js_on=1&utype=" + userType + "&upload_type=file";
			
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("js_on", new StringBody("1"));
            mpEntity.addPart("upload_id", new StringBody(uploadid_s));
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("utype", new StringBody(userType));
            mpEntity.addPart("sess_id", new StringBody(sess_id));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("file_0_descr", new StringBody(""));
            mpEntity.addPart("file_0_public", new StringBody("1"));
            mpEntity.addPart("file_0", createMonitoredFileBody());
            mpEntity.addPart("submit_btn", new StringBody("Done"));
            mpEntity.addPart("tos", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into VShare.eu");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            upload_fn = doc.select("textarea[name=fn]").val();
            
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://vshare.eu/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("fn", upload_fn));
                formparams.add(new BasicNameValuePair("op", "upload_result"));
                formparams.add(new BasicNameValuePair("st", "OK"));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());

                doc = Jsoup.parse(responseString);
                downloadlink = doc.select("textarea").first().val();
                deletelink = doc.select("textarea").eq(2).val();

                NULogger.getLogger().log(Level.INFO, "Delete link : {0}", deletelink);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;

                uploadFinished();
            }
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
/**
* Add all the allowed extensions.
* 
* var ext_allowed='avi|divx|xvid|mpg|mpeg|vob|mov|flv|mp4|wmv|mkv';
* http://vshare.eu
*/
    private void addExtensions(){
        allowedVideoExtensions.add("avi");
        allowedVideoExtensions.add("divx");
        allowedVideoExtensions.add("xvid");
        allowedVideoExtensions.add("mpg");
        allowedVideoExtensions.add("mpeg");
        allowedVideoExtensions.add("vob");
        allowedVideoExtensions.add("mov");
        allowedVideoExtensions.add("flv");
        allowedVideoExtensions.add("mp4");
        allowedVideoExtensions.add("wmv");
        allowedVideoExtensions.add("mkv");
    }
}