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
import java.util.LinkedList;
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
public class AppSearchReader extends DefaultHandler {

    private static final String KEY_TAG = "key";
    private static final String ARRAY_TAG = "array";
    private static final String DICT_TAG = "dict";
    private static final String ITEMS_KEY = "items";
    private static final String TITLE_KEY = "title";
    private static final String ITEM_ID_KEY = "item-id";
    private static final String URL_KEY = "url";
    private static final String ARTIST_KEY = "artist-name";
    private static final String ARTWORK_URLS_KEY = "artwork-urls";

    enum ReadState {

        IDLE,
        IN_KEY_DEPTH_3,
        SEEN_ITEMS_KEY,
        IN_ITEMS_ARRAY,
        IN_ITEM_DICT,
        SEEN_ARTWORK_KEY,
        IN_ARTWORK_ARRAY,
        COMPLETE
    };
    private ReadState mReadState;
    private String mCharacters;
    private String mCurrentKey;
    private int mDepth;

    private AppSearchResult mCurrentItem;
    private LinkedList<AppSearchResult> mApps;

    //
    //
    //
    @Override
    public void startElement(String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
        mDepth += 1;

        switch (mDepth) {
            case 3:
                switch (mReadState) {
                    case IDLE:
                        if (qName.equals(KEY_TAG)) {
                            mCharacters = new String();
                            mReadState = ReadState.IN_KEY_DEPTH_3;
                        }
                        break;

                    case SEEN_ITEMS_KEY:
                        if (qName.equals(ARRAY_TAG)) {
                            mReadState = ReadState.IN_ITEMS_ARRAY;
                        }
                        break;

                }
                break;

            case 4:
                switch (mReadState) {
                    case IN_ITEMS_ARRAY:
                        if (qName.equals(DICT_TAG)) {
                            mReadState = ReadState.IN_ITEM_DICT;
                            mCurrentItem = new AppSearchResult();
                        }
                        break;
                }
                break;

            case 5:
                if (mReadState == ReadState.IN_ITEM_DICT) {
                    // clear the character string for all tags here
                    mCharacters = new String();
                } else if (mReadState == ReadState.SEEN_ARTWORK_KEY) {
                    if (qName.equals(ARRAY_TAG)) {
                        mReadState = ReadState.IN_ARTWORK_ARRAY;
                    }
                }
                break;

            case 7:
                if (mReadState == ReadState.IN_ARTWORK_ARRAY) {
                    // clear the character string for all tags here
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

        switch (mDepth) {
            case 3:
                switch (mReadState) {
                    case IN_KEY_DEPTH_3:
                        if (qName.equals(KEY_TAG)) {
                            mCurrentKey = mCharacters;

                            if (mCurrentKey.equals(ITEMS_KEY)) {
                                mReadState = ReadState.SEEN_ITEMS_KEY;
                            } else {
                                mReadState = ReadState.IDLE;
                            }
                        }
                        break;

                    case IN_ITEMS_ARRAY:
                        if (qName.equals(ARRAY_TAG)) {
                            mReadState = ReadState.COMPLETE;
                            mCharacters = null;
                        }
                        break;
                }
                break;

            case 4:
                switch (mReadState) {
                    case IN_ITEM_DICT:
                        if (qName.equals(DICT_TAG)) {
                            mReadState = ReadState.IN_ITEMS_ARRAY;
                            mApps.add(mCurrentItem);
                            mCurrentItem = null;
                        }
                        break;
                }
                break;

            case 5:
                if (mReadState == ReadState.IN_ITEM_DICT) {
                    if (qName.equals(KEY_TAG)) {
                        // a key
                        mCurrentKey = mCharacters;

                        if (mCurrentKey.equals(ARTWORK_URLS_KEY)) {
                            mReadState = ReadState.SEEN_ARTWORK_KEY;
                        }
                    } else {
                        // value
                        if (mCurrentKey.equals(TITLE_KEY)) {
                            mCurrentItem.mName = mCharacters;
                        } else if (mCurrentKey.equals(ITEM_ID_KEY)) {
                            try {
                                mCurrentItem.mAppCode = Integer.parseInt(mCharacters);
                            } catch (Exception e) {
                            }
                        } else if (mCurrentKey.equals(URL_KEY)) {
                            mCurrentItem.mAppURL = mCharacters;
                        } else if (mCurrentKey.equals(ARTIST_KEY)) {
                            mCurrentItem.mArtist = mCharacters;
                        }
                    }
                } else if (mReadState == ReadState.IN_ARTWORK_ARRAY) {
                    if (qName.equals(ARRAY_TAG)) {
                        mReadState = ReadState.IN_ITEM_DICT;
                    }
                }
                break;

            case 7:
                if (mReadState == ReadState.IN_ARTWORK_ARRAY) {
                    if (qName.equals(KEY_TAG)) {
                        // a key
                        mCurrentKey = mCharacters;
                    } else {
                        if (mCurrentKey.equals(URL_KEY)) {
                            mCurrentItem.mArtURL = mCharacters;

                        }
                    }
                }
                break;

        }
        mDepth -= 1;
    }

    @Override
    public void characters(char[] ch,
            int start,
            int length)
            throws SAXException {
        if (mReadState != ReadState.COMPLETE) {
            String add_on = new String(ch, start, length);
            mCharacters = mCharacters.concat(add_on);
        }
    }

    //
    //
    //
    public List<AppSearchResult> getApps() {
        return mApps;
    }

    public AppSearchReader(String xml) {
        //mApps = new ArrayList<SearchTermResult>();
        mReadState = ReadState.IDLE;
        mCharacters = new String();
        mDepth = 0;

        mApps = new LinkedList<AppSearchResult>();

        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(new InputSource(new StringReader(xml)), this);
        } catch (Exception e) {
        }

        //System.out.println("Apps XML:\n" + xml);
        //System.out.println("Finish state: " + mReadState);

        //System.out.println("Apps found:\n" + mApps);
    }
}
