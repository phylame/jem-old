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

package pw.phylame.jem.formats.epub.html;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.tools.TextObject;

import java.util.List;
import java.util.Locale;
import java.io.IOException;

/**
 * Makes ePub HTML documents.
 */
public final class HtmlMaker {
    public static final String DT_ID = "-//W3C//DTD XHTML 1.1//EN";
    public static final String DT_URI = "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";
    public static final String NAMESPACE = "http://www.w3.org/1999/xhtml";

    private String css;
    private String encoding;

    public HtmlMaker(String css, String encoding) {
        this.css = css;
        this.encoding = encoding;
    }

    public Document makeCoverPage(String title, String cover, String imageStyle) {
        Document doc = DocumentHelper.createDocument();
        Element body = initHtml(doc, title);

        Element div = body.addElement("div").addAttribute("class", imageStyle);
        div.addElement("img").addAttribute("src", cover);

        return doc;
    }

    public Document makeIntroPage(String title, String bookTitle, String titleStyle, TextObject intro,
                                  String introStyle) throws IOException {
        List<String> lines = intro.getLines();
        if (lines.size() == 0) {
            return null;
        }

        Document doc = DocumentHelper.createDocument();
        Element body = initHtml(doc, title);

        // book title
        Element div = body.addElement("div").addAttribute("class", titleStyle);
        div.addElement("h1").setText(bookTitle);

        // intro content
        div = body.addElement("div").addAttribute("class", introStyle);
        div.addElement("h3").setText(title);

        for (String line: lines) {
            div.addElement("p").setText(line.trim());
        }

        return doc;
    }

    public Document makeInfoPage(String title, String titleStyle, String[] lines, String infoStyle) {
        Document doc = DocumentHelper.createDocument();
        Element body = initHtml(doc, title);

        Element div = body.addElement("div").addAttribute("class", titleStyle);
        div.addElement("h3").setText(title);

        div = body.addElement("div").addAttribute("class", infoStyle);
        for (String line: lines) {
            div.addElement("p").setText(line);
        }

        return doc;
    }

    public Document makeContentsPage(String title, String titleStyle, TextObject intro, String introStyle,
                                     String[] labels, String[] href, String tocStyle) throws IOException {
        Document doc = DocumentHelper.createDocument();
        Element body = initHtml(doc, title);

        // title
        Element div = body.addElement("div").addAttribute("class", titleStyle);
        div.addElement("h3").setText(title);

        // intro, optional
        if (intro != null) {
            div = body.addElement("div").addAttribute("class", introStyle);
            for (String line: intro.getLines()) {
                div.addElement("p").setText(line.trim());
            }
        }

        // contents
        div = body.addElement("div").addAttribute("class", tocStyle);
        for (int i = 0; i < labels.length; ++i) {
            div.addElement("p").addElement("a").addAttribute("href", href[i]).setText(labels[i]);
        }

        return doc;
    }

    public Document makeTextPage(String title, String titleStyle, TextObject text, String textStyle, TextObject intro,
                                 String introStyle)
            throws IOException {
        Document doc = DocumentHelper.createDocument();
        Element body = initHtml(doc, title);

        // title
        Element div = body.addElement("div").addAttribute("class", titleStyle);
        div.addElement("h3").setText(title);

        // intro, optional
        if (intro != null) {
            div = body.addElement("div").addAttribute("class", introStyle);
            for (String line: intro.getLines()) {
                div.addElement("p").setText(line.trim());
            }
        }

        // content
        div = body.addElement("div").addAttribute("class", textStyle);
        for (String line: text.getLines()) {
            div.addElement("p").setText(line.trim());
        }

        return doc;
    }

    public Element initHtml(Document doc, String title) {
        doc.addDocType("html", DT_ID, DT_URI);
        Element html = doc.addElement("html", NAMESPACE);
        html.addAttribute("xml:lang", Locale.getDefault().toLanguageTag());

        Element head = html.addElement("head");

        Element meta = head.addElement("meta");
        meta.addAttribute("http-equiv", "charset").addAttribute("content", encoding);

        // CSS
        Element link = head.addElement("link").addAttribute("rel", "stylesheet");
        link.addAttribute("type", EPUB.MT_CSS).addAttribute("href", css);

        // title
        head.addElement("title").setText(title);

        return html.addElement("body");
    }
}
