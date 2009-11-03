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
package uk.co.massycat.appreviewsfinder.google;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author ben
 */
public class Translator {

    static final String HOST = "ajax.googleapis.com";
    static final String PROTOCOL = "http";
    static final String FILE_START_STRING = "/ajax/services/language/translate?v=1.0&langpair=";
    static final String TEXT_VAR = "&q=";

    static private String unescapeCharacters(String original) {
        StringBuffer safe_string = new StringBuffer();

        for (int i = 0; i < original.length(); i++) {
            char cur_char = original.charAt(i);

            if (cur_char == '\\') {
                char next_char = original.charAt(i + 1);
                char actual_char = 0;
                i += 1;

                switch (next_char) {
                    case '"':
                        actual_char = '"';
                        break;

                    case '\\':
                        actual_char = '\\';
                        break;

                    case 't':
                        actual_char = '\t';
                        break;

                    case 'n':
                        actual_char = '\n';
                        break;

                    case 'r':
                        actual_char = '\r';
                        break;

                    case 'b':
                        actual_char = '\b';
                        break;

                    case 'f':
                        actual_char = '\f';
                        break;

                    case 'u':
                        String uni_str = original.substring(i + 1, i + 1 + 4);

                        try {
                            actual_char = (char) Integer.parseInt(uni_str, 16);
                        } catch (Exception e) {
                        }
                        i += 4;
                        break;
                }

                if (actual_char != 0) {
                    safe_string.append(actual_char);
                }
            } else {
                safe_string.append(cur_char);
            }
        }

        return safe_string.toString();
    }

    static public String spaceEncode(String orig_text) {
        return orig_text.replace(" ", "%20");
    }

    static public String urlencode(String orig_text) {
        StringBuffer safe_string = new StringBuffer();
        String[] escape_chars = {
            // % must be first
            "%",
            "$", "&", "+", ",", "/", ":", ";", "=", "?", "@",
            "\'", "<", ">", "#", "{", "}", "|", "^",
            "~", "[", "]", "`", "(", ")"
        };

        for (int i = 0; i < escape_chars.length; i++) {
            String escape_seq = "%" + Integer.toHexString(escape_chars[i].charAt(0));

            orig_text = orig_text.replace(escape_chars[i], escape_seq);
        }

        // change spaces to +'s
        orig_text = orig_text.replace(" ", "+");


        for (int i = 0; i < orig_text.length(); i++) {
            char cur_char = orig_text.charAt(i);
            int char_val = orig_text.codePointAt(i);//Character.getNumericValue(cur_char);
            boolean escape = false;

            if (char_val <= 0x1F || char_val >= 0x7f) {
                escape = true;
            }

            if (escape) {
                String char_str = orig_text.substring(i, i + 1);
                try {
                    byte[] utf8_bytes = char_str.getBytes("UTF-8");

                    for (int j = 0; j < utf8_bytes.length; j++) {
                        String escape_string = Integer.toHexString(((int) utf8_bytes[j]) & 0xff);
                        if (escape_string.length() == 1) {
                            escape_string = "0" + escape_string;
                        }
                        safe_string.append("%" + escape_string);
                    }
                } catch (Exception e) {
                }
            } else {
                safe_string.append(cur_char);
            }
        }

        return safe_string.toString();
    }

    static public String translate(String orig_text, String orig_google_code,
            String trans_google_code) {
        String trans_string = null;
        try {
            //orig_text = "bonjour {\"blanc\"}";

            String safe_text = orig_text;
            //safe_text = spaceEncode(safe_text);
            //safe_text = URLEncoder.encode(safe_text, "UTF-8");
            safe_text = urlencode(safe_text);
            //safe_text = "\"hello\"%20%5c%20%3cworld%3e%20bang";

            String file_string = FILE_START_STRING +
                    orig_google_code + "%7C" + trans_google_code +
                    TEXT_VAR + safe_text;
            URL trans_url = new URL(PROTOCOL, HOST, file_string);

            //System.out.println("Trans url: " + trans_url);

            HttpURLConnection connection = (HttpURLConnection) trans_url.openConnection();

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                byte[] buffer = new byte[100000];
                int offset = 0;

                while (true) {
                    int read = input.read(buffer, offset, buffer.length - offset);

                    if (read > 0) {
                        offset += read;
                    } else {
                        break;
                    }
                }

                if (offset > 0) {
                    String response = new String(buffer, 0, offset, "UTF-8");

                    //System.out.println(response);
                    String RESPONSE_STATUS = "\"responseStatus\":";
                    int response_status_index = response.indexOf(RESPONSE_STATUS);

                    if (response_status_index < 0) {
                        // no response
                        return null;
                    }
                    int end_index = response.indexOf("}", response_status_index);

                    int response_code = 0;
                    try {
                        String response_code_str =
                                response.substring(response_status_index + RESPONSE_STATUS.length(),
                                end_index);
                        response_code_str = response_code_str.trim();
                        response_code = Integer.parseInt(response_code_str);
                    } catch (Exception e) {
                    }

                    if (response_code != 200) {
                        // did not work
                        return null;
                    }

                    // find the translation
                    String TRANSLATION_TAG = "\"translatedText\":";
                    int trans_start = response.indexOf(TRANSLATION_TAG);
                    if (trans_start < 0) {
                        // cannot find translation
                        return null;
                    }

                    int trans_end = response.indexOf("}", trans_start);
                    if (trans_end < 0) {
                        // cannot find translation
                        return null;
                    }

                    String trans_text = response.substring(trans_start + TRANSLATION_TAG.length(),
                            trans_end);


                    trans_text = trans_text.trim();
                    if (trans_text.length() <= 2) {
                        return null;
                    }
                    trans_text = trans_text.substring(1, trans_text.length() - 1);

                    //System.out.println(trans_text);


                    trans_string = unescapeCharacters(trans_text);

                    //System.out.println(trans_string);
                }

                //System.out.println();
            } else {
                // fail to get translation
                System.err.println("Failed to get translation: " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Translation error: " + e);
        }
        return trans_string;
    }
}
