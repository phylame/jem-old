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

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import pw.phylame.jem.util.IOUtils;
import pw.phylame.jem.util.ImplementFactory;

/**
 * This class manages the parsers and makers.
 * <p>The parser or maker should be registered firstly before reading or writing.</p>
 * <p>To extend formats to Jem, add name and class path to the following files:</p>
 * <ul>
 * <li><tt>PARSER_DEFINE_FILE</tt> - declares Parser classes and supported file
 * extension name.
 * ex: "txt=foo.bar.TxtParser;txt abc", a parser for file with extension .txt .abc
 * ex: "xyz=foo.bar.XyzParser", a parser for file with extension .xyz, the parser name is as extension
 * </li>
 * <li><tt>MAKER_DEFINE_FILE</tt> - declares Maker classes, ex: txt=foo.bar.TxtMaker</li>
 * </ul>
 * The properties files must be stored in valid class path.
 */
public final class BookHelper {
    /**
     * File path of parser registration
     */
    public static final String PARSER_DEFINE_FILE = "META-INF/pw-jem/parsers.properties";

    /**
     * File path of maker registration
     */
    public static final String MAKER_DEFINE_FILE = "META-INF/pw-jem/makers.properties";

    /**
     * Holds registered <tt>Parser</tt> class information.
     */
    private static final ImplementFactory<Parser> parsers = new ImplementFactory<>(Parser.class);

    /**
     * Holds registered <tt>Maker</tt> class information.
     */
    private static final ImplementFactory<Maker> makers = new ImplementFactory<>(Maker.class);

    /**
     * Mapping parser and maker name to file extension names.
     */
    private static final HashMap<String, Set<String>> extensions = new HashMap<>();

    /**
     * Mapping file extension name to parser and maker name.
     */
    private static final HashMap<String, String> names = new HashMap<>();

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with
     * the new parser class.</p>
     * <p>NOTE: old parser and cached parser with the name will be removed.</p>
     *
     * @param name name of the parser (normally the extension name of book file)
     * @param path path of the parser class
     * @throws IllegalArgumentException if the <tt>name</tt> or
     *                                  <tt>path</tt> is <tt>null</tt> or empty string
     */
    public static void registerParser(String name, String path) {
        parsers.registerImplement(name, path);
    }

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with
     * the new parser class.</p>
     *
     * @param name  name of the parser (normally the extension name of book file)
     * @param clazz the <tt>Parser</tt> class
     * @throws IllegalArgumentException if the <tt>name</tt> is <tt>null</tt> or empty string
     * @throws NullPointerException     if the <tt>clazz</tt> is <tt>null</tt>
     */
    public static void registerParser(String name, Class<? extends Parser> clazz) {
        parsers.registerImplement(name, clazz);
    }

    /**
     * Removes registered parser with specified name.
     *
     * @param name name of the parser
     */
    public static void removeParser(String name) {
        parsers.removeImplement(name);
    }

    /**
     * Tests parser with specified name is registered or not.
     *
     * @param name the name of format
     * @return <tt>true</tt> if the parser is registered otherwise <tt>false</tt>
     */
    public static boolean hasParser(String name) {
        return parsers.hasImplement(name);
    }

    /**
     * Returns names of registered parser class.
     *
     * @return sequence of format names
     */
    public static String[] supportedParsers() {
        return parsers.implementNames();
    }

    /**
     * Returns parser instance with specified name.
     *
     * @param name name of the parser
     * @return <tt>Parser</tt> instance or <tt>null</tt> if parser not registered
     * @throws NullPointerException   if the <tt>name</tt> is <tt>null</tt>
     * @throws IllegalAccessException cannot access the parser class
     * @throws InstantiationException cannot create new instance of parser class
     * @throws ClassNotFoundException if registered class path is invalid
     */
    public static Parser getParser(String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return parsers.newImplement(name);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with
     * the new maker class.</p>
     * <p>NOTE: old maker and cached maker with the name will be removed.</p>
     *
     * @param name name of the maker (normally the extension name of book file)
     * @param path class path of the maker class
     * @throws IllegalArgumentException if the <tt>name</tt> or
     *                                  <tt>path</tt> is <tt>null</tt> or empty string
     */
    public static void registerMaker(String name, String path) {
        makers.registerImplement(name, path);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with
     * the new maker class.</p>
     *
     * @param name  name of the maker (normally the extension name of book file)
     * @param clazz the <tt>Maker</tt> class
     * @throws IllegalArgumentException if the <tt>name</tt> is <tt>null</tt> or empty string
     * @throws NullPointerException     if the <tt>clazz</tt> is <tt>null</tt>
     */
    public static void registerMaker(String name, Class<? extends Maker> clazz) {
        makers.registerImplement(name, clazz);
    }

    /**
     * Removes registered maker with specified name.
     *
     * @param name name of the maker
     */
    public static void removeMaker(String name) {
        makers.removeImplement(name);
    }

    /**
     * Tests maker with specified name is registered or not.
     *
     * @param name the name of format
     * @return <tt>true</tt> if the maker is registered otherwise <tt>false</tt>
     */
    public static boolean hasMaker(String name) {
        return makers.hasImplement(name);
    }

    /**
     * Returns names of registered maker class.
     *
     * @return sequence of format names
     */
    public static String[] supportedMakers() {
        return makers.implementNames();
    }

    /**
     * Returns maker instance with specified name.
     *
     * @param name name of the maker
     * @return <tt>Maker</tt> instance or <tt>null</tt> if maker not registered
     * @throws NullPointerException   if the <tt>name</tt> is <tt>null</tt>
     * @throws IllegalAccessException cannot access the maker class
     * @throws InstantiationException cannot create new instance of maker class
     * @throws ClassNotFoundException if registered class path is invalid
     */
    public static Maker getMaker(String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        return makers.newImplement(name);
    }

    /**
     * Maps specified file extension names to parser (or maker) name.
     *
     * @param name       the name of parser or maker
     * @param extensions file extension names supported by the parser (or maker),
     *                   if <tt>null</tt> use the parser name as one extension
     * @throws NullPointerException if the <tt>name</tt> is <tt>null</tt>
     */
    public static void mapExtensions(String name, String[] extensions) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        Set<String> old = BookHelper.extensions.get(name);
        if (old == null) {
            BookHelper.extensions.put(name, old = new HashSet<>());
        }
        if (extensions == null) {
            old.add(name);
        } else {
            Collections.addAll(old, extensions);
        }
        for (String extension : old) {
            names.put(extension, name);
        }
    }

    /**
     * Gets supported file extension names of specified parser or maker name.
     *
     * @param name the name of parser or maker
     * @return the string set of extension name
     */
    public static String[] extensionsOfName(String name) {
        Set<String> extensions = BookHelper.extensions.get(name);
        return extensions.toArray(new String[extensions.size()]);
    }

    /**
     * Gets parser or maker name by file extension name.
     *
     * @param extension the extension name
     * @return the name or <tt>null</tt> if the extension name is unknown.
     */
    public static String nameOfExtension(String extension) {
        return names.get(extension);
    }

    private static final String NAME_EXTENSION_SEPARATOR = ";";
    private static final String EXTENSION_SEPARATOR = " ";

    private static <T> void loadRegisters(ClassLoader cl, String path, ImplementFactory<T> factory) {
        Enumeration<URL> urls = IOUtils.getResources(path, cl);
        if (urls == null) {
            return;
        }
        InputStream in;
        Properties prop;
        try {
            while (urls.hasMoreElements()) {
                in = urls.nextElement().openStream();
                prop = new Properties();
                prop.load(in);
                for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                    String name = entry.getKey().toString();
                    String[] parts = entry.getValue().toString().split(NAME_EXTENSION_SEPARATOR, 2);
                    factory.registerImplement(name, parts[0]);
                    if (parts.length > 1) {
                        mapExtensions(name, parts[1].split(EXTENSION_SEPARATOR));
                    } else {
                        mapExtensions(name, null);
                    }
                }
            }
        } catch (IOException e) {
            // ignored
        }
    }

    static {
        ClassLoader classLoader = IOUtils.getContextClassLoader();
        loadRegisters(classLoader, PARSER_DEFINE_FILE, parsers);
        loadRegisters(classLoader, MAKER_DEFINE_FILE, makers);
    }
}
