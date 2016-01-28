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
import pw.phylame.jem.util.TextObject;
import pw.phylame.jem.formats.common.CommonMaker;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.text.TextUtils;
import pw.phylame.jem.formats.util.text.TextRender;

import java.io.*;

/**
 * <tt>Maker</tt> implement for TXT book.
 */
public class TxtMaker extends CommonMaker<TxtMakeConfig> {
    public TxtMaker() {
        super("txt", TxtMakeConfig.CONFIG_SELF, TxtMakeConfig.class);
    }

    @Override
    public void make(Book book, OutputStream output, TxtMakeConfig config)
            throws IOException, MakerException {
        if (config == null) {
            config = new TxtMakeConfig();
        }
        Writer writer = new OutputStreamWriter(output, config.encoding);
        try {
            writer = new BufferedWriter(writer);
            make(book, writer, config);
        } finally {
            writer.close();
        }
    }

    public void make(Book book, Writer writer, TxtMakeConfig config)
            throws IOException {
        if (config == null) {
            config = new TxtMakeConfig();
        }
        if (!(writer instanceof BufferedWriter)) {
            writer = new BufferedWriter(writer);
        }
        String lineSeparator = config.textConfig.lineSeparator;
        if (TextUtils.isValid(config.headerText)) {
            writer.write(config.headerText + lineSeparator);
        }
        writer.write(book.getTitle() + lineSeparator);
        String author = book.getAuthor();
        if (!author.isEmpty()) {
            writer.write(author + lineSeparator);
        }
        TxtRender txtRender = new TxtRender(writer, config.additionLine,
                lineSeparator);
        try {
            TextObject intro = book.getIntro();
            if (intro != null) {
                if (TextRender.renderText(intro, txtRender, config.textConfig)) {
                    writer.write(lineSeparator);
                }
            }
            writer.write(lineSeparator);
            if (book.isSection()) {
                TextRender.renderBook(book, txtRender, config.textConfig);
            } else {        // book has not sub-parts, then save its content
                TextRender.renderText(book.getContent(), txtRender,
                        config.textConfig);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        if (TextUtils.isValid(config.footerText)) {
            writer.write(config.footerText);
        }
        writer.flush();
    }
}
