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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
public class AppReviewXMLHandler {

    static final public String APP_REVIEWS_XML_FILE_SUFFIX = "_reviews.xml";
    static final public String APP_REVIEWS_TAG = "app-reviews";
    static final public String APP_REVIEW_TAG = "app-review";
    static final public String TITLE_TAG = "title";
    static final public String AUTHOR_TAG = "author";
    static final public String DATE_TAG = "date";
    static final public String VERSION_TAG = "version";
    static final public String RATING_TAG = "rating";
    static final public String REVIEW_TAG = "review";
    private List<AppReview> mReviews;

    static final private String[] mClearCharacterTags = {
        TITLE_TAG,
        AUTHOR_TAG,
        DATE_TAG,
        VERSION_TAG,
        RATING_TAG,
        REVIEW_TAG
    };

    class XmlReader extends DefaultHandler {

        private AppReview mCurrentReview;
        private StringBuffer mCharacters = new StringBuffer();

        @Override
        public void startElement(String uri,
                String localName,
                String qName,
                Attributes attributes)
                throws SAXException {
            if (qName.equals(APP_REVIEW_TAG)) {
                mCurrentReview = new AppReview();
            } else {
                boolean clear_characters = false;

                for ( int i = 0; i < mClearCharacterTags.length; i++) {
                    if ( qName.equals(mClearCharacterTags[i])) {
                        clear_characters = true;
                        break;
                    }
                }

                if ( clear_characters) {
                    mCharacters.setLength(0);
                }
            }
        }

        @Override
        public void endElement(String uri,
                String localName,
                String qName)
                throws SAXException {
            if ( qName.equals("br")) {
                mCharacters.append("\n");
            }
            else {
            String characters = null;
            if ( mCharacters.length() > 0) {
                characters = mCharacters.toString();

//                try {
//                    characters = new String(characters.getBytes(), "UTF-8");
//                }
//                catch ( Exception e) {}
            }
            if (qName.equals(APP_REVIEW_TAG)) {
                mReviews.add(mCurrentReview);
            }
            else if ( qName.equals(TITLE_TAG)) {
                    mCurrentReview.mTitle = characters;
            }
            else if ( qName.equals(AUTHOR_TAG)) {
                mCurrentReview.mAuthor = characters;
            }
            else if ( qName.equals(DATE_TAG)) {
                mCurrentReview.mDate = characters;
            }
            else if ( qName.equals(VERSION_TAG)) {
                mCurrentReview.mVersion = characters;
            }
            else if ( qName.equals(REVIEW_TAG)) {
                mCurrentReview.mReview = characters;
            }
            else if ( qName.equals(RATING_TAG)) {
                mCurrentReview.mRatings = Float.parseFloat(characters);
            }
            }
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

    public AppReviewXMLHandler(File reviews_xml) {
        mReviews = new LinkedList<AppReview>();

        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            //xml_parser.parse(new InputSource(new FileReader(reviews_xml)), new XmlReader());
            InputStream input_stream = new FileInputStream(reviews_xml);
            InputStreamReader input_reader = new InputStreamReader( input_stream, "UTF8");
            xml_parser.parse( new InputSource(input_reader), new XmlReader());
        } catch (Exception e) {
        }

    }

    public List<AppReview> getReviews() {
        return mReviews;
    }

    static public void addReviewToXml(AppReview review, StringBuffer xml_buffer) {
        xml_buffer.append("  " + Utilities.makeStartTag(APP_REVIEW_TAG) + "\n");

        xml_buffer.append("    " + Utilities.makeElement(TITLE_TAG, review.mTitle) + "\n");
        xml_buffer.append("    " + Utilities.makeElement(AUTHOR_TAG, review.mAuthor) + "\n");
        xml_buffer.append("    " + Utilities.makeElement(DATE_TAG, review.mDate) + "\n");
        xml_buffer.append("    " + Utilities.makeElement(VERSION_TAG, review.mVersion) + "\n");
        xml_buffer.append("    " + Utilities.makeElement(RATING_TAG, Float.toString(review.mRatings)) + "\n");
        xml_buffer.append("    " + Utilities.makeElement(REVIEW_TAG, review.mReview) + "\n");

        xml_buffer.append("  " + Utilities.makeEndTag(APP_REVIEW_TAG) + "\n");
    }

    static public void startReviewsXml(StringBuffer xml_buffer) {
        xml_buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<" + APP_REVIEWS_TAG + ">\n");
    }

    static public void endReviewsXml(StringBuffer xml_buffer) {
        xml_buffer.append("</" + APP_REVIEWS_TAG + ">\n");
    }

    static public String makeReviewsXml(List<AppReview> reviews_list) {
        StringBuffer xml_buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<" + APP_REVIEWS_TAG + ">\n");

        Iterator<AppReview> iterator = reviews_list.iterator();

        while (iterator.hasNext()) {
            AppReview review = iterator.next();

            addReviewToXml(review, xml_buffer);
        }

        xml_buffer.append("</" + APP_REVIEWS_TAG + ">\n");

        return xml_buffer.toString();
    }
}
