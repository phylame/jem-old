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
import java.util.HashMap;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory class for creating <tt>FileObject</tt>.
 */
public class FileFactory {
    private static final Log LOG = LogFactory.getLog(FileFactory.class);

    /**
     * File of builtin MIME mapping
     */
    public static final String MIME_MAPPING_FILE = "mime.properties";

    /**
     * Some known MIME types
     */
    public static final HashMap<String, String> names = new HashMap<String, String>();

    /**
     * Returns the MIME type of specified file name.
     *
     * @param name path name of file
     * @return string of MIME or empty string if <tt>name</tt>
     * is <tt>null</tt> or empty
     */
    public static String getMimeType(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        String ext = FilenameUtils.getExtension(name);
        if (ext.isEmpty()) {
            return FileObject.UNKNOWN_MIME;
        }
        String mime = names.get(ext);
        if (mime != null) {
            return mime;
        } else {
            return new javax.activation.MimetypesFileTypeMap().getContentType(name);
        }
    }

    /**
     * Detects MIME type by file name if not specified mime.
     *
     * @param path path name of file
     * @param mime given mime
     * @return the mime type text
     */
    private static String getOrDetectMime(String path, String mime) {
        if (mime == null || mime.isEmpty()) {
            return getMimeType(path);
        }
        return mime;
    }

    private static class NormalFile extends AbstractFile {
        private File file;

        NormalFile(File file, String mime) throws IOException {
            super(mime);
            if (file == null) {
                throw new NullPointerException("file");
            }
            this.file = file;
            if (!this.file.exists()) {
                throw new IOException("Not such file or directory: " + this.file.getPath());
            }
        }

        @Override
        public String getName() {
            return file.getPath();
        }

        @Override
        public InputStream openStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public byte[] readAll() throws IOException {
            return FileUtils.readFileToByteArray(file);
        }

        @Override
        public long writeTo(OutputStream output) throws IOException {
            return FileUtils.copyFile(file, output);
        }
    }

    private static class InnerZip extends AbstractFile {
        private ZipFile zip;
        private String name;

        InnerZip(ZipFile zipFile, String entry, String mime) throws IOException {
            super(mime);
            if (zipFile == null) {
                throw new NullPointerException("zipFile");
            }
            zip = zipFile;
            if (entry == null) {
                throw new NullPointerException("entry");
            }
            name = entry;
            if (zip.getEntry(name) == null) {
                throw new IOException("Not such entry in ZIP: " + name);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openStream() throws IOException {
            ZipEntry entry = zip.getEntry(name);
            assert entry != null;
            return zip.getInputStream(entry);
        }

        @Override
        public String toString() {
            return "zip://" + zip.getName() + "!" + name + ";mime=" + getMime();
        }
    }

    private static class BlockFile extends AbstractFile {
        private String name;
        private RandomAccessFile file;
        private long offset, size, oldOffset = -1;

        BlockFile(String name, RandomAccessFile file, long offset, long size, String mime)
                throws IOException {
            super(mime);
            if (name == null) {
                throw new NullPointerException("name");
            }
            this.name = name;
            if (file == null) {
                throw new NullPointerException("file");
            }
            this.file = file;
            this.offset = offset;
            this.size = size;
            if (this.size > (this.file.length() - this.offset)) {
                throw new IOException("Source size < " + this.size);
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InputStream openStream() throws IOException {
            oldOffset = file.getFilePointer();
            file.seek(offset);
            return new RAFInputStream(file, size);
        }

        @Override
        public byte[] readAll() throws IOException {
            oldOffset = file.getFilePointer();
            file.seek(offset);
            byte[] buf = new byte[(int) size];
            int n = file.read(buf);
            reset();
            if (n < size) {
                return java.util.Arrays.copyOf(buf, n);
            } else {
                return buf;
            }
        }

        @Override
        public long writeTo(OutputStream output) throws IOException {
            oldOffset = file.getFilePointer();
            file.seek(offset);
            byte[] buf = new byte[(int) size];
            int n = file.read(buf);
            output.write(buf);
            reset();
            return n;
        }

        @Override
        public void reset() throws IOException {
            if (oldOffset != -1) {
                file.seek(oldOffset);
            }
            oldOffset = -1;
        }

        @Override
        public String toString() {
            return String.format("block://%s;offset=%d;size=%d;mime=%s",
                    getName(), offset, size, getMime());
        }
    }

    private static class URLFile extends AbstractFile {
        private URL url;

        URLFile(URL url, String mime) {
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
        public byte[] readAll() throws IOException {
            return IOUtils.toByteArray(url);
        }

        @Override
        public long writeTo(OutputStream output) throws IOException {
            byte[] b = readAll();
            output.write(b);
            return b.length;
        }

        @Override
        public String toString() {
            return url.toString() + ";mime=" + getMime();
        }
    }

    private static class ByteFile extends AbstractFile {
        private String name;
        private byte[] buf;

        ByteFile(String name, byte[] buf, String mime) {
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
            byte[] b = new byte[buf.length];
            System.arraycopy(buf, 0, b, 0, b.length);
            return b;
        }

        @Override
        public long writeTo(OutputStream output) throws IOException {
            output.write(buf);
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

    public static FileObject fromZip(ZipFile zipFile, String entry,
                                     String mime) throws IOException {
        return new InnerZip(zipFile, entry, getOrDetectMime(entry, mime));
    }

    public static FileObject fromBlock(String name, RandomAccessFile file,
                                       long offset, long size,
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

    /**
     * Loads some known MIMEs from file.
     */
    private static void loadBuiltinMime() {
        URL url = FileFactory.class.getResource(MIME_MAPPING_FILE);
        if (url == null) {       // not found file
            LOG.debug("not found MIME mapping: " + MIME_MAPPING_FILE);
            return;
        }
        LineIterator iterator = new LineIterator(url, null);
        iterator.commentLabel = "#";
        iterator.trimSpace = true;
        iterator.skipEmpty = true;

        while (iterator.hasNext()) {
            String line = iterator.next();
            int index = line.indexOf('=');
            if (index < 0) {
                continue;
            }
            String name = line.substring(0, index).trim();
            if (name.isEmpty()) {
                continue;
            }
            String mime = line.substring(index + 1).trim();
            if (mime.isEmpty()) {
                continue;
            }
            names.put(name, mime);
        }
    }

    static {
        loadBuiltinMime();
    }
}
