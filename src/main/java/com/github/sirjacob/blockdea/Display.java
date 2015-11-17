/*
 * The MIT License
 *
 * Copyright 2015 Sir Jacob.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.sirjacob.blockdea;

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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author https://github.com/SirJacob
 * @version 1.1 Changes:
 * <p>
 * 1.1: Added license to code, changed package name, added version tag, added
 * changes, added comments/Javadoc, made getHiddenKey() deprecated, tweaked code
 * to make it more efficient. (2015-11-16)
 * <p>
 * 1.0: First release. (2015-11-05)
 */
public class Display extends javax.swing.JFrame {

    /**
     * Creates new form Display
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Display() {
        initComponents();
        ptxtKey.setEchoChar('*'); //Set the char to hide the API key with
        setLocationRelativeTo(null); //Center window
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
        } catch (IOException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
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
     * Gets the status of the API key.
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
        //--------------------//
        String rs = (String) jo.get("request_status");
        String aks = (String) jo.get("apikeystatus");
        String st = (String) jo.get("servertime");
        String v = (String) jo.get("version");
        String temp_credits = (String) jo.get("credits");
        int credits = 0;
        if (temp_credits != null) {
            credits = Integer.parseInt(temp_credits);
        }
        String ct = (String) jo.get("credits_time");
        String ccs = (String) jo.get("commercial_credit_status");
        String temp_commercial_credit_status_percent = (String) jo.get("commercial_credit_status_percent");
        double ccsp = 0;
        if (temp_commercial_credit_status_percent != null) {
            ccsp = Double.parseDouble(temp_commercial_credit_status_percent);
        }
        //--------------------//
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
    @SuppressWarnings("UnusedAssignment")
    private void statusToList(String requestStatus, String apiKeyStatus, String serverTime, String version, int credits, String creditsTime, String commercialCreditStatus, double commercialCreditPercent) {
        String customMsg = "";
        String header1 = "═════ Status Request ═════";
        String header2 = "══════════════════";
        int lineCount = 0;
        list.add(" ", lineCount++);
        list.add(header2, lineCount++);
        list.add(header1, lineCount++);
        list.add(header2, lineCount++);
        list.add("Checking status on key... " + "(" + getKeyEnding() + ")", lineCount++);
        list.add("Request Status: " + requestStatus.toUpperCase() + ", Version: " + version + ", Time: " + getTime(), lineCount++);
        switch (apiKeyStatus) {
            case "fail":
                customMsg = "Invalid Key";
                break;
            case "inactive":
                customMsg = "Unactivated Key (Deactivated due to abuse?)";
                break;
            case "active":
                customMsg = "Key OK";
                break;
        }
        list.add("Key Status: " + apiKeyStatus.toUpperCase() + " :: " + customMsg, lineCount++);
        customMsg = "";
        list.add("Credits Remaining: " + credits + " :: (Updated every 180 minutes)", lineCount++);
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
            if (commercialCreditPercent == 1) {
                customMsg = "100%";
            } else {
                String strPercent = String.valueOf(commercialCreditPercent);
                strPercent = strPercent.substring(2, strPercent.length());
                customMsg = strPercent + "%";
            }
            customMsg = "";
        }
        list.add(" ", lineCount++);
    }

    /**
     * Checks the supplied domain (getDomain()) and adds its status to the list.
     * Possible statuses: OK, BLOCK, or FAILURE.
     */
    private void checkDomain() {
        String response = getURLData("http://check.block-disposable-email.com/easyapi/txt/" + getKey() + "/" + getDomain());
        switch (response) {
            case "ok":
                list.add(getTime() + " | OK: " + getDomain(), 0);
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
            checkDomain();
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
        setTitle("Block DEA Client by Sir Jacob");

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
        checkDomain();
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
