/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.tools.file;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Factory to create <tt>FileObject</tt>.
 */
public final class FileFactory {
    private static String getMime(String path, String mime) {
        if (mime == null || "".equals(mime)) {
            return FileUtils.getMimeType(path);
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
        /**
         * Returns the name of file content.
         */
        @Override
        public String getName() {
            return file.getPath();
        }
        /**
         * Opens an {@code InputStream} for reading file content.
         *
         * @return the <tt>InputStream</tt>
         * @throws java.io.IOException occur IO errors
         */
        @Override
        public InputStream openInputStream() throws IOException {
            return new FileInputStream(file);
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
        /**
         * Returns the name of file content.
         *
         * @return the name
         */
        @Override
        public String getName() {
            return entryName;
        }

        /**
         * Opens an {@code InputStream} for reading file content.
         *
         * @return the <tt>InputStream</tt>
         * @throws java.io.IOException occur IO errors
         */
        @Override
        public InputStream openInputStream() throws IOException {
            ZipEntry zipEntry = zipFile.getEntry(entryName);
            assert zipEntry != null;
            return zipFile.getInputStream(zipEntry);
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
        /**
         * Returns the name of file content.
         *
         * @return the name
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * Opens an {@code InputStream} for reading file content.
         *
         * @return the <tt>InputStream</tt>
         * @throws java.io.IOException occur IO errors
         */
        @Override
        public InputStream openInputStream() throws IOException {
            oldOffset = file.getFilePointer();
            file.seek(offset);
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return file.read();
                }
            };
        }

        /**
         * Resets file object status to last status.
         * The method should be used after using <tt>openInputStream</tt>.
         */
        @Override
        public void reset() throws IOException {
            if (oldOffset != -1) {
                file.seek(oldOffset);
            }
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
        /**
         * Returns the name of file content.
         *
         * @return the name
         */
        @Override
        public String getName() {
            return url.getPath();
        }

        /**
         * Opens an {@code InputStream} for reading file content.
         *
         * @return the <tt>InputStream</tt>
         * @throws java.io.IOException occur IO errors
         */
        @Override
        public InputStream openInputStream() throws IOException {
            return url.openStream();
        }
    }

    public static FileObject getFile(File file, String mime) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        return new NormalFile(file, getMime(file.getPath(), mime));
    }

    public static FileObject getFile(ZipFile zipFile, String entryName, String mime) throws IOException {
        if (entryName == null) {
            throw new NullPointerException("entryName");
        }
        return new InnerZip(zipFile, entryName, getMime(entryName, mime));
    }

    public static FileObject getFile(String name, RandomAccessFile file, long offset, long size,
                                     String mime) throws IOException {
        if (name == null) {
            throw new NullPointerException("name");
        }
        return new AreaFile(name, file, offset, size, getMime(name, mime));
    }

    public static FileObject getFile(URL url, String mime) {
        if (url == null) {
            throw new NullPointerException("url");
        }
        return new URLFile(url, getMime(url.getPath(), mime));
    }
}
