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

import uk.co.massycat.appreviewsfinder.countries.CountriesManager;
import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 *
 * @author ben
 */
public class AppPreferences {
    private static final String APPS_PATH_KEY = "apps-path";
    private static final String COUNTRY_CODE_KEY = "country-code";
    private static final String TRANSLATION_KEY = "translation";
    private static final String EXPORT_PATH_KEY = "export-path";

    private static AppPreferences sSharedPreferences = null;

    private Preferences mUserPrefs;

    protected AppPreferences() {
        mUserPrefs = Preferences.userNodeForPackage(AppPreferences.class);
    }

    public String getAppsPath() {
        String start_dir = System.getProperty("user.home");

        if (Utilities.isWindowsOS()) {
            String test_path = new String(start_dir + File.separator + "My Documents");

            File test_dir = new File(test_path);

            if (test_dir.exists() && test_dir.isDirectory()) {
                start_dir = test_path;
            } else {
                test_path = new String(start_dir + File.separator + "Documents");
                test_dir = new File(test_path);
                if (test_dir.exists() && test_dir.isDirectory()) {
                    start_dir = test_path;
                }
            }
        }

        start_dir += File.separator + "AppReviewsFinder";
        String path = mUserPrefs.get(APPS_PATH_KEY, start_dir);

        return path;
    }

    public void setAppsPath( File path) {
        mUserPrefs.put(APPS_PATH_KEY, path.getAbsolutePath());
    }

    public String getSearchCountry() {
        String default_search = Locale.getDefault().getCountry().toLowerCase();
        return mUserPrefs.get(COUNTRY_CODE_KEY, default_search);
    }

    public void setSearchCountry(String code) {
        mUserPrefs.put(COUNTRY_CODE_KEY, code);
    }

    public Set<String> getReviewCountries() {
        Set<String> review_countries = new HashSet<String>();
        CountriesManager manager = CountriesManager.getManager();
        String[] all_countries = (String[]) manager.getAllCountryCodes().toArray(new String[0]);

        for( int i = 0; i < all_countries.length; i++) {
            boolean enabled = mUserPrefs.getBoolean(all_countries[i], true);

            if ( enabled) {
                review_countries.add(all_countries[i]);
            }
        }

        return review_countries;
    }

    public void setReviewCountries( Set<String> review_countries) {
        CountriesManager manager = CountriesManager.getManager();
        String[] all_countries = (String[]) manager.getAllCountryCodes().toArray(new String[0]);

        for( int i = 0; i < all_countries.length; i++) {
            boolean enabled = false;

            if ( review_countries.contains(all_countries[i])) {
                enabled = true;
            }

            mUserPrefs.putBoolean(all_countries[i], enabled);
        }
    }

    public String getTranslationCountry() {
        return mUserPrefs.get(TRANSLATION_KEY, "gb");
    }

    public void setTranslationCountry( String code) {
        mUserPrefs.put(TRANSLATION_KEY, code);
    }

    public File getExportPath() {
        String path_str = mUserPrefs.get(EXPORT_PATH_KEY, null);

        if ( path_str != null)
            return new File(path_str);
        else
            return null;
    }

    public void setExportPath( File path) {
        mUserPrefs.put(EXPORT_PATH_KEY, path.getAbsolutePath());
    }

    //
    //
    //
    static public synchronized AppPreferences getPreferences() {
        if (sSharedPreferences == null) {
            sSharedPreferences = new AppPreferences();
        }
        return sSharedPreferences;
    }
}

