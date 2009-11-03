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

import uk.co.massycat.appreviewsfinder.reviews.AppReviewXMLHandler;
import java.io.BufferedReader;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import uk.co.massycat.appreviewsfinder.*;
import uk.co.massycat.appreviewsfinder.apptree.entries.AppEntry;
import uk.co.massycat.appreviewsfinder.apptree.entries.VersionEntry;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import uk.co.massycat.appreviewsfinder.apptree.entries.CountryTreeEntry;

/**
 *
 * @author ben
 */
public class AppTreeHandler implements TreeWillExpandListener {

    private JTree mTree;

    private class CountryReviewsFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            boolean accept = false;
            if ( pathname.isFile()) {
                // accept files of the form xx_reviews.xml
                String filename = pathname.getName();

                if ( filename.length() == AppReviewXMLHandler.APP_REVIEWS_XML_FILE_SUFFIX.length() + 2) {
                    String end_string = filename.substring(2);

                    if ( end_string.equals(AppReviewXMLHandler.APP_REVIEWS_XML_FILE_SUFFIX)) {
                        accept = true;
                    }
                }
            }
            return accept;
        }
    }

    public AppTreeHandler(JTree tree) {
        mTree = tree;
    }

    public void treeWillExpand(TreeExpansionEvent event)
            throws ExpandVetoException {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
        Object user_obj = node.getUserObject();
        if (node.getChildCount() != 0) {
            return;
        }

        if (user_obj instanceof AppEntry) {
            //
            // Find the versions of the App
            //
            AppEntry app_entry = (AppEntry) user_obj;
            DefaultTreeModel model = (DefaultTreeModel) mTree.getModel();

            String apps_path = AppPreferences.getPreferences().getAppsPath();
            File app_dir = new File(apps_path, Integer.toString(app_entry.mAppCode));

            File[] versions = app_dir.listFiles(Utilities.getDirsOnlyFileFilter());
            for (int j = 0; j < versions.length; j++) {
                String version = versions[j].getName();

                VersionEntry version_entry = new VersionEntry(false);
                version_entry.mRating = -1.f;
                version_entry.mVersion = version;

                DefaultMutableTreeNode country_node = new MyTreeNode(version_entry);
                model.insertNodeInto(country_node, node, node.getChildCount());
            }
        } else if ( user_obj instanceof VersionEntry) {
            //
            // Find the countries from which there are reviews
            //
            AppEntry app_entry = (AppEntry)((DefaultMutableTreeNode)node.getParent()).getUserObject();
            VersionEntry version_entry = (VersionEntry)user_obj;
            String apps_path = AppPreferences.getPreferences().getAppsPath();
            DefaultTreeModel model = (DefaultTreeModel) mTree.getModel();

            File app_dir = new File(apps_path, Integer.toString(app_entry.mAppCode));
            File version_dir = new File(app_dir, version_entry.mVersion);

            File[] countries = version_dir.listFiles(new CountryReviewsFileFilter());


            CountriesManager manager = CountriesManager.getManager();
            for (int j = 0; j < countries.length; j++) {
                String country_code = countries[j].getName().substring(0, 2);

                if (manager.isCountrySupported(country_code)) {
                    CountryTreeEntry country = new CountryTreeEntry(true);
                    country.mCode = country_code;
                    country.mRating = -1.f;
                    country.mReviewsCount = 0;
                    country.mCountry = manager.getCountryEntry(country_code);

                    // file the reviews count
                    File review_count = new File(version_dir, country_code + "_counts.txt");
                    if ( review_count.exists()) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(review_count));

                            country.mReviewsCount = Integer.parseInt(reader.readLine());
                            country.mRating = Float.parseFloat(reader.readLine());
                        }
                        catch ( Exception e){}
                    }

                    DefaultMutableTreeNode country_node = new MyTreeNode(country);
                    model.insertNodeInto(country_node, node, node.getChildCount());
                }
            }
        }
        else if (false) {
            AppEntry app_entry = (AppEntry) user_obj;
            String apps_path = AppPreferences.getPreferences().getAppsPath();

            File app_dir = new File(apps_path, Integer.toString(app_entry.mAppCode));
            DefaultTreeModel model = (DefaultTreeModel) mTree.getModel();
            //System.out.println("Will expand " + event);

            // find the countries with reviews of the app
            File[] countries = app_dir.listFiles(Utilities.getDirsOnlyFileFilter());
            CountriesManager manager = CountriesManager.getManager();

            for (int j = 0; j < countries.length; j++) {
                String country_code = countries[j].getName();

                if (manager.isCountrySupported(country_code)) {
                    CountryTreeEntry country = new CountryTreeEntry(false);
                    country.mCode = country_code;
                    country.mRating = -1.f;
                    country.mCountry = manager.getCountryEntry(country_code);
                    DefaultMutableTreeNode version_node = new MyTreeNode(country);
                    model.insertNodeInto(version_node, node, node.getChildCount());
                }
            }
        } else if (user_obj.getClass() == CountryTreeEntry.class) {
            DefaultTreeModel model = (DefaultTreeModel) mTree.getModel();
            CountryTreeEntry country_entry = (CountryTreeEntry) user_obj;
            AppEntry app_entry = (AppEntry) ((DefaultMutableTreeNode) node.getParent()).getUserObject();

            String apps_path = AppPreferences.getPreferences().getAppsPath();

            File app_dir = new File(apps_path, Integer.toString(app_entry.mAppCode));
            File country_dir = new File(app_dir, country_entry.mCode);

            // find the known countries for this version of the app
            CountriesManager manager = CountriesManager.getManager();
            File[] versions = country_dir.listFiles(Utilities.getDirsOnlyFileFilter());
            for (int j = 0; j < versions.length; j++) {
                String version = versions[j].getName();

                VersionEntry version_entry = new VersionEntry(false);
                version_entry.mRating = -1.f;
                version_entry.mVersion = version;

                DefaultMutableTreeNode country_node = new MyTreeNode(version_entry);
                model.insertNodeInto(country_node, node, node.getChildCount());
            }
        }
    }

    public void treeWillCollapse(TreeExpansionEvent event)
            throws ExpandVetoException {
    }
}
