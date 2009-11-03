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
package uk.co.massycat.appreviewsfinder.apptree;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import uk.co.massycat.appreviewsfinder.apptree.entries.AppEntry;
import uk.co.massycat.appreviewsfinder.apptree.entries.CountryTreeEntry;
import uk.co.massycat.appreviewsfinder.apptree.entries.VersionEntry;

/**
 *
 * @author ben
 */
public class CellRenderer implements TreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        JComponent comp = null;
        //System.out.println("Class of tree object: " + value.getClass());
        DefaultMutableTreeNode tree_node = (DefaultMutableTreeNode) value;
        Object user_object = tree_node.getUserObject();

        if (user_object instanceof AppEntry) {
            AppEntry app = (AppEntry) user_object;
            JLabel label = new JLabel(app.mName + " - by " + app.mArtist, app.mArt,
                    SwingConstants.LEFT);
            comp = label;
        } else if (user_object instanceof VersionEntry) {
            VersionEntry version = (VersionEntry) user_object;
            JLabel label = new JLabel("Version: " + version.mVersion);
            comp = label;
        } else if (user_object instanceof CountryTreeEntry) {
            //
            //
            //
            CountryTreeEntry country = (CountryTreeEntry) user_object;
            if (country.mReviewsCount > 0) {
                JPanel panel = new JPanel(new FlowLayout());
                JLabel label = new JLabel(country.mCode + "  " +
                        country.mReviewsCount + " reviews",
                        country.mCountry.mFlag,
                        SwingConstants.LEFT);
                panel.add(label);

                int average = Math.round(country.mRating);


                String icon_resource = "/uk/co/massycat/appreviewsfinder/resources/stars/" + average + "stars_16.png";
                URL resource_url = getClass().getResource(icon_resource);
                if (resource_url != null) {
                    Icon stars = new ImageIcon(resource_url);
                    JLabel rating = new JLabel(stars);
                    panel.add(rating);
                } else {
                    JLabel rating = new JLabel("Averaging " + country.mRating);
                    panel.add(rating);
                }


                comp = panel;
            } else {
                comp = new JLabel(country.mCode,
                        country.mCountry.mFlag,
                        SwingConstants.LEFT);
            }
        } else if (user_object instanceof String) {
            String string = (String) user_object;
            JLabel label = new JLabel(string);
            comp = label;
        } else {
            JLabel label = new JLabel(new ImageIcon(getClass().getResource("/uk/co/massycat/appreviewsfinder/resources/trockle2.png")));

//            System.out.println( "Preferred size is " + label.getPreferredSize());
//            System.out.println( "Min size is " + label.getMinimumSize());
//            System.out.println( "Max size is " + label.getMaximumSize());
            comp = label;
        }

        comp.setOpaque(selected);
        if (selected) {
            comp.setBackground(Color.lightGray);
        }

        return comp;
    }
}
