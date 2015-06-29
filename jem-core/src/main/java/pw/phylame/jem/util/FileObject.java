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

package pw.phylame.jem.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;

import org.apache.commons.io.IOUtils;

/**
 * Provides read only input source.
 * <p><tt>FileObject</tt> is designed for multiple uses,
 * so that <tt>openInputStream</tt> can be called any times.</p>
 */
public abstract class FileObject {
    /** MIME type */
    private String mime;

    /**
     * Constructs object with specified MIME type.
     * @param mime the MIME type of file content
     */
    public FileObject(String mime) {
        this.mime = mime;
    }

    /**
     * Returns the name of file content.
     * @return the name
     */
    public abstract String getName();

    /**
     * Returns MIME type of file content.
     * @return the MIME name
     */
    public String getMime() {
        return mime;
    }

    /**
     * Opens an {@code InputStream} for reading file content.
     * @return the <tt>InputStream</tt>
     * @throws java.io.IOException occur IO errors
     */
    public abstract InputStream openInputStream() throws IOException;

    /**
     * Resets file object status to last status for next reading.
     * The method should be used after using <tt>openInputStream</tt>.
     * @throws java.io.IOException occur IO errors
     */
    public void reset() throws IOException {}

    /**
     * Reads all bytes from file content.
     * @return the data in file
     * @throws java.io.IOException occur IO errors
     */
    public byte[] readAll() throws IOException {
        byte[] bytes = IOUtils.toByteArray(new BufferedInputStream(openInputStream()));
        reset();
        return bytes;
    }

    /**
     * Writes file content to <tt>OutputStream</tt>.
     * @param out the <tt>OutputStream</tt>
     * @return number of copied bytes
     * @throws java.io.IOException occur IO errors
     */
    public long copyTo(OutputStream out) throws IOException {
        long total = IOUtils.copy(
                new BufferedInputStream(openInputStream()), out);
        reset();
        return total;
    }

    /**
     * Writes some bytes of file content to <tt>OutputStream</tt>.
     * @param out the <tt>OutputStream</tt>
     * @param size number of bytes to written, if {@code -1} writes all bytes.
     * @return number of copied bytes
     * @throws java.io.IOException occur IO errors
     */
    public long copyTo(OutputStream out, long size) throws IOException {
        long total = IOUtils.copyLarge(
                new BufferedInputStream(openInputStream()), out, 0, size);
        reset();
        return total;
    }

    /**
     * Gets available bytes to be read.
     * @return the size or <tt>-1</tt> if not supported
     * @throws java.io.IOException occur IO errors
     */
    public long available() throws IOException {
        return -1;
    }

    @Override
    public String toString() {
        return getName();
    }
}
