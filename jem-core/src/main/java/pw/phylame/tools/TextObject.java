/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.tools;

import java.util.List;

import java.io.Writer;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import org.apache.commons.io.IOUtils;
import pw.phylame.tools.file.FileObject;

/**
 * Provides unicode text.
 * <p><tt>TextObject</tt> uses some text source:</p>
 * <ul>
 *     <li>raw text: normal text content</li>
 *     <li>text file with encoding: a file contains text content</li>
 * </ul>
 */
public class TextObject {
    protected enum SourceProvider {
        TEXT, FILE
    }

    /** Content type */
    public static final String PLAIN = "plain";
    public static final String HTML = "html";

    /** More than this size of content is large. */
    public static int LARGE_SIZE = 4096;

    /**
     * Constructs object with empty content.
     */
    public TextObject() {
        this("");
    }

    /**
     * Constructs object with specified raw text.
     * @param raw the raw text
     */
    public TextObject(String raw) {
        setType(PLAIN);
        setRaw(raw);
    }

    /**
     * Constructs object with specified file and encoding.
     * @param file file contains text content
     * @param encoding encoding for the file, if <tt>null</tt> means platform encoding
     */
    public TextObject(FileObject file, String encoding) {
        setType(PLAIN);
        setFile(file, encoding);
    }

    /**
     * Returns the previous raw text of the object.
     * @return the raw string
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Sets raw text and changes source to raw text.
     * @param raw the raw text
     */
    public void setRaw(String raw) {
        this.raw = raw != null ? raw : "";
        sourceProvider = SourceProvider.TEXT;
    }

    /**
     * Returns the previous text file of the object.
     * @return the file contains text content or <tt>null</tt> if not present.
     */
    public FileObject getFile() {
        return file;
    }

    /**
     * Returns the current encoding for the content file.
     * @return the encoding or <tt>null</tt> that means platform encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets text file and the encoding, changes source to text file.
     * @param file file contains text content
     * @param encoding encoding for the file, if <tt>null</tt> means platform encoding
     */
    public void setFile(FileObject file, String encoding) {
        setFile(file, encoding, PLAIN);
    }

    /**
     * Sets text file and the encoding, changes source to text file.
     * @param file file contains text content
     * @param encoding encoding for the file, if <tt>null</tt> means platform encoding
     * @param type type of content text
     * @since 2.0.1
     */
    public void setFile(FileObject file, String encoding, String type) {
        if (file == null) {
            throw new NullPointerException();
        }
        this.file = file;
        this.encoding = encoding;
        sourceProvider = SourceProvider.FILE;
        setType(type);
    }

    /**
     * Returns content type.
     * @return the type
     * @since 2.0.1
     */
    public String getType() {
        return contentType;
    }

    /**
     * Sets type of text content
     * @param type the type
     * @since 2.0.1
     */
    public void setType(String type) {
        this.contentType = type;
    }

    /**
     * Returns <tt>true</tt> if the mount of text content is large.
     * @return <tt>true</tt> if large otherwise <tt>false</tt>
     */
    public boolean isLarge() {
        return aboutSize() >= LARGE_SIZE;
    }

    /**
     * Returns about text size in the source.
     * @return the size
     */
    protected long aboutSize() {
        long size;
        switch (sourceProvider) {
            case FILE:
                assert file != null;
                try {
                    size = file.available();
                } catch (IOException e) {
                    size = LARGE_SIZE;
                }
                break;
            default:
                size = raw.length();
                break;
        }
        return size;
    }

    /**
     * Returns text content in source of this object.
     * @return the string of text
     * @throws IOException occurs IO errors when reading text file if source is text file.
     */
    public String getText() throws IOException {
        switch (sourceProvider) {
            case FILE:
                assert file != null;
                InputStream input = new BufferedInputStream(file.openInputStream());
                String text = IOUtils.toString(input, encoding);
                file.reset();
                return text;
            default:
                return raw;
        }
    }

    /**
     * Returns list of lines split from text content in this object.
     * @return list of lines
     * @throws IOException occurs IO errors when reading text file if source is text file.
     */
    public List<String> getLines() throws IOException {
        switch (sourceProvider) {
            case FILE:
                assert file != null;
                InputStream input = new BufferedInputStream(file.openInputStream());
                List<String> lines = IOUtils.readLines(input, encoding);
                file.reset();
                return lines;
            default:
                return java.util.Arrays.asList(raw.split("(\\r\\n)|(\\r)|(\\n)"));
        }
    }

    /**
     * Writes all text content in this object to output writer.
     * @param writer output <tt>Writer</tt> to store text content
     * @throws IOException occurs IO errors
     * @since 2.0.1
     */
    public void writeTo(Writer writer) throws IOException {
        switch (sourceProvider) {
            case FILE:
                assert file != null;
                InputStream input = new BufferedInputStream(file.openInputStream());
                IOUtils.copy(input, writer, encoding);
                file.reset();
                break;
            default:
                writer.write(raw);
                break;
        }
    }

    /** Content type */
    private String contentType;

    /** Raw text */
    private String raw;

    /** Text file */
    private FileObject file;

    /** Encoding of the text file */
    private String encoding;

    /** Content contentType */
    private SourceProvider sourceProvider;
}
