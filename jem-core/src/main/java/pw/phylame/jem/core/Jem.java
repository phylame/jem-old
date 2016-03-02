/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.core;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import pw.phylame.jem.util.*;

/**
 * This class contains utility methods for book operations.
 */
public final class Jem {
    /**
     * Version message.
     */
    public static final String VERSION = "2.4";

    /**
     * Vendor message.
     */
    public static final String VENDOR = "PW";

    /**
     * The default format of Jem.
     */
    public static final String PMAB = "pmab";

    /**
     * Gets the format of specified file path.
     *
     * @param path the path string
     * @return string represent the format
     */
    public static String formatByExtension(String path) {
        return BookHelper.nameOfExtension(IOUtils.getExtension(path).toLowerCase());
    }

    public static Parser getParser(String format) throws UnsupportedFormatException {
        if (format == null) {
            throw new NullPointerException();
        }

        Parser parser;
        try {
            parser = BookHelper.getParser(format);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new UnsupportedFormatException(format, "Unsupported format '" + format + '\'', e);
        }
        if (parser == null) {
            throw new UnsupportedFormatException(format, "Unsupported format '" + format + '\'');
        }
        return parser;
    }

    /**
     * Reads <tt>Book</tt> from book file.
     *
     * @param input     book file to be read
     * @param format    format of the book file
     * @param arguments arguments to parser
     * @return <tt>Book</tt> instance represents the book file
     * @throws NullPointerException if the file or format is <tt>null</tt>
     * @throws IOException          if occurs I/O errors
     * @throws JemException         if occurs errors when parsing book file
     */
    public static Book readBook(File input, String format, Map<String, Object> arguments)
            throws IOException, JemException {
        if (input == null) {
            throw new NullPointerException("input");
        }
        return getParser(format).parse(input, arguments);
    }

    public static Maker getMaker(String format) throws UnsupportedFormatException {
        if (format == null) {
            throw new NullPointerException();
        }

        Maker maker;
        try {
            maker = BookHelper.getMaker(format);
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            throw new UnsupportedFormatException(format, "Unsupported format '" + format + "'", e);
        }
        if (maker == null) {
            throw new UnsupportedFormatException(format, "Unsupported format '" + format + "'");
        }
        return maker;
    }

    /**
     * Writes <tt>Book</tt> to book with specified format.
     *
     * @param book      the <tt>Book</tt> to be written
     * @param output    output book file
     * @param format    output format
     * @param arguments arguments to maker
     * @throws NullPointerException if the book, output or format is <tt>null</tt>
     * @throws IOException          if occurs I/O errors
     * @throws JemException         if occurs errors when making book file
     */
    public static void writeBook(Book book, File output, String format, Map<String, Object> arguments)
            throws IOException, JemException {
        if (book == null) {
            throw new NullPointerException("book");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        getMaker(format).make(book, output, arguments);
    }

    /**
     * Converts specified chapter to <tt>Book</tt> instance.
     * <p>Attributes and sub-chapter of specified chapter will be copied to
     * the new book.</p>
     * <p><strong>NOTE:</strong> the returned book is recommended for
     * only temporary using as it change the child-parent relation of <tt>chapter</tt>,
     * and the book should not call {@link Chapter#cleanup()} method.
     *
     * @param chapter the chapter to be converted
     * @return the new book
     * @throws NullPointerException if the chapter is <tt>null</tt>
     */
    @Deprecated
    public static Book toBook(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        return new Book(chapter);
    }

    /**
     * Finds child chapter with specified index from chapter sub-chapter tree.
     *
     * @param chapter the <tt>Chapter</tt> to be indexed
     * @param indices list of index in sub-chapter tree
     * @return the <tt>Chapter</tt>, never <tt>null</tt>
     * @throws NullPointerException      if the chapter or indices is <tt>null</tt>
     * @throws IndexOutOfBoundsException if the index in indices is out of
     *                                   range (index &lt; 0 || index &ge; size())
     */
    public static Chapter locate(Chapter chapter, int[] indices) {
        if (chapter == null) {
            throw new NullPointerException("chapter");
        }
        if (indices == null) {
            throw new NullPointerException("indices");
        }
        for (int index : indices) {
            if (index < 0) {    // find from end
                index = chapter.size() + index;
            }
            try {
                chapter = chapter.chapterAt(index);
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException(chapter.getTitle() + ": " + e.getMessage());
            }
        }
        return chapter;
    }

    /**
     * Returns the depth of sub-chapter tree in specified chapter.
     *
     * @param chapter the chapter
     * @return depth of the chapter
     * @throws NullPointerException if the chapter is <tt>null</tt>
     */
    public static int depthOf(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }

        if (!chapter.isSection()) {
            return 0;
        }

        int depth = 0;
        for (Chapter sub : chapter) {
            int d = depthOf(sub);
            if (d > depth) {
                depth = d;
            }
        }

        return depth + 1;
    }

    /**
     * Filter for matching <tt>Chapter</tt>.
     */
    public interface Filter {
        /**
         * Tests the specified chapter is wanted or not.
         *
         * @param chapter the chapter
         * @return <tt>true</tt> if the chapter is matched
         */
        boolean accept(Chapter chapter);
    }

    /**
     * Finds a matched sub-chapter from specified chapter with filter.
     *
     * @param chapter   the parent chapter
     * @param filter    the filter
     * @param from      begin index of sub-chapter to be filtered in <tt>chapter</tt>
     * @param recursion <tt>true</tt> to find sub-chapter(s) of <tt>chapter</tt>
     * @return the first matched chapter or <tt>null</tt> if no matched found
     */
    public static Chapter find(Chapter chapter, Filter filter, int from, boolean recursion) {
        Chapter ch;
        for (int ix = from; ix < chapter.size(); ++ix) {
            ch = chapter.chapterAt(ix);
            if (filter.accept(ch)) {
                return ch;
            }
            if (ch.isSection() && recursion) {
                ch = find(ch, filter, 0, true);
                if (ch != null) {
                    return ch;
                }
            }
        }
        return null;
    }

    /**
     * Selected sub-chapter from specified chapter with specified condition.
     *
     * @param chapter   the parent chapter
     * @param filter    the filter
     * @param result    store matched chapters
     * @param limit     limits of matched chapters
     * @param recursion <tt>true</tt> to find sub-chapter(s) of <tt>chapter</tt>
     * @return the number of found chapter(s)
     */
    public static int select(Chapter chapter, Filter filter, List<Chapter> result, int limit,
                             boolean recursion) {
        int count = 0;
        for (Chapter c : chapter) {
            if (filter.accept(c)) {
                result.add(c);
                if (++count == limit) {
                    break;
                }
            }
            if (c.isSection() && recursion) {
                count += select(c, filter, result, limit, true);
            }
        }
        return count;
    }

    // declare variant type aliases
    public static final String FILE = "file";
    public static final String TEXT = "text";
    public static final String STRING = "str";
    public static final String INTEGER = "int";
    public static final String REAL = "real";
    public static final String LOCALE = "locale";
    public static final String DATETIME = "datetime";
    public static final String BOOLEAN = "bool";

    /**
     * Returns supported types by Jem.
     *
     * @return array of type names
     */
    public static String[] supportedTypes() {
        return new String[]{FILE, TEXT, STRING, INTEGER, REAL, LOCALE, DATETIME, BOOLEAN};
    }

    private static final HashMap<Class<?>, String> variantTypes = new HashMap<>();

    static {
        variantTypes.put(Character.class, STRING);
        variantTypes.put(String.class, STRING);
        variantTypes.put(Date.class, DATETIME);
        variantTypes.put(Locale.class, LOCALE);
        variantTypes.put(Byte.class, INTEGER);
        variantTypes.put(Short.class, INTEGER);
        variantTypes.put(Integer.class, INTEGER);
        variantTypes.put(Long.class, INTEGER);
        variantTypes.put(Boolean.class, BOOLEAN);
        variantTypes.put(Float.class, REAL);
        variantTypes.put(Double.class, REAL);
    }

    public static void mapVariantType(Class<?> clazz, String type) {
        variantTypes.put(clazz, type);
    }

    /**
     * Returns type name of specified object.
     *
     * @param o attribute value
     * @return the type name
     * @throws NullPointerException if the obj is <tt>null</tt>
     */
    public static String typeOfVariant(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        Class<?> clazz = o.getClass();
        String name = variantTypes.get(clazz);
        if (name != null) {
            return name;
        }
        if (TextObject.class.isAssignableFrom(clazz)) {
            return TEXT;
        } else if (FileObject.class.isAssignableFrom(clazz)) {
            return FILE;
        } else {
            return clazz.getName();
        }
    }

    private static final HashMap<String, String> attributeTypes = new HashMap<>();

    static {
        attributeTypes.put(Attributes.COVER, FILE);
        attributeTypes.put(Attributes.INTRO, TEXT);
        attributeTypes.put(Attributes.WORDS, INTEGER);
        attributeTypes.put(Attributes.DATE, DATETIME);
        attributeTypes.put(Attributes.LANGUAGE, LOCALE);
    }

    public static void mapAttributeType(String name, String type) {
        attributeTypes.put(name, type);
    }

    /**
     * Returns type of specified attribute name.
     *
     * @param name name of attribute
     * @return the type string
     */
    public static String typeOfAttribute(String name) {
        String type = attributeTypes.get(name);
        return type != null ? type : STRING;
    }

    /**
     * Returns default value of specified type.
     *
     * @param type type string
     * @return the value
     */
    public static Object defaultOfType(String type) {
        switch (type) {
            case STRING:
                return "";
            case TEXT:
                return TextFactory.emptyText();
            case FILE:
                return FileFactory.emptyFile();
            case DATETIME:
                return new Date();
            case LOCALE:
                return Locale.getDefault();
            case INTEGER:
                return 0;
            case REAL:
                return 0.0D;
            case BOOLEAN:
                return false;
            default:
                return "";
        }
    }

    /**
     * Converts specified object to string.
     *
     * @param o the object
     * @return a string represent the object
     */
    public static String formatVariant(Object o) {
        if (o == null) {
            return "";
        }
        switch (typeOfVariant(o)) {
            case TEXT:
                try {
                    return ((TextObject) o).getText();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            case DATETIME:
                return new SimpleDateFormat("yy-M-d").format((Date) o);
            case LOCALE:
                return ((Locale) o).getDisplayName();
            default:
                return String.valueOf(o);
        }
    }
}
