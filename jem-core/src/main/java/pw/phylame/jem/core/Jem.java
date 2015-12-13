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

package pw.phylame.jem.core;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.util.UnsupportedFormatException;

/**
 * This class contains utility methods for book operations.
 */
public final class Jem {
    private static final Log LOG = LogFactory.getLog(Jem.class);

    /**
     * Jem version
     */
    public static final String VERSION = "2.3";

    /**
     * Jem vendor
     */
    public static final String VENDOR = "PW";

    /**
     * Format of Pem default format
     */
    public static final String PMAB_FORMAT = "pmab";

    public static Parser getParser(String format) throws UnsupportedFormatException {
        if (format == null) {
            throw new NullPointerException("format");
        }

        Parser parser;
        try {
            parser = BookHelper.getParser(format);
        } catch (IllegalAccessException e) {
            format = format.toUpperCase();
            LOG.debug("cannot access parser class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (InstantiationException e) {
            format = format.toUpperCase();
            LOG.debug("cannot create parser instance for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (ClassNotFoundException e) {
            format = format.toUpperCase();
            LOG.debug("not found parser class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (UnsupportedFormatException e) {
            format = format.toUpperCase();
            LOG.debug("no registered parser class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        }

        return parser;
    }

    /**
     * Reads <tt>Book</tt> from book file.
     *
     * @param file   book file to be read
     * @param format format of the book file
     * @param kw     arguments to parser
     * @return <tt>Book</tt> instance represents the book file
     * @throws NullPointerException             if the file or format is <tt>null</tt>
     * @throws java.io.IOException              occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when parsing book file
     */
    public static Book readBook(File file, String format, Map<String, Object> kw)
            throws IOException, JemException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        Parser parser = getParser(format);
        return parser.parse(file, kw);
    }

    public static Maker getMaker(String format) throws UnsupportedFormatException {
        if (format == null) {
            throw new NullPointerException("format");
        }

        Maker maker;
        try {
            maker = BookHelper.getMaker(format);
        } catch (IllegalAccessException e) {
            format = format.toUpperCase();
            LOG.debug("cannot access maker class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (InstantiationException e) {
            format = format.toUpperCase();
            LOG.debug("cannot create maker instance for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (ClassNotFoundException e) {
            format = format.toUpperCase();
            LOG.debug("not found maker class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        } catch (UnsupportedFormatException e) {
            format = format.toUpperCase();
            LOG.debug("no registered maker class for " + format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: " + format, e);
        }

        return maker;
    }

    /**
     * Writes <tt>Book</tt> to book with specified format.
     *
     * @param book   the <tt>Book</tt> to be written
     * @param output output book file
     * @param format output format
     * @param kw     arguments to maker
     * @throws NullPointerException             if the book, output or format is <tt>null</tt>
     * @throws java.io.IOException              occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when making book file
     */
    public static void writeBook(Book book, File output, String format, Map<String, Object> kw)
            throws IOException, JemException {
        if (book == null) {
            throw new NullPointerException("book");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        Maker maker = getMaker(format);
        maker.make(book, output, kw);
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
    public static Chapter chapterForIndex(Chapter chapter, int[] indices) {
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
                throw new IndexOutOfBoundsException(
                        chapter.getTitle() + ": " + e.getMessage());
            }
        }
        return chapter;
    }

    /**
     * Converts specified chapter to <tt>Book</tt> instance.
     * <p>Attributes and sub-parts of specified chapter will be copied to
     * the new book.</p>
     * <p><strong>NOTE:</strong> the returned book is recommended for
     * only temporary using as it change the child-parent relation of <tt>chapter</tt>,
     * and the book should not call {@link Chapter#cleanup()} method.
     *
     * @param chapter the chapter to be converted
     * @return the new book
     * @throws NullPointerException if the chapter is <tt>null</tt>
     */
    public static Book chapterToBook(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        return new Book(chapter);
    }

    /**
     * Returns the depth of sub-parts tree in specified chapter.
     *
     * @param chapter the chapter
     * @return depth of the chapter
     * @throws NullPointerException if the chapter is <tt>null</tt>
     */
    public static int depthOfChapter(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }

        if (!chapter.isSection()) {
            return 0;
        }

        int depth = 0;
        for (Chapter sub : chapter) {
            int d = depthOfChapter(sub);
            if (d > depth) {
                depth = d;
            }
        }

        return depth + 1;
    }

    public static final Map<Class<?>, String> variantTypes = new HashMap<Class<?>, String>();

    static {
        variantTypes.put(Character.class, "str");
        variantTypes.put(String.class, "str");
        variantTypes.put(CharSequence.class, "str");
        variantTypes.put(Date.class, "datetime");
        variantTypes.put(Byte.class, "int");
        variantTypes.put(Short.class, "int");
        variantTypes.put(Integer.class, "int");
        variantTypes.put(Long.class, "int");
        variantTypes.put(Boolean.class, "bool");
        variantTypes.put(Float.class, "real");
        variantTypes.put(Double.class, "real");
        variantTypes.put(Number.class, "real");
    }

    /**
     * Returns type name of attribute value format for PEM declaration.
     *
     * @param obj attribute value
     * @return the type name
     * @throws NullPointerException if the obj is <tt>null</tt>
     */
    public static String variantType(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        Class<?> clazz = obj.getClass();
        String name = variantTypes.get(clazz);
        if (name != null) {
            return name;
        }
        if (TextObject.class.isAssignableFrom(clazz)) {
            return "text";
        } else if (FileObject.class.isAssignableFrom(clazz)) {
            return "file";
        } else {
            return clazz.getName();
        }
    }
}
