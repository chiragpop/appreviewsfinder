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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ben
 */
public class AppReviewDecoder {
    private static final String TITLE_START_STRING = "<TextView topInset=\"0\" truncation=\"right\" leftInset=\"0\" squishiness=\"1\" styleSet=\"basic13\" textJust=\"left\" maxLines=\"1\"><SetFontStyle normalStyle=\"textColor\"><b>";
    private static final String TITLE_END_STRING = "</b>";
    private static final String RATING_START_STRING = "<HBoxView topInset=\"1\" alt=\"";
    private static final String RATING_END_STRING = " ";
    private static final String AUTHOR_START_STRING_1 = "<TextView topInset=\"0\" truncation=\"right\" leftInset=\"0\" squishiness=\"1\" styleSet=\"basic13\" textJust=\"left\" maxLines=\"1\"><SetFontStyle normalStyle=\"textColor\">";
    private static final String AUTHOR_START_STRING_2 = "\">";
    private static final String AUTHOR_END_STRING = "</GotoURL>";
    private static final String VERSION_START_STRING = "Version ";
    //private static final String VERSION_END_STRING = "</SetFontStyle>";
    private static final String VERSION_END_STRING = "\n";
    private static final String DATE_START_STRING = "-";
    private static final String DATE_END_STRING = "</SetFontStyle>";
    private static final String REVIEW_START_STRING = "<TextView topInset=\"2\" leftInset=\"0\" rightInset=\"0\" styleSet=\"normal11\" textJust=\"left\"><SetFontStyle normalStyle=\"textColor\">";
    private static final String REVIEW_END_STRING = "</SetFontStyle>";
    private LinkedList<AppReview> mReviews = new LinkedList<AppReview>();

    private String mReviewsXml;
    private int mOffset;

    private String findBoundString( String start_bound, String end_bound) {
        int start = mOffset;
        int start_len = 0;

        if ( start_bound != null) {
            start = mReviewsXml.indexOf(start_bound, mOffset);
            start_len = start_bound.length();
        }

        if ( start < 0) {
            return null;
        }

        mOffset = start + start_len;

        int end = mReviewsXml.indexOf(end_bound, mOffset);

        if ( end < 0) {
            return null;
        }

        String content = mReviewsXml.substring(mOffset, end);
        mOffset = end + end_bound.length();

        return content;
    }

    public List<AppReview> getReviews() {
        return mReviews;
    }

    public AppReviewDecoder( String reviews_xml) {
        mOffset = 0;
        mReviewsXml = reviews_xml;

        while ( true) {
            AppReview new_review = new AppReview();

            new_review.mTitle = findBoundString(TITLE_START_STRING, TITLE_END_STRING);

            if ( new_review.mTitle == null)
                break;


            new_review.mRatings = Float.parseFloat(findBoundString(RATING_START_STRING, RATING_END_STRING));

            mOffset = mReviewsXml.indexOf(AUTHOR_START_STRING_1, mOffset);

            if ( mOffset < 0) {
                break;
            }
            mOffset += AUTHOR_START_STRING_1.length();

            new_review.mAuthor = findBoundString( AUTHOR_START_STRING_2, AUTHOR_END_STRING);
            if ( new_review.mAuthor == null)
                break;
            new_review.mAuthor = new_review.mAuthor.replaceAll("<b>", "");
            new_review.mAuthor = new_review.mAuthor.replaceAll("</b>", "");
            new_review.mAuthor = new_review.mAuthor.trim();

            new_review.mVersion = findBoundString( VERSION_START_STRING, VERSION_END_STRING);
            if ( new_review.mVersion == null)
                break;

            new_review.mDate = findBoundString( DATE_START_STRING, DATE_END_STRING);
            if ( new_review.mDate == null)
                break;
            new_review.mDate = new_review.mDate.trim();

            new_review.mReview = findBoundString( REVIEW_START_STRING, REVIEW_END_STRING);
            if ( new_review.mReview == null)
                break;

            //System.out.println(new_review.toString());

            mReviews.add(new_review);
        }
    }
}
