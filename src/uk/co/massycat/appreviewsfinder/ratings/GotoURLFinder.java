/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.co.massycat.appreviewsfinder.ratings;

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
public class GotoURLFinder {
    enum ReaderState {
        kLookingForKey,
        kLookingForKindKeyValue,
        kLookingForKindString,
        kLookingForKindStringValue,
        kLookingForUrlKey,
        kLookingForUrlKeyValue,
        kLookingForUrlString,
        kLookingForUrlStringValue,
        kComplete
    };
    String mGotoUrl = null;

    class XmlReader extends DefaultHandler {
        int mLevel = 0;
        final static String kKeyElement = "key";
        final static String kStringElement = "string";

        final static String kKindValue = "kind";
        final static String kGotoValue = "Goto";
        final static String kUrlValue = "url";

        ReaderState mState = ReaderState.kLookingForKey;

        StringBuffer mValueString = new StringBuffer();


        @Override
        public void startElement(String uri,
                String localName,
                String qName,
                Attributes attributes)
                throws SAXException {
            mLevel += 1;
            switch ( mState) {
                case kLookingForKey:
                    if (qName.equals(kKeyElement)) {
                        mState = ReaderState.kLookingForKindKeyValue;
                    }
                    break;

                case kLookingForKindString:
                    if (qName.equals(kStringElement)) {
                        mState = ReaderState.kLookingForKindStringValue;
                    }
                    break;

                case kLookingForUrlKey:
                    if (qName.equals(kKeyElement)) {
                        mState = ReaderState.kLookingForUrlKeyValue;
                    }
                    break;

                case kLookingForUrlString:
                    if (qName.equals(kStringElement)) {
                        mState = ReaderState.kLookingForUrlStringValue;
                    }
                    break;

                case kLookingForKindKeyValue:
                case kLookingForKindStringValue:
                case kLookingForUrlKeyValue:
                case kLookingForUrlStringValue:
                    // not expected the start of another element
                    mState = ReaderState.kLookingForKey;
                    break;
            }
        }
        @Override
        public void endElement(String uri,
                String localName,
                String qName)
                throws SAXException {
            mLevel -= 1;

            switch ( mState) {
                case kLookingForKindKeyValue: {
                    String value = mValueString.toString();
                    if ( value.equals(kKindValue)) {
                        mState = ReaderState.kLookingForKindString;
                    }
                    else {
                        mState = ReaderState.kLookingForKey;
                    }
                }
                    break;

                case kLookingForKindStringValue: {
                    String value = mValueString.toString();
                    if ( value.equals(kGotoValue)) {
                        mState = ReaderState.kLookingForUrlKey;
                    }
                    else {
                        mState = ReaderState.kLookingForKey;
                    }
                }
                    break;

                case kLookingForUrlKeyValue: {
                    String value = mValueString.toString();
                    if ( value.equals(kUrlValue)) {
                        mState = ReaderState.kLookingForUrlString;
                    }
                    else {
                        mState = ReaderState.kLookingForKey;
                    }
                }
                    break;

                case kLookingForUrlStringValue: {
                    mGotoUrl = mValueString.toString();
                    mState = ReaderState.kComplete;
                }
                    break;
            }
            mValueString.setLength(0);
        }

        @Override
        public void characters(char[] ch,
                       int start,
                       int length)
                throws SAXException {
            switch ( mState) {
                case kLookingForKindKeyValue:
                case kLookingForKindStringValue:
                case kLookingForUrlKeyValue:
                case kLookingForUrlStringValue:
                    mValueString.append(ch, start, length);
                    break;
            }
        }
    }

        public String getGotoUrl() {
            return mGotoUrl;
        }

        public GotoURLFinder( String xml) {
        try {
            SAXParser xml_parser = SAXParserFactory.newInstance().newSAXParser();
            xml_parser.parse(new InputSource(new StringReader(xml)), new XmlReader());
        } catch (Exception e) {
        }
        }
}
