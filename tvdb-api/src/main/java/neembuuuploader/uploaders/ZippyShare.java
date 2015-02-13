/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.ZippyShareAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 * @author davidepastore
 */
public class ZippyShare extends AbstractUploader {
    
    ZippyShareAccount zippyShareAccount = (ZippyShareAccount) AccountsManager.getAccount("Zippyshare.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String postURL;
    private String downloadlink;
    private String uploadresponse;
    private String zippylink;
    private String usercookie;


    public ZippyShare(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "ZippyShare.com";
        maxFileSizeLimit = 209715200l; //200 MB
    }
    
    
    private void initialize() throws Exception {
        NULogger.getLogger().info("Set the cookie if the user has an account.");
        if (zippyShareAccount.loginsuccessful) {
            usercookie = zippyShareAccount.getSessioncookie();
            host = zippyShareAccount.username + " | Zippyshare.com";
        }
        
        NULogger.getLogger().info("Getting zippyshare dynamic upload link");
        httpGet = new NUHttpGet("http://www.zippyshare.com/");
        httpResponse = httpclient.execute(httpGet);
        stringResponse = EntityUtils.toString(httpResponse.getEntity());
        zippylink = StringUtils.stringBetweenTwoStrings(stringResponse, "var server = '", "'");
        zippylink = zippylink.toLowerCase();
        NULogger.getLogger().info(zippylink);
    }


    private void fileUpload() throws Exception {
        httpPost = new NUHttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("notprivate", new StringBody("false"));
        mpEntity.addPart("folder", new StringBody("/"));
        mpEntity.addPart("Filedata", createMonitoredFileBody()); 
        httpPost.setHeader("Cookie", usercookie);
        httpPost.setEntity(mpEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into zippyshare.com");
        httpResponse = httpclient.execute(httpPost);
        gettingLink();
        HttpEntity resEntity = httpResponse.getEntity();
        if (resEntity != null) {
            uploadresponse = EntityUtils.toString(resEntity);
            downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "value=\"http://", "\"");
            downloadlink = "http://" + downloadlink;
            NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
            downURL=downloadlink;
            
        }else{
            throw new Exception("ZippyShare server problem or Internet connectivity problem");
        }

    }

    @Override
    public void run() {
        try {
            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), zippyShareAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();
            postURL = "http://" + zippylink + ".zippyshare.com/upload";
            fileUpload();
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());
            
            uploadFailed();
        }
    }
}
