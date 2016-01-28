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

import java.util.HashMap;

public class ImplementFactory<T> {
    private final Class<T> type;
    private final HashMap<String, ImplHolder> implementations = new HashMap<>();

    public ImplementFactory(Class<T> type) {
        this.type = type;
    }

    public void registerImplement(String name, String path) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path cannot be null or empty");
        }
        ImplHolder imp = implementations.get(name);
        if (imp != null) {
            imp.path = path;
        } else {
            implementations.put(name, new ImplHolder(path));
        }
    }

    public void registerImplement(String name, Class<? extends T> clazz) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        ImplHolder imp = implementations.get(name);
        if (imp != null) {
            imp.clazz = clazz;
        } else {
            implementations.put(name, new ImplHolder(clazz));
        }
    }

    public boolean hasImplement(String name) {
        return implementations.containsKey(name);
    }

    public void removeImplement(String name) {
        implementations.remove(name);
    }

    public String[] implementNames() {
        return implementations.keySet().toArray(new String[implementations.size()]);
    }

    public T newImplement(String name) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (name == null) {
            throw new NullPointerException();
        }
        ImplHolder imp = implementations.get(name);
        return imp != null ? imp.instantiate() : null;
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
         * @return the new instance or <tt>null</tt> if class for path does not extends <tt>T</tt>.
         * @throws ClassNotFoundException if the class of <tt>path</tt> is not found
         * @throws IllegalAccessException if the class of <tt>path</tt> is inaccessible
         * @throws InstantiationException if cannot create instance of the class
         */
        @SuppressWarnings("unchecked")
        private T instantiate() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
            if (clazz != null) {
                clazz.newInstance();
            }
            assert path != null;
            Class<?> klass = Class.forName(path);
            if (!type.isAssignableFrom(klass)) {
                return null;
            }
            clazz = (Class<T>) klass;
            return clazz.newInstance();
        }
    }
}
