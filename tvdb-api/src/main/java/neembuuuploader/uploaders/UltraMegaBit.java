/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.UltraMegaBitAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author davidepastore
 */
public class UltraMegaBit extends AbstractUploader implements UploaderAccountNecessary{
    
    UltraMegaBitAccount ultraMegaBitAccount = (UltraMegaBitAccount) AccountsManager.getAccount("UltraMegaBit.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String userType;
    private String csrf_token = "";
    private String folder_id = "";
    private String user_id = "";
    
    private String downloadlink = "";

    public UltraMegaBit(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "UltraMegaBit.com";
        if (ultraMegaBitAccount.loginsuccessful) {
            host = ultraMegaBitAccount.username + " | UltraMegaBit.com";
        }
        maxFileSizeLimit = 1073741824l; //1 GB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("https://ultramegabit.com/", httpContext);
        
        doc = Jsoup.parse(responseString);
        Element form = doc.select("#fileupload").first();
        uploadURL = "https://web.ultramegabit.com/web/add";
        csrf_token = form.select("input[name=csrf_token]").val();
        folder_id = form.select("input[name=folder_id]").val();
        user_id = form.select("input[name=user_id]").val();
    }

    @Override
    public void run() {
        try {
            if (ultraMegaBitAccount.loginsuccessful) {
                userType = "reg";
                httpContext = ultraMegaBitAccount.getHttpContext();
                maxFileSizeLimit = 1073741824l; //1 GB
            } else {
                host = "UltraMegaBit.com";
                uploadInvalid();
                return;
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), host);
            }
            uploadInitialising();
            initialize();
            
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("csrf_token", new StringBody(csrf_token));
            mpEntity.addPart("folder_id", new StringBody(folder_id));
            mpEntity.addPart("user_id", new StringBody(user_id));
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into UltraMegaBit.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            //FileUtils.saveInFile("UltraMegaBit.html", responseString);
            
            //Read the links
            gettingLink();
            
            NULogger.getLogger().log(Level.INFO, "jsonArray: {0}", responseString);
            JSONArray jSonArray = new JSONArray(responseString);
            downloadlink = jSonArray.getJSONObject(0).getString("url");
            
            NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
            downURL = downloadlink;
            
            uploadFinished();
        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
}
