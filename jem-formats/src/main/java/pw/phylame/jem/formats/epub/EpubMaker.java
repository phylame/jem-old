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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.util.I18N;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.epub.writer.EpubWriter;
import pw.phylame.jem.formats.epub.writer.EpubWriterFactory;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Maker for ePub.
 */
public class EpubMaker implements Maker {
    private static      Log    LOG                 = LogFactory.getLog(EpubMaker.class);

    public static final String KEY_CONFIG          = "epub_config";
    public static final String KEY_VERSION         = "epub_version";
    public static final String KEY_HTML_ENCODING   = "epub_html_encoding";
    public static final String KEY_COMPRESS_METHOD = "epub_compress_method";
    public static final String KEY_COMPRESS_LEVEL  = "epub_compress_level";
    public static final String KEY_COMMENT         = "epub_comment";

    @Override
    public String getName() {
        return "epub";
    }

    private EpubConfig parseConfig(Map<String, Object> kw) throws JemException {
        EpubConfig config = new EpubConfig();
        if (kw == null || kw.size() == 0) {
            return config;
        }

        Object o = kw.get(KEY_CONFIG);
        if (o != null) {
            if (o instanceof EpubConfig) {
                return (EpubConfig) o;
            } else {
                throw new JemException(I18N.getText("Epub.Maker.InvalidConfig", KEY_CONFIG, o.getClass()));
            }
        }

        o = kw.get(KEY_VERSION);
        if (o != null) {
            if (o instanceof String) {
                config.version = (String) o;
            } else {
                throw ExceptionFactory.forInvalidStringArgument(KEY_VERSION, o);
            }
        }
        o = kw.get(KEY_HTML_ENCODING);
        if (o != null) {
            if (o instanceof String) {
                config.htmlEncoding = (String) o;
            } else {
                throw ExceptionFactory.forInvalidStringArgument(KEY_HTML_ENCODING, o);
            }
        }
        o = kw.get(KEY_COMPRESS_METHOD);
        if (o != null) {
            if (o instanceof Integer) {
                config.zipMethod = (Integer) o;
            } else if (o instanceof String) {
                String s = (String) o;
                try {
                    config.zipMethod = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    throw new JemException(I18N.getText("Epub.Maker.InvalidZipMethod", s), ex);
                }
            } else {
                throw ExceptionFactory.forInvalidIntegerArgument(KEY_COMPRESS_METHOD, o);
            }
        }
        o = kw.get(KEY_COMPRESS_LEVEL);
        if (o != null) {
            if (o instanceof Integer) {
                config.zipLevel = (Integer) o;
            } else if (o instanceof String) {
                String s = (String) o;
                try {
                    config.zipLevel = Integer.parseInt(s);
                } catch (NumberFormatException ex) {
                    throw new JemException(I18N.getText("Epub.Maker.InvalidZipLevel", s), ex);
                }
            } else {
                throw ExceptionFactory.forInvalidIntegerArgument(KEY_COMPRESS_LEVEL, o);
            }
        }
        o = kw.get(KEY_COMMENT);
        if (o instanceof String) {
            config.zipComment = (String) o;
        } else {
            LOG.debug("invalid 'epub_comment', required string");
        }

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

    public void make(Book book, ZipOutputStream zipout, EpubConfig config) throws IOException, JemException {
        EpubWriter writer = EpubWriterFactory.getWriter(config.version);
        if (writer == null) {
            throw new MakerException(I18N.getText("Epub.UnsupportedVersion", config.version), getName());
        }
        writer.make(book, zipout, config);
        writeMIME(zipout);
        zipout.setComment(config.zipComment);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(EPUB.MIME_FILE));
        zipout.write(EPUB.MT_EPUB.getBytes("ASCII"));
        zipout.closeEntry();
    }
}
