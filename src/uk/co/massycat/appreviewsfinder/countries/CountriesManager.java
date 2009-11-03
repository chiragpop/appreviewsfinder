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
package uk.co.massycat.appreviewsfinder.countries;

import java.net.URL;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author ben
 */
public class CountriesManager extends DefaultHandler {

    private static CountriesManager sSharedManager = null;
    private static final String PLIST_TAG = "plist";
    private static final String DICT_TAG = "dict";
    private static final String KEY_TAG = "key";
    private static final String NAME_KEY = "Name";
    private static final String ITUNE_CODE_KEY = "iTunesCode";
    private static final String GOOGLE_CODE_KEY = "GoogleCode";

    private static final String DEFAULT_COUNTRY_CODE = "us";

    enum ReadState {

        IDLE,
        IN_PLIST,
        IN_MAIN_DICT,
        SEEN_COUNTRIES_KEY,
        IN_COUNTRIES_DICT,
        SEEN_COUNTRY_KEY,
        IN_COUNTRY_DICT,
        IN_COUNTRY_KEY,
        IN_COUNTRY_VALUE
    };
    private TreeMap<String, CountryEntry> mDictionary = new TreeMap<String, CountryEntry>();
    private ReadState mReadState = ReadState.IDLE;
    private String mCurrentKey;
    private String mCurrentValue;
    private CountryEntry mCurrentCountry;
    private String mCurrentCountryCode;

    @Override
    public void startElement(String uri,
            String localName,
            String qName,
            Attributes attributes)
            throws SAXException {
        switch ( mReadState) {
            case IDLE:
                if ( qName.equals(PLIST_TAG)) {
                    mReadState = ReadState.IN_PLIST;
                }
                break;

            case IN_PLIST:
                if ( qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_MAIN_DICT;
                }
                break;

            case IN_MAIN_DICT:
                break;

            case SEEN_COUNTRIES_KEY:
                if ( qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_COUNTRIES_DICT;
                }
                break;

            case IN_COUNTRIES_DICT:
                if ( qName.equals(KEY_TAG)) {
                    mCurrentKey = new String();
                }
                break;

            case SEEN_COUNTRY_KEY:
                if ( qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_COUNTRY_DICT;
                }
                break;

            case IN_COUNTRY_DICT:
                if ( qName.equals(KEY_TAG)) {
                    mCurrentKey = new String();
                    mCurrentValue = null;
                    mReadState = ReadState.IN_COUNTRY_KEY;
                }
                else if ( qName.equals("string") || qName.equals("integer")) {
                    mCurrentValue = new String();
                    mReadState = ReadState.IN_COUNTRY_VALUE;
                }
                break;
        }
    }


    @Override
    public void endElement(String uri,
            String localName,
            String qName)
            throws SAXException {
        switch ( mReadState) {
            case IDLE:
                break;

            case IN_PLIST:
                if ( qName.equals(PLIST_TAG)) {
                    mReadState = ReadState.IDLE;
                }
                break;

            case IN_MAIN_DICT:
                if ( qName.equals(PLIST_TAG)) {
                    mReadState = ReadState.IN_PLIST;
                }
                else if ( qName.equals(KEY_TAG)) {
                    mReadState = ReadState.SEEN_COUNTRIES_KEY;
                }
                break;

            case SEEN_COUNTRIES_KEY:
                break;

            case IN_COUNTRIES_DICT:
                if ( qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_MAIN_DICT;
                }
                else if ( qName.equals(KEY_TAG)) {
                    // create the country here
                    mCurrentCountry = new CountryEntry();
                    mCurrentCountryCode = mCurrentKey;
                    mReadState = ReadState.SEEN_COUNTRY_KEY;
                }
                break;

            case SEEN_COUNTRY_KEY:
                break;

            case IN_COUNTRY_DICT:
                if ( qName.equals(DICT_TAG)) {
                    mReadState = ReadState.IN_COUNTRIES_DICT;

                    if ( mCurrentCountryCode != null &&
                            mCurrentCountry != null) {
                        // get the flag icon
                        String icon_resource = "/uk/co/massycat/appreviewsfinder/resources/flags/" + mCurrentCountryCode + ".png";
                        mCurrentCountry.mFlag = new ImageIcon(getClass().getResource(icon_resource));
                        mDictionary.put(mCurrentCountryCode, mCurrentCountry);
                    }
                    mCurrentCountryCode = null;
                    mCurrentCountry = null;
                }
                break;

            case IN_COUNTRY_KEY:
                if ( qName.equals(KEY_TAG)) {
                    mReadState = ReadState.IN_COUNTRY_DICT;
                }
                break;

            case IN_COUNTRY_VALUE:
                if ( qName.equals("string") || qName.equals("integer")) {
                    if ( mCurrentKey.equals(NAME_KEY)) {
                        mCurrentCountry.mName = mCurrentValue;
                    }
                    else if ( mCurrentKey.equals(ITUNE_CODE_KEY)) {
                        int code = -1;

                        try {
                            code = Integer.parseInt(mCurrentValue);
                        }
                        catch ( Exception e) {

                        }

                        mCurrentCountry.mITunesCode = code;
                    }
                    else if ( mCurrentKey.equals(GOOGLE_CODE_KEY)) {
                        mCurrentCountry.mGoogleCode = mCurrentValue;
                    }

                    mReadState = ReadState.IN_COUNTRY_DICT;
                }
                break;
        }
    }

    @Override
    public void characters(char[] ch,
            int start,
            int length)
            throws SAXException {
        switch ( mReadState) {
            case IDLE:
                break;

            case IN_PLIST:
                break;

            case IN_MAIN_DICT:
                break;

            case SEEN_COUNTRIES_KEY:
                break;

            case IN_COUNTRIES_DICT:
            {
                String add_on = new String(ch, start, length);
                mCurrentKey = mCurrentKey.concat(add_on);
            }
                break;

            case SEEN_COUNTRY_KEY:
                break;

            case IN_COUNTRY_DICT:
                break;

            case IN_COUNTRY_KEY:
                if ( mCurrentKey != null) {
                    String add_on = new String(ch, start, length);
                    mCurrentKey = mCurrentKey.concat(add_on);
                }
                break;

            case IN_COUNTRY_VALUE:
                if ( mCurrentValue != null) {
                    String add_on = new String(ch, start, length);
                    mCurrentValue = mCurrentValue.concat(add_on);
                }
                break;
        }
    }


    protected CountriesManager() {
        URL countries_xml_url = getClass().getResource("/uk/co/massycat/appreviewsfinder/resources/Countries.plist");

        // read in the country values
        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(countries_xml_url.toURI().toString(), this);
        }
        catch ( Exception e) {}

        mCurrentKey = null;
        mCurrentValue = null;
        mCurrentCountry = null;
        mCurrentCountryCode = null;

        //System.out.println("Countries:\n" + mDictionary);
    }


    public Set<String> getAllCountryCodes() {
        return mDictionary.keySet();
    }

    public int getCountriesCount() {
        return mDictionary.size();
    }

    public boolean isCountrySupported( String country_code) {
        CountryEntry country = mDictionary.get(country_code);

        return country != null;
    }

    //
    // Access the information about a country.
    // Functions use the two character country code to search.
    //
    public int getITunesCodeForCountry(String country_code) {
        int code;

        CountryEntry country = mDictionary.get(country_code);

        if (country != null) {
            code = country.mITunesCode;
        }
        else {
            code = mDictionary.get(DEFAULT_COUNTRY_CODE).mITunesCode;
        }

        return code;
    }

    public String getNameForCountry(String country_code) {
        String name = null;

        CountryEntry country = mDictionary.get(country_code);

        if (country != null) {
            name = country.mName;
        }
        else {
            name = mDictionary.get(DEFAULT_COUNTRY_CODE).mName;
        }

        return name;
    }

    public String getGoogleCodeForCountry(String country_code) {
        String code = null;

        CountryEntry country = mDictionary.get(country_code);

        if (country != null) {
            code = country.mGoogleCode;
        }

        return code;
    }

    public Icon getFlagIconForCountry(String country_code) {
        Icon icon = null;

        CountryEntry country = mDictionary.get(country_code);

        if (country != null) {
            icon = country.mFlag;
        }
        else {
            icon = mDictionary.get(DEFAULT_COUNTRY_CODE).mFlag;
        }

        return icon;
    }

    public CountryEntry getCountryEntry( String country_code) {
        CountryEntry entry = null;

        CountryEntry country = mDictionary.get(country_code);

        if ( country != null) {
            entry = new CountryEntry(country);
        }
        else {
            entry = mDictionary.get(DEFAULT_COUNTRY_CODE);
        }

        return entry;
    }

    //
    //
    //
    static public synchronized CountriesManager getManager() {
        if (sSharedManager == null) {
            sSharedManager = new CountriesManager();
        }
        return sSharedManager;
    }
}
