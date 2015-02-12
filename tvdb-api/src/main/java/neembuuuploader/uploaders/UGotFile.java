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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.UGotFileAccount;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.CommonUploaderTasks;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Dinesh
 */
public class UGotFile extends AbstractUploader {

    UGotFileAccount uGotFileAccount = (UGotFileAccount) AccountsManager.getAccount("UGotFile.com");
    private URL u;
    private HttpURLConnection uc;
    private String tmp;
    private BufferedReader br;
    private String postURL;
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private String phpsessioncookie;
    private long fileSizeLimit = 5242880000l; //5000 MB

    public UGotFile(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "UGotFile.com";
        if (uGotFileAccount.loginsuccessful) {
            host = uGotFileAccount.username + " | UGotFile.com";
        }
    }

    private void initialize() throws Exception {
        NULogger.getLogger().info("Getting startup cookie from ugotfile.com");
        u = new URL("http://ugotfile.com/");
        uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerFields = uc.getHeaderFields();

        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                tmp = header.get(i);
                if (tmp.contains("PHPSESSID")) {
                    phpsessioncookie = tmp;
                }

            }
        }
        phpsessioncookie = phpsessioncookie.substring(0, phpsessioncookie.indexOf(";"));
        NULogger.getLogger().log(Level.INFO, "phpsessioncookie : {0}", phpsessioncookie);

        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }

        postURL = StringUtils.stringBetweenTwoStrings(k, "action=\"", "\"");
        NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
    }

    private void fileUpload() throws Exception {

        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(postURL);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        mpEntity.addPart("Filename", new StringBody(file.getName()));
        mpEntity.addPart("Filedata", createMonitoredFileBody());
//        mpEntity.addPart("server", new StringBody(server));
        httppost.setEntity(mpEntity);
        NULogger.getLogger().log(Level.INFO, "executing request {0}", httppost.getRequestLine());
        NULogger.getLogger().info("Now uploading your file into ugotfile.com");
        uploading();
        HttpResponse response = httpclient.execute(httppost);
        gettingLink();
        NULogger.getLogger().info(response.getStatusLine().toString());
        if (response != null) {
            uploadresponse = EntityUtils.toString(response.getEntity());
        }
        NULogger.getLogger().log(Level.INFO, "Upload Response : {0}", uploadresponse);
        downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "[\"", "\"");
        downloadlink = downloadlink.replaceAll("\\\\/", "/");
        deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "\",\"", "\"");
        deletelink = deletelink.replaceAll("\\\\/", "/");
        downURL = downloadlink;
        delURL = deletelink;
        NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadlink);
        NULogger.getLogger().log(Level.INFO, "Delete Link : {0}", deletelink);

        uploadFinished();
    }

    private String getData(String myurl) throws Exception {
        u = new URL(myurl);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", UGotFileAccount.getPhpsessioncookie());
        br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
        String k = "";
        while ((tmp = br.readLine()) != null) {
            k += tmp;
        }
        return k;
    }

    public void run() {

        //5000MB - last char is a 'l' for long
        if (file.length() > fileSizeLimit) {
            JOptionPane.showMessageDialog(neembuuuploader.NeembuuUploader.getInstance(), "<html><b>" + getClass().getSimpleName() + "</b> " + TranslationProvider.get("neembuuuploader.exceptions.maxfilesize") + ": <b>5000MB</b></html>", getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);

            uploadInvalid();
            return;
        }

        if (uGotFileAccount.loginsuccessful) {

            host = uGotFileAccount.username + " | UGotFile.com";
        } else {

            host = "UGotFile.com";
        }

        try {
            uploadInitialising();
            if (uGotFileAccount.loginsuccessful) {
                tmp = getData("http://ugotfile.com/");
                postURL = StringUtils.stringBetweenTwoStrings(tmp, "action=\"", "\"");
                NULogger.getLogger().log(Level.INFO, "Post URL : {0}", postURL);
            } else {
                initialize();
            }
            fileUpload();
        } catch (Exception e) {
            NULogger.getLogger().severe(e.toString());

            uploadFailed();
        }

    }
}
