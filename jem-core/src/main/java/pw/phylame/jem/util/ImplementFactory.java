/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

import java.util.HashMap;

public class ImplementFactory<T> {
    private final Class<T> type;
    private final HashMap<String, ImplHolder> implementations = new HashMap<>();
    private final HashMap<String, T> objectCache;

    /**
     * Constructs object with specified class type.
     *
     * @param type     class of the interface
     * @param reusable <code>true</code> to reuse instance
     */
    public ImplementFactory(Class<T> type, boolean reusable) {
        this.type = type;
        objectCache = reusable ? new HashMap<String, T>() : null;
    }

    /**
     * Registers new implementation with name and class path.
     * <strong>NOTE:</strong> old implementation will be overwritten
     *
     * @param name name of the implementation
     * @param path full class path of the implementation
     */
    public void registerImplement(String name, String path) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path cannot be null or empty");
        }
        ImplHolder impl = implementations.get(name);
        if (impl != null) {
            impl.path = path;
            if (objectCache != null) {
                objectCache.remove(name);
            }
        } else {
            implementations.put(name, new ImplHolder(path));
        }
    }

    /**
     * Registers new implementation with name and class.
     * <strong>NOTE:</strong> old implementation will be overwritten
     *
     * @param name  name of the implementation
     * @param clazz class of the implementation
     */
    public void registerImplement(String name, Class<? extends T> clazz) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        ImplHolder impl = implementations.get(name);
        if (impl != null) {
            impl.clazz = clazz;
            if (objectCache != null) {
                objectCache.remove(name);
            }
        } else {
            implementations.put(name, new ImplHolder(clazz));
        }
    }

    public boolean hasImplement(String name) {
        return implementations.containsKey(name);
    }

    public void removeImplement(String name) {
        implementations.remove(name);
        if (objectCache != null) {
            objectCache.remove(name);
        }
    }

    public String[] implementNames() {
        return implementations.keySet().toArray(new String[implementations.size()]);
    }

    /**
     * Returns an instance for specified implementation name.
     *
     * @param name name of the implementation
     * @return instance for the implementation
     * @throws IllegalAccessException if the class cannot access
     * @throws InstantiationException if the instance cannot be created
     * @throws ClassNotFoundException if the class path is invalid
     */
    public T newInstance(String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (name == null) {
            throw new NullPointerException();
        }
        T obj = null;
        if (objectCache != null && (obj = objectCache.get(name)) != null) { // get from cache
            return obj;
        }
        ImplHolder impl = implementations.get(name);
        if (impl != null) {
            obj = impl.instantiate();
            if (objectCache != null) {
                objectCache.put(name, obj);
            }
        }
        return obj;
    }

    private class ImplHolder {
        private String path;
        private Class<? extends T> clazz;

        private ImplHolder(String path) {
            this.path = path;
        }

        private ImplHolder(Class<? extends T> clazz) {
            this.clazz = clazz;
        }

        /**
         * Creates a new instance of implement for <tt>T</tt>.
         *
         * @return the new instance or <tt>null</tt> if class for path does not extends from <tt>T</tt>.
         * @throws ClassNotFoundException if the class of <tt>path</tt> is not found
         * @throws IllegalAccessException if the class of <tt>path</tt> is inaccessible
         * @throws InstantiationException if cannot create instance of the class
         */
        @SuppressWarnings("unchecked")
        private T instantiate() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
            if (clazz != null) {
                clazz.newInstance();
            }
            if (path == null) {
                throw new AssertionError("BUG: implementation without clazz and path specified");
            }
            Class<?> klass = Class.forName(path);
            if (!type.isAssignableFrom(klass)) {
                return null;
            }
            clazz = (Class<T>) klass;
            return clazz.newInstance();
        }
    }
}
