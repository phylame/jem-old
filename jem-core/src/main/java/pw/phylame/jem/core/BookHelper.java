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
import java.util.Properties;
import java.util.Enumeration;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.util.UnsupportedFormatException;

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
    private static Log LOG = LogFactory.getLog(BookHelper.class);

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
     * <p>If parser class with same name exists, replaces the old with new parser class.</p>
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
     * @throws pw.phylame.jem.util.UnsupportedFormatException the parser is not registered
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
            throw new UnsupportedFormatException(name, "not found parser");
        }
        Class clazz = Class.forName(path);
        if (! Parser.class.isAssignableFrom(clazz)) {
            throw new UnsupportedFormatException(name, "class not extend Parser");
        }
        return (Parser) clazz.newInstance();
    }

    /**
     * Returns names of registered parser class.
     * @return sequence of format names
     */
    public static String[] supportedParsers() {
        java.util.Set<String> names = parsers.keySet();
        names.addAll(cachedParsers.keySet());
        return names.toArray(new String[0]);
    }

    /**
     * Registers maker class with specified name.
     * <p>If maker class with same name exists, replaces the old with new maker class.</p>
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
     * <p>If maker class with same name exists, replaces the old with new maker class.</p>
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
            throw new UnsupportedFormatException(name, "not found maker");
        }
        Class clazz = Class.forName(path);
        if (! Maker.class.isAssignableFrom(clazz)) {
            throw new UnsupportedFormatException(name, "class not extend Maker");
        }
        return (Maker) clazz.newInstance();
    }

    /**
     * Returns names of registered maker class.
     * @return sequence of format names
     */
    public static String[] supportedMakers() {
        java.util.Set<String> names = makers.keySet();
        names.addAll(cachedMakers.keySet());
        return names.toArray(new String[0]);
    }

    /**
     * Calls directGetContextClassLoader under the control of an
     * AccessController class. This means that java code running under a
     * security manager that forbids access to ClassLoaders will still work
     * if this class is given appropriate privileges, even when the caller
     * doesn't have such privileges. Without using an AccessController, the
     * the entire call stack must have the privilege before the call is
     * allowed.
     *
     * @return the context classloader associated with the current thread,
     *  or null if security doesn't allow it.
     */
    private static ClassLoader getContextClassLoaderInternal() {
        return (ClassLoader)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return directGetContextClassLoader();
                    }
                });
    }

    /**
     * Return the thread context class loader if available; otherwise return null.
     * <p>
     * Most/all code should call getContextClassLoaderInternal rather than
     * calling this method directly.
     * <p>
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * <p>
     * Note that no internal logging is done within this method because
     * this method is called every time LogFactory.getLogger() is called,
     * and we don't want too much output generated here.
     *
     * @return the thread's context classloader or {@code null} if the java security
     *  policy forbids access to the context classloader from one of the classes
     *  in the current call stack.
     * @since 1.1
     */
    protected static ClassLoader directGetContextClassLoader() {
        ClassLoader classLoader = null;

        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException ex) {
            /**
             * getContextClassLoader() throws SecurityException when
             * the context class loader isn't an ancestor of the
             * calling class's class loader, or if security
             * permissions are restricted.
             *
             * We ignore this exception to be consistent with the previous
             * behavior (e.g. 1.1.3 and earlier).
             */
            // ignore
        }

        // Return the selected class loader
        return classLoader;
    }

    /**
     * Given a filename, return an enumeration of URLs pointing to
     * all the occurrences of that filename in the classpath.
     * <p>
     * This is just like ClassLoader.getResources except that the
     * operation is done under an AccessController so that this method will
     * succeed when this jar file is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * If no instances are found, an Enumeration is returned whose
     * hasMoreElements method returns false (ie an "empty" enumeration).
     * If resources could not be listed for some reason, null is returned.
     */
    private static Enumeration getResources(final ClassLoader loader, final String name) {
        PrivilegedAction action =
                new PrivilegedAction() {
                    public Object run() {
                        try {
                            if (loader != null) {
                                return loader.getResources(name);
                            } else {
                                return ClassLoader.getSystemResources(name);
                            }
                        } catch (IOException e) {
                            LOG.debug("Exception while trying to find configuration file " +
                                    name + ":" + e);
                            return null;
                        } catch (NoSuchMethodError e) {
                            // we must be running on a 1.1 JVM which doesn't support
                            // ClassLoader.getSystemResources; just return null in
                            // this case.
                            return null;
                        }
                    }
                };
        Object result = AccessController.doPrivileged(action);
        return (Enumeration) result;
    }

    /**
     * Given a URL that refers to a .properties file, load that file.
     * This is done under an AccessController so that this method will
     * succeed when this jar file is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * {@code Null} is returned if the URL cannot be opened.
     */
    private static Properties getProperties(final URL url) {
        PrivilegedAction action =
                new PrivilegedAction() {
                    public Object run() {
                        InputStream stream = null;
                        try {
                            // We must ensure that useCaches is set to false, as the
                            // default behaviour of java is to cache file handles, and
                            // this "locks" files, preventing hot-redeploy on windows.
                            URLConnection connection = url.openConnection();
                            connection.setUseCaches(false);
                            stream = connection.getInputStream();
                            if (stream != null) {
                                Properties props = new Properties();
                                props.load(stream);
                                stream.close();
                                stream = null;
                                return props;
                            }
                        } catch (IOException e) {
                            LOG.debug("Unable to read URL " + url, e);
                        } finally {
                            if (stream != null) {
                                try {
                                    stream.close();
                                } catch (IOException e) {
                                    // ignore exception; this should not happen
                                    LOG.debug("Unable to close stream for URL " + url, e);
                                }
                            }
                        }

                        return null;
                    }
                };
        return (Properties) AccessController.doPrivileged(action);
    }

    private static void loadRegistrations(String fileName, Map<String, String> map) {
        // Identify the class loader we will be using
        ClassLoader contextClassLoader = getContextClassLoaderInternal();

        Enumeration urls = getResources(contextClassLoader, fileName);
        if (urls == null) {
            return;
        }
        while (urls.hasMoreElements()) {
            URL url = (URL) urls.nextElement();
            Properties prop = getProperties(url);
            if (prop != null) {
                for (String name: prop.stringPropertyNames()) {
                    String clazz = prop.getProperty(name);
                    if (clazz != null && ! "".equals(clazz)) {
                        map.put(name, clazz);
                    }
                }
            }
        }
    }

    /** Registers custom parser classes from config file. */
    private static void registerCustomParsers() {
        loadRegistrations(PARSER_DEFINE_FILE, parsers);
    }

    /** Registers custom maker classes from config file. */
    private static void registerCustomMakers() {
        loadRegistrations(MAKER_DEFINE_FILE, makers);
    }

    static {
        registerCustomParsers();
        registerCustomMakers();
    }
}
