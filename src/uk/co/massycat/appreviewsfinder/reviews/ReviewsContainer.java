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
package uk.co.massycat.appreviewsfinder.reviews;

import uk.co.massycat.appreviewsfinder.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import uk.co.massycat.appreviewsfinder.google.Translator;

/**
 *
 * @author ben
 */
public class ReviewsContainer extends JPanel implements Scrollable, ChangeListener, TranslationButtonListener {

    List<AppReview> mReviews = null;
    String mGoogleCode;
    static final Dimension mReviewSize;

    static {
        ReviewPanel review = new ReviewPanel();

        mReviewSize = review.getPreferredSize();
    }

    /*
     * Scrollable interface
     */
    public Dimension getPreferredScrollableViewportSize() {
        Dimension dim = new Dimension(mReviewSize);

        dim.height *= 4;

        return dim;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation,
            int direction) {
        int increment = 1;

        if (orientation == SwingConstants.VERTICAL) {
            increment = mReviewSize.height / 4;
            int remainder = visibleRect.y % mReviewSize.height;

            if (direction > 0) {
                // down
                remainder = mReviewSize.height - remainder;
            }
            if (remainder < increment && remainder != 0) {
                increment = remainder;
            }
        }

        return increment;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation,
            int direction) {
        int increment = 1;

        if (orientation == SwingConstants.VERTICAL) {
            increment = mReviewSize.height;
        }

        return increment;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /*
     * End of Scrollable interface
     */
    public void translationRequested(ReviewPanel review_panel) {
        // do the translation switch
        JButton trans_button = review_panel.getTranslateButton();
        AppReview review = mReviews.get(review_panel.mReviewNumber);

        if (trans_button.getText().equals(ReviewPanel.ORIGINAL_STRING)) {
            // switch to the original text
            review_panel.getReviewArea().setText(review.mReview);
            trans_button.setText(ReviewPanel.TRANSLATE_STRING);
        } else {
            //System.out.println("Translation requested for review " + review_panel.mReviewNumber);

            String trans_country = AppPreferences.getPreferences().getTranslationCountry();

            if ( !(trans_country.equals(review.mTransCountry) && review.mTranslation != null)) {
                // need to get the review
                String trans_code = CountriesManager.getManager().getGoogleCodeForCountry(trans_country);
                review.mTransCountry = trans_country;
                review.mTranslation = Translator.translate(review.mReview, mGoogleCode, trans_code);
            }

            if (review.mTranslation != null) {
                review_panel.getReviewArea().setText(review.mTranslation);
                trans_button.setText(ReviewPanel.ORIGINAL_STRING);
            }
            else {
                JOptionPane.showMessageDialog(this.getRootPane(), "Failed to retrieve translation",
                        "Translation error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private ReviewPanel makeReviewPanel(int rev_num) {
        AppReview review = mReviews.get(rev_num);
        ReviewPanel panel = new ReviewPanel(this);
        panel.mReviewNumber = rev_num;
        JLabel title = panel.getTitleLabel();
        title.setText(/*"(" + rev_num + ") " +*/review.mTitle);

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

        panel.getTranslateButton().setVisible(mGoogleCode != null);

        //System.out.println("Review is: " + review.mReview);
        //System.out.println("Font: " + review_field.getFont());
        //System.out.println("Locale: " + review_field.getLocale());

        return panel;
    }

    private void setupReviewsForViewport(JViewport viewport) {
        Rectangle view_rect = viewport.getViewRect();
        boolean added = false;
        //System.out.println("Viewport view rect: " + view_rect);
        //System.out.println("Review height: " + mReviewSize.height);

        int reviews_on_screen = view_rect.height / mReviewSize.height;
        reviews_on_screen += 2;
        int first_review = view_rect.y / mReviewSize.height;

        //System.out.println("Reviews in view = " + reviews_on_screen);
        //System.out.println("First review is " + first_review);
        int last_review = first_review + reviews_on_screen;
        if (last_review > mReviews.size()) {
            last_review = mReviews.size();
        }

        if (true) {
            int num_comps = this.getComponentCount();

            for (int i = 0; i < num_comps; i++) {
                Component comp = this.getComponent(i);

                if (comp instanceof ReviewPanel) {
                    ReviewPanel panel = (ReviewPanel) comp;

                    if (panel.mReviewNumber < first_review ||
                            panel.mReviewNumber >= last_review) {
                        this.remove(i);
                        num_comps -= 1;
                        i -= 1;
                    }
                }
            }
            for (int rev_num = first_review; rev_num < last_review; rev_num++) {
                boolean already_in = false;
                num_comps = this.getComponentCount();

                for (int i = 0; i < num_comps; i++) {
                    Component comp = this.getComponent(i);

                    if (comp instanceof ReviewPanel) {
                        ReviewPanel panel = (ReviewPanel) comp;

                        if (panel.mReviewNumber == rev_num) {
                            already_in = true;
                            break;
                        }
                    }
                }

                if (!already_in) {
                    added = true;
                    this.add(makeReviewPanel(rev_num));
                }
            }
        } else {
            // FIXME: for testing removing and adding reviews instead of reusing
            this.removeAll();
            added = true;
            for (int rev_num = first_review; rev_num < last_review; rev_num++) {
                this.add(makeReviewPanel(rev_num));
            }
        }

        if (added) {
            this.revalidate();
        }
    }

    public void setReviews(List<AppReview> reviews, String language) {
        mReviews = reviews;

        CountriesManager country_manager = CountriesManager.getManager();
        mGoogleCode = country_manager.getGoogleCodeForCountry(language);

        this.removeAll();

        if (mReviews == null || mReviews.size() == 0) {
            this.revalidate();
            return;
        }

        setLayout(new ReviewsLayout());

        Container parent = this.getParent();
        if (parent != null) {
            if (parent instanceof JViewport) {
                setupReviewsForViewport((JViewport) parent);
            }
        }
        this.revalidate();
        this.repaint();
    }

    public void stateChanged(ChangeEvent e) {
        Container parent = this.getParent();

        if (mReviews == null || parent == null || mReviews.size() == 0) {
            // nothing to do
            return;
        }

        if (parent == e.getSource() && parent instanceof JViewport) {
            setupReviewsForViewport((JViewport) parent);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Component parent = getParent();
        if (parent == null) {
            return new Dimension(mReviewSize.height, mReviewSize.width);
        }

        if (mReviews == null || mReviews.size() == 0) {
            return parent.getSize();
        }

        Dimension size = parent.getSize();

        if (size.width < mReviewSize.width) {
            size.width = mReviewSize.width;
        }

        int reviews_height = mReviews.size() * mReviewSize.height;
        if (size.height < reviews_height) {
            size.height = reviews_height;
        }

        return size;
    }
}
