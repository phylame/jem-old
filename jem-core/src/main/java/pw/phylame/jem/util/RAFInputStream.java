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
 * Wrapper for block of <tt>RandomAccessFile</tt> as <tt>InputStream</tt>.
 */
public class RAFInputStream extends InputStream {
    private RandomAccessFile source;
    private long curpos;
    private long endpos;    // pos: size + 1

    public RAFInputStream(RandomAccessFile source, long size) throws IOException {
        this(source, source.getFilePointer(), size);
    }

    public RAFInputStream(RandomAccessFile source, long offset, long size) throws IOException {
        if (source == null) {
            throw new NullPointerException("source");
        }

        this.source = source;
        long length = source.length();

        curpos = (offset < 0) ? 0 : offset;
        endpos = (size < 0) ? length : curpos + size + 1;

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
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        long count = endpos - curpos - 1;
        if (count == 0) {
            return -1;
        }
        count = count < len ? count : len;
        len = source.read(b, off, (int) count);
        curpos += count;
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n < 0) {
            return 0;
        }
        n = source.skipBytes((int) Math.min(n, endpos - curpos - 1));
        curpos = endpos;
        return n;
    }

    @Override
    public int available() throws IOException {
        return (int) (endpos - curpos - 1);
    }
}
