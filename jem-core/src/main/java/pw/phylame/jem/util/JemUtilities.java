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

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for Jem.
 */
public final class JemUtilities {
    private static final Log LOG = LogFactory.getLog(JemUtilities.class);

    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                ClassLoader classLoader = null;
                try {
                    classLoader = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ex) {
                    // ignore
                }
                return classLoader;
            }
        });
    }

    public static Enumeration<URL> resourcesForPath(String name, ClassLoader loader) {
        return AccessController.doPrivileged(new FindResourcesAction(name, loader));
    }

    public static List<String> linesOfResource(String path,
                                               String encoding,
                                               ClassLoader classLoader,
                                               String commentLabel,
                                               boolean trimSpace,
                                               boolean skipEmpty) {
        List<String> lines = new ArrayList<String>();
        Enumeration<URL> urls = resourcesForPath(path, classLoader);
        if (urls == null) {
            return lines;
        }
        while (urls.hasMoreElements()) {
            LineIterator iterator = new LineIterator(urls.nextElement(), encoding);
            iterator.commentLabel = commentLabel;
            iterator.trimSpace = trimSpace;
            iterator.skipEmpty = skipEmpty;

            while (iterator.hasNext()) {
                lines.add(iterator.next());
            }

        }
        return lines;
    }

    private static class FindResourcesAction implements PrivilegedAction<Enumeration<URL>> {
        private final String name;
        private final ClassLoader classLoader;

        FindResourcesAction(String name, ClassLoader classLoader) {
            this.name = name;
            this.classLoader = classLoader;
        }

        @Override
        public Enumeration<URL> run() {
            Enumeration<URL> urls = null;
            try {
                if (classLoader != null) {
                    urls = classLoader.getResources(name);
                } else {
                    urls = ClassLoader.getSystemResources(name);
                }
            } catch (IOException e) {
                LOG.debug("Failed to find resource " + name + " :" + e);
            } catch (NoSuchMethodError e) {
                LOG.debug("Failed to find resource " + name + " :" + e);
            }
            return urls;
        }
    }
}
