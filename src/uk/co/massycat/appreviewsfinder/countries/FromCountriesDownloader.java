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

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author ben
 */
public abstract class FromCountriesDownloader extends Thread {
    protected Set<String> mCountries;
    protected boolean mCauseExit;
    protected String mCurrentCode = "";
    protected int mCurrentCountryNum;

    public abstract int getCurrentTotal();

    public int getCountriesCount() {
        return mCountries.size();
    }

    public int getCurrentCountryNumber() {
        return mCurrentCountryNum;
    }

    public String getCurrentCountry() {
        return mCurrentCode;
    }

    protected abstract void doWorkForCountry();

    @Override
    public void interrupt() {
        mCauseExit = true;
        super.interrupt();
    }

    @Override
    public void run() {
        mCurrentCountryNum = 0;
        Iterator<String> country_iterator = mCountries.iterator();

        while (country_iterator.hasNext() && !mCauseExit) {
            mCurrentCode = country_iterator.next();

            doWorkForCountry();

            mCurrentCountryNum += 1;
        }
    }

    public FromCountriesDownloader( Set<String> countries) {
        mCountries = countries;
        mCauseExit = false;

        Iterator<String> iter = countries.iterator();
        if ( iter.hasNext()) {
            mCurrentCode = iter.next();
        }
    }

}
