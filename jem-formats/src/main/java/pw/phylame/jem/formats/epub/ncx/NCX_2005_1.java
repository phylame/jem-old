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

import org.apache.commons.io.FilenameUtils;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.epub.html.HtmlMaker;
import pw.phylame.jem.formats.epub.opf.OpfBuilder;
import pw.phylame.jem.formats.util.I18N;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;

import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

/**
 * NCX version 2005-1
 */
public class NCX_2005_1 extends AbstractNcxBuilder {
    public static final String DT_TYPE = "ncx";
    public static final String DT_ID   = "-//NISO//DTD ncx 2005-1//EN";
    public static final String DT_URI  = "http://www.daisy.org/z3986/2005/ncx-2005-1.dtd";

    public static final String VERSION   = "2005-1";
    public static final String NAMESPACE = "http://www.daisy.org/z3986/2005/ncx/";

    public static final String[] INFO_ATTRIBUTE_NAMES = {"author", "genre", "publisher", "date", "language", "rights"};

    private int playOrder = 1;

    private void addHead(Element ncx, String uuid, Book book, int depth) {
        Element head = ncx.addElement("head");
        head.addElement("meta").addAttribute("name", "dtb:uid").addAttribute("content", uuid);
        head.addElement("meta").addAttribute("name", "dtb:depth").addAttribute("content",
                Integer.toString(depth));
        Object o = book.getAttribute("pages", 0);   // total page number
        String str;
        if (o instanceof Integer) {
            str = Integer.toString((Integer) o);
        } else {
            str = "0";
        }
        head.addElement("meta").addAttribute("name", "dtb:totalPageCount").addAttribute("content", str);
        head.addElement("meta").addAttribute("name", "dtb:maxPageNumber").addAttribute("content", "0");
    }

    private ArrayList<String> getInfoLines(Book book, EpubConfig config) {
        ArrayList<String> lines = new ArrayList<String>();
        for (String name: INFO_ATTRIBUTE_NAMES) {
            Object o = book.getAttribute(name, null);
            if (o != null) {
                String str;
                if (o instanceof Date) {
                    str = DateUtils.formatDate((Date)o, config.dateFormat);
                } else if (name.equals("language")) {
                    str = o.toString().replace("_", "-");
                    str = Locale.forLanguageTag(str).getDisplayName();
                } else {
                    str = o.toString();
                }
                if (str.length() != 0) {
                    String line = I18N.getText("Epub.Page.Info." + StringUtils.toCapital(name), str);
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private void writeBookCoverPage(HtmlMaker htmlMaker, OpfBuilder opfBuilder, ZipOutputStream zipout, EpubConfig config)
            throws IOException {
        String href = config.textDir + "/" + EPUB.COVER_PAGE_FILE;
        Document page = htmlMaker.makeCoverPage(I18N.getText("Epub.Page.Cover.Title"), "../"+opfBuilder.getCover(),
                config.styleProvider.getBookCoverStyle());
        writeHtmlPage(page, zipout, href, config);
        opfBuilder.addManifestItem(EPUB.COVER_PAGE_ID, href, EPUB.MT_XHTML);
        opfBuilder.addSpineItem(EPUB.COVER_PAGE_ID, true, EPUB.DUOKAN_FULL_SCREEN);
        opfBuilder.addGuideItem(href, "cover", EPUB.MT_XHTML);
    }

    private void writeBookIntroPage(Element parent, HtmlMaker htmlMaker, Book book, OpfBuilder opfBuilder,
                                    ZipOutputStream zipout, EpubConfig config) throws IOException {
        String href = config.textDir + "/" + EPUB.INTRO_PAGE_FILE;
        TextObject intro = book.getIntro();
        if (intro == null) {
            return;
        }

        String pageTitle = I18N.getText("Epub.Page.Intro.Title");
        Document page = htmlMaker.makeIntroPage(pageTitle, book.getTitle(),
                config.styleProvider.getIntroTitleStyle(), intro, config.styleProvider.getIntroContentStyle());
        if (page == null) {
            return;
        }

        writeHtmlPage(page, zipout, href, config);
        opfBuilder.addManifestItem(EPUB.INTRO_PAGE_ID, href, EPUB.MT_XHTML);
        opfBuilder.addSpineItem(EPUB.INTRO_PAGE_ID, true, null);
        addNavPoint(parent, EPUB.INTRO_PAGE_ID, pageTitle, href);

    }

    private void writeBookInfoPage(Element parent, HtmlMaker htmlMaker, Book book, OpfBuilder opfBuilder,
                                   ZipOutputStream zipout, EpubConfig config) throws IOException {
        String href = config.textDir + "/" + EPUB.INFO_PAGE_FILE;
        List<String> lines = getInfoLines(book, config);
        if (lines.size() > 0) {
            String pageTitle = I18N.getText("Epub.Page.Info.Title");
            Document page = htmlMaker.makeInfoPage(pageTitle, config.styleProvider.getInfoTitleStyle(),
                    lines, config.styleProvider.getInfoContentStyle());
            writeHtmlPage(page, zipout, href, config);
            opfBuilder.addManifestItem(EPUB.INFO_PAGE_ID, href, EPUB.MT_XHTML);
            opfBuilder.addSpineItem(EPUB.INFO_PAGE_ID, true, null);
            opfBuilder.addGuideItem(href, "title-page", EPUB.MT_XHTML);
            addNavPoint(parent, EPUB.INFO_PAGE_ID, pageTitle, href);
        }
    }

    private void writePartCoverPage(HtmlMaker htmlMaker, Part part, String prefix, OpfBuilder opfBuilder,
                                    ZipOutputStream zipout, EpubConfig config) throws IOException {
        Object o = part.getAttribute(Book.COVER, null);
        if (! (o instanceof FileObject)) {
            return;
        }
        FileObject cover = (FileObject) o;

        // write cover image
        String href = config.imageDir + "/" + prefix + "." + FilenameUtils.getExtension(cover.getName());
        ZipUtils.writeFile(cover, zipout, EPUB.getOpsPath(href, config));
        String coverID = prefix+"-cover";
        opfBuilder.addManifestItem(coverID, href, cover.getMime());

        // write cover page
        String pageTitle = part.getTitle();
        String coverStyle = part.isSection() ? config.styleProvider.getSectionCoverStyle() :
                config.styleProvider.getChapterCoverStyle();
        Document page = htmlMaker.makeCoverPage(pageTitle, "../"+href, coverStyle);
        href = config.textDir + "/" + prefix + "-cover.xhtml";
        writeHtmlPage(page, zipout, href, config);

        String pageID = prefix+"-cover-page";
        opfBuilder.addManifestItem(pageID, href, EPUB.MT_XHTML);
        opfBuilder.addSpineItem(pageID, true, EPUB.DUOKAN_FULL_SCREEN);
    }

    private String writeSectionPage(Element parent, HtmlMaker htmlMaker, Part section, String prefix, String topHref,
                                    OpfBuilder opfBuilder, ZipOutputStream zipout, EpubConfig config) throws IOException {
        // cover
        writePartCoverPage(htmlMaker, section, prefix, opfBuilder, zipout, config);

        // items
        String base = prefix + ".xhtml";
        String href = config.textDir + "/" + base;
        String pageTitle = section.getTitle();

        opfBuilder.addManifestItem(prefix, href, EPUB.MT_XHTML);
        opfBuilder.addSpineItem(prefix, true, null);
        Element navPoint = addNavPoint(parent, prefix, pageTitle, href);

        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();

        int i = 1;
        for (Part sub: section) {
            String link;
            if (sub.isSection()) {
                link = writeSectionPage(navPoint, htmlMaker, sub, prefix+"-"+i, base, opfBuilder, zipout, config);
            } else {
                link = writeChapterPage(navPoint, htmlMaker, sub, prefix+"-"+i, opfBuilder, zipout, config);
            }
            links.add(link);
            titles.add(sub.getTitle());
            ++i;
        }

        if (topHref != null) {
            links.add(topHref);
            titles.add(I18N.getText("Epub.Page.Contents.GoToTop"));
        }

        TextObject intro = null;
        Object o = section.getAttribute(Book.INTRO, null);
        if (o instanceof TextObject) {
            intro = (TextObject) o;
        }

        Document page = htmlMaker.makeContentsPage(pageTitle, config.styleProvider.getSectionTitleStyle(),
                intro, config.styleProvider.getSectionIntroStyle(), titles, links,
                config.styleProvider.getTocItemsStyle());
        writeHtmlPage(page, zipout, href, config);

        return base;
    }

    private String writeChapterPage(Element parent, HtmlMaker htmlMaker, Part chapter, String prefix,
                                    OpfBuilder opfBuilder, ZipOutputStream zipout, EpubConfig config) throws IOException {
        // cover
        writePartCoverPage(htmlMaker, chapter, prefix, opfBuilder, zipout, config);

        // text
        String base = prefix + ".xhtml";
        String href = config.textDir + "/" + base;
        String pageTitle= chapter.getTitle();
        TextObject text = chapter.getSource();

        if (text.getType().equals(TextObject.HTML)) {       // content is HTML
            ZipUtils.writeText(text, zipout, href, text.getEncoding());
        } else {
            TextObject intro = null;
            Object o = chapter.getAttribute(Book.INTRO, null);
            if (o instanceof TextObject) {
                intro = (TextObject) o;
            }
            Document page = htmlMaker.makeTextPage(pageTitle, config.styleProvider.getChapterTitleStyle(), text,
                    config.styleProvider.getChapterTextStyle(), intro, config.styleProvider.getChapterIntroStyle());
            writeHtmlPage(page, zipout, href, config);
        }

        opfBuilder.addManifestItem(prefix, href, EPUB.MT_XHTML);
        opfBuilder.addSpineItem(prefix, true, null);
        addNavPoint(parent, prefix, pageTitle, href);

        return base;
    }

    @Override
    public Document make(Book book, String uuid, ZipOutputStream zipout, OpfBuilder opfBuilder, EpubConfig config)
            throws IOException {
        Document doc = DocumentHelper.createDocument();
        doc.addDocType(DT_TYPE, DT_ID, DT_URI);

        Element root = doc.addElement("ncx", NAMESPACE);
        root.addAttribute("version", VERSION);

        int depth = Jem.getDepth(book);

        // head
        addHead(root, uuid, book, depth);

        root.addElement("docTitle").addElement("text").setText(book.getTitle());
        if (!"".equals(book.getAuthor())) {
            root.addElement("docAuthor").addElement("text").setText(book.getAuthor());
        }

        Element navMap = root.addElement("navMap");

        // write CSS file
        String css = config.styleDir + "/" + EPUB.CSS_FILE;
        ZipUtils.writeFile(config.styleProvider.getCssFile(), zipout, EPUB.getOpsPath(css, config));
        opfBuilder.addManifestItem(EPUB.CSS_FILE_ID, css, EPUB.MT_CSS);

        // CSS link URL in HTML page
        String cssLink = "../" + css;
        HtmlMaker htmlMaker = new HtmlMaker(cssLink, config.htmlEncoding);

        // cover page
        writeBookCoverPage(htmlMaker, opfBuilder, zipout, config);

        // intro page
        writeBookIntroPage(navMap, htmlMaker, book, opfBuilder, zipout, config);

        // info page
        writeBookInfoPage(navMap, htmlMaker, book, opfBuilder, zipout, config);

        // toc page
        String tocHref = config.textDir + "/" + EPUB.TOC_PAGE_FILE;
        String tocTitle = I18N.getText("Epub.Page.Toc.Title");
        // main contents, only book depth > 1
        if (depth > 1) {
            addNavPoint(navMap, EPUB.TOC_PAGE_ID, tocTitle, tocHref);
            opfBuilder.addManifestItem(EPUB.TOC_PAGE_ID, tocHref, EPUB.MT_XHTML);
            opfBuilder.addSpineItem(EPUB.TOC_PAGE_ID, true, null);
            opfBuilder.addGuideItem(tocHref, "toc", EPUB.TOC_PAGE_ID);
        }

        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();

        int i = 1;
        for (Part sub: book) {
            String href;
            if (sub.isSection()) {
                String topHref = depth > 1 ? EPUB.TOC_PAGE_FILE : null;
                href = writeSectionPage(navMap, htmlMaker, sub, "chapter-"+i, topHref, opfBuilder, zipout, config);
            } else {
                href = writeChapterPage(navMap, htmlMaker, sub, "chapter-"+i, opfBuilder, zipout, config);
            }
            links.add(href);
            titles.add(sub.getTitle());
            ++i;
        }

        if (depth > 1) {
            Document page = htmlMaker.makeContentsPage(tocTitle, config.styleProvider.getTocTitleStyle(), null, null,
                    titles, links, config.styleProvider.getTocItemsStyle());
            writeHtmlPage(page, zipout, tocHref, config);
        }
        return doc;
    }

    private Element addNavPoint(Element parent, String id, String label, String href) {
        Element nvp = parent.addElement("navPoint");
        nvp.addAttribute("id", id).addAttribute("playOrder", Integer.toString(this.playOrder++));
        nvp.addElement("navLabel").addElement("text").setText(label);
        nvp.addElement("content").addAttribute("src", href);
        return nvp;
    }
}
