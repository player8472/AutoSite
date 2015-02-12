/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.GettAccount;
import neembuuuploader.exceptions.NUException;
import neembuuuploader.exceptions.uploaders.NUMaxFileSizeException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpOptions;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NUHttpClientUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author davidepastore
 */
public class Gett extends AbstractUploader{
    
    GettAccount gettAccount = (GettAccount) AccountsManager.getAccount("Ge.tt");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpOptions httpOptions;
    private CookieStore cookieStore;
    private String responseString;
    
    
    private JSONObject jSonObject;
    
    private String openApi;
    private String accessToken;
    private String shareName;
    //private long updated;
    
    //Formats
    private final String FIRST_STEP_URL = "%s/1/shares/create?accesstoken=%s&t=%s";
    private final String SECOND_STEP_URL = "%s/1/files/%s/create?accesstoken=%s&t=%s";
    private final String THIRD_STEP_URL = "%s&nounce=%s";
    private final String FOURTH_STEP_URL = "%s&nounce=%s";

    
    private int inc = 0; //For uuid and encode methods
    
    private String postUrl;
    
    private String downloadlink = "";
    private String deletelink = "";

    public Gett(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();
        host = "Ge.tt";
        if (gettAccount.loginsuccessful) {
            host = gettAccount.username + " | Ge.tt";
        }
        maxFileSizeLimit = 262144000l; //250 MB
        
    }

    private void initialize() throws Exception {
        responseString = NUHttpClientUtils.getData("http://ge.tt/", httpContext);
        
        responseString = StringUtils.stringBetweenTwoStrings(responseString, "window.config = ", ";");
        NULogger.getLogger().log(Level.INFO, "Response String : {0}", responseString);
        
        jSonObject = new JSONObject(responseString);
        openApi = jSonObject.getString("openapi");
        
        
        String session;
        session = CookieUtils.getCookieValue(httpContext, "session");
        session = URLDecoder.decode(session);
        session = session.substring(2);

        jSonObject = new JSONObject(session);

        accessToken = jSonObject.getString("accesstoken");
        //updated = jSonObject.getLong("updated");

        //NULogger.getLogger().log(Level.INFO, "accessToken : {0}", accessToken);
        //NULogger.getLogger().log(Level.INFO, "updated : {0}", updated);
        
        
        //First step
        httpPost = new NUHttpPost(String.format(FIRST_STEP_URL, openApi, accessToken, Calendar.getInstance().getTimeInMillis()));
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httpPost.setEntity(entity);
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        
        //NULogger.getLogger().log(Level.INFO, "responseString : {0}", responseString);
        jSonObject = new JSONObject(responseString);
        shareName = jSonObject.getString("sharename");
        
        
        
        //Second step
        httpPost = new NUHttpPost(String.format(SECOND_STEP_URL, openApi, shareName, accessToken, Calendar.getInstance().getTimeInMillis()));
        
        //Parameters creation
        jSonObject = new JSONObject();
        jSonObject.put("filename", file.getName());
        jSonObject.put("session", "api-" + uuid());
        
        httpPost.setEntity(new StringEntity(jSonObject.toString(), Consts.UTF_8));
        httpResponse = httpclient.execute(httpPost, httpContext);
        responseString = EntityUtils.toString(httpResponse.getEntity());
        
        //NULogger.getLogger().log(Level.INFO, "responseString : {0}", responseString);
        
        jSonObject = new JSONObject(responseString);
        downloadlink = jSonObject.getString("getturl");
        jSonObject = jSonObject.getJSONObject("upload");
        postUrl = jSonObject.getString("posturl");
        
    }

    @Override
    public void run() {
        try {
            
            if (gettAccount.loginsuccessful) {
                httpContext = gettAccount.getHttpContext();
                maxFileSizeLimit = gettAccount.getMaxFileSize(); //insert here size
            } else {
                cookieStore = new BasicCookieStore();
                httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
                maxFileSizeLimit = 262144000l; //250 MB
            }

            if (file.length() > maxFileSizeLimit) {
                throw new NUMaxFileSizeException(maxFileSizeLimit, file.getName(), gettAccount.getHOSTNAME());
            }
            uploadInitialising();
            initialize();
            
            
             //Third step
            String nounce = uuid();
            httpOptions = new NUHttpOptions(String.format(THIRD_STEP_URL, postUrl, nounce));
            httpOptions.setHeader("Access-Control-Request-Method", "POST");
            httpResponse = httpclient.execute(httpOptions);
            responseString = EntityUtils.toString(httpResponse.getEntity());

            //NULogger.getLogger().log(Level.INFO, "responseString : {0}", responseString);
            
            //Fourth step
            httpPost = new NUHttpPost(String.format(FOURTH_STEP_URL, postUrl, nounce));
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("Filedata", createMonitoredFileBody());
            reqEntity.addPart("name", new StringBody("blob"));
            reqEntity.addPart("filename", new StringBody(file.getName()));
            httpPost.setEntity(reqEntity);
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            responseString = EntityUtils.toString(httpResponse.getEntity());

            //NULogger.getLogger().log(Level.INFO, "responseString : {0}", responseString);

            if(responseString.contains("computer says yes")){
                NULogger.getLogger().log(Level.INFO, "Download link : {0}", downloadlink);
                downURL = downloadlink;
                delURL = deletelink;

                uploadFinished();
            }
            else{
                throw new Exception("Problem with upload for Ge.tt");
            }

        } catch(NUException ex){
            ex.printError();
            uploadInvalid();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();
        }
    }
    
    
    /**
     * Function in notify-snippet.js
     * @return 
     */
    private String uuid(){
        String uuid = "";
        for (int i = 0; i < 36; i++) {
            uuid += encode((int) (Math.random() * 62));
        }
        return uuid + "-" + encode(inc++);
    }

    
    /**
     * Function in notify-snippet.js
     * @return 
     */
    private String encode(int num){
        String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        
        if(num < alpha.length()){
            return Character.toString(alpha.charAt(num));
        }
        else {
            return encode((int) (num / alpha.length())) + alpha.charAt(num % alpha.length());
        }
    }

    
}
 