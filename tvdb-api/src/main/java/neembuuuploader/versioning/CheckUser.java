/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.versioning;

import java.util.logging.Level;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.settings.SettingsProperties;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Shashank Tulsyan
 */
public class CheckUser {

    public static void getCanCustomizeNormalizing(UserSetPriv usp) {
        boolean canCustomizeNormalizing = true;
        String normalization = ".neembuu";
        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        HttpClient httpclient = NUHttpClient.getHttpClient();
        HttpGet httpget = new HttpGet("http://neembuu.com/uploader/api/user.xml?userid="+UserImpl.I().uid());
        NULogger.getLogger().info("Checking for user priviledges ...");
        try {
            HttpResponse response = httpclient.execute(httpget);
            String respxml = EntityUtils.toString(response.getEntity());
            canCustomizeNormalizing = getCanCustomizeNormalizingFromXml(respxml);
            normalization = getNormalization(respxml);
            NULogger.getLogger().log(Level.INFO, "CanCustomizeNormalizing: {0}", canCustomizeNormalizing);
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Exception while checking update\n{0}", ex);
        }
        usp.setCanCustomizeNormalizing(canCustomizeNormalizing);
        usp.setNormalization(normalization);
    }
    
    private  static boolean getCanCustomizeNormalizingFromXml(String str) {
        boolean canCustomizeNormalizing = true;
        try {
            String start = "<canCustomizeNormalizing>";
            String end = "</canCustomizeNormalizing>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            canCustomizeNormalizing = findBooleanValue(str);
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return canCustomizeNormalizing;
    }
    
    private static String getNormalization(String str) {
        String normalization = ".neembuu";
        try {
            String start = "<normalization>";
            String end = "</normalization>";

            str = str.substring(str.indexOf(start) + start.length());

            str = str.substring(0, str.indexOf(end));
            normalization = str;
        } catch (Exception any) {
            NULogger.getLogger().severe(any.toString());
        }
        return normalization;
    }
    
    private static boolean findBooleanValue(String name){
        return ((name == null) || !name.equalsIgnoreCase("false"));
    }
    
    public static void main(String[] args) throws Exception {
        UserImpl.init(SettingsProperties.getUserId());
        for (int i = 0; i < 10000; i++) {
            getCanCustomizeNormalizing( new UserSetPriv() {

                @Override public void setCanCustomizeNormalizing(boolean canCustomizeNormalizing) {
                    System.out.println("canCustomize = "+canCustomizeNormalizing);
                }

                @Override public void setNormalization(String normalization) {
                    System.out.println("nomalization = "+normalization);
                }
            });
            Thread.sleep(1000);
        }
    }
}
