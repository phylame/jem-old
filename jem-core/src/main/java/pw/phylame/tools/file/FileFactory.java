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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

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
                throw new IOException("not such file or directory: "+file.getPath());
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

    public static FileObject getFile(File file, String mime) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        return new NormalFile(file, getMime(file.getPath(), mime));
    }
}
