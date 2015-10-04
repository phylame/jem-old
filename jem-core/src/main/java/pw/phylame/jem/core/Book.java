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

import java.util.Map;
import java.util.HashMap;

/**
 * Common <tt>Book</tt> model describes book structure.
 * <p>A book structure contains the following parts:</p>
 * <ul>
 *     <li>attributes - meta attributes of book</li>
 *     <li>contents - table of contents</li>
 *     <li>extensions - extension data, like attributes but not part of
 *          book itself</li>
 * </ul>
 */
public class Book extends Chapter {

    public Book() {
        this("", "");
    }

    public Book(String title, String author) {
        super(title);
        setAttribute(AUTHOR, author);
    }

    // *********************
    // ** Extension items **
    // *********************

    /** Extension mapping */
    private Map<String, Object> extension = new HashMap<String, Object>();

    /**
     * Associates the specified value with the specified name in extensions.
     * <p>If the {@code name} not exists add a new item, otherwise overwritten
     *      old value.</p>
     * @param name name of the item
     * @param value value of the item
     */
    public void setItem(String name, Object value) {
        extension.put(name, value);
    }

    /**
     * Returns item value in extensions by its name, if not present
     *      returns {@code defaultValue}.
     * @param name name of item
     * @param defaultValue default value to returned if not found item
     *                     with <tt>name</tt>
     * @return item value associated with <tt>name</tt> or
     *          <tt>defaultValue</tt> if not found item for <tt>name</tt>
     */
    public Object getItem(String name, Object defaultValue) {
        Object v = extension.get(name);
        if (v != null || extension.containsKey(name)) {
            return v;
        } else {
            return defaultValue;
        }
    }

    /**
     * Removes one item with specified name from extensions.
     * @param name name of item
     * @return the previous item value associated with <tt>name</tt>, or
     *         <tt>null</tt> if there was item for <tt>name</tt>.
     */
    public Object removeItem(String name) {
        return extension.remove(name);
    }

    /**
     * Returns size of extensions in this object.
     * @return number of items
     */
    public int itemSize() {
        return extension.size();
    }

    /**
     * Returns all names of item in extensions.
     * @return sequence of item names
     */
    public String[] itemNames() {
        return extension.keySet().toArray(new String[0]);
    }

    /**
     * Removes all items from extension map.
     */
    public void clearItems() {
        extension.clear();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        clearItems();
    }
}
