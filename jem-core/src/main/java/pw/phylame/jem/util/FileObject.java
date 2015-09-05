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
    String getName();

    String getMime();

    InputStream openStream() throws IOException;

    void reset() throws IOException;

    byte[] readAll() throws IOException;

    long writeTo(OutputStream output) throws IOException;
}
