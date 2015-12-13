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

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.ParserException;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.common.ZipBookParser;

import java.io.*;
import java.util.zip.ZipFile;

/**
 * <tt>Parser</tt> implement for JAR book.
 */
public class JarParser extends ZipBookParser<NonConfig> {
    public JarParser() {
        super("jar");
    }

    @Override
    public Book parse(ZipFile input, NonConfig config) throws IOException,
            ParserException {
        return parse(input);
    }

    public Book parse(ZipFile zipFile) throws IOException, ParserException {
        Book book = new Book();
        readMeta(book, zipFile);
        return book;
    }

    private void readMeta(Book book, ZipFile zipFile) throws IOException,
            ParserException {
        InputStream stream = new BufferedInputStream(ZipUtils.openStream(zipFile, "0"));
        try {
            DataInput input = new DataInputStream(stream);
            if (input.readInt() != JAR.FILE_HEADER) {
                throw parserException("jar.parse.invalidMeta", zipFile.getName());
            }
            int length = input.readByte();
            byte[] buf = new byte[length];
            input.readFully(buf);
            book.setTitle(new String(buf, JAR.META_ENCODING));

            int chapterCount = Integer.parseInt(readString(input));
            for (int i = 0; i < chapterCount; ++i) {
                String str = readString(input);
                String[] items = str.split(",");
                if (items.length < 3) {
                    throw parserException("jar.parse.invalidMeta", zipFile.getName());
                }
                FileObject fb = FileFactory.fromZip(zipFile, items[0], "text/plain");
                Chapter chapter = new Chapter(items[2],
                        TextFactory.fromFile(fb, JAR.TEXT_ENCODING));
                book.append(chapter);
            }

            input.skipBytes(2); // what ?
            String str = readString(input);
            if (!str.isEmpty()) {
                book.setIntro(TextFactory.fromString(str));
            }
        } finally {
            stream.close();
        }
    }

    private String readString(DataInput input) throws IOException {
        int length = input.readShort();
        byte[] buf = new byte[length];
        input.readFully(buf);
        return new String(buf, JAR.META_ENCODING);
    }
}
