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

package pw.phylame.jem.formats.pmab;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.XmlUtils;
import pw.phylame.jem.formats.util.I18nMessage;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ExceptionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <tt>Maker</tt> implement for PMAB book.
 */
public class PmabMaker implements Maker {
    private static Log LOG = LogFactory.getLog(PmabMaker.class);

    public static final String KEY_CONFIG          = "pmab_config";
    public static final String KEY_VERSION         = "pmab_version";
    public static final String KEY_TEXT_ENCODING   = "pmab_text_encoding";
    public static final String KEY_COMPRESS_METHOD = "pmab_compress_method";
    public static final String KEY_COMPRESS_LEVEL  = "pmab_compress_level";
    public static final String KEY_COMMENT         = "pmab_comment";
    public static final String KEY_META_DATA       = "pmab_meta_data";


    /**
     * Returns the format name(normally the extension name).
     */
    @Override
    public String getName() {
        return "pmab";
    }

    /**
     * Writes <tt>Book</tt> to book file.
     *
     * @param book the <tt>Book</tt> to be written
     * @param file output file to store book
     * @param kw   arguments to the maker
     * @throws java.io.IOException              occurs IO errors
     * @throws pw.phylame.jem.util.JemException occurs errors when making book file
     */
    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        make(book, file, parseConfig(kw));
    }

    private PmabConfig parseConfig(Map<String, Object> kw) throws JemException {
        PmabConfig config = new PmabConfig();
        if (kw == null || kw.size() == 0) {
            return config;
        }

        Object o = kw.get(KEY_CONFIG);
        if (o != null) {
            if (o instanceof PmabConfig) {
                return (PmabConfig) o;
            } else {
                throw new JemException(
                        I18nMessage.getText("Pmab.Maker.InvalidConfig", KEY_CONFIG, o.getClass()));
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
        o = kw.get(KEY_TEXT_ENCODING);
        if (o != null) {
            if (o instanceof String) {
                config.textEncoding = (String) o;
            } else {
                throw ExceptionFactory.forInvalidStringArgument(KEY_TEXT_ENCODING, o);
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
                    throw new JemException(I18nMessage.getText("Pmab.Maker.InvalidZipMethod", s), ex);
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
                    throw new JemException(I18nMessage.getText("Pmab.Maker.InvalidZipLevel", s), ex);
                }
            } else {
                throw ExceptionFactory.forInvalidIntegerArgument(KEY_COMPRESS_LEVEL, o);
            }
        }
        o = kw.get(KEY_COMMENT);
        if (o instanceof String) {
            config.zipComment = (String) o;
        } else {
            LOG.debug("invalid 'pmab_comment', required string");
        }
        o = kw.get(KEY_META_DATA);
        if (o instanceof Map) {
            Map map = (Map) o;
            config.metaInfo = new HashMap<Object, Object>(map);
        } else {
            LOG.debug("invalid 'pmab_meta_data', required map");
        }

        return config;
    }

    public void make(Book book, File file, PmabConfig config) throws IOException, JemException {
        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        ZipOutputStream zipout = new ZipOutputStream(stream);
        zipout.setMethod(config.zipMethod);
        zipout.setLevel(config.zipLevel);
        make(book, zipout, config);
        zipout.close();
    }

    public void make(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        writePBM(book, zipout, config);
        writePBC(book, zipout, config);
        writeMIME(zipout);
        zipout.setComment(config.zipComment);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(PMAB.MIME_FILE));
        zipout.write(PMAB.MT_PMAB.getBytes("ASCII"));
        zipout.closeEntry();
    }

    private void writePBM(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if (config.version.startsWith("3")) {
            pw.phylame.jem.formats.pmab.v3.Writer.writePBM(book, doc, zipout, config);
        } else if (config.version.startsWith("2")) {
            pw.phylame.jem.formats.pmab.v2.Writer.writePBM(book, doc, zipout, config);
        } else {
            throw new MakerException(I18nMessage.getText("Pmab.UnsupportedVersion", config.version), getName());
        }
        zipout.putNextEntry(new ZipEntry(PMAB.PBM_FILE));
        XmlUtils.writeXML(doc, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();
    }

    private void writePBC(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if (config.version.startsWith("3")) {
            pw.phylame.jem.formats.pmab.v3.Writer.writePBC(book, doc, zipout, config);
        } else if (config.version.startsWith("2")) {
            pw.phylame.jem.formats.pmab.v2.Writer.writePBC(book, doc, zipout, config);
        } else {
            throw new MakerException(I18nMessage.getText("Pmab.UnsupportedVersion", config.version), getName());
        }
        zipout.putNextEntry(new ZipEntry(PMAB.PBC_FILE));
        XmlUtils.writeXML(doc, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();
    }
}
