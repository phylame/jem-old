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
 * Abstract <tt>FileObject</tt> implement.
 */
public abstract class AbstractFile implements FileObject {
    private String mime;

    protected AbstractFile(String mime) {
        if (mime == null) {
            throw new NullPointerException();
        }
        this.mime = mime;
    }

    @Override
    public String getMime() {
        return mime;
    }

    @Override
    public byte[] readAll() throws IOException {
        InputStream input = null;
        try {
            InputStream stream = openStream();
            assert stream != null;
            input = new BufferedInputStream(stream);
            return IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
            reset();
        }
    }

    @Override
    public void reset() throws IOException {

    }

    @Override
    public long writeTo(OutputStream output) throws IOException {
        InputStream input = null;
        try {
            InputStream stream = openStream();
            assert stream != null;
            input = new BufferedInputStream(stream);
            return IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
            reset();
        }
    }

    @Override
    public String toString() {
        return getName() + ";mime=" + getMime();
    }
}
