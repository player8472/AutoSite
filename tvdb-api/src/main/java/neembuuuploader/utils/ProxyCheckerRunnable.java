/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import neembuuuploader.exceptions.proxy.NUProxyException;
import neembuuuploader.exceptions.proxy.NUProxyHostException;
import neembuuuploader.exceptions.proxy.NUProxyPortException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.settings.SettingsManager;

/**
 * Code to execute to control the proxy
 * @author davidepastore
 */
public class ProxyCheckerRunnable implements Runnable {

    private String proxyAddress;
    private String proxyPort;
    private Exception ex;
    
    
    public ProxyCheckerRunnable(String proxyAddress, String proxyPort){
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }

    @Override
    public void run() {
        //This code must be exec in another thread
        try {
            //Checking and setting proxy on HttpClient
            NUHttpClient.setProxy(proxyAddress, proxyPort);
        } catch (NUProxyPortException ex) {
            this.ex = ex;
        } catch (NUProxyHostException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            this.ex = ex;
        } catch (NUProxyException ex) {
            Logger.getLogger(SettingsManager.class.getName()).log(Level.SEVERE, null, ex);
            this.ex = ex;
        }
    }

    public Exception getException(){
        return ex;
    }
    
}
