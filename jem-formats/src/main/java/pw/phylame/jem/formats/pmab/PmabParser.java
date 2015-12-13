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

package pw.phylame.jem.formats.pmab;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.NumberUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.xml.XmlUtils;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.formats.common.ZipBookParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * PMAB e-book parser.
 */
public class PmabParser extends ZipBookParser<PmabParseConfig> {
    private PmabParseConfig myConfig;
    // temporary book
    private Book book;
    // PBM 3 data
    private String itemName, itemType;      // item attribute
    private boolean inAttributes = false;   // item is contained in <attributes>
    // PMAB 2 counter
    private int count, order;
    // PBM 2 data
    private String attrName, mediaType;
    // pbc data
    private Chapter currentChapter;
    // used for encoding of intro in chapter
    private String chapterEncoding;

    private HashMap<String, Object> metaInfo;

    public PmabParser() {
        super("pmab", PmabParseConfig.class, PmabParseConfig.CONFIG_SELF);
    }

    @Override
    protected void validateFile(ZipFile input, PmabParseConfig config)
            throws IOException, ParserException {
        String text = ZipUtils.readString(input, PMAB.MIME_FILE, null);
        if (!PMAB.MT_PMAB.equals(text)) {
            throw parserException("pmab.parse.invalidMT", PMAB.MIME_FILE,
                    PMAB.MT_PMAB);
        }
    }

    @Override
    public Book parse(ZipFile input, PmabParseConfig config) throws IOException,
            ParserException {
        myConfig = (config != null) ? config : new PmabParseConfig();
        book = new Book();
        XmlPullParser xpp = XmlUtils.newPullParser();
        int version = readPBM(input, xpp);
        readPBC(input, xpp);

        book.setExtension(PmabInfo.FILE_INFO, new PmabInfo(version, metaInfo));
        return book;
    }

    private int getVersion(XmlPullParser xpp, String error) throws ParserException {
        String str = XmlUtils.getAttribute(xpp, "version");
        if (str.equals("3.0")) {
            return 3;
        } else if (str.equals("2.0")) {
            return 2;
        } else {
            throw parserException(error, str);
        }
    }

    private int readPBM(ZipFile zipFile, XmlPullParser xpp) throws IOException,
            ParserException {
        InputStream stream = ZipUtils.openStream(zipFile, PMAB.PBM_FILE);
        int pbmVersion = 0;
        boolean hasText = false;
        StringBuilder textBuffer = new StringBuilder();
        try {
            xpp.setInput(stream, null);
            int eventType = xpp.getEventType();
            do {
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        String tag = xpp.getName();
                        if (pbmVersion == 3) {
                            hasText = startPBMv3(tag, xpp);
                        } else if (pbmVersion == 2) {
                            hasText = startPBMv2(tag, xpp, zipFile);
                        } else if (tag.equals("pbm")) {
                            pbmVersion = getVersion(xpp, "pmab.parse.unsupportedPBM");
                        } else {
                            hasText = false;
                        }
                    }
                    break;
                    case XmlPullParser.TEXT: {
                        if (hasText) {
                            textBuffer.append(xpp.getText());
                        }
                    }
                    break;
                    case XmlPullParser.END_TAG: {
                        String tag = xpp.getName();
                        if (pbmVersion == 3) {
                            endPBMv3(tag, textBuffer, zipFile);
                        } else if (pbmVersion == 2) {
                            endPBMv2(tag, textBuffer, zipFile);
                        }
                        textBuffer.setLength(0);
                    }
                    break;
                }
                eventType = xpp.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw parserException(e, "pmab.parse.invalidPBM",
                    e.getLocalizedMessage());
        } finally {
            stream.close();
        }
        return pbmVersion;
    }

    private String findPbm3ItemConfig(String name, String[] parts,
                                      String defaultValue) {
        for (int ix = 1; ix < parts.length; ++ix) {
            String str = parts[ix];
            int pos = str.indexOf('=');
            if (pos > 0 && str.substring(0, pos).equals(name)) {   // found lv = name
                return str.substring(pos + 1);
            }
        }
        return defaultValue;
    }

    private Object parsePMABv3Item(String text, ZipFile zipFile)
            throws IOException, ParserException {
        Object value;
        if (!TextUtils.isValid(itemType)) {
            value = text;
        } else {
            String[] parts = itemType.split(";");
            String type = parts[0];
            if (type.equals("str")) {
                value = text;
            } else if (type.equals("datetime") || type.equals("date")
                    || type.equals("time")) {
                String format = findPbm3ItemConfig("format", parts,
                        myConfig.dateFormat);
                value = TextUtils.parseDate(text, format);
            } else if (type.startsWith("text/")) {  // text object
                String t = type.substring(5);
                FileObject fb = FileFactory.fromZip(zipFile, text, "text/" + t);
                String encoding = findPbm3ItemConfig("encoding", parts,
                        myConfig.textEncoding);
                value = TextFactory.fromFile(fb, encoding, t);
            } else if (type.matches("[\\w]+/[\\w\\-]+")) {   // file object
                value = FileFactory.fromZip(zipFile, text, type);
            } else if (type.equals("int") || type.equals("uint")) {
                value = NumberUtils.parseInt(text);
            } else if (type.equals("real")) {
                value = NumberUtils.parseDouble(text);
            } else if (type.equals("bytes")) {
                System.err.println("PMAB: <item> with 'bytes' type is ignored");
                value = text;
            } else if (type.equals("bool")) {
                value = Boolean.parseBoolean(text);
            } else {    // store as string
                value = text;
            }
        }
        return value;
    }

    private boolean startPBMv3(String tag, XmlPullParser xpp) throws ParserException {
        boolean hasText = false;
        if (tag.equals("item")) {
            itemName = XmlUtils.getAttribute(xpp, "name");
            itemType = xpp.getAttributeValue(null, "type");
            hasText = true;
        } else if (tag.equals("attributes")) {
            inAttributes = true;
        } else if (tag.equals("meta")) {
            metaInfo.put(XmlUtils.getAttribute(xpp, "name"),
                    XmlUtils.getAttribute(xpp, "value"));
        } else if (tag.equals("head")) {
            metaInfo = new HashMap<String, Object>();
        }
        return hasText;
    }

    private void endPBMv3(String tag, StringBuilder textBuffer, ZipFile zipFile)
            throws IOException, ParserException {
        if (tag.equals("item")) {
            Object value = parsePMABv3Item(textBuffer.toString().trim(), zipFile);
            if (inAttributes) {
                book.setAttribute(itemName, value);
            } else {
                book.setExtension(itemName, value);
            }
        } else if (tag.equals("attributes")) {
            inAttributes = false;
        }
    }

    private boolean checkCount() {
        return count < 0 || order < count;
    }

    private boolean startPBMv2(String tag, XmlPullParser xpp, ZipFile zipFile)
            throws IOException, ParserException {
        boolean hasText = false;
        if (tag.equals("attr")) {
            if (checkCount()) {
                attrName = XmlUtils.getAttribute(xpp, "name");
                if (attrName.equals(Book.COVER)) {
                    mediaType = xpp.getAttributeValue(null, "media-type");
                } else {
                    mediaType = null;
                }
                hasText = true;
            }
        } else if (tag.equals("item")) {
            if (checkCount()) {
                String name = XmlUtils.getAttribute(xpp, "name");
                String type = xpp.getAttributeValue(null, "type");
                if (!TextUtils.isValid(type) || type.equals("text")) {
                    String value = XmlUtils.getAttribute(xpp, "value");
                    book.setExtension(name, value);
                } else if (type.equals("number")) {
                    String value = XmlUtils.getAttribute(xpp, "value");
                    book.setExtension(name, NumberUtils.parseNumber(value));
                } else if (type.equals("file")) {    // file will be processed in <object>
                    attrName = name;
                } else {
                    throw parserException("pmab.parse2.unknownItemType", name);
                }
            }
        } else if (tag.equals("object")) {
            if (checkCount()) {
                String href = XmlUtils.getAttribute(xpp, "href");
                String mime = XmlUtils.getAttribute(xpp, "media-type");
                FileObject fb = FileFactory.fromZip(zipFile, href, mime);
                Object value = fb;
                if (mime.startsWith("text/plain")) {
                    String encoding = xpp.getAttributeValue(null, "encoding");
                    if (!TextUtils.isValid(encoding)) {
                        value = TextFactory.fromFile(fb, myConfig.textEncoding);
                    } else {
                        value = TextFactory.fromFile(fb, encoding);
                    }
                }
                book.setExtension(attrName, value);
            }
        } else if (tag.equals("metadata")) {
            String str = xpp.getAttributeValue(null, "count");
            if (TextUtils.isValid(str)) {
                count = NumberUtils.parseInt(str);
            } else {
                count = -1;
            }
            order = 0;
        } else if (tag.equals("extension")) {
            String str = xpp.getAttributeValue(null, "count");
            if (TextUtils.isValid(str)) {
                count = NumberUtils.parseInt(str);
            } else {
                count = -1;
            }
            order = 0;
        } else if (tag.equals("meta")) {
            metaInfo.put(XmlUtils.getAttribute(xpp, "name"),
                    XmlUtils.getAttribute(xpp, "content"));
        } else if (tag.equals("head")) {
            metaInfo = new HashMap<String, Object>();
        }
        return hasText;
    }

    private void endPBMv2(String tag, StringBuilder textBuffer, ZipFile zipFile)
            throws IOException, ParserException {
        if (tag.equals("attr")) {
            if (checkCount()) {
                String text = textBuffer.toString().trim();
                Object value;
                if (attrName.equals("date")) {
                    value = TextUtils.parseDate(text, myConfig.dateFormat);
                } else if (attrName.equals("intro")) {
                    value = TextFactory.fromString(text);
                } else if (TextUtils.isValid(mediaType)) {
                    value = FileFactory.fromZip(zipFile, text, mediaType);
                } else {
                    value = text;
                }
                book.setAttribute(attrName, value);
            }
            ++order;
        } else if (tag.equals("item")) {
            ++order;
        }
    }

    private void readPBC(ZipFile zipFile, XmlPullParser xpp) throws IOException,
            ParserException {
        InputStream stream = ZipUtils.openStream(zipFile, PMAB.PBC_FILE);
        int pbcVersion = 0;
        boolean hasText = false;
        StringBuilder textBuffer = new StringBuilder();
        try {
            xpp.setInput(stream, null);
            int eventType = xpp.getEventType();
            do {
                switch (eventType) {
                    case XmlPullParser.START_TAG: {
                        String tag = xpp.getName();
                        if (pbcVersion == 3) {
                            hasText = startPBCv3(tag, xpp);
                        } else if (pbcVersion == 2) {
                            hasText = startPBCv2(tag, xpp, zipFile);
                        } else if (tag.equals("pbc")) {
                            pbcVersion = getVersion(xpp, "pmab.parse.unsupportedPBC");
                        } else {
                            hasText = false;
                        }
                    }
                    break;
                    case XmlPullParser.TEXT: {
                        if (hasText) {
                            textBuffer.append(xpp.getText());
                        }
                    }
                    break;
                    case XmlPullParser.END_TAG: {
                        String tag = xpp.getName();
                        if (pbcVersion == 3) {
                            endPBCv3(tag, textBuffer, zipFile);
                        } else if (pbcVersion == 2) {
                            endPBCv2(tag, textBuffer);
                        }
                        textBuffer.setLength(0);
                    }
                    break;
                    case XmlPullParser.START_DOCUMENT: {
                        currentChapter = book;
                    }
                    break;
                }
                eventType = xpp.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        } catch (XmlPullParserException e) {
            throw parserException(e, "pmab.parse.invalidPBC",
                    e.getLocalizedMessage());
        } finally {
            stream.close();
        }
    }

    private void appendChapter() {
        Chapter chapter = new Chapter();
        currentChapter.append(chapter);
        currentChapter = chapter;
    }

    private boolean startPBCv3(String tag, XmlPullParser xpp) throws ParserException {
        boolean hasText = false;
        if (tag.equals("chapter")) {
            appendChapter();
        } else if (tag.equals("item")) {
            itemName = XmlUtils.getAttribute(xpp, "name");
            itemType = xpp.getAttributeValue(null, "type");
            hasText = true;
        } else if (tag.equals("content")) {
            itemType = xpp.getAttributeValue(null, "type");
            hasText = true;
        }
        return hasText;
    }

    private void endPBCv3(String tag, StringBuilder textBuffer, ZipFile zipFile)
            throws IOException, ParserException {
        if (tag.equals("chapter")) {
            currentChapter = currentChapter.getParent();
        } else if (tag.equals("item")) {
            String text = textBuffer.toString().trim();
            currentChapter.setAttribute(itemName, parsePMABv3Item(text, zipFile));
        } else if (tag.equals("content")) {
            TextObject content;
            String text = textBuffer.toString().trim();
            if (!TextUtils.isValid(itemType)) {
                content = TextFactory.fromString(text);
            } else if (itemType.startsWith("text/")) {
                String[] parts = itemType.split(";");
                FileObject fb = FileFactory.fromZip(zipFile, text, parts[0]);
                String encoding = findPbm3ItemConfig("encoding", parts,
                        myConfig.textEncoding);
                content = TextFactory.fromFile(fb, encoding);
            } else {
                content = TextFactory.fromString(text);
            }
            currentChapter.setContent(content);
        }
    }

    private boolean startPBCv2(String tag, XmlPullParser xpp, ZipFile zipFile)
            throws IOException, ParserException {
        boolean hasText = false;
        if (tag.equals("chapter")) {
            String href = xpp.getAttributeValue(null, "href");
            if (!TextUtils.isValid(href)) {
                appendChapter();
            } else {
                FileObject fb = FileFactory.fromZip(zipFile, href, "text/plain");
                chapterEncoding = xpp.getAttributeValue(null, "encoding");
                if (!TextUtils.isValid(chapterEncoding)) {
                    chapterEncoding = myConfig.textEncoding;
                }
                appendChapter();
                currentChapter.setContent(TextFactory.fromFile(fb, chapterEncoding));
            }
        } else if (tag.equals("title")) {
            hasText = true;
        } else if (tag.equals("cover")) {
            String href = XmlUtils.getAttribute(xpp, "href");
            String mime = XmlUtils.getAttribute(xpp, "media-type");
            currentChapter.setCover(FileFactory.fromZip(zipFile, href, mime));
        } else if (tag.equals("intro")) {
            String href = XmlUtils.getAttribute(xpp, "href");
            FileObject fb = FileFactory.fromZip(zipFile, href, "text/plain");
            String encoding = xpp.getAttributeValue(null, "encoding");
            if (!TextUtils.isValid(encoding)) {
                if (myConfig.useChapterEncoding) {
                    encoding = chapterEncoding;
                } else {
                    encoding = myConfig.textEncoding;
                }
            }
            currentChapter.setIntro(TextFactory.fromFile(fb, encoding));
        }
        return hasText;
    }

    private void endPBCv2(String tag, StringBuilder textBuffer) {
        if (tag.equals("chapter")) {
            currentChapter = currentChapter.getParent();
        } else if (tag.equals("title")) {
            currentChapter.setTitle(textBuffer.toString().trim());
        }
    }
}
