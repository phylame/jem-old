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

package pw.phylame.jem.formats.ebk;

import java.io.*;
import java.util.ArrayList;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.AbstractText;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.common.BinaryParser;
import pw.phylame.jem.formats.util.ZLibUtils;
import pw.phylame.jem.formats.util.ByteUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.text.TextUtils;

public class Ebk2Parser extends BinaryParser<NonConfig> {
    public Ebk2Parser() {
        super("ebk", null, null);
    }

    @Override
    public Book parse(RandomAccessFile input, NonConfig config) throws IOException, ParserException {
        InternalData data = new InternalData(input);
        readHeader(data);
        readIndexes(data);
        data.book.setExtension(EbkInfo.FILE_INFO, new EbkInfo(2));
        return data.book;
    }

    private void readHeader(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;
        Book book = data.book;

        book.setAttribute("book_id", readUInt32(file));
        data.headerSize = readUInt16(file);
        int version = readUInt16(file);
        if (version != 2) {
            throw ExceptionFactory.parserException("ebk.parse.unsupportedVersion", version);
        }

        file.skipBytes(4);     // ebk2 size
        book.setTitle(readString(file, 64));
        file.skipBytes(4);     // file size
        data.indexesSize = readUInt32(file);
        file.skipBytes(4);     // first block
        data.chapterCount = readUInt16(file);
        data.blockCount = readUInt16(file);
        data.mediaCount = (int) readUInt32(file);
        file.skipBytes(8);     // media size and txt size
    }

    private void readIndexes(InternalData data) throws IOException, ParserException {
        byte[] bytes = readBytes(data.file, (int) data.indexesSize);
        ByteArrayInputStream stream = new ByteArrayInputStream(ZLibUtils.decompress(bytes));
        readChapters(data, stream);
        readBlocks(data, stream);
    }

    private void readChapters(InternalData data, InputStream stream) throws IOException, ParserException {
        String title;
        long offset, length;

        for (int i = 0; i < data.chapterCount; ++i) {
            title = readString(stream, 64);
            offset = readUInt32(stream);
            length = readUInt32(stream);
            EbkText content = new EbkText(data.file, data.blocks, offset, length);
            content.headSize = data.headerSize;
            content.indexSize = data.indexesSize;
            data.book.append(new Chapter(title, content));
        }
    }

    private void readBlocks(InternalData data, InputStream stream) throws IOException, ParserException {
        long offset, length;
        for (int i = 0; i < data.blockCount; ++i) {
            offset = readUInt32(stream);
            length = readUInt32(stream);
            data.blocks.add(new TextBlock(offset, length));
        }
    }

    @Override
    protected void onReadingError() throws ParserException {
        throw ExceptionFactory.parserException("ebk.parse.invalidFile", source);
    }

    private byte[] readBytes(InputStream in, long size) throws IOException, ParserException {
        byte[] b = new byte[(int) size];
        if (in.read(b) != size) {
            throw ExceptionFactory.parserException("ebk.parse.invalidFile");
        }
        return b;
    }

    private long readUInt32(InputStream in) throws IOException, ParserException {
        return ByteUtils.littleParser.getInt32(readBytes(in, 4), 0);
    }

    private String readString(RandomAccessFile file, int length) throws IOException, ParserException {
        return TextUtils.trimmed(new String(readBytes(file, length), EBK.TEXT_ENCODING));
    }

    private String readString(InputStream in, int length) throws IOException, ParserException {
        return TextUtils.trimmed(new String(readBytes(in, length), EBK.TEXT_ENCODING));
    }

    private class TextBlock {
        private final long offset, size;

        private TextBlock(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }
    }

    private class InternalData {
        private final RandomAccessFile file;
        private final Book book;

        private int headerSize;
        private long indexesSize;

        private int chapterCount;
        private int blockCount;
        private int mediaCount;

        private final ArrayList<TextBlock> blocks = new ArrayList<>();

        private InternalData(RandomAccessFile file) {
            this.file = file;
            book = new Book();
        }
    }

    private class EbkText extends AbstractText {
        private final RandomAccessFile file;
        private final ArrayList<TextBlock> blocks;
        private final long offset;
        private final long size;

        private int headSize;
        private long indexSize;

        private EbkText(RandomAccessFile file, ArrayList<TextBlock> blocks, long offset, long size) {
            super(PLAIN);
            this.file = file;
            this.blocks = blocks;
            this.offset = offset;
            this.size = size;
        }

        private String rawText() throws IOException {
            int index = (int) (offset >> 16);   // div 0x10000
            final int start = (int) (offset & 0x9999);  // mod 0x10000
            int length = -start;
            StringBuilder sb = new StringBuilder();
            do {
                TextBlock block = blocks.get(index++);
                file.seek(headSize + indexSize + block.offset);
                byte[] bytes = new byte[(int) block.size];
                int n = file.read(bytes);
                bytes = ZLibUtils.decompress(bytes, 0, n);
                length += bytes.length;
                sb.append(new String(bytes, EBK.TEXT_ENCODING));
                if (size <= length) {
                    return sb.substring(start >> 1, (int) (start + size) >> 1);
                }
            } while (true);
        }

        @Override
        public String getText() throws IOException {
            return rawText();
        }
    }
}
