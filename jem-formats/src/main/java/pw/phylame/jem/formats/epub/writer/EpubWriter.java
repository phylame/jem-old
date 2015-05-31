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
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.epub.EpubConfig;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * ePub writer
 */
public abstract class EpubWriter {
    public static final String CONTAINER_XML_NS = "urn:oasis:names:tc:opendocument:xmlns:container";
    public static final String CONTAINER_VERSION = "1.0";

    public abstract void make(Book book, ZipOutputStream zipout, EpubConfig config)
            throws IOException, JemException;

    protected Document makeContainer(String opfPath) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("container", CONTAINER_XML_NS);
        root.addAttribute("version", CONTAINER_VERSION);
        Element rootfile = root.addElement("rootfiles").addElement("rootfile");
        rootfile.addAttribute("full-path", opfPath);
        rootfile.addAttribute("media-type", EPUB.MT_OPF);
        return doc;
    }
}
