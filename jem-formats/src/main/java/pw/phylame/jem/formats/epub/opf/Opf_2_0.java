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

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.EPUB;

import java.util.zip.ZipOutputStream;

/**
 * OPF 2.0
 */
public class Opf_2_0 implements OpfBuilder {
    public static final String OPF_XML_NS = "http://www.idpf.org/2007/opf";
    public static final String OPF_VERSION_2 = "2.0";


    @Override
    public Document make(Book book, String uuid, ZipOutputStream zipout) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("package", OPF_XML_NS);
        root.addAttribute("version", OPF_VERSION_2);
        root.addAttribute("unique-identifier", EPUB.BOOK_ID_NAME);

        Element metadataElement = root.addElement("metadata");
        metadataElement.addNamespace("dc", EPUB.DC_XML_NS);
        metadataElement.addNamespace("opf", OPF_XML_NS);
//
//        this.manifestElement = root.addElement("manifest");
//        this.spineElement = root.addElement("spine");
//        this.spineElement.addAttribute("toc", Epub.NcxFileId);
//
//        this.guideElement = root.addElement("guide");
//
//        makeDcmi(metadataElement, uuid, book, zipOut);

        return doc;

    }
}
