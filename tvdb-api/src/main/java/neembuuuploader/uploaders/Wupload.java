/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.WuploadAccount;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.CommonUploaderTasks;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

/**
 *
 * @author dinesh
 */
public class Wupload extends AbstractUploader implements UploaderAccountNecessary {

    WuploadAccount wuploadAccount = (WuploadAccount) AccountsManager.getAccount("Wupload.com");
    
    private String uploadID = "";
    private String postURL = "";
    private long fileSizeLimit = 2147483648l; //2 GB

    public Wupload(File file) {

        super(file);
        downURL = UploadStatus.NA.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = WuploadAccount.getWudomain();
        if (wuploadAccount.loginsuccessful) {
    
            host = wuploadAccount.username + " | Wupload.com";
        }
    }

    public void run() {

        try {

            if (file.length() > fileSizeLimit) {
                JOptionPane.showMessageDialog(neembuuuploader.NeembuuUploader.getInstance(), "<html><b>" + getClass().getSimpleName() + "</b> " + TranslationProvider.get("neembuuuploader.exceptions.maxfilesize") + ": <b>2GB</b></html>", getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);

                uploadInvalid();
                return;
            }
            if (wuploadAccount.loginsuccessful) {
    
                host = wuploadAccount.username + " | Wupload.com";
            } else {
    
                host = "Wupload.com";
                uploadInvalid();
                return;
            }

            uploadID = "upload_" + new Date().getTime() + "_" + WuploadAccount.getSessioncookie().replace("PHPSESSID", "") + "_" + Math.round(Math.random() * 90000);

            postURL = "http://s" + (new Random().nextInt(3) + 1) + WuploadAccount.getWudomain().replaceAll("http://www", "")
                    + "?callbackUrl=" + WuploadAccount.getWudomain() + "upload/done/:uploadProgressId&X-Progress-ID=" + uploadID;
            System.out.println("post URL : " + postURL);

            fileUpload();


            uploadFinished();
        } catch (Exception ex) {
            Logger.getLogger(Wupload.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();

            uploadFailed();
        }
    }

    public void fileUpload() throws Exception {
        uploading();
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        HttpPost httppost = new HttpPost(postURL);

        httppost.setHeader("Cookie", WuploadAccount.getLangcookie() + ";" + WuploadAccount.getSessioncookie() + ";" + WuploadAccount.getMailcookie() + ";" + WuploadAccount.getNamecookie() + ";" + WuploadAccount.getRolecookie() + ";" + WuploadAccount.getOrderbycookie() + ";" + WuploadAccount.getDirectioncookie() + ";");

        MultipartEntity mpEntity = new MultipartEntity();
        mpEntity.addPart("files[]", createMonitoredFileBody());
        httppost.setEntity(mpEntity);
        NULogger.getLogger().info("Now uploading your file into wupload...........................");
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity resEntity = response.getEntity();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (response.getStatusLine().getStatusCode() == 302
                && response.getFirstHeader("Location").getValue().contains("upload/done/")) {

            System.out.println("Upload successful :)");
        } else {
            System.out.println("Upload failed :(");
        }

    }
}
