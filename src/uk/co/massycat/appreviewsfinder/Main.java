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

package uk.co.massycat.appreviewsfinder;

import uk.co.massycat.appreviewsfinder.ratings.AppRatingsFinder;
import uk.co.massycat.appreviewsfinder.countries.CountriesManager;

/**
 *
 * @author ben
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Find app reviews on App Store");
        // The UK iTunes store code
        String iTunesCode = "143444" + ",2";



        String http_string = "http://ax.search.itunes.apple.com/WebObjects/MZSearchHints.woa/wa/hints?media=software&q=" +
                // testing a search for flickmation
                "Flickma";
        //connectAndGetResponse( http_string);

        String flickmation_string =
                "http://ax.search.itunes.apple.com/WebObjects/MZSearch.woa/wa/search?submit=edit&term=flickmation&media=software";
        //connectAndGetResponse(flickmation_string, iTunesCode);

        int app_code = 322312672;
        String review_string =
                "http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?sortOrdering=4&onlyLatestVersion=false&sortAscending=true&pageNumber=" + 0 + "&type=Purple+Software&id=" + app_code;
        //String review = Utilities.connectAndGetResponse(review_string, "143444" + "-1");

        // This gets Flight Control info
        int flight_app_code = 306220440;
        //app_code = flight_app_code;
        String app_code_string = String.format("%09d", app_code);
        String flight_control =
                "http://ax.itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=" + app_code_string +
                "&mt=8";
        String flight_info = Utilities.connectAndGetResponse(flight_control, "143444" + "-1");
        System.out.println(flight_info);

        AppRatingsFinder ratings_finder = new AppRatingsFinder(flight_info);

        CountriesManager manager = CountriesManager.getManager();

        System.out.println("CountriesManager test: gb is " + manager.getNameForCountry("gb"));
    }
}
