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
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.epub.EpubConfig;
import pw.phylame.jem.formats.epub.opf.OpfBuilder;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * NCX builder.
 */
public interface NcxBuilder {
    Document make(Book book, String uuid, ZipOutputStream zipout, OpfBuilder opfBuilder, EpubConfig config) throws IOException;
}
