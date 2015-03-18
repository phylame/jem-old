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

package pw.phylame.jem.formats.pmab;

import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

/**
 * Configuration for make PMAB.
 */
public class PmabConfig {
    // directory
    public String textDir = "text", imageDir = "images", extraDir = "extras";

    // XML style
    public String xmlEncoding = "UTF-8", xmlIndent = "  ",
            xmlLineSeparator = System.getProperty("line.separator");

    // encoding for text content
    public String textEncoding = System.getProperty("file.encoding");

    // output version
    public String pbmVersion = "3.0", pbcVersion = "3.0";

    // ZIP compression
    public int zipMethod = Deflater.DEFLATED, zipLevel = Deflater.DEFAULT_COMPRESSION;
}