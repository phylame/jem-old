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

package pw.phylame.jem.formats.epub;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.common.ZipBookMaker;
import pw.phylame.jem.formats.epub.writer.EpubWriter;
import pw.phylame.jem.formats.epub.writer.EpubWriterFactory;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.ZipUtils;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * ePub e-book maker.
 */
public class EpubMaker extends ZipBookMaker<EpubMakeConfig> {
    public EpubMaker() {
        super("epub", EpubMakeConfig.class, EpubMakeConfig.CONFIG_SELF);
    }

    @Override
    public void make(Book book, ZipOutputStream zipout, EpubMakeConfig config)
            throws IOException, MakerException {
        if (config == null) {
            config = new EpubMakeConfig();
        }
        EpubWriter writer = EpubWriterFactory.getWriter(config.version);
        if (writer == null) {
            throw makerException("epub.make.unsupportedVersion", config.version);
        }
        writeMIME(zipout);
        writer.write(book, config, zipout);
    }

    private void writeMIME(ZipOutputStream zipout) throws IOException {
        ZipUtils.writeString(EPUB.MT_EPUB, EPUB.MIME_FILE, "ASCII", zipout);
    }
}
