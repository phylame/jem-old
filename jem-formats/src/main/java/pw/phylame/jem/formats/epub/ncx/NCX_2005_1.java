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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.formats.epub.*;
import pw.phylame.jem.formats.epub.writer.EpubWriter;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.formats.util.xml.XmlRender;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * NCX version 2005-1
 */
class NCX_2005_1 implements NcxWriter, ContentsListener {
    public static final String DT_ID = "-//NISO//DTD ncx 2005-1//EN";
    public static final String DT_URI = "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd";

    public static final String VERSION = "2005-1";
    public static final String NAMESPACE = "http://www.daisy.org/z3986/2005/ncx/";

    private int playOrder = 1;

    private XmlRender xmlRender;
    private ContentsRender contentsRender;

    @Override
    public void write(Book book, EpubMakeConfig epubConfig, XmlRender xmlRender,
                      EpubWriter epubWriter,
                      ZipOutputStream zipout) throws IOException, MakerException {
        this.xmlRender = xmlRender;
        xmlRender.startXml();
        xmlRender.docdecl("ncx", DT_ID, DT_URI);

        xmlRender.startTag("ncx").attribute("version", VERSION);
        String lang = EPUB.languageOfBook(book);
        epubConfig.htmlConfig.htmlLanguage = lang;
        xmlRender.attribute("xml:lang", lang).attribute("xmlns", NAMESPACE);

        int depth = Jem.depthOfChapter(book);
        writeHead(depth, epubConfig.uuid, 0, 0, xmlRender);

        // docTitle
        xmlRender.startTag("docTitle");
        xmlRender.startTag("text").text(book.getTitle()).endTag();
        xmlRender.endTag();
        // docAuthor
        String author = book.getAuthor();
        if (TextUtils.isValid(author)) {
            xmlRender.startTag("docAuthor");
            xmlRender.startTag("text").text(author).endTag();
            xmlRender.endTag();
        }

        // navMap
        xmlRender.startTag("navMap");
        // render contents
        contentsRender = new ContentsRender(book, epubWriter,
                epubConfig, zipout, this);
        contentsRender.start();

        xmlRender.endTag(); // navMap
        xmlRender.endTag(); // ncx

        xmlRender.endXml();
    }

    @Override
    public String getCoverID() {
        return contentsRender.getCoverID();
    }

    @Override
    public List<Resource> getResources() {
        return contentsRender.getResources();
    }

    @Override
    public List<SpineItem> getSpineItems() {
        return contentsRender.getSpineItems();
    }

    @Override
    public List<GuideItem> getGuideItems() {
        return contentsRender.getGuideItems();
    }

    private void writeHead(int depth, String uuid,
                           int totalPageCount, int maxPageNumber,
                           XmlRender xmlRender) throws IOException {
        xmlRender.startTag("head");
        xmlRender.startTag("meta").attribute("name", "dtb:uid");
        xmlRender.attribute("content", uuid).endTag();
        xmlRender.startTag("meta").attribute("name", "dtb:depth");
        xmlRender.attribute("content", Integer.toString(depth)).endTag();
        xmlRender.startTag("meta").attribute("name", "dtb:totalPageCount");
        xmlRender.attribute("content", Integer.toString(totalPageCount)).endTag();
        xmlRender.startTag("meta").attribute("name", "dtb:maxPageNumber");
        xmlRender.attribute("content", Integer.toString(maxPageNumber)).endTag();
        xmlRender.endTag();
    }


    @Override
    public void startNaviPoint(String id, String href, String title) throws IOException {
        xmlRender.startTag("navPoint").attribute("id", id);
        xmlRender.attribute("playOrder", Integer.toString(playOrder++));

        xmlRender.startTag("navLabel");
        xmlRender.startTag("text").text(title).endTag();
        xmlRender.endTag(); // navLabel

        xmlRender.startTag("content").attribute("src", href).endTag();
    }

    @Override
    public void endNaviPoint() throws IOException {
        xmlRender.endTag(); // navPoint
    }
}
