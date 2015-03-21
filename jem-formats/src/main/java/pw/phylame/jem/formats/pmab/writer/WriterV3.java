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

package pw.phylame.jem.formats.pmab.writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.formats.pmab.Pmab;
import pw.phylame.jem.formats.pmab.PmabConfig;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.StringUtils;
import pw.phylame.tools.TextObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Date;
import java.util.zip.ZipOutputStream;

/**
 * PBM and PBC writer for PMAB 3.x.
 */
public final class WriterV3 {
    private static Log LOG = LogFactory.getLog(WriterV3.class);

    private static void makeItem(Element parent, String name, Object value, ZipOutputStream zipOut,
                                 PmabConfig config) {
        Element item = parent.addElement("item").addAttribute("name", name);
        String type = Jem.variantType(value);
        String text;
        if (type.equals("file")) {
            pw.phylame.tools.file.FileObject fb = (pw.phylame.tools.file.FileObject) value;
            type = fb.getMime();
            File file = new File(fb.getName());
            if (name.equals("cover")) {
                text = config.imageDir + "/" + file.getName();
            } else {
                text = config.extraDir + "/" + file.getName();
            }
            try {
                Pmab.writeFile(fb, zipOut, text);
            } catch (IOException ex) {
                LOG.debug("cannot write file to PMAB: "+fb.getName(), ex);
            }
        } else if (type.equals("text")) {
            TextObject tb = (TextObject) value;
            String encoding = config.textEncoding != null ? config.textEncoding :
                    System.getProperty("file.encoding");
            type = "text/plain;encoding=" + encoding;
            if (name.equals("intro")) {
                text = config.textDir + "/" + tb.hashCode() + ".txt";
            } else {
                text = config.textDir + "/" + tb.hashCode() + ".txt";
            }
            try {
                Pmab.writeText(tb, zipOut, text, encoding);
            } catch (IOException ex) {
                LOG.debug("cannot write text to PMAB: "+text, ex);
            }
        } else if (type.equals("datetime")) {
            type += ";format=" + config.dateFormat;
            text = DateUtils.formatDate((Date) value, config.dateFormat);
        } else if (type.equals("bytes")) {
            text = StringUtils.join((Byte[])value, " ");
        } else {
            text = String.valueOf(value);
        }
        item.addAttribute("type", type);
        if (! "".equals(text)) {
            item.setText(text);
        }
    }

    private static void makeHead(Element parent, Map<String, String> metaInfo) {
        Element head = parent.addElement("head");
        for (String name: metaInfo.keySet()) {
            String value = metaInfo.get(name);
            if (value != null) {
                head.addElement("meta").addAttribute("name", name).addAttribute("value", value);
            }
        }
    }

    private static void makeAttributes(Element parent, Part part, ZipOutputStream zipOut, PmabConfig config)
            throws IOException {
        Element attributes = parent.addElement("attributes");
        for (String name: part.attributeNames()) {
            makeItem(attributes, name, part.getAttribute(name, null), zipOut, config);
        }
    }

    public static void writePBM(Book book, Document doc, ZipOutputStream zipOut, PmabConfig config)
            throws IOException {
        doc.addDocType("pbm", null, null);
        Element root = doc.addElement("pbm", Pmab.PBM_XML_NS).addAttribute("version", "3.0");
        // head
        if (config.metaInfo != null && config.metaInfo.size() > 0) {
            makeHead(root, config.metaInfo);
        }
        // attributes
        makeAttributes(root, book, zipOut, config);
        // extensions
        Element extensions = root.addElement("extensions");
        for (String name: book.itemNames()) {
            makeItem(extensions, name, book.getItem(name, null), zipOut, config);
        }
    }

    private static void makeChapter(Element parent, Part part, ZipOutputStream zipOut, PmabConfig config,
                                    String suffix) throws IOException {
        Element item = parent.addElement("chapter");
        makeAttributes(item, part, zipOut, config);
        // TODO  content
        String href = config.textDir + "/chapter-"+suffix+".txt";
        String encoding = config.textEncoding != null ? config.textEncoding :
                System.getProperty("file.encoding");
        Pmab.writeText(part.getSource(), zipOut, href, encoding);
        Element content = item.addElement("content");
        content.addAttribute("type", "text/plain;encoding="+encoding);
        content.setText(href);

        int ix = 1;
        for (Part sub: part) {
            makeChapter(item, sub, zipOut, config, suffix+"-"+ix++);
        }
    }

    public static void writePBC(Book book, Document doc, ZipOutputStream zipOut, PmabConfig config)
            throws IOException {
        doc.addDocType("pbc", null, null);
        Element root = doc.addElement("pbc", Pmab.PBC_XML_NS).addAttribute("version", "3.0");

        Element toc = root.addElement("toc");
        int ix = 1;
        for (Part part: book) {
            makeChapter(toc, part, zipOut, config, ix+++"");
        }
    }
}
