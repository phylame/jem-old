/*
 * Copyright 2015 Peng Wan
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

import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;

/**
 * <p>A part in book contents.</p>
 * <p>The <tt>Part</tt> represents base part of book.</p>
 * <p><tt>Part</tt> structure contains following parts:</p>
 * <ul>
 *     <li>attributes map: Meta attributes map</li>
 *     <li>text content: main text of the part</li>
 *     <li>sub-part list: list of sub parts</li>
 *     <li>clean works: clean resources and others</li>
 * </ul>
 *
 */
public class Part extends Attributes implements Iterable<Part> {
    /** Key name for part title.*/
    public static final String TITLE = "title";

    /** Constructs part used empty title and content. */
    public Part() {
        this("", "");
    }

    /**
     * Constructs part with specified title and text.
     * @param title title of part
     * @param text text content, if <tt>null</tt> sets empty text
     */
    public Part(String title, String text) {
        this(title, new TextObject(text));
    }

    /**
     * Constructs part with specified title and content file and encoding.
     * @param title title of part
     * @param file file contains text
     * @param encoding encoding of the file, if <tt>null</tt> uses platform encoding
     */
    public Part(String title, FileObject file, String encoding) {
        this(title, new TextObject(file, encoding));
    }

    /**
     * Constructs part with specified title and content source.
     * @param title title of part
     * @param content text content provider
     */
    public Part(String title, TextObject content) {
        super();
        setTitle(title);
        if (content == null) {
            throw new NullPointerException("content");
        }
        source = content;
    }

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
        return getStringAttribute(TITLE, "");
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Returns text content of the <tt>Part</tt>.
     * @return content string or <tt>null</tt> if occurs errors.
     */
    public String getContent() {
        try {
            return source.getText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns text content and split by line separator.
     * @return array of lines or <tt>null</tt> if occurs errors.
     */
    public String[] getLines() {
        try {
            return source.getLines();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes all text content to output writer.
     * @param writer output <tt>Writer</tt>
     * @return number of written characters
     */
    public long writeTo(java.io.Writer writer) throws IOException {
        return source.writeTo(writer);
    }

    /**
     * Writes some characters to output writer.
     * @param writer output <tt>Writer</tt>
     * @param size number of characters to written
     * @return number of written characters
     */
    public long writeTo(java.io.Writer writer, long size) throws IOException {
        return source.writeTo(writer, size);
    }

    // ************************
    // ** Sub-part operations
    // ************************

    /** Returns size of sub-part list. */
    public int size() {
        return children.size();
    }

    /** Returns <tt>true</tt> if has sub-part(s). */
    public boolean isSection() {
        return size() != 0;
    }

    /**
     * Appends the specified part to the end of sub-part list.
     * @param part the <tt>Part</tt> to be added
     */
    public void append(Part part) {
        if (part == null) {
            throw new NullPointerException("part");
        }
        children.add(part);
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
        children.add(index, part);
    }

    /**
     * Removes the part at specified position from sub-part list.
     * @param index index of the part to be removed
     * @return the part at specified position or <tt>null</tt> if {@code index} not exists
     */
    public Part remove(int index) {
        return children.remove(index);
    }

    /**
     * Removes the specified part from sub-part list.
     * @param part part to be removed from sub-part list, if present
     * @return <tt>true</tt> if sub-part list contained the specified part
     */
    public boolean remove(Part part) {
        return children.remove(part);
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
        children.set(index, part);
    }

    /**
     * Returns the part at specified position in sub-part list.
     * @param index index of the part to return
     * @return the part at specified position or <tt>null</tt> if {@code index} not exists
     */
    public Part get(int index) {
        return children.get(index);
    }

    /**
     * Returns an iterator over sub-part list.
     * @return an Iterator.
     */
    @Override
    public Iterator<Part> iterator() {
        return children.iterator();
    }

    /**
     * Removes all sub-parts from sub-part list.
     */
    public void clear() {
        children.clear();
    }

    // ****************
    // ** Clean works
    // ****************

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
     * Invokes all <tt>Cleanable</tt> in clean works list.
     * <p>Clears the clean works list after executing all cleanup works.</p>
     */
    public void cleanup() {
        for (Cleanable work: cleanWorks) {
            work.clean(this);
        }
        cleanWorks.clear();
        children.clear();
        clearAttributes();
    }

    // ****************
    // * private data
    // ****************

    /** Sub-parts */
    private List<Part> children = new java.util.ArrayList<Part>();

    /** Text content */
    private TextObject source;

    /** Clean works */
    private List<Cleanable> cleanWorks = new java.util.ArrayList<Cleanable>();
}
