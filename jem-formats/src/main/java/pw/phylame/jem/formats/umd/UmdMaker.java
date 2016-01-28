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

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.common.CommonMaker;
import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.text.TextRender;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.util.FileObject;

import pw.phylame.jem.formats.util.*;
import pw.phylame.jem.util.IOUtils;

import static pw.phylame.jem.formats.util.ByteUtils.littleRender;

import java.io.*;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.List;

/**
 * <tt>Maker</tt> implement for UMD book.
 */
public class UmdMaker extends CommonMaker<UmdMakeConfig> {
    private Book book;
    private OutputStream output;
    private long writtenBytes = 0L;

    public UmdMaker() {
        super("umd", UmdMakeConfig.CONFIG_SELF, UmdMakeConfig.class);
    }

    @Override
    public void make(Book book, OutputStream output, UmdMakeConfig config)
            throws IOException, MakerException {
        if (config == null) {
            config = new UmdMakeConfig();
        }
        this.output = output;
        this.book = book;

        writtenBytes = 0L;
        output.write(littleRender.putUInt32(UMD.FILE_MAGIC_NUMBER));
        switch (config.umdType) {
            case UMD.TEXT:
                makeText(config.textConfig);
                break;
            case UMD.CARTOON:
                makeCartoon(config);
                break;
            case UMD.COMIC:
                makeComic();
                break;
            default:
                throw makerException("umd.make.invalidType", config.umdType);
        }
    }

    private void makeText(TextConfig config) throws IOException {
        writeUmdHead(UMD.TEXT);
        writeAttributes();

        // prepare text
        File cache = File.createTempFile("umd_", ".tmp");
        try (BufferedRandomAccessFile source = new BufferedRandomAccessFile(cache, "rw")) {
            UmdRender umdRender = new UmdRender(this, source);
            config.lineSeparator = UMD.UMD_LINE_FEED;
            try {
                TextRender.renderBook(book, umdRender, config);
            } catch (Exception e) {
                throw new IOException(e);
            }

            long contentLength = source.getFilePointer();
            source.seek(0L);

            writeContentLength(contentLength);
            writeChapterOffsets(umdRender.offsets);
            writeChapterTitles(umdRender.titles);

            LinkedList<Long> blockChecks = new LinkedList<>();
            writeText(source, contentLength, blockChecks);
            writeContentEnd(blockChecks);

            writeCoverImage();
            writeSimplePageOffsets(contentLength);
            writeUmdEnd();
        } finally {
            if (!cache.delete()) {
                System.err.println("Failed delete UMD cached file: " + cache);
            }
        }
    }

    private void makeCartoon(UmdMakeConfig config) throws IOException {
        writeUmdHead(UMD.CARTOON);
        writeAttributes();
        // ignored chapter offsets and titles
        List<FileObject> images;
        String imageFormat = "jpg";

        // get cartoon images
        if (config.cartoonImages != null && !config.cartoonImages.isEmpty()) {
            images = config.cartoonImages;
            imageFormat = config.imageFormat;
        } else {
            images = new LinkedList<>();
            // prepare images
            for (Chapter sub : book) {
                findImages(sub, images);
            }
        }

        writeChapterOffsets(null);
        writeChapterTitles(null);
        writeImageFormat(imageFormat);

        LinkedList<Long> blockChecks = new LinkedList<>();
        writeImages(images, blockChecks);
        writeContentEnd(blockChecks);

        writeCoverImage();
        writeLicenseKey();
        writeUmdEnd();
    }

    private void makeComic() throws MakerException {
        throw makerException("umd.make.unsupportedType", UMD.COMIC);
    }

    private void writeChunk(int id, boolean hasAddition, byte[] data) throws IOException {
        writeChunk(id, hasAddition ? UMD.CONTENT_APPENDED : UMD.CONTENT_SINGLE, data);
    }

    private void writeChunk(int id, int type, byte[] data) throws IOException {
        output.write(UMD.CHUNK_SEPARATOR);
        output.write(littleRender.putUInt16(id));
        output.write(type);
        output.write(5 + data.length);
        output.write(data);
        writtenBytes += 5 + data.length;
    }

    private void writeAddition(long checkVal, byte[] data) throws IOException {
        output.write(UMD.ADDITION_SEPARATOR);
        output.write(littleRender.putUInt32(checkVal));
        output.write(littleRender.putUInt32(9 + data.length));
        output.write(data);
        writtenBytes += 9 + data.length;
    }

    // 1
    private void writeUmdHead(int umdType) throws IOException {
        byte[] data = new byte[3];
        data[0] = (byte) umdType;
        int val = NumberUtils.randInteger(0x401, 0x7FFF);
        data[1] = (byte) ((val & 0xFF00) >> 8);
        data[2] = (byte) (val & 0xFF);
        writeChunk(UMD.CDT_UMD_HEAD, false, data);
    }

    private void writeMetaField(int id, String str) throws IOException {
        if (!TextUtils.isValid(str)) {
            return;
        }
        writeChunk(id, false, str.getBytes(UMD.TEXT_ENCODING));
    }

    // 2-9
    private void writeAttributes() throws IOException {
        writeMetaField(UMD.CDT_TITLE, book.getTitle());
        writeMetaField(UMD.CDT_AUTHOR, book.getAuthor());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(book.getDate());
        writeMetaField(UMD.CDT_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
        writeMetaField(UMD.CDT_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
        writeMetaField(UMD.CDT_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));

        writeMetaField(UMD.CDT_GENRE, book.getGenre());
        writeMetaField(UMD.CDT_PUBLISHER, book.getPublisher());
        writeMetaField(UMD.CDT_VENDOR, book.getVendor());
    }

    // B
    private void writeContentLength(long length) throws IOException {
        writeChunk(UMD.CDT_CONTENT_LENGTH, false, littleRender.putUInt32(length));
    }

    // 83
    private void writeChapterOffsets(LinkedList<Long> offsets) throws IOException {
        int checkVal = NumberUtils.randInteger(0x3000, 0x3FFF);
        writeChunk(UMD.CDT_CHAPTER_OFFSET, true, littleRender.putUInt32(checkVal));
        byte[] data;
        if (offsets != null && !offsets.isEmpty()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (long offset : offsets) {
                out.write(littleRender.putUInt32(offset));
            }
            data = out.toByteArray();
        } else {
            data = new byte[0];
        }
        writeAddition(checkVal, data);
    }

    // 84
    private void writeChapterTitles(LinkedList<String> titles) throws IOException {
        int checkVal = NumberUtils.randInteger(0x4000, 0x4FFF);
        writeChunk(UMD.CDT_CHAPTER_TITLE, true, littleRender.putUInt32(checkVal));
        byte[] data;
        if (titles != null && !titles.isEmpty()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (String title : titles) {
                byte[] b = title.getBytes(UMD.TEXT_ENCODING);
                out.write(b.length);
                out.write(b);
            }
            data = out.toByteArray();
        } else {
            data = new byte[0];
        }
        writeAddition(checkVal, data);
    }

    // E
    private void writeImageFormat(String format) throws IOException {
        byte[] bytes = {(byte) UMD.formatOfName(format)};
        writeChunk(UMD.CDT_IMAGE_FORMAT, false, bytes);
    }

    // F1
    private void writeLicenseKey() throws IOException {
        byte[] data;
        Object key = book.getAttribute("license_key", null);
        if (key != null && key instanceof byte[]) {
            data = (byte[]) key;
        } else {
            data = new byte[]{  // 0 x 16
                    0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0
            };
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
        byte[] data = littleRender.putUInt32(bookId);
        writeChunk(UMD.CDT_CONTENT_ID, false, data);
    }

    // 81
    private void writeContentEnd(LinkedList<Long> blockChecks) throws IOException {
        int randVal = NumberUtils.randInteger(0x2000, 0x2FFF);
        writeChunk(UMD.CDT_CONTENT_END, true, littleRender.putUInt32(randVal));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (long checkVal : blockChecks) {
            out.write(littleRender.putUInt32(checkVal));
        }
        writeAddition(randVal, out.toByteArray());
        out.close();
    }

    // 82
    private void writeCoverImage() throws IOException {
        FileObject cover = book.getCover();
        if (cover == null) {
            return;
        }
        int type = UMD.formatOfName(IOUtils.getExtension(cover.getName()));
        int checkVal = NumberUtils.randInteger(0x1000, 0x1FFF);
        byte[] data = new byte[5];
        data[0] = (byte) type;
        System.arraycopy(littleRender.putUInt32(checkVal), 0, data, 1, 4);
        writeChunk(UMD.CDT_COVER_IMAGE, true, data);
        writeAddition(checkVal, cover.readAll());
    }

    // 87, placeholder page
    private void writeSimplePageOffsets(long contentLength) throws IOException {
        int[][] pages = {
                {0x1, 0x10, 0xD0},
                {0x1, 0x10, 0xB0},
                {0x1, 0x0C, 0xD0},
                {0x1, 0x10, 0xB0},
                {0x5, 0x0A, 0xA6}
        };
        byte[] buf6 = new byte[6], buf12 = new byte[12];
        for (int[] page : pages) {
            buf6[0] = (byte) page[1];
            buf6[1] = (byte) page[2];
            long checkVal = NumberUtils.randLong(0x7000, 0x7FFF);
            System.arraycopy(littleRender.putUInt32(checkVal), 0, buf6, 2, 4);
            writeChunk(UMD.CDT_PAGE_OFFSET, page[0], buf6);

            System.arraycopy(littleRender.putUInt32(17), 0, buf12, 0, 4);
            System.arraycopy(littleRender.putUInt32(0), 0, buf12, 4, 4);
            System.arraycopy(littleRender.putUInt32(contentLength), 0, buf12, 8, 4);
            writeAddition(checkVal, buf12);
        }
    }

    // C
    private void writeUmdEnd() throws IOException {
        long length = writtenBytes;
        length += 1 + 2 + 2 + 4 + 4;
        writeChunk(UMD.CDT_UMD_END, false, littleRender.putUInt32(length));
    }

    private void writeText(BufferedRandomAccessFile file, long contentLength,
                           LinkedList<Long> blockChecks) throws IOException {
        int count = (int) (contentLength >> 15);  // div 0x8000
        count += ((contentLength & 0x7FFF) > 0) ? 1 : 0;    // mod 0x8000 > 0
        int randValA = NumberUtils.randInteger(0, count);
        int randValB = NumberUtils.randInteger(0, count);
        for (int i = 0; i < count; ++i) {
            long checkVal = NumberUtils.randLong(4026530000L, 4294970000L);
            blockChecks.add(checkVal);
            byte[] buf = new byte[UMD.BLOCK_SIZE];
            file.read(buf);
            byte[] data = ZLibUtils.compress(buf);
            writeAddition(checkVal, data);
            if (i == randValA) {
                writeLicenseKey();
            } else if (i == randValB) {
                writeContentId();
            }
        }
    }

    void writeString(RandomAccessFile file, String text) throws IOException {
        byte[] data = text.getBytes(UMD.TEXT_ENCODING);
        file.write(data);
    }

    private void findImages(Chapter chapter, List<FileObject> images) {
        FileObject cover = chapter.getCover();
        if (cover != null) {
            images.add(cover);
        }
        for (Chapter sub : chapter) {
            findImages(sub, images);
        }
    }

    private void writeImages(List<FileObject> images, LinkedList<Long> blockChecks)
            throws IOException {
        if (images.isEmpty()) {
            return;
        }
        int randVal = NumberUtils.randInteger(0, images.size() - 1);
        int i = 0;
        for (FileObject img : images) {
            long checkVal = NumberUtils.randLong(4026530000L, 4294970000L);
            blockChecks.add(checkVal);
            writeAddition(checkVal, img.readAll());
            if (i++ == randVal) {
                writeContentId();
            }
        }
    }
}
