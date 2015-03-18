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
import java.util.Date;
import java.util.Collection;

/**
 * <tt>Book</tt> describes book structure.
 * <p>A book structure contains 3 parts:</p>
 * <ul>
 *     <li>attributes - meta attributes of book</li>
 *     <li>contents - table of contents</li>
 *     <li>extensions - extension data, like attributes but not included in book content</li>
 * </ul>
 */
public class Book extends Chapter {
    /** Key name for book author.*/
    public final String AUTHOR = "author";

    /** Key name for book genre.*/
    public final String GENRE = "genre";

    /** Key name for book state.*/
    public final String STATE = "state";

    /** Key name for book subject.*/
    public final String SUBJECT = "subject";

    /** Key name for book date.*/
    public final String DATE = "date";

    /** Key name for book publisher.*/
    public final String PUBLISHER = "publisher";

    /** Key name for book rights.*/
    public final String RIGHTS = "rights";

    /** Key name for book language.*/
    public final String LANGUAGE = "language";


    public Book() {
        this("", "");
    }

    public Book(String title, String author) {
        super(title, "", null, null);
        setAuthor(author);
        setGenre("");
        setState("");
        setSubject("");
        setDate(new Date());
        setPublisher("");
        setRights("");
        java.util.Locale locale = java.util.Locale.getDefault();
        setLanguage(locale.getLanguage()+"_"+locale.getCountry());
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
        } else {
            return new Date();
        }
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

    /**
     * Associates the specified value with the specified name in extensions.
     * <p>If the {@code name} not exists add a new item, otherwise overwritten old value.</p>
     * @param name name of the item
     * @param value value of the item
     */
    public void setItem(String name, Object value) {
        extensions.put(name, value);
    }

    /** Returns item in extensions by its name, if not present returns {@code defaultValue}. */
    public Object getItem(String name, Object defaultValue) {
        Object v = extensions.get(name);
        return (v != null || extensions.containsKey(name)) ? v : defaultValue;
    }

    /** Removes one item with specified name from extensions. */
    public Object removeItem(String name) {
        return extensions.remove(name);
    }

    /** Returns number of items in extensions. */
    public int itemSize() {
        return extensions.size();
    }

    /** Returns all names of item in extensions. */
    public Collection<String> itemNames() {
        return extensions.keySet();
    }

    /**
     * Removes all items from extension map.
     */
    public void clearItems() {
        extensions.clear();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        clearItems();
    }

    // extensions
    private Map<String, Object> extensions = new java.util.TreeMap<String, Object>();
}
