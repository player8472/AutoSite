/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.interfaces.abstractimpl;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.Uploader;
import neembuuuploader.uploaders.common.CommonUploaderTasks;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.MonitoredFileEntity;
import neembuuuploader.versioning.UserImpl;

/**
 *
 * @author vigneshwaran
 */
public abstract class AbstractUploader implements Uploader {

    protected File file;
    protected String downURL = "";
    protected String delURL = "";
    protected String host = "";
    protected final AtomicInteger uploadProgress = new AtomicInteger(0);
    protected StringBuffer speed = new StringBuffer();
    protected UploadStatus status = UploadStatus.QUEUED;
    protected Thread thread = new Thread(this);
    protected long maxFileSizeLimit = Long.MAX_VALUE;
    private boolean retry = false;

    public AbstractUploader(File file) {
        this.file = file;
    }

    public String getFileName() {
        String toRet = file.getName();
        if(!UserImpl.I().canCustomizeNormalizing()){
            toRet = UserImpl.I().normalizeFileName(toRet);
        }
        return toRet;
    }

    @Override
    public String getDisplayFileName() {
        return getFileName();
    }

    public String getSize() {
        return CommonUploaderTasks.getSize(file.length());
    }
    
    public long getMaxFileSizeLimit() {
        return maxFileSizeLimit;
    }

    public String getHost() {
        return host;
    }

    public int getProgress() {
        return uploadProgress.get();
    }
    
    public String getSpeed(){
        return speed.toString();
    }

    public UploadStatus getStatus() {
        return status;
    }

    public String getDownloadURL() {
        return downURL;
    }

    public String getDeleteURL() {
        return delURL;
    }

    public void startUpload() {
        thread.start();
    }

    public void stopUpload() {
        status = UploadStatus.UPLOADSTOPPED;
        CommonUploaderTasks.uploadStopped(this);
        thread.stop();
    }

    public File getFile() {
        return file;
    }

    public abstract void run();

    /**
     * The Uploader is initializing its variables.
     */
    protected void uploadInitialising() {
        if (retry) {
            status = UploadStatus.RETRYING;
        } else {
            status = UploadStatus.INITIALISING;
        }
    }
    
    /**
     * The upload operation is starting now.
     */
    protected void uploading() {
        if (retry) {
            status = UploadStatus.REUPLOADING;
        } else {
            status = UploadStatus.UPLOADING;
        }
    } 
    
    /**
     * The upload is invalid. One reason can be the file size.
     */
    protected void uploadInvalid() {
        status = UploadStatus.UPLOADINVALID;
        resetSpeed();
        CommonUploaderTasks.uploadFailed(this);
    }
    
    /**
     * The upload is failed.
     */
    protected void uploadFailed() {
        if (retry) {
            status = UploadStatus.RETRYFAILED;
        } else {
            status = UploadStatus.UPLOADFAILED;
        }
        resetSpeed();
        CommonUploaderTasks.uploadFailed(this);
    }

    /**
     * The upload is completed correctly.
     */
    protected void uploadFinished() {
        status = UploadStatus.UPLOADFINISHED;
        resetSpeed();
        CommonUploaderTasks.uploadFinished(this);
        ///
    }
    
    /**
     * Change the status of the Uploader.
     */
    protected void gettingLink(){
        status = UploadStatus.GETTINGLINK;
    }
    
    public void setRetry(boolean retry) {
        this.retry = retry;
        status = UploadStatus.TORETRY;
    }
    
    /**
     * Reset the speed.
     */
    private void resetSpeed(){
        speed.setLength(0);
    }
    
    /**
     * Create the MonitoredFileBody.
     * @return returns the MonitoredFileBody.
     */
    protected MonitoredFileBody createMonitoredFileBody(){
        return new MonitoredFileBody(file, uploadProgress, speed);
    }
    
    /**
     * Create the MonitoredFileEntity.
     * @return returns the MonitoredFileEntity.
     */
    protected MonitoredFileEntity createMonitoredFileEntity(){
        return new MonitoredFileEntity(file, uploadProgress, speed);
    }
}
