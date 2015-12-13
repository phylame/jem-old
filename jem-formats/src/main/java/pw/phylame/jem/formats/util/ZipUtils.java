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

import java.io.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

/**
 * Utilities operations for ZIP..
 */
public final class ZipUtils {
    private ZipUtils() {
    }

    public static InputStream openStream(ZipFile zipFile, String name)
            throws IOException {
        ZipEntry zipEntry = zipFile.getEntry(name);
        if (zipEntry == null) {
            throw ExceptionFactory.ioException("error.zip.noEntry",
                    name, zipFile.getName());
        }
        return zipFile.getInputStream(zipEntry);
    }

    public static String readString(ZipFile zipFile, String name,
                                    String encoding) throws IOException {
        InputStream stream = openStream(zipFile, name);
        try {
            return IOUtils.toString(stream, encoding);
        } finally {
            stream.close();
        }
    }

    public static void writeString(String str, String name, String encoding,
                                   ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(name));
        zipout.write(str.getBytes(encoding));
        zipout.closeEntry();
    }

    public static void writeFile(FileObject file, String name,
                                 ZipOutputStream zipout) throws IOException {
        zipout.putNextEntry(new ZipEntry(name));
        file.writeTo(zipout);
        zipout.closeEntry();
    }

    /**
     * Writes text content in TextObject to PMAB archive.
     *
     * @param text     the TextObject
     * @param name     name of entry to store text content
     * @param encoding encoding to encode text
     * @param zipout   PMAB archive stream
     * @throws NullPointerException if arguments contain <tt>null</tt>
     * @throws IOException          if occurs IO errors when writing text
     */
    public static void writeText(TextObject text, String name, String encoding,
                                 ZipOutputStream zipout) throws IOException {
        if (encoding == null) {
            throw new NullPointerException("encoding");
        }
        zipout.putNextEntry(new ZipEntry(name));
        Writer writer = new BufferedWriter(new OutputStreamWriter(zipout, encoding));
        text.writeTo(writer);
        writer.flush();
        zipout.closeEntry();
    }
}
