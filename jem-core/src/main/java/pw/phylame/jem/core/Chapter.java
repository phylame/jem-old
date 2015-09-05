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
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.util.TextFactory;

/**
 * <p>Abstract chapter model in book contents.</p>
 * <p>The <tt>Chapter</tt> represents base chapter of book, it may be:</p>
 * <ul>
 *     <li>Chapter: common chapter</li>
 *     <li>Section: collection of chapters</li>
 *     <li>and others</li>
 * </ul>
 * <p>A common <tt>Chapter</tt> structure contains following parts:</p>
 * <ul>
 *     <li>attributes map: a string-value map contains information of chapter</li>
 *     <li>text content: main text of the chapter, provided by
 *          <tt>TextObject</tt> source</li>
 *     <li>sub-chapter list: list of sub chapters</li>
 *     <li>clean works: task for cleaning resources and others</li>
 * </ul>
 *
 */
public class Chapter implements Iterable<Chapter>, Attributes {

    /** Constructs chapter used empty title and content. */
    public Chapter() {
        this("", TextFactory.fromString(""), null, null);
    }

    /**
     * Constructs chapter with specified title.
     * @param title title of chapter
     */
    public Chapter(String title) {
        this(title, TextFactory.fromString(""), null, null);
    }

    /**
     * Constructs chapter with specified title and content source.
     * @param title title of chapter
     * @param source text content provider
     */
    public Chapter(String title, TextObject source) {
        this(title, source, null, null);
    }


    /**
     * Constructs chapter with specified title, content source, cover image
     *      and intro text.
     * @param title title of chapter
     * @param source text content provider
     * @param cover <tt>FileObject</tt> contains cover image
     * @param intro intro text
     */
    public Chapter(String title, TextObject source, FileObject cover, TextObject intro) {
        setTitle(title);
        setSource(source);
        setCover(cover);
        setIntro(intro);
    }

    // **************************
    // ** Attributes supported **
    // **************************

    private Map<String, Object> mAttributes = new HashMap<String, Object>();

    /**
     * Associates the specified value with the specified key in attributes map.
     * <p>If the {@code key} not exists add a new attribute, otherwise
     *      overwritten old value.</p>
     * @param key key of the attribute
     * @param value value of the attribute
     */
    public void setAttribute(String key, Object value) {
        mAttributes.put(key, value);
    }

    /**
     * Updates attributes with specified map.
     * @param map the source key-value map
     */
    public void updateAttributes(Map<String, Object> map) {
        mAttributes.putAll(map);
    }

    /**
     * Updates attributes with other chapter.
     * @param other another chapter
     */
    public void updateAttributes(Chapter other) {
        mAttributes.putAll(other.mAttributes);
    }

    /**
     * Tests attribute with <tt>key</tt> is mapped or not.
     * @param key key of the attribute
     * @return <tt>true</tt> if has value mapped to <tt>key</tt>
     *          otherwise <tt>not</tt>
     */
    public boolean hasAttribute(String key) {
        return mAttributes.containsKey(key);
    }

    /**
     * Returns attribute value by its key. If {@code key} not exists
     *      return {@code defaultValue}.
     * @param key key of the attribute
     * @param defaultValue the default value of the key
     * @return the value to which the specified key is mapped, or
     *         <tt>defaultValue</tt> if this map contains no attribute for the key
     */
    public Object getAttribute(String key, Object defaultValue) {
        Object v = mAttributes.get(key);
        if (v != null || mAttributes.containsKey(key)) {
            return v;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns attribute value by its key. If {@code key} not exists
     *      return {@code null}.
     * @param key key of the attribute
     * @return the value to which the specified key is mapped, or
     *         <tt>null</tt> if this map contains no attribute for the key
     */
    public Object getAttribute(String key) {
        return getAttribute(key, null);
    }

    /**
     * Returns attribute value converted to string by its key.
     * @param key key of the attribute
     * @param defaultValue the default value of the key
     * @return the value to which the specified key is mapped, or
     *         <tt>defaultValue</tt> if this map contains no attribute for the key
     */
    public String stringAttribute(String key, String defaultValue) {
        Object v = mAttributes.get(key);
        if (v != null) {
            return String.valueOf(v);
        } else if (! mAttributes.containsKey(key)) {
            return defaultValue;
        } else {
            return null;
        }
    }

    /**
     * Returns attribute value converted to string by its key.
     * @param key key of the attribute
     * @return the value to which the specified key is mapped, or
     *         <tt>null</tt> if this map contains no attribute for the key
     */
    public String stringAttribute(String key) {
        return stringAttribute(key, null);
    }

    /**
     * Removes one attribute from attributes map.
     * @param key key of the attribute
     * @return removed attribute value or <tt>null</tt> if the key not exists.
     */
    public Object removeAttribute(String key) {
        return mAttributes.remove(key);
    }

    /**
     * Removes all attributes from the map.
     */
    public void clearAttributes() {
        mAttributes.clear();
    }

    /**
     * Returns size of attributes map.
     * @return number of attributes
     */
    public int attributeSize() {
        return mAttributes.size();
    }

    /**
     * Returns all names in attributes map.
     * @return sequence of attribute names
     */
    public String[] attributeNames() {
        return mAttributes.keySet().toArray(new String[0]);
    }

    // ********************************
    // ** Shortcut attributes access **
    // ********************************

    /**
     * Sets the title of the <tt>Chapter</tt>.
     * @param title title string
     */
    public void setTitle(String title) {
        setAttribute(TITLE, Objects.requireNonNull(title));
    }

    /**
     * Returns the title of the <tt>Chapter</tt>.
     * @return title string if present or <tt>null</tt>
     */
    public String getTitle() {
        return stringAttribute(TITLE, "");
    }


    /**
     * Returns cover of the chapter.
     * @return cover file or <tt>null</tt> if not present
     */
    public FileObject getCover() {
        Object o = getAttribute(COVER, null);
        if (o instanceof FileObject) {
            return (FileObject) o;
        }
        return null;
    }

    /**
     * Replaces chapter cover with specified cover.
     * @param cover cover provider
     */
    public void setCover(FileObject cover) {
        setAttribute(COVER, cover);
    }

    /**
     * Returns intro content of the chapter.
     * @return intro provider or <tt>null</tt> if not present.
     */
    public TextObject getIntro() {
        Object o = getAttribute(INTRO, null);
        if (o instanceof TextObject) {
            return (TextObject) o;
        }
        return null;
    }

    /**
     * Replaces chapter intro with specified intro provider.
     * @param intro intro content provider
     */
    public void setIntro(TextObject intro) {
        setAttribute(INTRO, intro);
    }

    @Override
    public String toString() {
        return getTitle();
    }

    // ****************************
    // ** Text content supported **
    // ****************************

    /** Text content */
    private TextObject mSource;

    /**
     * Returns the current content source.
     * @return the TextObject
     */
    public TextObject getSource() {
        return mSource;
    }

    /**
     * Replaces source content with specified source.
     * @param source content source
     */
    public void setSource(TextObject source) {
        mSource = Objects.requireNonNull(source);
    }

    // *************************
    // ** Sub-chapter operations **
    // *************************

    /** Sub-chapters */
    private List<Chapter> mChildren = new ArrayList<Chapter>();

    /**
     * Appends the specified chapter to the end of sub-chapter list.
     * @param chapter the <tt>Chapter</tt> to be added
     */
    public void append(Chapter chapter) {
        mChildren.add(Objects.requireNonNull(chapter));
    }

    /**
     * Inserts the specified chapter at specified position in sub-chapter list.
     * @param index index of the chapter to be inserted
     * @param chapter the <tt>Chapter</tt> to be added
     */
    public void insert(int index, Chapter chapter) {
        mChildren.add(index, Objects.requireNonNull(chapter));
    }

    /**
     * Removes the chapter at specified position from sub-chapter list.
     * @param index index of the chapter to be removed
     * @return the chapter at specified position or <tt>null</tt>
     *      if {@code index} not exists
     */
    public Chapter remove(int index) {
        return mChildren.remove(index);
    }

    /**
     * Removes the specified chapter from sub-chapter list.
     * @param chapter chapter to be removed from sub-chapter list, if present
     * @return <tt>true</tt> if sub-chapter list contained the specified chapter
     */
    public boolean remove(Chapter chapter) {
        return mChildren.remove(chapter);
    }

    /**
     * Replaces the chapter at specified position in sub-chapter list with specified chapter.
     * @param index index of the chapter to replace
     * @param chapter chapter to be stored at the specified position
     */
    public void set(int index, Chapter chapter) {
        mChildren.set(index, Objects.requireNonNull(chapter));
    }

    /**
     * Returns the chapter at specified position in sub-chapter list.
     * @param index index of the chapter to return
     * @return the chapter at specified position or <tt>null</tt>
     *      if {@code index} not exists
     */
    public Chapter get(int index) {
        return mChildren.get(index);
    }

    /**
     * Removes all chapters from sub-chapter list.
     */
    public void clear() {
        mChildren.clear();
    }

    /**
     * Returns size of sub-chapter list.
     * @return number of sub-chapters
     */
    public int size() {
        return mChildren.size();
    }

    /**
     * Tests this object is a section or not.
     * <p>A section without text content is a container of chapters.</p>
     * @return <tt>true</tt> if has sub-chapters otherwise <tt>false</tt>
     */
    public boolean isSection() {
        return ! mChildren.isEmpty();
    }

    /**
     * Returns an iterator over sub-chapter list.
     * @return an Iterator for chapter.
     */
    @Override
    public Iterator<Chapter> iterator() {
        return mChildren.iterator();
    }

    // *****************
    // ** Clean works **
    // *****************

    /**
     * Interface for cleaning resources and others when destroying chapter.
     * @since 2.0.1
     */
    public static interface Cleanable {

        /**
         * Cleans the specified <tt>Chapter</tt>.
         * @param chapter the <tt>Chapter</tt> to be cleaned
         */
        void clean(Chapter chapter);
    }

    /** Clean works */
    private List<Cleanable> mWorks = new ArrayList<Cleanable>();

    /**
     * Registers the specified <tt>Cleanable</tt> to clean works list.
     * @param clean the <tt>Cleanable</tt> instance, if <tt>null</tt> do nothing
     * @exception NullPointerException if the specified <tt>clean</tt> is <tt>null</tt>
     */
    public void registerCleanup(Cleanable clean) {
        mWorks.add(Objects.requireNonNull(clean));
    }

    /**
     * Removes the specified <tt>Cleanable</tt> from clean works list.
     * @param clean the <tt>Cleanable</tt> to be removed, if <tt>null</tt> do nothing
     */
    public void removeCleanup(Cleanable clean) {
        if (clean != null) {
            mWorks.remove(clean);
        }
    }

    /**
     * Invokes all <tt>Cleanable</tt> in clean works list and in sub-chapter list.
     * <p>Clean works in sub-chapter list were invoked firstly then invokes this.</p>
     * <p>The clean works list after executing all cleanup works will be cleared.</p>
     * <p>After invoking this method, this <tt>Chapter</tt> should be invalid because
     *      sub-chapter list and attribute map will also be cleared.</p>
     */
    public void cleanup() {
        // clean all sub chapters
        for (Chapter sub : mChildren) {
            sub.cleanup();
        }

        for (Cleanable work : mWorks) {
            work.clean(this);
        }
        mWorks.clear();

        mChildren.clear();
        clearAttributes();
    }
}
