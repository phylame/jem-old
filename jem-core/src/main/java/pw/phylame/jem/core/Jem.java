/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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
import java.io.File;
import java.io.IOException;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;
import pw.phylame.jem.util.JemException;

/**
 * This class contains utility methods for book operations.
 */
public final class Jem {
    /** Jem version */
    public static final String VERSION = "2.0-SNAPSHOT";

    /** Format of Pem's default format */
    public static final String PMAB_FORMAT = "pmab";

    /**
     * Reads <tt>Book</tt> from book file.
     * @param name path name of book file
     * @param format format of the book file
     * @param kw arguments to parser
     * @return <tt>Book</tt> instance represents the book file
     * @throws java.io.IOException occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when parsing book file
     */
    public static Book readBook(String name, String format, Map<String, Object> kw)
            throws IOException, JemException {
        return readBook(new File(name), format, kw);
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
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (format == null) {
            throw new NullPointerException("format");
        }
        Parser parser;
        try {
            parser = BookHelper.getParser(format);
        } catch (IllegalAccessException e) {
            throw new JemException(e);
        } catch (InstantiationException e) {
            throw new JemException(e);
        } catch (ClassNotFoundException e) {
            throw new JemException(e);
        }
        return parser.parse(file, kw);
    }

    /**
     * Writes <tt>Book</tt> to book with specified format.
     * @param book the <tt>Book</tt> to be written
     * @param output path name of output book file (must exists)
     * @param format output format
     * @param kw arguments to maker
     * @throws java.io.IOException occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when making book file
     */
    public static void writeBook(Book book, String output, String format, Map<String, Object> kw)
            throws IOException, JemException {
        writeBook(book, new File(output), format, kw);
    }

    /**
     * Writes <tt>Book</tt> to book with specified format.
     * @param book the <tt>Book</tt> to be written
     * @param output output book file (must exists)
     * @param format output format
     * @param kw arguments to maker
     * @throws java.io.IOException occurs IO errors
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
        if (format == null) {
            throw new NullPointerException("format");
        }
        Maker maker;
        try {
            maker = BookHelper.getMaker(format);
        } catch (IllegalAccessException e) {
            throw new JemException(e);
        } catch (InstantiationException e) {
            throw new JemException(e);
        } catch (ClassNotFoundException e) {
            throw new JemException(e);
        }
        maker.make(book, output, kw);
    }

    /**
     * Gets child part with specified index from part sub-part tree.
     * @param owner the <tt>Part</tt> to be indexed
     * @param orders list of index in sub-part tree
     * @param fromIndex begin position of index in {@code orders}
     * @return the <tt>Part</tt>
     * @throws IndexOutOfBoundsException index in {@code orders} or {@code  fromIndex} is invalid
     */
    public static Part getPart(Part owner, int[] orders, int fromIndex) {
        int index = orders[fromIndex];
        if (index >= owner.size()) {
            throw new IndexOutOfBoundsException("index out of range: "+index);
        }
        Part part;
        if (index < 0) {
            part = owner.get(owner.size()+index);
        } else {
            part = owner.get(index);
        }
        if (fromIndex == orders.length-1) {   // last one
            return part;
        } else {
            return getPart(part, orders, fromIndex+1);
        }
    }

    public static Book toBook(Part part) {
        Book book = new Book();
        // copy attributes
        book.updateAttributes(part);
        // copy sub-parts
        for (Part sub: part) {
            book.append(sub);
        }
        return book;
    }

    /**
     * This interface used for walking sub-parts.
     */
    public static interface  Walker {
        /** Returns <tt>false</tt> to stop walking. */
        boolean watch(Part part);
    }

    /**
     * Walks <tt>Part</tt> sub-part tree.
     * @param part the <tt>Part</tt> to be watched
     * @param walker watch the part
     */
    public static void walkPart(Part part, Walker walker) {
        if (part == null) {
            throw new NullPointerException("part");
        }
        if (walker == null) {
            throw new NullPointerException("walker");
        }
        if (!walker.watch(part)) {
            return;
        }
        for (Part sub: part) {
            walkPart(sub, walker);
        }
    }

    private static java.util.Map<Class<?>, String> variantTypes = new java.util.HashMap<Class<?>, String>();
    static {
        variantTypes.put(Character.class, "str");
        variantTypes.put(String.class, "str");
        variantTypes.put(java.util.Date.class, "datetime");
        variantTypes.put(Byte.class, "int");
        variantTypes.put(Short.class, "int");
        variantTypes.put(Integer.class, "int");
        variantTypes.put(Long.class, "int");
        variantTypes.put(Boolean.class, "int");
        variantTypes.put(byte[].class, "bytes");
        variantTypes.put(Byte[].class, "bytes");
        variantTypes.put(Float.class, "int");
        variantTypes.put(Double.class, "int");
    }

    /**
     * Returns type name of attribute value format with PEM.
     * @param o attribute value
     * @return type name
     */
    public static String variantType(Object o) {
        if (o instanceof FileObject) {
            return "file";
        } else if (o instanceof TextObject) {
            return "text";
        } else if (o instanceof Part) {
            return "part";
        }
        else {
            String name = variantTypes.get(o.getClass());
            if (name == null || !variantTypes.containsKey(o.getClass())) {
                name = o.getClass().getSimpleName().toLowerCase();
            }
            return name;
        }
    }
}