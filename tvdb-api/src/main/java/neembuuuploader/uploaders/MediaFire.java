/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.MediaFireAccount;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class MediaFire extends AbstractUploader implements UploaderAccountNecessary {

    MediaFireAccount mediaFireAccount = (MediaFireAccount) AccountsManager.getAccount("MediaFire.com");
    //Necessary variables
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    private String ukeycookie;
    private String skeycookie = "";
    private String usercookie;
    private String mfulconfig;
    private String postURL;
    private String uploadresponsekey;
    private String downloadlink;
    private long freeFileSizeLimit = 209715200l; //200 MB
    private long proFileSizeLimit = 4294967296l; //4 GB
    private long busFileSizeLimit = 10737418240l; //10 GB

    public MediaFire(File file) {
        super(file);
        host = "MediaFire.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();

        if (mediaFireAccount.loginsuccessful) {
            host = mediaFireAccount.username + " | MediaFire.com";
        }

    }

    @Override
    public void run() {

        //Checking once again as user may disable account while this upload thread is waiting in queue

        if (mediaFireAccount.loginsuccessful) {
            host = mediaFireAccount.username + " | MediaFire.com";
        } else {
            host = "MediaFire.com";
            uploadInvalid();
            return;
        }


        uploadMediaFire();


    }

    public String getData(String url) throws Exception {
        httpGet = new NUHttpGet(url);
        httpResponse = httpclient.execute(httpGet, httpContext);
        return EntityUtils.toString(httpResponse.getEntity());
    }

    public void getDownloadLink() throws Exception {
        gettingLink();
        //http://www.mediafire.com/basicapi/pollupload.php?key=gngikno37s9&MFULConfig=118i8bx3xd6d3y1xwl307x41f6cd09dy
        do {
            downloadlink = getData("http://www.mediafire.com/basicapi/pollupload.php?key=" + uploadresponsekey + "&MFULConfig=" + mfulconfig);
            //NULogger.getLogger().info(downloadlink);
        } while (!downloadlink.contains("No more"));
        downloadlink = StringUtils.stringBetweenTwoStrings(downloadlink, "<quickkey>", "<");
        downloadlink = "http://www.mediafire.com/?" + downloadlink;
        NULogger.getLogger().log(Level.INFO, "download link is {0}", downloadlink);
        downURL = downloadlink;
    }

    public void getMFULConfig() throws Exception {
        mfulconfig = getData("http://www.mediafire.com/basicapi/uploaderconfiguration.php?45144");
        mfulconfig = StringUtils.stringBetweenTwoStrings(mfulconfig, "<MFULConfig>", "<");
    }

    public void getUploadResponseKey() {
        uploadresponsekey = StringUtils.stringBetweenTwoStrings(uploadresponsekey, "<key>", "<");
    }

    private void uploadMediaFire() {
        try {
            uploadInitialising();
            ukeycookie = mediaFireAccount.getUKeyCookie();
            skeycookie = mediaFireAccount.getSKeyCookie();
            usercookie = mediaFireAccount.getUserCookie();


            /**
             * The following four lines I am commenting out due to some bug I'm
             * changing postURL line also.
             *
             * Vigneshwaran Dec 10, 2011
             */
//            NULogger.getLogger().info("Getting myfiles links........");
//            getMyFilesLinks();
//            NULogger.getLogger().info("Getting uploadkey value..........");
//            getUploadKey();
//            NULogger.getLogger().log(Level.INFO, "uploadkey {0}", uploadkey);
            NULogger.getLogger().info("Getting MFULConfig value........");
            getMFULConfig();
            postURL = "http://www.mediafire.com/douploadtoapi/?type=basic&" + ukeycookie + "&" + usercookie + "&uploadkey=myfiles&filenum=0&uploader=0&MFULConfig=" + mfulconfig;
            uploading();
            httpPost = new NUHttpPost(postURL);
            NULogger.getLogger().info(ukeycookie);
            MultipartEntity mpEntity = new MultipartEntity();
            MonitoredFileBody cbFile = createMonitoredFileBody();
            mpEntity.addPart("", cbFile);
            httpPost.setEntity(mpEntity);
            NULogger.getLogger().info("Now uploading your file into mediafire...........................");
            httpResponse = httpclient.execute(httpPost, httpContext);
            HttpEntity resEntity = httpResponse.getEntity();

            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            if (resEntity != null) {
                NULogger.getLogger().info("Getting upload response key value..........");
                uploadresponsekey = EntityUtils.toString(resEntity);
                getUploadResponseKey();
                NULogger.getLogger().log(Level.INFO, "upload response key {0}", uploadresponsekey);
            }
            getDownloadLink();

            uploadFinished();
        } catch (Exception ex) {
            NULogger.getLogger().severe(ex.toString());

            uploadFailed();
        }
    }
}
