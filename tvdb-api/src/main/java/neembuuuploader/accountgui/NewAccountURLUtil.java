/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accountgui;

import neembuuuploader.NeembuuUploader;
import neembuuuploader.utils.NeembuuUploaderLanguages;

/**
 * This is simply to export the logic of creation of url for new account
 * registration/creation
 *
 * @author Shashank Tulsyan
 */
public class NewAccountURLUtil {

    public static String createNewAccountRegistrationURL(String hostName) {
        ////http://neembuuuploader.sourceforge.net/redirector.php?version=2.9&host=UpBooth.com&os=Linux&jre=1.7.0_25&locale=en
        String url = "http://neembuuuploader.sourceforge.net/redirector.php";

        url += "?version=" + NeembuuUploader.getVersionForProgam();
        url += "&host=" + hostName;
        url += "&os=" + removeSpaces(System.getProperty("os.name"));
        url += "&jre=" + System.getProperty("java.version");
        url += "&locale=" + NeembuuUploaderLanguages.getUserLanguageCode();

        return url;
    }
    
    private static String removeSpaces(String text){
        return text.replace(" ","%20");
    }
}
