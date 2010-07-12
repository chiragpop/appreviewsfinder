/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.massycat.appreviewsfinder.reviews;

import java.io.File;
import java.io.FileFilter;

    class CountryReviewsFileFilter implements FileFilter {
        String mCountryCode;

        public boolean accept(File pathname) {
            boolean accept = false;
            if (pathname.isFile()) {
                // accept files of the form <country_code><*><APP_REVIEWS_XML_FILE_SUFFIX>
                // where <*> is zero or more characters
                String filename = pathname.getName();

                int code_length = mCountryCode.length();
                int suffix_length = AppReviewsUtils.APP_REVIEWS_XML_FILE_SUFFIX.length();
                int filename_length = filename.length();
                if (filename_length >= suffix_length + code_length) {
                    String code_string = filename.substring(0, code_length);
                    String end_string = filename.substring(filename_length - suffix_length,filename_length);

                    if (end_string.equals(AppReviewsUtils.APP_REVIEWS_XML_FILE_SUFFIX)) {
                        if ( code_string.equals(mCountryCode)) {
                            accept = true;
                        }
                    }
                }
            }
            return accept;
        }

        public CountryReviewsFileFilter(String country_code) {
            mCountryCode = new String(country_code);
        }
    }


/**
 *
 * @author ben
 */
public class AppReviewsUtils {

    static final public String kAppReviewsCountsSuffix = "_counts.txt";
    static final public String APP_REVIEWS_XML_FILE_SUFFIX = "_reviews.xml";

    static public String makeReviewsFilename(String country_code, int file_number) {
        return country_code + "_" + file_number + APP_REVIEWS_XML_FILE_SUFFIX;
    }

    static public File[] getReviewFilesForCountryInDirectory(String country_code, File directory) {
        return directory.listFiles(new CountryReviewsFileFilter(country_code));
    }
}
