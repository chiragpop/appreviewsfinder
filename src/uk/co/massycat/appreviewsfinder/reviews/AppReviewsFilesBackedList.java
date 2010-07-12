//
// Copyright (C) 2010 Ben Jaques.
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
import java.util.AbstractList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ben
 */
public class AppReviewsFilesBackedList extends AbstractList<AppReview> {

    int mTotalReviews;
    List<PerFileInfo> mFilesList = null;
    private static final int kNumCachedInfos = 2;
    private int mOldCacheIndex = 0;
    private PerFileInfo[] mCachedInfos = new PerFileInfo[kNumCachedInfos];
    private List<AppReview>[] mCachedReviewLists = new List[kNumCachedInfos];

    private class PerFileInfo {

        public int mStartIndex;
        public int mNumReviews;
        File mFile;
    }

    private PerFileInfo findFileInfo(int index) {
        if (index < 0 || index >= mTotalReviews) {
            return null;
        }

        PerFileInfo info = null;
        int end_index = 0;
        Iterator<PerFileInfo> iterator = mFilesList.iterator();

        while (iterator.hasNext()) {
            PerFileInfo cur_info = iterator.next();

            end_index += cur_info.mNumReviews;

            if (index < end_index) {
                info = cur_info;
                break;
            }
        }

        return info;
    }

    public AppReview get(int index) {
        PerFileInfo info = findFileInfo(index);

        if (info == null) {
            throw new IndexOutOfBoundsException();
        }

        // see if it is cached
        for (int i = 0; i < kNumCachedInfos; i++) {
            if (mCachedInfos[i] == info) {
                if (mOldCacheIndex == i) {
                    mOldCacheIndex = i + 1;
                    if (mOldCacheIndex >= kNumCachedInfos) {
                        mOldCacheIndex = 0;
                    }
                }
                // is in cache
                int review_index = index - info.mStartIndex;
                return mCachedReviewLists[i].get(review_index);
            }
        }

        AppReviewXMLHandler handler = new AppReviewXMLHandler(info.mFile);
        List<AppReview> reviews = handler.getReviews();

        int review_index = index - info.mStartIndex;

        //
        // put reviews into the cache
        //
        mCachedInfos[mOldCacheIndex] = info;
        mCachedReviewLists[mOldCacheIndex] = reviews;
        mOldCacheIndex += 1;
        if (mOldCacheIndex >= kNumCachedInfos) {
            mOldCacheIndex = 0;
        }


        return reviews.get(review_index);
    }

    public int size() {
        return mTotalReviews;
    }

    public AppReviewsFilesBackedList(File[] files) {
        mTotalReviews = 0;
        mFilesList = new LinkedList<PerFileInfo>();

        for (int i = 0; i < kNumCachedInfos; i++) {
            mCachedInfos[i] = null;
            mCachedReviewLists[i] = null;
        }

        for (int i = 0; i < files.length; i++) {
            PerFileInfo info = new PerFileInfo();
            info.mFile = files[i];

            AppReviewXMLHandler handler = new AppReviewXMLHandler(info.mFile);
            List<AppReview> reviews = handler.getReviews();
            info.mNumReviews = reviews.size();
            info.mStartIndex = mTotalReviews;

            mTotalReviews += info.mNumReviews;
            mFilesList.add(info);
        }
    }
}
