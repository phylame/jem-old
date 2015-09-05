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

package pw.phylame.jem.util;

import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for Jem.
 */
public final class JemUtilities {
    private static Log LOG = LogFactory.getLog(JemUtilities.class);

    /**
     * Calls directGetContextClassLoader under the control of an
     * AccessController class. This means that java code running under a
     * security manager that forbids access to ClassLoaders will still work
     * if this class is given appropriate privileges, even when the caller
     * doesn't have such privileges. Without using an AccessController, the
     * the entire call stack must have the privilege before the call is
     * allowed.
     *
     * @return the context class loader associated with the current thread,
     *  or null if security doesn't allow it.
     */
    public static ClassLoader getContextClassLoaderInternal() {
        return AccessController.doPrivileged(
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
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
    private static ClassLoader directGetContextClassLoader() {
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
    public static Enumeration<URL> getResources(final ClassLoader loader,
                                                 final String name) {
        PrivilegedAction<Enumeration<URL>> action =
                new PrivilegedAction<Enumeration<URL>>() {
                    public Enumeration<URL> run() {
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
        return AccessController.doPrivileged(action);
    }

    public static Enumeration<URL> getResources(String name) {
        return getResources(getContextClassLoaderInternal(), name);
    }

    /**
     * Given a URL that refers to a .properties file, load that file.
     * This is done under an AccessController so that this method will
     * succeed when this jar file is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * {@code Null} is returned if the URL cannot be opened.
     */
    public static Properties getProperties(final URL url) {
        PrivilegedAction<Properties> action =
                new PrivilegedAction<Properties>() {
                    public Properties run() {
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
        return AccessController.doPrivileged(action);
    }

}
