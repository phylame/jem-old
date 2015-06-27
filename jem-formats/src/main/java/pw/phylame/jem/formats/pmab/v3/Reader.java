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

import org.dom4j.Element;

import java.util.Map;
import java.util.Iterator;
import java.util.zip.ZipFile;

import java.io.IOException;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.ParserException;

import pw.phylame.tools.DateUtils;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;

/**
 * PBM and PBC reader for PMAB 3.x.
 */
public class Reader {
    private static Log LOG = LogFactory.getLog(Reader.class);

    private static byte[] toBytes(String[] num) {
        byte[] bytes = new byte[num.length];
        int ix = 0;
        for (String s: num) {
            try {
                bytes[ix++] = Byte.parseByte(s);
            } catch (NumberFormatException ex) {
                LOG.debug("invalid 'bytes' data", ex);
                return null;
            }
        }
        return bytes;
    }

    private static Map<String, String> toMap(String[] parts, int offset) {
        Map<String, String> map = new java.util.HashMap<String, String>();
        for (int ix = offset; ix < parts.length; ++ix) {
            String[] nv = parts[ix].split("=");
            if (nv.length != 2) {
                LOG.debug("invalid key-value: "+parts[ix]);
                continue;
            }
            map.put(nv[0], nv[1]);
        }
        return map;
    }

    private static Object prepareFile(FileObject fb, String[] parts) {
        String mime = parts[0];
        Object ret;
        Map<String, String> prop = toMap(parts, 1);
        if (mime.startsWith("text/")) {     // to TextObject
            String encoding = prop.get("encoding");
            if (encoding != null && encoding.equals("")) {
                encoding = null;
            }
            ret = new TextObject(fb, encoding);
        } else {
            ret = fb;
        }
        return ret;
    }

    private static Object parseItem(String type, String data, ZipFile zipFile) {
        Object value = null;
        if (type == null || "str".equals(type)) {
            value = data;
        } else if ("int".equals(type) || "uint".equals(type)) {
            try {
                value = Integer.parseInt(data);
            } catch (NumberFormatException ex) {
                LOG.debug("invalid int data: "+data, ex);
            }
        } else if (type.startsWith("datetime") || type.startsWith("date") ||
                type.startsWith("time")) {
            String[] parts = type.split(";");
            String format = null;
            if (parts.length > 1) {
                Map<String, String> prop = toMap(parts, 1);
                format = prop.get("format");
            }
            if (format == null || "".equals(format)) {
                if (parts[0].equals("date")) {
                    format = "yyyy-M-d";
                } else if (parts[0].equals("time")) {
                    format = "H:m:S";
                } else {
                    format = "yyyy-M-d H:m:S";
                }
            }
            value = DateUtils.parseDate(data, format, null);
        } else if ("bool".equals(type)) {
            value = Boolean.parseBoolean(data);
        } else if ("real".equals(type)) {
            try {
                value = Double.parseDouble(data);
            } catch (NumberFormatException ex) {
                LOG.debug("invalid real data: "+data, ex);
            }
        } else if (type.startsWith("bytes")) {
            String[] parts = type.split(";");
            String sep = null;
            if (parts.length > 1) {
                Map<String, String> prop = toMap(parts, 1);
                sep = prop.get("sep");
            }
            if (sep == null || "".equals(sep)) {
                sep = " ";
            }
            value = toBytes(data.split(sep));
        } else {        // file
            String[] parts = type.split(";");
            try {
                FileObject fb = FileFactory.fromZip(zipFile, data, parts[0]);   // 0 is MIME
                value = prepareFile(fb, parts);
            } catch (IOException e) {
                LOG.debug("not found file: "+data, e);
            }
        }
        return value;
    }

    private static void readAttributes(Element parent, Part part, ZipFile zipFile)
            throws JemException {
        Element attrs = parent.element("attributes");
        if (attrs == null) {
            throw new ParserException("Not found 'attributes' in PBM", "pmab");
        }

        for (Iterator it = attrs.elementIterator(); it.hasNext();) {
            Element item = (Element) it.next();
            if (! "item".equals(item.getName())) {
                LOG.debug("PBM: ignore unexpected element: "+item.getName());
                continue;
            }
            String name = item.attributeValue("name");
            if (name == null || "".equals(name)) {
                LOG.debug("PBM: 'item' element without 'name' or empty 'name'");
                continue;
            }
            String type = item.attributeValue("type");
            Object data = parseItem(type, item.getText(), zipFile);
            if (data != null) {
                part.setAttribute(name, data);
            }
        }
    }

    private static void readExtensions(Element parent, Book book, ZipFile zipFile)
            throws JemException {
        Element ext = parent.element("extensions");
        if (ext == null) {
            LOG.debug("no 'extensions' found in PBM");
            return;
        }

        for (Iterator it = ext.elementIterator(); it.hasNext();) {
            Element item = (Element) it.next();
            if (! "item".equals(item.getName())) {
                LOG.debug("PBM: ignore unexpected element: "+item.getName());
                continue;
            }
            String name = item.attributeValue("name");
            if (name == null || "".equals(name)) {
                LOG.debug("PBM: 'item' element without 'name' or empty 'name'");
                continue;
            }
            String type = item.attributeValue("type");
            Object data = parseItem(type, item.getText(), zipFile);
            if (data != null) {
                book.setItem(name, data);
            }
        }
    }

    public static void readPBM(Element root, Book book, ZipFile zipFile)
            throws JemException {
        // ignore <head/>
        readAttributes(root, book, zipFile);
        readExtensions(root, book, zipFile);
    }

    private static void readChapter(Element parent, Part owner, ZipFile zipFile)
            throws JemException {
        Chapter chapter = new Chapter();
        readAttributes(parent, chapter, zipFile);
        Element content = parent.element("content");
        if (content != null) {
            String type = content.attributeValue("type");
            if (type == null || type.equals("str")) {
                chapter.getSource().setRaw(content.getText());
            } else {
                String[] parts = type.split(";");
                String mime = parts[0], encoding = null;
                if (parts.length > 1) {
                    Map<String, String> prop = toMap(parts, 1);
                    encoding = prop.get("encoding");
                    if (encoding != null && "".equals(encoding)) {
                        encoding = null;
                    }
                }
                try {
                    FileObject fb = FileFactory.fromZip(zipFile, content.getText(), mime);
                    chapter.getSource().setFile(fb, encoding);
                } catch (IOException e) {
                    LOG.debug("cannot load text content: "+content.getText(), e);
                }
            }
        }

        for (Iterator it = parent.elementIterator("chapter"); it.hasNext(); ) {
            readChapter((Element) it.next(), chapter, zipFile);
        }

        owner.append(chapter);
    }

    public static void readPBC(Element root, Book book, ZipFile zipFile)
            throws JemException {
        Element toc = root.element("toc");
        if (toc == null) {
            throw new ParserException("Not found 'toc' in PBC", "pmab");
        }
        for (Iterator it = toc.elementIterator("chapter"); it.hasNext(); ) {
            readChapter((Element) it.next(), book, zipFile);
        }
    }
}
