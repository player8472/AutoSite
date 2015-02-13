/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.NetLoadAccount;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.CookieUtils;
import neembuuuploader.utils.NULogger;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author dinesh
 */
public class NetLoad extends AbstractUploader {

    NetLoadAccount netLoadAccount = (NetLoadAccount) AccountsManager.getAccount("Netload.in");
    
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private CookieStore cookieStore;
    private String stringResponse;
    
    private String uploadresponse;
    private String downloadlink;
    private String deletelink;
    private String uid;
    private String phpsessioncookie;
    private String usercookie;
    private String postURL;
    private String upload_hash;
    private long fileSizeLimit = Long.MAX_VALUE; //Seems unlimited. Before it was: 10 GB

    public NetLoad(File file) {
        super(file);
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        host = "Netload.in";
        if (netLoadAccount.loginsuccessful) {
            //  login = true;
            host = netLoadAccount.username + " | Netload.in";
        }

    }

    private String getData(String myurl) throws Exception {
        //This is for braces: netload doesn't know the standard for URLs.
        if(myurl.contains("{")){
            int index = myurl.indexOf("?");
            String myurlPart2;
            myurlPart2 = myurl.substring(index+1);

            String myurlPart1 = myurl.substring(0, index);
            NULogger.getLogger().log(Level.INFO, "index: {0}", index);

            NULogger.getLogger().log(Level.INFO, "myurlPart1: {0}", myurlPart1);
            NULogger.getLogger().log(Level.INFO, "myurlPart2: {0}", myurlPart2);

            //Get parameter
            /*
            String[] parameters = myurlPart2.split("&");
            for(int i = 0; i < parameters.length; i++){
                NULogger.getLogger().log(Level.INFO, "parameters: {0}", parameters[i]);
            }
            */

            URI uri = new URI(
                "https", 
                "netload.in", 
                "/index.php",
                myurlPart2,
                null);
            httpGet = new NUHttpGet(uri);
        }
        else{
            httpGet = new NUHttpGet(myurl);
        }
        
        httpResponse = httpclient.execute(httpGet, httpContext);
        String response = EntityUtils.toString(httpResponse.getEntity());
        //FileUtils.saveInFile("OneFichier.html", response);
        return response;
    }

    private void initialize() throws Exception {


        if (netLoadAccount.loginsuccessful) {
            httpContext = netLoadAccount.getHttpContext();
            stringResponse = getData("http://netload.in/index.php");
            NULogger.getLogger().info("After login success, getting netload page again for post url & upload hash value......");
            
        } else {
            cookieStore = new BasicCookieStore();
            httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            NULogger.getLogger().info("gettig netload post url.......");
            stringResponse = getData("http://netload.in/");
        }
        
        if(CookieUtils.existCookie(httpContext, "PHPSESSID")){
            phpsessioncookie = CookieUtils.getCookieNameValue(httpContext, "PHPSESSID");
        }
        
        NULogger.getLogger().log(Level.INFO, "PHP session cookie : {0}", phpsessioncookie);

        postURL = StringUtils.stringBetweenTwoStrings(stringResponse, "action=\"http://", "\"");

        postURL = "http://" + postURL;

        NULogger.getLogger().log(Level.INFO, "postURL : {0}", postURL);

        if (netLoadAccount.loginsuccessful) {
            upload_hash = StringUtils.stringBetweenTwoStrings(stringResponse, "\"upload_hash\" value=\"", "\"");
            NULogger.getLogger().log(Level.INFO, "Upload hash : {0}", upload_hash);
        }

    }

    @Override
    public void run() {

        try {

            if (netLoadAccount.loginsuccessful) {
                host = netLoadAccount.username + " | Netload.in";
            } else {
                host = "Netload.in";
            }

//            
//            if (file.length() > fileSizeLimit) {
//                JOptionPane.showMessageDialog(neembuuuploader.NeembuuUploader.getInstance(), "<html><b>" + getClass().getSimpleName() + "</b> " + TranslationProvider.get("neembuuuploader.exceptions.maxfilesize") + ": <b>10GB</b></html>", getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
//                
//                uploadFailed();
//                return;
//            }
            uploadInitialising();
            initialize();

            httpPost = new NUHttpPost(postURL);

            MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (netLoadAccount.loginsuccessful) {
                mpEntity.addPart("upload_hash", new StringBody(upload_hash));
            }
            mpEntity.addPart("file", createMonitoredFileBody());
            httpPost.setEntity(mpEntity);
            NULogger.getLogger().log(Level.INFO, "executing request {0}", httpPost.getRequestLine());
            NULogger.getLogger().info("Now uploading your file into netload");
            uploading();
            httpResponse = httpclient.execute(httpPost, httpContext);
            gettingLink();
            stringResponse = EntityUtils.toString(httpResponse.getEntity());

            NULogger.getLogger().info(httpResponse.getStatusLine().toString());
            //httpclient.getConnectionManager().shutdown();
            if (httpResponse.containsHeader("Location")) {
                Header firstHeader = httpResponse.getFirstHeader("Location");
                NULogger.getLogger().info(firstHeader.getValue());
                uploadresponse = getData(firstHeader.getValue());
//                NULogger.getLogger().info("Upload response : " + uploadresponse);
                downloadlink = StringUtils.stringBetweenTwoStrings(uploadresponse, "The download link is: <br/>", "\" target=\"_blank\">");
                downloadlink = downloadlink.substring(downloadlink.indexOf("href=\""));
                downloadlink = downloadlink.replace("href=\"", "");
                NULogger.getLogger().log(Level.INFO, "download link : {0}", downloadlink);
                deletelink = StringUtils.stringBetweenTwoStrings(uploadresponse, "The deletion link is: <br/>", "\" target=\"_blank\">");
                deletelink = deletelink.substring(deletelink.indexOf("href=\""));
                deletelink = deletelink.replace("href=\"", "");
                NULogger.getLogger().log(Level.INFO, "delete link : {0}", deletelink);
                downURL = downloadlink;
                delURL = deletelink;

                uploadFinished();
            } else {
                throw new Exception("There might be a problem with your internet connection or server error. Please try after some time :(");
            }

        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);

            uploadFailed();

        }

    }
}