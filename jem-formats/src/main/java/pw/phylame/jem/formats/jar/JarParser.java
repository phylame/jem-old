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
import pw.phylame.jem.formats.common.ZipParser;
import pw.phylame.jem.formats.common.NonConfig;
import pw.phylame.jem.formats.util.ZipUtils;
import pw.phylame.jem.formats.util.ParserException;

import java.io.*;
import java.util.zip.ZipFile;

/**
 * <tt>Parser</tt> implement for JAR book.
 */
public class JarParser extends ZipParser<NonConfig> {
    public JarParser() {
        super("jar", null, null);
    }

    @Override
    protected Book parse(ZipFile input, NonConfig config) throws IOException, ParserException {
        return parse0(input);
    }

    private Book parse0(ZipFile zipFile) throws IOException, ParserException {
        Book book = new Book();
        parseMetadata(book, zipFile);
        return book;
    }

    private void parseMetadata(Book book, ZipFile zipFile) throws IOException, ParserException {
        try (InputStream stream = new BufferedInputStream(ZipUtils.openStream(zipFile, "0"))) {
            DataInput input = new DataInputStream(stream);
            if (input.readInt() != JAR.FILE_MAGIC_NUMBER) {
                throw parserException("jar.parse.badMetadata", zipFile.getName());
            }
            book.setTitle(readString(input, 1));

            int count = Integer.parseInt(readString(input, 2));
            for (int i = 0; i < count; ++i) {
                String[] items = readString(input, 2).split(",");
                if (items.length < 3) {
                    throw parserException("jar.parse.badMetadata", zipFile.getName());
                }
                FileObject fb = FileFactory.fromZip(zipFile, items[0], "text/plain");
                book.append(new Chapter(items[2], TextFactory.fromFile(fb, JAR.TEXT_ENCODING)));
            }

            input.skipBytes(2); // what ?
            String str = readString(input, 2);
            if (!str.isEmpty()) {
                book.setIntro(TextFactory.fromString(str));
            }
        }
    }

    private String readString(DataInput input, int size) throws IOException {
        int length = size == 1 ? input.readByte() : input.readShort();
        byte[] b = new byte[length];
        input.readFully(b);
        return new String(b, JAR.METADATA_ENCODING);
    }
}
