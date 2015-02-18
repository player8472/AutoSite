/*
 * NeembuuUploader.java
 *
 * Created on Mar 28, 2010, 8:56:48 PM
 */
package neembuuuploader;

import java.sql.Connection;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.exceptions.proxy.NUProxyException;
import neembuuuploader.httpclient.NUHttpClient;
import neembuuuploader.interfaces.UploadStatus;
import neembuuuploader.interfaces.Uploader;
import neembuuuploader.settings.SettingsManager;
import neembuuuploader.settings.SettingsProperties;
import neembuuuploader.uploaders.*;
import neembuuuploader.uploaders.common.MonitoredFileBody;
import neembuuuploader.uploaders.common.MonitoredFileEntity;
import neembuuuploader.uploaders.common.StringUtils;
import neembuuuploader.utils.FileClassPair;
import neembuuuploader.utils.FileDrop;
import neembuuuploader.utils.NULogger;
import neembuuuploader.utils.NeembuuUploaderLanguages;
import neembuuuploader.utils.NeembuuUploaderProperties;
import neembuuuploader.utils.NeembuuUploaderSplashScreen;
import neembuuuploader.utils.ProxyChecker;
import neembuuuploader.utils.UploadStatusUtils;
import neembuuuploader.versioning.CheckUpdate;
//import neembuuuploader.versioning.UserImpl;
import org.apache.http.conn.params.ConnRoutePNames;

/**
 * Main class of this project. Everything starts from the main method in this
 * class.
 *
 * @author vigneshwaran
 */
public class NeembuuUploader extends javax.swing.JFrame {

    //Version variable
    public static final float version = 2.98f;
    //ArrayList of files
    public List<File> files = new ArrayList<File>();
    //reference for NUTableModel singleton
    NUTableModel nuTableModel;
    //mapping of checkboxes with their associated hosts
    private Map<JCheckBox, Class<? extends Uploader>> map = null;
    //ImageIcons for 3 states of each of the 4 move rows buttons
    private ImageIcon topnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24.png"));
    private ImageIcon topmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24mouseentered.png"));
    private ImageIcon topmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/top24mousepressed.png"));
    private ImageIcon bottomnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24.png"));
    private ImageIcon bottommouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24mouseentered.png"));
    private ImageIcon bottommousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24mousepressed.png"));
    private ImageIcon upnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24.png"));
    private ImageIcon upmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24mouseentered.png"));
    private ImageIcon upmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/up24mousepressed.png"));
    private ImageIcon downnormal = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24.png"));
    private ImageIcon downmouseover = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24mouseentered.png"));
    private ImageIcon downmousepressed = new ImageIcon(getClass().getResource("/neembuuuploader/resources/down24mousepressed.png"));
    private JFileChooser f = null;
    private TrayIcon trayIcon = null;

    /**
     *
     * @return singleton instance
     */
    public static NeembuuUploader getInstance() {
        return LazyInitialize.lazy_singleton;
    }
    //Reference for CheckBoxActionListener common for all the checkboxes
    private ActionListener checkBoxActionListener = new CheckBoxActionListener();

    private void setUpFileChooser() {
        f = new JFileChooser();
        //Enable selection of multiple files
        f.setMultiSelectionEnabled(true);
    }

    private void setUpTrayIcon() {
        if (SystemTray.isSupported()) {
            //trayIcon.setImageAutoSize(true); It renders the icon very poorly.
            //So we render the icon ourselves with smooth settings.
            {
                Dimension d = SystemTray.getSystemTray().getTrayIconSize();
                trayIcon = new TrayIcon(getIconImage().getScaledInstance(d.width, d.height, Image.SCALE_SMOOTH));
            }
            //trayIcon = new TrayIcon(getIconImage());
            //trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(TranslationProvider.get("neembuuuploader.NeembuuUploader.trayIconToolTip"));
            trayIcon.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    NULogger.getLogger().info("System tray double clicked");

                    setExtendedState(JFrame.NORMAL);
                    setVisible(true);
                    repaint();
                    SystemTray.getSystemTray().remove(trayIcon);
                }
            });
        }
    }
    
    /**
     * Creates the initial instance of HttpClient.
     */
    private void setUpHttpClient() {
        NUHttpClient.getHttpClient();
    }

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }

    private static final class LazyInitialize {

        private static final NeembuuUploader lazy_singleton = new NeembuuUploader();
    }

    /**
     * Creates new form NeembuuUploader
     */
    private NeembuuUploader() {

        NULogger.getLogger().log(Level.INFO, "{0}: Starting up..", getClass().getName());

        //Display the splashscreen until the NeembuuUploader is initialized
        NeembuuUploaderSplashScreen.getInstance().setVisible(true);

        //Setup NeembuuUploaderProperties.. Create the file if it doesn't exist.
        NeembuuUploaderProperties.setUp();

        //Initialize components
       initComponents();
       try{
        
        Connection testcon=autouploader.database.postgresql.Conn.connection();
        System.out.println("Testcon offen");
        testcon.close();
        System.out.println("closed");
        }catch (Exception e){
            e.printStackTrace();
        }

        setUpTrayIcon();

        setUpFileChooser();
        
        setUpHttpClient();


        //map each checkbox to its class in the hashmap variable
        checkBoxOperations();

        //Load previously saved state
        loadSavedState();

        //This 3rd party code is to enable Drag n Drop of files
        FileDrop fileDrop = new FileDrop(this, new FileDrop.Listener() {

            @Override
            public void filesDropped(java.io.File[] filesSelected) {
                //Ignore directories
                for (File file : filesSelected) {
                    if (file.isFile()) {
                        files.add(file);
                    }
                }
                if (files.isEmpty()) {
                    return;
                }
                //If one file is dropped, display its name.. If more than one dropped, display the number of files selected
                if (files.size() == 1) {
                    inputFileTextField.setText(files.get(0) + "");
                } else {
                    inputFileTextField.setText(files.size() + " " + TranslationProvider.get("neembuuuploader.NeembuuUploader.nfilesselected"));
                }
                NULogger.getLogger().info("Files Dropped");
            }
        });



        //Timer is used to periodically refresh the table so that the progress will appear smoothly.
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //Update every 1s
                Timer autoUpdate = new Timer(1000, new ActionListener() {
                    //Check if the queue is locked. If not, repaint.

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!QueueManager.getInstance().isQueueLocked()) {
                            //Should not call firetablerowsupdated as it'll lose the selection of rows.                             //
                            //So repaint is called.
                            neembuuUploaderTable.repaint();
                        }
                    }
                });
                //Unnecessary.. but waits for 3 seconds within which other threads will get more juice and initialize faster..
                //reduced from 10 to 3 as I moved to faster pc
                autoUpdate.setInitialDelay(3000);
                //Start the timer.
                autoUpdate.start();
                NULogger.getLogger().info("Timer started..");
            }
        });

        //By now everything is loaded, so no need of splashscreen anymore,, dispose it. :)
        NeembuuUploaderSplashScreen.getInstance().dispose();
        NULogger.getLogger().info("Splash screen disposed..");

        //Make the NeembuuUploader appear in center of screen.
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        logo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        inputFileTextField = new javax.swing.JTextField();
        selectFileButton = new javax.swing.JButton();
        addToQueueButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        selectHostsButton = new javax.swing.JButton();
        selectedHostsLabel = new javax.swing.JLabel();
        exitButton = new javax.swing.JButton();
        aboutButton = new javax.swing.JButton();
        recentButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        nuTableModel = NUTableModel.getInstance();
        neembuuUploaderTable = new javax.swing.JTable(nuTableModel);
        moveToTopButton = new javax.swing.JLabel();
        moveToBottomButton = new javax.swing.JLabel();
        moveUpButton = new javax.swing.JLabel();
        moveDownButton = new javax.swing.JLabel();
        startQueueButton = new javax.swing.JButton();
        stopFurtherButton = new javax.swing.JButton();
        accountsButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        maxuploadspinner = new javax.swing.JSpinner();
        settingsButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Neembuu Uploader");
        setIconImage(Toolkit.getDefaultToolkit().getImage((getClass().getResource("/neembuuuploader/resources/Icon.png"))));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/Icon.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 3, 24)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/Logo.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(logo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Select File(s) to upload or Drag and drop files over this window:"));

        inputFileTextField.setEditable(false);
        inputFileTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                inputFileTextFieldMouseClicked(evt);
            }
        });

        selectFileButton.setText("Select File(s)");
        selectFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFileButtonActionPerformed(evt);
            }
        });

        addToQueueButton.setText("Add to Queue");
        addToQueueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addToQueueButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Selected Host(s):");

        selectHostsButton.setText("Select Hosts");
        selectHostsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectHostsButtonActionPerformed(evt);
            }
        });

        selectedHostsLabel.setText("<html><i>None.. :(</i></html>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(inputFileTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 808, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(selectFileButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(selectedHostsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(selectHostsButton)
                        .addGap(18, 18, 18)
                        .addComponent(addToQueueButton)))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addToQueueButton, selectFileButton, selectHostsButton});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputFileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectFileButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addToQueueButton)
                        .addComponent(selectHostsButton))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(selectedHostsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {addToQueueButton, inputFileTextField, jLabel2, selectFileButton, selectHostsButton, selectedHostsLabel});

        exitButton.setText("Exit");
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });

        aboutButton.setText("About");
        aboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutButtonActionPerformed(evt);
            }
        });

        recentButton.setText("Upload History");
        recentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recentButtonActionPerformed(evt);
            }
        });

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Upload Queue:"));

        //Set up ProgressBar as renderer for progress column
        ProgressRenderer renderer = new ProgressRenderer(0,100);
        renderer.setStringPainted(true); //Show Progress text
        UploadStatusRenderer uploadStatusRenderer = new UploadStatusRenderer();
        neembuuUploaderTable.setDefaultRenderer(JProgressBar.class, renderer);
        neembuuUploaderTable.setDefaultRenderer(UploadStatus.class, uploadStatusRenderer);
        //Set table's row height large enough to fit JProgressBar.
        neembuuUploaderTable.setRowHeight((int)renderer.getPreferredSize().getHeight());
        neembuuUploaderTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        neembuuUploaderTable.getTableHeader().setReorderingAllowed(false);
        neembuuUploaderTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                neembuuUploaderTableMouseReleased(evt);
            }
        });
        neembuuUploaderTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                neembuuUploaderTableKeyTyped(evt);
            }
        });
        jScrollPane1.setViewportView(neembuuUploaderTable);
        neembuuUploaderTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        moveToTopButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/top24.png"))); // NOI18N
        moveToTopButton.setToolTipText("Move selected row(s) to Top of Queue");
        moveToTopButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveToTopButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveToTopButtonMouseReleased(evt);
            }
        });

        moveToBottomButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/bottom24.png"))); // NOI18N
        moveToBottomButton.setToolTipText("Move selected row(s) to Bottom of Queue");
        moveToBottomButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveToBottomButtonMouseReleased(evt);
            }
        });

        moveUpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/up24.png"))); // NOI18N
        moveUpButton.setToolTipText("Move selected row(s) Up in Queue");
        moveUpButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveUpButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveUpButtonMouseReleased(evt);
            }
        });

        moveDownButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/neembuuuploader/resources/down24.png"))); // NOI18N
        moveDownButton.setToolTipText("Move selected row(s) Down in Queue");
        moveDownButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseExited(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                moveDownButtonMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                moveDownButtonMouseReleased(evt);
            }
        });

        startQueueButton.setText("Start Queue");
        startQueueButton.setToolTipText("Start queued uploads if any");
        startQueueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startQueueButtonActionPerformed(evt);
            }
        });

        stopFurtherButton.setText("Stop Further");
        stopFurtherButton.setToolTipText("Stop when the current upload is finished");
        stopFurtherButton.setEnabled(false);
        stopFurtherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopFurtherButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 924, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(moveToTopButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveUpButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveDownButton)
                        .addGap(18, 18, 18)
                        .addComponent(moveToBottomButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 582, Short.MAX_VALUE)
                        .addComponent(startQueueButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stopFurtherButton)))
                .addContainerGap())
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {moveDownButton, moveToBottomButton, moveToTopButton, moveUpButton});

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {startQueueButton, stopFurtherButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 227, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(moveToTopButton)
                    .addComponent(moveUpButton)
                    .addComponent(moveDownButton)
                    .addComponent(moveToBottomButton)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(stopFurtherButton)
                        .addComponent(startQueueButton))))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {moveDownButton, moveToBottomButton, moveToTopButton, moveUpButton, startQueueButton, stopFurtherButton});

        accountsButton.setText("Accounts");
        accountsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountsButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Max. no. of simultaneous uploads:");

        maxuploadspinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2), Integer.valueOf(1), null, Integer.valueOf(1)));
        maxuploadspinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxuploadspinnerStateChanged(evt);
            }
        });

        settingsButton.setText("Settings");
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(maxuploadspinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 335, Short.MAX_VALUE)
                        .addComponent(recentButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(accountsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(settingsButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aboutButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exitButton))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {aboutButton, exitButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(exitButton)
                    .addComponent(aboutButton)
                    .addComponent(settingsButton)
                    .addComponent(accountsButton)
                    .addComponent(recentButton)
                    .addComponent(maxuploadspinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel3, maxuploadspinner});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFileButtonActionPerformed



        //Open up the Open File dialog
        //If the user clicks cancel or close, do not continue.
        if (f.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        //getSelectedFiles() returns as File array.
        //We need ArrayList for efficiency. So convert array to ArrayList
        this.files = new ArrayList<File>(Arrays.asList(f.getSelectedFiles()));

        //Same stuff as in FileDrop code in constructor
        if (files.size() == 1) {
            inputFileTextField.setText(files.get(0) + "");
        } else {
            inputFileTextField.setText(files.size() + " " + TranslationProvider.get("neembuuuploader.NeembuuUploader.nfilesselected"));
        }
        NULogger.getLogger().info("Files selected");
    }//GEN-LAST:event_selectFileButtonActionPerformed

    private void addToQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addToQueueButtonActionPerformed
        // If no files are selected, do not continue. Show error msg.
        if (files.isEmpty()) {
            JOptionPane.showMessageDialog(this, TranslationProvider.get("neembuuuploader.NeembuuUploader.pleaseSelectAnyFiles"));
            return;
        }

        //Declare an empty list for adding selected classes into it.
        List<Class<? extends Uploader>> selectedUploaderClasses = new ArrayList<Class<? extends Uploader>>();

        //Iterate throught the map. If a checkbox is selected, add it's associated class into the above list.
        for (Map.Entry<JCheckBox, Class<? extends Uploader>> entry : map.entrySet()) {
            if (entry.getKey().isSelected()) {
                selectedUploaderClasses.add(entry.getValue());
            }
        }

        //If class list is empty, that is no checkbox selected, show error msg and go back.
        if (selectedUploaderClasses.isEmpty()) {
            JOptionPane.showMessageDialog(this, TranslationProvider.get("neembuuuploader.NeembuuUploader.selectAtleastOneHost"));
            return;
        }


        //If everything is okay, then we must lock the queue to prevent removal(will cause conflict with indices) or starting next upload
        //Lock Queue
        QueueManager.getInstance().setQueueLock(true);

        //Iterate through each file in selected files list
        for (File file : files) {

            //For each file, iterate through each class in selected host classes
            for (Class<? extends Uploader> uploaderClass : selectedUploaderClasses) {
                try {
                    //Get the constructor of that class which has one File parameter.. Infact there is only one constructor and that's that..
                    Constructor<? extends Uploader> uploaderConstructor = uploaderClass.getConstructor(File.class);

                    //Use the constructor to create a new instance by passing the file parameter.
                    //Pass that instance to tablemodel's addUpload method
                    nuTableModel.addUpload(uploaderConstructor.newInstance(file));
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        //We must always unlock queue if we had locked before so that the queuing sequence may start again.
        //Unlock Queue
        QueueManager.getInstance().setQueueLock(false);

        //Set a friendly text for fun.. :)
        inputFileTextField.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.goAheadMakeMoreUploads"));


        //Clear the list of files // May change this in future..
        files.clear();


        NULogger.getLogger().info("Files added to queue");
    }//GEN-LAST:event_addToQueueButtonActionPerformed

    private void inputFileTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_inputFileTextFieldMouseClicked
        //if the textfield is clicked, do the same operations as when the select button is clicked.
        selectFileButtonActionPerformed(null);
    }//GEN-LAST:event_inputFileTextFieldMouseClicked

    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
        //Must save the states if user set like that, before exiting
        saveStateOnClosing();
    }//GEN-LAST:event_exitButtonActionPerformed

    private void recentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recentButtonActionPerformed
        //Opens UploadHistory window
        UploadHistory foo = UploadHistory.getInstance();
        foo.setLocationRelativeTo(this);
        foo.setVisible(true);
        NULogger.getLogger().info("Upload history window opened");
    }//GEN-LAST:event_recentButtonActionPerformed

    private void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutButtonActionPerformed
        //Opens About Window
        NULogger.getLogger().info("Opening About window");
        AboutNeembuuUploader foo = AboutNeembuuUploader.getInstance();
        foo.setLocationRelativeTo(this);
        foo.setVisible(true);
    }//GEN-LAST:event_aboutButtonActionPerformed

    private void maxuploadspinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxuploadspinnerStateChanged
        //Event listener for state changed event for the spinner        

        //getValue() returns as object.. So must convert to string and then to int
        int maxNoOfUploads = Integer.parseInt(maxuploadspinner.getValue() + "");

        // set this bufferSize to queuemanager's variable
        QueueManager.getInstance().setMaxNoOfUploads(maxNoOfUploads);

        //Update the queuing sequence so that more uploads may be started.
        QueueManager.getInstance().updateQueue();
    }//GEN-LAST:event_maxuploadspinnerStateChanged

    private void accountsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountsButtonActionPerformed
        //Displays Account Manager Window
        NULogger.getLogger().info("Opening Accounts Manager window");
        AccountsManager.getInstance().setVisible(true);
    }//GEN-LAST:event_accountsButtonActionPerformed
    /**
     * Depending on OS, the right click menu may be triggered for one of these 3
     * methods. So better register event for all.
     *
     * @param evt
     */
private void neembuuUploaderTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMouseClicked
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMouseClicked

private void neembuuUploaderTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMousePressed
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMousePressed

private void neembuuUploaderTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_neembuuUploaderTableMouseReleased
    openPopup(evt);
}//GEN-LAST:event_neembuuUploaderTableMouseReleased

    /**
     * The following methods set the icons for each state of mouse event of the
     * 4 move row buttons and also call the appropriate functions when clicked
     *
     * @param evt
     */
private void moveToTopButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseEntered
    moveToTopButton.setIcon(topmouseover);
}//GEN-LAST:event_moveToTopButtonMouseEntered

private void moveToTopButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseExited
    moveToTopButton.setIcon(topnormal);
}//GEN-LAST:event_moveToTopButtonMouseExited

private void moveToBottomButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseEntered
    moveToBottomButton.setIcon(bottommouseover);
}//GEN-LAST:event_moveToBottomButtonMouseEntered

private void moveToBottomButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseExited
    moveToBottomButton.setIcon(bottomnormal);
}//GEN-LAST:event_moveToBottomButtonMouseExited

private void moveUpButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseEntered
    moveUpButton.setIcon(upmouseover);
}//GEN-LAST:event_moveUpButtonMouseEntered

private void moveUpButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseExited
    moveUpButton.setIcon(upnormal);
}//GEN-LAST:event_moveUpButtonMouseExited

private void moveDownButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseEntered
    moveDownButton.setIcon(downmouseover);
}//GEN-LAST:event_moveDownButtonMouseEntered

private void moveDownButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseExited
    moveDownButton.setIcon(downnormal);
}//GEN-LAST:event_moveDownButtonMouseExited

private void moveToTopButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMousePressed
    moveToTopButton.setIcon(topmousepressed);
}//GEN-LAST:event_moveToTopButtonMousePressed

private void moveToTopButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseReleased
    moveToTopButton.setIcon(topmouseover);
}//GEN-LAST:event_moveToTopButtonMouseReleased

private void moveToTopButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToTopButtonMouseClicked
    QueueManager.getInstance().moveRowsTop();
}//GEN-LAST:event_moveToTopButtonMouseClicked

private void moveUpButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMousePressed
    moveUpButton.setIcon(upmousepressed);
}//GEN-LAST:event_moveUpButtonMousePressed

private void moveUpButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseReleased
    moveUpButton.setIcon(upmouseover);
}//GEN-LAST:event_moveUpButtonMouseReleased

private void moveDownButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMousePressed
    moveDownButton.setIcon(downmousepressed);
}//GEN-LAST:event_moveDownButtonMousePressed

private void moveDownButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseReleased
    moveDownButton.setIcon(downmouseover);
}//GEN-LAST:event_moveDownButtonMouseReleased

private void moveToBottomButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMousePressed
    moveToBottomButton.setIcon(bottommousepressed);
}//GEN-LAST:event_moveToBottomButtonMousePressed

private void moveToBottomButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseReleased
    moveToBottomButton.setIcon(bottommouseover);
}//GEN-LAST:event_moveToBottomButtonMouseReleased

private void moveUpButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveUpButtonMouseClicked
    QueueManager.getInstance().moveRowsUp();
}//GEN-LAST:event_moveUpButtonMouseClicked

private void moveDownButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveDownButtonMouseClicked
    QueueManager.getInstance().moveRowsDown();
}//GEN-LAST:event_moveDownButtonMouseClicked

private void moveToBottomButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_moveToBottomButtonMouseClicked
    QueueManager.getInstance().moveRowsBottom();
}//GEN-LAST:event_moveToBottomButtonMouseClicked

private void startQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startQueueButtonActionPerformed
    //Start the queued items if any.. This will set the stopfurther to false and update the queuing there.
    QueueManager.getInstance().setStopFurther(false);

    //Toggle the enabled state of these two buttons
    stopFurtherButton.setEnabled(true);
    startQueueButton.setEnabled(false);
    NULogger.getLogger().info("Start Queue Button clicked.");
}//GEN-LAST:event_startQueueButtonActionPerformed

private void stopFurtherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopFurtherButtonActionPerformed
    //Do not upload the further items in queue.. 
    //Stop with the current uploads.
    //This will set the stopfurther to false and update the queuing there.
    QueueManager.getInstance().setStopFurther(true);

    //Toggle the enabled state of these two buttons
    startQueueButton.setEnabled(true);
    stopFurtherButton.setEnabled(false);
    NULogger.getLogger().info("Stop Further Button clicked");
}//GEN-LAST:event_stopFurtherButtonActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    //If the user clicks the close button on NeembuuUploader, save the state before closing
    saveStateOnClosing();
}//GEN-LAST:event_formWindowClosing

private void neembuuUploaderTableKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_neembuuUploaderTableKeyTyped
    //If the delete key is pressed, then selected rows must be deleted.

    //Must be delete key and minimum of one row must be selected.
    if ((evt.getKeyChar() != KeyEvent.VK_DELETE) || (neembuuUploaderTable.getSelectedRowCount() < 0)) {
        return;
    }

    NULogger.getLogger().info("Delete Key event on Main window");

    //Must lock queue
    QueueManager.getInstance().setQueueLock(true);

    int selectedrow;
    int[] selectedrows = neembuuUploaderTable.getSelectedRows();

    //Remove from the end.. This is the correct way.
    //If you remove from top, then index will change everytime and it'll be stupid to try to do that way.
    for (int i = selectedrows.length - 1; i >= 0; i--) {
        selectedrow = selectedrows[i];

        //Remove only if the selected upload is in one of these states. For others, there is stop method.
        if (UploadStatusUtils.isRowStatusOneOf(selectedrow, UploadStatus.QUEUED, UploadStatus.UPLOADFINISHED,
                UploadStatus.UPLOADFAILED, UploadStatus.UPLOADSTOPPED)) {

            NUTableModel.getInstance().removeUpload(selectedrow);
            NULogger.getLogger().log(Level.INFO, "{0}: Removed row no. {1}", new Object[]{getClass().getName(), selectedrow});
        }

    }


    //Unlock Queue back
    QueueManager.getInstance().setQueueLock(false);
}//GEN-LAST:event_neembuuUploaderTableKeyTyped

private void selectHostsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectHostsButtonActionPerformed
    //Display HostsPanel
    NULogger.getLogger().info("Opening HostsPanel window");
    NULogger.getLogger().info("Updating language");
    HostsPanel.updateLanguage();
    HostsPanel.getInstance().setVisible(true);
}//GEN-LAST:event_selectHostsButtonActionPerformed

    private void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsButtonActionPerformed
        //Display Settings Window
        NULogger.getLogger().info("Opening Settings window");
        SettingsManager.getInstance().setVisible(true);
    }//GEN-LAST:event_settingsButtonActionPerformed

private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
    if (!SettingsProperties.isPropertyTrue("minimizetotray") || !SystemTray.isSupported() || trayIcon == null || !isActive()) {
        return;
    }
    NULogger.getLogger().info("Minimizing to Tray");
    setVisible(false);
    try {
        SystemTray.getSystemTray().add(trayIcon);
    } catch (AWTException ex) {
        setVisible(true);
        Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
    }
}//GEN-LAST:event_formWindowIconified

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        SettingsProperties.init();
        if (SettingsProperties.isPropertyTrue("logging")) {
            NULogger.getLogger().setLevel(Level.INFO);
            NULogger.getLogger().info("Logger turned on");
        } else {
            NULogger.getLogger().info("Turning off logger");
            NULogger.getLogger().setLevel(Level.OFF);
        }


        ///Set Up UI Look and Feel
        try {
            //Read the settings file and set up the user preferred theme
            String theme = SettingsProperties.getProperty("theme", "default");
            if (theme.equals("nimbus")) {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        NULogger.getLogger().info("Setting Nimbus Look and Feel");
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } else {
                //Else set the System look and feel.
                NULogger.getLogger().info("Setting System Look and Feel");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
            try {
                // In case any exception occured, try to set the System Look and feel again. It must not give any problems
                NULogger.getLogger().info("Setting System Look and Feel (under Exception)");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex);
            }
        }

        //Update selected Language on GUI components
        TranslationProvider.changeLanguage(NeembuuUploaderLanguages.getUserLanguageCode());


        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {


                    //Initialize the instance..
                    //Actually this statement was used to initialize for sometime.
                    //But the TranslationProvider.changeLanguage() method few lines above will do that for us.
                    //This will just return the already initialized instance. :)
                    NeembuuUploader.getInstance();




                    //Runs in a separate thread.
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                //If this is the firstlaunch(set by the NeembuuUploaderProperties class),
                                //then display AccountsManager
                                //and set the key back to false
                                if (NeembuuUploaderProperties.isPropertyTrue("firstlaunch")) {
                                    NULogger.getLogger().info("First launch.. Display Language Dialog..");
                                    displayLanguageOptionDialog();
                                    NULogger.getLogger().info("First launch.. Display Accounts Manager..");
                                    AccountsManager.getInstance().setVisible(true);
                                    NeembuuUploaderProperties.setProperty("firstlaunch", "false");
                                } else {
                                    //If it is not the first launch, then
                                    //start login process for enabled accounts
                                    AccountsManager.loginEnabledAccounts();
                                }
                            } catch (Exception ex) {
                                NULogger.getLogger().log(Level.WARNING, "{0}: Exception while logging in", getClass().getName());
                                System.err.println(ex);
                            }
                        }
                    });

                    //The following code is to write the fallback location to the Readme_for_Tamil_Locale.txt file
                    File readmetamil = new File(AppLocation.getPath(), "Readme_for_Tamil_Locale.txt");
                    if (!readmetamil.exists()) {
                        NULogger.getLogger().info("Writing Readme_for_Tamil_Locale.txt");
                        PrintWriter out = new PrintWriter(readmetamil);
                        out.write("If you don't use Tamil language, ignore this file."
                                + "\r\n\r\nTamil is not one of the officially supported locale by Java. But there is a workaround for this."
                                + "\r\n\r\nIf you wish to use Neembuu Uploader in Tamil, kindly copy the included 'LATHA.TTF' font to"
                                + "\r\n\"<JRE_INSTALL_DIR>/jre/lib/fonts/fallback\""
                                + "\r\n\r\nLocation to paste the fallback font for your pc is:\r\n\"" + System.getProperty("java.home") + File.separator
                                + "lib" + File.separator + "fonts" + File.separator + "fallback"
                                + File.separator + "\"");
                        out.close();
                        NULogger.getLogger().log(Level.INFO, "Fallback location: {0}{1}lib{2}fonts{3}fallback", new Object[]{System.getProperty("java.home"), File.separator, File.separator, File.separator});
                    }

                    //Finally start the update checking thread.
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            new CheckUpdate().start();
                        }
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    NULogger.getLogger().severe(ex.toString());
                }
            }
        });
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aboutButton;
    private javax.swing.JButton accountsButton;
    private javax.swing.JButton addToQueueButton;
    private javax.swing.JButton exitButton;
    private javax.swing.JTextField inputFileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel logo;
    private javax.swing.JSpinner maxuploadspinner;
    private javax.swing.JLabel moveDownButton;
    private javax.swing.JLabel moveToBottomButton;
    private javax.swing.JLabel moveToTopButton;
    private javax.swing.JLabel moveUpButton;
    public javax.swing.JTable neembuuUploaderTable;
    private javax.swing.JButton recentButton;
    private javax.swing.JButton selectFileButton;
    private javax.swing.JButton selectHostsButton;
    private javax.swing.JLabel selectedHostsLabel;
    private javax.swing.JButton settingsButton;
    private javax.swing.JButton startQueueButton;
    private javax.swing.JButton stopFurtherButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Opens up the Popup Menu
     *
     * @param evt
     */
    private void openPopup(MouseEvent evt) {
        //The above three mouse events call this method.
        //So check which one will trigger Rightclick menu depending on os and use it.
        if (evt.isPopupTrigger()) {
            //Check if it is right click.
            if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                NULogger.getLogger().info("RightClick event");
                //if already 2 or more rows selected, leave it.
                //EDIT: After some days I forgot what this code does,.. This is copied from a stackoverflow post actually..
                //But it's an unchangeable code and will just work forever, so I'm not gonna mess with it.
                if (neembuuUploaderTable.getSelectedRowCount() < 2) {
                    int r = neembuuUploaderTable.rowAtPoint(evt.getPoint());
                    if (r >= 0 && r < neembuuUploaderTable.getRowCount()) {
                        neembuuUploaderTable.setRowSelectionInterval(r, r);
                    } else {
                        neembuuUploaderTable.clearSelection();
                    }

                    int rowindex = neembuuUploaderTable.getSelectedRow();
                    if (rowindex < 0) {
                        return;
                    }
                }
                //Display the popup menu on the exact point of right click.
                PopupBuilder.getInstance().show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }

    /**
     * This method will map the checkbox and associated classes. If you are a
     * developer, who added a new host, you'll have to insert your line here in
     * the appropriate position in alphabetical order
     */
    private void checkBoxOperations() {
        ////////////////////List of all accounts//////////////////////////////////

        //If you are a plugin developer, this is the only place for you in this class.
        //You'll have to insert your code somewhere in alphabetical order.
        //This will add the row to the table..
        //It is better you insert in alphabetical order yourself, 
        //rather than using sorted list which reduces performance.
        //Refer to the project explorer which displays the classes in alphabetical order
        NULogger.getLogger().info("Mapping Checkboxes");
        map = new LinkedHashMap<JCheckBox, Class<? extends Uploader>>();
        
        //===============================================================================
        //================================DEAD file-hosts================================
        //===============================================================================
        
        //map.put(HostsPanel.getInstance().badongoCheckBox, Badongo.class);
        //map.put(HostsPanel.getInstance().cloudZerCheckBox, CloudZer.class);
        //map.put(HostsPanel.getInstance().easyshareCheckBox, EasyShare.class);
        //map.put(HostsPanel.getInstance().enterUploadCheckBox, EnterUpload.class);
        //map.put(HostsPanel.getInstance().fileDenCheckBox, FileDen.class);
        //map.put(HostsPanel.getInstance().fileDudeCheckBox, FileDude.class);
        //map.put(HostsPanel.getInstance().fileSonicCheckBox, FileSonic.class);
        //map.put(HostsPanel.getInstance().flameUploadCheckBox, FlameUpload.class);
        //map.put(HostsPanel.getInstance().gruploadCheckBox, GRupload.class);
        //map.put(HostsPanel.getInstance().hotFileCheckBox, HotFile.class);
        //map.put(HostsPanel.getInstance().iFileCheckBox, IFile.class); //moved to filecloud.io
        //map.put(HostsPanel.getInstance().megaUpCheckBox, MegaUp.class);
        //map.put(HostsPanel.getInstance().megaUploadCheckBox, MegaUpload.class); // R.I.P
        //map.put(HostsPanel.getInstance().multiUploadCheckBox, MultiUpload.class);
        //map.put(HostsPanel.getInstance().oronCheckBox, Oron.class);
        //map.put(HostsPanel.getInstance().uGotFileCheckBox, UGotFile.class);
        //map.put(HostsPanel.getInstance().uploadBoxCheckBox, UploadBox.class);
        //map.put(HostsPanel.getInstance().upBoothCheckBox, UpBooth.class);
        //map.put(HostsPanel.getInstance().wuploadCheckBox, Wupload.class);
        //map.put(HostsPanel.getInstance().zShareCheckBox, ZShare.class);
        
        //===============================================================================
        //========================Hosts which start with a number========================
        //===============================================================================
        
        map.put(HostsPanel.getInstance().oneEightyCheckBox, OneEightyUpload.class);
        map.put(HostsPanel.getInstance().oneFichierCheckBox, OneFichier.class);
        map.put(HostsPanel.getInstance().twoSharedCheckBox, TwoShared.class);
        map.put(HostsPanel.getInstance().fourSharedCheckBox, FourShared.class);
        map.put(HostsPanel.getInstance().fourUpFilesCheckBox, FourUpFiles.class);

        //===============================================================================
        //=====================Standard hosts in alphabetical order======================
        //===============================================================================
        
        map.put(HostsPanel.getInstance().allMyVideosCheckBox, AllMyVideos.class);
        map.put(HostsPanel.getInstance().anonFilesCheckBox, AnonFiles.class);
        map.put(HostsPanel.getInstance().arabLoadsCheckBox, ArabLoads.class);
        map.put(HostsPanel.getInstance().asFileCheckBox, Asfile.class);
        map.put(HostsPanel.getInstance().bayFilesCheckBox, BayFiles.class);
        map.put(HostsPanel.getInstance().billionUploadsCheckBox, BillionUploads.class);
        map.put(HostsPanel.getInstance().bitShareCheckBox, BitShare.class);
        map.put(HostsPanel.getInstance().bLCheckBox, Blst.class);
        map.put(HostsPanel.getInstance().boxDotComCheckBox, BoxDotCom.class);
        map.put(HostsPanel.getInstance().clicknUploadCheckBox, ClicknUpload.class);
        map.put(HostsPanel.getInstance().clickToWatchCheckBox, ClickToWatch.class);
        map.put(HostsPanel.getInstance().cloudyEcCheckBox, CloudyEc.class);
        map.put(HostsPanel.getInstance().cloudFlyCheckBox, CloudFly.class);
        map.put(HostsPanel.getInstance().crockoCheckBox, Crocko.class);
        map.put(HostsPanel.getInstance().dataFileCheckBox, DataFile.class);
        map.put(HostsPanel.getInstance().ddlStorageCheckBox, DDLStorage.class);
        map.put(HostsPanel.getInstance().depositFilesCheckBox, DepositFiles.class);
        map.put(HostsPanel.getInstance().dogeFileCheckBox, DogeFile.class);
        map.put(HostsPanel.getInstance().dropBoxCheckBox, DropBox.class);
        map.put(HostsPanel.getInstance().easyBytezCheckBox, EasyBytez.class);
        map.put(HostsPanel.getInstance().ediskCzCheckBox, EdiskCz.class);
        map.put(HostsPanel.getInstance().fileCloudCheckBox, FileCloud.class);
        map.put(HostsPanel.getInstance().fileCloudCcCheckBox, FileCloudCc.class);
        map.put(HostsPanel.getInstance().fileDaisCheckBox, FileDais.class);
        map.put(HostsPanel.getInstance().fileDropperCheckBox, FileDropper.class);
        map.put(HostsPanel.getInstance().fileFactoryCheckBox, FileFactory.class);
        map.put(HostsPanel.getInstance().fileHootCheckBox, FileHoot.class);
        map.put(HostsPanel.getInstance().fileInzCheckBox, FileInz.class);
        map.put(HostsPanel.getInstance().fileJokerCheckBox, FileJoker.class);
        map.put(HostsPanel.getInstance().fileOmCheckBox, FileOm.class);
        map.put(HostsPanel.getInstance().fileParadoxCheckBox, FileParadox.class);
        map.put(HostsPanel.getInstance().filePostCheckBox, FilePost.class);
        map.put(HostsPanel.getInstance().fileRioCheckBox, FileRio.class);
        map.put(HostsPanel.getInstance().filesTwoShareCheckBox, FilesTwoShare.class);
        map.put(HostsPanel.getInstance().fileServeCheckBox, FileServe.class);
        map.put(HostsPanel.getInstance().fileStormCheckBox, FileStorm.class);
        map.put(HostsPanel.getInstance().fileViceCheckBox, FileVice.class);
        map.put(HostsPanel.getInstance().filesFlashCheckBox, FilesFlash.class);
        map.put(HostsPanel.getInstance().fireDriveCheckBox, FireDrive.class);
        map.put(HostsPanel.getInstance().flashXCheckBox, FlashX.class);
        map.put(HostsPanel.getInstance().freakShareCheckBox, FreakShare.class);
        map.put(HostsPanel.getInstance().gBoxesCheckBox, GBoxes.class);
        map.put(HostsPanel.getInstance().gettCheckBox, Gett.class);
        map.put(HostsPanel.getInstance().gigaSizeCheckBox, GigaSize.class);
        map.put(HostsPanel.getInstance().griftHostCheckBox, GriftHost.class);
        map.put(HostsPanel.getInstance().hostrCheckBox, Hostr.class);
        map.put(HostsPanel.getInstance().hugeFilesCheckBox, HugeFiles.class);
        map.put(HostsPanel.getInstance().imageShackCheckBox, ImageShack.class);
        map.put(HostsPanel.getInstance().junoCloudCheckBox, JunoCloud.class);
        map.put(HostsPanel.getInstance().keepTwoShareCheckBox, KeepTwoShare.class);
        map.put(HostsPanel.getInstance().kingFilesCheckBox, KingFiles.class);
        map.put(HostsPanel.getInstance().letitbitCheckBox, Letitbit.class);
        map.put(HostsPanel.getInstance().loadToCheckBox, LoadTo.class);
        map.put(HostsPanel.getInstance().lomaFileCheckBox, LomaFile.class);
        map.put(HostsPanel.getInstance().luckyShareCheckBox, LuckyShare.class);
        map.put(HostsPanel.getInstance().massMirrorCheckBox, MassMirror.class);
        map.put(HostsPanel.getInstance().mediaFireCheckBox, MediaFire.class);
        map.put(HostsPanel.getInstance().mediaFreeCheckBox, MediaFree.class);
        map.put(HostsPanel.getInstance().megaCacheCheckBox, MegaCache.class);
        map.put(HostsPanel.getInstance().megasharesCheckBox, Megashares.class);
        map.put(HostsPanel.getInstance().mightyUploadCheckBox, MightyUpload.class);
        map.put(HostsPanel.getInstance().mixtureCloudCheckBox, MixtureCloud.class);
        map.put(HostsPanel.getInstance().multiUploadDotBizCheckBox, MultiUploadDotBiz.class);
        map.put(HostsPanel.getInstance().multiUploadDotNlCheckBox, MultiUploadDotNl.class);
        map.put(HostsPanel.getInstance().myDiscCheckBox, MyDisc.class);
        map.put(HostsPanel.getInstance().netLoadCheckBox, NetLoad.class);
        map.put(HostsPanel.getInstance().netUCheckBox, NetU.class);
        map.put(HostsPanel.getInstance().nitroFlareCheckBox, NitroFlare.class);
        map.put(HostsPanel.getInstance().novaFileCheckBox, NovaFile.class);
        map.put(HostsPanel.getInstance().nowDownloadCheckBox, NowDownload.class);
        map.put(HostsPanel.getInstance().nowVideoCheckBox, NowVideo.class);
        map.put(HostsPanel.getInstance().obOomCheckBox, Oboom.class);
        map.put(HostsPanel.getInstance().promptFileCheckBox, PromptFile.class);
        map.put(HostsPanel.getInstance().privateFilesCheckBox, PrivateFiles.class);
        map.put(HostsPanel.getInstance().rainUploadCheckBox, RainUpload.class);
        map.put(HostsPanel.getInstance().rapidGatorCheckBox, RapidGator.class);
        map.put(HostsPanel.getInstance().rapidShareCheckBox, RapidShare.class);
        map.put(HostsPanel.getInstance().rapidUCheckBox, RapidU.class);
        map.put(HostsPanel.getInstance().rockFileCheckBox, RockFile.class);
        map.put(HostsPanel.getInstance().ryuShareCheckBox, RyuShare.class);
        map.put(HostsPanel.getInstance().safeSharingCheckBox, SafeSharing.class);
        map.put(HostsPanel.getInstance().scribdCheckBox, Scribd.class);
        map.put(HostsPanel.getInstance().secureUploadCheckBox, SecureUpload.class);
        map.put(HostsPanel.getInstance().sendSpaceCheckBox, SendSpace.class);
        map.put(HostsPanel.getInstance().sharedCheckBox, Shared.class);
        map.put(HostsPanel.getInstance().shareFlareCheckBox, ShareFlare.class);
        map.put(HostsPanel.getInstance().shareOnlineCheckBox, ShareOnline.class);
        map.put(HostsPanel.getInstance().shareSendCheckBox, ShareSend.class);
        map.put(HostsPanel.getInstance().slingFileCheckBox, SlingFile.class);
        map.put(HostsPanel.getInstance().sockShareCheckBox, SockShare.class);
        map.put(HostsPanel.getInstance().solidFilesCheckBox, Solidfiles.class);
        map.put(HostsPanel.getInstance().speedyShareCheckBox, SpeedyShare.class);
        map.put(HostsPanel.getInstance().speedVideoCheckBox, SpeedVideo.class);
        map.put(HostsPanel.getInstance().streamCloudCheckBox, StreamCloud.class);
        map.put(HostsPanel.getInstance().streamInCheckBox, Streamin.class);
        map.put(HostsPanel.getInstance().sugarSyncCheckBox, SugarSync.class);
        map.put(HostsPanel.getInstance().teraFileCheckBox, TeraFile.class);
        map.put(HostsPanel.getInstance().turboBitCheckBox, TurboBit.class);
        map.put(HostsPanel.getInstance().turtleShareCheckBox, TurtleShare.class);
        map.put(HostsPanel.getInstance().tusFilesCheckBox, TusFiles.class);
        map.put(HostsPanel.getInstance().ultraMegaBitCheckBox, UltraMegaBit.class);
        map.put(HostsPanel.getInstance().updownBzCheckBox, UpdownBz.class);
        map.put(HostsPanel.getInstance().uploadAbleCheckBox, UploadAble.class);
        map.put(HostsPanel.getInstance().uploadBoyCheckBox, UploadBoy.class);
        map.put(HostsPanel.getInstance().uploadDriveCheckBox, UploadDrive.class);
        map.put(HostsPanel.getInstance().uploadHeroCheckBox, UploadHero.class);
        map.put(HostsPanel.getInstance().uploadMBCheckBox, UploadMB.class);
        map.put(HostsPanel.getInstance().uploadedDotToCheckBox, UploadedDotTo.class);
        map.put(HostsPanel.getInstance().uploadingDotComCheckBox, UploadingDotCom.class);
        map.put(HostsPanel.getInstance().uploadRocketCheckBox, UploadRocket.class);
        map.put(HostsPanel.getInstance().upaFileCheckBox, UpaFile.class);
        map.put(HostsPanel.getInstance().uppItCheckBox, UppIt.class);
        map.put(HostsPanel.getInstance().upSharedCheckBox, UpShared.class);
        map.put(HostsPanel.getInstance().upStoreCheckBox, UpStore.class);
        map.put(HostsPanel.getInstance().uptoboxCheckBox, Uptobox.class);
        map.put(HostsPanel.getInstance().upZeroSevenCheckBox, UpZeroSeven.class);
        map.put(HostsPanel.getInstance().useFileCheckBox, UseFile.class);
        map.put(HostsPanel.getInstance().verZendCheckBox, Verzend.class);
        map.put(HostsPanel.getInstance().vidBullCheckBox, VidBull.class);
        map.put(HostsPanel.getInstance().videoMegaCheckBox, VideoMega.class);
        map.put(HostsPanel.getInstance().videoWoodCheckBox, VideoWood.class);
        map.put(HostsPanel.getInstance().vidToCheckBox, VidTo.class);
        map.put(HostsPanel.getInstance().vidUpCheckBox, VidUp.class);
        map.put(HostsPanel.getInstance().vidXdenCheckBox, VidXden.class);
        map.put(HostsPanel.getInstance().vidZiCheckBox, VidZi.class);
        map.put(HostsPanel.getInstance().vipFileCheckBox, VipFile.class);
        map.put(HostsPanel.getInstance().vodLockerCheckBox, VodLocker.class);
        map.put(HostsPanel.getInstance().vozUploadCheckBox, VozUpload.class);
        map.put(HostsPanel.getInstance().vShareCheckBox, VShare.class);
        map.put(HostsPanel.getInstance().xerVerCheckBox, Xerver.class);
        map.put(HostsPanel.getInstance().xfileLoadCheckBox, XfileLoad.class);
        map.put(HostsPanel.getInstance().xvidStageCheckBox, XvidStage.class);
        map.put(HostsPanel.getInstance().youTubeCheckBox, YouTube.class);
        map.put(HostsPanel.getInstance().youWatchCheckBox, YouWatch.class);
        map.put(HostsPanel.getInstance().yourVideoHostCheckBox, YourVideoHost.class);
        map.put(HostsPanel.getInstance().zippyShareCheckBox, ZippyShare.class);
        map.put(HostsPanel.getInstance().zohoDocsCheckBox, ZohoDocs.class);
        
        //Arrange the checkboxes in alphabetical order
        HostsPanel.getInstance().arrangeCheckBoxes(map);

        //Add action listeners common to all the checkboxes.
        Iterator<JCheckBox> it = map.keySet().iterator();
        while (it.hasNext()) {
            it.next().addActionListener(checkBoxActionListener);
        }

        //Log supported sites count
        NULogger.getLogger().log(Level.INFO, "{0}: Number of supported sites: {1}", new Object[]{getClass().getName(), map.size()});
    }

    public JTable getTable() {
        return neembuuUploaderTable;
    }

    /**
     * Save the state on exit (unless the user specified otherwise)
     */
    private void saveStateOnClosing() {
        //Show a splash screen for a while
        NeembuuUploaderSplashScreen.getInstance().setMessage(TranslationProvider.get("neembuuuploader.NeembuuUploader.savingstate")).setVisible(true);

        //Do the operations if user settings are enabled..
        if (SettingsProperties.isPropertyTrue("savecontrolstate")) {
            /////////////Save Max No Of Uploads//////////////
            //Convert the integer to string before setting.
            SettingsProperties.setProperty("maxNoOfUploads",
                    Integer.toString(QueueManager.getInstance().getMaxNoOfUploads()));
            NULogger.getLogger().log(Level.INFO, "{0}: Maxnoofuploads saved", getClass().getName());
            /////////////////////////////////////////////////

            ////////////////Save selected checkboxes/////////////////
            //Save selected checkboxes on exit
            StringBuilder sb = new StringBuilder();
            Iterator<JCheckBox> it = map.keySet().iterator();
            while (it.hasNext()) {
                sb.append(it.next().isSelected()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            SettingsProperties.setProperty("selectedcheckboxes", sb.toString());
            NULogger.getLogger().log(Level.INFO, "{0}: Checkbox state saved", getClass().getName());
            ////////////////////////////////////////////////////
        }

        ////////////////////////Save queued files////////////////////////
        if (SettingsProperties.isPropertyTrue("savequeuedlinks")) {
            ///IMPORTANT PART - Serialize upload list to disk

            //We need only two parameters.. File and Class. So created a class called FileClassPair and made it serializable
            //Now Declared an empty arraylist of FileClassPair
            List<FileClassPair> savelist = new ArrayList<FileClassPair>();

            //Iterate through each row in the queue
            for (Uploader uploader : nuTableModel.uploadList) {
                //Add only if the status is Queued
                if (uploader.getStatus() == UploadStatus.QUEUED) {
                    savelist.add(new FileClassPair(uploader.getFile(), uploader.getClass()));
                }
            }

            //Serialize the object to nu.dat file locally.
            File savefile = new File(AppLocation.getPath(), "nu.dat");
            //savefile.delete(); //no need.

            //Continue if the list is not empty.
            if (!savelist.isEmpty()) {
                try {
                    //Write object to that file and close the stream.
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savefile));
                    oos.writeObject(savelist);
                    oos.close();
                    NULogger.getLogger().log(Level.INFO, "{0}: Queued List saved", getClass().getName());
                } catch (IOException ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        }

        /////////////Save current path//////////////
        if (SettingsProperties.isPropertyTrue("savecurrentpath")) {
            
            //Save the position in the property file
            SettingsProperties.setProperty("currentpath", f.getCurrentDirectory().getAbsolutePath());
            NULogger.getLogger().log(Level.INFO, "{0}: currentpath saved: "+f.getCurrentDirectory(), getClass().getName());
        }
        
        //After all over finally exit.. :)
        NULogger.getLogger().log(Level.INFO, "{0}: Exiting..", getClass().getName());
        System.exit(0);
    }

    /**
     * Loads previously saved state (based on user preference).
     */
    private void loadSavedState() {
        //Check user preference
        if (SettingsProperties.isPropertyTrue("savecontrolstate")) {

            //////////////////////////////Set Max no of uploads///////////////////////////////////////////////
            NULogger.getLogger().log(Level.INFO, "{0}: Loading maxnoofuploads value..", getClass().getName());
            maxuploadspinner.setValue(Integer.valueOf(SettingsProperties.getProperty("maxNoOfUploads", "2")));
            //////////////////////////////////////////////////////////////////////////////////////////////////

            //////////////////////////////Set selected checkboxes///////////////////////////////////////////////
            try {
                NULogger.getLogger().log(Level.INFO, "{0}: Setting preferred checkboxes..", getClass().getName());
                String selectedcheckboxesproperty = SettingsProperties.getProperty("selectedcheckboxes", "");
                //if the property is empty (or if it is first launch), ignore
                if (!selectedcheckboxesproperty.isEmpty()) {
                    //The string will be something like true,false,false,true..
                    List<String> selectedcheckboxes = Arrays.asList(selectedcheckboxesproperty.split(","));
                    Iterator<JCheckBox> mapit = map.keySet().iterator();
                    for (String selected : selectedcheckboxes) {
                        mapit.next().setSelected(Boolean.valueOf(selected));
                    }
                }
                //Update the hosts label to display the selected hosts
                updateSelectedHostsLabel();
            } catch (Exception e) {
                NULogger.getLogger().severe(e.toString());
                System.err.println(e);
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////save queued links//////////////////////////////////////////////////
        if (SettingsProperties.isPropertyTrue("savequeuedlinks")) {
            ///Important.. Deserialize listofuploads
            NULogger.getLogger().log(Level.INFO, "{0}: Adding previously queued uploads", getClass().getName());
            File savefile = new File(AppLocation.getPath(), "nu.dat");
            //If this file exists, continue. It may not exist if there was no queued upload on previous exit or if it is the first launch.
            if (savefile.exists()) {

                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(savefile));
                    //Get Map
                    List<FileClassPair> savelist = (List<FileClassPair>) ois.readObject();

                    //Add files if they exist to uploadlist
                    //already initcomponents executed. so no need to worry if uploadlist is created or not.

                    for (FileClassPair pair : savelist) {
                        //The user may have deleted some files. So we must check whether each file exists before adding.
                        if (pair.getFile().exists()) {
                            Constructor<? extends Uploader> uploaderConstructor = pair.getHostclass().getConstructor(File.class);
                            nuTableModel.addUpload(uploaderConstructor.newInstance(pair.getFile()));
                        }
                    }

                    //Delete the file after closing all streams
                    ois.close();
                    savefile.delete();

                } catch (Exception ex) {
                    Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                    System.err.println(ex);
                }

            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////Save current path//////////////////////////////////////////////////
        if (SettingsProperties.isPropertyTrue("savecurrentpath")) {
            f.setCurrentDirectory(new File(SettingsProperties.getProperty("currentpath", null)));
            NULogger.getLogger().log(Level.INFO, "{0}: Loading currentpath value: "+f.getCurrentDirectory(), getClass().getName() );
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        /////////////////////////////////////Using Proxy//////////////////////////////////////////////////
        if (SettingsProperties.isPropertyTrue("usingProxy")) {
            try {
                String proxyAddress = SettingsProperties.getProperty("proxyAddress");
                String proxyPort = SettingsProperties.getProperty("proxyPort");
                
                ProxyChecker proxyChecker = new ProxyChecker(proxyAddress, proxyPort);
                
                if(proxyChecker.control()){
                    NULogger.getLogger().log(Level.INFO, "Proxy is ok!");
                }
                else{
                    NULogger.getLogger().log(Level.INFO, "Proxy isn't ok!");
                }
            }
            catch (NUProxyException ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
                ex.printError();
            } catch (Exception ex) {
                Logger.getLogger(NeembuuUploader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        NULogger.getLogger().log(Level.INFO, "Route is: {0}", NUHttpClient.getHttpClient().getParams().getParameter(ConnRoutePNames.DEFAULT_PROXY));
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        
        
        /////////////////////////////////////Buffer Size//////////////////////////////////////////////////
        if (SettingsProperties.getProperty("bufferSize", null) != null) {
            int bufferSize = (int) StringUtils.getSizeFromString(SettingsProperties.getProperty("bufferSize"));
            MonitoredFileBody.setBufferSize(bufferSize);
            MonitoredFileEntity.setBufferSize(bufferSize);
            
            NULogger.getLogger().log(Level.INFO, "Buffer size is: {0}", bufferSize);
        }
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    /**
     * Update the hosts label to show the list of checkboxes selected
     */
    public void updateSelectedHostsLabel() {
        StringBuilder sb = new StringBuilder();
        List<Class<? extends Uploader>> classlist = new ArrayList<Class<? extends Uploader>>();
        for (Map.Entry<JCheckBox, Class<? extends Uploader>> entry : map.entrySet()) {
            if (entry.getKey().isSelected()) {
                classlist.add(entry.getValue());
            }
        }

        //If no checkboxes selected, show None and a sad smiley
        if (classlist.isEmpty()) {
            selectedHostsLabel.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.selectedHostsLabel"));
            return;
        }

        //If the hosts are upto 3, display their names separated by comma
        //If more than 3, display the first three and "and n more"
        //Why 3? There's more space.. For some language, the label to the left can become long. So 3 is safe..
        if (classlist.size() <= 3) {
            for (Class<? extends Uploader> cl : classlist) {
                sb.append(cl.getSimpleName()).append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append(classlist.get(0).getSimpleName()).append(", ").append(classlist.get(1).getSimpleName()).append(", ").append(classlist.get(2).getSimpleName()).append(" and ");
            sb.append((classlist.size() - 3)).append(" more..");
        }
        selectedHostsLabel.setText(sb.toString());
        NULogger.getLogger().fine(sb.toString());
    }

    /**
     * Update the language at runtime Also updates the column of tablemodel and
     * repaints the table.
     */
    final void languageChanged_UpdateGUI() { /*
         * package private
         */
        NULogger.getLogger().log(Level.INFO, "{0} Calling languageChanged_UpdateGUI", getClass().getName());
        setTitle(TranslationProvider.get("neembuuuploader.NeembuuUploader.neembuuuploader"));
        jLabel2.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.jLabel2"));
        selectHostsButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.selectHostsButton"));
        selectFileButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.selectFileButton"));
        addToQueueButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.addToQueueButton"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(TranslationProvider.get("neembuuuploader.NeembuuUploader.jPanel2.setBorder")));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(TranslationProvider.get("neembuuuploader.NeembuuUploader.jPanel3.setBorder")));

        startQueueButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.startQueueButton"));
        stopFurtherButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.stopFurtherButton"));
        jLabel3.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.jLabel3"));
        recentButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.recentButton"));
        accountsButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.accountsButton"));
        settingsButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.settings"));
        aboutButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.aboutButton"));
        exitButton.setText(TranslationProvider.get("neembuuuploader.NeembuuUploader.exitButton"));



        moveToTopButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.moveToTopToolTip"));
        moveUpButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.moveUpButtonToolTip"));
        moveDownButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.moveDownButtonToolTip"));
        moveToBottomButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.moveToBottomButtonToolTip"));

        startQueueButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.startQueueButtonToolTip"));
        stopFurtherButton.setToolTipText(TranslationProvider.get("neembuuuploader.NeembuuUploader.stopFurtherButtonToolTip"));


        //Set selected hosts label
        updateSelectedHostsLabel();

        //Update table columns
        nuTableModel.languageChanged_UpdateColumnNames();
        neembuuUploaderTable.repaint();

    }

    /**
     * Whenever a checkbox is selected, the hosts label must update. This is a
     * listener common for all checkboxes.
     */
    private class CheckBoxActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            updateSelectedHostsLabel();
            HostsPanel.getInstance().setFocusToTabbedPane();
        }
    }

    /**
     * This displays a small dialog for user to choose the language from a set
     * of available options
     */
    private static void displayLanguageOptionDialog() {
        //This code returns the selected language if and only if the user selects the Ok button
        String selectedlanguage = (String) JOptionPane.showInputDialog(NeembuuUploader.getInstance(),
                "Choose your language: ",
                "Language",
                JOptionPane.PLAIN_MESSAGE, null,
                NeembuuUploaderLanguages.getLanguageNames(),
                NeembuuUploaderLanguages.getUserLanguageName());

        //selectedlanguage will be null if the user clicks the cancel or close button
        //so check for that.
        if (selectedlanguage != null) {
            //Set the language to settings file
            NeembuuUploaderLanguages.setUserLanguageByName(selectedlanguage);
            //Change the GUI to new language.
            TranslationProvider.changeLanguage(NeembuuUploaderLanguages.getUserLanguageCode());
        }
    }

    /*
     * package
     */ Map<JCheckBox, Class<? extends Uploader>> getMap() {
        return map;
    }
     
     /**
      * For peace of mind, the version number used for properties file,
      * version.xml and other stuf is Float.toString(version).
      * For display in user interface 
      * @see #getVersionNumberForUI() 
      * @return version number for usage for non-ui part
      */
     public static String getVersionForProgam(){
         return Float.toString(version);
}
     
     /**
      * This should be used only for user interface.
      * It converts float into a stylised version
      * Like if version = 2.912213 this return 2.9.12213
      * @return version number for user interface
      */
     public static String getVersionNumberForUI(){
         return getVersionNumber(version);
     }
     
     static String getVersionNumber(float ver){
         String v = "";
         int major = (int)ver;
         int minor = (int)((ver*10)-major*10);
         String sub_minor = "";
         
         v = major+"."+minor;
         
         if(ver> major+minor*0.1f){
            sub_minor = Float.toString(ver);
            sub_minor = sub_minor.substring(sub_minor.indexOf(".")+2);
            v = v + "." + sub_minor;
         }
         
         return v;
     }
     
     /**
      * Reset the title of the frame.
      */
     public void resetTitle(){
          setTitle(TranslationProvider.get("neembuuuploader.NeembuuUploader.neembuuuploader"));
    }
}
