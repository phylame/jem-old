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

import pw.phylame.tools.file.FileObject;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Constants and utilities for ePub.
 */
public class EPUB {
    ///// MIME type for ePub /////
    public static final String MIME_FILE = "mimetype";

    // container.xml
    public static final String CONTAINER_FILE = "META-INF/container.xml";

    // the Open Packaging Format (OPF)
    public static final String OPF_FILE = "content.opf";

    // Dublin Core Metadata Initiative (DCMI)
    public static final String DC_XML_NS = "http://purl.org/dc/elements/1.1/";

    // NCX (the Navigation Center eXtended)
    public static final String NCX_FILE    = "toc.ncx";
    public static final String NCX_FILE_ID = "ncx";

    public static final String BOOK_ID_NAME = "book_id";

    // required media type
    public static final String MT_EPUB  = "application/epub+zip";
    public static final String MT_OPF   = "application/oebps-package+xml";
    public static final String MT_NCX   = "application/x-dtbncx+xml";
    public static final String MT_CSS   = "text/css";
    public static final String MT_XHTML = "application/xhtml+xml";

    public static final String COVER_NAME = "cover";
    public static final String COVER_FILE_ID = "cover-image";

    public static final String CSS_FILE = "style.css";
    public static final String CSS_FILE_ID = "main-css";

    public static final String DUOKAN_FULL_SCREEN = "duokan-page-fullscreen";

    // content
    public static final String COVER_PAGE_FILE = "cover.xhtml";
    public static final String COVER_PAGE_ID   = "cover-page";

    public static final String INTRO_PAGE_FILE = "intro.xhtml";
    public static final String INTRO_PAGE_ID   = "intro-page";

    public static final String INFO_PAGE_FILE = "info.xhtml";
    public static final String INFO_PAGE_ID   = "info-page";

    public static final String TOC_PAGE_FILE = "toc.xhtml";
    public static final String TOC_PAGE_ID   = "toc-page";

    public static String getOpsPath(String name, EpubConfig config) {
        return config.opsDir + "/" + name;
    }

    public static void writeToOps(FileObject fb, String href, ZipOutputStream zipout, EpubConfig config)
            throws IOException {
        String name = getOpsPath(href, config);
        zipout.putNextEntry(new ZipEntry(name));
        fb.copyTo(zipout);
        zipout.closeEntry();
    }
}
