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

package pw.phylame.jem.formats.txt;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;
import pw.phylame.jem.formats.util.TextUtilities;
import pw.phylame.jem.formats.util.ExceptionFactory;
import pw.phylame.jem.util.TextObject;

import java.io.*;
import java.util.Map;

/**
 * <tt>Maker</tt> implement for TXT book.
 */
public class TxtMaker implements Maker {
    public static final String KEY_TEXT_ENCODING = "txt_encoding";
    public static final String KEY_LINE_FEED = "txt_linefeed";
    public static final String KEY_PARAGRAPH_PREFIX = "txt_para_prefix";

    @Override
    public String getName() {
        return "txt";
    }

    protected TxtConfig parseConfig(Map<String, Object> kw) throws JemException {
        TxtConfig config = new TxtConfig();
        if (kw != null && kw.size() > 0) {
            Object o = kw.get(KEY_TEXT_ENCODING);
            if (o != null) {
                if (o instanceof String) {
                    config.encoding = (String) o;
                } else {
                    throw ExceptionFactory.forInvalidStringArgument(KEY_TEXT_ENCODING, o);
                }
            }
            o = kw.get(KEY_LINE_FEED);
            if (o != null) {
                if (o instanceof String) {
                    config.lineSeparator = (String) o;
                } else {
                    throw ExceptionFactory.forInvalidStringArgument(KEY_LINE_FEED, o);
                }
            }
            o = kw.get(KEY_PARAGRAPH_PREFIX);
            if (o != null) {
                if (o instanceof String) {
                    config.paragraphPrefix = (String) o;
                } else {
                    throw ExceptionFactory.forInvalidStringArgument(KEY_PARAGRAPH_PREFIX, o);
                }
            }
        }
        return config;
    }

    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        TxtConfig config = parseConfig(kw);
        OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
        Writer writer = new BufferedWriter(new OutputStreamWriter(output, config.encoding));
        make(book, writer, config);
        writer.close();
    }

    public void make(Book book, Writer writer, TxtConfig config) throws IOException {
        writer.write(book.getTitle()+config.lineSeparator);
        String author = book.getAuthor();
        if (! "".equals(author)) {
            writer.write(author+config.lineSeparator);
        }
        TextObject intro = book.getIntro();
        if (intro != null) {
            writeIntro(intro, writer, config);
        }
        if (book.isSection()) {
            for (Chapter sub: book) {
                writeChapter(sub, writer, config);
            }
        } else {        // book has not sub-parts, then save its content
            writeContent(book, writer, config);
        }
        writer.flush();
    }

    private void writeChapter(Chapter part, Writer writer, TxtConfig config) throws IOException {
        writer.write(config.lineSeparator+part.getTitle()+config.lineSeparator);
        Object o = part.getAttribute(Book.INTRO, null);
        if (o instanceof TextObject) {  // valid intro
            writeIntro((TextObject) o, writer, config);
        }
        if (! part.isSection()) {
            writeContent(part, writer, config);
        } else {
            for (Chapter sub: part) {
                writeChapter(sub, writer, config);
            }
        }
    }

    private void writeIntro(TextObject intro, Writer writer, TxtConfig config) throws IOException {
        String[] lines = TextUtilities.plainLines(intro);
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            writer.write(config.paragraphPrefix+line.trim()+config.lineSeparator);
        }
        if (lines.length > 0) {
            writer.write(config.introSeparator+config.lineSeparator);
        }
    }

    private void writeContent(Chapter part, Writer writer, TxtConfig config) throws IOException {
        String[] lines = TextUtilities.plainLines(part.getSource());
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            writer.write(config.paragraphPrefix+line.trim()+config.lineSeparator);
        }
    }
}
