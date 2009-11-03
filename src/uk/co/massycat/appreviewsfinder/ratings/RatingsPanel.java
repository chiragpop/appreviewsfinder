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
 * RatingsPanel.java
 *
 * Created on Sep 24, 2009, 9:37:41 PM
 */
package uk.co.massycat.appreviewsfinder.ratings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import uk.co.massycat.appreviewsfinder.apptree.entries.AppEntry;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;

/**
 *
 * @author ben
 */
public class RatingsPanel extends javax.swing.JPanel {

    private AppEntry mApp;
    private RatingPanel mOverallRatingPanel;
    private Icon[] mIcons;
    private DefaultListModel mModel;
    private CountriesManager mCountriesManager = CountriesManager.getManager();

    class CountryRatingsPair {

        public String mCountry;
        public RatingsData mRatings;

        public CountryRatingsPair(String country, RatingsData ratings) {
            mCountry = country;
            mRatings = ratings;
        }
    }

    class Renderer implements ListCellRenderer {

        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            RatingPanel panel = new RatingPanel();
            CountryRatingsPair pair = (CountryRatingsPair) value;

            panel.setValuesAndIcons(pair.mRatings, mIcons);
            JLabel title = panel.getMainLabel();

            title.setIcon(mCountriesManager.getFlagIconForCountry(pair.mCountry));

            String title_text = mCountriesManager.getNameForCountry(pair.mCountry);

            if ( pair.mRatings.mRetrieveDate != null) {
                title_text += ", retrieved on " + pair.mRatings.mRetrieveDate;
            }

            title.setText(title_text);

            return panel;
        }
    }

    public void setApp(AppEntry app) {
        mModel.removeAllElements();
        mApp = app;

        MainLabel.setText("Ratings for " + mApp.mName + " by " + mApp.mArtist);
        MainLabel.setIcon(mApp.mArt);

        if (mApp.mRatings == null) {
            mOverallRatingPanel.setValuesAndIcons(new RatingsData(new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, null), mIcons);
        } else {
            // Work out the total and average ratings
            int[] current_ratings = new int[5];
            int[] all_ratings = new int[5];

            Enumeration<String> countries = mApp.mRatings.keys();

            while (countries.hasMoreElements()) {
                String country = countries.nextElement();
                RatingsData pair = mApp.mRatings.get(country);

                CountryRatingsPair for_model = new CountryRatingsPair(country, pair);
                mModel.add(mModel.getSize(), for_model);

                for (int i = 0; i < pair.mCurrent.length; i++) {
                    current_ratings[i] += pair.mCurrent[i];
                    all_ratings[i] += pair.mAll[i];
                }
            }

            mOverallRatingPanel.setValuesAndIcons(new RatingsData(current_ratings, all_ratings, null), mIcons);
        }
    }

    /** Creates new form RatingsPanel */
    public RatingsPanel() {
        mIcons = new Icon[5];

        for (int i = 0; i < mIcons.length; i++) {
            String icon_resource = "/uk/co/massycat/appreviewsfinder/resources/stars/" +
                    (5 - i) + "stars_16.png";
            URL resource_url = getClass().getResource(icon_resource);
            mIcons[i] = new ImageIcon(resource_url);
        }

        initComponents();

        MainLabel.setText("Unknown app");
        mOverallRatingPanel = new RatingPanel();
        mOverallRatingPanel.getMainLabel().setText("Overall ratings");
        mOverallRatingPanel.setValuesAndIcons(new RatingsData(new int[]{0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0}, null), mIcons);

        mTopPanel.add(mOverallRatingPanel, BorderLayout.CENTER);

        mModel = new DefaultListModel();
        mRatingsList.setModel(mModel);

        mRatingsList.setCellRenderer(new Renderer());
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                JFrame frame = new javax.swing.JFrame();

                Container content = frame.getContentPane();
                content.add(new RatingsPanel());
                frame.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
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

        jScrollPane1 = new javax.swing.JScrollPane();
        mRatingsList = new javax.swing.JList();
        mTopPanel = new javax.swing.JPanel();
        MainLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mRatingsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(mRatingsList);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        mTopPanel.setLayout(new java.awt.BorderLayout());

        MainLabel.setText("jLabel1");
        mTopPanel.add(MainLabel, java.awt.BorderLayout.NORTH);

        add(mTopPanel, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel MainLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList mRatingsList;
    private javax.swing.JPanel mTopPanel;
    // End of variables declaration//GEN-END:variables
}
