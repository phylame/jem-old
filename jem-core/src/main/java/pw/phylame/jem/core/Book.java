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

import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.Locale;

import pw.phylame.jem.util.VariantMap;

/**
 * Common <tt>Book</tt> model describes book structure.
 * <p>A book structure contains the following parts:</p>
 * <ul>
 * <li>attributes - meta attributes of book</li>
 * <li>contents - table of contents</li>
 * <li>extensions - extension data, like attributes but not part of
 * book itself</li>
 * </ul>
 */
public class Book extends Chapter {

    /**
     * Constructs instance with empty title and author.
     */
    public Book() {
        super();
    }

    /**
     * Constructs instance with specified title.
     *
     * @param title the title of book
     */
    public Book(String title) {
        super(title);
    }

    /**
     * Constructs instance with specified title and author.
     *
     * @param title  the title string
     * @param author the author string
     * @throws NullPointerException if the argument list contains <tt>null</tt>
     */
    public Book(String title, String author) {
        super(title);
        setAuthor(author);
    }

    Book(Chapter chapter) {
        super(chapter);
    }

    public String getAuthor() {
        return stringAttribute(AUTHOR, "");
    }

    public void setAuthor(String author) {
        setAttribute(AUTHOR, author);
    }

    public Date getDate() {
        return attributes.get(DATE, null, Date.class);
    }

    public void setDate(Date date) {
        setAttribute(DATE, date);
    }

    public String getGenre() {
        return stringAttribute(GENRE, "");
    }

    public void setGenre(String genre) {
        setAttribute(GENRE, genre);
    }

    public Locale getLanguage() {
        return attributes.get(LANGUAGE, null, Locale.class);
    }

    public void setLanguage(Locale language) {
        setAttribute(LANGUAGE, language);
    }

    public String getPublisher() {
        return stringAttribute(PUBLISHER, "");
    }

    public void setPublisher(String publisher) {
        setAttribute(PUBLISHER, publisher);
    }

    public String getRights() {
        return stringAttribute(RIGHTS, "");
    }

    public void setRights(String rights) {
        setAttribute(RIGHTS, rights);
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

    public String getVendor() {
        return stringAttribute(VENDOR, "");
    }

    public void setVendor(String vendor) {
        setAttribute(VENDOR, vendor);
    }

    // *********************
    // ** Extension items **
    // *********************

    /**
     * Extensions map.
     */
    protected final VariantMap extensions = new VariantMap();

    /**
     * Associates the specified value with the specified name in extensions.
     * <p>If the <tt>name</tt> not exists add a new extension, otherwise overwritten
     * old value.</p>
     *
     * @param name  name of the extension
     * @param value value of the extension, cannot be <tt>null</tt>
     * @throws NullPointerException if the <tt>value</tt> is <tt>null</tt>
     */
    public void setExtension(String name, Object value) {
        extensions.put(name, value);
    }

    /**
     * Tests extension with <tt>key</tt> is mapped or not.
     *
     * @param name key of the extension
     * @return <tt>true</tt> if has value mapped to <tt>key</tt>
     * otherwise <tt>not</tt>
     */
    public boolean hasExtension(String name) {
        return extensions.contains(name);
    }

    /**
     * Returns extension in extensions map by its name, if not present
     * returns <tt>defaultValue</tt>.
     *
     * @param name         name of extension
     * @param defaultValue default value to returned if not found extension
     *                     with <tt>name</tt>
     * @return extension value associated with <tt>name</tt> or
     * <tt>defaultValue</tt> if not found extension for <tt>name</tt>
     */
    public Object getExtension(String name, Object defaultValue) {
        return extensions.get(name, defaultValue);
    }

    /**
     * Removes one extension with specified name from extensions.
     *
     * @param name name of extension
     * @return the previous extension value associated with <tt>name</tt>, or
     * <tt>null</tt> if there was extension for <tt>name</tt>.
     */
    public Object removeExtension(String name) {
        return extensions.remove(name);
    }

    /**
     * Removes all items from extension map.
     */
    public void clearExtensions() {
        extensions.clear();
    }

    /**
     * Returns number of extensions in this object.
     *
     * @return number of items
     */
    public int extensionCount() {
        return extensions.size();
    }

    /**
     * Returns all names of extension in extensions.
     *
     * @return sequence of extension names
     */
    public String[] extensionNames() {
        return extensions.keys();
    }

    /**
     * Returns a set of all attribute key and values.
     *
     * @return the entry set
     * @since 2.3
     */
    public Set<Map.Entry<String, Object>> extensionEntries() {
        return extensions.entries();
    }

    @Override
    public void cleanup() {
        clearExtensions();
        super.cleanup();
    }

    @Override
    public String debugMessage() {
        return super.debugMessage() + ", extensions=" + extensions;
    }
}
