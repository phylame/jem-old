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

package pw.phylame.jem.formats.epub.writer;

import org.dom4j.Document;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.util.I18N;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.epub.ncx.NcxBuilder;
import pw.phylame.jem.formats.epub.ncx.NcxBuilderFactory;
import pw.phylame.jem.formats.epub.opf.OpfBuilder;
import pw.phylame.jem.formats.epub.opf.OpfBuilderFactory;
import pw.phylame.jem.formats.util.XmlUtils;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.tools.StringUtils;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ePub 2.0 implements.
 * Using components:
 * <ul>
 *     <li>OPF: 2.0</li>
 *     <li>NCX: 2005-1</li>
 * </ul>
 */
public class WriterV2 extends EpubWriter {
    @Override
    public void make(Book book, ZipOutputStream zipout, EpubConfig config) throws IOException, JemException {
        // UUID of the book
        String uuid = book.stringAttribute("uuid", null);
        if (StringUtils.isEmpty(uuid)) {
            uuid = java.util.UUID.randomUUID().toString();
        }

        // make OPF document
        OpfBuilder opfBuilder = OpfBuilderFactory.getBuilder("2.0");
        if (opfBuilder == null) {
            throw new MakerException(I18N.getText("Epub.WriterV2.NoOpf_2_0"), "epub");
        }
        Document opfDocument = opfBuilder.make(book, uuid, zipout, config);

        // make and write NCX document
        NcxBuilder ncxBuilder = NcxBuilderFactory.getBuilder("2005-1");
        if (ncxBuilder == null) {
            throw new MakerException(I18N.getText("Epub.WriterV2.NoNcx_2005_1"), "epub");
        }

        opfBuilder.addManifestItem(EPUB.NCX_FILE_ID, EPUB.NCX_FILE, EPUB.MT_NCX);

        Document ncxDocument = ncxBuilder.make(book, uuid, zipout, opfBuilder, config);
        zipout.putNextEntry(new ZipEntry(EPUB.getOpsPath(EPUB.NCX_FILE, config)));
        XmlUtils.writeXML(ncxDocument, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();

        // write OPF document
        String opfPath = EPUB.getOpsPath(EPUB.OPF_FILE, config);
        zipout.putNextEntry(new ZipEntry(opfPath));
        XmlUtils.writeXML(opfDocument, zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();

        // container
        zipout.putNextEntry(new ZipEntry(EPUB.CONTAINER_FILE));
        XmlUtils.writeXML(makeContainer(opfPath), zipout, config.xmlEncoding, config.xmlIndent, config.xmlLineSeparator);
        zipout.closeEntry();
    }
}
