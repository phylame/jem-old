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

package pw.phylame.jem.formats.pmab.reader;

import org.dom4j.Element;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipFile;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Part;
import pw.phylame.jem.util.JemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.tools.DateUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileObject;

/**
 * PBM and PBC reader for PMAB 2.x.
 */
public class ReaderV2 {
    private static Log LOG = LogFactory.getLog(ReaderV2.class);

    private static int parseNumber(String value, int def) {
        if (value == null) {
            return def;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            LOG.debug("invalid number: "+value, ex);
            return def;
        }
    }

    private static void readMetadata(Element root, Book book, ZipFile zipFile) throws JemException {
        Element md = root.element("metadata");
        if (md == null) {
            throw new JemException("Not found 'metadata' in PBM");
        }
        for (Iterator it = md.elementIterator(); it.hasNext(); ) {
            Element attr = (Element) it.next();
            if (! "attr".equals(attr.getName())) {
                LOG.debug("ignore unexpected element '"+attr.getName()+"' in PBM metadata");
                continue;
            }
            String name = attr.attributeValue("name");
            if (name == null) {
                LOG.debug("ignore 'attr' element without 'name' attribute in PBM metadata");
                continue;
            }
            String value = attr.getText().trim();
            if (name.equals("cover")) {
                String mime = attr.attributeValue("media-type");
                try {
                    book.setCover(FileFactory.getFile(zipFile, value, mime));
                } catch (IOException e) {
                    LOG.debug("not found cover source: "+value, e);
                }
            } else if (name.equals("date") || name.equals("datetime")) {
                book.setDate(DateUtils.parseDate(value, "yyyy-M-d H:m:s", new Date()));
            } else if (name.equals("intro")) {
                book.setIntro(value);
            } else {
                book.setAttribute(name, value);
            }
        }
    }

    private static void readExtension(Element root, Book book, ZipFile zipFile) {
        Element ext = root.element("extension");
        if (ext == null) {
            return;
        }
        int count = parseNumber(ext.attributeValue("count"), -1);
        for (Iterator it = ext.elementIterator(); it.hasNext(); ) {
            if (count >= 0 && book.itemSize() >= count) {
                break;
            }
            Element item = (Element) it.next();
            if (! "item".equals(item.getName())) {
                LOG.debug("ignore unexpected element '"+item.getName()+"' in PBM extension");
                continue;
            }
            String name = item.attributeValue("name");
            if (name == null) {
                LOG.debug("ignore 'item' element without 'name' attribute in PBM extension");
                continue;
            }
            String type = item.attributeValue("type");
            if (type == null) {
                type = "text";      // as text
            }
            String value = item.attributeValue("value");
            if (type.equals("file")) {
                Element obj = item.element("object");
                if (obj == null) {
                    LOG.debug("not found 'object' element when item type is file");
                    continue;
                }
                String href = obj.attributeValue("href");
                if (href == null) {
                    LOG.debug("ignore 'object' element without 'href' attribute in PBM extension");
                    continue;
                }
                String mt = obj.attributeValue("media-type");
                if (mt == null) {
                    LOG.debug("not found 'media-type' attribute of 'object' element in PBM extension");
                }
                try {
                    book.setItem(name, FileFactory.getFile(zipFile, href, mt));
                } catch (IOException e) {
                    LOG.debug("not found file source: "+href+" in PBM extension");
                }
            } else if (type.equals("number")) {
                if (value != null) {
                    try {
                        book.setItem(name, Integer.parseInt(value));
                    } catch (NumberFormatException ex) {
                        LOG.debug("invalid number: "+value, ex);
                    }
                }
            } else {
                book.setItem(name, value);
            }
        }
    }

    public static void readPBM(Element root, Book book, ZipFile zipFile) throws JemException {
        readMetadata(root, book, zipFile);
        readExtension(root, book, zipFile);
    }

    private static void readContents(Element elem, Part parent, ZipFile zipFile) throws JemException {
        Element title = elem.element("title");
        if (title == null) {
            throw new JemException("Not found 'title' element in PBC contents");
        }
        Chapter chapter = new Chapter(title.getText().trim(), "");
        parent.append(chapter);

        int count = parseNumber(elem.attributeValue("count"), -1);

        String href = elem.attributeValue("href");
        String encoding = elem.attributeValue("encoding");
        if (href != null) { // base chapter
            try {
                FileObject fb = FileFactory.getFile(zipFile, href, null);
                chapter.getSource().setFile(fb, encoding);
            } catch (IOException e) {
                LOG.debug("not found text source: "+href+" in PBC", e);
            }
        }

        for (Iterator it = elem.elementIterator(); it.hasNext(); ) {
            if (count >= 0 && chapter.size() >= count) {
                break;
            }
            Element item = (Element) it.next();
            String tag = item.getName();
            if (tag.equals("chapter")) {
                readContents(item, chapter, zipFile);
            } else if (tag.equals("cover")) {
                href = item.attributeValue("href");
                if (href == null) {
                    LOG.debug("ignore 'cover' element without 'href' attribute in PBC");
                    continue;
                }
                String mt = item.attributeValue("media-type");
                if (mt == null) {
                    LOG.debug("found 'cover' element without 'media-type' in PBC");
                }
                try {
                    chapter.setCover(FileFactory.getFile(zipFile, href, mt));
                } catch (IOException e) {
                    LOG.debug("not found cover source: "+href+"in PBC", e);
                }
            } else if (tag.equals("intro")) {
                href = item.attributeValue("href");
                if (href == null) {
                    LOG.debug("ignore 'intro' element without 'href' attribute in PBC");
                    continue;
                }
                encoding = item.attributeValue("encoding");
                if (encoding == null) {
                    LOG.debug("found 'intro' element without 'encoding' in PBC");
                }

                try {
                    FileObject fb = FileFactory.getFile(zipFile, href, null);
                    chapter.setIntro(new TextObject(fb, encoding));
                } catch (IOException e) {
                    LOG.debug("not found intro source: "+href, e);
                }
            }
        }
    }

    public static void readPBC(Element root, Book book, ZipFile zipFile) throws JemException {
        Element contents = root.element("contents");
        if (contents == null) {
            throw new JemException("Not found 'contents' element in PBC");
        }
        int count = parseNumber(contents.attributeValue("count"), -1);
        for (Iterator it = contents.elementIterator(); it.hasNext(); ) {
            if (count >= 0 && book.size() >= count) {
                break;
            }
            readContents((Element) it.next(), book, zipFile);
        }
    }
}
