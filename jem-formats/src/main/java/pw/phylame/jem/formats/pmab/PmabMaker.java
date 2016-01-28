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

import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.IOUtils;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.util.FileInfo;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.xml.XmlRender;
import pw.phylame.jem.formats.common.ZipMaker;

import static pw.phylame.jem.formats.util.text.TextUtils.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

/**
 * PMAB e-book maker.
 */
public class PmabMaker extends ZipMaker<PmabMakeConfig> {
    private PmabMakeConfig mycfg;

    private int version = 3;

    public PmabMaker() {
        super("pmab", PmabMakeConfig.CONFIG_SELF, PmabMakeConfig.class);
    }

    @Override
    public void make(Book book, ZipOutputStream zipout, PmabMakeConfig config)
            throws IOException, MakerException {
        mycfg = (config != null) ? config : new PmabMakeConfig();
        if (isValid(mycfg.version)) {
            char ch = mycfg.version.charAt(0);
            if (ch == '3') {
                version = 3;
            } else if (ch == '2') {
                version = 2;
            } else {
                throw makerException("pmab.make.unsupportedVersion", mycfg.version);
            }
        }
        XmlRender xmlRender = new XmlRender(mycfg.xmlConfig);
        writePBM(book, zipout, xmlRender);
        writePBC(book, zipout, xmlRender);
        writeMIME(zipout);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        ZipUtils.writeString(PMAB.MT_PMAB, PMAB.MIME_FILE, "ASCII", zipout);
    }

    private void writePBM(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException, MakerException {
        StringWriter writer = prepareXml(xmlRender, "pbm", mycfg.version,
                PMAB.PBM_XML_NS);
        switch (version) {
            case 3:
                writePBMHead("value", true, xmlRender);
                writePBMv3(book, zipout, xmlRender);
                break;
            case 2:
                writePBMHead("content", false, xmlRender);
                writePBMv2(book, zipout, xmlRender);
                break;
        }
        writeXml(xmlRender, writer, PMAB.PBM_FILE, zipout);
    }

    private void writePBC(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException {
        StringWriter writer = prepareXml(xmlRender, "pbc", mycfg.version,
                PMAB.PBC_XML_NS);
        switch (version) {
            case 3:
                writePBCv3(book, zipout, xmlRender);
                break;
            case 2:
                writePBCv2(book, zipout, xmlRender);
                break;
        }
        writeXml(xmlRender, writer, PMAB.PBC_FILE, zipout);
    }

    private void writePBMHead(String valueName, boolean ignoreEmpty,
                              XmlRender xmlRender) throws IOException {
        Map<Object, Object> metaInfo = mycfg.metaInfo;
        if (metaInfo == null || metaInfo.isEmpty()) {
            if (!ignoreEmpty) {
                xmlRender.startTag("head").endTag();
            }
            return;
        }
        xmlRender.startTag("head");
        for (Map.Entry<Object, Object> entry : metaInfo.entrySet()) {
            xmlRender.startTag("meta").attribute("name", entry.getKey().toString());
            xmlRender.attribute(valueName, entry.getValue().toString()).endTag();
        }
        xmlRender.endTag();
    }

    private void writePBMv3(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException {
        writeV3Attributes(book, "", zipout, xmlRender);
        xmlRender.startTag("extensions");
        for (Map.Entry<String, Object> entry : book.extensionEntries()) {
            String key = entry.getKey();
            if (key.equals(FileInfo.FILE_INFO)) {
                continue;
            }
            writeV3Item(key, entry.getValue(), "", zipout, xmlRender);
        }
        xmlRender.endTag();
    }

    private void writeV3Attributes(Chapter chapter, String prefix,
                                   ZipOutputStream zipout,
                                   XmlRender xmlRender) throws IOException {
        xmlRender.startTag("attributes");
        for (Map.Entry<String, Object> entry : chapter.attributeEntries()) {
            writeV3Item(entry.getKey(), entry.getValue(), prefix, zipout, xmlRender);
        }
        xmlRender.endTag();
    }

    private void writeV3Item(String key, Object value, String prefix,
                             ZipOutputStream zipout,
                             XmlRender xmlRender) throws IOException {
        xmlRender.startTag("item").attribute("name", key);
        String text;
        String type = Jem.typeOfVariant(value);
        if (!type.equals("str")) {
            switch (type) {
                case "text":
                    String dir;
                    if (key.equals(Chapter.INTRO)) {    // only intro stored to text dir
                        dir = mycfg.textDir;
                    } else {
                        dir = mycfg.extraDir;
                    }
                    text = writeV3Text((TextObject) value, dir, prefix + key,
                            zipout, xmlRender);
                    type = null;
                    break;
                case "file":
                    text = writeFile((FileObject) value, prefix + key, "type",
                            zipout, xmlRender);
                    type = null;
                    break;
                case "datetime":
                    text = formatDate((Date) value, mycfg.dateFormat);
                    type = type + ";format=" + mycfg.dateFormat;
                    break;
                case "locale":
                    text = formatLocale((Locale) value);
                    break;
                default:
                    text = value.toString();
                    break;
            }
        } else {
            text = (String) value;
        }
        if (type != null) {
            xmlRender.attribute("type", type);
        }
        xmlRender.text(text).endTag();
    }

    private String writeV3Text(TextObject text, String dir, String baseName,
                               ZipOutputStream zipout,
                               XmlRender xmlRender) throws IOException {
        String[] objects = writeText(text, dir, baseName, zipout);
        xmlRender.attribute("type", "text/" + text.getType() + ";encoding=" + objects[1]);
        return objects[0];
    }

    private void writePBMv2(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException, MakerException {
        xmlRender.startTag("metadata");
        xmlRender.attribute("count", Integer.toString(book.attributeCount()));
        for (Map.Entry<String, Object> entry : book.attributeEntries()) {
            writePBMv2Attr(entry.getKey(), entry.getValue(), zipout, xmlRender);
        }
        xmlRender.endTag();

        xmlRender.startTag("extension");
        int size = book.extensionCount();
        if (book.hasExtension(FileInfo.FILE_INFO)) {
            --size;
        }
        xmlRender.attribute("count", Integer.toString(size));
        xmlRender.comment("The following data will be added to PMAB.");
        for (Map.Entry<String, Object> entry : book.extensionEntries()) {
            String key = entry.getKey();
            if (key.equals(FileInfo.FILE_INFO)) {
                continue;
            }
            writePBMv2Item(xmlRender, key, entry.getValue(), zipout);
        }
        xmlRender.endTag();
    }

    private void writePBMv2Attr(String key, Object value, ZipOutputStream zipout,
                                XmlRender xmlRender) throws IOException, MakerException {
        xmlRender.startTag("attr").attribute("name", key);
        String text;
        String type = Jem.typeOfVariant(value);
        if (type.equals("text")) {
            text = fetchText((TextObject) value, "");
        } else if (key.equals(Chapter.COVER)) {
            text = writeV2Cover((FileObject) value, "", zipout, xmlRender);
        } else if (type.equals("datetime")) {
            text = formatDate((Date) value, mycfg.dateFormat);
        } else if (type.equals("locale")) {
            text = formatLocale((Locale) value);
        } else {
            text = value.toString();
        }
        xmlRender.text(text).endTag();
    }

    private String writeV2Cover(FileObject cover, String prefix,
                                ZipOutputStream zipout,
                                XmlRender xmlRender) throws IOException {
        String name = prefix + "cover." + IOUtils.getExtension(cover.getName());
        return writeFile(cover, mycfg.imageDir, name, "media-type", zipout, xmlRender);
    }

    private void writePBMv2Item(XmlRender xmlRender, String key, Object value,
                                ZipOutputStream zipout) throws IOException {
        xmlRender.startTag("item").attribute("name", key);
        String text = null;
        if (value instanceof FileObject) {
            xmlRender.attribute("type", "file");
            xmlRender.startTag("object");
            String href = writeFile((FileObject) value, key, "media-type",
                    zipout, xmlRender);
            xmlRender.attribute("href", href).endTag();
        } else if (value instanceof TextObject) {
            xmlRender.attribute("type", "file");
            xmlRender.startTag("object");
            TextObject tb = (TextObject) value;
            String href = writeV2Text(tb, mycfg.extraDir, key, zipout, xmlRender);
            xmlRender.attribute("media-type", "text/" + tb.getType());
            xmlRender.attribute("href", href).endTag();
        } else if (value instanceof Number) {
            xmlRender.attribute("type", "number");
            text = value.toString();
        } else {
            xmlRender.attribute("type", "text");
            text = value.toString();
        }
        if (text != null) {
            xmlRender.text(text);
        }
        xmlRender.endTag();
    }

    private String writeV2Text(TextObject text, String dir, String baseName,
                               ZipOutputStream zipout,
                               XmlRender xmlRender) throws IOException {
        String[] objects = writeText(text, dir, baseName, zipout);
        xmlRender.attribute("encoding", objects[1]);
        return objects[0];
    }

    private void writePBCv3(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException {
        xmlRender.startTag("toc");
        int count = 1;
        for (Chapter chapter : book) {
            writeV3Chapter(chapter, Integer.toString(count), zipout, xmlRender);
            ++count;
        }
        xmlRender.endTag();
    }

    private void writeV3Chapter(Chapter chapter, String suffix,
                                ZipOutputStream zipout,
                                XmlRender xmlRender) throws IOException {
        xmlRender.startTag("chapter");
        String base = "chapter-" + suffix;

        // attributes
        writeV3Attributes(chapter, base + "-", zipout, xmlRender);

        // content
        TextObject content = chapter.getContent();
        xmlRender.startTag("content");
        String href = writeV3Text(content, mycfg.textDir, base, zipout, xmlRender);
        xmlRender.text(href).endTag();

        int count = 1;
        for (Chapter sub : chapter) {
            writeV3Chapter(sub, suffix + "-" + count, zipout, xmlRender);
            ++count;
        }
        xmlRender.endTag();
    }

    private void writePBCv2(Book book, ZipOutputStream zipout, XmlRender xmlRender)
            throws IOException {
        xmlRender.startTag("contents");
        xmlRender.attribute("depth", Integer.toString(Jem.depthOf(book)));
        int count = 1;
        for (Chapter chapter : book) {
            writeV2Chapter(chapter, Integer.toString(count), zipout, xmlRender);
            ++count;
        }
        xmlRender.endTag();
    }

    private void writeV2Chapter(Chapter chapter, String suffix,
                                ZipOutputStream zipout,
                                XmlRender xmlRender) throws IOException {
        xmlRender.startTag("chapter");
        String base = "chapter-" + suffix;

        // content
        TextObject content = chapter.getContent();
        String href = writeV2Text(content, mycfg.textDir, base, zipout, xmlRender);
        xmlRender.attribute("href", href);

        // size of sub chapters
        int size = chapter.size();
        if (size != 0) {
            xmlRender.attribute("count", Integer.toString(size));
        }

        // title
        xmlRender.startTag("title").text(chapter.getTitle()).endTag();

        // cover
        FileObject cover = chapter.getCover();
        if (cover != null) {
            xmlRender.startTag("cover");
            href = writeV2Cover(cover, base + "-", zipout, xmlRender);
            xmlRender.attribute("href", href).endTag();
        }
        // intro
        TextObject intro = chapter.getIntro();
        if (intro != null) {
            xmlRender.startTag("intro");
            href = writeV2Text(intro, mycfg.textDir, base + "-intro", zipout, xmlRender);
            xmlRender.attribute("href", href).endTag();
        }

        int count = 1;
        for (Chapter sub : chapter) {
            writeV2Chapter(sub, suffix + "-" + count, zipout, xmlRender);
            ++count;
        }
        xmlRender.endTag();
    }

    // return href and encoding
    private String[] writeText(TextObject text, String dir, String baseName,
                               ZipOutputStream zipout) throws IOException {
        String encoding = mycfg.textEncoding != null
                ? mycfg.textEncoding : PMAB.defaultEncoding;
        String href;
        String type = text.getType();
        if (type.equals(TextObject.PLAIN)) {
            href = baseName + ".txt";
        } else {
            href = baseName + "." + type;
        }
        href = dir + "/" + href;
        ZipUtils.writeText(text, href, encoding, zipout);
        return new String[]{href, encoding};
    }

    private String writeFile(FileObject file, String baseName,
                             String mimeKey, ZipOutputStream zipout,
                             XmlRender xmlRender) throws IOException {
        String name = baseName + "." + IOUtils.getExtension(file.getName());
        String dir;
        if (file.getMime().startsWith("image/")) {  // image file stored to image dir
            dir = mycfg.imageDir;
        } else {
            dir = mycfg.extraDir;
        }
        return writeFile(file, dir, name, mimeKey, zipout, xmlRender);
    }

    private String writeFile(FileObject file, String dir, String name,
                             String mimeKey, ZipOutputStream zipout,
                             XmlRender xmlRender) throws IOException {
        String href = dir + "/" + name;
        ZipUtils.writeFile(file, href, zipout);
        xmlRender.attribute(mimeKey, file.getMime());
        return href;
    }

    private StringWriter prepareXml(XmlRender xmlRender, String root, String version,
                                    String ns) throws IOException {
        StringWriter writer = new StringWriter();
        xmlRender.setOutput(writer);
        xmlRender.startXml();
        xmlRender.docdecl(root);
        xmlRender.startTag(root).attribute("version", version);
        xmlRender.attribute("xmlns", ns);
        return writer;
    }

    private void writeXml(XmlRender xmlRender, StringWriter writer, String name,
                          ZipOutputStream zipout) throws IOException {
        xmlRender.endTag();
        xmlRender.endXml();
        ZipUtils.writeString(writer.toString(), name, mycfg.xmlConfig.encoding, zipout);
    }
}
