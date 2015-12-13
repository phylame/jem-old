/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pw.phylame.jem.formats.util.text;

import java.util.List;
import java.util.Date;
import java.util.Collection;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;

/**
 * String & text utilities.
 */
public final class TextUtils {
    /**
     * Chinese paragraph indent character.
     */
    public static final char CHINESE_INDENT = '\u3000';

    private TextUtils() {
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static Date parseDate(String text, String format) throws ParserException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(text);
        } catch (ParseException e) {
            throw ExceptionFactory.parserException(e,
                    "error.text.invalidDate", text, format);
        }
    }

    public static String fetchText(TextObject tb) throws MakerException {
        try {
            return tb.getText();
        } catch (Exception e) {
            throw ExceptionFactory.makerException(e, "error.text.fetchText");
        }
    }

    /**
     * Tests <code>str</code> is valid or not, a valid string is not null and length > 0
     *
     * @param str a <tt>CharSequence</tt> represent string
     * @return <code>true</code> if <code>str</code> is not null and length > 0,
     * otherwise <code>false</code>
     */
    public static boolean isValid(CharSequence str) {
        return str != null && str.length() != 0;
    }

    /**
     * Returns a copy of {@code str} that first letter was converted to upper case.
     *
     * @param str a <tt>CharSequence</tt> represent string
     * @return string which first character is upper
     */
    public static CharSequence toCapitalized(CharSequence str) {
        if (!isValid(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) +
                str.subSequence(1, str.length()).toString().toLowerCase();
    }

    public static CharSequence toCamelized(CharSequence str) {
        if (!isValid(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.subSequence(1, str.length()).toString();
    }

    /**
     * Returns a copy of {@code str} that each word was converted to capital.
     *
     * @param str a <tt>CharSequence</tt> represent string
     * @return string which each word is capital
     */
    public static CharSequence toTitled(CharSequence str) {
        if (!isValid(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str.length());
        boolean isFirst = true;
        int length = str.length();
        for (int i = 0; i < length; ++i) {
            char ch = str.charAt(i);
            if (!Character.isLetter(ch)) {
                isFirst = true;
            } else if (isFirst) {
                ch = Character.toUpperCase(ch);
                isFirst = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Tests if all characters of specified string are upper case.
     *
     * @param str a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are upper case or
     * <tt>false</tt> if contains lower case character(s)
     */
    public static boolean isLowerCase(CharSequence str) {
        for (int i = 0; i < str.length(); ++i) {
            /* found upper case */
            if (Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if all characters of specified string are lower case.
     *
     * @param str a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are lower case or
     * <tt>false</tt> if contains upper case character(s)
     */
    public static boolean isUpperCase(CharSequence str) {
        for (int i = 0; i < str.length(); ++i) {
            /* found lower case */
            if (Character.isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Like {@link String#trim()} but removes Chinese paragraph prefix (u3000).
     *
     * @param str the input string
     * @return the string removed space
     */
    public static String trim(String str) {
        int len = str.length();
        int st = 0;
        char[] val = str.toCharArray();

        char ch;
        while ((st < len) && (((ch = val[st]) <= ' ') || (ch == CHINESE_INDENT))) {
            st++;
        }
        while ((st < len) && (((ch = val[len - 1]) <= ' ') || (ch == CHINESE_INDENT))) {
            len--;
        }
        return ((st > 0) || (len < val.length)) ? str.substring(st, len) : str;
    }

    public static <T> String join(CharSequence separator, T[] objects) {
        StringBuilder sb = new StringBuilder();
        int end = objects.length - 1;
        for (int ix = 0; ix < end; ++ix) {
            sb.append(objects[ix].toString()).append(separator);
        }
        return sb.append(objects[end].toString()).toString();
    }

    public static <T> String join(CharSequence separator, Collection<T> objects) {
        StringBuilder sb = new StringBuilder();
        int end = objects.size(), ix = 1;
        for (T object : objects) {
            sb.append(object.toString());
            if (ix++ != end) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static String plainText(TextObject text,
                                   HtmlConverter converter) throws Exception {
        if (TextObject.HTML.equals(text.getType())) {
            return htmlText(text, converter);
        } else {
            return text.getText();
        }
    }

    public static String htmlText(TextObject text, HtmlConverter converter)
            throws Exception {
        if (converter == null) {
            return text.getText();
        }
        return converter.getText(text);
    }

    public static List<String> plainLines(TextObject text, boolean skipEmpty,
                                          HtmlConverter converter) throws Exception {
        if (TextObject.HTML.equals(text.getType())) {
            return htmlLines(text, skipEmpty, converter);
        } else {
            return text.getLines(skipEmpty);
        }
    }

    public static List<String> htmlLines(TextObject text, boolean skipEmpty,
                                         HtmlConverter converter) throws Exception {
        if (converter == null) {
            return text.getLines(skipEmpty);
        }
        // not supported currently
        return converter.getLines(text, skipEmpty);
    }
}
