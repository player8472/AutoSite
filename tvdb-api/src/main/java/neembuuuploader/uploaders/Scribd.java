/* * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.ScribdAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUFileExtensionException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.FileUtils;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.NULogger;
import neembuuuploader.utils.NeembuuUploaderProperties;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class Scribd extends AbstractUploader implements UploaderAccountNecessary {

    ScribdAccount scribdAccount = (ScribdAccount) AccountsManager.getAccount("Scribd.com");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    
    private String doc_id;
    private String SCRIBD_API_KEY = "5t8cd1k0ww3iupw31bb2a";
    private String SCRIBD_API_SIGNATURE = "sec-b27x1xqgbvhrtccudzeh0s709n";
    private String SCRIBD_UPLOAD_URL = "http://api.scribd.com/api?method=docs.upload&api_key=" + SCRIBD_API_KEY + "&api_sig=" + SCRIBD_API_SIGNATURE + "&session_key" + NeembuuUploaderProperties.getEncryptedProperty("scribd_session_key");
    private long fileSizeLimit = 104857600; //100 MB
    
    private ArrayList<String> allowedExtensions = new ArrayList<String>();

    public Scribd(File file) {
        super(file);
        downURL = UploadStatus.NA.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Scribd.com";

        //It has to be successful.. as it won't work without login
        if (scribdAccount.loginsuccessful) {
            host = scribdAccount.username + " | Scribd.com";
        }
    }

    @Override
    public void run() {

        try {
            //Check size
            if (file.length() > fileSizeLimit) {
                throw new NUMaxFileSizeException(fileSizeLimit, file.getName(), scribdAccount.getHOSTNAME());
            }
            
            addExtensions();
            
            //Check extension
            if(!FileUtils.checkFileExtension(allowedExtensions, file)){
                throw new NUFileExtensionException(file.getName(), host);
            }
            
            uploadInitialising();

            httpPost = new NUHttpPost(SCRIBD_UPLOAD_URL);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            reqEntity.addPart("file", createMonitoredFileBody());

            httpPost.setEntity(reqEntity);
            NULogger.getLogger().log(Level.INFO, "Now uploading your file into scribd........ Please wait......................");
            uploading();
            httpResponse = httpclient.execute(httpPost);
            HttpEntity resEntity = httpResponse.getEntity();

            if (resEntity != null) {

                doc_id = EntityUtils.toString(resEntity);
                System.out.println(doc_id);
                if (doc_id.contains("stat=\"ok\"")) {
                    doc_id = StringUtils.stringBetweenTwoStrings(doc_id, "<doc_id>", "</doc_id>");
                    NULogger.getLogger().log(Level.INFO, "doc id :", doc_id);
                    uploadFinished();
                } else {
                    throw new Exception("There might be problem with your internet connection or server error. Please try again some after time :(");
                }
            } else {
                throw new Exception("There might be problem with your internet connection or server error. Please try again some after time :(");
            }


        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(Scribd.class.getName()).log(Level.SEVERE, null, e);

            uploadFailed();

        } finally {
            doc_id = null;
            SCRIBD_API_KEY = null;
            SCRIBD_API_SIGNATURE = null;
            SCRIBD_UPLOAD_URL = null;
        }

    }
    
    /**
    * Add all the allowed extensions.
    */
    private void addExtensions(){
        allowedExtensions.addAll(Arrays.asList(new String[]{"pdf", "ps", "doc", "docx", "ppt", "pptx", "pps", "ppsx", "xls", "xlsx", "odt", "sxw", "odp", "sxi", "ods", "sxc", "txt", "rtf", "tif", "tiff", "otg", "otf", "sxd"}));
    }
}
