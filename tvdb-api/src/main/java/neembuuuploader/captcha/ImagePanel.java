/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.captcha;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.utils.NULogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author davidepastore
 */
public class ImagePanel extends JPanel{

    private BufferedImage image;
    
    public ImagePanel(){
        
    }
    
    public ImagePanel(URL imageFileUrl, HttpContext httpContext){
        try {                
            HttpClient httpClient = NUHttpClient.getHttpClient();
            HttpGet httpGet = new HttpGet(imageFileUrl.toURI());
            HttpResponse httpresponse = httpClient.execute(httpGet, httpContext);
            byte[] imageInByte = EntityUtils.toByteArray(httpresponse.getEntity());
            InputStream in = new ByteArrayInputStream(imageInByte);
            image = ImageIO.read(in);
            //image = ImageIO.read(imageFileUrl);
        } catch (Exception ex) {
            NULogger.getLogger().log(Level.INFO, "ImagePanel exception: {0}", ex.getMessage());
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //int x = this.getWidth()/4;
        g.drawImage(image, 10, 10, null); // see javadoc for more info on the parameters            
    }
}
