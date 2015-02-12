/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuuuploader.utils;

import javax.swing.JTable;
import neembuuuploader.NUTableModel;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.interfaces.UploadStatus;

/**
 * Commons utils for UploadStatus.
 * @author davidepastore
 */
public class UploadStatusUtils {

    /**
     * Check if a row is with a status.
     * @param row The row index of the neembuu table.
     * @param uploadStatus A list of UploadStatus objects.
     * @return Returns true if the row status with i is one of the uploadStatus, false otherwise.
     */
    public static boolean isRowStatusOneOf(int row, UploadStatus... uploadStatus) {
        JTable table = NeembuuUploader.getInstance().getTable();
        for (UploadStatus status : uploadStatus) {
            if (table.getValueAt(row, NUTableModel.STATUS) == status) {
                return true;
            }
        }
        return false;
    }
    
}
