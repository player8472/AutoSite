/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.uploaders;

import java.io.File;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.accounts.VReerAccount;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.httpclient.httprequest.NUHttpGet;
import neembuuuploader.httpclient.httprequest.NUHttpPost;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.UploaderAccountNecessary;
import neembuuuploader.interfaces.abstractimpl.AbstractUploader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 *
 * @author davidepastore
 */
public class VReer extends AbstractUploader implements UploaderAccountNecessary {
    
    VReerAccount vReerAccount = (VReerAccount) AccountsManager.getAccount("VReer.com");
    //Necessary variables
    private HttpClient httpclient = NUHttpClient.getHttpClient();
    private HttpContext httpContext = new BasicHttpContext();
    private HttpResponse httpResponse;
    private NUHttpPost httpPost;
    private NUHttpGet httpGet;
    private String stringResponse;
    
    
    public VReer(File file) {
        super(file);
        host = "VReer.com";
        downURL = UploadStatus.PLEASEWAIT.getLocaleSpecificString();
        delURL = UploadStatus.NA.getLocaleSpecificString();

        if (vReerAccount.loginsuccessful) {
            host = vReerAccount.username + " | VReer.com";
        }

    }
    
    
    @Override
    public void run() {

        //Checking once again as user may disable account while this upload thread is waiting in queue
        if (vReerAccount.loginsuccessful) {
            host = vReerAccount.username + " | VReer.com";
        } else {
            host = "VReer.com";
            uploadInvalid();
            return;
        }
        
        //Check file type


        //uploadVReer();


    }
    
}
