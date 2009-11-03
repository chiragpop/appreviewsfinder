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

import java.io.File;
import java.io.FileReader;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.co.massycat.appreviewsfinder.Utilities;

/**
 *
 * @author ben
 */
public class RatingsXMLHandler {

    Dictionary<String, RatingsData> mRatings;
    public static final String APP_RATINGS_TAG = "app-ratings";
    public static final String APP_RATING_TAG = "rating";
    public static final String COUNTRY_TAG = "country";
    public static final String RETRIEVE_DATE_TAG = "retrieve-date";
    public static final String CURRENT_5_TAG = "cur-5";
    public static final String CURRENT_4_TAG = "cur-4";
    public static final String CURRENT_3_TAG = "cur-3";
    public static final String CURRENT_2_TAG = "cur-2";
    public static final String CURRENT_1_TAG = "cur-1";
    public static final String ALL_5_TAG = "all-5";
    public static final String ALL_4_TAG = "all-4";
    public static final String ALL_3_TAG = "all-3";
    public static final String ALL_2_TAG = "all-2";
    public static final String ALL_1_TAG = "all-1";
    private static final String[] CURRENT_TAGS = {
        CURRENT_5_TAG,
        CURRENT_4_TAG,
        CURRENT_3_TAG,
        CURRENT_2_TAG,
        CURRENT_1_TAG
    };
    private static final String[] ALL_TAGS = {
        ALL_5_TAG,
        ALL_4_TAG,
        ALL_3_TAG,
        ALL_2_TAG,
        ALL_1_TAG
    };

    class XmlReader extends DefaultHandler {

        private int mLevel = 0;
        private StringBuffer mCharacters = new StringBuffer();
        private int[] mCurrentRatings = new int[5];
        private int[] mAllRatings = new int[5];
        private String mCountry = null;
        private String mRetrieveDate = null;

        @Override
        public void startElement(String uri,
                String localName,
                String qName,
                Attributes attributes)
                throws SAXException {
            mLevel += 1;

            switch(mLevel) {
                case 2:
                    if ( qName.equals(APP_RATING_TAG)) {
                        for ( int i = 0; i < mCurrentRatings.length; i++) {
                            mCurrentRatings[i] = 0;
                            mAllRatings[i] = 0;
                        }
                        mCountry = null;
                        mRetrieveDate = null;
                    }
                    break;

                case 3:
                    mCharacters.setLength(0);
                    break;
            }
        }

        @Override
        public void endElement(String uri,
                String localName,
                String qName)
                throws SAXException {
            switch(mLevel) {
                case 2:
                    if ( qName.equals(APP_RATING_TAG)) {
                        // FIXME: save the current ratings to the dictionary
                        //System.out.println("Ratings for country " + mCountry);
                        int current_total = 0;
                        int all_total = 0;

                        for ( int i = 0; i < mCurrentRatings.length; i++) {
                            current_total += mCurrentRatings[i];
                            all_total += mAllRatings[i];
                        }

                        if ( current_total > 0 || all_total > 0) {
                            mRatings.put(mCountry, new RatingsData( mCurrentRatings.clone(),
                                    mAllRatings.clone(), mRetrieveDate));
                        }
                    }
                    break;

                case 3:
                    if ( qName.equals(COUNTRY_TAG)) {
                        mCountry = mCharacters.toString();
                    }
                    else if ( qName.equals(RETRIEVE_DATE_TAG)) {
                        mRetrieveDate = mCharacters.toString();
                    }
                    else {
                        boolean handled = false;
                        for ( int i = 0; i < CURRENT_TAGS.length && !handled; i++) {
                            if ( qName.equals(CURRENT_TAGS[i])) {
                                handled = true;
                                mCurrentRatings[i] = Integer.parseInt(mCharacters.toString());
                            }
                        }
                        for ( int i = 0; i < ALL_TAGS.length && !handled; i++) {
                            if ( qName.equals(ALL_TAGS[i])) {
                                handled = true;
                                mAllRatings[i] = Integer.parseInt(mCharacters.toString());
                            }
                        }
                    }
                    break;
            }

            mLevel -= 1;
        }

        @Override
        public void characters(char[] ch,
                int start,
                int length)
                throws SAXException {
            String add_on = new String(ch, start, length);
            mCharacters.append(add_on);
        }
    }

    public Dictionary<String, RatingsData> getRatings() {
        return mRatings;
    }

    public RatingsXMLHandler(File source_file) {
        mRatings = new Hashtable<String, RatingsData>();

        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(new InputSource(new FileReader(source_file)), new XmlReader());
        } catch (Exception e) {
        }
    }

    //
    //
    //
    //
    //
    //
    public static void addRatingToXml(String country, RatingsData pair, StringBuffer xml_buffer) {
        xml_buffer.append("  " + Utilities.makeStartTag(APP_RATING_TAG) + "\n");

        xml_buffer.append("    " + Utilities.makeElement(COUNTRY_TAG, country) + "\n");

        if ( pair.mRetrieveDate != null) {
            xml_buffer.append("    " + Utilities.makeElement(RETRIEVE_DATE_TAG, pair.mRetrieveDate) + "\n");
        }

        for (int i = 0; i < pair.mCurrent.length; i++) {
            xml_buffer.append("    " + Utilities.makeElement(CURRENT_TAGS[i],
                    Integer.toString(pair.mCurrent[i])) + "\n");
            xml_buffer.append("    " + Utilities.makeElement(ALL_TAGS[i],
                    Integer.toString(pair.mAll[i])) + "\n");
        }

        xml_buffer.append("  " + Utilities.makeEndTag(APP_RATING_TAG) + "\n");
    }

    public static String createXML(Dictionary<String, RatingsData> ratings) {
        StringBuffer xml_buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                Utilities.makeStartTag(APP_RATINGS_TAG) + "\n");

        Enumeration<String> ratings_enumer = ratings.keys();
        while (ratings_enumer.hasMoreElements()) {
            String country_code = ratings_enumer.nextElement();
            RatingsData pair = ratings.get(country_code);

            addRatingToXml(country_code, pair, xml_buffer);
        }

        xml_buffer.append(Utilities.makeEndTag(APP_RATINGS_TAG) + "\n");

        return xml_buffer.toString();

    }
}
