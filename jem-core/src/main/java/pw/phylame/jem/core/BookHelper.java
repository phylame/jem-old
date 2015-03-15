/*
 * Copyright 2015 Peng Wan
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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Collection;
import pw.phylame.jem.util.JemException;

/**
 * This class manages the parsers and makers.
 * <p>The parser or maker should be registered firstly before reading or writing.</p>
 */
public final class BookHelper {
    /** Registered book parsers */
    private static Map<String, Class<? extends BookParser>> parsers = new java.util.HashMap<String, Class<? extends BookParser>>();

    /** Registered book makers */
    private static Map<String, Class<? extends BookMaker>> makers = new java.util.HashMap<String, Class<? extends BookMaker>>();

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with new parser class.</p>
     * @param name name of the parser class(normally the extension name of book file)
     * @param parser the <tt>BookParser</tt> class
     */
    public static void registerParser(String name, Class<? extends BookParser> parser) {
        if (parser == null) {
            throw new NullPointerException("parser");
        }
        parsers.put(name, parser);
    }

    /**
     * Returns <tt>true</tt> if the specified {@code name} of parser class is registered otherwise <tt>false</tt>.
     */
    public static boolean hasParser(String name) {
        return parsers.containsKey(name);
    }

    /**
     * Returns parser instance with specified name.
     * @param name name of the parser class
     * @return <tt>BookParser</tt> instance or <tt>null</tt> if parser not registered
     * @throws IllegalAccessException cannot access the parser class
     * @throws InstantiationException cannot create new instance of parser class
     */
    public static BookParser getParser(String name) throws IllegalAccessException, InstantiationException,
            UnsupportedFormatException {
        Class<? extends BookParser> clazz = parsers.get(name);
        if (clazz == null) {
            throw new UnsupportedFormatException(name);
        }
        return clazz.newInstance();
    }

    /**
     * Returns names of registered parser class.
     */
    public static Collection<String> getSupportedParsers() {
        return parsers.keySet();
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with new maker class.</p>
     * @param name name of the maker class(normally the extension name of book file)
     * @param maker the <tt>BookMaker</tt> class
     */
    public static void registerMaker(String name, Class<? extends BookMaker> maker) {
        if (maker == null) {
            throw new NullPointerException("maker");
        }
        makers.put(name, maker);
    }

    /**
     * Returns <tt>true</tt> if the specified {@code name} of maker class is registered otherwise <tt>false</tt>.
     */
    public static boolean hasMaker(String name) {
        return makers.containsKey(name);
    }

    /**
     * Returns maker instance with specified name.
     * @param name name of the maker class
     * @return <tt>BookMaker</tt> instance or <tt>null</tt> if maker not registered
     * @throws IllegalAccessException cannot access the maker class
     * @throws InstantiationException cannot create new instance of maker class
     */
    public static BookMaker getMaker(String name) throws IllegalAccessException, InstantiationException,
            UnsupportedFormatException {
        Class<? extends BookMaker> clazz = makers.get(name);
        if (clazz == null) {
            throw new UnsupportedFormatException(name);
        }
        return clazz.newInstance();
    }

    /**
     * Returns names of registered maker class.
     */
    public static Collection<String> getSupportedMakers() {
        return makers.keySet();
    }

    /** Registers parser class provided by Jem. */
    private static void registerBuiltinParsers() {
        registerParser("pmab", pw.phylame.jem.formats.pmab.PmabParser.class);
    }

    /** Registers maker class provided by Jem. */
    private static void registerBuiltinMakers() {
        registerMaker("pmab", pw.phylame.jem.formats.pmab.PmabMaker.class);
    }

    static {
        registerBuiltinParsers();
        registerBuiltinMakers();
    }


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
     * @throws JemException occurs errors when parsing book file
     */
    public static Book readBook(File file, String format, Map<String, Object> kw)
            throws IOException, JemException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        if (format == null) {
            throw new NullPointerException("format");
        }
        BookParser parser;
        try {
            parser = BookHelper.getParser(format);
        } catch (IllegalAccessException e) {
            throw new JemException(e);
        } catch (InstantiationException e) {
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
     * @throws JemException occurs errors when making book file
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
     * @throws JemException occurs errors when making book file
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
        BookMaker maker;
        try {
            maker = BookHelper.getMaker(format);
        } catch (IllegalAccessException e) {
            throw new JemException(e);
        } catch (InstantiationException e) {
            throw new JemException(e);
        }
        maker.make(book, output, kw);
    }

    /**
     * Gets child with specified index from part sub-part tree.
     * @param owner the <tt>Part</tt> to be indexed
     * @param orders list of index in sub-part tree
     * @param fromIndex begin position of index in {@code orders}
     * @return the <tt>Part</tt> or <tt>null</tt> if not found
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

    /**
     * Walks sub-part tree of specified part.
     * @param part the <tt>Part</tt> to watched
     * @param walker watch the part
     */
    public static void walkPart(Part part, Walker walker) {
        if (walker == null) {
            throw new NullPointerException("walker");
        }
        assert part != null;
        if (!walker.watch(part)) {
            return;
        }
        for (int ix = 0; ix < part.size(); ++ix) {
            walkPart(part.get(ix), walker);
        }
    }
}
