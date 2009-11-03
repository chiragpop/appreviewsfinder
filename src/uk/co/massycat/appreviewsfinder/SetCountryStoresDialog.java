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
 * SetCountryStoresDialog.java
 *
 * Created on Aug 31, 2009, 8:36:35 PM
 */
package uk.co.massycat.appreviewsfinder;

import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author ben
 */
public class SetCountryStoresDialog extends javax.swing.JDialog {

    static private final int COLUMNS = 7;
    private JCheckBox[] mCountryCheckBoxes;
    private String[] mCountries;
    private boolean mCancelled = true;

    private void countryCheckBoxChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            boolean at_least_one = false;

            for (int i = 0; i < mCountryCheckBoxes.length; i++) {
                if (mCountryCheckBoxes[i].isSelected()) {
                    at_least_one = true;
                    break;
                }
            }

            if (at_least_one) {
                mOkButton.setEnabled(true);
            } else {
                mOkButton.setEnabled(false);
            }
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
            mOkButton.setEnabled(true);
        }

    }

    public boolean wasCancelled() {
        return mCancelled;
    }

    /** Creates new form SetCountryStoresDialog */
    public SetCountryStoresDialog(java.awt.Frame parent, boolean modal, Set<String> start_values) {
        super(parent, modal);
        initComponents();

        CountriesManager manager = CountriesManager.getManager();

        mCountries = (String[]) manager.getAllCountryCodes().toArray(new String[0]);
        mCountryCheckBoxes = new JCheckBox[mCountries.length];

        int rows = (mCountries.length + COLUMNS - 1) / COLUMNS;

        GridLayout layout = new GridLayout(rows, COLUMNS);
        mCountriesPanel.setLayout(layout);
        Dimension size = null;
        int country_num = 0;
        ItemListener listener = new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                countryCheckBoxChanged(e);
            }
        };

        for (int i = 0; i < mCountries.length; i++) {
            String country = mCountries[i];
            JCheckBox checkbox = new JCheckBox();
            JPanel panel = new JPanel(new BorderLayout());
            checkbox.addItemListener(listener);
            JLabel label = new JLabel(country, manager.getFlagIconForCountry(country), SwingConstants.LEFT);
            panel.add(checkbox, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
            mCountriesPanel.add(panel);

            size = panel.getPreferredSize();
            size.width += 20;
            mCountryCheckBoxes[country_num++] = checkbox;

            if ( start_values.contains(country)) {
                checkbox.setSelected(true);
            }
        }

        size.height = size.height * rows;
        size.width = size.width * COLUMNS;
        mCountriesPanel.setPreferredSize(size);
        mCountriesPanel.setMinimumSize(size);
        Dimension dialog_size = this.getSize();
        if (dialog_size.width < size.width) {
            dialog_size.width = size.width;
        }

        dialog_size.height += size.height;
        this.setSize(dialog_size);
    }

    public Set<String> getCountries() {
        Set<String> countries = new HashSet<String>();

        if ( !mCancelled) {
            for ( int i = 0; i < mCountryCheckBoxes.length; i++) {
                if ( mCountryCheckBoxes[i].isSelected()) {
                    countries.add(mCountries[i]);
                }
            }
        }

        return countries;
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
        mCountriesPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        mDeselectButton = new javax.swing.JButton();
        mSelectButton = new javax.swing.JButton();
        mInvertButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        mCanelButton = new javax.swing.JButton();
        mOkButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Set Search Countries");

        jPanel1.setLayout(new java.awt.BorderLayout());

        mCountriesPanel.setLayout(new java.awt.GridLayout(7, 7));
        jPanel1.add(mCountriesPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        mDeselectButton.setText("Deselect All");
        mDeselectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mDeselectButtonActionPerformed(evt);
            }
        });
        jPanel5.add(mDeselectButton);

        mSelectButton.setText("Select All");
        mSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSelectButtonActionPerformed(evt);
            }
        });
        jPanel5.add(mSelectButton);

        mInvertButton.setText("Invert Selection");
        mInvertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mInvertButtonActionPerformed(evt);
            }
        });
        jPanel5.add(mInvertButton);

        jPanel3.add(jPanel5, java.awt.BorderLayout.CENTER);

        mCanelButton.setText("Cancel");
        mCanelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mCanelButtonActionPerformed(evt);
            }
        });
        jPanel4.add(mCanelButton);

        mOkButton.setText("Ok");
        mOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mOkButtonActionPerformed(evt);
            }
        });
        jPanel4.add(mOkButton);

        jPanel3.add(jPanel4, java.awt.BorderLayout.PAGE_END);

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mDeselectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mDeselectButtonActionPerformed
        int country_count = mCountries.length;

        for (int i = 0; i < country_count; i++) {
            mCountryCheckBoxes[i].setSelected(false);
        }
    }//GEN-LAST:event_mDeselectButtonActionPerformed

    private void mSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSelectButtonActionPerformed
        int country_count = mCountries.length;

        for (int i = 0; i < country_count; i++) {
            mCountryCheckBoxes[i].setSelected(true);
        }
    }//GEN-LAST:event_mSelectButtonActionPerformed

    private void mCanelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mCanelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_mCanelButtonActionPerformed

    private void mOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mOkButtonActionPerformed
        mCancelled = false;
                setVisible(false);
        dispose();
    }//GEN-LAST:event_mOkButtonActionPerformed

    private void mInvertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mInvertButtonActionPerformed
                int country_count = mCountries.length;

        for (int i = 0; i < country_count; i++) {
            mCountryCheckBoxes[i].setSelected(!mCountryCheckBoxes[i].isSelected());
        }
    }//GEN-LAST:event_mInvertButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                SetCountryStoresDialog dialog = new SetCountryStoresDialog(new javax.swing.JFrame(), true, new HashSet<String>());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton mCanelButton;
    private javax.swing.JPanel mCountriesPanel;
    private javax.swing.JButton mDeselectButton;
    private javax.swing.JButton mInvertButton;
    private javax.swing.JButton mOkButton;
    private javax.swing.JButton mSelectButton;
    // End of variables declaration//GEN-END:variables
}
