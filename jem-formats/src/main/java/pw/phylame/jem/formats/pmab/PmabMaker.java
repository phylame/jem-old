/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.file.FileUtils;

import java.io.*;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <tt>Maker</tt> implement for PMAB book.
 */
public class PmabMaker implements Maker {
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
        if (kw != null && kw.size() != 0) {
            Object o = kw.get("pmab_version");
            if (o != null) {
                if (o instanceof String) {
                    config.pmabVersion = (String) o;
                } else {
                    throw new JemException("Invalid pmab_version string: " + o);
                }
            }
            o = kw.get("pmab_text_encoding");
            if (o != null) {
                if (o instanceof String) {
                    config.textEncoding = (String) o;
                } else {
                    throw new JemException("Invalid pmab_text_encoding: " + o);
                }
            }
            o = kw.get("pmab_compress_method");
            if (o != null) {
                if (o instanceof Integer) {
                    config.zipMethod = (Integer) o;
                } else if (o instanceof String) {
                    String s = (String) o;
                    try {
                        config.zipMethod = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        throw new JemException("Invalid ZIP method: "+s, ex);
                    }
                } else {
                    throw new JemException("pmab_compress_method require int or str");
                }
            }
            o = kw.get("pmab_compress_level");
            if (o != null) {
                if (o instanceof Integer) {
                    config.zipLevel = (Integer) o;
                } else if (o instanceof String) {
                    String s = (String) o;
                    try {
                        config.zipLevel = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        throw new JemException("Invalid ZIP level: "+s, ex);
                    }
                } else {
                    throw new JemException("pmab_compress_level require int or str");
                }
            }
            o = kw.get("pmab_meta_data");
            if (o instanceof Map) {
                Map map = (Map) o;
                config.metaInfo = new HashMap<String, String>();
                for (Object key: map.keySet()) {
                    Object v = map.get(key);
                    if (v != null) {
                        config.metaInfo.put(String.valueOf(key), String.valueOf(v));
                    }
                }
            }
        }
        return config;
    }

    public void make(Book book, File file, PmabConfig config) throws IOException, JemException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        ZipOutputStream zipout = new ZipOutputStream(stream);
        zipout.setMethod(config.zipMethod);
        zipout.setLevel(config.zipLevel);
        make(book, zipout, config);
        zipout.close();
    }

    public void make(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        zipout.setComment(config.zipComment);
        writeMIME(zipout);
        writePBM(book, zipout, config);
        writePBC(book, zipout, config);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(PMAB.MIME_FILE));
        FileUtils.writeText(zipout, PMAB.MT_PMAB, "UTF-8");
        zipout.closeEntry();
    }

    private void writePBM(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if (config.pmabVersion.startsWith("3")) {
            pw.phylame.jem.formats.pmab.v3.Writer.writePBM(book, doc, zipout, config);
        } else if (config.pmabVersion.startsWith("2")) {
            pw.phylame.jem.formats.pmab.v2.Writer.writePBM(book, doc, zipout, config);
        } else {
            throw new JemException("Unsupported PMAB version: "+config.pmabVersion);
        }
        zipout.putNextEntry(new ZipEntry(PMAB.PBM_FILE));
        PMAB.writeXML(doc, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();
    }

    private void writePBC(Book book, ZipOutputStream zipout, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if (config.pmabVersion.startsWith("3")) {
            pw.phylame.jem.formats.pmab.v3.Writer.writePBC(book, doc, zipout, config);
        } else if (config.pmabVersion.startsWith("2")) {
            pw.phylame.jem.formats.pmab.v2.Writer.writePBC(book, doc, zipout, config);
        } else {
            throw new JemException("Unsupported PMAB version: "+config.pmabVersion);
        }
        zipout.putNextEntry(new ZipEntry(PMAB.PBC_FILE));
        PMAB.writeXML(doc, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();
    }
}
