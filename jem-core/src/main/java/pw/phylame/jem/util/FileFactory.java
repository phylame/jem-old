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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Factory class to create <tt>FileObject</tt>.
 */
public class FileFactory {
    private static Log                     LOG   = LogFactory.getLog(FileFactory.class);

    /** Some known MIME types */
    private static HashMap<String, String> MIMEs = new HashMap<String, String>();

    /** File of builtin MIME mapping */
    public static final String MIME_MAPPING_FILE = "mime.properties";

    /** Loads some known MIMEs from file. */
    static void loadBuiltinMime() {
        Properties prop = new Properties();
        InputStream in =
                FileFactory.class.getResourceAsStream(MIME_MAPPING_FILE);
        if (in == null) {       // not found file
            LOG.trace("not found MIME mapping: "+MIME_MAPPING_FILE);
            return;
        }
        try {
            prop.load(in);
        } catch (IOException e) {
            LOG.debug("failed to load MIME mapping", e);
        }
        for (String fmt : prop.stringPropertyNames()) {
            MIMEs.put(fmt, prop.getProperty(fmt));
        }
    }

    static {
        loadBuiltinMime();
    }

    /**
     * Returns the MIME type of specified file name.
     * @param name path name of file
     * @return string of MIME.
     */
    public static String getMimeType(String name) {
        if (name == null || name.equals("")) {
            return "";
        }
        String mime = MIMEs.get(FilenameUtils.getExtension(name));
        if (mime != null) {
            return mime;
        } else {
            return new javax.activation.MimetypesFileTypeMap().getContentType(name);
        }
    }

    /**
     * Detects MIME type by file name if not specified mime.
     * @param path path name of file
     * @param mime given mime
     * @return the mime type text
     */
    private static String getOrDetectMime(String path, String mime) {
        if (mime == null || "".equals(mime)) {
            return getMimeType(path);
        }
        return mime;
    }

    private static class NormalFile extends FileObject {
        private File file;

        public NormalFile(File file, String mime) throws IOException {
            super(mime);
            if (file == null) {
                throw new NullPointerException("file");
            }
            if (! file.exists()) {
                throw new IOException("Not such file or directory: "+file.getPath());
            }
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getPath();
        }

        @Override
        public long available() throws IOException {
            return file.length();
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return new FileInputStream(file);
        }

        @Override
        public String toString() {
            return "file:///" + file.getAbsolutePath();
        }
    }

    private static class InnerZip extends FileObject {
        private ZipFile zipFile;
        private String entryName;

        public InnerZip(ZipFile zipFile, String name, String mime) throws IOException {
            super(mime);
            if (zipFile == null) {
                throw new NullPointerException("zipFile");
            }
            if (name == null) {
                throw new NullPointerException("name");
            }
            if (zipFile.getEntry(name) == null) {
                throw new IOException("Not found entry "+name+" in ZIP");
            }
            this.zipFile = zipFile;
            entryName = name;
        }

        @Override
        public String getName() {
            return entryName;
        }

        @Override
        public long available() throws IOException {
            ZipEntry zipEntry = zipFile.getEntry(entryName);
            assert zipEntry != null;
            return zipEntry.getSize();
        }

        @Override
        public InputStream openInputStream() throws IOException {
            ZipEntry zipEntry = zipFile.getEntry(entryName);
            assert zipEntry != null;
            return zipFile.getInputStream(zipEntry);
        }

        @Override
        public String toString() {
            return "zip://"+zipFile.getName()+"!"+getName();
        }
    }

    private static class AreaFile extends FileObject {
        private String name;
        private RandomAccessFile file;
        private long offset, size, oldOffset = -1;

        public AreaFile(String name, RandomAccessFile file, long offset, long size, String mime)
                throws IOException {
            super(mime);
            if (file == null) {
                throw new NullPointerException("file");
            }
            if (offset <0 || offset >= file.length()) {
                throw new IOException("Invalid offset: "+offset);
            }
            if (size > (file.length() - offset)) {
                throw new IOException("Available size lesser than "+size);
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
        public InputStream openInputStream() throws IOException {
            oldOffset = file.getFilePointer();
            file.seek(offset);
            return new InputStream() {
                private long readBytes = 0;

                @Override
                public int read() throws IOException {
                    if (readBytes++ >= size) {
                        return -1;
                    }
                    return file.read();
                }
            };
        }

        @Override
        public void reset() throws IOException {
            if (oldOffset != -1) {
                file.seek(oldOffset);
            }
        }

        @Override
        public long available() throws IOException {
            return size;
        }

        @Override
        public String toString() {
            return String.format("block %s <offset=%d; size=%d>", getName(), offset, size);
        }
    }

    private static class URLFile extends FileObject {
        private URL url;

        public URLFile(URL url, String mime) {
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
        public InputStream openInputStream() throws IOException {
            return url.openStream();
        }

        @Override
        public String toString() {
            return url.toString();
        }
    }

    public static FileObject fromFile(File file, String mime) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        return new NormalFile(file, getOrDetectMime(file.getPath(), mime));
    }

    public static FileObject fromZip(ZipFile zipFile, String entryName,
                                     String mime) throws IOException {
        if (entryName == null) {
            throw new NullPointerException("entryName");
        }
        return new InnerZip(zipFile, entryName, getOrDetectMime(entryName, mime));
    }

    public static FileObject fromBlock(String name, RandomAccessFile file,
                                       long offset, long size,
                                       String mime) throws IOException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return new AreaFile(name, file, offset, size, getOrDetectMime(name, mime));
    }

    public static FileObject fromURL(URL url, String mime) {
        if (url == null) {
            throw new NullPointerException("url");
        }
        return new URLFile(url, getOrDetectMime(url.getPath(), mime));
    }
}
