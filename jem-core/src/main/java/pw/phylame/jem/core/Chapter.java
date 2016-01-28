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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;

import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.util.VariantMap;

/**
 * <p>Common chapter model in book contents.</p>
 * <p>The <tt>Chapter</tt> represents base element of book, it may be:</p>
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
     * Constructs chapter used empty title.
     */
    public Chapter() {
        setTitle("");
    }

    /**
     * Constructs chapter with specified title.
     *
     * @param title title of chapter
     * @throws NullPointerException if the <tt>title</tt> is <tt>null</tt>
     */
    public Chapter(String title) {
        setTitle(title);
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
     * @param cover   <tt>FileObject</tt> contains cover image, <tt>null</tt> will be ignored
     * @param intro   intro text, <tt>null</tt> will be ignored
     * @param content text content provider
     * @throws NullPointerException if the argument list contains <tt>null</tt>
     */
    public Chapter(String title, FileObject cover, TextObject intro, TextObject content) {
        setTitle(title);
        setCover(cover);
        setIntro(intro);
        setContent(content);
    }

    Chapter(Chapter other) {
        attributes.update(other.attributes);
        children.addAll(other.children);
        content = other.content;
    }

    // ************************
    // ** Attributes support **
    // ************************

    protected final VariantMap attributes = new VariantMap();

    /**
     * Associates the specified value with the specified name in attributes map.
     * <p>If the <tt>name</tt> not exists add a new attribute, otherwise
     * overwritten old value.</p>
     *
     * @param name  name of the attribute
     * @param value value of the attribute, cannot be <tt>null</tt>
     * @throws NullPointerException if the <tt>value</tt> is <tt>null</tt>
     */
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Updates attributes with specified map.
     *
     * @param map the content key-value map
     * @throws NullPointerException if the <tt>map</tt> is <tt>null</tt>
     */
    public void updateAttributes(Map<String, Object> map) {
        attributes.update(map);
    }

    /**
     * Updates attributes with attributes of specified chapter.
     *
     * @param chapter another chapter
     * @throws NullPointerException if the <tt>chapter</tt> is <tt>null</tt>
     */
    public void updateAttributes(Chapter chapter) {
        attributes.update(chapter.attributes);
    }

    /**
     * Tests attribute with <tt>name</tt> is mapped or not.
     *
     * @param name name of the attribute
     * @return <tt>true</tt> if has value mapped to <tt>name</tt>
     * otherwise <tt>not</tt>
     */
    public boolean hasAttribute(String name) {
        return attributes.contains(name);
    }

    /**
     * Returns attribute by its name. If <tt>name</tt> not exists
     * return <tt>defaultValue</tt>.
     *
     * @param name         name of the attribute
     * @param defaultValue the default value for the name
     * @return the value to which the specified name is mapped, or
     * <tt>defaultValue</tt> if this map contains no attribute for the name
     */
    public Object getAttribute(String name, Object defaultValue) {
        return attributes.get(name, defaultValue);
    }

    /**
     * Returns attribute converted to string by its name.
     *
     * @param name         name of the attribute
     * @param defaultValue the default value for the name
     * @return the value to which the specified name is mapped, or
     * <tt>defaultValue</tt> if this map contains no attribute for the name
     */
    public String stringAttribute(String name, String defaultValue) {
        return attributes.get(name, defaultValue);
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
     * Returns number of attributes.
     *
     * @return number of attributes
     */
    public int attributeCount() {
        return attributes.size();
    }

    /**
     * Returns all names in attributes map.
     *
     * @return sequence of attribute names
     */
    public String[] attributeNames() {
        return attributes.keys();
    }

    /**
     * Returns a set of all attribute name and values.
     *
     * @return the entry set
     * @since 2.3
     */
    public Set<Map.Entry<String, Object>> attributeEntries() {
        return attributes.entries();
    }

    public String getTitle() {
        return stringAttribute(TITLE, "");
    }

    public void setTitle(String title) {
        setAttribute(TITLE, title);
    }

    public FileObject getCover() {
        return attributes.get(COVER, null, FileObject.class);
    }

    public void setCover(FileObject cover) {
        setAttribute(COVER, cover);
    }

    public TextObject getIntro() {
        return attributes.get(INTRO, null, TextObject.class);
    }

    public void setIntro(TextObject intro) {
        setAttribute(INTRO, intro);
    }

    /**
     * Returns word number of content.
     *
     * @return the word number or {@literal 0} if absent.
     * @since 2.3.1
     */
    public Integer getWords() {
        return attributes.get(WORDS, 0, Integer.class);
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
     * @return the TextObject, or <tt>null</tt> if not present
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

    /**
     * Parent of current chapter.
     */
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
     * Sub-chapters list.
     */
    protected final ArrayList<Chapter> children = new ArrayList<>();

    private Chapter checkChapter(Chapter chapter) {
        if (chapter == null) {
            throw new NullPointerException();
        }
        if (chapter.parent != null) {
            throw new IllegalArgumentException("Chapter already in some section: " + chapter);
        }
        return chapter;
    }

    /**
     * Appends the specified chapter to the end of sub-chapter list.
     *
     * @param chapter the <tt>Chapter</tt> to be added
     * @throws NullPointerException if the <tt>chapter</tt> is <tt>null</tt>
     */
    public void append(Chapter chapter) {
        children.add(checkChapter(chapter));
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
        children.add(index, checkChapter(chapter));
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
        if (chapter.parent != this) {  // to be faster
            return -1;
        }
        return children.indexOf(chapter);
    }

    /**
     * Removes the chapter at specified position from sub-chapter list.
     *
     * @param index index of the chapter to be removed
     * @return the chapter at specified position or <tt>null</tt>
     * if <tt>index</tt> not exists
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
        Chapter previous = children.set(index, checkChapter(chapter));
        chapter.parent = this;
        previous.parent = null;
        return previous;
    }

    /**
     * Returns the chapter at specified position in sub-chapter list.
     *
     * @param index index of the chapter to return
     * @return the chapter at specified position or <tt>null</tt>
     * if <tt>index</tt> not exists
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
    private final LinkedList<Cleanable> cleaners = new LinkedList<>();

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
        // remove all attributes
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
            System.err.printf("*** Chapter \"%s@%d\" not cleaned ***\n", getTitle(), hashCode());
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
        Chapter dump = new Chapter();
        dump.attributes.update(attributes);
        dump.content = content;    // TextObject is reusable
        return dump;
    }

    public String debugMessage() {
        return getClass().getSimpleName() + ": attributes=" + attributes;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
