/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package neembuuuploader.accounts;

import java.util.logging.Level;
import javax.swing.JOptionPane;
import neembuuuploader.HostsPanel;
import neembuuuploader.NeembuuUploader;
import neembuuuploader.TranslationProvider;
import neembuuuploader.accountgui.AccountsManager;
import neembuuuploader.interfaces.abstractimpl.AbstractAccount;
import neembuuuploader.uploaders.api._4shared.DesktopAppJax2;
import neembuuuploader.uploaders.api._4shared.DesktopAppJax2Service;
import neembuuuploader.utils.NULogger;

/**
 *
 * @author dinesh
 */
public class FourSharedAccount extends AbstractAccount {

    public DesktopAppJax2 da = null;

    public FourSharedAccount() {

        KEY_USERNAME = "4susername";
        KEY_PASSWORD = "4spassword";
        HOSTNAME = "4Shared.com";
    }

    @Override
    public void disableLogin() {
        loginsuccessful = false;
        //These code are necessary for account only sites.
        HostsPanel.getInstance().fourSharedCheckBox.setEnabled(false);
        HostsPanel.getInstance().fourSharedCheckBox.setSelected(false);
        NeembuuUploader.getInstance().updateSelectedHostsLabel();

        NULogger.getLogger().log(Level.INFO, "{0} account disabled", getHOSTNAME());

    }

    @Override
    public void login() {
        loginsuccessful = false;
        String loginRes = null;
        try {
            da = new DesktopAppJax2Service().getDesktopAppJax2Port();
            loginRes = da.login(getUsername(), getPassword());
            if (!loginRes.isEmpty()) {
                throw new Exception();
            } else {
                loginsuccessful = true;
                HostsPanel.getInstance().fourSharedCheckBox.setEnabled(true);
                username = getUsername();
                password = getPassword();
                NULogger.getLogger().info("4shared Login success :)");
            }

        } catch (Exception e) {
            NULogger.getLogger().log(Level.INFO, "4Shared Login failed: {0}", loginRes);
            loginsuccessful = false;
            username = "";
            password = "";
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html>" + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }
}
