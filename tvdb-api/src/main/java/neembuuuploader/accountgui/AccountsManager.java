/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accountgui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accounts.AllMyVideosAccount;
import neembuuuploader.accounts.ArabLoadsAccount;
import neembuuuploader.accounts.AsfileAccount;
//import neembuuuploader.accounts.BadongoAccount;
import neembuuuploader.accounts.BayFilesAccount;
import neembuuuploader.accounts.BillionUploadsAccount;
import neembuuuploader.accounts.BitShareAccount;
import neembuuuploader.accounts.BlstAccount;
import neembuuuploader.accounts.BoxDotComAccount;
import neembuuuploader.accounts.ClicknUploadAccount;
import neembuuuploader.accounts.ClickToWatchAccount;
import neembuuuploader.accounts.CloudyEcAccount;
import neembuuuploader.accounts.CloudFlyAccount;
//import neembuuuploader.accounts.CloudZerAccount;
import neembuuuploader.accounts.CrockoAccount;
import neembuuuploader.accounts.DDLStorageAccount;
import neembuuuploader.accounts.DataFileAccount;
import neembuuuploader.accounts.DepositFilesAccount;
import neembuuuploader.accounts.DogeFileAccount;
import neembuuuploader.accounts.DropBoxAccount;
import neembuuuploader.accounts.EasyBytezAccount;
import neembuuuploader.accounts.EdiskCzAccount;
//import neembuuuploader.accounts.ExtabitAccount;
import neembuuuploader.accounts.FileCloudAccount;
import neembuuuploader.accounts.FileCloudCcAccount;
import neembuuuploader.accounts.FileDaisAccount;
//import neembuuuploader.accounts.FileDenAccount;
import neembuuuploader.accounts.FileFactoryAccount;
import neembuuuploader.accounts.FileHootAccount;
import neembuuuploader.accounts.FileInzAccount;
import neembuuuploader.accounts.FileJokerAccount;
import neembuuuploader.accounts.FileOmAccount;
import neembuuuploader.accounts.FileParadoxAccount;
import neembuuuploader.accounts.FilePostAccount;
import neembuuuploader.accounts.FileRioAccount;
import neembuuuploader.accounts.FilesTwoShareAccount;
import neembuuuploader.accounts.FileServeAccount;
//import neembuuuploader.accounts.FileSonicAccount;
import neembuuuploader.accounts.FileStormAccount;
import neembuuuploader.accounts.FilesFlashAccount;
import neembuuuploader.accounts.FileViceAccount;
import neembuuuploader.accounts.FlashXAccount;
import neembuuuploader.accounts.FourSharedAccount;
import neembuuuploader.accounts.FourUpFilesAccount;
import neembuuuploader.accounts.GBoxesAccount;
import neembuuuploader.accounts.GettAccount;
import neembuuuploader.accounts.GigaSizeAccount;
import neembuuuploader.accounts.GriftHostAccount;
//import neembuuuploader.accounts.HotFileAccount;
import neembuuuploader.accounts.ImageShackAccount;
import neembuuuploader.accounts.LetitbitAccount;
import neembuuuploader.accounts.HostrAccount;
import neembuuuploader.accounts.HugeFilesAccount;
import neembuuuploader.accounts.KeepTwoShareAccount;
import neembuuuploader.accounts.KingFilesAccount;
import neembuuuploader.accounts.LuckyShareAccount;
import neembuuuploader.accounts.MassMirrorAccount;
import neembuuuploader.accounts.MediaFireAccount;
import neembuuuploader.accounts.MediaFreeAccount;
import neembuuuploader.accounts.MixtureCloudAccount;
import neembuuuploader.accounts.MultiUploadDotBizAccount;
import neembuuuploader.accounts.NetLoadAccount;
import neembuuuploader.accounts.NetUAccount;
import neembuuuploader.accounts.NitroFlareAccount;
import neembuuuploader.accounts.OneEightyUploadAccount;
import neembuuuploader.accounts.OneFichierAccount;
//import neembuuuploader.accounts.OronAccount;
import neembuuuploader.accounts.FireDriveAccount;
import neembuuuploader.accounts.FreakShareAccount;
import neembuuuploader.accounts.JunoCloudAccount;
import neembuuuploader.accounts.LomaFileAccount;
import neembuuuploader.accounts.MegaCacheAccount;
import neembuuuploader.accounts.MegasharesAccount;
import neembuuuploader.accounts.MightyUploadAccount;
import neembuuuploader.accounts.MyDiscAccount;
import neembuuuploader.accounts.NovaFileAccount;
import neembuuuploader.accounts.NowDownloadAccount;
import neembuuuploader.accounts.NowVideoAccount;
import neembuuuploader.accounts.OboomAccount;
import neembuuuploader.accounts.PromptFileAccount;
import neembuuuploader.accounts.PrivateFilesAccount;
import neembuuuploader.accounts.RainUploadAccount;
import neembuuuploader.accounts.RapidGatorAccount;
import neembuuuploader.accounts.RapidShareAccount;
import neembuuuploader.accounts.RapidUAccount;
import neembuuuploader.accounts.RockFileAccount;
import neembuuuploader.accounts.RyuShareAccount;
import neembuuuploader.accounts.SafeSharingAccount;
import neembuuuploader.accounts.ScribdAccount;
import neembuuuploader.accounts.SecureUploadAccount;
import neembuuuploader.accounts.SendSpaceAccount;
import neembuuuploader.accounts.SharedAccount;
import neembuuuploader.accounts.ShareFlareAccount;
import neembuuuploader.accounts.ShareOnlineAccount;
import neembuuuploader.accounts.SlingFileAccount;
import neembuuuploader.accounts.SockShareAccount;
import neembuuuploader.accounts.SolidfilesAccount;
import neembuuuploader.accounts.SpeedyShareAccount;
import neembuuuploader.accounts.SpeedVideoAccount;
import neembuuuploader.accounts.SugarSyncAccount;
import neembuuuploader.accounts.StreamCloudAccount;
import neembuuuploader.accounts.StreaminAccount;
import neembuuuploader.accounts.TeraFileAccount;
import neembuuuploader.accounts.TurboBitAccount;
import neembuuuploader.accounts.TurtleShareAccount;
import neembuuuploader.accounts.TusFilesAccount;
import neembuuuploader.accounts.TwoSharedAccount;
import neembuuuploader.accounts.UltraMegaBitAccount;
import neembuuuploader.accounts.UpaFileAccount;
//UpBooth.com is dead
//import neembuuuploader.accounts.UpBoothAccount;
import neembuuuploader.accounts.UpdownBzAccount;
import neembuuuploader.accounts.UploadAbleAccount;
import neembuuuploader.accounts.UploadBoyAccount;
import neembuuuploader.accounts.UploadDriveAccount;
import neembuuuploader.accounts.UploadHeroAccount;
import neembuuuploader.accounts.UploadedDotToAccount;
import neembuuuploader.accounts.UploadingDotComAccount;
//import neembuuuploader.accounts.UploadizAccount;
import neembuuuploader.accounts.UploadRocketAccount;
import neembuuuploader.accounts.UppItAccount;
import neembuuuploader.accounts.UpSharedAccount;
import neembuuuploader.accounts.UpStoreAccount;
import neembuuuploader.accounts.UptoboxAccount;
import neembuuuploader.accounts.UpZeroSevenAccount;
import neembuuuploader.accounts.UseFileAccount;
//import neembuuuploader.accounts.VReerAccount;
import neembuuuploader.accounts.VerzendAccount;
import neembuuuploader.accounts.VozUploadAccount;
import neembuuuploader.accounts.VodLockerAccount;
import neembuuuploader.accounts.VShareAccount;
import neembuuuploader.accounts.VidBullAccount;
import neembuuuploader.accounts.VideoMegaAccount;
import neembuuuploader.accounts.VideoWoodAccount;
import neembuuuploader.accounts.VidToAccount;
import neembuuuploader.accounts.VidUpAccount;
import neembuuuploader.accounts.VidXdenAccount;
import neembuuuploader.accounts.VidZiAccount;
import neembuuuploader.accounts.VipFileAccount;
//import neembuuuploader.accounts.WuploadAccount;
import neembuuuploader.accounts.XerverAccount;
import neembuuuploader.accounts.XfileLoadAccount;
import neembuuuploader.accounts.XvidStageAccount;
import neembuuuploader.accounts.YouTubeAccount;
import neembuuuploader.accounts.YouWatchAccount;
import neembuuuploader.accounts.YourVideoHostAccount;
//import neembuuuploader.accounts.ZShareAccount;
import neembuuuploader.accounts.ZippyShareAccount;
import neembuuuploader.accounts.ZohoDocsAccount;
import neembuuuploader.interfaces.Account;
import neembuuuploader.utils.NULogger;
import neembuuuploader.utils.NeembuuUploaderProperties;

/**
 *
 * @author dsivaji
 */
public class AccountsManager extends javax.swing.JDialog {

    //Singleton instance
    private static AccountsManager INSTANCE = new AccountsManager(NeembuuUploader.getInstance(), true);
    //This is the list of accounts to be displayed in Table
    private static Map<String, Account> accounts;
    //Reference to table model.
    private static DefaultTableModel model;
    //Reference to Column index. Use this instead of explicitly using index no.
    public static final int HOSTNAME = 0;
    public static final int USERNAME = 1;
    public static final int PASSWORD = 2;
    //This renderer is used only for decent look. Without this, table looks ugly.
    UsernameRenderer usernamerenderer = new UsernameRenderer("");
    //This editor is used to display *** instead of plain text "while" typing password
    TableCellEditor passwordeditor = new DefaultCellEditor(new JPasswordField(""));
    //This renderer is used to display *** instead of plain text "after" typing password
    PasswordRenderer passwordrenderer = new PasswordRenderer("");
    //This renderer+editor is to allow a "Register" button along with host name to exist in the table
    HostNameRendererEditor hostNameRendererEditor = new HostNameRendererEditor();

    public static Account getAccount(String hostname) {
        return accounts.get(hostname);
    }

    /**
     * Use this to get instance of the AccountsManager. It also updates the
     * language before returning.
     *
     * @return the singleton instance.
     */
    public static AccountsManager getInstance() {
        updateLanguage();
        return INSTANCE;
    }

    /**
     * This method is used to login enabled accounts.. Use this at startup or
     * after the save button in accounts table is clicked.
     */
    public static void loginEnabledAccounts() {

        //Create a separate thread for responsiveness of the save button
        new Thread() {
            @Override
            public void run() {
                //Iterate through each account
                for (Account account : accounts.values()) {
                    //May need to add additional conditions if premium accts have different login mechanism
                    //But that'll be in future..
                    if (account.getUsername().isEmpty() || account.getPassword().isEmpty()) {
                        //If either one field is empty, disable the account if logged in already.
                        //In fact it's enough to check one condition 
                        //as the AccountsManager won't let you save with one field empty
                        account.disableLogin();
                    } else {
                        //If both fields are present, login that account
                        NULogger.getLogger().log(Level.INFO, "Logging in to {0}", account.getHOSTNAME());
                        if (account.canLogin()) {
                            account.login();
                        }
                    }
                }
            }
        }.start();
    }

    /**
     * Private method to update the current language everytime the window is
     * about to be displayed
     */
    private static void updateLanguage() {
        NULogger.getLogger().log(Level.INFO, "{0}: Updating Language", AccountsManager.class);
        INSTANCE.setTitle(TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.title"));
        INSTANCE.infoLabel.setText(TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.infoLabel"));
        INSTANCE.infoLabel2.setText(TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.infoLabel2"));
        INSTANCE.infoLabel3.setText(TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.infoLabel3"));

        //This stupid code clears any editors or renderers...
        model.setColumnIdentifiers(new String[]{
            TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.Hostname"),
            TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.Username"),
            TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.Password")
        });

        //... so have to set them again :'(
        INSTANCE.accountsTable.getColumnModel().getColumn(0).setCellEditor(INSTANCE.hostNameRendererEditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(0).setCellRenderer(INSTANCE.hostNameRendererEditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(1).setCellRenderer(INSTANCE.usernamerenderer);
        INSTANCE.accountsTable.getColumnModel().getColumn(2).setCellEditor(INSTANCE.passwordeditor);
        INSTANCE.accountsTable.getColumnModel().getColumn(2).setCellRenderer(INSTANCE.passwordrenderer);
        //Repaint the table.. // no.. wait.. no need to call repaint.
        //INSTANCE.accountsTable.repaint();

        INSTANCE.saveButton.setText(TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.savebutton"));

        //Pack the window as the font sizes may have changed.
        INSTANCE.pack();
    }

    /**
     * Creates new form AccountsManager
     */
    public AccountsManager(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        //First initialize the components.
        initComponents();

        //Assign reference to the table model for easy typing.
        model = (DefaultTableModel) accountsTable.getModel();

        //////////List of all accounts///////

        //If you are a plugin developer, this is the only place for you in this class.
        //You'll have to insert your code somewhere here in alphabetical order.
        //It is better you insert in this linkedhashmap in alphabetical order yourself, 
        //rather than using treemap which can sort alphabetically itself but reduces performance.
        //Refer to the project explorer which displays the classes in alphabetical order
        NULogger.getLogger().info("Adding accounts to accounts table");
        accounts = new LinkedHashMap<String, Account>();
        //1Fichier- Starts with number one.. But Class names can't start with 1..
        //So putting at top.
        accounts.put("180Upload.com", new OneEightyUploadAccount());
        accounts.put("1fichier.com", new OneFichierAccount());
        accounts.put("2Shared.com", new TwoSharedAccount());
        accounts.put("4Shared.com", new FourSharedAccount());
        accounts.put("4upFiles.com", new FourUpFilesAccount());
        //Other sites in alphabetical order

        accounts.put("AllMyVideos.net", new AllMyVideosAccount());
        accounts.put("ArabLoads.net", new ArabLoadsAccount());
        accounts.put("Asfile.com", new AsfileAccount());
        //SEMI-RIP
        //accounts.put("Badongo.com", new BadongoAccount());
        accounts.put("BayFiles.com", new BayFilesAccount());
        accounts.put("BillionUploads.com", new BillionUploadsAccount());
        accounts.put("BitShare.com", new BitShareAccount());
        accounts.put("Bl.st", new BlstAccount());
        accounts.put("Box.com", new BoxDotComAccount());
        
        //Cloudzer is dead. :(
        //accounts.put("CloudZer.net", new CloudZerAccount());
        accounts.put("Crocko.com", new CrockoAccount());
        accounts.put("ClicknUpload.com", new ClicknUploadAccount());
        accounts.put("ClickToWatch.net", new ClickToWatchAccount());
        accounts.put("Cloudy.ec", new CloudyEcAccount());
        accounts.put("CloudFly.us", new CloudFlyAccount());
        accounts.put("DataFile.com", new DataFileAccount());
        accounts.put("DDLStorage.com", new DDLStorageAccount());
        accounts.put("DepositFiles.com", new DepositFilesAccount());
        accounts.put("DogeFile.com", new DogeFileAccount());
        accounts.put("DropBox.com", new DropBoxAccount());
        accounts.put("EasyBytez.com", new EasyBytezAccount());
        accounts.put("Edisk.cz", new EdiskCzAccount());
        //accounts.put("EasyShare.com", new EasyShareAccount());

        //Damn it..
        //accounts.put("EnterUpload.com",new EnterUploadAccount());

        //accounts.put("Extabit.com", new ExtabitAccount());
        accounts.put("FileDais.com", new FileDaisAccount());
        accounts.put("FileCloud.io", new FileCloudAccount());
        accounts.put("FileCloud.cc", new FileCloudCcAccount());
        //Changed to Cx.com
        //accounts.put("FileDen.com", new FileDenAccount());
        accounts.put("FileFactory.com", new FileFactoryAccount());
        accounts.put("FileHoot.com", new FileHootAccount());
        accounts.put("FileInz.com", new FileInzAccount());
        accounts.put("FileJoker.net", new FileJokerAccount());
        accounts.put("FileOM.com", new FileOmAccount());
        accounts.put("FileParadox.in", new FileParadoxAccount());
        accounts.put("FilePost.com", new FilePostAccount());
        accounts.put("FileRio.in", new FileRioAccount());
        accounts.put("Files2Share.ch", new FilesTwoShareAccount());
        accounts.put("FileServe.com", new FileServeAccount());
        accounts.put("FileStorm.to", new FileStormAccount());
        accounts.put("FilesFlash.com", new FilesFlashAccount());
        accounts.put("FileVice.com", new FileViceAccount());
        accounts.put("FlashX.tv", new FlashXAccount());
        //RIP
        //accounts.put("FileSonic.com", new FileSonicAccount());
        accounts.put("FireDrive.com", new FireDriveAccount());
        accounts.put("FreakShare.com", new FreakShareAccount());
        accounts.put("GBoxes.com", new GBoxesAccount());
        accounts.put("Ge.tt", new GettAccount());
        //accounts.put("GRupload.com", new GRuploadAccount());
        accounts.put("GigaSize.com", new GigaSizeAccount());
        accounts.put("GriftHost.com", new GriftHostAccount());
        accounts.put("Hostr.co", new HostrAccount());
        
        //Hot File is dead
        //accounts.put("HotFile.com", new HotFileAccount());
        accounts.put("HugeFiles.net", new HugeFilesAccount());
        //accounts.put("IFile.it", new IFileAccount());
        accounts.put("ImageShack.us", new ImageShackAccount());
        accounts.put("JunoCloud.me", new JunoCloudAccount());
        accounts.put("Keep2Share.cc", new KeepTwoShareAccount());
        accounts.put("KingFiles.net", new KingFilesAccount());
        accounts.put("Letitbit.net", new LetitbitAccount());
        accounts.put("Lomafile.com", new LomaFileAccount());
        accounts.put("LuckyShare.net", new LuckyShareAccount());
        accounts.put("MassMirror.com", new MassMirrorAccount());
        accounts.put("MediaFire.com", new MediaFireAccount());
        accounts.put("MediaFree.co", new MediaFreeAccount());
        accounts.put("MegaCache.net", new MegaCacheAccount());
        accounts.put("Megashares.com", new MegasharesAccount());
        accounts.put("MightyUpload.com", new MightyUploadAccount());
        //RIP MegaUpload.. We hope we'll see you soon ;(
        //accounts.put("MegaUpload.com",new MegaUploadAccount());

        accounts.put("MixtureCloud.com", new MixtureCloudAccount());
        accounts.put("MultiUpload.biz", new MultiUploadDotBizAccount());
        accounts.put("MyDisc.net", new MyDiscAccount());
        accounts.put("Netload.in", new NetLoadAccount());
        accounts.put("NetU.tv", new NetUAccount());
        accounts.put("NitroFlare.com", new NitroFlareAccount());
        accounts.put("NovaFile.com", new NovaFileAccount());
        accounts.put("NowDownload.ch", new NowDownloadAccount());
        accounts.put("NowVideo.sx", new NowVideoAccount());
        accounts.put("Oboom.com", new OboomAccount());

        //It is offline
        //accounts.put("Oron.com", new OronAccount());
        accounts.put("PromptFile.com", new PromptFileAccount());
        accounts.put("PrivateFiles.com", new PrivateFilesAccount());
        accounts.put("RainUpload.net", new RainUploadAccount());
        accounts.put("RapidGator.net", new RapidGatorAccount());
        accounts.put("RapidShare.com", new RapidShareAccount());
        accounts.put("RapidU.net", new RapidUAccount());
        accounts.put("RockFile.eu", new RockFileAccount());
        accounts.put("RyuShare.com", new RyuShareAccount());
        accounts.put("SafeSharing.eu", new SafeSharingAccount());
        accounts.put("Scribd.com", new ScribdAccount());
        accounts.put("SecureUpload.eu", new SecureUploadAccount());
        accounts.put("SendSpace.com", new SendSpaceAccount());
        accounts.put("Shared.com", new SharedAccount());
        accounts.put("ShareFlare.net", new ShareFlareAccount());
        accounts.put("Share-Online.biz", new ShareOnlineAccount());
        accounts.put("SlingFile.com", new SlingFileAccount());
        accounts.put("SockShare.com", new SockShareAccount());
        accounts.put("Solidfiles.com", new SolidfilesAccount());
        accounts.put("SpeedVideo.net", new SpeedVideoAccount());
        accounts.put("SpeedyShare.com", new SpeedyShareAccount());
        accounts.put("StreamCloud.eu", new StreamCloudAccount());
        accounts.put("Streamin.to", new StreaminAccount());
        accounts.put("SugarSync.com", new SugarSyncAccount());
        accounts.put("TeraFile.co", new TeraFileAccount());
        accounts.put("TurboBit.net", new TurboBitAccount());
        accounts.put("TurtleShare.com", new TurtleShareAccount());
        accounts.put("TusFiles.net", new TusFilesAccount());
        //may be  back
        //accounts.put("UGotFile.com",new UGotFileAccount());
        accounts.put("UltraMegaBit.com",new UltraMegaBitAccount());

        //RIP UploadBox
        //accounts.put("UploadBox.com",new UploadBoxAccount());
        accounts.put("UpaFile.com", new UpaFileAccount());
        //UpBooth.com is dead
        //accounts.put("UpBooth.com", new UpBoothAccount());
        accounts.put("Updown.bz", new UpdownBzAccount());
        accounts.put("UploadAble.ch", new UploadAbleAccount());
        accounts.put("UploadBoy.com", new UploadBoyAccount());
        accounts.put("UploadDrive.com", new UploadDriveAccount());
        accounts.put("UploadHero.co", new UploadHeroAccount());
        accounts.put("Uploaded.net", new UploadedDotToAccount());
        accounts.put("Uploading.com", new UploadingDotComAccount());
        accounts.put("UploadRocket.net", new UploadRocketAccount());

        //Uploadiz is <a href="https://sourceforge.net/p/neembuuuploader/tickets/168/">down</a>
        //accounts.put("Uploadiz.com", new UploadizAccount());

        accounts.put("UppIT.com", new UppItAccount());
        accounts.put("UpShared.com", new UpSharedAccount());
        accounts.put("UpStore.net", new UpStoreAccount());
        accounts.put("Uptobox.com", new UptoboxAccount());
        accounts.put("Up07.net", new UpZeroSevenAccount());
        accounts.put("UseFile.com", new UseFileAccount());
        accounts.put("Verzend.be", new VerzendAccount());
        accounts.put("VidBull.com", new VidBullAccount());
        accounts.put("VideoMega.tv", new VideoMegaAccount());
        accounts.put("VideoWood.tv", new VideoWoodAccount());
        accounts.put("VidTo.me", new VidToAccount());
        accounts.put("VidUp.me", new VidUpAccount());
        accounts.put("VidXden.com", new VidXdenAccount());
        accounts.put("VidZi.tv", new VidZiAccount());
        accounts.put("Vip-File.com", new VipFileAccount());
        accounts.put("VodLocker.com", new VodLockerAccount());
        accounts.put("VozUpload.com", new VozUploadAccount());
        accounts.put("VShare.eu", new VShareAccount());
        //accounts.put("Vreer.com", new VReerAccount());
        //Offline http://www.isitdownrightnow.com/wupload.com.html
        //accounts.put("Wupload.com", new WuploadAccount());
        accounts.put("Xerver.co", new XerverAccount());
        accounts.put("XfileLoad.com", new XfileLoadAccount());
        accounts.put("XvidStage.com", new XvidStageAccount());
        accounts.put("YouTube.com", new YouTubeAccount());
        accounts.put("YouWatch.org", new YouWatchAccount());
        accounts.put("YourVideoHost.com", new YourVideoHostAccount());
        accounts.put("Zippyshare.com", new ZippyShareAccount());
        
        //ZShare is down
        //accounts.put("ZShare.ma", new ZShareAccount());
        accounts.put("ZohoDocs.com", new ZohoDocsAccount());
        //Then update the rows with values from the .nuproperties files.
        updateRows();

        //Pack the frame
        //    pack();
        pack();

        //Set the window relative to NU
        setLocationRelativeTo(NeembuuUploader.getInstance());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new javax.swing.JLabel();
        infoLabel2 = new javax.swing.JLabel();
        infoLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        accountsTable = new javax.swing.JTable();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Accounts Manager");
        setMinimumSize(new java.awt.Dimension(700, 450));

        infoLabel.setFont(infoLabel.getFont().deriveFont(infoLabel.getFont().getStyle() | java.awt.Font.BOLD));
        infoLabel.setText("Enter your account details for the appropriate hosts..");

        infoLabel2.setText("If you don't have an account or if you want to disable an account or if a site has temporary login problems, leave both the fields blank and save..");

        infoLabel3.setFont(infoLabel3.getFont().deriveFont((infoLabel3.getFont().getStyle() | java.awt.Font.ITALIC), infoLabel3.getFont().getSize()-1));
        infoLabel3.setText("Get a free account from the appropriate sites if you don't have one so you can manage files on the cloud..");

        accountsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Host name", "Username", "Password"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        accountsTable.setColumnSelectionAllowed(true);
        accountsTable.setRowHeight((int)usernamerenderer.getPreferredSize().getHeight());
        accountsTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(accountsTable);

        saveButton.setText("Save & Close");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(infoLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
                    .addComponent(infoLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveButton)
                    .addComponent(infoLabel3, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(infoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(infoLabel2)
                .addGap(18, 18, 18)
                .addComponent(infoLabel3)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(saveButton)
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        //The user may type into the field and while the cursor is still inside, 
        //he may click save button.
        //It will not set the value into that field and will cause error.
        //So when the user clicks save button, we have to stop that cellediting process
        //and set the value to the field.

        //Get the currently edited cell's celleditor
        TableCellEditor cellEditor = accountsTable.getCellEditor();

        //Call stopCellEditing() to stop the editing process
        //But if the selected cell is in first column which is non editable, then
        //calling stopCellEditing will throw nullpointer exception because there's
        //no editor there.. So check for null, before calling stopCellEditing().
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        //Iterate through each row..
        int row = 0;
        for (Account account : accounts.values()) {
            //Declare local variables to store the username and password
            //If none present, empty "" is stored.
            String username = accountsTable.getValueAt(row, USERNAME).toString();
            String password = accountsTable.getValueAt(row, PASSWORD).toString();

            //The username and password field must be both filled or both empty
            //Only one field should not be filled.
            if (username.isEmpty() ^ password.isEmpty()) {
                NULogger.getLogger().info("The username and password field must be both filled or both empty");
                JOptionPane.showMessageDialog(this,
                        account.getHOSTNAME() + " " + TranslationProvider.get("neembuuuploader.accountgui.AccountsManager.dialogerror"),
                        account.getHOSTNAME(),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            //Username and Password (encrypted) must be stored in the .nuproperties file in the user's home folder.
            NULogger.getLogger().info("Setting username and password(encrypted) to the .nuproperties file in user home folder.");
            NeembuuUploaderProperties.setProperty(account.getKeyUsername(), username);
            NeembuuUploaderProperties.setEncryptedProperty(account.getKeyPassword(), password);

            row++;
        }

        //Separate thread to start the login process
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    loginEnabledAccounts();
                } catch (Exception ex) {
                    System.err.println("Exception while logging in.." + ex);
                    NULogger.getLogger().severe(ex.toString());
                }
            }
        });

        //Disposing the window
        NULogger.getLogger().info("Closing Accounts Manager..");
        dispose();
    }//GEN-LAST:event_saveButtonActionPerformed

    /**
     * Private method to update rows
     */
    private void updateRows() {
        //Iterate through each account
        for (Account account : accounts.values()) {
            //Get the values and update the rows.
            model.addRow(new Object[]{
                account.getHOSTNAME(),
                account.getUsername(),
                account.getPassword()
            });
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AccountsManager.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AccountsManager dialog = new AccountsManager(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable accountsTable;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel infoLabel2;
    private javax.swing.JLabel infoLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
