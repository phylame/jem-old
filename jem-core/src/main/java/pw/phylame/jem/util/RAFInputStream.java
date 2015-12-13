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
import java.io.RandomAccessFile;

/**
 * Warps area <tt>RandomAccessFile</tt> as <tt>InputStream</tt>.
 */
public class RAFInputStream extends InputStream {
    private RandomAccessFile source;
    private long begpos, curpos, endpos;

    public RAFInputStream(RandomAccessFile source, long size) throws IOException {
        this(source, source.getFilePointer(), size);
    }

    public RAFInputStream(RandomAccessFile source, long offset, long size) throws IOException {
        if (source == null) {
            throw new NullPointerException("source");
        }

        this.source = source;
        long length = source.length();

        curpos = begpos = (offset < 0) ? 0 : offset;
        endpos = (size < 0) ? length : begpos + size + 1;

        if (curpos >= length) {
            throw new IllegalArgumentException("offset >= length of source");
        }
        if (endpos > length) {
            throw new IllegalArgumentException("offset + size > length of source");
        }
    }

    @Override
    public int read() throws IOException {
        if (curpos + 1 < endpos) {
            ++curpos;
            return source.read();
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (curpos + len < endpos) {
            curpos += len;
            return source.read(b, off, len);
        } else {
            return -1;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        long pos = curpos + n;
        if (pos >= begpos && pos < endpos) {
            curpos = pos;
            return source.skipBytes((int) n);
        } else {
            return 0;
        }
    }

    @Override
    public int available() throws IOException {
        return (int) (endpos - curpos);
    }
}
