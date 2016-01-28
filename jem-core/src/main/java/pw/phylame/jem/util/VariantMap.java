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

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class VariantMap {
    private final HashMap<String, Object> map = new HashMap<>();

    public VariantMap() {
    }

    public void put(String key, Object value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (value == null) {
            throw new NullPointerException("value");
        }
        map.put(key, value);
    }

    public void update(Map<String, Object> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        this.map.putAll(map);
    }

    public void update(VariantMap rhs) {
        if (rhs == null) {
            throw new NullPointerException();
        }
        map.putAll(rhs.map);
    }

    public boolean contains(String key) {
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def, Class<T> type) {
        Object v = map.get(key);
        return v != null && type.isInstance(v) ? (T) v : def;
    }

    public Object get(String key, Object def) {
        Object v = map.get(key);
        return v != null ? v : def;
    }

    public String get(String key, String def) {
        Object v = map.get(key);
        return (v != null) ? v.toString() : def;
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    public void clear() {
        map.clear();
    }

    public int size() {
        return map.size();
    }

    public String[] keys() {
        return map.keySet().toArray(new String[map.size()]);
    }

    public Set<Map.Entry<String, Object>> entries() {
        return map.entrySet();
    }
}
