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

import java.util.Objects;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory class for creating <tt>TextObject</tt>.
 */
public class TextFactory {
    private static Log LOG = LogFactory.getLog(TextFactory.class);

    private static class StringSource implements TextObject {
        private CharSequence mText;
        private String       mType;

        StringSource(CharSequence str, String type) {
            mText = Objects.requireNonNull(str);
            mType = Objects.requireNonNull(type);
        }

        @Override
        public String getType() {
            return mType;
        }

        @Override
        public String getText() {
            return mText.toString();
        }

        @Override
        public String[] getLines() {
            return mText.toString().split("(\\r\\n)|(\\r)|(\\n)");
        }

        @Override
        public void writeTo(Writer writer) throws IOException {
            writer.write(mText.toString());
        }
    }

    private static class FileSource implements TextObject {
        private FileObject mFile;
        private String      mEncoding;
        private String      mType;

        FileSource(FileObject file, String encoding, String type) {
            mFile = Objects.requireNonNull(file);
            // if null, using platform default encoding
            mEncoding = encoding;
            mType = Objects.requireNonNull(type);
        }

        @Override
        public String getType() {
            return mType;
        }

        @Override
        public String getText() {
            BufferedInputStream input = null;
            try {
                InputStream stream = mFile.openStream();
                assert stream != null;
                input = new BufferedInputStream(stream);
                return IOUtils.toString(input, mEncoding);
            } catch (IOException e) {
                LOG.debug(e);
                return null;
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    mFile.reset();
                } catch (IOException e) {
                    LOG.debug(e);
                }
            }
        }

        @Override
        public String[] getLines() {
            BufferedInputStream input = null;
            try {
                InputStream stream = mFile.openStream();
                assert stream != null;
                input = new BufferedInputStream(stream);
                return IOUtils.readLines(input, mEncoding).toArray(new String[0]);
            } catch (IOException e) {
                LOG.debug(e);
                return null;
            } finally {
                try {
                    if (input != null) {
                        input.close();
                    }
                    mFile.reset();
                } catch (IOException e) {
                    LOG.debug(e);
                }
            }
        }

        @Override
        public void writeTo(Writer writer) throws IOException {
            InputStream stream = mFile.openStream();
            BufferedInputStream input = new BufferedInputStream(stream);
            IOUtils.copy(input, writer, mEncoding);
            input.close();
            mFile.reset();
        }
    }

    public static TextObject fromString(CharSequence str) {
        return fromString(str, TextObject.PLAIN);
    }

    public static TextObject fromString(CharSequence str, String type) {
        return new StringSource(str, type);
    }

    public static TextObject fromFile(FileObject file) {
        return fromFile(file, null);
    }

    public static TextObject fromFile(FileObject file, String encoding) {
        return fromFile(file, encoding, TextObject.PLAIN);
    }

    public static TextObject fromFile(FileObject file, String encoding, String type) {
        return new FileSource(file, encoding, type);
    }
}
