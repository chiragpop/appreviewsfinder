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

import uk.co.massycat.appreviewsfinder.*;
import java.io.StringReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ben
 */
public class AppRatingsFinder {

    enum InRatings {

        NONE,
        CURRENT_VERSION,
        ALL_VERSIONS
    };
    private int[] mCurrentRatings = new int[5];
    private int[] mAllRatings = new int[5];

    class XmlReader extends DefaultHandler {

        InRatings mInRatings = InRatings.NONE;
        int mLevel = -1;
        int mRatingsLevel = -1;

        @Override
        public void startElement(String uri,
                String localName,
                String qName,
                Attributes attributes)
                throws SAXException {
            if (mLevel >= 0) {
                mLevel += 1;

                if (qName.equals("Test")) {
                    String id_value = attributes.getValue("id");

                    if (id_value != null) {
                        if (id_value.equals("1234")) {
                            //System.out.println("Test 1234 at level " + mLevel);
                            mInRatings = InRatings.CURRENT_VERSION;
                            mRatingsLevel = mLevel;
                        } else if (id_value.equals("5678")) {
                            //System.out.println("Test 5678 at level " + mLevel);
                            mInRatings = InRatings.ALL_VERSIONS;
                            mRatingsLevel = mLevel;
                        }
                    }
                }

                if (qName.equals("HBoxView")) {
                    String alt_value = attributes.getValue("alt");

                    if (alt_value != null && mInRatings != InRatings.NONE) {
                        // make sure the alt_value is of the form:
                        // <number> star[s], <number> rating(s)
                        int stars_end_index = alt_value.indexOf(" stars, ");
                        int stars_length = " stars, ".length();
                        if (stars_end_index == -1) {
                            stars_end_index = alt_value.indexOf(" star, ");
                            stars_length = " star, ".length();
                        }
                        int num_ratings_end_index = alt_value.indexOf(" ratings");
                        if (num_ratings_end_index == -1) {
                            num_ratings_end_index = alt_value.indexOf(" rating");
                        }

                        if (stars_end_index > 0 && num_ratings_end_index > 0) {
                            String stars_str = alt_value.substring(0, stars_end_index);
                            String ratings_str = alt_value.substring(stars_end_index + stars_length,
                                    num_ratings_end_index);

                            try {
                                int stars = Integer.parseInt(stars_str);
                                int num_ratings = Integer.parseInt(ratings_str);

                                if (stars >= 1 && stars <= 5) {
                                    if (mInRatings == InRatings.CURRENT_VERSION) {
                                        mCurrentRatings[5 - stars] = num_ratings;
                                    } else if (mInRatings == InRatings.ALL_VERSIONS) {
                                        mAllRatings[5 - stars] = num_ratings;
                                    }
                                }
                            } catch (NumberFormatException e) {
                            }
                        }

                        //System.out.println("Star rating? \"" + alt_value + "\" at level " + mLevel);
                        //System.out.println("In ratings: " + mInRatings);
                    }
                }
            }

            if (qName.equals("View")) {
                //System.out.println("Bonk");
                String view_name = attributes.getValue("viewName");

                if (view_name != null) {
                    if (view_name.equals("RatingsFrame")) {
                        //System.out.println("Found ratings frame: " + attributes);

                        mLevel = 0;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri,
                String localName,
                String qName)
                throws SAXException {
            if (mLevel >= 0) {
                if (mLevel == mRatingsLevel) {
                    if (mInRatings == InRatings.CURRENT_VERSION) {
                        mInRatings = InRatings.NONE;
                    } else if (mInRatings == InRatings.ALL_VERSIONS) {
                        mInRatings = InRatings.NONE;
                    }
                }

                mLevel -= 1;

            }
        }
    }

    public int[] getCurrentVersionRatings() {
        return mCurrentRatings;
    }

    public int[] getAllVersionsRatings() {
        return mAllRatings;
    }

    public AppRatingsFinder(String xml) {
        for (int i = 0; i < mCurrentRatings.length; i++) {
            mCurrentRatings[i] = 0;
            mAllRatings[i] = 0;
        }

        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(new InputSource(new StringReader(xml)), new XmlReader());
        } catch (Exception e) {
        }

    }

    public static void main(String[] args) {

        //int itunes_id = 143442; // France
        int itunes_id = 143444; // UK
        //int itunes_id = 143441; // USA
        //int itunes_id = 143462; // Japan
        // This gets Flight Control info
        int app_code;
        int flickmation_app_code = 322312672;
        int flickmationlite_app_code = 324687219;
        int flight_app_code = 306220440;
        app_code = flight_app_code;
        String app_code_string = String.format("%09d", app_code);
        String flight_control =
                "http://ax.itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=" + app_code_string +
                "&mt=8";
        String flight_info = Utilities.connectAndGetResponse(flight_control, Integer.toString(itunes_id) + "-1");
        System.out.println(flight_info);

        //
        // Need to find the goto link in what we got back.
        //
        GotoURLFinder goto_finder = new GotoURLFinder(flight_info);
        System.out.println("Goto url is \"" + goto_finder.getGotoUrl() + "\"");
        flight_info = Utilities.connectAndGetResponse(goto_finder.getGotoUrl(), Integer.toString(itunes_id) + "-1");
        //String flight_control2 = "http://ax.itunes.apple.com/app/flight-control/id306220440?mt=8";
        //String flight_info2 = Utilities.connectAndGetResponse(flight_control2, Integer.toString(itunes_id) + "-1");
        //System.out.println(flight_info2);

        AppRatingsFinder ratings_finder = new AppRatingsFinder(flight_info);

        int[] current = ratings_finder.getCurrentVersionRatings();
        System.out.println("Current version ratings:");
        for ( int i = 0; i < current.length; i++) {
            System.out.println( Integer.toString(i+1) + " stars, " + current[i] + " ratings");
        }
        System.out.println("\n");
        
        
        int[] all_versions = ratings_finder.getAllVersionsRatings();
        System.out.println("All versions ratings:");
        for ( int i = 0; i < all_versions.length; i++) {
            System.out.println( Integer.toString(i+1) + " stars, " + all_versions[i] + " ratings");
        }


    }
}
