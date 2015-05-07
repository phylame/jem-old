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
import pw.phylame.jem.core.Part;
import pw.phylame.jem.core.Maker;
import pw.phylame.jem.util.JemException;
import pw.phylame.tools.TextObject;

import java.io.*;
import java.util.Map;
import java.util.List;

/**
 * <tt>Maker</tt> implement for TXT book.
 */
public class TxtMaker implements Maker {
    @Override
    public String getName() {
        return "txt";
    }

    @Override
    public void make(Book book, File file, Map<String, Object> kw) throws IOException, JemException {
        TxtConfig config = new TxtConfig();
        if (kw != null && kw.size() > 0) {
            Object o = kw.get("txt_encoding");
            if (o != null) {
                if (o instanceof String) {
                    config.encoding = (String) o;
                } else {
                    throw new JemException("Invalid txt_encoding string: " + o);
                }
            }
            o = kw.get("txt_linefeed");
            if (o != null) {
                if (o instanceof String) {
                    config.lineSeparator = (String) o;
                } else {
                    throw new JemException("Invalid txt_linefeed string: " + o);
                }
            }
            o = kw.get("txt_para_prefix");
            if (o != null) {
                if (o instanceof String) {
                    config.paragraphPrefix = (String) o;
                } else {
                    throw new JemException("Invalid txt_para_prefix string: " + o);
                }
            }
        }
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
            for (Part sub: book) {
                writeChapter(sub, writer, config);
            }
        } else {        // book has not sub-parts, then save its content
            writeContent(book, writer, config);
        }
        writer.flush();
    }

    private void writeChapter(Part part, Writer writer, TxtConfig config) throws IOException {
        writer.write(config.lineSeparator+part.getTitle()+config.lineSeparator);
        Object o = part.getAttribute(Chapter.INTRO, null);
        if (o instanceof TextObject) {  // valid intro
            writeIntro((TextObject) o, writer, config);
        }
        if (! part.isSection()) {
            writeContent(part, writer, config);
        } else {
            for (Part sub: part) {
                writeChapter(sub, writer, config);
            }
        }
    }

    private void writeIntro(TextObject intro, Writer writer, TxtConfig config) throws IOException {
        List<String> lines = intro.getLines();
        for (String line : lines) {
            writer.write(config.paragraphPrefix+line.trim()+config.lineSeparator);
        }
        if (lines.size() > 0) {
            writer.write(config.introSeparator+config.lineSeparator);
        }
    }

    private void writeContent(Part part, Writer writer, TxtConfig config) throws IOException {
        List<String> lines = part.getLines();
        for (String line : lines) {
            writer.write(config.paragraphPrefix+line.trim()+config.lineSeparator);
        }
    }
}
