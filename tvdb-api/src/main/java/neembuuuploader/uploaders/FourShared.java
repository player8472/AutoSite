/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.FourSharedAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.NULogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class FourShared extends AbstractUploader implements UploaderAccountNecessary {

    FourSharedAccount fourSharedAccount = (FourSharedAccount) AccountsManager.getAccount("4Shared.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String stringResponse;
    
    private String downloadlink;
    private long fileSizeLimit = 2147483648L; //2048 MB

    public FourShared(File file) {
        super(file);
        host = "4Shared.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        //It has to be successful.. as it won't work without login
        if (fourSharedAccount.loginsuccessful) {
            host = fourSharedAccount.username + " | 4Shared.com";
        }
    }

    @Override
    public void run() {

        try {
            if (fourSharedAccount.loginsuccessful) {
                host = fourSharedAccount.username + " | 4Shared.com";
            } else {
                host = "4shared.com";
                uploadInvalid();
                return;
            }

            
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), fourSharedAccount.getHOSTNAME());
            }
            uploadInitialising();
            if (!fourSharedAccount.da.hasRightUpload()) {
                throw new Exception("Uploading is temporarily disabled for your account :(. Conact 4shared support.");
            }


            long newFileId = fourSharedAccount.da.uploadStartFile(fourSharedAccount.getUsername(), fourSharedAccount.getPassword(), -1, file.getName(), file.length());
            NULogger.getLogger().log(Level.INFO, "File id : {0}", newFileId);
            String sessionKey = fourSharedAccount.da.createUploadSessionKey(fourSharedAccount.getUsername(), fourSharedAccount.getPassword(), -1);
            long dcId = fourSharedAccount.da.getNewFileDataCenter(fourSharedAccount.getUsername(), fourSharedAccount.getPassword());
            String url = fourSharedAccount.da.getUploadFormUrl((int) dcId, sessionKey);


            httpPost = new NUHttpPost(url);
            MultipartEntity me = new MultipartEntity();
            StringBody rfid = new StringBody("" + newFileId);
            StringBody rfb = new StringBody("" + 0);
//            InputStreamBody isb = new InputStreamBody(new BufferedInputStream(new FileInputStream(f)), "FilePart");
            me.addPart("resumableFileId", rfid);
            me.addPart("resumableFirstByte", rfb);
            me.addPart("FilePart", createMonitoredFileBody());

            httpPost.setEntity(me);
            uploading();
            NULogger.getLogger().info("Now uploading your file into 4Shared............");
            httpResponse = httpclient.execute(httpPost);
            stringResponse = EntityUtils.toString(httpResponse.getEntity());
            String res = fourSharedAccount.da.uploadFinishFile(fourSharedAccount.getUsername(), fourSharedAccount.getPassword(), newFileId, DigestUtils.md5Hex(new FileInputStream(file)));
            if (res.isEmpty()) {
                NULogger.getLogger().info("File uploaded successfully :)");
                NULogger.getLogger().info("Now getting download link............");
                downloadlink = fourSharedAccount.da.getFileDownloadLink(fourSharedAccount.getUsername(), fourSharedAccount.getPassword(), newFileId);
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                uploadFinished();
            } else {
                NULogger.getLogger().log(Level.INFO, "Upload failed: {0}", res);
                uploadFailed();
            }
            
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(RapidShare.class.getName()).log(Level.SEVERE, null, e);
            uploadFailed();

        }


    }
}
