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

import kbps.io.BufferedRandomAccessFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.util.I18nMessage;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.AbstractParser;
import pw.phylame.jem.util.TextObject;
import pw.phylame.tools.ZLibUtils;

import static pw.phylame.tools.ByteUtils.littleParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class EbkParser extends AbstractParser {
    private static Log LOG = LogFactory.getLog(EbkParser.class);

    private BufferedRandomAccessFile input;
    private Book book;

    private int headSize;
    private long indexSize;

    private int chapterCount;
    private int blockCount;
    private int mediaCount;

    private ArrayList<TextBlock> blocks;

    @Override
    public String getName() {
        return "ebk2";
    }

    @Override
    public Book parse(final File file, Map<String, Object> kw) throws IOException, JemException {
        final BufferedRandomAccessFile in = new BufferedRandomAccessFile(file, "r");
        try {
            book = parse(in);
        } catch (IOException e) {
            closeFile(in, file.getAbsolutePath());
            throw e;
        } catch (JemException e) {
            closeFile(in, file.getAbsolutePath());
            throw e;
        }
        book.registerCleanup(new Chapter.Cleanable() {
            @Override
            public void clean(Chapter chapter) {
                closeFile(in, file.getAbsolutePath());
            }
        });
        return book;
    }

    public Book parse(BufferedRandomAccessFile in) throws IOException, JemException {
        input = in;
        book = new Book();
        readHead();
        byte[] b = readBytes(indexSize);
        ByteArrayInputStream bais = new ByteArrayInputStream(ZLibUtils.decompress(b));
        readChapters(bais);
        readBlocks(bais);
        bais.close();
        return book;
    }

    private void closeFile(BufferedRandomAccessFile in, String path) {
        try {
            in.close();
        } catch (IOException e) {
            if (path == null) {
                LOG.debug("cannot close EBK file", e);
            } else {
                LOG.debug("cannot close EBK file: " + path, e);
            }
        }
    }

    private void readHead() throws IOException, JemException {
        book.setAttribute("book_id", readInt());
        headSize = readShort();
        int version = readShort();
        if (version == 2) {
            readHead2();
        } else {
            throw makeParserException(I18nMessage.getText("Ebk.UnsupportedVersion", version));
        }
    }

    private void readHead2() throws IOException, JemException {
        book.setItem("ebk_version", 2);
        input.skipBytes(4);     // ebk size
        book.setAttribute(Book.TITLE, readString(64));
        input.skipBytes(4);     // file size
        indexSize = readInt();
        input.skipBytes(4);     // first block
        chapterCount = readShort();
        blockCount = readShort();
        mediaCount = (int)readInt();
        input.skipBytes(8);     // media size and txt size
    }

    private void readChapters(InputStream in) throws IOException, JemException {
        String title;
        long offset, length;

        for (int i = 0; i < chapterCount; i++) {
            title = readString(in, 64);
            offset = readInt(in);
            length = readInt(in);
            Chapter chapter = new Chapter(title, new EbkSource(offset, length));
            book.append(chapter);
        }
    }

    private void readBlocks(InputStream in) throws IOException, JemException {
        long offset, length;
        blocks = new ArrayList<TextBlock>();
        for (int i = 0; i < blockCount; i++) {
            offset = readInt(in);
            length = readInt(in);
            blocks.add(new TextBlock(offset, length));
        }
    }

    private byte[] readBytes(long size) throws IOException, JemException {
        byte[] b = new byte[(int)size];
        if (input.read(b) != size) {
            throw makeParserException(I18nMessage.getText("Ebk.InvalidFile"));
        }
        return b;
    }

    private byte[] readBytes(InputStream in, long size) throws IOException, JemException {
        byte[] b = new byte[(int)size];
        if (in.read(b) != size) {
            throw makeParserException(I18nMessage.getText("Ebk.InvalidFile"));
        }
        return b;
    }

    private String readString(int length) throws IOException, JemException {
        return new String(readBytes(length), EBK.TEXT_ENCODING).trim();
    }

    private String readString(InputStream in, int length) throws IOException, JemException {
        return new String(readBytes(in, length), EBK.TEXT_ENCODING).trim();
    }

    private long readInt() throws IOException, JemException {
        return littleParser.getUint32(readBytes(4), 0);
    }

    private long readInt(InputStream in) throws IOException, JemException {
        return littleParser.getUint32(readBytes(in, 4), 0);
    }

    private int readShort() throws IOException, JemException {
        return littleParser.getUint16(readBytes(2), 0);
    }

    private int readByte() throws IOException, JemException {
        return readBytes(1)[0] & 0xFF;
    }

    private class TextBlock {
        public long offset;
        public long size;

        public TextBlock(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }
    }

    private class EbkSource implements TextObject {
        private long offset;
        private long size;

        EbkSource(long offset, long size) {
            this.offset = offset;
            this.size = size;
        }

        @Override
        public String getType() {
            return PLAIN;
        }

        private String rawText() throws IOException {
            int           index  = (int) (offset / EBK.BLOCK_SIZE);
            final int           start  = (int) (offset % EBK.BLOCK_SIZE);
            int           length = -start;
            StringBuilder sb     = new StringBuilder();
            do {
                TextBlock block = blocks.get(index++);
                input.seek(headSize+indexSize+block.offset);
                byte[] bytes = new byte[(int)block.size];
                int n = input.read(bytes);
                bytes = ZLibUtils.decompress(bytes, 0, n);
                length += bytes.length;
                sb.append(new String(bytes, EBK.TEXT_ENCODING));
                if (size <= length) {
                    return sb.substring(start / 2, start / 2 + (int) size / 2);
                }
            } while (true);
        }

        @Override
        public String getText() {
            try {
                return rawText();
            } catch (IOException e) {
                LOG.debug("cannot load chapter text", e);
                return null;
            }
        }

        @Override
        public String[] getLines() {
            String text = getText();
            if (text == null) {
                return null;
            } else {
                return text.split("(\\r\\n)|(\\r)|(\\n)");
            }
        }

        @Override
        public void writeTo(Writer writer) throws IOException {
            writer.write(rawText());
        }
    }
}
