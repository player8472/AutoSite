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
import neembuuuploader.accounts.VodLockerAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUFileExtensionException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.FileUtils;
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
import java.util.Random;

/**
 *
 * @author Paralytic
 */
public class VodLocker extends AbstractUploader implements UploaderAccountNecessary{
    
    VodLockerAccount vodLockerAccount = (VodLockerAccount) AccountsManager.getAccount("VodLocker.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String sessionID = "";
    private String srv_tmp_url = "";
    private String srv_id = "";
    private String disk_id = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    
    private String downloadlink = "";
    private String deletelink = "";
    private final ArrayList<String> allowedVideoExtensions = new ArrayList<String>();

    public VodLocker(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "VodLocker.com";
        if (vodLockerAccount.loginsuccessful) {
            host = vodLockerAccount.username + " | VodLocker.com";
        }
        maxFileSizeLimit = 4297064448L; // 4,098 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://vodlocker.com/?op=upload", httpContext);
        
        doc = Jsoup.parse(responseString);
        uploadURL = doc.select("form[name=file]").first().attr("action");
        srv_tmp_url = doc.select("form[name=file]").select("input[name=srv_tmp_url]").attr("value");
        sessionID = doc.select("form[name=file]").select("input[name=sess_id]").attr("value");
	srv_id = doc.select("form[name=file]").select("input[name=srv_id]").attr("value");
	disk_id = doc.select("form[name=file]").select("input[name=disk_id]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (vodLockerAccount.loginsuccessful) {
                userType = "reg";
                httpContext = vodLockerAccount.getHttpContext();
                maxFileSizeLimit = 4297064448L; // 4,098 MB
            } else {
                host = "VodLocker.com";
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

            long uploadID;
            Random random = new Random();
            uploadID = Math.round(random.nextFloat() * Math.pow(10,12));
            uploadid_s = String.valueOf(uploadID);

            uploadURL += uploadid_s + "&js_on=1&utype=" + userType + "&upload_type=file&disk_id=" + disk_id;
            // http://77.81.98.229/cgi-bin/upload.cgi?upload_id=
            // http://77.81.98.229/cgi-bin/upload.cgi?upload_id=236505606542&js_on=1&utype=reg&upload_type=file&disk_id=01
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("sess_id", new StringBody(sessionID));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("srv_id", new StringBody(srv_id));
            mpEntity.addPart("disk_id", new StringBody(disk_id));
            mpEntity.addPart("file", createMonitoredFileBody());
            mpEntity.addPart("fakefilepc", new StringBody(file.getName()));
            mpEntity.addPart("file_descr", new StringBody("Uploaded via Neembuu Uploader!"));
            mpEntity.addPart("file_public", new StringBody("1"));
            mpEntity.addPart("tos", new StringBody("1"));
            mpEntity.addPart("submit_btn", new StringBody("Upload!"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into VodLocker.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            upload_fn = doc.select("textarea[name=fn]").val();
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://vodlocker.com/");
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
                deletelink = doc.select("textarea").eq(3).val();
                
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
     * var ext_allowed='avi|mkv|mpg|mpeg|vob|wmv|flv|mp4|mov|m2v|divx|xvid|3gp|webm|ogv|ogg';
     */
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
    }
}
