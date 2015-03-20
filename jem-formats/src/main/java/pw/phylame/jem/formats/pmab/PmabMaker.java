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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.formats.pmab.writer.*;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <tt>Maker</tt> implement for PMAB book.
 */
public class PmabMaker implements Maker {
    private static Log LOG = LogFactory.getLog(PmabMaker.class);

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
        PmabConfig config = new PmabConfig();
        if (kw != null && kw.size() != 0) {
            Object o = kw.get("pmab_version");
            if (o instanceof String) {
                config.pmabVersion = (String)o;
            } else {
                throw new JemException("invalid pmab_version string: "+o);
            }
            o = kw.get("pmab_method");
            if (o != null) {
                if (o instanceof Integer) {
                    config.zipMethod = (Integer) o;
                } else if (o instanceof String) {
                    String s = (String) o;
                    try {
                        int n = Integer.parseInt(s);
                    } catch (NumberFormatException ex) {
                        throw new JemException("Invalid ZIP method: "+s);
                    }
                } else {
                    throw new JemException("pmab_method require int or str");
                }
            }
        }
        make(book, file, config);
    }

    public void make(Book book, File file, PmabConfig config) throws IOException, JemException {
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
        zipOut.setMethod(config.zipMethod);
        zipOut.setLevel(config.zipLevel);
        make(book, zipOut, config);
        zipOut.close();
    }

    public void make(Book book, ZipOutputStream zipOut, PmabConfig config) throws IOException, JemException {
        zipOut.setComment("Generated by Jem v"+ Jem.VERSION);
        writeMIME(zipOut);
        writePBM(book, zipOut, config);
        writePBC(book, zipOut, config);
    }

    private void writeMIME(ZipOutputStream zipOut) throws IOException {
        zipOut.putNextEntry(new ZipEntry(Pmab.MIME_FILE));
        FileUtils.writeText(zipOut, Pmab.MT_PMAB, "UTF-8");
    }

    private void writePBM(Book book, ZipOutputStream zipOut, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if ("3.0".equals(config.pmabVersion)) {
            WriterV3.writePBM(book, doc, zipOut, config);
        } else if ("2.0".equals(config.pmabVersion)) {
            WriterV2.writePBM(book, doc, zipOut, config);
        } else if ("1.0".equals(config.pmabVersion)) {
            WriterV1.writePBM(book, doc, zipOut, config);
        } else {
            throw new JemException("Unsupported PMAB version: "+config.pmabVersion);
        }
        zipOut.putNextEntry(new ZipEntry(Pmab.PBM_FILE));
        Pmab.writeXML(doc, zipOut, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
    }

    private void writePBC(Book book, ZipOutputStream zipOut, PmabConfig config) throws IOException, JemException {
        Document doc = DocumentHelper.createDocument();
        if ("3.0".equals(config.pmabVersion)) {
            WriterV3.writePBC(book, doc, zipOut, config);
        } else if ("2.0".equals(config.pmabVersion)) {
            WriterV2.writePBC(book, doc, zipOut, config);
        } else if ("1.0".equals(config.pmabVersion)) {
            WriterV1.writePBC(book, doc, zipOut, config);
        } else {
            throw new JemException("Unsupported PMAB version: "+config.pmabVersion);
        }
        zipOut.putNextEntry(new ZipEntry(Pmab.PBC_FILE));
        Pmab.writeXML(doc, zipOut, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
    }
}
