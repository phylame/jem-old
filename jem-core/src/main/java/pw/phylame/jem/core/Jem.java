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
import java.util.Objects;
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
    private static Log LOG = LogFactory.getLog(Jem.class);

    /** Jem version */
    public static final String VERSION          = "2.3.0";

    /** Jem vendor */
    public static final String VENDOR           = "PW";

    /** Format of Pem default format */
    public static final String PMAB_FORMAT      = "pmab";

    public static Parser getParser(String format) throws UnsupportedFormatException {
        format = Objects.requireNonNull(format);

        Parser parser;
        try {
            parser = BookHelper.getParser(format);
        } catch (IllegalAccessException e) {
            format = format.toUpperCase();
            LOG.debug("cannot access parser class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (InstantiationException e) {
            format = format.toUpperCase();
            LOG.debug("cannot create parser instance for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (ClassNotFoundException e) {
            format = format.toUpperCase();
            LOG.debug("not found parser class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (UnsupportedFormatException e) {
            format = format.toUpperCase();
            LOG.debug("no registered parser class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        }

        return parser;
    }

    /**
     * Reads <tt>Book</tt> from book file.
     * @param file book file to be read
     * @param format format of the book file
     * @param kw arguments to parser
     * @return <tt>Book</tt> instance represents the book file
     * @throws java.io.IOException occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when parsing book file
     */
    public static Book readBook(File file, String format, Map<String, Object> kw)
            throws IOException, JemException {
        Parser parser = getParser(format);
        return parser.parse(Objects.requireNonNull(file), kw);
    }

    public static Maker getMaker(String format) throws UnsupportedFormatException {
        format = Objects.requireNonNull(format);

        Maker maker;
        try {
            maker = BookHelper.getMaker(format);
        } catch (IllegalAccessException e) {
            format = format.toUpperCase();
            LOG.debug("cannot access maker class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (InstantiationException e) {
            format = format.toUpperCase();
            LOG.debug("cannot create maker instance for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (ClassNotFoundException e) {
            format = format.toUpperCase();
            LOG.debug("not found maker class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        } catch (UnsupportedFormatException e) {
            format = format.toUpperCase();
            LOG.debug("no registered maker class for "+format, e);
            throw new UnsupportedFormatException(format, "Unsupported format: "+format);
        }

        return maker;
    }

    /**
     * Writes <tt>Book</tt> to book with specified format.
     * @param book the <tt>Book</tt> to be written
     * @param output output book file
     * @param format output format
     * @param kw arguments to maker
     * @throws java.io.IOException occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when making book file
     */
    public static void writeBook(Book book, File output, String format, Map<String, Object> kw)
            throws IOException, JemException {
        Maker maker = getMaker(format);
        maker.make(Objects.requireNonNull(book), Objects.requireNonNull(output), kw);
    }

    /**
     * Finds child chapter with specified index from chapter sub-chapter tree.
     * @param owner the <tt>Chapter</tt> to be indexed
     * @param orders list of index in sub-chapter tree
     * @param fromIndex begin position of index in {@code orders}
     * @return the <tt>Chapter</tt>
     * @throws IndexOutOfBoundsException if index in {@code orders} or
     *          {@code fromIndex} is invalid
     */
    public static Chapter findChapter(Chapter owner, int[] orders, int fromIndex) {
        int index = orders[fromIndex];
        if (index >= owner.size()) {
            throw new IndexOutOfBoundsException("Index out of range: "+index);
        }
        Chapter chapter;
        if (index < 0) {
            chapter = owner.get(owner.size()+index);
        } else {
            chapter = owner.get(index);
        }
        if (fromIndex == orders.length-1) {   // last one
            return chapter;
        } else {
            return findChapter(chapter, orders, fromIndex + 1);
        }
    }

    /**
     * Converts specified chapter to <tt>Book</tt> instance.
     * <p>Attributes and sub-parts of specified chapter will be copied to
     *      the new book.</p>
     * @param chapter the chapter to be converted
     * @return the new book
     */
    public static Book toBook(Chapter chapter) {
        Book book = new Book();

        // copy attributes
        book.updateAttributes(chapter);

        // copy sub-parts
        for (Chapter sub: chapter) {
            book.append(sub);
        }

        return book;
    }

    /**
     * Returns the depth of sub-parts tree in specified chapter.
     * @param chapter the chapter
     * @return depth of the chapter
     */
    public static int getDepth(Chapter chapter) {
        if (chapter == null || ! chapter.isSection()) {
            return 0;
        }

        int depth = 0;
        for (Chapter sub: chapter) {
            int d = getDepth(sub);
            if (d > depth) {
                depth = d;
            }
        }

        return depth + 1;
    }


    /**
     * This interface used for walking sub-parts.
     * @since 2.0.1
     */
    public static interface Walker {
        /**
         * Watches the specified chapter.
         * @param chapter the chapter to be watched
         * @return <tt>false</tt> to stop walking
         */
        boolean watch(Chapter chapter);
    }

    /**
     * Walks <tt>Chapter</tt> sub-chapter tree (first order).
     * <p>The specified chapter will be watched after walking all sub-parts.</p>
     * @param chapter the <tt>Chapter</tt> to be watched
     * @param walker watch the chapter
     */
    public static void walkChapter(Chapter chapter, Walker walker) {
        walker = Objects.requireNonNull(walker);
        chapter = Objects.requireNonNull(chapter);
        if (! walker.watch(chapter)) {
            return;
        }
        for (Chapter sub: chapter) {
            walkChapter(sub, walker);
        }
    }

    public static final Map<Class<?>, String> variantTypes =
            new HashMap<Class<?>, String>();
    static {
        variantTypes.put(Character.class, "str");
        variantTypes.put(String.class, "str");
        variantTypes.put(CharSequence.class, "str");
        variantTypes.put(Date.class, "datetime");
        variantTypes.put(Byte.class, "int");
        variantTypes.put(Short.class, "int");
        variantTypes.put(Integer.class, "int");
        variantTypes.put(Long.class, "int");
        variantTypes.put(Boolean.class, "int");
        variantTypes.put(byte[].class, "bytes");
        variantTypes.put(Byte[].class, "bytes");
        variantTypes.put(Float.class, "real");
        variantTypes.put(Double.class, "real");
        variantTypes.put(Number.class, "real");
    }

    /**
     * Returns type name of attribute value format for PEM declaration.
     * @param o attribute value
     * @return the type name
     */
    public static String variantType(Object o) {
        o = Objects.requireNonNull(o);
        if (o instanceof FileObject) {
            return "file";
        } else if (o instanceof TextObject) {
            return "text";
        } else if (o instanceof Chapter) {
            return "chapter";
        } else {
            String name = variantTypes.get(o.getClass());
            if (name == null) {
                name = o.getClass().getName();
            }
            return name;
        }
    }
}
