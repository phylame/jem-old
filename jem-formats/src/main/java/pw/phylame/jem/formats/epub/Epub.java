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

/**
 * Constants and utilities for ePub.
 */
public class EPUB {
    ///// MIME type for ePub /////
    public static final String MIME_FILE = "mimetype";
    public static final String MT_EPUB = "application/epub+zip";

    // container.xml
    public static final String CONTAINER_FILE = "META-INF/container.xml";

    // the Open Packaging Format (OPF)
    public static String OpfFileName = "content.opf";
    public static final String MT_OPF = "application/oebps-package+xml";

    // Dublin Core Metadata Initiative (DCMI)
    public static final String DC_XML_NS = "http://purl.org/dc/elements/1.1/";

    // the Navigation Center eXtended (NCX)
    public static String NcxFileName = "toc.ncx";
    public static final String NcxFileId = "ncx";
    public static final String MT_NCX = "application/x-dtbncx+xml";

    public static final String BOOK_ID_NAME = "book_id";

    public static String getOpsPath(String name, EpubConfig config) {
        return config.opsDir + "/" + name;
    }
}
