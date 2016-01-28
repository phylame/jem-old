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

package pw.phylame.jem.util;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipFile;

import pw.phylame.jem.core.Jem;

/**
 * Factory class for creating <tt>FileObject</tt>.
 */
public class FileFactory {

    /**
     * Detects MIME type by file name if not specified.
     *
     * @param path path name of file
     * @param mime given mime
     * @return the mime type text
     */
    private static String getOrDetectMime(String path, String mime) {
        if (mime == null || mime.isEmpty()) {
            return IOUtils.getMimeType(path);
        }
        return mime;
    }

    private static class NormalFile extends AbstractFile {
        static {
            Jem.variantTypes.put(NormalFile.class, Jem.FILE);
        }

        private final File file;

        private NormalFile(File file, String mime) throws IOException {
            super(mime);
            if (file == null) {
                throw new NullPointerException("file");
            }
            if (!file.exists()) {
                throw new FileNotFoundException("Not such file: " + file);
            }
            if (file.isDirectory()) {
                throw new IOException("Require file but found directory: " + file);
            }
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getPath();
        }

        @Override
        public FileInputStream openStream() throws IOException {
            return new FileInputStream(file);
        }
    }

    private static class EntryFile extends AbstractFile {
        static {
            Jem.variantTypes.put(EntryFile.class, Jem.FILE);
        }

        private final ZipFile zip;
        private final String entry;

        private EntryFile(ZipFile zipFile, String entryName, String mime) throws IOException {
            super(mime);
            if (zipFile == null) {
                throw new NullPointerException("zipFile");
            }

            if (entryName == null) {
                throw new NullPointerException("entryName");
            }
            if (zipFile.getEntry(entryName) == null) {
                throw new IOException("Not such entry in ZIP: " + entryName);
            }
            zip = zipFile;
            entry = entryName;
        }

        @Override
        public String getName() {
            return entry;
        }

        @Override
        public InputStream openStream() throws IOException {
            return zip.getInputStream(zip.getEntry(entry));
        }

        @Override
        public String toString() {
            return "zip://" + zip.getName() + '!' + super.toString();
        }
    }

    public static class BlockFile extends AbstractFile {
        static {
            Jem.variantTypes.put(BlockFile.class, Jem.FILE);
        }

        private final String name;
        private final RandomAccessFile file;

        public long offset, size;

        public BlockFile(String name, RandomAccessFile file, long offset, long size, String mime)
                throws IOException {
            super(mime);
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (file == null) {
                throw new NullPointerException("file");
            }
            if (size > (file.length() - offset)) {
                throw new IOException("Source size < " + size);
            }
            this.name = name;
            this.file = file;
            this.offset = offset;
            this.size = size;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openStream() throws IOException {
            file.seek(offset);
            return new RAFInputStream(file, size);
        }

        @Override
        public byte[] readAll() throws IOException {
            file.seek(offset);
            byte[] buf = new byte[(int) size];
            int n = file.read(buf);
            if (n < size) {
                return Arrays.copyOf(buf, n);
            } else {
                return buf;
            }
        }

        @Override
        public int writeTo(OutputStream out) throws IOException {
            file.seek(offset);
            return IOUtils.copy(file, out, (int) size);
        }

        @Override
        public String toString() {
            return String.format("block://%s;offset=%d;size=%d;mime=%s", getName(), offset, size, getMime());
        }
    }

    private static class URLFile extends AbstractFile {
        static {
            Jem.variantTypes.put(URLFile.class, Jem.FILE);
        }

        private final URL url;

        private URLFile(URL url, String mime) {
            super(mime);
            if (url == null) {
                throw new NullPointerException("url");
            }
            this.url = url;
        }

        @Override
        public String getName() {
            return url.getPath();
        }

        @Override
        public InputStream openStream() throws IOException {
            return url.openStream();
        }

        @Override
        public String toString() {
            return url.toString() + ";mime=" + getMime();
        }
    }

    private static class ByteFile extends AbstractFile {
        static {
            Jem.variantTypes.put(ByteFile.class, Jem.FILE);
        }

        private final String name;
        private final byte[] buf;

        private ByteFile(String name, byte[] buf, String mime) {
            super(mime);
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.buf = (buf != null) ? buf : new byte[0];
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openStream() throws IOException {
            return new ByteArrayInputStream(buf);
        }

        @Override
        public byte[] readAll() throws IOException {
            return Arrays.copyOf(buf, buf.length);
        }

        @Override
        public int writeTo(OutputStream out) throws IOException {
            out.write(buf);
            return buf.length;
        }

        @Override
        public String toString() {
            return "bytes://" + super.toString();
        }
    }

    public static FileObject fromFile(File file, String mime) throws IOException {
        return new NormalFile(file, getOrDetectMime(file.getPath(), mime));
    }

    public static FileObject fromZip(ZipFile zipFile, String entry, String mime) throws IOException {
        return new EntryFile(zipFile, entry, getOrDetectMime(entry, mime));
    }

    public static BlockFile fromBlock(String name, RandomAccessFile file, long offset, long size,
                                      String mime) throws IOException {
        return new BlockFile(name, file, offset, size, getOrDetectMime(name, mime));
    }

    public static FileObject fromURL(URL url, String mime) {
        return new URLFile(url, getOrDetectMime(url.getPath(), mime));
    }

    public static FileObject fromBytes(String name, byte[] bytes, String mime) {
        return new ByteFile(name, bytes, getOrDetectMime(name, mime));
    }

    private static FileObject EMPTY_FILE;

    public static FileObject emptyFile() {
        if (EMPTY_FILE == null) {
            EMPTY_FILE = new ByteFile("_empty_", new byte[0], FileObject.UNKNOWN_MIME);
        }
        return EMPTY_FILE;
    }
}
