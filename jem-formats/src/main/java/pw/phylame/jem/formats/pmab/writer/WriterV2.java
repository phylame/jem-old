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

package pw.phylame.jem.formats.pmab.writer;

import org.dom4j.Document;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.formats.pmab.PmabConfig;

import java.util.zip.ZipOutputStream;

/**
 * PBM and PBC writer for PMAB 2.x.
 */
public class WriterV2 {
    public static void writePBM(Book book, Document doc, ZipOutputStream zipOut, PmabConfig config) {
        doc.addDocType("pbm", null, null);
    }

    public static void writePBC(Book book, Document doc, ZipOutputStream zipOut, PmabConfig config) {
        doc.addDocType("pbc", null, null);
    }
}
