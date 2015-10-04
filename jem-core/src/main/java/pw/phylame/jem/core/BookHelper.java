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

import java.net.URL;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import pw.phylame.jem.util.JemUtilities;
import pw.phylame.jem.util.UnsupportedFormatException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manages the parsers and makers.
 * <p>The parser or maker should be registered firstly before reading or writing.</p>
 * <p>To extend formats to Jem, add name and class path to the following files:</p>
 * <ul>
 *     <li><tt>PARSER_DEFINE_FILE</tt> - declares Parser classes and
 *          supported file extension name. <br/>
 *          ex: "txt=foo.bar.TxtParser;txt abc", a parser for file with extension .txt .abc<br/>
 *          ex: "xyz=foo.bar.XyzParser", a parser for file with extension .xyz, the parser name is as extension
 *          </li>
 *     <li><tt>MAKER_DEFINE_FILE</tt> - declares Maker classes, ex: txt=foo.bar.TxtMaker</li>
 * </ul>
 * The properties files must be stored in valid class path.
 */
public final class BookHelper {
    private static      Log    LOG                = LogFactory.getLog(BookHelper.class);
    /** File path of parser registration */
    public static final String PARSER_DEFINE_FILE = "META-INF/pw-jem/parsers.properties";

    /** Class path of registered book parsers */
    private static Map<String, String> parsers = new HashMap<String, String>();

    /** Caches loaded classes of parser */
    private static Map<String, Class<? extends Parser>> cachedParsers =
            new HashMap<String, Class<? extends Parser>>();

    /** File path of maker registration */
    public static final String MAKER_DEFINE_FILE = "META-INF/pw-jem/makers.properties";

    /** Class path of registered book makers */
    private static Map<String, String> makers = new HashMap<String, String>();

    /** Cached loaded classes of maker */
    private static Map<String, Class<? extends Maker>> cachedMakers =
            new HashMap<String, Class<? extends Maker>>();

    private static final String NAME_EXTENSION_SEPARATOR = ";";
    private static final String EXTENSION_SEPARATOR      = " ";

    /**
     * Mapping parser and maker name to file extension name
     */
    private static Map<String, String[]> nameMap = new HashMap<String, String[]>();

    private static Map<String, String> extMap = new HashMap<String, String>();

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with
     *      the new parser class.</p>
     * <p>NOTE: old parser and cached parser with the name will be removed.</p>
     * @param name name of the parser (normally the extension name of book file)
     * @param classPath class path of the parser class
     */
    public static void registerParser(String name, String classPath) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (classPath == null || "".equals(name)) {
            throw new IllegalArgumentException("classPath cannot be null or empty");
        }
        cachedParsers.remove(name);
        parsers.put(name, classPath);
    }

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with
     *      the new parser class.</p>
     * @param name name of the parser (normally the extension name of book file)
     * @param parser the <tt>Parser</tt> class
     */
    public static void registerParser(String name, Class<? extends Parser> parser) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (parser == null) {
            throw new NullPointerException("parser");
        }
        cachedParsers.put(name, parser);
    }

    /**
     * Removes registered parser with specified name.
     * @param name name of the parser
     */
    public static void removeParser(String name) {
        cachedParsers.remove(name);
        parsers.remove(name);
    }

    /**
     * Tests parser with specified name is registered or not.
     * @param name the name of format
     * @return <tt>true</tt> if the parser is registered otherwise <tt>false</tt>
     */
    public static boolean hasParser(String name) {
        return cachedParsers.containsKey(name) || parsers.containsKey(name);
    }

    /**
     * Returns parser instance with specified name.
     * @param name name of the parser
     * @return <tt>Parser</tt> instance or <tt>null</tt> if parser not registered
     * @throws IllegalAccessException cannot access the parser class
     * @throws InstantiationException cannot create new instance of parser class
     * @throws ClassNotFoundException if registered class path is invalid
     * @throws UnsupportedFormatException if the parser is not registered
     */
    public static Parser getParser(String name) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException, UnsupportedFormatException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        // get if cached
        Class<? extends Parser> parser = cachedParsers.get(name);
        if (parser != null) {
            return parser.newInstance();
        }
        String path = parsers.get(name);
        if (path == null) {
            throw new UnsupportedFormatException(name, "Not found book parser: "+name);
        }
        Class<?> clazz = Class.forName(path);
        if (! Parser.class.isAssignableFrom(clazz)) {
            throw new UnsupportedFormatException(name, "Class not extend Parser: "+path);
        }
        cachedParsers.put(name, (Class<Parser>) clazz);
        return (Parser) clazz.newInstance();
    }

    /**
     * Returns names of registered parser class.
     * @return sequence of format names
     */
    public static String[] supportedParsers() {
        Set<String> names = new HashSet<String>(cachedParsers.keySet());
        names.addAll(parsers.keySet());
        return names.toArray(new String[0]);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with
     *      the new maker class.</p>
     * <p>NOTE: old maker and cached maker with the name will be removed.</p>
     * @param name  name of the maker (normally the extension name of book file)
     * @param classPath class path of the maker class
     */
    public static void registerMaker(String name, String classPath) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (classPath == null || "".equals(classPath)) {
            throw new NullPointerException("classPath cannot be null or empty");
        }
        cachedMakers.remove(name);
        makers.put(name, classPath);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with
     *      the new maker class.</p>
     * @param name name of the maker (normally the extension name of book file)
     * @param maker the <tt>Maker</tt> class
     */
    public static void registerMaker(String name, Class<? extends Maker> maker) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (maker == null) {
            throw new NullPointerException("maker");
        }
        cachedMakers.put(name, maker);
    }

    /**
     * Removes registered maker with specified name.
     * @param name name of the maker
     */
    public static void removeMaker(String name) {
        cachedMakers.remove(name);
        makers.remove(name);
    }

    /**
     * Tests maker with specified name is registered or not.
     * @param name the name of format
     * @return <tt>true</tt> if the maker is registered otherwise <tt>false</tt>
     */
    public static boolean hasMaker(String name) {
        return cachedMakers.containsKey(name) || makers.containsKey(name);
    }

    /**
     * Returns maker instance with specified name.
     * @param name name of the maker
     * @return <tt>Maker</tt> instance or <tt>null</tt> if maker not registered
     * @throws IllegalAccessException cannot access the maker class
     * @throws InstantiationException cannot create new instance of maker class
     * @throws ClassNotFoundException if registered class path is invalid
     * @throws UnsupportedFormatException if the maker is not registered
     */
    public static Maker getMaker(String name) throws IllegalAccessException,
            InstantiationException, ClassNotFoundException, UnsupportedFormatException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        // get if cached
        Class<? extends Maker> maker = cachedMakers.get(name);
        if (maker != null) {
            return maker.newInstance();
        }
        String path = makers.get(name);
        if (path == null) {
            throw new UnsupportedFormatException(name, "Not found book maker: "+name);
        }
        Class<?> clazz = Class.forName(path);
        if (! Maker.class.isAssignableFrom(clazz)) {
            throw new UnsupportedFormatException(name, "Class not extend Maker: "+path);
        }
        cachedMakers.put(name, (Class<Maker>) clazz);
        return (Maker) clazz.newInstance();
    }

    /**
     * Returns names of registered maker class.
     * @return sequence of format names
     */
    public static String[] supportedMakers() {
        Set<String> names = new HashSet<String>(cachedMakers.keySet());
        names.addAll(makers.keySet());
        return names.toArray(new String[0]);
    }

    public static String[] getExtensions(String name) {
        return nameMap.get(name);
    }

    public static String getFormat(String ext) {
        return extMap.get(ext);
    }

    private static Map<String, String> loadRegistrations(String fileName) {
        Map<String, String> map = new HashMap<String, String>();

        // Identify the class loader we will be using
        ClassLoader contextClassLoader = JemUtilities.getContextClassLoaderInternal();

        Enumeration<URL> urls = JemUtilities.getResources(contextClassLoader, fileName);
        if (urls == null) {
            return map;
        }
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            Properties prop = JemUtilities.getProperties(url);
            if (prop != null) {
                for (String name: prop.stringPropertyNames()) {
                    String clazz = prop.getProperty(name);
                    if (clazz != null && ! clazz.isEmpty()) {
                        map.put(name, clazz);
                    }
                }
            }
        }
        return map;
    }

    /** Registers custom parser classes from config file. */
    private static void registerCustomParsers() {
        for (Map.Entry<String, String> entry:
                loadRegistrations(PARSER_DEFINE_FILE).entrySet()) {
            String name = entry.getKey();
            String[] parts = entry.getValue().split(NAME_EXTENSION_SEPARATOR, 2);
            if (parts.length == 0) {
                LOG.debug("declared parser class path of "+name+" is empty");
                continue;
            }
            registerParser(name, parts[0]);
            String[] ext;
            if (parts.length > 1) {
                ext = parts[1].split(EXTENSION_SEPARATOR);
            } else {
                ext = new String[]{name};
            }
            nameMap.put(name, ext);
            for (String e : ext) {
                extMap.put(e, name);
            }
        }
    }

    /** Registers custom maker classes from config file. */
    private static void registerCustomMakers() {
        for (Map.Entry<String, String> entry:
                loadRegistrations(MAKER_DEFINE_FILE).entrySet()) {
            registerMaker(entry.getKey(), entry.getValue());
        }
    }

    static {
        registerCustomParsers();
        registerCustomMakers();
    }
}
