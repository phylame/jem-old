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

package pw.phylame.jem.formats.epub;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.formats.epub.writer.EpubWriter;
import pw.phylame.jem.formats.epub.writer.EpubWriterFactory;
import pw.phylame.jem.util.JemException;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Maker for ePub.
 */
public class EpubMaker implements Maker {
    public static final String KEY_VERSION = "epub_version";
    public static final String KEY_HTML_ENCODING   = "epub_html_encoding";
    public static final String KEY_COMPRESS_METHOD = "epub_compress_method";
    public static final String KEY_COMPRESS_LEVEL  = "epub_compress_level";

    @Override
    public String getName() {
        return "epub";
    }

    private EpubConfig parseConfig(Map<String, Object> kw) {
        EpubConfig config = new EpubConfig();

        return config;
    }

    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        make(book, file, parseConfig(kw));
    }

    public void make(Book book, File file, EpubConfig config) throws IOException, JemException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        ZipOutputStream zipout = new ZipOutputStream(stream);
        zipout.setMethod(config.zipMethod);
        zipout.setLevel(config.zipLevel);
        make(book, zipout, config);
        zipout.close();
    }

    public void make(Book book, ZipOutputStream zipout, EpubConfig config) throws IOException,
            JemException {
        zipout.setComment(config.zipComment);
        writeMIME(zipout);
        EpubWriter writer = EpubWriterFactory.getWriter(config.version);
        if (writer == null) {
            throw new JemException("Unsupported ePub version: "+config.version);
        }
        writer.make(book, zipout, config);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(EPUB.MIME_FILE));
        zipout.write(EPUB.MT_EPUB.getBytes("ASCII"));
        zipout.closeEntry();
    }
}
