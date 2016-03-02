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

package pw.phylame.jem.formats.umd;

import java.util.*;
import java.io.IOException;
import java.io.RandomAccessFile;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.AbstractText;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.common.BinaryParser;
import pw.phylame.jem.formats.util.ZLibUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;

/**
 * <tt>Parser</tt> implement for UMD book.
 */
public class UmdParser extends BinaryParser<NonConfig> {
    public UmdParser() {
        super("umd", null, null);
    }

    @Override
    protected void validateFile(RandomAccessFile input, NonConfig config) throws IOException,
            ParserException {
        if (readUInt32(input) != UMD.MAGIC_NUMBER) {
            throw ExceptionFactory.parserException("umd.parse.invalidMagic");
        }
    }

    @Override
    public Book parse(RandomAccessFile input, NonConfig config) throws IOException, ParserException {
        return parse(input);
    }

    public Book parse(RandomAccessFile file) throws IOException, ParserException {
        InternalData data = new InternalData(file);
        int sep;
        while ((sep = file.read()) != -1) {
            switch (sep) {
                case UMD.CHUNK_SEPARATOR:
                    readChunk(data);
                    break;
                case UMD.ADDITION_SEPARATOR:
                    readContent(data);
                    break;
                default:
                    throw ExceptionFactory.parserException("umd.parse.badSeparator", sep);
            }
        }
        data.book.setExtension(UmdInfo.FILE_INFO, new UmdInfo(data.umdType));
        return data.book;
    }

    private void readChunk(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;
        Book book = data.book;

        int chunkId = readUInt16(file);
        file.skipBytes(1);
        int length = file.read() - 5;

        switch (chunkId) {
            case UMD.CDT_UMD_HEAD: {
                int umdType = file.read();
                if (umdType != UMD.TEXT && umdType != UMD.CARTOON) {
                    throw ExceptionFactory.parserException("umd.parse.invalidType", umdType);
                }
                data.umdType = umdType;
                file.skipBytes(2);
            }
            break;
            case UMD.CDT_TITLE: {
                book.setTitle(readString(file, length));
            }
            break;
            case UMD.CDT_AUTHOR: {
                book.setAuthor(readString(file, length));
            }
            break;
            case UMD.CDT_YEAR: {
                data.year = Integer.parseInt(readString(file, length));
            }
            break;
            case UMD.CDT_MONTH: {
                data.month = Integer.parseInt(readString(file, length)) - 1;
            }
            break;
            case UMD.CDT_DAY: {
                data.day = Integer.parseInt(readString(file, length));
            }
            break;
            case UMD.CDT_GENRE: {
                book.setGenre(readString(file, length));
            }
            break;
            case UMD.CDT_PUBLISHER: {
                book.setPublisher(readString(file, length));
            }
            break;
            case UMD.CDT_VENDOR: {
                book.setVendor(readString(file, length));
            }
            break;
            case UMD.CDT_CONTENT_LENGTH: {
                data.contentLength = readUInt32(file);
            }
            break;
            case UMD.CDT_CHAPTER_OFFSET: {
                file.skipBytes(9);
                readChapterOffsets(data);
            }
            break;
            case UMD.CDT_CHAPTER_TITLE: {
                file.skipBytes(9);
                readChapterTitles(data);
            }
            break;
            case UMD.CDT_CONTENT_END: {
                file.skipBytes(9);
                readContentEnd(data.file);
            }
            break;
            case 0x85:
            case 0x86: {
                file.skipBytes(9);
                skipBlock(data.file);
            }
            break;
            case UMD.CDT_IMAGE_FORMAT: {
                data.imageFormat = file.read();
            }
            break;
            case UMD.CDT_CONTENT_ID: {
                book.setAttribute("book_id", readUInt32(file));
            }
            break;
            case UMD.CDT_CDS_KEY: {
                byte[] bytes = readBytes(file, length, "umd.parse.badCDSKey");
                book.setAttribute("cds_key", FileFactory.forBytes("cds_key", bytes,
                        FileObject.UNKNOWN_MIME));
            }
            break;
            case UMD.CDT_LICENSE_KEY: {
                byte[] bytes = readBytes(file, length, "umd.parse.badLicenseKey");
                book.setAttribute("license_key", FileFactory.forBytes("license_key", bytes,
                        FileObject.UNKNOWN_MIME));
            }
            break;
            case UMD.CDT_COVER_IMAGE: {
                data.coverFormat = file.read();
                file.skipBytes(9);
                readCoverImage(data);
            }
            break;
            case UMD.CDT_PAGE_OFFSET: {
                // ignore page information
                file.skipBytes(11);
                readPageOffsets(data);
            }
            break;
            case UMD.CDT_UMD_END: {
                if (readUInt32(file) != file.getFilePointer()) {
                    throw ExceptionFactory.parserException("umd.parse.badEnd");
                }
                if (data.year > 0 && data.day > 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, data.year);
                    calendar.set(Calendar.MONTH, data.month);
                    calendar.set(Calendar.DAY_OF_MONTH, data.day);
                    book.setDate(calendar.getTime());
                }
            }
            break;
            case 0xD:
                // ignored
                break;
        }
    }

    private String readString(RandomAccessFile file, int length) throws IOException, ParserException {
        return new String(readBytes(file, length), UMD.TEXT_ENCODING);
    }

    private void skipBlock(RandomAccessFile file) throws IOException, ParserException {
        long length = readUInt32(file) - 9;
        file.skipBytes((int) length);
    }

    private void readChapterOffsets(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;
        Book book = data.book;
        long count = (readUInt32(file) - 9) >> 2; // div 4
        data.chapterCount = (int) count;

        if (data.chapterCount == 0) {     // no chapter
            return;
        }

        long prevOffset = readUInt32(file);
        UmdText umdText = new UmdText(file, prevOffset, 0, data.blocks);
        book.append(new Chapter("", umdText));
        for (int ix = 1; ix < count; ++ix) {
            long offset = readUInt32(file);
            umdText.size = offset - prevOffset;
            umdText = new UmdText(file, offset, 0, data.blocks);
            prevOffset = offset;
            book.append(new Chapter("", umdText));
        }
        umdText.size = data.contentLength - prevOffset;
    }

    private void readChapterTitles(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;
        file.skipBytes(4);
        for (Chapter ch : data.book) {
            String title = readString(file, file.read());
            ch.setTitle(title);
        }
    }

    private void readContentEnd(RandomAccessFile file) throws IOException, ParserException {
        // ignored
        skipBlock(file);
    }

    private void readCoverImage(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;

        long length = readUInt32(file) - 9;
        String format = UMD.nameOfFormat(data.coverFormat);
        FileObject cover = FileFactory.forBlock("cover." + format, file,
                file.getFilePointer(), length, "image/" + format);
        data.book.setCover(cover);
        file.skipBytes((int) length);
    }

    private void readPageOffsets(InternalData data) throws IOException, ParserException {
        // ignored
        skipBlock(data.file);
    }

    private void readContent(InternalData data) throws IOException, ParserException {
        RandomAccessFile file = data.file;
        Book book = data.book;

        file.skipBytes(4);
        long offset, length = readUInt32(file) - 9;
        offset = file.getFilePointer();

        switch (data.umdType) {
            case UMD.TEXT: {
                data.blocks.add(new TextBlock((int) file.getFilePointer(), (int) length));
            }
            break;
            case UMD.CARTOON: {
                String format = UMD.nameOfFormat(data.imageFormat);
                String name = String.format("img_%d.%s", book.size() + 1, format);
                FileObject image = FileFactory.forBlock(name, file, offset, length, "image/" + format);
                Chapter chapter = new Chapter(String.valueOf(book.size() + 1));
                chapter.setCover(image);
                book.append(chapter);
            }
            break;
            case UMD.COMIC:
                break;
        }
        file.skipBytes((int) length);
    }

    @Override
    protected void onReadingError() throws ParserException {
        throw ExceptionFactory.parserException("umd.parse.invalidFile", source);
    }

    private class TextBlock {
        private final int offset, length;

        private TextBlock(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }

    private class InternalData {
        private final RandomAccessFile file;
        private final Book book;
        private int umdType;

        private int year = 0, month = 0, day = 0;

        private int chapterCount;

        private long contentLength;
        private int coverFormat, imageFormat;

        private final ArrayList<TextBlock> blocks = new ArrayList<>();

        private InternalData(RandomAccessFile file) {
            this.file = file;
            book = new Book();
        }
    }

    private class UmdText extends AbstractText {
        private final RandomAccessFile file;
        private final long offset;
        private long size;
        private final List<TextBlock> blocks;

        private UmdText(RandomAccessFile file, long offset, long size, List<TextBlock> blocks) {
            super(PLAIN);
            this.file = file;
            this.offset = offset;
            this.size = size;
            this.blocks = blocks;
        }

        private String rawText() throws IOException {
            int index = (int) (offset >> 15);   // div 0x8000
            int start = (int) (offset & 0x7FFF);    // mod 0x8000
            int length = -start;
            StringBuilder sb = new StringBuilder();
            do {
                TextBlock block = blocks.get(index++);
                file.seek(block.offset);
                byte[] bytes = new byte[block.length];
                int n = file.read(bytes);
                bytes = ZLibUtils.decompress(bytes, 0, n);
                length += bytes.length;
                sb.append(new String(bytes, UMD.TEXT_ENCODING));
                if (size <= length) {
                    return sb.substring(start >> 1, (int) (start + size) >> 1); // div 2
                }
            } while (true);
        }

        @Override
        public String getText() throws IOException {
            return rawText().replaceAll(UMD.UMD_LINE_FEED, System.lineSeparator());
        }

        @Override
        public List<String> getLines(boolean skipEmpty) throws IOException {
            return Arrays.asList(rawText().split(UMD.UMD_LINE_FEED));
        }
    }
}

