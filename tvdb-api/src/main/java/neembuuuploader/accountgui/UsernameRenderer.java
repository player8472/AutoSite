/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accountgui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

/** This renderer is used only for textfield look instead of default label look.
 *
 * @author vigneshwaran
 */
public class UsernameRenderer extends JTextField
        implements TableCellRenderer {

    public UsernameRenderer(String text) {
        super(text);
    }

    

    
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        return this;
    }
    
}
