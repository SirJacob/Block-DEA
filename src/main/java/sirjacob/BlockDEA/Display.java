/*
 * Copyright 2015-Present Cory Ugone (A.K.A. Sir Jacob)
 */
package sirjacob.BlockDEA;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Cory Ugone (A.K.A. Sir Jacob) <https://github.com/SirJacob>
 * @version 1.3 Changelog:
 * <p>
 * 1.3: Fixed parsing of the commercial credit status percent, key check now
 * prints server time over local time, credits remaining now shows the server
 * time at which your credit balance was recalculated, calculations for
 * commercial credit percent have been removed (never shown in GUI anyway),
 * added more error handling, added {@link #preformCheckDomain()} to replace
 * {@link #checkDomain()} (now deprecated). (01/22/2016)
 * <p>
 * 1.2: Updated pom.xml to enable creation of jar files. (12/17/2015)
 * <p>
 * 1.1: Added license to code, changed package name, added version tag, added
 * changelog, added comments/Javadoc, made {@link #getHiddenKey()} deprecated,
 * tweaked code to make it more efficient, added
 * {@link #checkStatusAPIVersion(double)} along with
 * {@link #SUPPORTED_STATUS_API_VERSION}, added {@link #showBadKeyError()},
 * added {@link #DEFAULT_TITLE} and {@link #appendTitle(String)}, added import
 * for JOptionPane, tweaked printing to GUI list in
 * {@link #statusToList(String, String, String, double, int, String, String,double)}.
 * (11/16/2015)
 * <p>
 * 1.0: First release. (11/05/2015)
 */
public class Display extends javax.swing.JFrame {

    /**
     * Latest tested version of Status API that is supported.
     */
    private static final double SUPPORTED_STATUS_API_VERSION = 1.3;
    /**
     * Default title for the GUI window.
     */
    private static final String DEFAULT_TITLE = "Block DEA Client by Sir Jacob";

    /**
     * Creates new form Display
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Display() {
        initComponents();
        ptxtKey.setEchoChar('*'); //Hide the API key with this char.
        setLocationRelativeTo(null); //Center the window.
        setTitle(DEFAULT_TITLE); //Set the default window title.
    }

    /**
     * Displays a JOptionPane that informs the user that there was a problem
     * with their API key.
     *
     * @since 1.1
     */
    private void showBadKeyError() {
        JOptionPane.showMessageDialog(this,
                "It appears that your key is invalid, please check your API key and try again.",
                "API Key Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Checks if the Status API version is the same as the
     * {@link #SUPPORTED_STATUS_API_VERSION}.
     *
     * @since 1.1
     * @param v Accepts current Status API version as double v.
     */
    private void checkStatusAPIVersion(double v) {
        if (v != SUPPORTED_STATUS_API_VERSION) {
            System.out.println("[WARNING] This program not been tested with the Status API version you are using!");
            System.out.println("[WARNING] Current Status API Version: " + v);
            System.out.println("[WARNING] Supported Status API Version: " + SUPPORTED_STATUS_API_VERSION);
            appendTitle("Untested Status API Version!");
        }
    }

    /**
     * @since 1.1
     * @param title Accepts text to append to the end of the window's title.
     */
    private void appendTitle(String title) {
        setTitle(DEFAULT_TITLE + " | " + title);
    }

    /**
     * @param str Accepts URL (as a string)
     * @return Returns the first line of the website.
     */
    private String getURLData(String str) {
        try {
            URL url = new URL(str);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            return bufferedReader.readLine();
        } catch (NullPointerException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Please be sure to remove any protocals/ports from the domain you are checking. Ex: Don't include http(s)://", "Domain Error", JOptionPane.ERROR_MESSAGE);
            //Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return Replaces all but the last 4 numbers/letters of the key with x.
     * @deprecated Method getHiddenKey() is not used but should still work.
     */
    private String getHiddenKey() {
        String key = getKey();
        String hiddenKey = "";
        for (int i = 0; i < key.length() - 4; i++) {
            hiddenKey += "x";
        }
        return hiddenKey + key.substring(key.length() - 4);
    }

    /**
     *
     * @return Returns the last 4 letters/numbers of the API key.
     */
    private String getKeyEnding() {
        return getKey().substring(getKey().length() - 4);
    }

    /**
     * Gets the status of the API key. Preformed when Key (Checkmark) is pressed
     * on the GUI.
     */
    @SuppressWarnings("UnusedAssignment")
    private void checkStatus() {
        Object obj = null;
        String response = getURLData("http://status.block-disposable-email.com/status/?apikey=" + getKey());
        JSONParser jp = new JSONParser();
        try {
            obj = jp.parse(response);
        } catch (ParseException ex) {
            System.out.println("ParseException: " + ex);
            return;
        }
        JSONObject jo = (JSONObject) obj;
        String rs = (String) jo.get("request_status");
        String aks = (String) jo.get("apikeystatus");
        /**
         * Gets and checks the Status API version.
         */
        double v = Double.parseDouble((String) jo.get("version"));
        checkStatusAPIVersion(v);
        /**
         * If the request status or the API key status are not ok then there is
         * no reason to continue to attempt to get values from the JSON object.
         */
        if (!"ok".equals(rs) || !"active".equals(aks)) {
            showBadKeyError();
            return;
        }
        String st = (String) jo.get("servertime");
        String temp_credits = (String) jo.get("credits");
        int credits = 0;
        if (temp_credits != null) {
            credits = Integer.parseInt(temp_credits);
        }
        String ct = (String) jo.get("credits_time");
        String ccs = (String) jo.get("commercial_credit_status");
        Object temp_commercial_credit_status_percent = jo.get("commercial_credit_status_percent");
        double ccsp = 0;
        if (temp_commercial_credit_status_percent != null) {
            ccsp = Double.valueOf(String.valueOf(temp_commercial_credit_status_percent));
        }
        statusToList(rs, aks, st, v, credits, ct, ccs, ccsp);
    }

    /**
     * Takes args from checkStatus() and displays the values on the GUI list.
     * Parameter descriptions provided by BDEA.
     *
     * @param requestStatus Describes if the server succeeded or found any
     * problems.
     * @param apiKeyStatus The simple answer if you should block or accept a
     * domain.
     * @param serverTime The local time of the server in the moment of your
     * request. (Not represented)
     * @param version The current version of the service.
     * @param credits The number of remaining credits.
     * @param creditsTime This timestamp shows you when the credits of your
     * account have been recalculated. (Not represented)
     * @param commercialCreditStatus This attribute indicates the status of
     * remaining credits.
     * @param commercialCreditPercent Remaining credits in percent.
     */
    @SuppressWarnings({"UnusedAssignment", "ValueOfIncrementOrDecrementUsed"})
    private void statusToList(String requestStatus, String apiKeyStatus, String serverTime, double version, int credits, String creditsTime, String commercialCreditStatus, double commercialCreditPercent) {
        String customMsg = "";
        String header1 = "~~~~~ Key Check ~~~~~";
        int lineCount = 0;
        list.add(header1, lineCount++);
        list.add("Checking status on key... " + "(" + getKeyEnding() + ")", lineCount++);
        list.add("Request Status: " + requestStatus.toUpperCase() + ", Version: " + version + ", Server Time: " + serverTime, lineCount++);
        list.add("Key Status: " + apiKeyStatus.toUpperCase(), lineCount++);
        customMsg = "";
        list.add("Credits Remaining: " + credits + " :: (Last Updated in Server Time: " + creditsTime + ")", lineCount++);
        if (commercialCreditStatus != null) {
            switch (commercialCreditStatus) {
                case "good":
                    customMsg = "Credits are higher than 20% of your last purchase";
                    break;
                case "low":
                    customMsg = "Remaining credits are lower than 20% of your last purchase";
                    break;
                case "exhausted":
                    customMsg = "You have exhausted your supply of credits!";
                    break;
            }
            list.add("Credit Status: " + commercialCreditStatus.toUpperCase() + " :: " + customMsg, lineCount++);
            customMsg = "";
            /*            if (commercialCreditPercent == 1) {
            customMsg = "100%";
            } else {
            String strPercent = String.valueOf(commercialCreditPercent);
            strPercent = strPercent.substring(2, strPercent.length());
            customMsg = strPercent + "%";
            System.out.println(customMsg);
            }
            customMsg = "";*/
        }
        list.add(null, lineCount++);
    }

    /**
     * Queries BDEA's EasyAPI (Simple Text Output Method) Checks the supplied
     * domain (getDomain()) and adds its status to the list. Possible Responses:
     * ok, block, fail_key, fail_server, fail_input_domain,
     * fail_parameter_count, fail_key_low_credits (EasyAPI v0.2)
     *
     * @deprecated checkDomain does not validate that the API key is
     * Alphanumeric. See {@link #preformCheckDomain()}.
     */
    private void checkDomain() {
        String response = getURLData("http://check.block-disposable-email.com/easyapi/txt/" + getKey() + "/" + getDomain());
        switch (response) {
            case "ok":
                list.add(getTime() + " | ALLOW: " + getDomain(), 0);
                break;
            case "block":
                list.add(getTime() + " | BLOCK: " + getDomain(), 0);
                break;
            default:
                list.add(getTime() + " | FAILURE: " + response, 0);
                break;
        }
    }

    /**
     *
     * @return Returns the current Epoch time.
     */
    private String getTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    /**
     *
     * @return Returns the supplied domain from the GUI text input (txtDomain).
     */
    private String getDomain() {
        return txtDomain.getText();
    }

    /**
     * Converts password text input into string and returns.
     *
     * @return Returns the supplied key from the GUI password text input
     * (ptxtKey).
     */
    private String getKey() {
        String key = "";
        for (char x : ptxtKey.getPassword()) {
            key += x;
        }
        return key;
    }

    /**
     * Updates the GUI text input, txtDomain.
     *
     * @param domain Accepts new domain.
     */
    private void setDomain(String domain) {
        txtDomain.setText(domain);
    }

    /**
     * Opens web browser and navigates to supplied URL.
     *
     * @param url Accepts URL as string.
     */
    private void openURL(String url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Regulates what GUI buttons can be pressed/when they can be pressed.
     */
    private void enableBtn() {
        if ((!"".equals(getKey())) && !"".equals(getDomain())) {
            btnQuery.setEnabled(true);
        } else {
            btnQuery.setEnabled(false);
        }

        if (!"".equals(getKey())) {
            btnTestBlock.setEnabled(true);
            btnTestOk.setEnabled(true);
            btnCheckStatus.setEnabled(true);
        } else {
            btnTestBlock.setEnabled(false);
            btnTestOk.setEnabled(false);
            btnCheckStatus.setEnabled(false);
        }
    }

    /**
     * Preforms a free test check.
     * <p>
     * ok.bdea.cc returns OK
     * <p>
     * block.bdea.cc returns BLOCK
     *
     * @param testingDomain Accepts ok.bdea.cc or block.bdea.cc
     */
    private void testBtnPress(String testingDomain) {
        if ("ok.bdea.cc".equals(testingDomain) || "block.bdea.cc".equals(testingDomain)) {
            setDomain(testingDomain);
            enableBtn();
            preformCheckDomain();
        }
    }

    /**
     * Queries BDEA's EasyAPI (Simple Text Output Method) Checks the supplied
     * domain (getDomain()) and adds its status to the list. Possible Responses:
     * ok, block, fail_key, fail_server, fail_input_domain,
     * fail_parameter_count, fail_key_low_credits (EasyAPI v0.2)
     * <p>
     * preformCheckDomain also validates that the API key is Alphanumeric.
     */
    private void preformCheckDomain() {
        if (StringUtils.isAlphanumeric(getKey())) {
            String response = getURLData("http://check.block-disposable-email.com/easyapi/txt/" + getKey() + "/" + getDomain());
            if (response == null) {
                return;
            }
            switch (response) {
                case "ok":
                    list.add(getTime() + " | ALLOW: " + getDomain(), 0);
                    break;
                case "block":
                    list.add(getTime() + " | BLOCK: " + getDomain(), 0);
                    break;
                default:
                    list.add(getTime() + " | FAILURE: " + response, 0);
                    break;
            }
        } else {
            showBadKeyError();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        btnQuery = new javax.swing.JButton();
        btnClrList = new javax.swing.JButton();
        btnTestOk = new javax.swing.JButton();
        btnTestBlock = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtDomain = new javax.swing.JTextField();
        ptxtKey = new javax.swing.JPasswordField();
        jSeparator1 = new javax.swing.JSeparator();
        list = new java.awt.List();
        btnCheckStatus = new javax.swing.JButton();
        tBtnShowKey = new javax.swing.JToggleButton();
        lblManageAccount = new javax.swing.JLabel();
        lblPersonalStats = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Domain:");

        btnQuery.setText("Query");
        btnQuery.setToolTipText("Preform a query (Uses credits)");
        btnQuery.setEnabled(false);
        btnQuery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQueryActionPerformed(evt);
            }
        });

        btnClrList.setText("Clear List");
        btnClrList.setToolTipText("Clear the list above");
        btnClrList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClrListActionPerformed(evt);
            }
        });

        btnTestOk.setText("Test Ok");
        btnTestOk.setToolTipText("Preform a FREE query that always return OK");
        btnTestOk.setEnabled(false);
        btnTestOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestOkActionPerformed(evt);
            }
        });

        btnTestBlock.setText("Test Block");
        btnTestBlock.setToolTipText("Preform a FREE query that always return BLOCK");
        btnTestBlock.setEnabled(false);
        btnTestBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTestBlockActionPerformed(evt);
            }
        });

        jLabel2.setText("API Key:");

        txtDomain.setToolTipText("Enter the domain you would like to query.");
        txtDomain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDomainKeyReleased(evt);
            }
        });

        ptxtKey.setToolTipText("Enter your API key.");
        ptxtKey.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                ptxtKeyKeyReleased(evt);
            }
        });

        list.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N

        btnCheckStatus.setText("Key ✔");
        btnCheckStatus.setToolTipText("Check status on given API Key.");
        btnCheckStatus.setEnabled(false);
        btnCheckStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckStatusActionPerformed(evt);
            }
        });

        tBtnShowKey.setText("Show Key");
        tBtnShowKey.setToolTipText("(Toggle) Show/Hide API Key");
        tBtnShowKey.setHideActionText(true);
        tBtnShowKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tBtnShowKeyActionPerformed(evt);
            }
        });

        lblManageAccount.setForeground(java.awt.Color.blue);
        lblManageAccount.setText("<html><u>Manage Account</u></html>");
        lblManageAccount.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblManageAccountMousePressed(evt);
            }
        });

        lblPersonalStats.setForeground(java.awt.Color.blue);
        lblPersonalStats.setText("<html><u>Personal Statistics</u></html>");
        lblPersonalStats.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                lblPersonalStatsMousePressed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(list, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(7, 7, 7)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtDomain, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                            .addComponent(ptxtKey))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnCheckStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnQuery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnTestOk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(tBtnShowKey, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnTestBlock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnClrList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblManageAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblPersonalStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblManageAccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPersonalStats, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addComponent(list, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDomain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnTestOk)
                        .addComponent(jLabel1)
                        .addComponent(btnTestBlock)
                        .addComponent(btnQuery)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnClrList)
                        .addComponent(tBtnShowKey)
                        .addComponent(btnCheckStatus))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(ptxtKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     *
     * @param evt On button press clear the GUI list.
     */
    private void btnClrListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClrListActionPerformed
        list.removeAll();
    }//GEN-LAST:event_btnClrListActionPerformed

    private void txtDomainKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDomainKeyReleased
        enableBtn();
    }//GEN-LAST:event_txtDomainKeyReleased

    private void ptxtKeyKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ptxtKeyKeyReleased
        enableBtn();
    }//GEN-LAST:event_ptxtKeyKeyReleased

    private void btnTestOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestOkActionPerformed
        testBtnPress("ok.bdea.cc");
    }//GEN-LAST:event_btnTestOkActionPerformed

    private void btnTestBlockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestBlockActionPerformed
        testBtnPress("block.bdea.cc");
    }//GEN-LAST:event_btnTestBlockActionPerformed

    private void btnQueryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQueryActionPerformed
        preformCheckDomain();
    }//GEN-LAST:event_btnQueryActionPerformed

    private void btnCheckStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckStatusActionPerformed
        checkStatus();
    }//GEN-LAST:event_btnCheckStatusActionPerformed
    /**
     *
     * @param evt On button press mask/unmask the API key text.
     */
    private void tBtnShowKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tBtnShowKeyActionPerformed
        if (tBtnShowKey.isSelected()) {
            ptxtKey.setEchoChar((char) 0);
        } else {
            ptxtKey.setEchoChar('*');
        }
    }//GEN-LAST:event_tBtnShowKeyActionPerformed

    private void lblManageAccountMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblManageAccountMousePressed
        openURL("http://www.block-disposable-email.com/cms/manage/");
    }//GEN-LAST:event_lblManageAccountMousePressed

    private void lblPersonalStatsMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblPersonalStatsMousePressed
        openURL("http://www.block-disposable-email.com/cms/manage/your-personal-dea-statistics/");
    }//GEN-LAST:event_lblPersonalStatsMousePressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc="Look and feel setting code">
        /* If Windows look and feel is not available, stay with the default.
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) { //I like the Windows look and feel better than the Nimbus l&f.
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Display().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCheckStatus;
    private javax.swing.JButton btnClrList;
    private javax.swing.JButton btnQuery;
    private javax.swing.JButton btnTestBlock;
    private javax.swing.JButton btnTestOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblManageAccount;
    private javax.swing.JLabel lblPersonalStats;
    private java.awt.List list;
    private javax.swing.JPasswordField ptxtKey;
    private javax.swing.JToggleButton tBtnShowKey;
    private javax.swing.JTextField txtDomain;
    // End of variables declaration//GEN-END:variables
}
