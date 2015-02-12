/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.FileFactoryAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.FileUtils;
import neembuuuploader.uploaders.common.MonitoredFileBody;
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
 * @author dinesh
 * @author davidepastore
 */
public class FileFactory extends AbstractUploader implements UploaderAccountNecessary {

    FileFactoryAccount fileFactoryAccount = (FileFactoryAccount) AccountsManager.getAccount("FileFactory.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    
    private boolean login = false;
    private String downloadLink = "";
    private long fileSizeLimit = 2097152000l; //2000 MB

    public FileFactory(File file) {
        super(file);
        host = "FileFactory.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        if (fileFactoryAccount.loginsuccessful) {
            login = true;
            host = fileFactoryAccount.username + " | FileFactory.com";
        }

    }

    @Override
    public void run() {
        try {
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), fileFactoryAccount.getHOSTNAME());
            }

            if (fileFactoryAccount.loginsuccessful) {
                login = true;
                host = fileFactoryAccount.username + " | FileFactory.com";
            } else {
                host = "FileFactory.com";
                uploadInvalid();
                return;
            }
            uploadInitialising();
            fileupload();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(FileFactory.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }

    }

    private void fileupload() throws Exception {
        httpContext = fileFactoryAccount.getHttpContext();
        
        httpPost = new NUHttpPost("http://upload.filefactory.com/upload-beta.php");

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("Filedata", createMonitoredFileBody());
        reqEntity.addPart("cookie", new StringBody(URLDecoder.decode(fileFactoryAccount.getFileFactoryMembershipcookie(), "UTF-8")));
        reqEntity.addPart("Filename", new StringBody(file.getName()));
        httpPost.setEntity(reqEntity);
        uploading();
        NULogger.getLogger().info("Now uploading your file into filefactory.com. Please wait......................");
        httpResponse = httpclient.execute(httpPost, httpContext);
        HttpEntity resEntity = httpResponse.getEntity();
        String id = "";
        if (resEntity != null) {
            id = EntityUtils.toString(resEntity);
            NULogger.getLogger().log(Level.INFO, "ID value: {0}", id);
        }
        gettingLink();
        downloadLink = NUHttpClientUtils.getData("http://www.filefactory.com/upload/results.php?files=" + id, httpContext);
        
        //FileUtils.saveInFile("FileFactory.com.html", downloadLink);
        
        Document doc = Jsoup.parse(downloadLink);
        downloadLink = doc.select("#row_"+ id +" td a").attr("href");

        NULogger.getLogger().log(Level.INFO, "Download Link : {0}", downloadLink);
        downURL = downloadLink;

        uploadFinished();
    }

}
