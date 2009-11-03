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

import uk.co.massycat.appreviewsfinder.countries.FromCountriesDownloader;
import uk.co.massycat.appreviewsfinder.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;

/**
 *
 * @author ben
 */
public class ReviewsDownloader extends FromCountriesDownloader {
    private File AppDir;
    private int mAppCode;
    private int mTotalReviews;
    private boolean mLatestOnly;

    public int getCurrentTotal() {
        return mTotalReviews;
    }

    public ReviewsDownloader(Set<String> countries, File app_dir, int app_code, boolean latest_only) {
        super(countries);
        AppDir = app_dir;
        mAppCode = app_code;
        mLatestOnly = latest_only;
    }

    protected void doWorkForCountry() {
        Hashtable<String, LinkedList<AppReview>> reviews_by_version = new Hashtable<String, LinkedList<AppReview>>();
        for (int page_num = 0; !mCauseExit; page_num++) {
            String http_request = "http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?sortOrdering=4&onlyLatestVersion=" + (mLatestOnly ? "true" : "false") + "&sortAscending=true&pageNumber=" + page_num + "&type=Purple+Software&id=" + mAppCode;
            int itunes_code = CountriesManager.getManager().getITunesCodeForCountry(mCurrentCode);
            String iTunesCode = Integer.toString(itunes_code) + "-1";

            String review_string = Utilities.connectAndGetResponse(http_request, iTunesCode);

            //System.out.println("Review string:\n" + review_string);

            AppReviewDecoder decoder = new AppReviewDecoder(review_string);

            List<AppReview> reviews = decoder.getReviews();
            mTotalReviews += reviews.size();

            if (reviews.size() == 0) {
                // finished
                break;
            }

            Iterator<AppReview> iterator = reviews.iterator();

            while (iterator.hasNext()) {
                AppReview review = iterator.next();

                LinkedList<AppReview> reviews_list = reviews_by_version.get(review.mVersion);

                // create the version list if it has not been seen yet
                if (reviews_list == null) {
                    reviews_list = new LinkedList<AppReview>();
                    reviews_by_version.put(review.mVersion, reviews_list);
                }

                reviews_list.add(review);
            }
        }

        //System.out.println("Reviews:\n" + reviews);
        Set<String> versions = reviews_by_version.keySet();

        //System.out.println("Versions seen: " + versions);

        Iterator<String> version_iter = versions.iterator();

        while (version_iter.hasNext()) {
            String version = version_iter.next();

            File version_dir = new File(AppDir, version);

            if (!version_dir.exists()) {
                version_dir.mkdir();
            }

            List<AppReview> reviews_list = reviews_by_version.get(version);
            Iterator<AppReview> reviews_iter = reviews_list.iterator();
            StringBuffer reviews_xml = new StringBuffer();
            float total_rating = 0.f;
            int reviews_count = 0;

            AppReviewXMLHandler.startReviewsXml(reviews_xml);

            while (reviews_iter.hasNext()) {
                AppReview review = reviews_iter.next();

                AppReviewXMLHandler.addReviewToXml(review, reviews_xml);

                reviews_count += 1;
                total_rating += review.mRatings;
            }

            AppReviewXMLHandler.endReviewsXml(reviews_xml);

            if (reviews_xml != null) {
                try {
                    File file = new File(version_dir,
                            mCurrentCode + AppReviewXMLHandler.APP_REVIEWS_XML_FILE_SUFFIX);
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
                    writer.write(reviews_xml.toString());
                    writer.close();

                    file = new File(version_dir, mCurrentCode + "_counts.txt");
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
                    writer.write(Integer.toString(reviews_count) + "\n");
                    writer.write(Float.toString(total_rating / (float) reviews_count) + "\n");
                    writer.close();
                } catch (Exception e) {
                }
            }
        }
        reviews_by_version = null;
    }
}
