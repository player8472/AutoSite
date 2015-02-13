/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import neembuuuploader.HostsPanel;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 *
 * @author dinesh
 */
public class WuploadAccount extends AbstractAccount {

    private URL u;
    private HttpURLConnection uc;
    private static String rolecookie = "", langcookie = "";
    File file;
    private static boolean login;
    private static String sessioncookie = "", mailcookie = "", namecookie = "", affiliatecookie = "";
    private static String orderbycookie = "", directioncookie = "";
    private BufferedReader br;
    private String tmp;
    private static String wuplodDomain;

    public WuploadAccount() {
        KEY_USERNAME = "wuusername";
        KEY_PASSWORD = "wupassword";
        HOSTNAME = "Wupload.com";
    }

    public static String getWudomain() {
        return wuplodDomain;
    }

    public void disableLogin() {
        loginsuccessful = false;
        sessioncookie = "";
        mailcookie = "";
        namecookie = "";
        affiliatecookie = "";
        orderbycookie = "";
        directioncookie = "";
        HostsPanel.getInstance().wuploadCheckBox.setEnabled(false);
        HostsPanel.getInstance().wuploadCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());
    }

    public static String getAffiliatecookie() {
        return affiliatecookie;
    }

    public static String getLangcookie() {
        return langcookie;
    }

    public static String getMailcookie() {
        return mailcookie;
    }

    public static String getOrderbycookie() {
        return orderbycookie;
    }

    public static String getDirectioncookie() {
        return directioncookie;
    }

    public static String getRolecookie() {
        return rolecookie;
    }

    public static String getNamecookie() {
        return namecookie;
    }

    public static String getSessioncookie() {
        return sessioncookie;
    }

    @Override
    public void login() {
        try {

            initialize();
            loginWuploader();
        } catch (Exception ex) {
            Logger.getLogger(FileSonicAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loginWuploader() throws Exception {

        login = false;

        HttpParams params = new BasicHttpParams();
        params.setParameter(
                "http.useragent",
                "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);

        System.out.println("Trying to log in to Wupload");
        HttpPost httppost = new HttpPost(wuplodDomain + "account/login");
        httppost.setHeader("Referer", wuplodDomain);
        httppost.setHeader("Accept", "application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("email", getUsername()));
        formparams.add(new BasicNameValuePair("password", getPassword()));
        formparams.add(new BasicNameValuePair("redirect", "/"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);


        NULogger.getLogger().info("Getting wupload cookies........");
        Iterator<Cookie> it = httpclient.getCookieStore().getCookies().iterator();
        Cookie escookie = null;
        while (it.hasNext()) {
            escookie = it.next();
            if (escookie.getName().equalsIgnoreCase("PHPSESSID")) {
                sessioncookie = "PHPSESSID=" + escookie.getValue();
                NULogger.getLogger().info(sessioncookie);
            }
            if (escookie.getName().equalsIgnoreCase("email")) {
                mailcookie = "email=" + escookie.getValue();
                login = true;
                NULogger.getLogger().info(mailcookie);
            }
            if (escookie.getName().equalsIgnoreCase("nickname")) {
                namecookie = "nickname=" + escookie.getValue();
                NULogger.getLogger().info(namecookie);
            }
            if (escookie.getName().equalsIgnoreCase("isAffiliate")) {
                affiliatecookie = "isAffiliate=" + escookie.getValue();
                NULogger.getLogger().info(affiliatecookie);
            }
            if (escookie.getName().equalsIgnoreCase("role")) {
                rolecookie = "role=" + escookie.getValue();
                NULogger.getLogger().info(rolecookie);
            }

        }
        if (login) {
            NULogger.getLogger().info("Wupload Login Success");
            loginsuccessful = true;
            HostsPanel.getInstance().wuploadCheckBox.setEnabled(true);

            username = getUsername();
            password = getPassword();
            getFolderCookies();
        } else {
            NULogger.getLogger().info("Wupload Login failed");
            loginsuccessful = false;
            username = "";
            password = "";
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }

    }

    private void initialize() throws Exception {
        System.out.println("Getting domain from wupload.com");
        u = new URL("http://www.wupload.com/");
        uc = (HttpURLConnection) u.openConnection();

        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            wuplodDomain = header.get(0).substring(header.get(0).indexOf("domain=."));
            wuplodDomain = wuplodDomain.replaceAll("domain=.", "");

        }
        wuplodDomain = "http://www." + wuplodDomain + "/";
        System.out.println(wuplodDomain);
    }

    public void getFolderCookies() throws IOException {
        u = new URL(wuplodDomain);
        uc = (HttpURLConnection) u.openConnection();
        uc.setRequestProperty("Cookie", langcookie + ";" + sessioncookie + ";" + mailcookie + ";" + namecookie + ";" + rolecookie);
        Map<String, List<String>> headerFields = uc.getHeaderFields();
        if (headerFields.containsKey("Set-Cookie")) {
            List<String> header = headerFields.get("Set-Cookie");
            for (int i = 0; i < header.size(); i++) {
                String tmp = header.get(i);
                if (tmp.contains("fs_orderFoldersBy")) {
                    orderbycookie = tmp;
                    orderbycookie = orderbycookie.substring(0, orderbycookie.indexOf(";"));
                }
                if (tmp.contains("fs_orderFoldersDirection")) {
                    directioncookie = tmp;
                    directioncookie = directioncookie.substring(0, directioncookie.indexOf(";"));
                }
            }
            NULogger.getLogger().log(Level.INFO, "ordercookie : {0}", orderbycookie);
            NULogger.getLogger().log(Level.INFO, "directioncookie : {0}", directioncookie);
        }

    }
}
