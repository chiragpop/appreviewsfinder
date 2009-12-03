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
 * MainWindow.java
 *
 * Created on Aug 28, 2009, 11:28:45 AM
 */
package uk.co.massycat.appreviewsfinder;

import java.awt.BorderLayout;
import uk.co.massycat.appreviewsfinder.countries.FromCountriesDownloaderProgressDialog;
import uk.co.massycat.appreviewsfinder.reviews.ReviewPanel;
import uk.co.massycat.appreviewsfinder.reviews.AppReviewXMLHandler;
import uk.co.massycat.appreviewsfinder.reviews.AppReview;
import uk.co.massycat.appreviewsfinder.reviews.ReviewsDownloader;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import uk.co.massycat.appreviewsfinder.apptree.AppTreeHandler;
import uk.co.massycat.appreviewsfinder.apptree.MyTreeNode;
import uk.co.massycat.appreviewsfinder.apptree.entries.AppEntry;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import uk.co.massycat.appreviewsfinder.apptree.CellRenderer;
import uk.co.massycat.appreviewsfinder.apptree.entries.AppEntryNameComparator;
import uk.co.massycat.appreviewsfinder.apptree.entries.CountryTreeEntry;
import uk.co.massycat.appreviewsfinder.apptree.entries.VersionEntry;
import uk.co.massycat.appreviewsfinder.countries.CountryEntry;
import uk.co.massycat.appreviewsfinder.ratings.RatingsDownloader;
import uk.co.massycat.appreviewsfinder.ratings.RatingsData;
import uk.co.massycat.appreviewsfinder.ratings.RatingsPanel;
import uk.co.massycat.appreviewsfinder.ratings.RatingsXMLHandler;

/**
 *
 * @author ben
 */
public class MainWindow extends javax.swing.JFrame {

    File mAppsDir = null;
    private RatingsPanel mRatingsPanel = new RatingsPanel();

    private DefaultMutableTreeNode getAncestorAppEntryNode(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode app_entry_node = null;

        while (node != null) {
            Object user_obj = node.getUserObject();

            if (user_obj instanceof AppEntry) {
                app_entry_node = node;
                break;
            } else {
                node = (DefaultMutableTreeNode) node.getParent();
            }
        }

        return app_entry_node;
    }

    private void setupUI() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent();

        // Setup the UI for selected tree element
        if (node == null) {
            mDeleteAppMenuItem.setEnabled(false);
            mGetReviewsMenuItem.setEnabled(false);
            mGetLatestReviewsMenuItem.setEnabled(false);
            mGetRatingsMenuItem.setEnabled(false);
            mExportMenu.setEnabled(false);
            mShowAppDirMenuItem.setEnabled(false);

            return;
        }

        DefaultMutableTreeNode app_entry_node = getAncestorAppEntryNode(node);
        if (app_entry_node != null) {
            mDeleteAppMenuItem.setEnabled(true);
            mGetReviewsMenuItem.setEnabled(true);
            mGetLatestReviewsMenuItem.setEnabled(true);
            mGetRatingsMenuItem.setEnabled(true);
            mExportMenu.setEnabled(true);
            mShowAppDirMenuItem.setEnabled(true);
        } else {
            mDeleteAppMenuItem.setEnabled(false);
            mGetReviewsMenuItem.setEnabled(false);
            mGetLatestReviewsMenuItem.setEnabled(false);
            mGetRatingsMenuItem.setEnabled(false);
            mExportMenu.setEnabled(false);
            mShowAppDirMenuItem.setEnabled(false);
        }
    }

    private void createEnvironment() {
        String apps_path = AppPreferences.getPreferences().getAppsPath();

        mAppsDir = new File(apps_path);

        if (!mAppsDir.exists()) {
            mAppsDir.mkdir();
        }
    }

    private void buildIntialTree() {
        File[] apps_files = mAppsDir.listFiles(Utilities.getDirsOnlyFileFilter());

        //
        // Find the applications relating to the files
        //
        LinkedList<AppEntry> apps = new LinkedList<AppEntry>();

        for ( int i = 0; i < apps_files.length; i++) {
            //
            // build an app entry for this app id
            //
            File app_dir = apps_files[i];
            AppEntry app = new AppEntry();
            
            try {
                app.mAppCode = Integer.parseInt(app_dir.getName());

                FileInputStream file_input_stream = new FileInputStream(new File(app_dir, Constants.APP_NAME_FILENAME));

                BufferedReader reader = new BufferedReader(new InputStreamReader(file_input_stream, "UTF-8"));
                app.mName = reader.readLine();
                reader.close();

                file_input_stream = new FileInputStream(new File(app_dir, Constants.APP_ARTIST_FILENAME));
                reader = new BufferedReader(new InputStreamReader(file_input_stream, "UTF-8"));
                app.mArtist = reader.readLine();
                reader.close();

                app.mArt = new ImageIcon(new File(app_dir, Constants.APP_ICON_FILENAME).toURI().toURL());

                File xml_file = new File(app_dir, Constants.APP_RATINGS_XML_FILENAME);
                RatingsXMLHandler ratings_handler = new RatingsXMLHandler(xml_file);
                app.mRatings = ratings_handler.getRatings();

                apps.add(app);
            } catch (Exception e) {
                // simply skip the app that caused the exception
            }
        }

        Collections.sort(apps, new AppEntryNameComparator());

        DefaultTreeModel model = (DefaultTreeModel) mAppsTree.getModel();
        DefaultMutableTreeNode root_node = new DefaultMutableTreeNode("Hello");
        model.setRoot(root_node);


        //
        // Put the discovered apps into the tree
        //
        Iterator<AppEntry> iterator = apps.iterator();
        while ( iterator.hasNext()) {
            AppEntry app = iterator.next();
            DefaultMutableTreeNode app_node = new MyTreeNode();
            
                app_node.setUserObject(app);
                model.insertNodeInto(app_node, root_node, root_node.getChildCount());
        }

        mAppsTree.expandPath(new TreePath(root_node));
    }

    //
    ///
    //
    //
    private void getReviews(boolean latest_only) {
        DefaultMutableTreeNode node = getAncestorAppEntryNode((DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent());

        if (node == null) {
            return;
        }

        Object user_obj = node.getUserObject();

        if (user_obj instanceof AppEntry) {
            // get the countries to search in
            Set<String> default_review_countries = AppPreferences.getPreferences().getReviewCountries();
            SetCountryStoresDialog countries_dialog = new SetCountryStoresDialog(this, true, default_review_countries);
            countries_dialog.setLocationRelativeTo(this);

            countries_dialog.setVisible(true);

            Set<String> countries = countries_dialog.getCountries();

            if (countries.size() == 0) {
                return;
            }

            // collapse the tree and remove the app node's children so it
            // can be rebuilt after download
            DefaultTreeModel model = (DefaultTreeModel) mAppsTree.getModel();
            mAppsTree.collapsePath(new TreePath(model.getPathToRoot(node)));
            while (node.getChildCount() > 0) {
                model.removeNodeFromParent((DefaultMutableTreeNode) node.getChildAt(0));
            }

            // get all the reviews for this app
            AppEntry app = (AppEntry) user_obj;

            //System.out.println("Getting reviews for: " + app);
            File app_dir = new File(mAppsDir, Integer.toString(app.mAppCode));

            ReviewsDownloader downloader = new ReviewsDownloader(countries, app_dir, app.mAppCode, latest_only);
            downloader.start();

            FromCountriesDownloaderProgressDialog progress_dialog = new FromCountriesDownloaderProgressDialog(this, true, downloader);

            progress_dialog.setLocationRelativeTo(this);
            progress_dialog.setVisible(true);

            if (downloader.isAlive()) {
                downloader.interrupt();
            }

            // expand the node to see what was downloaded
            mAppsTree.expandPath(new TreePath(model.getPathToRoot(node)));
        }
    }
    //
    //

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();

        //mReviewsList.setModel(new DefaultListModel());
        //mReviewsList.setCellRenderer(new ReviewCellRender());
        //mReviewsPanel = new ReviewsContainer();
        JViewport reviews_viewport = mReviewsScrollPane.getViewport();
        reviews_viewport.addChangeListener(mReviewsPanel);

        if (!(Utilities.isMacOSX() || Utilities.isWindowsOS())) {
            mFileMenu.remove(mShowAppDirMenuItem);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        mAppsTree = new javax.swing.JTree();
        jPanel3 = new javax.swing.JPanel();
        mAddApplicationButton = new javax.swing.JButton();
        mTopReviewsPanel = new javax.swing.JPanel();
        mReviewsScrollPane = new javax.swing.JScrollPane();
        mReviewsPanel = new uk.co.massycat.appreviewsfinder.reviews.ReviewsContainer();
        jMenuBar1 = new javax.swing.JMenuBar();
        mFileMenu = new javax.swing.JMenu();
        mGetReviewsMenuItem = new javax.swing.JMenuItem();
        mGetLatestReviewsMenuItem = new javax.swing.JMenuItem();
        mGetRatingsMenuItem = new javax.swing.JMenuItem();
        mShowAppDirMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        mDeleteAppMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        mQuitMenuItem = new javax.swing.JMenuItem();
        mExportMenu = new javax.swing.JMenu();
        mExportRatingsMenuItem = new javax.swing.JMenuItem();
        mPrefsMenu = new javax.swing.JMenu();
        mSetSearchITunesMenuItem = new javax.swing.JMenuItem();
        mSetCountryReviews = new javax.swing.JMenuItem();
        mSetTranslationLangMenuItem = new javax.swing.JMenuItem();
        Help = new javax.swing.JMenu();
        mAboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AppReviewsFinder");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(400, 275));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        mAppsTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        mAppsTree.setRootVisible(false);
        mAppsTree.setShowsRootHandles(true);
        mAppsTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                mAppsTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(mAppsTree);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        mAddApplicationButton.setText("Add Application");
        mAddApplicationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAddApplicationButtonActionPerformed(evt);
            }
        });
        jPanel3.add(mAddApplicationButton);

        jPanel1.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(jPanel1);

        mTopReviewsPanel.setLayout(new java.awt.BorderLayout());

        mReviewsScrollPane.setPreferredSize(new java.awt.Dimension(500, 200));

        mReviewsPanel.setLayout(null);
        mReviewsScrollPane.setViewportView(mReviewsPanel);

        mTopReviewsPanel.add(mReviewsScrollPane, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(mTopReviewsPanel);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        mFileMenu.setText("File");

        mGetReviewsMenuItem.setText("Get all reviews");
        mGetReviewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGetReviewsMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mGetReviewsMenuItem);

        mGetLatestReviewsMenuItem.setText("Get latest version reviews only");
        mGetLatestReviewsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGetLatestReviewsMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mGetLatestReviewsMenuItem);

        mGetRatingsMenuItem.setText("Get ratings");
        mGetRatingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mGetRatingsMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mGetRatingsMenuItem);

        mShowAppDirMenuItem.setText("Show App's directory");
        mShowAppDirMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mShowAppDirMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mShowAppDirMenuItem);
        mFileMenu.add(jSeparator2);

        mDeleteAppMenuItem.setText("Delete App");
        mDeleteAppMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mDeleteAppMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mDeleteAppMenuItem);
        mFileMenu.add(jSeparator1);

        mQuitMenuItem.setText("Quit");
        mQuitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mQuitMenuItemActionPerformed(evt);
            }
        });
        mFileMenu.add(mQuitMenuItem);

        jMenuBar1.add(mFileMenu);

        mExportMenu.setText("Export");

        mExportRatingsMenuItem.setText("Export ratings");
        mExportRatingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mExportRatingsMenuItemActionPerformed(evt);
            }
        });
        mExportMenu.add(mExportRatingsMenuItem);

        jMenuBar1.add(mExportMenu);

        mPrefsMenu.setText("Preferences");

        mSetSearchITunesMenuItem.setText("Set search iTunes Store");
        mSetSearchITunesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSetSearchITunesMenuItemActionPerformed(evt);
            }
        });
        mPrefsMenu.add(mSetSearchITunesMenuItem);

        mSetCountryReviews.setText("Set review countries");
        mSetCountryReviews.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSetCountryReviewsActionPerformed(evt);
            }
        });
        mPrefsMenu.add(mSetCountryReviews);

        mSetTranslationLangMenuItem.setText("Set translation language");
        mSetTranslationLangMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mSetTranslationLangMenuItemActionPerformed(evt);
            }
        });
        mPrefsMenu.add(mSetTranslationLangMenuItem);

        jMenuBar1.add(mPrefsMenu);

        Help.setText("Help");

        mAboutMenuItem.setText("About");
        mAboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mAboutMenuItemActionPerformed(evt);
            }
        });
        Help.add(mAboutMenuItem);

        jMenuBar1.add(Help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        CellRenderer my_renderer = new CellRenderer();
        //DefaultTreeCellRenderer my_renderer = new DefaultTreeCellRenderer();

        //my_renderer.setLeafIcon(new ImageIcon(getClass().getResource("/uk/co/massycat/appreviewsfinder/resources/trockle2.png")));
//        Dimension size = my_renderer.getPreferredSize();
//        size.height = 100;
//        size.width = 200;
//        my_renderer.setPreferredSize(size);
//        my_renderer.setMinimumSize(size);
//        my_renderer.setMaximumSize(size);
        mAppsTree.setRowHeight(0);
        mAppsTree.setCellRenderer(my_renderer);

        // create the running environment
        createEnvironment();

        buildIntialTree();

        mAppsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        AppTreeHandler tree_handler = new AppTreeHandler(mAppsTree);

        mAppsTree.addTreeWillExpandListener(tree_handler);

        setupUI();
    }//GEN-LAST:event_formWindowOpened

    private void mAddApplicationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAddApplicationButtonActionPerformed
        SearchAppDialog add_dialog = new SearchAppDialog(this, true);
        add_dialog.setLocationRelativeTo(this);

        add_dialog.setVisible(true);

        //System.out.println("After dialog");

        AppSearchResult app = add_dialog.getResult();

        if (app != null) {
            File app_dir = new File(mAppsDir, Integer.toString(app.mAppCode));

            if (!app_dir.exists()) {
                if (app_dir.mkdir()) {
                    File icon_file = new File(app_dir, Constants.APP_ICON_FILENAME);
                    Image icon = app.mArt.getImage();

                    BufferedImage writable_image = new BufferedImage(icon.getWidth(null), icon.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB);

                    Graphics2D graphs = writable_image.createGraphics();

                    graphs.drawImage(icon, 0, 0, null);

                    //System.out.println("Icon: " + icon);

                    try {
                        ImageIO.write(writable_image, "PNG", icon_file);
                    } catch (Exception e) {
                    }

                    try {
                        OutputStreamWriter name_writer = new OutputStreamWriter(new FileOutputStream(new File(app_dir, Constants.APP_NAME_FILENAME)), "UTF-8");
                        name_writer.write(app.mName);
                        name_writer.close();
                    } catch (Exception e) {
                    }

                    try {
                        OutputStreamWriter artist_writer = new OutputStreamWriter(new FileOutputStream(new File(app_dir, Constants.APP_ARTIST_FILENAME)), "UTF-8");
                        artist_writer.write(app.mArtist);
                        artist_writer.close();
                    } catch (Exception e) {
                    }

                    DefaultMutableTreeNode app_node = new MyTreeNode();

                    AppEntry app_entry = new AppEntry(app);
                    File xml_file = new File(app_dir, Constants.APP_RATINGS_XML_FILENAME);
                    RatingsXMLHandler ratings_handler = new RatingsXMLHandler(xml_file);
                    app_entry.mRatings = ratings_handler.getRatings();
                    app_node.setUserObject(app_entry);

                    DefaultTreeModel model = (DefaultTreeModel) mAppsTree.getModel();
                    DefaultMutableTreeNode root_node = (DefaultMutableTreeNode) model.getRoot();

                    //
                    // find where to insert the new app
                    //
                    int insert_index = 0;
                    int child_count = root_node.getChildCount();

                    for ( int i = 0; i < child_count; i++) {
                        DefaultMutableTreeNode child_node = (DefaultMutableTreeNode)root_node.getChildAt(i);
                        AppEntry child_app = (AppEntry)child_node.getUserObject();

                        if ( AppEntryNameComparator.compareApps(app_entry, child_app) < 0) {
                            // found the insert point
                            break;
                        }
                        else {
                            insert_index += 1;
                        }
                    }
                    model.insertNodeInto(app_node, root_node, insert_index);

                    TreePath root_path = new TreePath(root_node);

                    if (!mAppsTree.isExpanded(root_path)) {
                        mAppsTree.expandPath(root_path);
                    }
                }
            }
        }
    }//GEN-LAST:event_mAddApplicationButtonActionPerformed

    private void mSetSearchITunesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSetSearchITunesMenuItemActionPerformed
        // TODO add your handling code here:
        String search_code = AppPreferences.getPreferences().getSearchCountry();

        CountriesManager manager = CountriesManager.getManager();
        Set<String> code_set = manager.getAllCountryCodes();
        TreeMap<String, CountryEntry> countries_map = new TreeMap<String, CountryEntry>();

        Iterator<String> iterator = code_set.iterator();

        while (iterator.hasNext()) {
            String code = iterator.next();

            countries_map.put(code, manager.getCountryEntry(code));
        }

        SelectCountryDialog country_selector = new SelectCountryDialog(this, true, search_code, countries_map);
        country_selector.setLocationRelativeTo(this);
        country_selector.setTitle("Select search iTunes Store");

        country_selector.setVisible(true);

        String code = country_selector.getSelectedCode();

        if (code != null) {
            AppPreferences.getPreferences().setSearchCountry(code);
        }
    }//GEN-LAST:event_mSetSearchITunesMenuItemActionPerformed

    private void mAboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mAboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this,
                "AppReviewsFinder downloads ratings and reviews of iPhone Apps\n" +
                "from iTunes Stores around the world.\n" +
                "Version " + Version.version() + "\n\n" +
                "Any comments welcome, email:\n\n" +
                "appreviewsfinder@massycat.co.uk\n\n" +
                "If you find AppReviewsFinder useful then please show\n" +
                "your support by buying a copy of Flickmation on\n" +
                "iPhone/iPod Touch.\n\n" +
                "www.massycat.co.uk\n\n" +
                "AppReviewsFinder uses the method from \"Review Scraper\"\n" +
                "by Didev Studios to retrieve the reviews so support them\n" +
                "too, www.didev-studios.com",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_mAboutMenuItemActionPerformed

    private void mQuitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mQuitMenuItemActionPerformed
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_mQuitMenuItemActionPerformed

    private void mAppsTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_mAppsTreeValueChanged
        setupUI();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent();
        // handle selection, setting up what can be done
        if (node == null) {
            return;
        }

        Object user_obj = node.getUserObject();

        if (user_obj instanceof AppEntry) {
            mRatingsPanel.setApp((AppEntry) user_obj);

            // show the ratings for this app
            if (mRatingsPanel.getParent() == null) {
                mTopReviewsPanel.removeAll();
                mTopReviewsPanel.add(mRatingsPanel, BorderLayout.CENTER);
                mTopReviewsPanel.repaint();
            }

            mTopReviewsPanel.revalidate();
        } else {
            if (user_obj instanceof CountryTreeEntry) {
                //
                // Get the reviews and put them in the table
                //
                CountryTreeEntry CountryTreeEntry = (CountryTreeEntry) node.getUserObject();

                DefaultMutableTreeNode version_node = (DefaultMutableTreeNode) node.getParent();
                DefaultMutableTreeNode app_node = (DefaultMutableTreeNode) version_node.getParent();
                VersionEntry version = (VersionEntry) version_node.getUserObject();
                AppEntry app_entry = (AppEntry) app_node.getUserObject();

                File app_dir = new File(mAppsDir, Integer.toString(app_entry.mAppCode));
                File version_dir = new File(app_dir, version.mVersion);
                File country_file = new File(version_dir, CountryTreeEntry.mCode +
                        AppReviewXMLHandler.APP_REVIEWS_XML_FILE_SUFFIX);
                AppReviewXMLHandler handler = new AppReviewXMLHandler(country_file);
                List<AppReview> reviews = handler.getReviews();

                if (reviews.size() == 0) {
                    return;
                }

                //DefaultListModel model = (DefaultListModel)mReviewsList.getModel();
                //model.clear();
                if (true) {
                    mReviewsScrollPane.getViewport().setViewPosition(new Point(0, 0));
                    mReviewsPanel.setReviews(reviews, CountryTreeEntry.mCode);
                } else {
                    mReviewsPanel.removeAll();
                    mReviewsPanel.setLayout(new GridLayout(reviews.size(), 1));
                    Iterator<AppReview> iterator = reviews.iterator();

                    while (iterator.hasNext()) {
                        AppReview review = iterator.next();
                        ReviewPanel panel = new ReviewPanel();
                        JLabel title = panel.getTitleLabel();
                        title.setText(review.mTitle);

                        int rating = (int) review.mRatings;
                        String icon_resource = "/uk/co/massycat/appreviewsfinder/resources/stars/" + rating + "stars_16.png";
                        URL resource_url = getClass().getResource(icon_resource);
                        Icon stars = null;
                        if (resource_url != null) {
                            stars = new ImageIcon(resource_url);
                        }
                        title.setIcon(stars);

                        JLabel subtitle = panel.getSubTitleLabel();
                        subtitle.setText("By " + review.mAuthor + " on " + review.mDate);

                        JTextArea review_field = panel.getReviewArea();
                        review_field.setText(review.mReview);
                        mReviewsPanel.add(panel);
                    }
                }
                if (mReviewsScrollPane.getParent() == null) {
                    mTopReviewsPanel.removeAll();
                    mTopReviewsPanel.add(mReviewsScrollPane, BorderLayout.CENTER);
                    mTopReviewsPanel.repaint();
                }

                mTopReviewsPanel.revalidate();
            }
        }
    }//GEN-LAST:event_mAppsTreeValueChanged

    private void mGetReviewsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGetReviewsMenuItemActionPerformed
        getReviews(false);
    }//GEN-LAST:event_mGetReviewsMenuItemActionPerformed

    private void mDeleteAppMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mDeleteAppMenuItemActionPerformed
        DefaultMutableTreeNode node = getAncestorAppEntryNode((DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent());

        if (node == null) {
            return;
        }

        Object user_obj = node.getUserObject();

        if (user_obj instanceof AppEntry) {
            // delete the app's directory and remove it from the
            // table
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete App", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                //System.out.println("Deleting app");
                AppEntry app = (AppEntry) user_obj;

                DefaultTreeModel model = (DefaultTreeModel) mAppsTree.getModel();
                model.removeNodeFromParent(node);

                File app_dir = new File(mAppsDir, Integer.toString(app.mAppCode));
                Utilities.deleteDirectory(app_dir);
            }
        }
    }//GEN-LAST:event_mDeleteAppMenuItemActionPerformed

    private void mSetCountryReviewsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSetCountryReviewsActionPerformed
        Set<String> review_countries = AppPreferences.getPreferences().getReviewCountries();
        SetCountryStoresDialog dialog = new SetCountryStoresDialog(this, true, review_countries);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        if (!dialog.wasCancelled()) {
            review_countries = dialog.getCountries();
            AppPreferences.getPreferences().setReviewCountries(review_countries);
        }
    }//GEN-LAST:event_mSetCountryReviewsActionPerformed

    private void mSetTranslationLangMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mSetTranslationLangMenuItemActionPerformed
        //
        // Show a dialog with countries that have Google translation codes
        //
        CountriesManager manager = CountriesManager.getManager();
        Set<String> code_set = manager.getAllCountryCodes();
        TreeMap<String, CountryEntry> countries_map = new TreeMap<String, CountryEntry>();

        Iterator<String> iterator = code_set.iterator();

        while (iterator.hasNext()) {
            String code = iterator.next();
            CountryEntry entry = manager.getCountryEntry(code);

            if (entry.mGoogleCode != null) {
                countries_map.put(code, entry);
            }
        }

        AppPreferences prefs = AppPreferences.getPreferences();
        SelectCountryDialog country_selector = new SelectCountryDialog(this, true, prefs.getTranslationCountry(), countries_map);
        country_selector.setLocationRelativeTo(this);
        country_selector.setTitle("Select Translation Language");
        country_selector.setVisible(true);

        String code = country_selector.getSelectedCode();

        if (code != null) {
            prefs.setTranslationCountry(code);
        }

    }//GEN-LAST:event_mSetTranslationLangMenuItemActionPerformed

    private void mGetLatestReviewsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGetLatestReviewsMenuItemActionPerformed
        getReviews(true);
    }//GEN-LAST:event_mGetLatestReviewsMenuItemActionPerformed

    private void mGetRatingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mGetRatingsMenuItemActionPerformed
        // Get some ratings
        DefaultMutableTreeNode node = getAncestorAppEntryNode((DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent());

        if (node == null) {
            return;
        }

        Object user_obj = node.getUserObject();

        if (user_obj instanceof AppEntry) {
            // get the countries to search in
            Set<String> default_review_countries = AppPreferences.getPreferences().getReviewCountries();
            SetCountryStoresDialog countries_dialog = new SetCountryStoresDialog(this, true, default_review_countries);
            countries_dialog.setLocationRelativeTo(this);

            countries_dialog.setVisible(true);

            Set<String> countries = countries_dialog.getCountries();

            if (countries.size() == 0) {
                return;
            }

            AppEntry app = (AppEntry) user_obj;

            RatingsDownloader downloader = new RatingsDownloader(countries, app.mAppCode);
            downloader.start();

            FromCountriesDownloaderProgressDialog progress_dialog = new FromCountriesDownloaderProgressDialog(this, true, downloader);
            progress_dialog.setTitle("Ratings downloading...");

            progress_dialog.setLocationRelativeTo(this);
            progress_dialog.setVisible(true);

            if (downloader.isAlive()) {
                downloader.interrupt();
            }

            Dictionary<String, RatingsData> ratings = downloader.getRatings();

            if (app.mRatings == null) {
                app.mRatings = ratings;
            } else {
                Enumeration<String> ratings_enumer = ratings.keys();

                while (ratings_enumer.hasMoreElements()) {
                    String country_code = ratings_enumer.nextElement();
                    RatingsData pair = ratings.get(country_code);
                    app.mRatings.put(country_code, pair);
                }
            }

//            Enumeration<String> ratings_enumer = app.mRatings.keys();

//            while (ratings_enumer.hasMoreElements()) {
//                String country_code = ratings_enumer.nextElement();
//                RatingsPair pair = ratings.get(country_code);
//
//                System.out.println("Ratings for " + country_code);
//
//                for (int i = 0; i < pair.mCurrent.length; i++) {
//                    System.out.println(Integer.toString(5 - i) + " stars: " + pair.mCurrent[i]);
//                }
//                System.out.println();
//                for (int i = 0; i < pair.mAll.length; i++) {
//                    System.out.println(Integer.toString(5 - i) + " stars: " + pair.mAll[i]);
//                }
//
//
//                System.out.println("\n");
//            }

            mRatingsPanel.setApp(app);

            File app_dir = new File(mAppsDir, Integer.toString(app.mAppCode));
            File xml_file = new File(app_dir, Constants.APP_RATINGS_XML_FILENAME);
            String ratings_xml = RatingsXMLHandler.createXML(app.mRatings);

            // save the xml
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xml_file), "UTF8"));
                writer.write(ratings_xml.toString());
                writer.close();
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_mGetRatingsMenuItemActionPerformed

    private void mExportRatingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mExportRatingsMenuItemActionPerformed
        //
        //
        // Export the ratings for the selected App as a csv file.
        //
        //
        DefaultMutableTreeNode node = getAncestorAppEntryNode((DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent());

        if (node == null) {
            return;
        }

        Object user_obj = node.getUserObject();

        if (!(user_obj instanceof AppEntry)) {
            return;
        }

        AppEntry app = (AppEntry) user_obj;
        JFileChooser chooser = new JFileChooser();

        File export_path = AppPreferences.getPreferences().getExportPath();
        chooser.setCurrentDirectory(export_path);

        chooser.setDialogTitle("Export ratings as CSV file");
        int res = chooser.showSaveDialog(this);

        if (res == JFileChooser.APPROVE_OPTION) {
            AppPreferences.getPreferences().setExportPath(chooser.getCurrentDirectory());

            File export_file = chooser.getSelectedFile();

            if (export_file.exists()) {
                int write = JOptionPane.showConfirmDialog(this, "File exists, overwrite?");

                if (write != JOptionPane.OK_OPTION) {
                    return;
                }
            }

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(export_file), "UTF8"));
                writer.write("Country, 5 stars, 4 stars, 3 stars, 2 stars, 1 star, " +
                        "all 5 star, all 4 star, all 3 star, all 2 stars, all 1 star, Retrieve date\n");

                Enumeration<String> key_enum = app.mRatings.keys();

                while (key_enum.hasMoreElements()) {
                    String country = key_enum.nextElement();

                    RatingsData rating = app.mRatings.get(country);

                    writer.write(country);

                    for (int i = 0; i < rating.mCurrent.length; i++) {
                        writer.write(", " + rating.mCurrent[i]);
                    }

                    for (int i = 0; i < rating.mAll.length; i++) {
                        writer.write(", " + rating.mAll[i]);
                    }

                    if (rating.mRetrieveDate != null) {
                        writer.write(", " + rating.mRetrieveDate);
                    }
                    writer.write("\n");
                }
                writer.close();
            } catch (Exception e) {
            }
        }




    }//GEN-LAST:event_mExportRatingsMenuItemActionPerformed

    private void mShowAppDirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mShowAppDirMenuItemActionPerformed
        // Show the directory that contains the App's data files
        DefaultMutableTreeNode node = getAncestorAppEntryNode((DefaultMutableTreeNode) mAppsTree.getLastSelectedPathComponent());

        if (node == null) {
            return;
        }

        AppEntry app_entry = (AppEntry)node.getUserObject();

        String file_manager_cmd = null;
        File app_dir = new File(mAppsDir, Integer.toString(app_entry.mAppCode));

        if (Utilities.isMacOSX()) {
            file_manager_cmd = "open " + app_dir.getPath();
        } else if (Utilities.isWindowsOS()) {
            file_manager_cmd = "Explorer.exe " + app_dir.getPath();
        }

        if (file_manager_cmd != null) {
            try {
                Runtime.getRuntime().exec(file_manager_cmd);
            } catch (Exception e) {
            }
        }
    }//GEN-LAST:event_mShowAppDirMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //System.out.println("Locale: " + Locale.getDefault());
        //System.out.println("CharacterSet: " + Charset.defaultCharset());

//        String file_encoding = System.getProperty("file.encoding");
//        if ( !file_encoding.equals("UTF-8")) {
//            System.setProperty("file.encoding", "UTF-8");
//            //System.out.println("file.encoding=" + System.getProperty("file.encoding"));
//        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu Help;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem mAboutMenuItem;
    private javax.swing.JButton mAddApplicationButton;
    private javax.swing.JTree mAppsTree;
    private javax.swing.JMenuItem mDeleteAppMenuItem;
    private javax.swing.JMenu mExportMenu;
    private javax.swing.JMenuItem mExportRatingsMenuItem;
    private javax.swing.JMenu mFileMenu;
    private javax.swing.JMenuItem mGetLatestReviewsMenuItem;
    private javax.swing.JMenuItem mGetRatingsMenuItem;
    private javax.swing.JMenuItem mGetReviewsMenuItem;
    private javax.swing.JMenu mPrefsMenu;
    private javax.swing.JMenuItem mQuitMenuItem;
    private uk.co.massycat.appreviewsfinder.reviews.ReviewsContainer mReviewsPanel;
    private javax.swing.JScrollPane mReviewsScrollPane;
    private javax.swing.JMenuItem mSetCountryReviews;
    private javax.swing.JMenuItem mSetSearchITunesMenuItem;
    private javax.swing.JMenuItem mSetTranslationLangMenuItem;
    private javax.swing.JMenuItem mShowAppDirMenuItem;
    private javax.swing.JPanel mTopReviewsPanel;
    // End of variables declaration//GEN-END:variables
}
