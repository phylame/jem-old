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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.NumberUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.formats.common.ZipParser;

import static pw.phylame.jem.formats.util.xml.XmlUtils.*;
import static pw.phylame.jem.formats.util.text.TextUtils.*;

/**
 * PMAB e-book parser.
 */
public class PmabParser extends ZipParser<PmabParseConfig> {
    private PmabParseConfig mycfg;
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
        super("pmab", PmabParseConfig.CONFIG_SELF, PmabParseConfig.class);
    }

    @Override
    protected void validateFile(ZipFile input, PmabParseConfig config) throws IOException, ParserException {
        String text = ZipUtils.readString(input, PMAB.MIME_FILE, null);
        if (!PMAB.MT_PMAB.equals(text)) {
            throw ExceptionFactory.parserException("pmab.parse.invalidMT", PMAB.MIME_FILE, PMAB.MT_PMAB);
        }
    }

    @Override
    public Book parse(ZipFile input, PmabParseConfig config) throws IOException, ParserException {
        mycfg = config;
        if (mycfg == null) {
            mycfg = new PmabParseConfig();
        }
        book = new Book();
        XmlPullParser xpp = newPullParser();
        int version = readPBM(input, xpp);
        readPBC(input, xpp);
        book.setExtension(PmabInfo.FILE_INFO, new PmabInfo(version, metaInfo));
        return book;
    }

    private int getVersion(XmlPullParser xpp, String error) throws ParserException {
        String str = getAttribute(xpp, "version");
        switch (str) {
            case "3.0":
                return 3;
            case "2.0":
                return 2;
            default:
                throw ExceptionFactory.parserException(error, str);
        }
    }

    private int readPBM(ZipFile zipFile, XmlPullParser xpp) throws IOException, ParserException {
        int pbmVersion = 0;
        boolean hasText = false;
        inAttributes = false;
        StringBuilder textBuffer = new StringBuilder();
        try (InputStream stream = ZipUtils.openStream(zipFile, PMAB.PBM_FILE)) {
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
            throw ExceptionFactory.parserException(e, "pmab.parse.invalidPBM", e.getLocalizedMessage());
        }
        return pbmVersion;
    }

    private String findV3Config(String name, String[] parts, String defaultValue) {
        for (int ix = 1; ix < parts.length; ++ix) {
            String str = parts[ix];
            int pos = str.indexOf('=');
            if (pos > 0 && str.substring(0, pos).equals(name)) {   // found lv = name
                return str.substring(pos + 1);
            }
        }
        return defaultValue;
    }

    private Object parseV3Item(String text, ZipFile zipFile) throws IOException, ParserException {
        Object value;
        if (isEmpty(itemType)) {
            value = text;
        } else {
            String[] parts = itemType.split(";");
            String type = parts[0];
            if (type.equals(Jem.STRING)) {
                if (itemName.equals(Chapter.LANGUAGE)) {
                    value = parseLocale(text);
                } else {
                    value = text;
                }
            } else if (type.equals(Jem.DATETIME) || type.equals("date") || type.equals("time")) {
                String format = findV3Config("format", parts, mycfg.dateFormat);
                value = parseDate(text, format);
            } else if (type.startsWith("text/")) {  // text object
                String t = type.substring(5);
                FileObject fb = FileFactory.forZip(zipFile, text, "text/" + t);
                String encoding = findV3Config("encoding", parts, mycfg.textEncoding);
                value = TextFactory.forFile(fb, encoding, t);
            } else if (type.equals(Jem.LOCALE)) {
                value = parseLocale(text);
            } else if (type.matches("[\\w]+/[\\w\\-]+")) {   // file object
                value = FileFactory.forZip(zipFile, text, type);
            } else if (type.equals(Jem.INTEGER) || type.equals("uint")) {
                value = NumberUtils.parseInt(text);
            } else if (type.equals(Jem.REAL)) {
                value = NumberUtils.parseDouble(text);
            } else if (type.equals("bytes")) {
                System.err.println("***PMAB: <item> with 'bytes' type is ignored***");
                value = text;
            } else if (type.equals(Jem.BOOLEAN)) {
                value = Boolean.parseBoolean(text);
            } else {    // store as string
                value = text;
            }
        }
        return value;
    }

    private boolean startPBMv3(String tag, XmlPullParser xpp) throws ParserException {
        boolean hasText = false;
        switch (tag) {
            case "item":
                itemName = getAttribute(xpp, "name");
                itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
                break;
            case "attributes":
                inAttributes = true;
                break;
            case "meta":
                metaInfo.put(getAttribute(xpp, "name"), getAttribute(xpp, "value"));
                break;
            case "head":
                metaInfo = new HashMap<>();
                break;
        }
        return hasText;
    }

    private void endPBMv3(String tag, StringBuilder textBuffer, ZipFile zipFile) throws IOException,
            ParserException {
        if (tag.equals("item")) {
            Object value = parseV3Item(textBuffer.toString().trim(), zipFile);
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

    private boolean startPBMv2(String tag, XmlPullParser xpp, ZipFile zipFile) throws IOException,
            ParserException {
        boolean hasText = false;
        switch (tag) {
            case "attr":
                if (checkCount()) {
                    attrName = getAttribute(xpp, "name");
                    if (attrName.equals(Book.COVER)) {
                        mediaType = xpp.getAttributeValue(null, "media-type");
                    } else {
                        mediaType = null;
                    }
                    hasText = true;
                }
                break;
            case "item":
                if (checkCount()) {
                    String name = getAttribute(xpp, "name");
                    String type = xpp.getAttributeValue(null, "type");
                    if (isEmpty(type) || type.equals("text")) {
                        book.setExtension(name, getAttribute(xpp, "value"));
                    } else if (type.equals("number")) {
                        book.setExtension(name, NumberUtils.parseNumber(getAttribute(xpp, "value")));
                    } else if (type.equals("file")) {    // file will be processed in <object>
                        attrName = name;
                    } else {
                        throw ExceptionFactory.parserException("pmab.parse.2.unknownItemType", name);
                    }
                }
                break;
            case "object":
                if (checkCount()) {
                    String href = getAttribute(xpp, "href");
                    String mime = getAttribute(xpp, "media-type");
                    FileObject fb = FileFactory.forZip(zipFile, href, mime);
                    Object value = fb;
                    if (mime.startsWith("text/plain")) {
                        String encoding = xpp.getAttributeValue(null, "encoding");
                        if (isEmpty(encoding)) {
                            value = TextFactory.forFile(fb, mycfg.textEncoding);
                        } else {
                            value = TextFactory.forFile(fb, encoding);
                        }
                    }
                    book.setExtension(attrName, value);
                }
                break;
            case "metadata": {
                String str = xpp.getAttributeValue(null, "count");
                if (isValid(str)) {
                    count = NumberUtils.parseInt(str);
                } else {
                    count = -1;
                }
                order = 0;
                break;
            }
            case "extension": {
                String str = xpp.getAttributeValue(null, "count");
                if (isValid(str)) {
                    count = NumberUtils.parseInt(str);
                } else {
                    count = -1;
                }
                order = 0;
                break;
            }
            case "meta":
                metaInfo.put(getAttribute(xpp, "name"), getAttribute(xpp, "content"));
                break;
            case "head":
                metaInfo = new HashMap<>();
                break;
        }
        return hasText;
    }

    private void endPBMv2(String tag, StringBuilder textBuffer, ZipFile zipFile) throws IOException,
            ParserException {
        if (tag.equals("attr")) {
            if (checkCount()) {
                String text = textBuffer.toString().trim();
                Object value;
                if (attrName.equals(Chapter.DATE)) {
                    value = parseDate(text, mycfg.dateFormat);
                } else if (attrName.equals(Chapter.INTRO)) {
                    value = TextFactory.forString(text);
                } else if (attrName.equals(Chapter.LANGUAGE)) {
                    value = parseLocale(text);
                } else if (isValid(mediaType)) {
                    value = FileFactory.forZip(zipFile, text, mediaType);
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

    private void readPBC(ZipFile zipFile, XmlPullParser xpp) throws IOException, ParserException {
        int pbcVersion = 0;
        boolean hasText = false;
        StringBuilder textBuffer = new StringBuilder();
        try (InputStream stream = ZipUtils.openStream(zipFile, PMAB.PBC_FILE)) {
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
            throw ExceptionFactory.parserException(e, "pmab.parse.invalidPBC", e.getLocalizedMessage());
        }
    }

    private void appendChapter() {
        Chapter chapter = new Chapter();
        currentChapter.append(chapter);
        currentChapter = chapter;
    }

    private boolean startPBCv3(String tag, XmlPullParser xpp) throws ParserException {
        boolean hasText = false;
        switch (tag) {
            case "chapter":
                appendChapter();
                break;
            case "item":
                itemName = getAttribute(xpp, "name");
                itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
                break;
            case "content":
                itemType = xpp.getAttributeValue(null, "type");
                hasText = true;
                break;
        }
        return hasText;
    }

    private void endPBCv3(String tag, StringBuilder textBuffer, ZipFile zipFile) throws IOException,
            ParserException {
        switch (tag) {
            case "chapter":
                currentChapter = currentChapter.getParent();
                break;
            case "item": {
                String text = textBuffer.toString().trim();
                currentChapter.setAttribute(itemName, parseV3Item(text, zipFile));
                break;
            }
            case "content": {
                TextObject content;
                String text = textBuffer.toString().trim();
                if (isEmpty(itemType)) {
                    content = TextFactory.forString(text);
                } else if (itemType.startsWith("text/")) {
                    String[] parts = itemType.split(";");
                    FileObject fb = FileFactory.forZip(zipFile, text, parts[0]);
                    String encoding = findV3Config("encoding", parts, mycfg.textEncoding);
                    content = TextFactory.forFile(fb, encoding);
                } else {
                    content = TextFactory.forString(text);
                }
                currentChapter.setContent(content);
                break;
            }
        }
    }

    private boolean startPBCv2(String tag, XmlPullParser xpp, ZipFile zipFile) throws IOException,
            ParserException {
        boolean hasText = false;
        switch (tag) {
            case "chapter": {
                String href = xpp.getAttributeValue(null, "href");
                if (isEmpty(href)) {
                    appendChapter();
                } else {
                    FileObject fb = FileFactory.forZip(zipFile, href, "text/plain");
                    chapterEncoding = xpp.getAttributeValue(null, "encoding");
                    if (isEmpty(chapterEncoding)) {
                        chapterEncoding = mycfg.textEncoding;
                    }
                    appendChapter();
                    currentChapter.setContent(TextFactory.forFile(fb, chapterEncoding));
                }
                break;
            }
            case "title":
                hasText = true;
                break;
            case "cover": {
                String href = getAttribute(xpp, "href");
                String mime = getAttribute(xpp, "media-type");
                currentChapter.setCover(FileFactory.forZip(zipFile, href, mime));
                break;
            }
            case "intro": {
                String href = getAttribute(xpp, "href");
                FileObject fb = FileFactory.forZip(zipFile, href, "text/plain");
                String encoding = xpp.getAttributeValue(null, "encoding");
                if (isEmpty(encoding)) {
                    if (mycfg.useChapterEncoding) {
                        encoding = chapterEncoding;
                    } else {
                        encoding = mycfg.textEncoding;
                    }
                }
                currentChapter.setIntro(TextFactory.forFile(fb, encoding));
                break;
            }
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
