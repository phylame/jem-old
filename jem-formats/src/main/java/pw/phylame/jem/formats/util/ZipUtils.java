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

package pw.phylame.jem.formats.util;

import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.TextObject;

import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP utilities.
 */
public class ZipUtils {
    public static void writeFile(FileObject file, ZipOutputStream zipout,
                                 String name) throws IOException {
        zipout.putNextEntry(new ZipEntry(name));
        file.copyTo(zipout);
        zipout.closeEntry();
    }

    /**
     * Writes text content in TextObject to PMAB archive.
     * @param text the TextObject
     * @param zipout PMAB archive stream
     * @param name name of entry to store text content
     * @param encoding encoding to encode text
     * @throws IOException occurs IO errors when writing text
     */
    public static void writeText(TextObject text, ZipOutputStream zipout,
                                 String name, String encoding)
            throws IOException {
        zipout.putNextEntry(new ZipEntry(name));
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        Writer writer = new OutputStreamWriter(zipout, encoding);
        text.writeTo(writer);
        writer.flush();
        zipout.closeEntry();
    }
}
