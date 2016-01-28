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

package pw.phylame.jem.formats.util;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.MissingResourceException;

import pw.phylame.jem.formats.util.text.TextUtils;

/**
 * Stores e-book file information.
 * <p>The <tt>FileInfo</tt> object is saved in book's extensions.
 */
public abstract class FileInfo {
    /**
     * Extension key for <tt>FileInfo</tt> object.
     * <p>This extension should be ignored when making book.
     */
    public static final String FILE_INFO = "file-info";

    // common keys
    protected static final String TYPE = "common.info.type";
    protected static final String VERSION = "common.info.version";

    protected final HashMap<String, Object> properties = new HashMap<>();

    protected final void put(String key, Object value) {
        properties.put(key, value);
    }

    protected final void unite(Map<String, Object> m) {
        properties.putAll(m);
    }

    public final Set<Map.Entry<String, Object>> infoEntries() {
        return properties.entrySet();
    }

    public String localizedKey(String key) {
        try {
            return MessageBundle.getText(key);
        } catch (MissingResourceException e) {
            return TextUtils.capitalized(key);
        }
    }

    public String localizedValue(Object value) {
        return String.valueOf(value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int ix = 0, end = properties.size() - 1;
        for (Map.Entry<String, Object> entry : infoEntries()) {
            builder.append(localizedKey(entry.getKey())).append("=");
            builder.append(localizedValue(entry.getValue()));
            if (ix++ != end) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
}
