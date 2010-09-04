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
package uk.co.massycat.appreviewsfinder.ratings;

import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;
import uk.co.massycat.appreviewsfinder.Utilities;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import uk.co.massycat.appreviewsfinder.countries.FromCountriesDownloader;

/**
 *
 * @author ben
 */
public class RatingsDownloader extends FromCountriesDownloader {

    private int mTotalNumRatings;
    private Dictionary mRatings;
    private String mAppCodeString;
    private CountriesManager mManager = CountriesManager.getManager();
    private String mRetrieveDate;

    public Dictionary<String, RatingsData> getRatings() {
        return mRatings;
    }

    public int getCurrentTotal() {
        return mTotalNumRatings;
    }

    protected void doWorkForCountry() {
        int itunes_store = mManager.getITunesCodeForCountry(mCurrentCode);

        String search_url =
                "http://ax.itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=" + mAppCodeString +
                "&mt=8";
        String app_info = Utilities.connectAndGetResponse(search_url, Integer.toString(itunes_store) + "-1");

        // iTunes now redirects us somewhere else
        GotoURLFinder goto_finder = new GotoURLFinder(app_info);
        if ( goto_finder.getGotoUrl() != null) {
            app_info = Utilities.connectAndGetResponse(goto_finder.getGotoUrl(), Integer.toString(itunes_store) + "-1");
        }

        AppRatingsFinder ratings_finder = new AppRatingsFinder(app_info);

        //System.out.println("Ratings from " + mManager.getNameForCountry(mCurrentCode));
        int[] current = ratings_finder.getCurrentVersionRatings();
        int current_total = 0;
        //System.out.println("Current version ratings:");
        for (int i = 0; i < current.length; i++) {
            current_total += current[i];
            //System.out.println(Integer.toString(i + 1) + " stars, " + current[i] + " ratings");
        }
        mTotalNumRatings += current_total;
        //System.out.println("\n");


        int[] all_versions = ratings_finder.getAllVersionsRatings();
        int all_total = 0;
        //System.out.println("All versions ratings:");
        for (int i = 0; i < all_versions.length; i++) {
            all_total += all_versions[i];
            //System.out.println(Integer.toString(i + 1) + " stars, " + all_versions[i] + " ratings");
        }
        mTotalNumRatings += all_total;
        //System.out.println("\n");

        if ( current_total > 0 || all_total > 0) {
            RatingsData pair = new RatingsData(current, all_versions, mRetrieveDate);
            mRatings.put( mCurrentCode, pair);
        }
    }

    public RatingsDownloader(Set<String> countries, int app_code) {
        super(countries);

        Calendar date = Calendar.getInstance();

        mRetrieveDate = String.format("%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH));
        mAppCodeString = String.format("%09d", app_code);
        mTotalNumRatings = 0;
        mRatings = new Hashtable<String, RatingsData>();
    }
}
