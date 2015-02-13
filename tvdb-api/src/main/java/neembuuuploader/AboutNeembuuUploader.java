/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Level;
import neembuuuploader.utils.NULogger;

/**
 *
 * @author dsivaji
 */
public class AboutNeembuuUploader extends javax.swing.JDialog {

    //Singleton
    private static AboutNeembuuUploader INSTANCE = new AboutNeembuuUploader(NeembuuUploader.getInstance(),true);

    /**
     * 
     * @return Singleton Instance of AboutNeembuuUploader
     */
    public static AboutNeembuuUploader getInstance() {
       // NULogger.getLogger().info("Opening About window");
        return INSTANCE;
    }

    /** 
     * Creates new form AboutNeembuuUploader
     */
    public AboutNeembuuUploader(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        //here we will use the UI
        aboutVersionLabel.setText("<html><b>v" + NeembuuUploader.getVersionNumberForUI() + "</b></html>");
    }

    private AboutNeembuuUploader() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aboutProductPanel = new javax.swing.JPanel();
        aboutLogo = new javax.swing.JLabel();
        aboutLabel = new javax.swing.JLabel();
        aboutVersionLabel = new javax.swing.JLabel();
        licenseLabel = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        descLabel = new javax.swing.JLabel();
        siteLabel = new javax.swing.JLabel();
        aboutTabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        developersTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        translatorsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        aboutProductPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Neembuu Uploader"));

        aboutLogo.setToolTipText("Open Source File Splitter and Joiner");

        aboutLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/AboutLogo.png"))); // NOI18N

        aboutVersionLabel.setText("...");

        licenseLabel.setText("License: GPLv3.0");

        authorLabel.setText("Admin: Vigneshwaran Raveendran");

        descLabel.setText("<html><b>Neembuu Uploader</b> is reviewed as the simplest and fastest file uploader program..<br />You can request new features, report bugs or broken plugin at Neembuu Uploader site..<br /><br /><i> P.S. We are in need of developers and translators..</i></html>");

        siteLabel.setText("<html><a href='http://neembuuuploader.sf.net'>http://neembuuuploader.sf.net</a></html>");
        siteLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        siteLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                siteLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout aboutProductPanelLayout = new javax.swing.GroupLayout(aboutProductPanel);
        aboutProductPanel.setLayout(aboutProductPanelLayout);
        aboutProductPanelLayout.setHorizontalGroup(
            aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutProductPanelLayout.createSequentialGroup()
                .addGroup(aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(aboutLogo)
                    .addGroup(aboutProductPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(aboutProductPanelLayout.createSequentialGroup()
                                .addComponent(aboutLabel)
                                .addGap(18, 18, 18)
                                .addComponent(aboutVersionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(licenseLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(authorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(siteLabel))))
                .addContainerGap())
        );
        aboutProductPanelLayout.setVerticalGroup(
            aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutProductPanelLayout.createSequentialGroup()
                .addGroup(aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(aboutProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(aboutVersionLabel)
                        .addComponent(aboutLabel))
                    .addComponent(aboutLogo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(licenseLabel)
                .addGap(11, 11, 11)
                .addComponent(authorLabel)
                .addGap(11, 11, 11)
                .addComponent(descLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(siteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        developersTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Davide Pastore", "Plugins and many features"},
                {"Dinesh Sivaji", "Plugins for lots of sites"},
                {"Jeyanthan Inbasekaran", "Community Documentation"},
                {"MNidhal", "Plugins"},
                {"Paralytic", "Plugins"},
                {"Shashaank Tulsyan", "Code Reviewing, Translation Framework"},
                {"Vigneshwaran Raveendran", "Framework, User Interface"}
            },
            new String [] {
                "Developer", "Role"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        developersTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(developersTable);
        developersTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        developersTable.getColumnModel().getColumn(0).setMaxWidth(250);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        aboutTabbedPane.addTab("<html><b>Development Team</b></html>", jPanel1);

        translatorsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Brazilian Portugese", "Maykon da Silva Siqueira"},
                {"Catalan", "Jordi Castells"},
                {"Chinese (Simplified)", "Raullen Qi Chai"},
                {"Chinese (Traditional)", "吳宇軒 (Nathan Wu)"},
                {"French", "Stéphane Rajalu"},
                {"German", "Florian Haag"},
                {"Greek", "Vasilis Lessis"},
                {"Hebrew", "Noam Y. Gherson, Itamar Shoham"},
                {"Hindi", "Shashaank Tulsyan, Vaishnavi Vasanth"},
                {"Hungarian", "Krisztian Mukli"},
                {"Italian", "Salvo Cortesiano"},
                {"Malay", "Natesan Vellaichamy"},
                {"Russian", "Ruslan Matsiev"},
                {"Sourashtra", "Balaji Chithu Sivanath"},
                {"Spanish", "Jordi Castells"},
                {"Tamil", "Vigneshwaran Raveendran"},
                {"Turkish", "Atif Zafrak"},
                {"Vietnamese", "Nguyen Kien"}
            },
            new String [] {
                "Language", "Translators"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(translatorsTable);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                .addContainerGap())
        );

        aboutTabbedPane.addTab("<html><b>Translation Team</b></html>", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(aboutProductPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(aboutTabbedPane, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aboutProductPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(aboutTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void siteLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_siteLabelMouseClicked

    if (!Desktop.isDesktopSupported()) {
        return;
    }
    try {
        NULogger.getLogger().log(Level.INFO, "{0}Opening Neembuu Site..", getClass().getName());
        Desktop.getDesktop().browse(new URI("http://neembuuuploader.sourceforge.net/"));
    } catch (Exception ex) {
    }
}//GEN-LAST:event_siteLabelMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
              
                new AboutNeembuuUploader().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JLabel aboutLogo;
    private javax.swing.JPanel aboutProductPanel;
    private javax.swing.JTabbedPane aboutTabbedPane;
    private javax.swing.JLabel aboutVersionLabel;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JLabel descLabel;
    private javax.swing.JTable developersTable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel licenseLabel;
    private javax.swing.JLabel siteLabel;
    private javax.swing.JTable translatorsTable;
    // End of variables declaration//GEN-END:variables
    }
