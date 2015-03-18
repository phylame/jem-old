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
import java.util.Collection;

/**
 * Provides map based attributes.
 */
public class Attributes {
    private Map<String, Object> metaMap = new java.util.TreeMap<String, Object>();

    /**
     * Associates the specified value with the specified key in attributes map.
     * <p>If the {@code key} not exists add a new attribute, otherwise overwritten old value.</p>
     * @param key key of the attribute
     * @param value value of the attribute
     */
    public void setAttribute(String key, Object value) {
        metaMap.put(key, value);
    }

    /**
     * Updates attributes with specified map
     * @param metaMap the source map
     */
    public void updateAttributes(Map<String, Object> metaMap) {
        this.metaMap.putAll(metaMap);
    }

    /**
     * Updates attributes with other object
     * @param other the source attributes
     */
    public void updateAttributes(Attributes other) {
        updateAttributes(other.metaMap);
    }

    /** Returns <tt>true</tt> if {@code key} in attributes map otherwise <tt>false</tt>. */
    public boolean hasAttribute(String key) {
        return metaMap.containsKey(key);
    }

    /** Returns attribute value by its key. If {@code key} not exists return {@code defaultValue}. */
    public Object getAttribute(String key, Object defaultValue) {
        Object v = metaMap.get(key);
        if (v != null || metaMap.containsKey(key)) {
            return v;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns attribute value converted to string by its key.
     * If {@code key} not exists return {@code defaultValue}.
     */
    public String stringAttribute(String key, String defaultValue) {
        Object v = metaMap.get(key);
        if (v != null) {
            return v.toString();
        } else if (!metaMap.containsKey(key)) {
            return defaultValue;
        } else {
            return null;
        }
    }

    /**
     * Removes one attribute from attributes map.
     * @param key key of the attribute
     * @return removed attribute value or <tt>null</tt> if the key not exists.
     */
    public Object removeAttribute(String key) {
        return metaMap.remove(key);
    }

    /** Returns number of attribute in attributes map. */
    public int attributeSize() {
        return metaMap.size();
    }

    /** Returns all names in attributes map. */
    public Collection<String> attributeNames() {
        return metaMap.keySet();
    }

    /**
     * Removes all attributes from the map.
     */
    public void clearAttributes() {
        metaMap.clear();
    }

}
