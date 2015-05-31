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

package pw.phylame.jem.formats.epub.opf;

import org.dom4j.Document;
import org.dom4j.Element;

import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.EpubConfig;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * OPF builder.
 */
public interface OpfBuilder {

    Element addManifestItem(String id, String href, String mediaType);

    Element addSpineItem(String idref, boolean linear, String properties);

    Element addGuideItem(String href, String type, String title);

    /**
     * Returns the cover image href in OPS.
     * @return the href
     */
    String getCover();

    Document make(Book book, String uuid, ZipOutputStream zipout, EpubConfig config) throws IOException;
}
