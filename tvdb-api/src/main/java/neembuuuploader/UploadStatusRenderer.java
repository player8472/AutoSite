package neembuuuploader;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import neembuuuploader.interfaces.UploadStatus;

/**
 *
 * @author Shashank Tulsyan
 */
public class UploadStatusRenderer extends DefaultTableCellRenderer {

    public UploadStatusRenderer() {
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        //This if condition is must. Otherwise problems during removing rows.
        if(value==null || value=="")setText("");
        else setText(((UploadStatus)value).getLocaleSpecificString());
        return this;
    }
    
}
