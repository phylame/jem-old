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
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.epub.ncx.NcxBuilder;
import pw.phylame.jem.formats.epub.ncx.NcxBuilderFactory;
import pw.phylame.jem.formats.epub.opf.OpfBuilder;
import pw.phylame.jem.formats.epub.opf.OpfBuilderFactory;
import pw.phylame.jem.formats.util.XmlUtils;
import pw.phylame.jem.util.JemException;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WriterV2 extends EpubWriter {
    @Override
    public void make(Book book, ZipOutputStream zipout, EpubConfig config) throws IOException,
            JemException {
        String uuid = book.stringAttribute("uuid", java.util.UUID.randomUUID().toString());
        System.out.println(uuid);

        NcxBuilder ncxBuilder = NcxBuilderFactory.getBuilder("2005-1");
        if (ncxBuilder == null) {
            throw new JemException("No NCX 2005-1 implement");
        }
        Document ncxDocument = ncxBuilder.make(book, uuid, zipout, config);
        zipout.putNextEntry(new ZipEntry(EPUB.getOpsPath(EPUB.NcxFileName, config)));
        XmlUtils.writeXML(ncxDocument, zipout, config.xmlEncoding, config.xmlIndent,
                config.xmlLineSeparator);
        zipout.flush();

        OpfBuilder opfBuilder = OpfBuilderFactory.getBuilder("2.0");
        if (opfBuilder == null) {
            throw new JemException("No OPF 2.0 implement");
        }
        Document opfDocument = opfBuilder.make(book, uuid, zipout);
        String opfPath = EPUB.getOpsPath(EPUB.OpfFileName, config);
        zipout.putNextEntry(new ZipEntry(opfPath));
        XmlUtils.writeXML(opfDocument, zipout, config.xmlEncoding, config.xmlIndent,
                config.xmlLineSeparator);
        zipout.flush();

        zipout.putNextEntry(new ZipEntry(EPUB.CONTAINER_FILE));
        XmlUtils.writeXML(makeContainer(opfPath), zipout, config.xmlEncoding, config.xmlIndent,
                config.xmlLineSeparator);
        zipout.flush();
    }

}
