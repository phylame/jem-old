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

package pw.phylame.jem.formats.pmab.v1;

import org.dom4j.Element;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipFile;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.JemException;

import pw.phylame.tools.DateUtils;
import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.tools.file.FileObject;

/**
 * PBM and PBC reader for PMAB 1.x.
 */
public class Reader {
    private static Log LOG = LogFactory.getLog(Reader.class);

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
            throw new JemException("Not found 'title' element in PBC contents");
        }
        for (Iterator it = md.elementIterator(); it.hasNext(); ) {
            Element item = (Element) it.next();
            String name = item.getName();
            String value = item.getText().trim();
            if (name.equals("cover")) {
                try {
                    book.setCover(FileFactory.fromZip(zipFile, value, null));
                } catch (IOException e) {
                    LOG.debug("not found cover source: "+value, e);
                }
            } else if (name.equals("date") || name.equals("datetime")) {
                Date date = DateUtils.parseDate(value, "yyyy-M-d H:m:s", null);
                if (date != null) {
                    book.setDate(date);
                } else {
                    LOG.debug("invalid date format: "+value);
                }
            } else if (name.equals("intro") || name.equals("description")) {
                book.setIntro(value);
            } else {
                book.setAttribute(name, value);
            }
        }
    }

    private static void readExtension(Element root, Book book, ZipFile zipFile) throws JemException {
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
                LOG.debug("ignore 'item' element without 'type' attribute in PBM extension");
                continue;
            }
            String value = item.attributeValue("value");
            if (value == null) {
                LOG.debug("ignore 'item' element without 'value' attribute in PBM extension");
                continue;
            }
            if (type.equals("file")) {
                try {
                    book.setItem(name, FileFactory.fromZip(zipFile, value, null));
                } catch (IOException e) {
                    LOG.debug("not found file source: "+value+" in PBM extension");
                }
            } else if (type.equals("number")) {
                try {
                    book.setItem(name, Integer.parseInt(value));
                } catch (NumberFormatException ex) {
                    LOG.debug("invalid number: "+value, ex);
                }
            } else {
                book.setItem(name, value);
            }
        }
    }

    public static void readPBM(Element root, Book book, ZipFile zipFile) throws JemException {
        // ignore /package/head
        readMetadata(root, book, zipFile);
        readExtension(root, book, zipFile);
    }

    private static String readChapters(Element root, ZipFile zipFile, HashMap<String, Chapter> chapters,
                                       LinkedList<String> chapterIDs) throws JemException {
        Element contents = root.element("contents");
        if (contents == null) {
            throw new JemException("Not found 'contents' element in PBC");
        }
        String encoding = contents.attributeValue("encoding");
        if (encoding == null) {
            throw new JemException("Not found attribute 'encoding' of 'contents' element in PBC");
        }
        int count = parseNumber(contents.attributeValue("count"), -1);
        for (Iterator it = contents.elementIterator(); it.hasNext(); ) {
            if (count >= 0 && chapters.size() >= count) {
                break;
            }
            Element item = (Element) it.next();
            if (! item.getName().equals("item")) {
                LOG.debug("ignore unexpected element '"+item.getName()+"' in PBC");
                continue;
            }
            String id = item.attributeValue("id");
            if (id == null) {
                LOG.debug("ignore 'item' element without 'id' attribute in PBC");
                continue;
            }
            String href = item.attributeValue("href");
            if (href == null) {
                LOG.debug("ignore 'item' element without 'href' attribute in PBC");
                continue;
            }
            Chapter chapter = new Chapter(item.getText().trim(), "");
            try {
                FileObject fb = FileFactory.fromZip(zipFile, href, null);
                chapter.getSource().setFile(fb, encoding);
            } catch (IOException e) {
                LOG.debug("not found text source: "+href, e);
            }
            String intro = item.attributeValue("intro");
            if (intro != null) {
                try {
                    FileObject fb = FileFactory.fromZip(zipFile, intro, null);
                    chapter.setIntro(new TextObject(fb, encoding));
                } catch (IOException e) {
                    LOG.debug("not found intro source: "+intro, e);
                }
            }
            String cover = item.attributeValue("cover");
            if (cover != null) {
                try {
                    chapter.setCover(FileFactory.fromZip(zipFile, cover, null));
                } catch (IOException e) {
                    LOG.debug("not found cover source: "+cover, e);
                }
            }
            chapters.put(id, chapter);
            chapterIDs.add(id);
        }
        return encoding;
    }

    private static void readSections(Element root, ZipFile zipFile, String encoding,
                                     HashMap<String, Chapter> chapters,
                                     LinkedList<String> chapterIDs) throws JemException {
        Element sections = root.element("sections");
        if (sections == null) {
            throw new JemException("Not found 'sections' element in PBC");
        }
        int count = parseNumber(sections.attributeValue("count"), -1), ix = 0;
        for (Iterator it = sections.elementIterator(); it.hasNext(); ) {
            if (count >= 0 && ix >= count) {
                break;
            }
            Element elem = (Element) it.next();
            if (! elem.getName().equals("section")) {
                LOG.debug("ignore unexpected element '"+elem.getName()+"' in PBC");
                continue;
            }
            String sectionId = elem.attributeValue("id");
            if (sectionId == null) {
                LOG.debug("ignore 'section' element without 'id' attribute in PBC");
                continue;
            }
            String title = elem.attributeValue("title");
            if (title == null) {
                LOG.debug("ignore 'section' element without 'title' attribute in PBC");
                continue;
            }
            Chapter chapter = new Chapter(title, "");
            String intro = elem.attributeValue("intro");
            if (intro != null) {
                try {
                    FileObject fb = FileFactory.fromZip(zipFile, intro, null);
                    chapter.setIntro(new TextObject(fb, encoding));
                } catch (IOException e) {
                    LOG.debug("not found intro source: "+intro, e);
                }
            }
            String cover = elem.attributeValue("cover");
            if (cover != null) {
                try {
                    chapter.setCover(FileFactory.fromZip(zipFile, cover, null));
                } catch (IOException e) {
                    LOG.debug("not found cover source: "+cover, e);
                }
            }
            int sub_count = parseNumber(elem.attributeValue("count"), -1);
            chapters.put(sectionId, chapter);
            boolean added = false;
            for (Iterator sub_it = elem.elementIterator(); sub_it.hasNext(); ) {
                if (sub_count >= 0 && chapter.size() >= sub_count) {
                    break;
                }
                Element item = (Element) sub_it.next();
                String chapterId = item.attributeValue("id");
                if (chapterId == null) {
                    LOG.debug("ignore 'chapter' element without 'id' attribute in PBC");
                    continue;
                }
                Chapter sub = chapters.get(chapterId);
                if (sub == null) {
                    continue;
                }
                chapter.append(sub);
                if (! added) {
                    int index = chapterIDs.indexOf(chapterId);
                    if (index != -1) {
                        chapterIDs.add(index, sectionId);
                        added = true;
                    }
                }
                chapterIDs.remove(chapterId);
            }
            ++ix;
        }
    }

    public static void readPBC(Element root, Book book, ZipFile zipFile) throws JemException {
        HashMap<String, Chapter> chapters = new HashMap<String, Chapter>();
        LinkedList<String> chapterIDs = new LinkedList<String>();
        String encoding = readChapters(root, zipFile, chapters, chapterIDs);
        readSections(root, zipFile, encoding, chapters, chapterIDs);
        for (String id: chapterIDs) {
            Chapter chapter = chapters.get(id);
            assert chapter != null;
            book.append(chapter);
        }
    }
}
