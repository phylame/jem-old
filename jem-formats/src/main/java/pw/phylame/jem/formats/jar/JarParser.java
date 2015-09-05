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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.phylame.jem.core.Parser;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.JemException;

import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextFactory;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * <tt>Parser</tt> implement for JAR book.
 */
public class JarParser implements Parser {
    private static Log LOG = LogFactory.getLog(JarParser.class);

    @Override
    public String getName() {
        return "txt";
    }

    @Override
    public Book parse(File file, Map<String, Object> kw)
            throws IOException, JemException {
        final ZipFile jarFile = new ZipFile(file);
        Book book = parse(jarFile);
        book.registerCleanup(new Chapter.Cleanable() {
            @Override
            public void clean(Chapter part) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    LOG.debug("cannot close JAR source: "+jarFile.getName(), e);
                }
            }
        });
        return book;
    }

    public Book parse(ZipFile zipFile) throws IOException, JemException {
        Book book = new Book();
        readMeta(book, zipFile);
        return book;
    }

    private void readMeta(Book book, ZipFile zipFile)
            throws IOException, JemException {
        ZipEntry entry = zipFile.getEntry("0");
        if (entry == null) {
            throw new ParserException("Not found '0' in JAR book", getName());
        }
        InputStream stream = new BufferedInputStream(zipFile.getInputStream(entry));
        DataInput input = new DataInputStream(stream);
        if (input.readInt() != JAR.FILE_HEADER) {
            throw new ParserException("Unsupported JAR book: magic number", getName());
        }
        int length = input.readByte();
        byte[] buf = new byte[length];
        input.readFully(buf);
        book.setTitle(new String(buf, JAR.META_ENCODING));

        length = input.readShort();
        buf = new byte[length];
        input.readFully(buf);
        int chapterCount = Integer.parseInt(new String(buf, JAR.META_ENCODING));

        for (int i = 0; i < chapterCount; ++i) {
            length = input.readShort();
            buf = new byte[length];
            input.readFully(buf);
            String str = new String(buf, JAR.META_ENCODING);
            String[] items = str.split(",");
            if (items.length < 3) {
                throw new ParserException("Invalid JAR book: bad chapter item",
                        getName());
            }
            Chapter chapter = new Chapter(items[2]);
            FileObject fb = FileFactory.fromZip(zipFile, items[0], null);
            chapter.setSource(TextFactory.fromFile(fb, JAR.TEXT_ENCODING));
            book.append(chapter);
        }

        length = input.readShort();
        buf = new byte[length];
        input.readFully(buf);
        book.setIntro(TextFactory.fromString(new String(buf, JAR.META_ENCODING)));
    }
}
