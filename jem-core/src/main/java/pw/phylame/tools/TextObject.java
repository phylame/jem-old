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

import pw.phylame.tools.file.FileObject;
import pw.phylame.tools.file.FileUtils;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

/**
 * Provides unicode text.
 * <p><tt>TextObject</tt> uses some text source:</p>
 * <ul>
 *     <li>raw text: normal text content</li>
 *     <li>text file with encoding: a file contains text content</li>
 * </ul>
 */
public class TextObject {
    protected static enum SourceType {
        TEXT, FILE
    }

    /** More than this size of content is large. */
    public static int LARGE_SIZE = 4096;

    /**
     * Constructs object with specified raw text.
     * @param raw the raw text
     */
    public TextObject(String raw) {
        setRaw(raw);
    }

    /**
     * Constructs object with specified file and encoding.
     * @param file file contains text content
     * @param encoding encoding for the file, if <tt>null</tt> uses platform encoding
     */
    public TextObject(FileObject file, String encoding) {
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
        sourceType = SourceType.TEXT;
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
     * @return the encoding or <tt>null</tt> that uses platform encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets text file and the encoding, changes source to text file.
     * @param file file contains text content
     * @param encoding encoding for the file, if <tt>null</tt> uses platform encoding
     */
    public void setFile(FileObject file, String encoding) {
        if (file == null) {
            throw new NullPointerException();
        }
        this.file = file;
        this.encoding = encoding;
        sourceType = SourceType.FILE;
    }

    /**
     * Returns <tt>true</tt> if the text content is large.
     * @return <tt>true</tt> if large otherwise <tt>false</tt>
     */
    public boolean isLarge() {
        long size;
        switch (sourceType) {
            case FILE:
                try {
                    size = getFile().available();
                } catch (IOException e) {
//                    e.printStackTrace();
                    size = LARGE_SIZE;
                }
                break;
            default:
                size = getRaw().length();
                break;
        }
        return size >= LARGE_SIZE;
    }

    /**
     * Opens a reader to read text if source is text file.
     * @return the <tt>Reader</tt> or <tt>null</tt> if source is not text file
     * @throws java.io.IOException cannot create reader
     */
    private Reader openReader() throws IOException {
        if (file == null) {
            return null;
        }
        if (encoding != null) {
            return new java.io.InputStreamReader(file.openInputStream(), encoding);
        } else {
            return new java.io.InputStreamReader(file.openInputStream());
        }
    }

    /**
     * Returns text content in source of this object.
     * @return the string of text
     * @throws java.io.IOException occurs IO errors when reading text file if source is text file.
     */
    public String getText() throws IOException {
        switch (sourceType) {
            case FILE:
                Reader reader = openReader();
                assert reader != null;
                String text = FileUtils.readText(reader);
                getFile().reset();
                return text;
            default:
                return getRaw();
        }
    }

    /**
     * Returns list of lines split from text content in this object.
     * @return list of lines
     * @throws java.io.IOException occurs IO errors when reading text file if source is text file.
     */
    public String[] getLines() throws IOException {
        return getLines(false);
    }

    /**
     * Returns list of lines split from text content in this object.
     * @param skipEmptyLine <tt>true</tt> to skips empty lines otherwise keeps all lines
     * @return list of lines
     * @throws java.io.IOException occurs IO errors when reading text file if source is text file.
     */
    public String[] getLines(boolean skipEmptyLine) throws IOException {
        switch (sourceType) {
            case FILE:
                Reader reader = openReader();
                assert reader != null;
                String[] lines = FileUtils.readLines(reader, skipEmptyLine);
                getFile().reset();
                return lines;
            default:
                return getRaw().split("(\\r\\n)|(\\r)|(\\n)");
        }
    }

    /**
     * Writes all text content in this object to output writer.
     * @param writer output <tt>Writer</tt> to store text content
     * @return the total number of written characters
     * @throws java.io.IOException occurs IO errors
     */
    public long writeTo(Writer writer) throws IOException {
        return writeTo(writer, -1);
    }

    /**
     * Writes some number of characters in text content to output writer.
     * @param writer output <tt>Writer</tt> to store characters
     * @param size the maximum number of characters to be written, if <tt>-1</tt> copies all
     * @return the total number of written characters
     * @throws java.io.IOException occurs IO errors
     */
    public long writeTo(Writer writer, long size) throws IOException {
        long total;
        switch (sourceType) {
            case FILE:
                Reader reader = openReader();
                assert reader != null;
                total = FileUtils.copy(reader, writer, size);
                getFile().reset();
                break;
            default:
                writer.write(getRaw());
                total = getRaw().length();
                break;
        }
        return total;
    }


    /** Raw text */
    private String raw;

    /** Text file */
    private FileObject file;

    /** Encoding of the text file */
    private String encoding;

    /** Content type */
    private SourceType sourceType;
}
