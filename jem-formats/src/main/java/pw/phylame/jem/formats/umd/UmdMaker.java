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

import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.core.Cleanable;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.util.JemException;

import pw.phylame.tools.ZLibUtils;
import pw.phylame.tools.NumberUtils;
import pw.phylame.tools.file.FileNameUtils;
import pw.phylame.tools.file.FileObject;

import static pw.phylame.tools.ByteUtils.littleRender;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * <tt>Maker</tt> implement for UMD book.
 */
public class UmdMaker implements Maker {
    private static Log LOG = LogFactory.getLog(UmdMaker.class);

    public static final String KEY_UMD_TYPE = "umd_type";

    private Book book;
    private int umdType;
    private OutputStream output;
    private long writtenBytes = 0;

    private RandomAccessFile source = null;

    @Override
    public String getName() {
        return "umd";
    }

    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        int umdType = UMD.TEXT;
        if (kw != null && kw.size() > 0) {
            Object o = kw.get(KEY_UMD_TYPE);
            if (o instanceof Integer) {
                umdType = (Integer)o;
            } else if (o instanceof String) {
                try {
                    umdType = Integer.parseInt((String)o);
                } catch (NumberFormatException e) {
                    LOG.debug("invalid UMD type: "+o, e);
                }
            } else {
                throw ExceptionFactory.forInvalidIntegerArgument(KEY_UMD_TYPE);
            }
        }
        make(book, output, umdType);
        output.close();
    }

    public void make(Book book, OutputStream output, int umdType) throws IOException, JemException {
        this.output = output;
        this.umdType = umdType;
        this.book = book;

        output.write(littleRender.putUint32(UMD.FILE_MAGIC));
        switch (umdType) {
            case UMD.TEXT:
                makeText();
                break;
            case UMD.CARTOON:
                makeCartoon();
                break;
            case UMD.COMIC:
                makeComic();
                break;
            default:
                throw new JemException("Invalid UMD type: "+umdType);
        }
    }

    private void makeText() throws IOException {
        writeUmdHead();
        writeAttributes();
        List<Long> offsets = new ArrayList<Long>();
        List<String> titles = new ArrayList<String>();

        // prepare text
        final File cache = File.createTempFile("umd_", ".stf");
        book.registerCleanup(new Cleanable() {
            @Override
            public void clean(Part part) {
                if (source != null) {
                    try {
                        source.close();
                        if (! cache.delete()) {
                            LOG.debug("cannot delete UMD cached file: "+cache.getPath());
                        }
                    } catch (IOException e) {
                        LOG.debug("cannot close UMD cached file: "+cache.getPath());
                    }
                }
            }
        });
        source = new RandomAccessFile(cache, "rw");
        for (Part sub: book) {
            cachePart(sub, source, titles, offsets);
        }
        long contentLength = source.getFilePointer();
        source.seek(0L);

        writeContentLength(contentLength);
        writeChapterOffsets(offsets);
        writeChapterTitles(titles);

        List<Long> blockChecks = new ArrayList<Long>();
        writeText(source, contentLength, blockChecks);
        writeContentEnd(blockChecks);

        writeCoverImage();
        writeSimplePageOffsets(contentLength);
        writeUmdEnd();

        source.close();
        source = null;
        if (! cache.delete()) {
            LOG.debug("cannot delete UMD cached file: "+cache.getPath());
        }
    }

    private void makeCartoon() throws IOException {
        writeUmdHead();
        writeAttributes();
        // ignored chapter offsets and titles
        List<FileObject> images = new ArrayList<FileObject>();
        String imageFormat = "jpg";

        // prepare images
        for (Part sub: book) {
            findImages(sub, images);
        }
        writeImageFormat(imageFormat);

        List<Long> blockChecks = new ArrayList<Long>();
        writeImages(images, blockChecks);
        writeContentEnd(blockChecks);

        writeCoverImage();
        writeLicenseKey();
        writeUmdEnd();
    }

    private void makeComic() {

    }

    private void writeChunk(int id, boolean hasAddition, byte[] data) throws IOException {
        writeChunk(id, hasAddition ? 1 : 0, data);
    }

    private void writeChunk(int id, int type, byte[] data) throws IOException {
        output.write(UMD.CHUNK_SEPARATOR);
        output.write(littleRender.putUint16(id));
        output.write(type);
        output.write(5+data.length);
        output.write(data);
        writtenBytes += 5+data.length;
    }

    private void writeAddition(long check, byte[] data) throws IOException {
        output.write(UMD.ADDITION_SEPARATOR);
        output.write(littleRender.putUint32(check));
        output.write(littleRender.putUint32(9+data.length));
        output.write(data);
        writtenBytes += 9+data.length;
    }

    // 1
    private void writeUmdHead() throws IOException {
        byte[] data = new byte[3];
        data[0] = (byte) umdType;
        int val = NumberUtils.randInteger(0x401, 0x7FFF);
        data[1] = (byte) ((val & 0xFF00) >> 8);
        data[2] = (byte) (val & 0xFF);
        writeChunk(UMD.CDT_UMD_HEAD, false, data);
    }

    // 2-9
    private void writeAttributes() throws IOException{
        writeChunk(UMD.CDT_TITLE, false, book.getTitle().getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_AUTHOR, false, book.getAuthor().getBytes(UMD.TEXT_ENCODING));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(book.getDate());
        writeChunk(UMD.CDT_YEAR, false,
                Integer.toString(calendar.get(Calendar.YEAR)).getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_MONTH, false,
                Integer.toString(calendar.get(Calendar.MONTH) + 1).getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_DAY, false,
                Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)).getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_GENRE, false, book.getGenre().getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_PUBLISHER, false, book.getPublisher().getBytes(UMD.TEXT_ENCODING));
        writeChunk(UMD.CDT_VENDOR, false, book.stringAttribute("vendor", "").getBytes(UMD.TEXT_ENCODING));
    }

    // B
    private void writeContentLength(long length) throws IOException {
        writeChunk(UMD.CDT_CONTENT_LENGTH, false, littleRender.putUint32(length));
    }

    // 83
    private void writeChapterOffsets(List<Long> chapterOffsets) throws IOException {
        int check = NumberUtils.randInteger(0x3000, 0x3FFF);
        writeChunk(UMD.CDT_CHAPTER_OFFSET, true, littleRender.putUint32(check));
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (long offset: chapterOffsets) {
            o.write(littleRender.putUint32(offset));
        }
        writeAddition(check, o.toByteArray());
        o.close();
    }

    // 84
    private void writeChapterTitles(List<String> titles) throws IOException {
        int check = NumberUtils.randInteger(0x4000, 0x4FFF);
        writeChunk(UMD.CDT_CHAPTER_TITLE, true, littleRender.putUint32(check));
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (String title: titles) {
            byte[] data =title.getBytes(UMD.TEXT_ENCODING);
            o.write(data.length);
            o.write(data);
        }
        writeAddition(check, o.toByteArray());
        o.close();
    }

    // E
    private void writeImageFormat(String format) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) UMD.getFormatOfName(format);
        writeChunk(UMD.CDT_IMAGE_FORMAT, false, bytes);
    }

    // F1
    private void writeLicenseKey() throws IOException {
        byte[] data;
        Object key = book.getAttribute("license_key", null);
        if (key != null && key instanceof byte[]) {
            data = (byte[]) key;
        } else {
            data = new byte[16];
            java.util.Arrays.fill(data, (byte) 0);
        }
        writeChunk(UMD.CDT_LICENSE_KEY, false, data);
    }

    // A
    private void writeContentId() throws IOException {
        int bookId;
        Object id = book.getAttribute("book_id", null);
        if (id != null && id instanceof Integer) {
            bookId = (Integer) id;
        } else {
            bookId = NumberUtils.randInteger(0, 1000) + 0x10000000;
        }
        byte[] data = littleRender.putUint32(bookId);
        writeChunk(UMD.CDT_CONTENT_ID, false, data);
    }

    // 81
    private void writeContentEnd(List<Long> blockChecks) throws IOException {
        int check = NumberUtils.randInteger(0x2000, 0x2FFF);
        writeChunk(UMD.CDT_CONTENT_END, true, littleRender.putUint32(check));
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (long offset: blockChecks) {
            o.write(littleRender.putUint32(offset));
        }
        writeAddition(check, o.toByteArray());
        o.close();
    }

    // 82
    private void writeCoverImage() throws IOException {
        FileObject cover = book.getCover();
        if (cover == null) {
            return;
        }
        int type = UMD.getFormatOfName(FileNameUtils.extensionName(cover.getName()));
        int check = NumberUtils.randInteger(0x1000, 0x1FFF);
        byte[] data = new byte[5];
        data[0] = (byte) type;
        System.arraycopy(littleRender.putUint32(check), 0, data, 1, 4);
        writeChunk(UMD.CDT_COVER_IMAGE, true, data);
        writeAddition(check, cover.readAll());
    }

    // 87
    private void writeSimplePageOffsets(long contentLength) throws IOException {
        int[][] pages = {
                {0x1, 0x10, 0xD0},
                {0x1, 0x10, 0xB0},
                {0x1, 0x0C, 0xD0},
                {0x1, 0x10, 0xB0},
                {0x5, 0x0A, 0xA6}
        };
        for (int[] page: pages) {
            byte[] data = new byte[6];
            data[0] = (byte) page[1];
            data[1] = (byte) page[2];
            long check = NumberUtils.randLong(0x7000, 0x7FFF);
            System.arraycopy(littleRender.putUint32(check), 0, data, 1, 4);
            writeChunk(UMD.CDT_PAGE_OFFSET, page[0], data);
            data = new byte[12];
            System.arraycopy(littleRender.putUint32(17), 0, data, 0, 4);
            System.arraycopy(littleRender.putUint32(0), 0, data, 4, 4);
            System.arraycopy(littleRender.putUint32(contentLength), 0, data, 8, 4);
            writeAddition(check, data);
        }
    }

    // C
    private void writeUmdEnd() throws IOException {
        long length = writtenBytes;
        length += 1+2+2+4+4;
        writeChunk(UMD.CDT_UMD_END, false, littleRender.putUint32(length));
    }

    private void cachePart(Part part, RandomAccessFile file, List<String> titles, List<Long> offsets)
            throws IOException {
        titles.add(part.getTitle());
        offsets.add(file.getFilePointer());
        writeString(file, part.getTitle() + UMD.SYMBIAN_LINE_FEED);
        String text = part.getText();
        if (text.length() != 0) {
            text = text.replaceAll("(\\r\\n)|(\\n)|(\\r)", UMD.SYMBIAN_LINE_FEED);
            writeString(file, text+UMD.SYMBIAN_LINE_FEED);
        }
        for (Part sub: part) {
            cachePart(sub, file, titles, offsets);
        }
    }

    private void writeText(RandomAccessFile file, long contentLength, List<Long> blockChecks)
            throws IOException {
        int count = (int) Math.ceil(contentLength / (double) UMD.BLOCK_SIZE);
        if (count == 1) {
            count = 2;
        }
        int randValA = NumberUtils.randInteger(0, count-1);
        int randValB = NumberUtils.randInteger(0, count - 1);
        for (int i=0; i<count; ++i) {
            long randVal = NumberUtils.randLong(4026530000L, 4294970000L);
            blockChecks.add(randVal);
            byte[] buf = new byte[UMD.BLOCK_SIZE];
            file.read(buf);
            byte[] data = ZLibUtils.compress(buf);
            writeAddition(randVal, data);
            if (i == randValA) {
                writeLicenseKey();
            } else if (i == randValB) {
                writeContentId();
            }
        }
    }

    private void writeString(RandomAccessFile file, String text) throws IOException {
        byte[] data = text.getBytes(UMD.TEXT_ENCODING);
        file.write(data);
    }

    private void findImages(Part part, List<FileObject> images) {
        Object o = part.getAttribute(Book.COVER, null);
        if (o instanceof FileObject) {
            images.add((FileObject) o);
        }
        for (Part sub: part) {
            findImages(sub, images);
        }
    }

    private void writeImages(List<FileObject> images, List<Long> blockChecks) throws IOException {
        if (images.size() == 0) {
            return;
        }
        int rand = NumberUtils.randInteger(0, images.size()-1);
        int i = 0;
        for (FileObject img: images) {
            long check = NumberUtils.randLong(4026530000L, 4294970000L);
            blockChecks.add(check);
            writeAddition(check, img.readAll());
            if (i++ == rand) {
                writeContentId();
            }
        }
    }
}
