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
 * RatingPanel.java
 *
 * Created on Sep 24, 2009, 9:47:19 PM
 */
package uk.co.massycat.appreviewsfinder.ratings;

import java.awt.Color;
import java.awt.Container;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;

/**
 *
 * @author ben
 */
public class RatingPanel extends javax.swing.JPanel {

    private RatingsBarView mCurrentBars;
    private RatingsBarView mAllBars;

    public void setValuesAndIcons(RatingsData ratings_pair, Icon[] icons) {
        mCurrentBars.setValuesAndIcons(ratings_pair.mCurrent, icons);
        mAllBars.setValuesAndIcons(ratings_pair.mAll, icons);
        int current_total = 0;
        int all_total = 0;

        int current_rating = 0;
        int all_rating = 0;

        for (int i = 0; i < ratings_pair.mCurrent.length; i++) {
            current_total += ratings_pair.mCurrent[i];
            all_total += ratings_pair.mAll[i];

            current_rating += ratings_pair.mCurrent[i] * (5 - i);
            all_rating += ratings_pair.mAll[i] * (5 - i);
        }

        String current_text = Integer.toString(current_total) + " ratings";
        String all_text = Integer.toString(all_total) + " ratings";

        if (current_total > 0) {
            current_text += ", average " + ((float) current_rating / (float) current_total);
        }
        if (all_total > 0) {
            all_text += ", average " + ((float) all_rating / (float) all_total);
        }

        mCurrentLabel.setText(current_text);
        mAllLabel.setText(all_text);
    }

    public JLabel getMainLabel() {
        return mCountryLabel;
    }

    /** Creates new form RatingPanel */
    public RatingPanel() {
        initComponents();

        mCurrentBars = new RatingsBarView();
        mAllBars = new RatingsBarView();


        mBarsPanel.add(mCurrentBars);
        mBarsPanel.add(mAllBars);
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = new javax.swing.JFrame();
                RatingPanel rating = new RatingPanel();

                Container content = frame.getContentPane();
                content.add(rating);
                frame.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });

                RatingsData ratings_pair = new RatingsData(new int[]{1, 2, 3, 4, 5},
                        new int[]{9, 8, 7, 6, 5}, null);


                Icon[] icons = new Icon[5];

                CountriesManager countries = CountriesManager.getManager();
                rating.getMainLabel().setIcon(countries.getFlagIconForCountry("gb"));
                rating.getMainLabel().setText(countries.getNameForCountry("gb"));

                for (int i = 0; i < icons.length; i++) {
                    String icon_resource = "/uk/co/massycat/appreviewsfinder/resources/stars/" +
                            (5 - i) + "stars_16.png";
                    URL resource_url = getClass().getResource(icon_resource);
                    icons[i] = new ImageIcon(resource_url);
                }
                rating.setValuesAndIcons(ratings_pair, icons);

                frame.pack();

                frame.setVisible(true);
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        mBarsPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        mCountryLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        mCurrentLabel = new javax.swing.JLabel();
        mAllLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();

        setLayout(new java.awt.BorderLayout());

        mBarsPanel.setLayout(new java.awt.GridLayout(1, 2));
        add(mBarsPanel, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        mCountryLabel.setText("jLabel1");
        jPanel1.add(mCountryLabel, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(2, 2));

        jLabel1.setText("Current version");
        jPanel2.add(jLabel1);

        jLabel2.setText("All versions");
        jPanel2.add(jLabel2);

        mCurrentLabel.setText("jLabel1");
        jPanel2.add(mCurrentLabel);

        mAllLabel.setText("jLabel2");
        jPanel2.add(mAllLabel);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        add(jPanel1, java.awt.BorderLayout.NORTH);
        add(jSeparator2, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel mAllLabel;
    private javax.swing.JPanel mBarsPanel;
    private javax.swing.JLabel mCountryLabel;
    private javax.swing.JLabel mCurrentLabel;
    // End of variables declaration//GEN-END:variables
}
