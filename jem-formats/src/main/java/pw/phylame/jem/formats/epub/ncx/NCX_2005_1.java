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

package pw.phylame.jem.formats.epub.ncx;

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.formats.epub.EpubConfig;

import java.util.zip.ZipOutputStream;

/**
 * NCX version 2005-1
 */
public class Ncx_2005_1 implements NcxBuilder {
    public static final String DT_TYPE = "ncx";
    public static final String DT_ID   = "-//NISO//DTD ncx 2005-1//EN";
    public static final String DT_URI  = "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd";

    public static final String VERSION   = "2005-1";
    public static final String NAMESPACE = "http://www.daisy.org/z3986/2005/ncx/";

    private int playOrder = 1;

    @Override
    public Document make(Book book, String uuid, ZipOutputStream zipout, EpubConfig config) {
        Document doc = DocumentHelper.createDocument();
        doc.addDocType(DT_TYPE, DT_ID, DT_URI);

        Element root = doc.addElement("ncx", NAMESPACE);
        root.addAttribute("version", VERSION);
        Element head = root.addElement("head");
        head.addElement("meta").addAttribute("name", "dtb:uid").addAttribute("content", uuid);
        head.addElement("meta").addAttribute("name", "dtb:depth").addAttribute("content",
                Integer.toString(Jem.getDepth(book)));
        Object o = book.getAttribute("pages", 0);   // page number
        String str;
        if (o instanceof Integer) {
            str = Integer.toString((Integer) o);
        } else {
            str = "0";
        }
        head.addElement("meta").addAttribute("name", "dtb:totalPageCount").addAttribute("content",
                str);
        head.addElement("meta").addAttribute("name", "dtb:maxPageNumber").addAttribute("content",
                "0");

        root.addElement("docTitle").addElement("text").setText(book.getTitle());
        if (!"".equals(book.getAuthor())) {
            root.addElement("docAuthor").addElement("text").setText(book.getAuthor());
        }

        Element navMap = root.addElement("navMap");

        // cover
        // intro
        // info
        // toc
        // content

        return doc;
    }

    private void writeBookCoverPage(Book book, ZipOutputStream zipout, EpubConfig config) {

    }

    private Element makeNavPoint(Element parent, String id, String label, String href) {
        Element nvp = parent.addElement("navPoint");
        nvp.addAttribute("id", id);
        nvp.addAttribute("playOrder", Integer.toString(this.playOrder++));
        nvp.addElement("navLabel").addElement("text").setText(label);
        if (href != null) {
            nvp.addElement("content").addAttribute("src", href);
        }
        return nvp;

    }

    private void makeChapter() {

    }
}
