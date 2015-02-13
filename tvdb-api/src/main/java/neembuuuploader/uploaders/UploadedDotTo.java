/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.UploadedDotToAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class UploadedDotTo extends AbstractUploader implements UploaderAccountNecessary {

    UploadedDotToAccount uploadedDotToAccount = (UploadedDotToAccount) AccountsManager.getAccount("Uploaded.net");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    
    
    private String downloadlink = "";
    private String admincode;
    private String userid;
    private String userpwd;
    private String postURL;
    private String uploadresponse;

    public UploadedDotTo(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Uploaded.net";
        if (uploadedDotToAccount.loginsuccessful) {
            host = uploadedDotToAccount.username + " | Uploaded.net";
        }
        maxFileSizeLimit = 1073741824l; // 1 GB
    }

    public void generateUploadedValue() {
        char[] nonvowel = new char[]{'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'};
        char[] vowel = new char[]{'a', 'e', 'i', 'o', 'u'};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(nonvowel[(int) Math.round(Math.random() * 1000) % 20]).append("").append(vowel[(int) Math.round(Math.random() * 1000) % 5]);
        }
        admincode = sb.toString();
        NULogger.getLogger().log(Level.INFO, "Admin Code : {0}", admincode);
    }

    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", createMonitoredFileBody());
        httpPost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into uploaded.net");
        uploading();
        httpResponse = httpclient.execute(httpPost, httpContext);
        gettingLink();
        HttpEntity resEntity = httpResponse.getEntity();

        NULogger.getLogger().info(httpResponse.getStatusLine().toString());
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
        }
//  
        NULogger.getLogger().log(Level.INFO, "Upload response : {0}", uploadresponse);
        uploadresponse = uploadresponse.substring(0, uploadresponse.indexOf(","));
        // changed to a descriptive/long download link (user-requested-feature) //Paralytic (01-AUG-2014)
        downloadlink = "http://ul.to/" + uploadresponse + "/" + file.getName();
        NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
        downURL = downloadlink;

        uploadFinished();
    }

    @Override
    public void run() {
        try {
            if (uploadedDotToAccount.loginsuccessful) {
                host = uploadedDotToAccount.username + " | Uploaded.net";
                httpContext = uploadedDotToAccount.getHttpContext();
            } else {
                host = "Uploaded.net";
                uploadInvalid();
                return;
            }
            
            if(uploadedDotToAccount.isPremium()){
                maxFileSizeLimit = 5368709120l; // 5 GB
            }
            else{
                maxFileSizeLimit = 1073741824l; // 1 GB
            }
            
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();

            responseString = NUHttpClientUtils.getData("http://uploaded.net/manage", httpContext);
            Document doc = Jsoup.parse(responseString);
            userid = doc.select("#user_id").val();
            userpwd = doc.select("#user_pw").val();

            responseString = NUHttpClientUtils.getData("http://uploaded.net/js/script.js", httpContext);
            generateUploadedValue();
            postURL = StringUtils.stringBetweenTwoStrings(responseString, "uploadServer = '", "'") + "upload?admincode=" + admincode + "&id=" + userid + "&pw=" + userpwd;
            NULogger.getLogger().log(Level.INFO, "postURL : {0}", postURL);
            fileUpload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(UploadedDotTo.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }
}
