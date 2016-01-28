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

import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.util.TextObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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
        return new SimpleDateFormat(format).format(date);
    }

    public static Date parseDate(String text, String format, Date defaultDate) {
        try {
            return new SimpleDateFormat(format).parse(text);
        } catch (ParseException e) {
            return defaultDate;
        }
    }

    public static Date parseDate(String text, String format) throws ParserException {
        try {
            return new SimpleDateFormat(format).parse(text);
        } catch (ParseException e) {
            throw ExceptionFactory.parserException(e, "error.text.invalidDate", text, format);
        }
    }

    public static String formatLocale(Locale locale) {
        String language = locale.getLanguage(), country = locale.getCountry();
        return isValid(country) ? language + '-' + country : language;
    }

    public static Locale parseLocale(String tag) {
        int index = tag.indexOf('-');
        if (index == -1) {
            index = tag.indexOf('_');
        }
        String language, country;
        if (index == -1) {
            language = tag;
            country = "";
        } else {
            language = tag.substring(0, index);
            country = tag.substring(index + 1);
        }
        return new Locale(language, country);
    }

    /**
     * Tests specified <tt>CharSequence</tt> is empty or not.
     *
     * @param cs the char sequence
     * @return <tt>true</tt> if <code>cs</code> is <tt>null</tt> or length = 0, otherwise <tt>false</tt>
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * Tests <code>cs</code> is valid or not, a valid string is not null and length > 0
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <code>true</code> if <code>cs</code> is not null and length > 0,
     * otherwise <code>false</code>
     */
    public static boolean isValid(CharSequence cs) {
        return cs != null && cs.length() != 0;
    }

    public static String toString(CharSequence cs) {
        return cs != null ? cs.toString() : null;
    }

    /**
     * Returns a copy of {@code cs} that first letter was converted to upper case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return string which first character is upper
     */
    public static String capitalized(CharSequence cs) {
        if (isEmpty(cs)) {
            return toString(cs);
        }
        return Character.toUpperCase(cs.charAt(0)) + cs.subSequence(1, cs.length()).toString().toLowerCase();
    }

    public static String camelized(CharSequence cs) {
        if (isEmpty(cs)) {
            return toString(cs);
        }
        return Character.toLowerCase(cs.charAt(0)) + cs.subSequence(1, cs.length()).toString();
    }

    /**
     * Returns a copy of {@code cs} that each word was converted to capital.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return string which each word is capital
     */
    public static String titled(CharSequence cs) {
        if (isEmpty(cs)) {
            return toString(cs);
        }
        StringBuilder sb = new StringBuilder(cs.length());
        boolean isFirst = true;
        int length = cs.length();
        for (int i = 0; i < length; ++i) {
            char ch = cs.charAt(i);
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
     * Like {@link String#trim()} but removes Chinese paragraph prefix (u3000).
     *
     * @param cs the input string
     * @return the string removed space
     */
    public static String trimmed(CharSequence cs) {
        int len = cs.length();
        int st = 0;

        char ch;
        while ((st < len) && (((ch = cs.charAt(st)) <= ' ') || (ch == CHINESE_INDENT))) {
            st++;
        }
        while ((st < len) && (((ch = cs.charAt(len - 1)) <= ' ') || (ch == CHINESE_INDENT))) {
            len--;
        }
        return toString(((st > 0) || (len < cs.length())) ? cs.subSequence(st, len) : cs);
    }

    /**
     * Tests if all characters of specified string are upper case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are upper case or
     * <tt>false</tt> if contains lower case character(s)
     */
    public static boolean isLowerCase(CharSequence cs) {
        for (int i = 0; i < cs.length(); ++i) {
            /* found upper case */
            if (Character.isUpperCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if all characters of specified string are lower case.
     *
     * @param cs a <tt>CharSequence</tt> represent string
     * @return <tt>true</tt> if all characters are lower case or
     * <tt>false</tt> if contains upper case character(s)
     */
    public static boolean isUpperCase(CharSequence cs) {
        for (int i = 0; i < cs.length(); ++i) {
            /* found lower case */
            if (Character.isLowerCase(cs.charAt(i))) {
                return false;
            }
        }
        return true;
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

    public static String fetchText(TextObject text, String defaultText) {
        try {
            return text.getText();
        } catch (Exception e) {
            return defaultText;
        }
    }

    public static List<String> fetchLines(TextObject text, boolean skipEmpty) {
        try {
            return text.getLines(skipEmpty);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static String plainText(TextObject text, TextConverter converter) {
        if (!TextObject.PLAIN.equals(text.getType())) {
            return styledText(text, converter);
        } else {
            return fetchText(text, null);
        }
    }

    public static String styledText(TextObject text, TextConverter converter) {
        return converter != null ? converter.getText(text) : fetchText(text, null);
    }

    public static List<String> plainLines(TextObject text, boolean skipEmpty, TextConverter converter) {
        if (!TextObject.PLAIN.equals(text.getType())) {
            return styledLines(text, skipEmpty, converter);
        } else {
            return fetchLines(text, skipEmpty);
        }
    }

    public static List<String> styledLines(TextObject text, boolean skipEmpty, TextConverter converter) {
        return converter != null ? converter.getLines(text, skipEmpty) : fetchLines(text, skipEmpty);
    }
}
