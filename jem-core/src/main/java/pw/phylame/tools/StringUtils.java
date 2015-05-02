/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.tools;

/**
 * Utility for string operations.
 */
public final class StringUtils {

    /**
     * Joins string array to string.
     * @param <T> the class of the objects in the array
     * @param seq array of object or string
     * @param separator separator between string and string
     * @return joined string
     */
    public static <T> String join(T[] seq, String separator) {
        int iMax = seq.length - 1;
        if (iMax == -1) {
            return "";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(seq[i]);
            if (i == iMax) {
                return b.toString();
            }
            b.append(separator);
        }
    }

    /**
     * Joins string collection to string.
     * @param <T> the class of the objects in the array
     * @param seq collection of object or string
     * @param separator separator between string and string
     * @return joined string
     */
    public static <T> String join(java.util.Collection<T> seq, String separator) {
        int iMax = seq.size() - 1;
        if (iMax == -1) {
            return "";
        }

        StringBuilder b = new StringBuilder();
        int ix = 0;
        for (T o: seq) {
            b.append(o);
            if (ix++ == iMax) {
                break;
            }
            b.append(separator);
        }
        return b.toString();
    }

    /**
     * Returns a copy of {@code s} converted first char to upper case.
     * @param s the source string
     * @return string which first character is upper
     */
    public static String toCapital(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        sb.setCharAt(0, Character.toUpperCase(s.charAt(0)));
        return sb.toString();
    }

    /**
     * Returns a copy of {@code s} converted each word to capital.
     * @param s the source string
     * @return string which each word is capital
     */
    public static String toTitle(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        StringBuilder sb = new StringBuilder(s);
        boolean isFirst = true;
        int length = sb.length();
        for (int i=0; i < length; ++i) {
            char ch = sb.charAt(i);
            if (! Character.isLetter(ch)) {
                isFirst = true;
            } else if (isFirst) {
                sb.setCharAt(i, Character.toUpperCase(ch));
                isFirst = false;
            }
        }
        return sb.toString();
    }

    /**
     * Tests if all characters of specified string are upper case.
     * @param s the string
     * @return <tt>true</tt> if all characters are upper case or <tt>false</tt> if contains
     *          lower case character(s)
     */
    public static boolean isLowerCase(String s) {
        for (int i=0; i<s.length(); ++i) {
            /* found upper case */
            if (Character.isUpperCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if all characters of specified string are lower case.
     * @param s the string
     * @return <tt>true</tt> if all characters are lower case or <tt>false</tt> if contains
     *          upper case character(s)
     */
    public static boolean isUpperCase(String s) {
        for (int i=0; i<s.length(); ++i) {
            /* found lower case */
            if (Character.isLowerCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests <code>s</code> is empty or not, empty string is null or length == 0
     * @param s the string
     * @return <code>true</code> if <code>s</code> is empty, otherwise <code>false</code>
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

}
