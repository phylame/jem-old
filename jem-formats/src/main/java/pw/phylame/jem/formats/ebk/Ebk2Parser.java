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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.AbstractText;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.common.BinaryBookParser;
import pw.phylame.jem.formats.util.ZLibUtils;
import pw.phylame.jem.formats.util.ByteUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.text.TextUtils;

import java.io.*;
import java.util.ArrayList;

public class Ebk2Parser extends BinaryBookParser<NonConfig> {

    private class ParserData {
        final RandomAccessFile file;
        final Book book;

        int headSize;
        long indexSize;

        int chapterCount;
        int blockCount;
        int mediaCount;

        final ArrayList<TextBlock> blocks = new ArrayList<TextBlock>();

        ParserData(RandomAccessFile file) {
            this.file = file;
            book = new Book();
        }
    }

    public Ebk2Parser() {
        super("ebk");
    }

    @Override
    public Book parse(RandomAccessFile input, NonConfig config)
            throws IOException, ParserException {
        ParserData pd = new ParserData(input);
        readHeader(pd);
        readChapterIndexes(pd);
        pd.book.setExtension(EbkInfo.FILE_INFO, new EbkInfo(2));
        return pd.book;
    }

    private void readHeader(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;
        Book book = pd.book;

        book.setAttribute("book_id", readUInt32(file));
        pd.headSize = readUInt16(file);
        int version = readUInt16(file);
        if (version != 2) {
            throw parserException("ebk.parse.unsupportedVersion", version);
        }
        book.setExtension("ebk_version", 2);

        file.skipBytes(4);     // ebk2 size
        book.setTitle(readString(file, 64));
        file.skipBytes(4);     // file size
        pd.indexSize = readUInt32(file);
        file.skipBytes(4);     // first block
        pd.chapterCount = readUInt16(file);
        pd.blockCount = readUInt16(file);
        pd.mediaCount = (int) readUInt32(file);
        file.skipBytes(8);     // media size and txt size
    }

    private void readChapterIndexes(ParserData pd) throws IOException,
            ParserException {
        byte[] b = readBytes(pd.file, (int) pd.indexSize);
        ByteArrayInputStream stream = new ByteArrayInputStream(ZLibUtils.decompress(b));
        readChapters(pd, stream);
        readBlocks(pd, stream);
    }

    private void readChapters(ParserData pd, InputStream stream)
            throws IOException, ParserException {
        String title;
        long offset, length;

        for (int i = 0; i < pd.chapterCount; ++i) {
            title = readString(stream, 64);
            offset = readUInt32(stream);
            length = readUInt32(stream);
            EbkText content = new EbkText(pd.file, pd.blocks, offset, length);
            content.headSize = pd.headSize;
            content.indexSize = pd.indexSize;
            pd.book.append(new Chapter(title, content));
        }
    }

    private void readBlocks(ParserData pd, InputStream stream)
            throws IOException, ParserException {
        long offset, length;
        for (int i = 0; i < pd.blockCount; ++i) {
            offset = readUInt32(stream);
            length = readUInt32(stream);
            pd.blocks.add(new TextBlock(offset, length));
        }
    }

    @Override
    protected byte[] readBytes(RandomAccessFile input, int size)
            throws IOException, ParserException {
        byte[] b = super.readBytes(input, size);
        if (b == null) {
            throw parserException("ebk.parse.invalidFile");
        }
        return b;
    }

    private byte[] readBytes(InputStream in, long size) throws IOException,
            ParserException {
        byte[] b = new byte[(int) size];
        if (in.read(b) != size) {
            throw parserException("ebk.parse.invalidFile");
        }
        return b;
    }

    private long readUInt32(InputStream in) throws IOException, ParserException {
        return ByteUtils.littleParser.getInt32(readBytes(in, 4), 0);
    }

    private String readString(RandomAccessFile file, int length) throws IOException,
            ParserException {
        String str = new String(readBytes(file, length), EBK.TEXT_ENCODING);
        return TextUtils.trim(str);
    }

    private String readString(InputStream in, int length) throws IOException,
            ParserException {
        String str = new String(readBytes(in, length), EBK.TEXT_ENCODING);
        return TextUtils.trim(str);
    }

    private class TextBlock {
        final long offset, size;

        TextBlock(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }
    }

    private class EbkText extends AbstractText {
        final private RandomAccessFile file;
        private final ArrayList<TextBlock> blocks;
        private final long offset;
        private final long size;

        int headSize;
        long indexSize;

        EbkText(RandomAccessFile file, ArrayList<TextBlock> blocks,
                long offset, long size) {
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

        @Override
        public void writeTo(Writer writer) throws IOException {
            writer.write(rawText());
        }
    }
}
