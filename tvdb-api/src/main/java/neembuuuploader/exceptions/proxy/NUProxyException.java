/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.exceptions.proxy;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.exceptions.NUException;

/**
 * This handles proxy exception for NUHttpClient.
 * @author davidepastore
 */
public class NUProxyException extends Exception{
    
    private String proxyAddress;
    private int proxyPort;
    
    /**
     * Constructs an instance of
     * <code>NUProxyException</code> with the proxy address and the proxy port.
     *
     * @param proxyAddress the proxy address.
     * @param proxyPort the proxy port.
     */
    public NUProxyException(String proxyAddress, int proxyPort) {
        super(NUException.PROXY_TIMEOUT);
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
    }
    
    /**
     * Constructs an instance of
     * <code>NUProxyException</code> with the specified detail message,
     * the proxy address and the proxy port.
     *
     * @param msg the detail message.
     * @param proxyAddress the proxy address.
     * @param proxyPort the proxy port.
     */
    public NUProxyException(String msg, String proxyAddress, String proxyPort) {
        super(msg);
        this.proxyAddress = proxyAddress;
        this.proxyPort = Integer.parseInt(proxyPort);
    }

    public void printError() {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, this.getMessage());
        JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>"+TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage())+" "+  this.proxyAddress + ":"+ this.proxyPort + "</html>", TranslationProvider.get("neembuuuploader.exceptions."+this.getMessage()), JOptionPane.WARNING_MESSAGE);
    }
}
