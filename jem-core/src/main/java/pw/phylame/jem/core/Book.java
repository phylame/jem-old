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
import java.util.Set;
import java.util.Date;
import java.util.Locale;
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
        setAuthor(author);
    }

    /**
     * Resets all default attributes to initialized value.
     * @since 2.0.1
     */
    public void reset() {
        setTitle("");
        setCover(null);
        setAuthor("");
        setGenre("");
        setState("");
        setIntro("");
        setDate(new Date());
        setSubject("");
        setPublisher("");
        setVendor("");
        setRights("");
        setLanguage(Locale.getDefault().toLanguageTag());
    }

    public String getAuthor() {
        return stringAttribute(AUTHOR, "");
    }

    public void setAuthor(String author) {
        setAttribute(AUTHOR, author);
    }

    public String getGenre() {
        return stringAttribute(GENRE, "");
    }

    public void setGenre(String genre) {
        setAttribute(GENRE, genre);
    }

    public String getState() {
        return stringAttribute(STATE, "");
    }

    public void setState(String state) {
        setAttribute(STATE, state);
    }

    public String getSubject() {
        return stringAttribute(SUBJECT, "");
    }

    public void setSubject(String subject) {
        setAttribute(SUBJECT, subject);
    }

    public Date getDate() {
        Object o = getAttribute(DATE, null);
        if (o instanceof Date) {
            return (Date) o;
        }
        return null;
    }

    public void setDate(Date date) {
        setAttribute(DATE, date);
    }

    public String getPublisher() {
        return stringAttribute(PUBLISHER, "");
    }

    public void setPublisher(String publisher) {
        setAttribute(PUBLISHER, publisher);
    }

    /**
     * Returns vendor of the book.
     * @return the vendor string
     * @since 2.0.2
     */
    public String getVendor() {
        return stringAttribute(VENDOR, "");
    }

    /**
     * Sets new vendor of the book.
     * @param vendor the vendor string
     * @since 2.0.2
     */
    public void setVendor(String vendor) {
        setAttribute(VENDOR, vendor);
    }

    public String getRights() {
        return stringAttribute(RIGHTS, "");
    }

    public void setRights(String rights) {
        setAttribute(RIGHTS, rights);
    }

    public String getLanguage() {
        return stringAttribute(LANGUAGE, "");
    }

    public void setLanguage(String language) {
        setAttribute(LANGUAGE, language);
    }

    // ********************
    // ** Extension item **
    // ********************

    /** Extension mapping */
    private Map<String, Object> extMap = new HashMap<String, Object>();

    /**
     * Associates the specified value with the specified name in extensions.
     * <p>If the {@code name} not exists add a new item, otherwise overwritten
     *      old value.</p>
     * @param name name of the item
     * @param value value of the item
     */
    public void setItem(String name, Object value) {
        extMap.put(name, value);
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
        Object v = extMap.get(name);
        if (v != null || extMap.containsKey(name)) {
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
        return extMap.remove(name);
    }

    /**
     * Returns size of extensions in this object.
     * @return number of items
     */
    public int itemSize() {
        return extMap.size();
    }

    /**
     * Returns all names of item in extensions.
     * @return sequence of item names
     */
    public String[] itemNames() {
        return extMap.keySet().toArray(new String[0]);
    }

    /**
     * Removes all items from extension map.
     */
    public void clearItems() {
        extMap.clear();
    }

    @Override
    public void cleanup() {
        clearItems();
        super.cleanup();
    }
}
