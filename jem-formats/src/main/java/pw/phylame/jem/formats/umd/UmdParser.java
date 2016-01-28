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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.AbstractText;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.common.BinaryParser;
import pw.phylame.jem.formats.util.ZLibUtils;
import pw.phylame.jem.formats.util.ParserException;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.*;

/**
 * <tt>Parser</tt> implement for UMD book.
 */
public class UmdParser extends BinaryParser<NonConfig> {
    public UmdParser() {
        super("umd", null, null);
    }

    private class ParserData {
        final RandomAccessFile file;
        final Book book;
        int umdType;

        int year = 0, month = 0, day = 0;

        int chapterCount;

        long contentLength;
        int coverFormat, imageFormat;

        final ArrayList<TextBlock> blocks = new ArrayList<>();

        ParserData(RandomAccessFile file) {
            this.file = file;
            book = new Book();
        }
    }

    @Override
    protected void validateInput(RandomAccessFile input, NonConfig config) throws IOException, ParserException {
        if (readUInt32(input) != UMD.FILE_MAGIC_NUMBER) {
            throw parserException("umd.parse.invalidMagic");
        }
    }

    @Override
    protected Book parse(RandomAccessFile input, NonConfig config) throws IOException, ParserException {
        return parse0(input);
    }

    private Book parse0(RandomAccessFile file) throws IOException, ParserException {
        ParserData pd = new ParserData(file);
        int sep;
        while ((sep = file.read()) != -1) {
            switch (sep) {
                case UMD.CHUNK_SEPARATOR:
                    readChunk(pd);
                    break;
                case UMD.ADDITION_SEPARATOR:
                    readContent(pd);
                    break;
                default:
                    throw parserException("umd.parse.badSeparator", sep);
            }
        }
        pd.book.setExtension(UmdInfo.FILE_INFO, new UmdInfo(pd.umdType));
        return pd.book;
    }

    private void readChunk(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;
        Book book = pd.book;

        int chunkId = readUInt16(file);
        file.skipBytes(1);
        int length = file.read() - 5;

        switch (chunkId) {
            case UMD.CDT_UMD_HEAD: {
                int umdType = file.read();
                if (umdType != UMD.TEXT && umdType != UMD.CARTOON) {
                    throw parserException("umd.parse.invalidType", umdType);
                }
                pd.umdType = umdType;
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
                pd.year = Integer.parseInt(readString(file, length));
            }
            break;
            case UMD.CDT_MONTH: {
                pd.month = Integer.parseInt(readString(file, length)) - 1;
            }
            break;
            case UMD.CDT_DAY: {
                pd.day = Integer.parseInt(readString(file, length));
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
                pd.contentLength = readUInt32(file);
            }
            break;
            case UMD.CDT_CHAPTER_OFFSET: {
                file.skipBytes(9);
                readChapterOffsets(pd);
            }
            break;
            case UMD.CDT_CHAPTER_TITLE: {
                file.skipBytes(9);
                readChapterTitles(pd);
            }
            break;
            case UMD.CDT_CONTENT_END: {
                file.skipBytes(9);
                readContentEnd(pd.file);
            }
            break;
            case 0x85:
            case 0x86: {
                file.skipBytes(9);
                skipBlock(pd.file);
            }
            break;
            case UMD.CDT_IMAGE_FORMAT: {
                pd.imageFormat = file.read();
            }
            break;
            case UMD.CDT_CONTENT_ID: {
                book.setAttribute("book_id", readUInt32(file));
            }
            break;
            case UMD.CDT_CDS_KEY: {
                byte[] bytes = readBytes(file, length, "umd.parse.badCDSKey");
                book.setAttribute("cds_key", FileFactory.fromBytes("cds_key", bytes,
                        FileObject.UNKNOWN_MIME));
            }
            break;
            case UMD.CDT_LICENSE_KEY: {
                byte[] bytes = readBytes(file, length, "umd.parse.badLicenseKey");
                book.setAttribute("license_key", FileFactory.fromBytes("license_key", bytes,
                        FileObject.UNKNOWN_MIME));
            }
            break;
            case UMD.CDT_COVER_IMAGE: {
                pd.coverFormat = file.read();
                file.skipBytes(9);
                readCoverImage(pd);
            }
            break;
            case UMD.CDT_PAGE_OFFSET: {
                // ignore page information
                file.skipBytes(11);
                readPageOffsets(pd);
            }
            break;
            case UMD.CDT_UMD_END: {
                if (readUInt32(file) != file.getFilePointer()) {
                    throw parserException("umd.parse.badEnd");
                }
                if (pd.year > 0 && pd.day > 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, pd.year);
                    calendar.set(Calendar.MONTH, pd.month);
                    calendar.set(Calendar.DAY_OF_MONTH, pd.day);
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

    private void readChapterOffsets(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;
        Book book = pd.book;
        long count = (readUInt32(file) - 9) >> 2; // div 4
        pd.chapterCount = (int) count;

        if (pd.chapterCount == 0) {     // no chapter
            return;
        }

        long prevOffset = readUInt32(file);
        UmdText umdText = new UmdText(file, prevOffset, 0, pd.blocks);
        book.append(new Chapter("", umdText));
        for (int ix = 1; ix < count; ++ix) {
            long offset = readUInt32(file);
            umdText.size = offset - prevOffset;
            umdText = new UmdText(file, offset, 0, pd.blocks);
            prevOffset = offset;
            book.append(new Chapter("", umdText));
        }
        umdText.size = pd.contentLength - prevOffset;
    }

    private void readChapterTitles(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;
        file.skipBytes(4);
        for (Chapter chapter : pd.book) {
            String title = readString(file, file.read());
            chapter.setTitle(title);
        }
    }

    private void readContentEnd(RandomAccessFile file) throws IOException, ParserException {
        // ignored
        skipBlock(file);
    }

    private void readCoverImage(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;

        long length = readUInt32(file) - 9;
        String format = UMD.nameOfFormat(pd.coverFormat);
        FileObject cover = FileFactory.fromBlock("cover." + format, file,
                file.getFilePointer(), length, "image/" + format);
        pd.book.setCover(cover);
        file.skipBytes((int) length);
    }

    private void readPageOffsets(ParserData pd) throws IOException, ParserException {
        // ignored
        skipBlock(pd.file);
    }

    private void readContent(ParserData pd) throws IOException, ParserException {
        RandomAccessFile file = pd.file;
        Book book = pd.book;

        file.skipBytes(4);
        long offset, length = readUInt32(file) - 9;
        offset = file.getFilePointer();

        switch (pd.umdType) {
            case UMD.TEXT: {
                pd.blocks.add(new TextBlock((int) file.getFilePointer(), (int) length));
            }
            break;
            case UMD.CARTOON: {
                String format = UMD.nameOfFormat(pd.imageFormat);
                String name = String.format("img_%d.%s", book.size() + 1, format);
                FileObject image = FileFactory.fromBlock(name, file, offset, length, "image/" + format);
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
        throw parserException("umd.parse.invalidFile", source);
    }

    class TextBlock {
        final int offset, length;

        TextBlock(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }

    private class UmdText extends AbstractText {
        private final RandomAccessFile file;
        private final long offset;
        long size;
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

