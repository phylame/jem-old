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

package pw.phylame.jem.formats.epub.ncx;

import org.dom4j.Document;
import pw.phylame.jem.formats.epub.EPUB;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.util.XmlUtils;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides HTML writer.
 */
public abstract class AbstractNcxBuilder implements NcxBuilder {
    public void writeHtmlPage(Document page, ZipOutputStream zipout, String href, EpubConfig config)
            throws IOException {
        zipout.putNextEntry(new ZipEntry(EPUB.getOpsPath(href, config)));
        XmlUtils.writeXML(page, zipout, config.htmlEncoding, config.htmlIndent, "\r\n");
        zipout.closeEntry();
    }
}
