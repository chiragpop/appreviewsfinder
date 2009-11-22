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

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author ben
 */
public class Utilities {

    static final String kWindows = "Windows";
    static final String kMacOsX = "Mac OS X";

    static public boolean isMacOSX() {
        boolean is_os_x = false;

        String os_name = System.getProperty("os.name");

        is_os_x = os_name.compareToIgnoreCase(kMacOsX) == 0;

        return is_os_x;
    }

    static public boolean isWindowsOS() {
        boolean windows = false;

        String os_name = System.getProperty("os.name");

        if (os_name.length() >= kWindows.length()) {
            String sub_os = os_name.substring(0, kWindows.length());

            if (sub_os.equals(kWindows)) {
                windows = true;
            }
        }

        return windows;
    }

    static public boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();

            for (int i = 0; i < children.length; i++) {
                boolean sub_good = Utilities.deleteDirectory(new File(directory, children[i]));

                if (!sub_good) {
                    return false;
                }
            }
        }

        return directory.delete();
    }

    public static String connectAndGetResponse(String url_str, String itunes_code) {
        String xml_string = null;
        try {
            URL url = new URL(url_str);

            HttpURLConnection http_conn = (HttpURLConnection) url.openConnection();
            http_conn.setRequestProperty("X-Apple-Store-Front", itunes_code);
            http_conn.setRequestProperty("User-Agent", "iTunes-iPhone/2.2 (2)");

            http_conn.connect();

            //String response = http_conn.getResponseMessage();

            //System.out.println("Response: " + response);
            //System.out.println("Content-type: " + http_conn.getContentType());
            //System.out.println("Content-length: " + http_conn.getContentLength());

            //int length = http_conn.getContentLength();

            if (http_conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                int content_length = http_conn.getContentLength();
                InputStream input_stream = http_conn.getInputStream();
                int alloc_length = content_length;

                //int header_content_length = http_conn.getHeaderFieldInt("Content-Length", -1);
                //System.out.println("Header Content-Length: " + header_content_length);
                //System.out.println("Content length: " + content_length);

//                if ( content_length == -1) {
//                    String header_field;
//                    int field_count = 0;
//                while(true) {
//                    header_field = http_conn.getHeaderField(field_count);
//
//                    if ( header_field != null) {
//                        String key = http_conn.getHeaderFieldKey(field_count++);
//
//                        System.out.println(key + ": " + header_field);
//                    }
//                    else {
//                        break;
//                }
//                }
//                }

                int read_total = 0;
                if (content_length < 0) {
                    alloc_length = 512 * 1024;
                }

                byte[] read_buffer = new byte[alloc_length];
                try {

                    while (read_total < read_buffer.length) {
                        int read = input_stream.read(read_buffer, read_total, read_buffer.length - read_total);

                        if (read > 0) {
                            read_total += read;

                            if (content_length < 0) {
//                                if ( input_stream.available() <= 0) {
//                                    break;
//                                }
                            }
                        } else {
                            break;
                        }
                    }

                } catch (Exception e) {
                }

                if (read_total > 0) {
                    xml_string = new String(read_buffer, 0, read_total, "UTF-8");
                }
            }


            //http_conn.disconnect();
        } catch (Exception e) {
            System.err.println("What went wrong? : " + e);
        }

        return xml_string;
    }

    public class DirsOnlyFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    }
    private DirsOnlyFileFilter mDirsOnlyFileFilter = new DirsOnlyFileFilter();
    static private Utilities sUtilities = null;

    static public DirsOnlyFileFilter getDirsOnlyFileFilter() {
        if (sUtilities == null) {
            sUtilities = new Utilities();
        }

        return sUtilities.mDirsOnlyFileFilter;
    }

    static public String makeStartTag(String tag) {
        return "<" + tag + ">";
    }

    static public String makeEndTag(String tag) {
        return "</" + tag + ">";
    }

    static public String makeElement(String tag, String value) {
        return makeStartTag(tag) +
                value +
                makeEndTag(tag);
    }
}
