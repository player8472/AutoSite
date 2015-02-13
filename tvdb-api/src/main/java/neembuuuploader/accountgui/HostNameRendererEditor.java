/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accountgui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.EventObject;
import java.util.logging.Level;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import neembuuuploader.TranslationProvider;
import neembuuuploader.utils.NULogger;

/**
 * This renderer is used only for textfield look instead of default label look.
 *
 * @author shashanktulsyan
 */
public class HostNameRendererEditor extends AbstractCellEditor
        implements TableCellRenderer, TableCellEditor {

    private HostNameCellPanel renderer = new HostNameCellPanel();
    private HostNameCellPanel editor = new HostNameCellPanel();
    
    public HostNameRendererEditor() {

    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        renderer.setHostNameValue((String) value);
        return renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        editor.setHostNameValue((String) value);
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return editor.getHostNameValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return false;
    }

    private static final class HostNameCellPanel extends JPanel {

        private final JLabel hostName = new JLabel();
        private final JButton registerButton = new JButton(
                TranslationProvider.get("neembuuuploader.accountgui.HostNameRendererEditor.HostNameCellPanel.registerButton"));

        public HostNameCellPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            registerButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        
                        String url = NewAccountURLUtil.createNewAccountRegistrationURL(hostName.getText());
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception ex) {
                        NULogger.getLogger().log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(null, "Could not open registration page", hostName.getText(), JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            add(hostName);
            add(Box.createHorizontalStrut(10));
            add(registerButton);
        }
        
        String getHostNameValue(){
            return hostName.getText();
        }
        
        void setHostNameValue(String hostNameText){
            hostName.setText(hostNameText);
        }

    }

}
