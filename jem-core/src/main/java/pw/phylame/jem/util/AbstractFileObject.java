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

import java.util.Objects;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import org.apache.commons.io.IOUtils;

/**
 * Abstract <tt>FileObject</tt> implement.
 */
public abstract class AbstractFileObject implements FileObject {
    private String mMime;

    protected AbstractFileObject(String mime) {
        mMime = Objects.requireNonNull(mime);
    }

    @Override
    public String getMime() {
        return mMime;
    }

    @Override
    public byte[] readAll() throws IOException {
        InputStream stream = openStream();
        assert stream != null;
        BufferedInputStream input = new BufferedInputStream(stream);
        byte[] bytes = IOUtils.toByteArray(input);
        input.close();
        reset();
        return bytes;
    }

    @Override
    public void reset() throws IOException {

    }

    @Override
    public long writeTo(OutputStream output) throws IOException {
        InputStream stream = openStream();
        assert stream != null;
        BufferedInputStream input = new BufferedInputStream(stream);
        long total = IOUtils.copy(input, output);
        input.close();
        reset();
        return total;
    }
}
