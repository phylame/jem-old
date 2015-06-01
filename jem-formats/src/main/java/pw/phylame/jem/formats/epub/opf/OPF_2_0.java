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

package pw.phylame.jem.formats.epub.opf;

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;

import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipOutputStream;

/**
 * OPF 2.0 implements.
 */
public class OPF_2_0 extends AbstractOpfBuilder {
    public static final String OPF_XML_NS = "http://www.idpf.org/2007/opf";
    public static final String OPF_VERSION_2 = "2.0";

    public static final String[] OPTIONAL_METADATA = {"source", "relation", "format"};

    private void addDcmi(Element parent, Book book, String uuid, ZipOutputStream zipout, EpubConfig config)
            throws IOException {
        Element elem = parent.addElement("dc:identifier").addAttribute("id", EPUB.BOOK_ID_NAME);
        elem.addAttribute("opf:scheme", "uuid").setText(uuid);

        parent.addElement("dc:title").setText(book.getTitle());

        String str = book.getAuthor();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:creator").addAttribute("opf:role", "aut").setText(str);
        }

        str = book.getGenre();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:type").setText(str);
        }

        str = book.getSubject();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:subject").setText(str);
        }

        TextObject intro = book.getIntro();
        if (intro != null) {
            String text = intro.getText();
            if (text.length() > 0) {
                parent.addElement("dc:description").setText(text);
            }
        }

        str = book.getPublisher();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:publisher").setText(str);
        }

        FileObject cover = book.getCover();
        if (cover != null) {
            coverHref = String.format("%s/%s.%s", config.imageDir, EPUB.COVER_NAME,
                    FilenameUtils.getExtension(cover.getName()));
            ZipUtils.writeFile(cover, zipout, EPUB.getOpsPath(coverHref, config));
            addManifestItem(EPUB.COVER_FILE_ID, coverHref, cover.getMime());
            parent.addElement("meta").addAttribute("name", "cover").addAttribute("content", EPUB.COVER_FILE_ID);
        }

        Date date = book.getDate();
        if (date != null) {
            elem = parent.addElement("dc:date").addAttribute("opf:event", "creation");
            elem.setText(DateUtils.formatDate(date, config.dateFormat));

            Date today = new Date();
            if (! today.equals(date)) {
                elem = parent.addElement("dc:date").addAttribute("opf:event", "modification");
                elem.setText(DateUtils.formatDate(today, config.dateFormat));
            }
        }

        str = book.getLanguage();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:language").setText(str);
        }

        str = book.getRights();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:rights").setText(str);
        }

        str = book.getVendor();
        if (!StringUtils.isEmpty(str)) {
            parent.addElement("dc:contributor").addAttribute("opf:role", "bkp").setText(str);
        }

        for (String key: OPTIONAL_METADATA) {
            str = book.stringAttribute(key, null);
            if (! StringUtils.isEmpty(str)) {
                parent.addElement("dc:"+key).setText(str);
            }
        }
    }

    @Override
    public Document make(Book book, String uuid, ZipOutputStream zipout, EpubConfig config) throws IOException {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("package", OPF_XML_NS);
        root.addAttribute("version", OPF_VERSION_2);
        root.addAttribute("unique-identifier", EPUB.BOOK_ID_NAME);

        Element metadataElement = root.addElement("metadata");
        metadataElement.addNamespace("dc", EPUB.DC_XML_NS);
        metadataElement.addNamespace("opf", OPF_XML_NS);

        manifestElement = root.addElement("manifest");
        spineElement = root.addElement("spine").addAttribute("toc", EPUB.NCX_FILE_ID);
        guideElement = root.addElement("guide");

        addDcmi(metadataElement, book, uuid, zipout, config);

        return doc;
    }
}
