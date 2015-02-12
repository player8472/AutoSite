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
import neembuuuploader.accounts.NovaFileAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
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
import neembuuuploader.interfaces.UploaderAccountNecessary;

/**
 *
 * @author Paralytic
 */
public class NovaFile extends AbstractUploader implements UploaderAccountNecessary{
    
    NovaFileAccount novaFileAccount = (NovaFileAccount) AccountsManager.getAccount("NovaFile.com");
    
    private final HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private String responseString;
    private Document doc;
    private String uploadURL;
    private String sess_id = "";
    private String uploadid_s = "";
    private String upload_fn = "";
    private String srv_tmp_url = "";
    private String srv_id = "";
    
    private String downloadlink = "";
    private String deletelink = "";

    public NovaFile(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "NovaFile.com";
        if (novaFileAccount.loginsuccessful) {
            host = novaFileAccount.username + " | NovaFile.com";
        }
        maxFileSizeLimit = 2097152000L; // 2,000 MB (default)
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://novafile.com/?op=upload", httpContext);
        
        doc = Jsoup.parse(responseString);
		
        uploadURL = doc.select("form").first().attr("action");
        srv_tmp_url = doc.select("form").first().select("input[name=srv_tmp_url]").attr("value");
        sess_id = doc.select("form").first().select("input[name=sess_id]").attr("value");
        srv_id = doc.select("form").first().select("input[name=srv_id]").attr("value");
    }

    @Override
    public void run() {
        try {
            if (novaFileAccount.loginsuccessful) {
                httpContext = novaFileAccount.getHttpContext();
                maxFileSizeLimit = 2097152000L; // 2,000 MB
            } else {
                host = "NovaFile.com";
                uploadInvalid();
                return;
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

            uploadURL += "/?X-Progress-ID=" + uploadid_s;
            // http://s07.novafile.com/upload/26/?X-Progress-ID=6320548861194
            httpPost = new NUHttpPost(uploadURL);
            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            mpEntity.addPart("upload_type", new StringBody("file"));
            mpEntity.addPart("srv_id", new StringBody(srv_id));
            mpEntity.addPart("sess_id", new StringBody(sess_id));
            mpEntity.addPart("srv_tmp_url", new StringBody(srv_tmp_url));
            mpEntity.addPart("file_1", createMonitoredFileBody());
            mpEntity.addPart("tos", new StringBody("1"));
            httpPost.setEntity(mpEntity);
            
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into NovaFile.com");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());
            
            doc = Jsoup.parse(responseString);
            
            //Read the links
            gettingLink();
            upload_fn = doc.select("input[name=fn]").val();
            if (upload_fn != null) {
                httpPost = new NUHttpPost("http://novafile.com/");
                List<NameValuePair> formparams = new ArrayList<NameValuePair>();
                formparams.add(new BasicNameValuePair("fn", upload_fn));
                formparams.add(new BasicNameValuePair("op", "upload_result"));
                formparams.add(new BasicNameValuePair("st", "OK"));

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
                httpPost.setEntity(entity);
                httpResponse = httpclient.execute(httpPost, httpContext);
                responseString = EntityUtils.toString(httpResponse.getEntity());
                
                doc = Jsoup.parse(responseString);

                downloadlink = doc.select("table").first().select("tr:nth-child(3)").select("td:nth-child(2)").select("a").attr("href");
                deletelink = doc.select("table").first().select("tr:nth-child(7)").select("td:nth-child(2)").select("input").attr("value");

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
    
}
