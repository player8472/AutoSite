package neembuuuploader.uploaders.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.QueueManager;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.Uploader;
import neembuuuploader.utils.NULogger;
import neembuuuploader.utils.NeembuuUploaderLanguages;
import neembuuuploader.versioning.UserImpl;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/** Noninstantiable class with static methods for common tasks for uploaders
 *
 * @author vigneshwaran
 * @author davidepastore
 */
public class CommonUploaderTasks {

    /**
     * Non-instantiable
     */
    private CommonUploaderTasks() {
    }

    /**
     * Call this method at the end of successful uploading. 
     * Without this, NeembuuUploader will not start the next upload.
     * 
     * It will print the upload record to file, send statistics 
     * and starts next upload
     * @param up 
     */
    public synchronized static void uploadFinished(Uploader up) {
        writeRecentlyUploaded(up);
        sendStatsInAnotherThread(up);
        QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * Call this method if the uploading failed.
     * Without this, NeembuuUploader will not start the next upload.
     * 
     * It will send failure statistics 
     * and starts next upload
     * @param up 
     */
    public synchronized static void uploadFailed(Uploader up) {
        sendStatsInAnotherThread(up);
        QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * Same as uploadFailed but may add more in future
     * @param up 
     */
    public synchronized static void uploadStopped(Uploader up) {
        sendStatsInAnotherThread(up);
        QueueManager.getInstance().startNextUploadIfAny();
    }

    /**
     * This private method writes recently uploaded files to a file on user's home folder.
     * @param up 
     */
    private static void writeRecentlyUploaded(Uploader up) {
        try {
            //Validate URL
            if (!up.getDownloadURL().equals(UploadStatus.NA.getLocaleSpecificString())) {
                new URL(up.getDownloadURL());
            }

            //Append to the file instead of overwriting.
            PrintWriter writer = new PrintWriter(new FileWriter(System.getProperty("user.home") + File.separator + "recent.log", true));
            writer.write(up.getDisplayFileName() + "<>" + up.getHost() + "<>" + up.getDownloadURL() + "<>" + up.getDeleteURL() + "\n");
            writer.close();
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Error while writing recent.log\n{0}", ex);
        }
    }

    /**
     * This private method will send statistics to the server.
     * Download Links or Delete Links will not be sent for privacy reasons.
     * 
     * These data are used for analysis and cleared periodically to avoid exceeding quota.
     * 
     * @param up 
     */
    private static void sendStats(Uploader up) {
        try {
            String status = up.getStatus().getDefaultLocaleSpecificString();
            if (!status.startsWith("Upload")) {
                return;
            }

            String hostName = up.getHost();
            if(hostName.contains("|")){
                hostName = hostName.substring(hostName.indexOf("|"));
                hostName = "account " + hostName;
            }

            NULogger.getLogger().info("Sending statistics..");
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("version", NeembuuUploader.getVersionForProgam() ) );
            formparams.add(new BasicNameValuePair("filename", up.getFileName()));
            formparams.add(new BasicNameValuePair("size", up.getSize()));
            formparams.add(new BasicNameValuePair("host", hostName));
            formparams.add(new BasicNameValuePair("status", status));
            formparams.add(new BasicNameValuePair("os", System.getProperty("os.name")));
            formparams.add(new BasicNameValuePair("locale", NeembuuUploaderLanguages.getUserLanguageCode()));
            formparams.add(new BasicNameValuePair("uid", UserImpl.I().uidString()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            HttpPost httppost = new HttpPost("http://neembuuuploader.sourceforge.net/insert.php");
            httppost.setEntity(entity);
            HttpParams params = new BasicHttpParams();
            params.setParameter(
                    "http.useragent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
            httppost.setParams(params);
            HttpClient httpclient = NUHttpClient.getHttpClient();
            EntityUtils.consume(httpclient.execute(httppost).getEntity());
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "Error while sending statistics\n{0}", ex);
        }
    }
    
    /**
     * Send the stats in another Thread, so the queue doesn't freeze.
     * @param uploader The uploader instance.
     */
    private static void sendStatsInAnotherThread(final Uploader uploader){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sendStats(uploader);
            }
        });
        thread.start();
    }

    /**
     * This public method can be used anywhere to get the String representation of file size
     * @param bytes - size of file in number of bytes (long type)
     * @return the string representation of file size.. Like 1GB, 200MB, 300KB, 400bytes etc..
     */
    public static String getSize(long bytes) {
        if (bytes > 1048576) {
            double div = bytes / 1048576;
            return div + "MB";
        } else if (bytes > 1024) {
            double div = bytes / 1024;
            return div + "KB";
        } else {
            return bytes + "bytes";
        }
    }
    
    /**
     * Get the String representation of the bytes per second. 
     * @param bytes the number of bytes.
     * @return the String representation of the bytes per second. Like 50KB/s
     */
    public static String getSpeed(long bytes){
        return getSize(bytes) + "/s";
    }

    /**
     * Create a random string of <i>length</i> size.
     * @param length Size of the string.
     * @return A random string of <i>length</i> size.
     */
    public static String createRandomString(int length) {
	Random random = new Random();
	StringBuilder sb = new StringBuilder();
	while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
	}
	return sb.toString();
    }
    
    /**
     * Read all the content from an InputStream instance.
     * @param inputStream the instance from which you want to read.
     * @return Read all the content from an inputStream instance.
     */
    public static String readAllFromInputStream(InputStream inputStream) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String result = "", line = "";
        while((line = reader.readLine()) != null){
            result += line;
        }
        return result;
    }

}
