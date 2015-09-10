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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory class for creating <tt>FileObject</tt>.
 */
public class FileFactory {
    private static Log LOG = LogFactory.getLog(FileFactory.class);
    
    /** Some known MIME types */
    private static HashMap<String, String> MIMEs = new HashMap<String, String>();

    /** File of builtin MIME mapping */
    public static final String MIME_MAPPING_FILE = "mime.properties";

    /** Loads some known MIMEs from file. */
    private static void loadBuiltinMime() {
        Properties prop = new Properties();
        InputStream in = FileFactory.class.getResourceAsStream(MIME_MAPPING_FILE);
        if (in == null) {       // not found file
            LOG.debug("not found MIME mapping: " + MIME_MAPPING_FILE);
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
    private static String getMimeType(String name) {
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

    private static class NormalFile extends AbstractFileObject {
        private File mFile;
        
        NormalFile(File file, String mime) throws IOException {
            super(mime);
            mFile = Objects.requireNonNull(file);
            if (! mFile.exists()) {
                throw new IOException("Not such file or directory: "+mFile.getPath());
            }
        }
        
        @Override
        public String getName() {
            return mFile.getPath();
        }

        @Override
        public InputStream openStream() throws IOException {
            return new FileInputStream(mFile);
        }

        @Override
        public String toString() {
            return "file:///" + mFile.getAbsolutePath();
        }
    }
    
    private static class InnerZip extends AbstractFileObject {
        private ZipFile mZip;
        private String mName;
        
        InnerZip(ZipFile zipFile, String entryName, String mime) throws IOException {
            super(mime);
            mZip = Objects.requireNonNull(zipFile);
            mName = Objects.requireNonNull(entryName);
            if (mZip.getEntry(mName) == null) {
                throw new IOException("Not such entry in ZIP: "+mName);
            }
        }
        
        @Override
        public String getName() {
            return mName;
        }

        @Override
        public InputStream openStream() throws IOException {
            ZipEntry entry = mZip.getEntry(mName);
            assert entry != null;
            return mZip.getInputStream(entry);
        }

        @Override
        public String toString() {
            return "zip://" + mZip.getName() + "!" + mName;
        }
    }

    private static class FileBlock extends AbstractFileObject {
        private String mName;
        private RandomAccessFile mFile;
        private long mOffset, mSize, mOldOffset = -1;

        FileBlock(String name, RandomAccessFile file, long offset, long size, String mime)
                throws IOException {
            super(mime);
            mName = Objects.requireNonNull(name);
            mFile = Objects.requireNonNull(file);
            mOffset = offset;
            mSize = size;
            if (mSize > (mFile.length() - mOffset)) {
                throw new IOException("Available size lesser than " + mSize);
            }
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public InputStream openStream() throws IOException {
            mOldOffset = mFile.getFilePointer();
            mFile.seek(mOffset);
            return new InputStream() {
                private long readBytes = 0;

                @Override
                public int read() throws IOException {
                    if (readBytes++ >= mSize) {
                        return -1;
                    }
                    return mFile.read();
                }
            };
        }

        @Override
        public void reset() throws IOException {
            if (mOldOffset != -1) {
                mFile.seek(mOldOffset);
            }
            mOldOffset = -1;
        }

        @Override
        public String toString() {
            return String.format("block %s <offset=%d; size=%d>", getName(), mOffset, mSize);
        }
    }

    private static class URLFile extends AbstractFileObject {
        private URL mUrl;

        URLFile(URL url, String mime) {
            super(mime);
            mUrl = Objects.requireNonNull(url);
        }

        @Override
        public String getName() {
            return mUrl.getPath();
        }

        @Override
        public InputStream openStream() throws IOException {
            return mUrl.openStream();
        }

        @Override
        public String toString() {
            return mUrl.toString();
        }
    }

    private static class EmptyFile extends AbstractFileObject {
        private String mName;

        private static InputStream input = new InputStream() {
            @Override
            public int read() throws IOException {
                return -1;
            }
        };

        protected EmptyFile(String name, String mime) {
            super(mime);
            mName = name;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public InputStream openStream() throws IOException {
            return input;
        }
    }

    public static FileObject fromFile(File file, String mime) throws IOException {
        return new NormalFile(file, getOrDetectMime(file.getPath(), mime));
    }

    public static FileObject fromZip(ZipFile zipFile, String entryName,
                                     String mime) throws IOException {
        return new InnerZip(zipFile, entryName, getOrDetectMime(entryName, mime));
    }

    public static FileObject fromBlock(String name, RandomAccessFile file,
                                       long offset, long size,
                                       String mime) throws IOException {
        return new FileBlock(name, file, offset, size, getOrDetectMime(name, mime));
    }

    public static FileObject fromURL(URL url, String mime) {
        return new URLFile(url, getOrDetectMime(url.getPath(), mime));
    }

    public static FileObject emptyFile(String name, String mime) {
        return new EmptyFile(name, getOrDetectMime(name, mime));
    }
}
