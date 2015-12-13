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
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;

import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.util.TextFactory;

/**
 * <p>Abstract chapter model in book contents.</p>
 * <p>The <tt>Chapter</tt> represents base chapter of book, it may be:</p>
 * <ul>
 * <li>Chapter: common chapter</li>
 * <li>Section: collection of chapters</li>
 * <li>and others</li>
 * </ul>
 * <p>A common <tt>Chapter</tt> structure contains following parts:</p>
 * <ul>
 * <li>attributes map: a string-value map contains information of chapter</li>
 * <li>text content: main text of the chapter, provided by
 * <tt>TextObject</tt> content</li>
 * <li>sub-chapter list: list of sub chapters</li>
 * <li>clean works: task for cleaning resources and others</li>
 * </ul>
 */
public class Chapter implements Iterable<Chapter>, Attributes {

    /**
     * Constructs chapter used empty title and content.
     */
    public Chapter() {
        this("", TextFactory.emptyText());
    }

    /**
     * Constructs chapter with specified title.
     *
     * @param title title of chapter
     * @throws NullPointerException if the <tt>title</tt> is <tt>null</tt>
     */
    public Chapter(String title) {
        this(title, TextFactory.emptyText());
    }

    /**
     * Constructs chapter with specified title and content.
     *
     * @param title   title of chapter
     * @param content text content provider
     * @throws NullPointerException if the argument list contains <tt>null</tt>
     */
    public Chapter(String title, TextObject content) {
        setTitle(title);
        setContent(content);
    }


    /**
     * Constructs chapter with specified title, content, cover image
     * and intro text.
     *
     * @param title   title of chapter
     * @param content text content provider
     * @param cover   <tt>FileObject</tt> contains cover image, <tt>null</tt> will be ignored
     * @param intro   intro text, <tt>null</tt> will be ignored
     * @throws NullPointerException if the argument list contains <tt>null</tt>
     */
    public Chapter(String title, TextObject content, FileObject cover, TextObject intro) {
        setTitle(title);
        setContent(content);
        setCover(cover);
        setIntro(intro);
    }

    // ************************
    // ** Attributes support **
    // ************************

    protected final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Associates the specified value with the specified name in attributes map.
     * <p>If the {@code name} not exists add a new attribute, otherwise
     * overwritten old value.</p>
     *
     * @param name  name of the attribute
     * @param value value of the attribute, cannot be <tt>null</tt>
     * @throws NullPointerException if the <tt>value</tt> is <tt>null</tt>
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        attributes.put(name, value);
    }

    public void updateAttributes(Map<String, Object> map) {
        updateAttributes(map, false);
    }

    /**
     * Updates attributes with specified map.
     *
     * @param map            the content key-value map
     * @param removePresents <tt>true</tt> to remove all presented attributes
     *                       before updating
     * @throws NullPointerException if the <tt>map</tt> is <tt>null</tt>
     */
    public void updateAttributes(Map<String, Object> map, boolean removePresents) {
        if (map == null) {
            throw new NullPointerException();
        }
        if (removePresents) {
            attributes.clear();
        }
        attributes.putAll(map);
    }

    public void updateAttributes(Chapter ch) {
        updateAttributes(ch, false);
    }

    /**
     * Updates attributes with other chapter.
     *
     * @param ch             another chapter
     * @param removePresents <tt>true</tt> to remove all presented attributes
     *                       before updating
     * @throws NullPointerException if the <tt>ch</tt> is <tt>null</tt>
     */
    public void updateAttributes(Chapter ch, boolean removePresents) {
        if (ch == null) {
            throw new NullPointerException();
        }
        updateAttributes(ch.attributes, removePresents);
    }

    /**
     * Tests attribute with <tt>name</tt> is mapped or not.
     *
     * @param name name of the attribute
     * @return <tt>true</tt> if has value mapped to <tt>name</tt>
     * otherwise <tt>not</tt>
     */
    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Returns attribute value by its name. If {@code name} not exists
     * return {@code defaultValue}.
     *
     * @param name         name of the attribute
     * @param defaultValue the default value of the name
     * @return the value to which the specified name is mapped, or
     * <tt>defaultValue</tt> if this map contains no attribute for the name
     */
    public Object getAttribute(String name, Object defaultValue) {
        Object v = attributes.get(name);
        return (v != null) ? v : defaultValue;
    }

    /**
     * Returns attribute value by its name. If {@code name} not exists
     * return {@code null}.
     *
     * @param name name of the attribute
     * @return the value to which the specified name is mapped, or
     * <tt>null</tt> if this map contains no attribute for the name
     */
    public Object getAttribute(String name) {
        return getAttribute(name, null);
    }

    /**
     * Returns attribute value converted to string by its name.
     *
     * @param name         name of the attribute
     * @param defaultValue the default value of the name
     * @return the value to which the specified name is mapped, or
     * <tt>defaultValue</tt> if this map contains no attribute for the name
     */
    public String stringAttribute(String name, String defaultValue) {
        Object v = attributes.get(name);
        return (v != null) ? v.toString() : defaultValue;
    }

    /**
     * Returns attribute value converted to string by its name.
     *
     * @param name name of the attribute
     * @return the value to which the specified name is mapped, or
     * <tt>null</tt> if this map contains no attribute for the name
     */
    public String stringAttribute(String name) {
        return stringAttribute(name, null);
    }

    /**
     * Removes one attribute from attributes map.
     *
     * @param name name of the attribute
     * @return removed attribute value or <tt>null</tt> if the name not exists.
     */
    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

    /**
     * Removes all attributes from the map.
     */
    public void clearAttributes() {
        attributes.clear();
    }

    /**
     * Returns size of attributes map.
     *
     * @return number of attributes
     */
    public int attributeSize() {
        return attributes.size();
    }

    /**
     * Returns all names in attributes map.
     *
     * @return sequence of attribute names
     */
    public String[] attributeNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

    /**
     * Returns a set of all attribute name and values.
     *
     * @return the entry set
     * @since 2.3
     */
    public Set<Map.Entry<String, Object>> attributeEntries() {
        return attributes.entrySet();
    }

    public String getTitle() {
        return stringAttribute(TITLE, "");
    }

    public void setTitle(String title) {
        setAttribute(TITLE, title);
    }

    public FileObject getCover() {
        Object o = getAttribute(COVER);
        if (o instanceof FileObject) {
            return (FileObject) o;
        }
        return null;
    }

    public void setCover(FileObject cover) {
        setAttribute(COVER, cover);
    }

    public TextObject getIntro() {
        Object o = getAttribute(INTRO);
        if (o instanceof TextObject) {
            return (TextObject) o;
        }
        return null;
    }

    public void setIntro(TextObject intro) {
        setAttribute(INTRO, intro);
    }

    /**
     * Returns word number of content.
     *
     * @return the word number
     * @since 2.3.1
     */
    public Integer getWords() {
        Object o = getAttribute(WORDS);
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return null;
    }

    /**
     * Sets word number of content.
     *
     * @param words the word number
     * @since 2.3.1
     */
    public void setWords(int words) {
        setAttribute(WORDS, words);
    }

    // **************************
    // ** Text content support **
    // **************************

    /**
     * Text content
     */
    protected TextObject content;

    /**
     * Returns the current content.
     *
     * @return the TextObject
     * @since 2.3.1
     */
    public TextObject getContent() {
        return content;
    }

    /**
     * Replaces content with specified content.
     *
     * @param content content
     * @throws NullPointerException if the <tt>content</tt> is <tt>null</tt>
     * @since 2.3.1
     */
    public void setContent(TextObject content) {
        if (content == null) {
            throw new NullPointerException();
        }
        this.content = content;
    }

    // ****************************
    // ** Sub-chapter operations **
    // ****************************

    private Chapter parent = null;

    /**
     * Returns parent chapter of current chapter.
     *
     * @return the parent or <tt>null</tt> if not present
     */
    public Chapter getParent() {
        return parent;
    }

    /**
     * Sub-chapters
     */
    protected final List<Chapter> children = new ArrayList<Chapter>();

    private void checkChapter(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        if (chapter.getParent() != null) {
            throw new IllegalArgumentException("chapter already in a certain section");
        }
    }

    /**
     * Appends the specified chapter to the end of sub-chapter list.
     *
     * @param chapter the <tt>Chapter</tt> to be added
     * @throws NullPointerException if the <tt>chapter</tt> is <tt>null</tt>
     */
    public void append(Chapter chapter) {
        checkChapter(chapter);
        children.add(chapter);
        chapter.parent = this;
    }

    /**
     * Inserts the specified chapter at specified position in sub-chapter list.
     *
     * @param index   index of the chapter to be inserted
     * @param chapter the <tt>Chapter</tt> to be added
     * @throws NullPointerException      if the <tt>chapter</tt> is <tt>null</tt>
     * @throws IndexOutOfBoundsException if the index is out of
     *                                   range (index &lt; 0 || index &ge; size())
     */
    public void insert(int index, Chapter chapter) {
        checkChapter(chapter);
        children.add(index, chapter);
        chapter.parent = this;
    }

    /**
     * Returns the index of the first occurrence of the specified chapter in
     * sub chapters list.
     *
     * @param chapter the chapter to search of
     * @return the index or <tt>-1</tt> if specified chapter not presents
     * @throws NullPointerException if the <tt>chapter</tt> is <tt>null</tt>
     */
    public int indexOf(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        // not contained in children list
        if (chapter.getParent() != this) {  // to be faster
            return -1;
        }
        return children.indexOf(chapter);
    }

    /**
     * Removes the chapter at specified position from sub-chapter list.
     *
     * @param index index of the chapter to be removed
     * @return the chapter at specified position or <tt>null</tt>
     * if {@code index} not exists
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &ge; size())
     */
    public Chapter removeAt(int index) {
        Chapter chapter = children.remove(index);
        chapter.parent = null;
        return chapter;
    }

    /**
     * Removes the specified chapter from sub-chapter list.
     *
     * @param chapter chapter to be removed from sub-chapter list, if present
     * @return <tt>true</tt> if sub-chapter list contained the specified chapter
     * @throws NullPointerException if the <tt>chapter</tt> is <tt>null</tt>
     */
    public boolean remove(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        // not contained in children list
        if (chapter.getParent() != this) {  // to be faster
            return false;
        }
        if (children.remove(chapter)) {     // contained in list
            chapter.parent = null;
            return true;
        }
        return false;
    }

    /**
     * Replaces the chapter at specified position in sub-chapter list with specified chapter.
     *
     * @param index   index of the chapter to replace
     * @param chapter chapter to be stored at the specified position
     * @return the chapter previously at the specified position
     * @throws NullPointerException      if the <tt>chapter</tt> is <tt>null</tt>
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &ge; size())
     */
    public Chapter replace(int index, Chapter chapter) {
        checkChapter(chapter);
        Chapter oldChapter = children.set(index, chapter);
        chapter.parent = this;
        oldChapter.parent = null;
        return oldChapter;
    }

    /**
     * Returns the chapter at specified position in sub-chapter list.
     *
     * @param index index of the chapter to return
     * @return the chapter at specified position or <tt>null</tt>
     * if {@code index} not exists
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &ge; size())
     */
    public Chapter chapterAt(int index) {
        return children.get(index);
    }

    /**
     * Removes all chapters from sub-chapter list.
     */
    public void clear() {
        for (Chapter chapter : children) {
            chapter.parent = null;
        }
        children.clear();
    }

    /**
     * Returns size of sub-chapter list.
     *
     * @return number of sub-chapters
     */
    public int size() {
        return children.size();
    }

    /**
     * Tests this object is a section or not.
     * <p>A section without text content is a container of chapters.</p>
     *
     * @return <tt>true</tt> if has sub-chapters otherwise <tt>false</tt>
     */
    public boolean isSection() {
        return !children.isEmpty();
    }

    /**
     * Returns an iterator over sub-chapter list.
     *
     * @return an Iterator for chapter.
     */
    @Override
    public Iterator<Chapter> iterator() {
        return children.iterator();
    }

    // *****************
    // ** Clean works **
    // *****************

    /**
     * Interface for cleaning resources and others when destroying chapter.
     *
     * @since 2.0.1
     */
    public interface Cleanable {
        /**
         * Cleans the specified <tt>Chapter</tt>.
         *
         * @param chapter the <tt>Chapter</tt> to be cleaned
         */
        void clean(Chapter chapter);
    }

    /**
     * Clean works
     */
    private final List<Cleanable> cleaners = new LinkedList<Cleanable>();

    /**
     * Registers the specified <tt>Cleanable</tt> to clean works list.
     *
     * @param clean the <tt>Cleanable</tt> instance, if <tt>null</tt> do nothing
     * @throws NullPointerException if the specified <tt>clean</tt> is <tt>null</tt>
     */
    public void registerCleanup(Cleanable clean) {
        if (clean == null) {
            throw new NullPointerException();
        }
        cleaners.add(clean);
    }

    /**
     * Removes the specified <tt>Cleanable</tt> from clean works list.
     *
     * @param clean the <tt>Cleanable</tt> to be removed, if <tt>null</tt> do nothing
     */
    public void removeCleanup(Cleanable clean) {
        if (clean != null) {
            cleaners.remove(clean);
        }
    }

    /**
     * Invokes all <tt>Cleanable</tt> in clean works list and in sub-chapter list.
     * <p>Clean works in sub-chapter list were invoked firstly then invokes this.</p>
     * <p>The clean works list after executing all cleanup works will be cleared.</p>
     * <p>After invoking this method, this <tt>Chapter</tt> should be invalid because
     * sub-chapter list and attribute map will also be cleared.</p>
     */
    public void cleanup() {
        for (Cleanable work : cleaners) {
            work.clean(this);
        }
        cleaners.clear();
        clearAttributes();

        // clean all sub chapters
        for (Chapter sub : children) {
            sub.cleanup();
        }
        children.clear();

        cleaned = true;
    }

    private boolean cleaned = false;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (!cleaned) {
            System.err.printf("***Chapter \"%s@%d\" not cleaned***\n", getTitle(), hashCode());
        }
    }

    /**
     * Returns a copy of current chapter.
     * <p>The current chapter must be a simple chapter without any sub-chapters,
     * see {@link Chapter#isSection()}.
     * <p><strong>Just</strong> attributes and content will be copied.
     *
     * @return the new copy chapter
     * @throws UnsupportedOperationException if current chapter is section
     */
    public Chapter copy() {
        if (isSection()) {
            throw new UnsupportedOperationException("copy a section is unsupported");
        }
        Chapter aCopy = new Chapter();
        aCopy.attributes.putAll(attributes);
        aCopy.content = content;    // TextObject is read-only
        return aCopy;
    }

    public String debugMessage() {
        return getClass().getSimpleName() + ": attributes=" + attributes;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
