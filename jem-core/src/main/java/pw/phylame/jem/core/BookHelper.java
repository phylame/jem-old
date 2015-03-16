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

import java.io.IOException;
import java.util.Map;
import java.util.Collection;

/**
 * This class manages the parsers and makers.
 * <p>The parser or maker should be registered firstly before reading or writing.</p>
 * <p>To extend formats to Jem, add name and class path to the following files:</p>
 * <ul>
 *     <li>jem-parsers.properties - declares Parser class, ex: txt=foo.bar.TxtParser</li>
 *     <li>jem-makers.properties - declares Maker class, ex: txt=foo.bar.TxtMaker</li>
 * </ul>
 * The properties files must be stored in valid class path.
 */
public final class BookHelper {
    public static final String PARSER_DEFINE_FILE = "jem-parsers.properties";

    /** Class path of registered book parsers */
    private static Map<String, String> parsers = new java.util.TreeMap<String, String>();

    /** Loaded classes of parser */
    private static Map<String, Class<? extends Parser>> cachedParsers = new java.util.TreeMap<String, Class<? extends Parser>>();

    public static final String MAKER_DEFINE_FILE = "jem-makers.properties";

    /** Class path of registered book makers */
    private static Map<String, String> makers = new java.util.TreeMap<String, String>();

    /** Loaded classes of maker */
    private static Map<String, Class<? extends Maker>> cachedMakers = new java.util.TreeMap<String, Class<? extends Maker>>();

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with new parser class.</p>
     * @param name  name of the parser (normally the extension name of book file)
     * @param classPath class path of the parser class
     */
    public static void registerParser(String name, String classPath) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (classPath == null || "".equals(name)) {
            throw new IllegalArgumentException("classPath cannot be empty");
        }
        cachedParsers.remove(name);
        parsers.put(name, classPath);
    }

    /**
     * Registers parser class with specified name.
     * <p>If parser class with same name exists, replaces the old with new parser class.</p>
     * @param name name of the parser (normally the extension name of book file)
     * @param parser the <tt>Parser</tt> class
     */
    public static void registerParser(String name, Class<? extends Parser> parser) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (parser == null) {
            throw new NullPointerException("parser");
        }
        cachedParsers.put(name, parser);
    }

    /**
     * Returns <tt>true</tt> if the specified {@code name} of parser is registered otherwise <tt>false</tt>.
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
     * @throws UnsupportedFormatException the parser is not registered
     */
    public static Parser getParser(String name) throws IllegalAccessException, InstantiationException,
            ClassNotFoundException, UnsupportedFormatException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        Class<? extends Parser> parser = cachedParsers.get(name);
        if (parser != null) {
            return parser.newInstance();
        }
        String path = parsers.get(name);
        if (path == null) {
            throw new UnsupportedFormatException(name, "Not found parser");
        }
        Class clazz = Class.forName(path);
        if (Parser.class.isAssignableFrom(clazz)) {
            throw new InstantiationException("Class not extend Parser");
        }
        return (Parser) clazz.newInstance();
    }

    /**
     * Returns names of registered parser class.
     */
    public static Collection<String> getSupportedParsers() {
        java.util.Set<String> names = parsers.keySet();
        names.addAll(cachedParsers.keySet());
        return names;
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with new maker class.</p>
     * @param name  name of the maker (normally the extension name of book file)
     * @param classPath class path of the maker class
     */
    public static void registerMaker(String name, String classPath) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (classPath == null || "".equals(classPath)) {
            throw new NullPointerException("classPath cannot be empty");
        }
        cachedMakers.remove(name);
        makers.put(name, classPath);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with new maker class.</p>
     * @param name name of the maker (normally the extension name of book file)
     * @param maker the <tt>Maker</tt> class
     */
    public static void registerMaker(String name, Class<? extends Maker> maker) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (maker == null) {
            throw new NullPointerException("maker");
        }
        cachedMakers.put(name, maker);
    }

    /**
     * Returns <tt>true</tt> if the specified {@code name} of maker is registered otherwise <tt>false</tt>.
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
     * @throws UnsupportedFormatException the maker is not registered
     */
    public static Maker getMaker(String name) throws IllegalAccessException, InstantiationException,
            ClassNotFoundException, UnsupportedFormatException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        Class<? extends Maker> maker = cachedMakers.get(name);
        if (maker != null) {
            return maker.newInstance();
        }
        String path = makers.get(name);
        if (path == null) {
            throw new UnsupportedFormatException(name, "Not found maker");
        }
        Class clazz = Class.forName(path);
        if (!Maker.class.isAssignableFrom(clazz)) {
            throw new InstantiationException("Class not extend Maker");
        }
        return (Maker) clazz.newInstance();
    }

    /**
     * Returns names of registered maker class.
     */
    public static Collection<String> getSupportedMakers() {
        java.util.Set<String> names = makers.keySet();
        names.addAll(cachedMakers.keySet());
        return names;
    }

    private static void loadAndRegister(String path, Map<String, String> recv) throws IOException {
        java.util.Properties prop = new java.util.Properties();
        java.io.InputStream in = BookHelper.class.getResourceAsStream(path);
        if (in == null) {
            return;
        }
        prop.load(in);
        for (String name: prop.stringPropertyNames()) {
            String clazz = prop.getProperty(name);
            if (clazz != null && !"".equals(clazz)) {
                recv.put(name, clazz);
            }
        }
    }

    /** Registers parser class provided by Jem. */
    private static void registerBuiltinParsers() {
        registerParser("pmab", "pw.phylame.jem.formats.pmab.PmabParser");
    }

    /** Registers custom parser classes from config file. */
    private static void registerCustomParsers() {
        try {
            loadAndRegister(PARSER_DEFINE_FILE, parsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Registers maker class provided by Jem. */
    private static void registerBuiltinMakers() {
        registerMaker("pmab", "pw.phylame.jem.formats.pmab.PmabMaker");
    }

    /** Registers custom maker classes from config file. */
    private static void registerCustomMakers() {
        try {
            loadAndRegister(MAKER_DEFINE_FILE, makers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        registerBuiltinParsers();
        registerCustomParsers();
        registerBuiltinMakers();
        registerCustomMakers();
    }
}
