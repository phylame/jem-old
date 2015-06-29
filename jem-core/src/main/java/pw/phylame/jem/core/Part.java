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
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Writer;
import java.io.IOException;

import pw.phylame.jem.util.TextObject;

/**
 * <p>Abstract part model in book contents.</p>
 * <p>The <tt>Part</tt> represents base part of book.</p>
 * <p>A common <tt>Part</tt> structure contains following parts:</p>
 * <ul>
 *     <li>attributes map: a string-value map contains information of part</li>
 *     <li>text content: main text of the part, provided by
 *          <tt>TextObject</tt> source</li>
 *     <li>sub-part list: list of sub parts</li>
 *     <li>clean works: task for cleaning resources and others</li>
 * </ul>
 *
 */
public class Part implements Iterable<Part>, FieldConstants {

    /** Constructs part used empty title and content. */
    public Part() {
        this("", new TextObject());
    }

    /**
     * Constructs part with specified title.
     * @param title title of part
     */
    public Part(String title) {
        this(title, new TextObject());
    }

    /**
     * Constructs part with specified title and content source.
     * @param title title of part
     * @param content text content provider
     */
    public Part(String title, TextObject content) {
        super();
        setTitle(title);
        setSource(content);
    }

    // **************************
    // ** Attributes supported **
    // **************************

    private Map<String, Object> metaMap = new TreeMap<String, Object>();

    /**
     * Associates the specified value with the specified key in attributes map.
     * <p>If the {@code key} not exists add a new attribute, otherwise
     *      overwritten old value.</p>
     * @param key key of the attribute
     * @param value value of the attribute
     */
    public void setAttribute(String key, Object value) {
        metaMap.put(key, value);
    }

    /**
     * Updates attributes with specified map.
     * @param metaMap the source map
     */
    public void updateAttributes(Map<String, Object> metaMap) {
        this.metaMap.putAll(metaMap);
    }

    /**
     * Updates attributes with other part.
     * @param other another part
     */
    public void updateAttributes(Part other) {
        updateAttributes(other.metaMap);
    }

    /**
     * Tests attribute with <tt>key</tt> is mapped or not.
     * @param key key of the attribute
     * @return <tt>true</tt> if has value mapped to <tt>key</tt>
     *          otherwise <tt>not</tt>
     */
    public boolean hasAttribute(String key) {
        return metaMap.containsKey(key);
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
        Object v = metaMap.get(key);
        if (v != null || metaMap.containsKey(key)) {
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
        return metaMap.remove(key);
    }

    /**
     * Removes all attributes from the map.
     */
    public void clearAttributes() {
        metaMap.clear();
    }

    /**
     * Returns size of attributes map.
     * @return number of attributes
     */
    public int attributeSize() {
        return metaMap.size();
    }

    /**
     * Returns all names in attributes map.
     * @return sequence of attribute names
     */
    public Set<String> attributeNames() {
        return metaMap.keySet();
    }

    // ********************************
    // ** Shortcut attributes access **
    // ********************************

    /**
     * Sets the title of the <tt>Part</tt>.
     * @param title title string
     */
    public void setTitle(String title) {
        setAttribute(TITLE, title);
    }

    /**
     * Returns the title of the <tt>Part</tt>.
     * @return title string if present or <tt>null</tt>
     */
    public String getTitle() {
        return stringAttribute(TITLE, "");
    }

    @Override
    public String toString() {
        return getTitle();
    }

    // ****************************
    // ** Text content supported **
    // ****************************

    /** Text content */
    private TextObject source;

    /**
     * Returns the current content source.
     * @return the TextObject
     */
    public TextObject getSource() {
        return source;
    }

    /**
     * Replaces source content with specified source.
     * @param source content source
     */
    public void setSource(TextObject source) {
        if (source == null) {
            throw new NullPointerException("source");
        }
        this.source = source;
    }

    /**
     * Returns text content of the <tt>Part</tt>.
     * @return content string
     * @throws IOException occurs IO errors when load text from text source
     * @deprecated use {@link #getSource()} for {@link TextObject#getText()}
     *      instead.
     */
    @Deprecated
    public String getText() throws IOException {
        return source.getText();
    }

    /**
     * Returns text content and split by line separator.
     * @return list of lines
     * @throws IOException occurs IO errors when load text from text source
     * @deprecated use {@link #getSource()} for {@link TextObject#getLines()}
     *      instead.
     */
    @Deprecated
    public List<String> getLines() throws IOException {
        return source.getLines();
    }

    /**
     * Writes some characters to output writer.
     * @param writer output <tt>Writer</tt>
     * @throws java.io.IOException occurs IO errors when loading source or
     *      write IO device.
     * @since 2.0.1
     * @deprecated use {@link #getSource()} for {@link TextObject#writeTo(Writer)}
     *      instead.
     */
    @Deprecated
    public void writeTo(java.io.Writer writer) throws IOException {
        source.writeTo(writer);
    }

    // *************************
    // ** Sub-part operations **
    // *************************

    /** Sub-parts */
    private List<Part> subParts = new ArrayList<Part>();

    /**
     * Appends the specified part to the end of sub-part list.
     * @param part the <tt>Part</tt> to be added
     */
    public void append(Part part) {
        if (part == null) {
            throw new NullPointerException("part");
        }
        subParts.add(part);
    }

    /**
     * Inserts the specified part at specified position in sub-part list.
     * @param index index of the part to be inserted
     * @param part the <tt>Part</tt> to be added
     */
    public void insert(int index, Part part) {
        if (part == null) {
            throw new NullPointerException("part");
        }
        subParts.add(index, part);
    }

    /**
     * Removes the part at specified position from sub-part list.
     * @param index index of the part to be removed
     * @return the part at specified position or <tt>null</tt>
     *      if {@code index} not exists
     */
    public Part remove(int index) {
        return subParts.remove(index);
    }

    /**
     * Removes the specified part from sub-part list.
     * @param part part to be removed from sub-part list, if present
     * @return <tt>true</tt> if sub-part list contained the specified part
     */
    public boolean remove(Part part) {
        return subParts.remove(part);
    }

    /**
     * Replaces the part at specified position in sub-part list with specified part.
     * @param index index of the part to replace
     * @param part part to be stored at the specified position
     */
    public void set(int index, Part part) {
        if (part == null) {
            throw new NullPointerException("part");
        }
        subParts.set(index, part);
    }

    /**
     * Returns the part at specified position in sub-part list.
     * @param index index of the part to return
     * @return the part at specified position or <tt>null</tt>
     *      if {@code index} not exists
     */
    public Part get(int index) {
        return subParts.get(index);
    }

    /**
     * Removes all parts from sub-part list.
     */
    public void clear() {
        subParts.clear();
    }

    /**
     * Returns size of sub-part list.
     * @return number of sub-parts
     */
    public int size() {
        return subParts.size();
    }

    /**
     * Tests this object is a section or not.
     * <p>A section is a container of part or chapter and without text content.</p>
     * @return <tt>true</tt> if has sub-parts otherwise <tt>false</tt>
     */
    public boolean isSection() {
        return size() != 0;
    }

    /**
     * Returns an iterator over sub-part list.
     * @return an Iterator for part.
     */
    @Override
    public Iterator<Part> iterator() {
        return subParts.iterator();
    }

    /**
     * Returns sub-parts list.
     * @return the list
     * @deprecated not use this method directly
     */
    @Deprecated
    public List<Part> partList() {
        return subParts;
    }

    // *****************
    // ** Clean works **
    // *****************

    /**
     * Interface for cleaning resources and others when destroying part.
     * @since 2.0.1
     */
    public static interface Cleanable {

        /**
         * Cleans the specified <tt>Part</tt>.
         * @param part the <tt>Part</tt> to be cleaned
         */
        void clean(Part part);
    }

    /** Clean works */
    private List<Cleanable> cleanWorks = new ArrayList<Cleanable>();

    /**
     * Registers the specified <tt>Cleanable</tt> to clean works list.
     * @param clean the <tt>Cleanable</tt> instance, if <tt>null</tt> do nothing
     */
    public void registerCleanup(Cleanable clean) {
        if (clean != null) {
            cleanWorks.add(clean);
        }
    }

    /**
     * Removes the specified <tt>Cleanable</tt> from clean works list.
     * @param clean the <tt>Cleanable</tt> to be removed, if <tt>null</tt> do nothing
     */
    public void removeCleanup(Cleanable clean) {
        if (clean != null) {
            cleanWorks.remove(clean);
        }
    }

    /**
     * Invokes all <tt>Cleanable</tt> in clean works list and in sub-part list.
     * <p>Clean works in sub-part list invoked firstly then invokes this.</p>
     * <p>The clean works list after executing all cleanup works will be cleared.</p>
     * <p>After invoking this method, this <tt>Part</tt> should be invalid because
     *      sub-part list and attribute map will also be cleared.</p>
     */
    public void cleanup() {
        for (Part sub: subParts) {
            sub.cleanup();
        }

        for (Cleanable work: cleanWorks) {
            work.clean(this);
        }
        cleanWorks.clear();

        subParts.clear();
        clearAttributes();
    }
}
