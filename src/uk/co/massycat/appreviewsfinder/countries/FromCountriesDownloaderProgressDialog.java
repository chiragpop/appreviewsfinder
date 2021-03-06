//
// Copyright (C) 2009 Ben Jaques.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this
//   list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// - Neither the name of the author nor the names of its contributors may be used
//   to endorse or promote products derived from this software without specific
//   prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/*
 * ReviewsDownloadProgressDialog.java
 *
 * Created on Sep 1, 2009, 2:02:45 PM
 */

package uk.co.massycat.appreviewsfinder.countries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.Timer;

/**
 *
 * @author ben
 */
public class FromCountriesDownloaderProgressDialog extends javax.swing.JDialog {
    private FromCountriesDownloader mDownloader;
    private Timer mUpdateTimer;

    private void setUpLabel() {
        String country = mDownloader.getCurrentCountry();
        Icon flag = CountriesManager.getManager().getFlagIconForCountry(country);

        mDownloadLabel.setIcon(flag);

        mDownloadLabel.setText("Total downloaded: " + mDownloader.getCurrentTotal());
    }

    private void updateProgress() {
        mDownloadProgressBar.setValue(mDownloader.getCurrentCountryNumber());

        setUpLabel();

        if ( !mDownloader.isAlive()) {
            setVisible(false);
            dispose();
        }
    }
    

    /** Creates new form ReviewsDownloadProgressDialog */
    public FromCountriesDownloaderProgressDialog(java.awt.Frame parent, boolean modal, FromCountriesDownloader downloader) {
        super(parent, modal);
        initComponents();

        mDownloader = downloader;

        mDownloadProgressBar.setMaximum(mDownloader.getCountriesCount());
        mDownloadProgressBar.setValue(0);
        setUpLabel();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        mDownloadLabel = new javax.swing.JLabel();
        mDownloadProgressBar = new javax.swing.JProgressBar();
        jPanel3 = new javax.swing.JPanel();
        mCancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reviews downloading...");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        mDownloadLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mDownloadLabel.setText("jLabel1");
        jPanel2.add(mDownloadLabel, java.awt.BorderLayout.NORTH);

        mDownloadProgressBar.setPreferredSize(new java.awt.Dimension(250, 20));
        jPanel2.add(mDownloadProgressBar, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel2);

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        mCancelButton.setText("Cancel");
        mCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCancelButtonActionPerformed(evt);
            }
        });
        jPanel3.add(mCancelButton);

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_mCancelButtonActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        mUpdateTimer.stop();
    }//GEN-LAST:event_formWindowClosed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        mUpdateTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed( ActionEvent evt) {
                updateProgress();
            }
        });
        mUpdateTimer.start();
    }//GEN-LAST:event_formWindowOpened

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton mCancelButton;
    private javax.swing.JLabel mDownloadLabel;
    private javax.swing.JProgressBar mDownloadProgressBar;
    // End of variables declaration//GEN-END:variables

}
