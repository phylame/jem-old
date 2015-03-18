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

package pw.phylame.tools.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides read only input source.
 * <p><tt>FileObject</tt> is designed for multiple uses,
 * so like <tt>openInputStream</tt> can be called any times.</p>
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

    /** Returns the name of file content. */
    public abstract String getName();

    /** Returns MIME type of file content. */
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
     * Resets file object status to last status.
     * The method should be used after using <tt>openInputStream</tt>.
     */
    public void reset() {}

    /**
     * Writes file content to <tt>OutputStream</tt>.
     * @param out the <tt>OutputStream</tt>
     * @return number of copied bytes
     * @throws java.io.IOException occur IO errors
     */
    public long copyTo(OutputStream out) throws IOException {
        return copyTo(out, -1);
    }

    /**
     * Writes some bytes of file content to <tt>OutputStream</tt>.
     * @param out the <tt>OutputStream</tt>
     * @param size number of bytes to written, if {@code -1} writes all bytes.
     * @return number of copied bytes
     * @throws java.io.IOException occur IO errors
     */
    public long copyTo(OutputStream out, long size) throws IOException {
        InputStream in = openInputStream();
        assert in != null;
        return FileUtils.copy(in, out, size);
    }
}
