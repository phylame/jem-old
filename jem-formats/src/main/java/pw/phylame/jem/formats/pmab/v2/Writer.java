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

package pw.phylame.jem.formats.pmab.v2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Element;
import org.dom4j.Document;

import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.formats.pmab.PMAB;
import pw.phylame.jem.formats.pmab.PmabConfig;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;
import pw.phylame.tools.file.FileNameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Date;
import java.util.zip.ZipOutputStream;

/**
 * PBM and PBC writer for PMAB 2.x.
 */
public class Writer {
    private static Log LOG = LogFactory.getLog(Writer.class);

    private static void makeHead(Element parent, Map<Object, Object> metaInfo) {
        Element head = parent.addElement("head");
        for (Object key: metaInfo.keySet()) {
            Object value = metaInfo.get(key);
            if (value != null) {
                head.addElement("meta").addAttribute("name", String.valueOf(key)).addAttribute("value",
                        String.valueOf(value));
            }
        }
    }

    private static void makeMetadata(Element parent, Book book, ZipOutputStream zipout, PmabConfig config) {
        Element md = parent.addElement("metadata");
        int count = 0;
        for (String name: book.attributeNames()) {
            Object value = book.getAttribute(name, null);
            if (value == null) {
                continue;
            }
            if (value instanceof FileObject) {
                FileObject fb = (FileObject) value;
                String mime = fb.getMime(), href;
                File file = new File(fb.getName());
                if (mime.startsWith("image/")) {        // image file
                    href = config.imageDir + "/" + file.getName();
                } else {
                    href = config.extraDir + "/" + file.getName();
                }
                try {
                    ZipUtils.writeFile(fb, zipout, href);
                } catch (IOException ex) {
                    LOG.debug("cannot write file to PMAB: "+fb.getName(), ex);
                    continue;
                }
                Element item = md.addElement("attr").addAttribute("name", name).addAttribute("media-type", mime);
                item.setText(href);
            } else if (value instanceof TextObject) {
                TextObject tb = (TextObject) value;
                try {
                    md.addElement("attr").addAttribute("name", name).setText(tb.getText());
                } catch (IOException e) {
                    LOG.debug("cannot write text to PMAB: "+name, e);
                    continue;
                }
            } else if (value instanceof Date) {
                String text = DateUtils.formatDate((Date) value, "yyyy-M-d H:m:s");
                md.addElement("attr").addAttribute("name", name).setText(text);
            } else {
                md.addElement("attr").addAttribute("name", name).setText(String.valueOf(value));
            }
            ++count;
        }
        md.addAttribute("count", String.valueOf(count));
    }

    private static void makeExtension(Element parent, Book book, ZipOutputStream zipout, PmabConfig config) {
        Element ext = parent.addElement("extension");
        ext.addComment("The following data will be added to PMAB.");
        int count = 0;
        for (String name: book.itemNames()) {
            Object value = book.getItem(name, null);
            String type = "text", text = null;
            Element item = ext.addElement("item").addAttribute("name", name);
            if (value instanceof FileObject) {
                FileObject fb = (FileObject) value;
                String href = config.extraDir + "/" + new File(fb.getName()).getName();
                try {
                    ZipUtils.writeFile(fb, zipout, href);
                } catch (IOException ex) {
                    LOG.debug("cannot write file to PMAB: "+fb.getName(), ex);
                    continue;
                }
                type = "file";
                Element obj = item.addElement("object").addAttribute("href", href);
                obj.addAttribute("media-type", fb.getMime());
            } else if (value instanceof TextObject) {
                TextObject tb = (TextObject) value;
                String encoding = config.textEncoding != null ? config.textEncoding : System.getProperty("file.encoding");
                String href = config.extraDir + "/" + tb.hashCode() + ".txt";
                try {
                    ZipUtils.writeText(tb, zipout, href, encoding);
                } catch (IOException ex) {
                    LOG.debug("cannot write text to PMAB: "+href, ex);
                    continue;
                }
                type = "file";
                item.addElement("object").addAttribute("href", href).addAttribute("media-type", "text/plain");
            } else if (value instanceof Integer) {
                type = "number";
                text = String.valueOf(value);
            } else if (value instanceof Date) {
                text = DateUtils.formatDate((Date) value, "yyyy-M-d H:m:s");
            } else if (value != null) {
                text = String.valueOf(value);
            }
            item.addAttribute("type", type);
            if (text != null) {
                item.addAttribute("value", text);
            }
            ++count;
        }
        ext.addAttribute("count", String.valueOf(count));
    }

    public static void writePBM(Book book, Document doc, ZipOutputStream zipout, PmabConfig config) {
        doc.addDocType("pbm", null, null);
        Element root = doc.addElement("pbm", PMAB.PBM_XML_NS).addAttribute("version", "2.0");
        // head
        if (config.metaInfo != null && config.metaInfo.size() > 0) {
            makeHead(root, config.metaInfo);
        }
        // metadata
        makeMetadata(root, book, zipout, config);
        // extension
        makeExtension(root, book, zipout, config);
    }

    private static void makeChapter(Element parent, Part part, ZipOutputStream zipout, PmabConfig config, String suffix) {
        Element elem = parent.addElement("chapter");
        elem.addElement("title").setText(part.getTitle());
        String base = "chapter-" + suffix;
        String encoding = config.textEncoding != null ? config.textEncoding : System.getProperty("file.encoding");
        // cover
        Object o = part.getAttribute(Chapter.COVER, null);
        if (o instanceof FileObject) {
            FileObject fb = (FileObject) o;
            String href = config.imageDir + "/" + base + "-cover." + FileNameUtils.extensionName(fb.getName());
            try {
                ZipUtils.writeFile(fb, zipout, href);
                elem.addElement("cover").addAttribute("href", href).addAttribute("media-type", fb.getMime());
            } catch (IOException ex) {
                LOG.debug("cannot write file to PMAB: "+fb.getName(), ex);
            }
        }
        // intro
        o = part.getAttribute(Chapter.INTRO, null);
        if (o instanceof TextObject) {
            TextObject tb = (TextObject) o;
            String href = config.textDir + "/" + base + "-intro.txt";
            try {
                ZipUtils.writeText(tb, zipout, href, encoding);
                elem.addElement("intro").addAttribute("href", href).addAttribute("encoding", encoding);
            } catch (IOException ex) {
                LOG.debug("cannot write text to PMAB: "+href, ex);
            }
        }
        if (! part.isSection()) {
            TextObject tb = part.getSource();
            String href = config.textDir + "/" + base + ".txt";
            try {
                ZipUtils.writeText(tb, zipout, href, encoding);
                elem.addAttribute("href", href).addAttribute("encoding", encoding);
            } catch (IOException ex) {
                LOG.debug("cannot write text to PMAB: "+href, ex);
            }
        } else {
            int count = 0;
            for (Part sub: part) {
                makeChapter(elem, sub, zipout, config, suffix + "-" + (++count));
            }
            elem.addAttribute("count", String.valueOf(count));
        }
    }

    public static void writePBC(Book book, Document doc, ZipOutputStream zipout, PmabConfig config) {
        doc.addDocType("pbc", null, null);
        Element root = doc.addElement("pbc", PMAB.PBC_XML_NS).addAttribute("version", "2.0");
        Element contents = root.addElement("contents");
        int count = 0;
        for (Part sub: book) {
            makeChapter(contents, sub, zipout, config, ++count + "");
        }
        contents.addAttribute("count", String.valueOf(count));
        contents.addAttribute("depth", String.valueOf(Jem.getDepth(book)));
    }
}
