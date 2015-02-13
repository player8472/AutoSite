/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accountgui;

import java.awt.Component;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**Renderer for Password field. Without this the password will display plainly.
 *
 * @author vigneshwaran
 */
public class PasswordRenderer extends JPasswordField
        implements TableCellRenderer {

    public PasswordRenderer(String text) {
        super(text);
    }

    
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        return this;
    }
    
}
