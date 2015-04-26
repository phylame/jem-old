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

package pw.phylame.jem.formats.pmab.v3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.formats.pmab.PMAB;
import pw.phylame.jem.formats.pmab.PmabConfig;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileNameUtils;
import pw.phylame.tools.file.FileObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.zip.ZipOutputStream;

/**
 * PBM and PBC writer for PMAB 3.x.
 */
public final class Writer {
    private static Log LOG = LogFactory.getLog(Writer.class);
    private static Set<String> IgnoredNames = new HashSet<String>();
    static {
        IgnoredNames.addAll(java.util.Arrays.asList("source_file", "source_format"));
    }

    private static Byte[] toBytes(byte[] ary) {
        Byte[] results = new Byte[ary.length];
        for (int ix = 0; ix < ary.length; ++ix) {
            results[ix] = ary[ix];
        }
        return results;
    }

    private static void makeItem(Element parent, String name, Object value, ZipOutputStream zipout,
                                 PmabConfig config, String prefix) {
        if (value == null) {    // ignore null value
            return;
        }
        Element item = parent.addElement("item").addAttribute("name", name);
        String text, type;
        if (value instanceof FileObject) {
            FileObject fb = (FileObject) value;
            type = fb.getMime();
            String baseName = prefix + name + "." + FileNameUtils.extensionName(fb.getName());
            if (type.startsWith("image/")) {        // image file
                text = config.imageDir + "/" + baseName;
            } else {
                text = config.extraDir + "/" + baseName;
            }
            try {
                PMAB.writeFile(fb, zipout, text);
            } catch (IOException ex) {
                LOG.debug("cannot write file to PMAB: "+fb.getName(), ex);
            }
        } else if (value instanceof TextObject) {
            TextObject tb = (TextObject) value;
            String encoding = config.textEncoding != null ? config.textEncoding : System.getProperty("file.encoding");
            type = "text/plain;encoding=" + encoding;
            String baseName = prefix + name + ".txt";
            if (name.equals("intro")) {
                text = config.textDir + "/" + baseName;
            } else {
                text = config.extraDir + "/" + baseName;
            }
            try {
                PMAB.writeText(tb, zipout, text, encoding);
            } catch (IOException ex) {
                LOG.debug("cannot write text to PMAB: "+text, ex);
            }
        } else if (value instanceof Date) {
            type = "datetime;format=" + config.dateFormat;
            text = DateUtils.formatDate((Date) value, config.dateFormat);
        } else if (value instanceof byte[]) {
            type = "bytes;sep=, ";
            text = java.util.Arrays.toString((byte[]) value);
        } else {
            type = "str";
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

    private static void makeAttributes(Element parent, Part part, ZipOutputStream zipout, PmabConfig config,
                                       String prefix)
            throws IOException {
        Element attributes = parent.addElement("attributes");
        for (String name: part.attributeNames()) {
            if (IgnoredNames.contains(name)) {
                continue;
            }
            makeItem(attributes, name, part.getAttribute(name, null), zipout, config, prefix);
        }
    }

    public static void writePBM(Book book, Document doc, ZipOutputStream zipout, PmabConfig config)
            throws IOException {
        doc.addDocType("pbm", null, null);
        Element root = doc.addElement("pbm", PMAB.PBM_XML_NS).addAttribute("version", "3.0");
        // head
        if (config.metaInfo != null && config.metaInfo.size() > 0) {
            makeHead(root, config.metaInfo);
        }
        // attributes
        makeAttributes(root, book, zipout, config, "");
        // extensions
        Element extensions = root.addElement("extensions");
        for (String name: book.itemNames()) {
            makeItem(extensions, name, book.getItem(name, null), zipout, config, "ext-");
        }
    }

    private static void makeChapter(Element parent, Part part, ZipOutputStream zipout, PmabConfig config,
                                    String suffix) throws IOException {
        Element item = parent.addElement("chapter");
        String base = "chapter-" + suffix;
        makeAttributes(item, part, zipout, config, base+"-");
        // TODO  content
        String href = config.textDir + "/"+ base + ".txt";
        String encoding = config.textEncoding != null ? config.textEncoding : System.getProperty("file.encoding");
        PMAB.writeText(part.getSource(), zipout, href, encoding);
        Element content = item.addElement("content");
        content.addAttribute("type", "text/plain;encoding="+encoding);
        content.setText(href);

        int count = 1;
        for (Part sub: part) {
            makeChapter(item, sub, zipout, config, suffix + "-" + count);
            ++count;
        }
    }

    public static void writePBC(Book book, Document doc, ZipOutputStream zipout, PmabConfig config)
            throws IOException {
        doc.addDocType("pbc", null, null);
        Element root = doc.addElement("pbc", PMAB.PBC_XML_NS).addAttribute("version", "3.0");

        Element toc = root.addElement("toc");
        int count = 1;
        for (Part part: book) {
            makeChapter(toc, part, zipout, config, count + "");
            ++count;
        }
    }
}
