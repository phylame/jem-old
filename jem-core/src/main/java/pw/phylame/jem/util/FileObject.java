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

/**
 * Provides read only input source.
 * <p><tt>FileObject</tt> is designed for multiple uses,
 * so that <tt>openStream</tt> can be called any times.</p>
 */
public interface FileObject {
    String UNKNOWN_MIME = "application/octet-stream";

    /**
     * Returns name of file content.
     *
     * @return the name, never be <tt>null</tt>
     */
    String getName();

    /**
     * Returns the MIME type of file content.
     *
     * @return the MIME string, never be <tt>null</tt>
     */
    String getMime();

    /**
     * Opens an <tt>InputStream</tt> for reading file content.
     *
     * @return the stream, never be <tt>null</tt>
     * @throws IOException if occur IO errors
     */
    InputStream openStream() throws IOException;

    /**
     * Resets object for next operation.
     *
     * @throws IOException if occur IO errors
     */
    void reset() throws IOException;

    /**
     * Reads all bytes from file content.
     *
     * @return the byte array, never be <tt>null</tt>
     * @throws IOException if occur IO errors
     */
    byte[] readAll() throws IOException;

    /**
     * Writes file content to specified output.
     *
     * @param output the destination output
     * @return number of written bytes
     * @throws IOException if occur IO errors
     */
    long writeTo(OutputStream output) throws IOException;
}
