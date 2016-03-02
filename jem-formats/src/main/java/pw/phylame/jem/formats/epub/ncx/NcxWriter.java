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

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.*;
import pw.phylame.jem.formats.epub.writer.EpubWriter;
import pw.phylame.jem.formats.util.MakerException;
import pw.phylame.jem.formats.util.xml.XmlRender;

/**
 * NCX builder.
 */
public interface NcxWriter {
    void write(Book book, EpubMakeConfig config, XmlRender xmlRender, EpubWriter epubWriter,
               ZipOutputStream zipout) throws IOException, MakerException;

    String getCoverID();

    List<Resource> getResources();

    List<SpineItem> getSpineItems();

    List<GuideItem> getGuideItems();
}
