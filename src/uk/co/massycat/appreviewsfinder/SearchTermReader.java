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

import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
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
public class SearchTermReader extends DefaultHandler {

    private static final String ARRAY_TAG = "array";
    private static final String DICT_TAG = "dict";
    private static final String KEY_TAG = "key";
    private static final String TERM_KEY = "term";
    private static final String URL_KEY = "url";
    private ArrayList<SearchTermResult> mApps;

    enum ReadState {

        IDLE,
        IN_HINTS_ARRAY,
        IN_ENTRY
    };
    private ReadState mReadState;
    private String mCharacters;
    private SearchTermResult mSearchResult;
    private String mKey;

    @Override
    public void startElement(String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
        switch (mReadState) {
            case IDLE:
                if (qName.equals(ARRAY_TAG)) {
                    mReadState = ReadState.IN_HINTS_ARRAY;
                }
                break;

            case IN_HINTS_ARRAY:
                if (qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_ENTRY;
                    mSearchResult = new SearchTermResult();
                }
                break;

            case IN_ENTRY:
                if (qName.equals(KEY_TAG)) {
                    mKey = null;
                    mCharacters = new String();
                } else {
                    mCharacters = new String();
                }
                break;
        }
    }

    @Override
    public void endElement(String uri,
            String localName,
            String qName)
            throws SAXException {
        switch (mReadState) {
            case IDLE:
                break;

            case IN_HINTS_ARRAY:
                break;

            case IN_ENTRY:
                if (qName.equals(DICT_TAG)) {
                    // save the app result
                    mApps.add(mSearchResult);
                    mReadState = ReadState.IN_HINTS_ARRAY;
                } else if (qName.equals(KEY_TAG)) {
                    mKey = mCharacters;
                } else {
                    // end of a value
                    if (mKey != null) {
                        if (mKey.equals(TERM_KEY)) {
                            try {
                                mSearchResult.mName = URLDecoder.decode(mCharacters, "UTF-8");
                            } catch (Exception e) {
                            }
                        } else if (mKey.equals(URL_KEY)) {
                                mSearchResult.mURL = mCharacters;
//                            try {
//                                mSearchResult.mURL = URLDecoder.decode(mCharacters, "UTF-8");
//                            } catch (Exception e) {
//                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void characters(char[] ch,
            int start,
            int length)
            throws SAXException {
        String add_on = new String(ch, start, length);
        mCharacters = mCharacters.concat(add_on);
    }

    public SearchTermReader(String xml) {
        mApps = new ArrayList<SearchTermResult>();
        mReadState = ReadState.IDLE;
        mCharacters = new String();

        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(new InputSource(new StringReader(xml)), this);
        } catch (Exception e) {
        }

        //System.out.println("Apps found:\n" + mApps);
    }

    public List getApps() {
        return mApps;
    }
}
