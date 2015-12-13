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

package pw.phylame.jem.formats.jar;

import pw.phylame.jem.core.Jem;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.common.ZipBookMaker;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.text.TextConfig;
import pw.phylame.jem.formats.util.text.TextRender;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * <tt>Maker</tt> implement for JAR book.
 */
public class JarMaker extends ZipBookMaker<JarMakeConfig> {
    public JarMaker() {
        super("jar", JarMakeConfig.class, JarMakeConfig.CONFIG_SELF);
    }

    @Override
    public void make(Book book, ZipOutputStream zipout, JarMakeConfig config)
            throws IOException, MakerException {
        if (config == null) {
            config = new JarMakeConfig();
        }
        // JAR template
        copyTemplate(zipout);

        // MANIFEST
        String title = book.getTitle();
        String mf = String.format(JAR.MANIFEST_TEMPLATE,
                "Jem", Jem.VERSION, title, config.vendor, title);
        ZipUtils.writeString(mf, JAR.MANIFEST_FILE, JAR.META_ENCODING, zipout);

        JarRender jarRender = new JarRender(zipout);
        try {
            TextRender.renderBook(book, jarRender, config.textConfig);
        } catch (Exception e) {
            throw new IOException(e);
        }

        // navigation
        try {
            writeMeta(book, zipout, jarRender.items, config.textConfig);
        } catch (Exception e) {
            throw new MakerException(e);
        }
    }

    private void copyTemplate(ZipOutputStream zipout) throws IOException {
        InputStream stream = JarMaker.class.getResourceAsStream(JAR.JAR_TEMPLATE);
        if (stream == null) {
            throw ioException("jar.make.noTemplate", JAR.JAR_TEMPLATE);
        }
        ZipInputStream zis = new ZipInputStream(stream);
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zipout.putNextEntry(new ZipEntry(entry.getName()));
                IOUtils.copy(zis, zipout);
                zis.closeEntry();
                zipout.closeEntry();
            }
        } finally {
            zis.close();
            stream.close();
        }
    }

    private void writeMeta(Book book, ZipOutputStream zipout, List<MetaItem> items,
                           TextConfig config) throws Exception {
        zipout.putNextEntry(new ZipEntry("0"));
        DataOutput output = new DataOutputStream(zipout);
        output.writeInt(JAR.FILE_HEADER);
        String title = book.getTitle();
        byte[] b = title.getBytes(JAR.META_ENCODING);
        output.writeByte(b.length);
        output.write(b);

        b = String.valueOf(items.size()).getBytes(JAR.META_ENCODING);
        output.writeShort(b.length);
        output.write(b);

        for (MetaItem item : items) {
            String str = item.name + "," + item.size + "," + item.title;
            b = str.getBytes(JAR.META_ENCODING);
            output.writeShort(b.length);
            output.write(b);
        }
        output.writeShort(0);  // what?
        String str;
        TextObject intro = book.getIntro();
        if (intro != null) {
            str = TextRender.renderText(intro, config);
        } else {
            str = "";
        }
        b = str.getBytes(JAR.META_ENCODING);
        output.writeShort(b.length);
        output.write(b);
        zipout.closeEntry();
    }

    static class MetaItem {
        final String name;
        final int size;
        final String title;

        MetaItem(String name, int size, String title) {
            this.name = name;
            this.size = size;
            this.title = title;
        }
    }
}
