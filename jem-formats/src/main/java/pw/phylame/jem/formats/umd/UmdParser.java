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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Parser;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.ZLibUtils;
import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileObject;

import static pw.phylame.tools.ByteUtils.littleParser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.*;

/**
 * <tt>Parser</tt> implement for UMD book.
 */
public class UmdParser implements Parser {
    private static Log LOG = LogFactory.getLog(UmdParser.class);

    private RandomAccessFile source;
    private Book book = null;
    private int umdType;
    private long contentLength;
    private int coverFormat, imageFormat;
    private Map<Long, Integer> blockOwners = new HashMap<Long, Integer>();
    private List<DataBlock> blocks = new ArrayList<DataBlock>();

    @Override
    public String getName() {
        return "umd";
    }

    @Override
    public Book parse(final File file, Map<String, Object> kw) throws IOException, JemException {
        final RandomAccessFile in = new RandomAccessFile(file, "r");
        book = parse(in);
        book.registerCleanup(new Part.Cleanable() {
            @Override
            public void clean(Part part) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.debug("cannot close UMD source: "+file.getPath(), e);
                }
            }
        });
        return book;
    }

    public Book parse(RandomAccessFile in) throws IOException, JemException {
        source = in;
        if (! isUmd()) {
            throw new JemException("Invalid UMD file: magic number");
        }
        book = new Book();
        int sep;
        while ((sep=source.read()) != -1) {
            if (sep == UMD.CHUNK_SEPARATOR) {
                readChunk();
            } else if (sep == UMD.ADDITION_SEPARATOR) {
                readAddition();
            } else {
                throw new JemException("Bad UMD file: unexpected separator: "+sep);
            }
        }
        return book;
    }

    private boolean isUmd() throws IOException {
        return readInt(source) == UMD.FILE_MAGIC;
    }

    private void readChunk() throws IOException, JemException {
        int chunkId = readShort(source), type = source.read(), length = source.read() - 5;

        switch (chunkId) {
            case UMD.CDT_UMD_HEAD:
                umdType = source.read();
                --length;
                break;
            case UMD.CDT_TITLE:
                book.setTitle(readString(length));
                length = 0;
                break;
            case UMD.CDT_AUTHOR:
                book.setAuthor(readString(length));
                length = 0;
                break;
            case UMD.CDT_YEAR:
                String s = readString(length);
                try {
                    int year = Integer.parseInt(s);
                    book.setDate(DateUtils.modifyDate(book.getDate(), Calendar.YEAR, year));
                } catch (Exception e) {
                    LOG.debug("invalid UMD year: "+s, e);
                }
                length = 0;
                break;
            case UMD.CDT_MONTH:
                s = readString(length);
                try {
                    int month = Integer.parseInt(s);
                    book.setDate(DateUtils.modifyDate(book.getDate(), Calendar.MONTH, month));
                } catch (Exception e) {
                    LOG.debug("invalid UMD month: "+s, e);
                }
                length = 0;
                break;
            case UMD.CDT_DAY:
                s = readString(length);
                try {
                    int day = Integer.parseInt(s);
                    book.setDate(DateUtils.modifyDate(book.getDate(), Calendar.DAY_OF_MONTH, day));
                } catch (Exception e) {
                    LOG.debug("invalid UMD day: "+s, e);
                }
                length = 0;
                break;
            case UMD.CDT_GENRE:
                book.setGenre(readString(length));
                length = 0;
                break;
            case UMD.CDT_PUBLISHER:
                book.setPublisher(readString(length));
                length = 0;
                break;
            case UMD.CDT_VENDOR:
                book.setAttribute("vendor", readString(length));
                length = 0;
                break;
            case UMD.CDT_CONTENT_LENGTH:
                contentLength = readInt(source);
                length -= 4;
                break;
            case UMD.CDT_CHAPTER_OFFSET:
            case UMD.CDT_CHAPTER_TITLE:
            case UMD.CDT_CONTENT_END:
            case 0x85:                  // unknown
            case 0x86:                  // unknown
                long check = readInt(source);
                blockOwners.put(check, chunkId);
                length -= 4;
                break;
            case UMD.CDT_IMAGE_FORMAT:
                imageFormat = source.read();
                --length;
                break;
            case UMD.CDT_CONTENT_ID:
                book.setAttribute("book_id", readInt(source));
                length -= 4;
                break;
            case UMD.CDT_CDS_KEY:
                byte[] bytes = new byte[length];
                if (source.read(bytes) != length) {
                    throw new JemException("Bad UMD file: CDS key");
                }
                book.setAttribute("cds_key", bytes);
                length = 0;
                break;
            case UMD.CDT_LICENSE_KEY:
                bytes = new byte[length];
                if (source.read(bytes) != length) {
                    throw new JemException("Bad UMD file: license key");
                }
                book.setAttribute("license_key", bytes);
                length = 0;
                break;
            case UMD.CDT_COVER_IMAGE:
                coverFormat = source.read();
                check = readInt(source);
                blockOwners.put(check, chunkId);
                length -= 1 + 4;
                break;
            case UMD.CDT_PAGE_OFFSET:
                // ignore page information
                source.skipBytes(2);
                check = readInt(source);
                blockOwners.put(check, chunkId);
                length -= 2 + 4;
                break;
            case UMD.CDT_UMD_END:
                if (readInt(source) != source.getFilePointer()) {
                    throw new JemException("Bad UMD file: end position");
                }
                length -= 4;
                break;
            case 0xD:
                // ignored
                break;
        }
        source.skipBytes(length);
    }

    private String readString(int size) throws IOException, JemException {
        byte[] bytes = new byte[size];
        if (source.read(bytes) != size) {
            throw new JemException("Bad UMD file: attributes");
        }
        return new String(bytes, UMD.TEXT_ENCODING);
    }

    private void readAddition() throws IOException, JemException {
        long check = readInt(source);
        Integer chunkId = blockOwners.get(check);

        if (chunkId == null) {
            readContent();
            return;
        }
        blockOwners.remove(check);
        switch (chunkId) {
            case UMD.CDT_CHAPTER_OFFSET:
                readChapterOffsets();
                break;
            case UMD.CDT_CHAPTER_TITLE:
                readChapterTitles();
                break;
            case UMD.CDT_CONTENT_END:
                readContentEnd();
                break;
            case UMD.CDT_COVER_IMAGE:
                readCoverImage();
                break;
            case UMD.CDT_PAGE_OFFSET:
                readPageOffsets();
                break;
            default:
                skipBlock();
                break;
        }
    }

    private void skipBlock() throws IOException {
        long length = readInt(source) - 9;
        source.skipBytes((int) length);
    }

    private void readChapterTitles() throws IOException, JemException {
        if (umdType != UMD.TEXT) {
            skipBlock();
            return;
        }

        long length = readInt(source) - 9;
        long end = source.getFilePointer() + length;
        int ix = 0;
        while (source.getFilePointer() < end) {
            int size = source.read();
            byte[] bytes = new byte[size];
            if (source.read(bytes) != size) {
                throw new JemException("Bad UMD file: chapter titles");
            }
            String title = new String(bytes, UMD.TEXT_ENCODING);
            try {
                book.get(ix++).setTitle(title);
            } catch (IndexOutOfBoundsException e) {
                book.newChapter(title);
            }
        }
    }

    private void readChapterOffsets() throws IOException {
        if (umdType != UMD.TEXT) {
            skipBlock();
            return;
        }

        long length = readInt(source) - 9, last = 0;
        length /= 4;

        Chapter chapter = null;
        for (int ix = 0; ix < length; ++ix) {
            long offset = readInt(source);
            try {
                chapter = (Chapter) book.get(ix);
            } catch (IndexOutOfBoundsException e) {
                chapter = book.newChapter("");
            }
            chapter.setSource(new UmdSource(source, offset, 0, blocks));
            if (ix > 0) {
                long size = offset - last;
                Part prev = book.get(ix-1);
                ((UmdSource)prev.getSource()).size = size;
            }
            last = offset;
        }
        if (contentLength > 0 && chapter != null) {
            ((UmdSource)chapter.getSource()).size = contentLength - last;
        }
    }

    private void readContentEnd() throws IOException {
        // ignored
        skipBlock();
    }

    private void readCoverImage() throws IOException {
        long length = readInt(source) - 9;
        FileObject cover = FileFactory.getFile("cover."+UMD.getNameOfFormat(coverFormat), source,
                source.getFilePointer(), length, null);
        book.setCover(cover);
        source.skipBytes((int) length);
    }

    private void readPageOffsets() throws IOException {
        // ignored
        skipBlock();
    }

    private void readContent() throws IOException {
        long offset, length = readInt(source) - 9;
        offset = source.getFilePointer();

        switch (umdType) {
            case UMD.TEXT:
                blocks.add(new DataBlock((int)source.getFilePointer(), (int)length));
                break;
            case UMD.CARTOON:
                String name = String.format("cartoon_%d.%s", book.size()+1, UMD.getNameOfFormat(imageFormat));
                FileObject image = FileFactory.getFile(name, source, offset, length, null);
                book.newChapter(String.valueOf(book.size() + 1), "", image, null);
                break;
            case UMD.COMIC:
                break;
        }
        source.skipBytes((int) length);
    }

    private static long readInt(RandomAccessFile in) throws IOException {
        byte[] b = new byte[4];
        if (in.read(b) != 4) {
            return -1;
        }
        return littleParser.getUint32(b, 0);
    }

    private static int readShort(RandomAccessFile in) throws IOException {
        byte[] b = new byte[2];
        if (in.read(b) != 2) {
            return -1;
        }
        return littleParser.getUint16(b, 0);
    }
}

class DataBlock {
    public int offset, length;

    public DataBlock(int offset, int length) {
        this.offset = offset;
        this.length = length;
    }
}

class UmdSource extends TextObject {
    private RandomAccessFile in;
    public long offset, size;
    private boolean fromUmd = false;
    private List<DataBlock> blocks;

    public UmdSource(RandomAccessFile in, long offset, long size, List<DataBlock> blocks) {
        super();
        this.in = in;
        this.offset = offset;
        this.size = size;
        this.blocks = blocks;
        fromUmd = true;
    }

    @Override
    public void setRaw(String raw) {
        super.setRaw(raw);
        fromUmd = false;
    }

    @Override
    public void setFile(FileObject file, String encoding) {
        super.setFile(file, encoding);
        fromUmd = false;
    }

    @Override
    protected long aboutSize() {
        if (fromUmd) {
            return size;
        } else {
            return super.aboutSize();
        }
    }

    private String rawText() throws IOException {
        int index = (int)(offset / UMD.BLOCK_SIZE);
        int start = (int)(offset % UMD.BLOCK_SIZE);
        int length = -start;
        StringBuilder sb = new StringBuilder();
        do {
            DataBlock block = blocks.get(index++);
            in.seek(block.offset);
            byte[] bytes = new byte[block.length];
            int n = in.read(bytes);
            bytes = ZLibUtils.decompress(bytes, 0, n);
            length += bytes.length;
            sb.append(new String(bytes, UMD.TEXT_ENCODING));
            if (size < length) {
                return sb.substring(start / 2, start / 2 + (int) size / 2);
            }
        } while (true);

    }

    @Override
    public String getText() throws IOException {
        if (fromUmd) {
            return rawText().replaceAll(UMD.SYMBIAN_LINE_FEED, System.getProperty("line.separator"));
        } else {
            return super.getText();
        }
    }

    @Override
    public String[] getLines() throws IOException {
        if (fromUmd) {
            return rawText().split(UMD.SYMBIAN_LINE_FEED);
        } else {
            return super.getLines();
        }
    }

    @Override
    public long writeTo(Writer writer, long size) throws IOException {
        if (fromUmd) {
            String text = rawText();
            if (size < 0) {
                size = text.length();
            }
            writer.write(text, 0, (int) size);
            return size;
        } else {
            return super.writeTo(writer, size);
        }
    }
}