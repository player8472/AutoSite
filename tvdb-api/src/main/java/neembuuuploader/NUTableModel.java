/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package neembuuuploader;

import java.util.logging.Level;
import neembuuuploader.interfaces.Uploader;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.utils.NULogger;

/**
 * Custom Table Model for Neembuu Uploader table
 * @author vigneshwaran
 */
public class NUTableModel extends AbstractTableModel {
    //Singleton
    private static NUTableModel INSTANCE = new NUTableModel();
    
    //These are the names for the table's columns.
    private static final String[] columnNames = new String[8];
    
    static {
        languageChanged_UpdateColumnNames();
    }

    //These are the classes for each column's values.
    private static final Class[] columnClasses = {String.class, String.class,
    String.class, UploadStatus.class, String.class, JProgressBar.class, String.class, String.class};

    //These int are used to access Column names without using explicit index
    public static final int FILE = 0;
    public static final int SIZE = 1;
    public static final int HOST = 2;
    public static final int STATUS = 3;
    public static final int SPEED = 4;
    public static final int PROGRESS = 5;
    public static final int DOWNLOADURL = 6;
    public static final int DELETEURL = 7;
    
    
    //The table's list of uploads.
    public static final ArrayList<Uploader> uploadList = new ArrayList<Uploader>();
    
    /**
     * Non instantiable. Use getInstance().
     */
    private NUTableModel(){
    }
    
    /**
     * 
     * @return singleton instance of table model
     */
    public static NUTableModel getInstance() {
        return INSTANCE;
    }
    

    /**
     * Adds a new upload to the table.
     */
    public void addUpload(Uploader upload){

        uploadList.add(upload);
        //Fire table row insertion notification to table.
        fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
        NULogger.getLogger().log(Level.INFO, "{0}New upload added", getClass().getName());
    }

    /**
     * Remove the selected row from table. 
     * Careful when removing as index of all rows change 
     * after removing a particular row
     * (if that row is not the last)
     */
    public void removeUpload(int selectedrow) {
        uploadList.remove(selectedrow);
        //Fire table row insertion notification to table.
        fireTableRowsDeleted(selectedrow, selectedrow);
        NULogger.getLogger().log(Level.INFO, "{0}: Row at {1} deleted", new Object[]{getClass().getName(), selectedrow});
    }
    
    /**
     * 
     * @return no of rows
     */
    @Override
    public int getRowCount() {
        return uploadList.size();
    }

    /**
     * 
     * @return no of columns
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets a column's name.
     */
    @Override
    public String getColumnName(int col){
        return columnNames[col];
    }

    /**
     * Gets a column's class.
     */
    @Override
    public Class getColumnClass(int col){
        return columnClasses[col];
    }

    /**
     * 
     * @param rowIndex
     * @param columnIndex
     * @return the value at the cell under particular row index and column index
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
        Uploader upload = uploadList.get(rowIndex);
        switch(columnIndex){
            case 0: //Filename
                return upload.getDisplayFileName();
            case 1: //Size
                return upload.getSize();
            case 2: //Host
                return upload.getHost();
            case 3: //Status
                return upload.getStatus();
            case 4: //Speed
                return upload.getSpeed();
            case 5: //Progress
                return new Integer(upload.getProgress());
            case 6: //DownloadURL
                return upload.getDownloadURL();
            case 7: //DeleteURL
                return upload.getDeleteURL();
        }
        
        } catch(Exception e) {
            ///Exception occurs when user removes some rows and progress bar requesting old index.. Must catch this otherwise runtime error
            NULogger.getLogger().log(Level.SEVERE, "{0}: {1}", new Object[]{getClass().getName(), e});
        }
        return "";
    }
    
    
    /**
     * Update the columns when the language is changed..
     */
    final static void languageChanged_UpdateColumnNames(){ 
        NULogger.getLogger().log(Level.INFO, "{0}Updating column names", NUTableModel.class.getName());
        columnNames[0]=TranslationProvider.get("neembuuuploader.NUTableModel.File");
        columnNames[1]=TranslationProvider.get("neembuuuploader.NUTableModel.Size");
        columnNames[2]=TranslationProvider.get("neembuuuploader.NUTableModel.Host");
        columnNames[3]=TranslationProvider.get("neembuuuploader.NUTableModel.Status");
        columnNames[4]=TranslationProvider.get("neembuuuploader.NUTableModel.Speed");
        columnNames[5]=TranslationProvider.get("neembuuuploader.NUTableModel.Progress");
        columnNames[6]=TranslationProvider.get("neembuuuploader.NUTableModel.Download_URL");
        columnNames[7]=TranslationProvider.get("neembuuuploader.NUTableModel.Delete_URL");
        //Must call this to reflect change on runtime..
        INSTANCE.fireTableStructureChanged();
        //
    }
}
