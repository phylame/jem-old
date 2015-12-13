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
import java.util.List;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;

/**
 * Factory class for creating <tt>TextObject</tt>.
 */
public class TextFactory {
    /**
     * Returns list of lines split from text content in this object.
     *
     * @param str       the input string
     * @param skipEmpty <tt>true</tt> to skip empty line
     * @return list of lines, never <tt>null</tt>
     */
    public static List<String> splitLines(String str, boolean skipEmpty) {
        List<String> lines = new LinkedList<String>();
        int ix, begin = 0, length = str.length();
        String sub;
        for (ix = 0; ix < length; ) {
            char ch = str.charAt(ix);
            if ('\n' == ch) {   // \n
                sub = str.substring(begin, ix);
                if (!sub.isEmpty() || !skipEmpty) {
                    lines.add(sub);
                }
                begin = ++ix;
            } else if ('\r' == ch) {
                sub = str.substring(begin, ix);
                if (!sub.isEmpty() || !skipEmpty) {
                    lines.add(sub);
                }
                if (ix + 1 < length && '\n' == str.charAt(ix + 1)) {   // \r\n
                    begin = ix += 2;
                } else {    // \r
                    begin = ++ix;
                }
            } else {
                ++ix;
            }
        }
        if (ix >= begin) {
            sub = str.substring(begin);
            if (!sub.isEmpty() || !skipEmpty) {
                lines.add(sub);
            }
        }
        return lines;
    }

    private static class RawText extends AbstractText {
        private CharSequence text;

        RawText(CharSequence str, String type) {
            super(type);
            if (str == null) {
                throw new NullPointerException("str");
            }
            this.text = str;
        }

        @Override
        public String getText() {
            return text.toString();
        }

        @Override
        public void writeTo(Writer writer) throws IOException {
            writer.write(text.toString());
        }
    }

    private static class FileText extends AbstractText {
        private FileObject file;
        private String encoding;

        FileText(FileObject file, String encoding, String type) {
            super(type);
            if (file == null) {
                throw new NullPointerException("file");
            }
            this.file = file;
            // if null, using platform default encoding
            this.encoding = (encoding != null) ? encoding :
                    System.getProperty("file.encoding");
        }

        @Override
        public String getText() throws Exception {
            InputStream stream = file.openStream();
            assert stream != null;
            BufferedInputStream input = new BufferedInputStream(stream);
            try {
                return IOUtils.toString(input, encoding);
            } finally {
                input.close();
                file.reset();
            }
        }

        @Override
        public List<String> getLines(boolean skipEmpty) throws Exception {
            InputStream stream = file.openStream();
            assert stream != null;
            BufferedInputStream input = new BufferedInputStream(stream);
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(input, encoding));
            } catch (IOException e) {
                input.close();
                throw e;
            }
            List<String> lines = new LinkedList<String>();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty() || !skipEmpty) {
                        lines.add(line);
                    }
                }
                return lines;
            } finally {
                reader.close();
                file.reset();
            }
        }

        @Override
        public void writeTo(Writer writer) throws IOException {
            BufferedInputStream input = null;
            try {
                InputStream stream = file.openStream();
                assert stream != null;
                input = new BufferedInputStream(stream);
                IOUtils.copy(input, writer, encoding);
            } finally {
                if (input != null) {
                    input.close();
                    file.reset();
                }
            }
        }
    }

    private static TextObject EMPTY_TEXT;

    public static TextObject emptyText() {
        if (EMPTY_TEXT == null) {
            EMPTY_TEXT = new RawText("", TextObject.PLAIN);
        }
        return EMPTY_TEXT;
    }

    public static TextObject fromString(CharSequence str) {
        return fromString(str, TextObject.PLAIN);
    }

    public static TextObject fromString(CharSequence str, String type) {
        return new RawText(str, type);
    }

    public static TextObject fromFile(FileObject file) {
        return fromFile(file, null, TextObject.PLAIN);
    }

    public static TextObject fromFile(FileObject file, String encoding) {
        return fromFile(file, encoding, TextObject.PLAIN);
    }

    public static TextObject fromFile(FileObject file, String encoding, String type) {
        return new FileText(file, encoding, type);
    }
}
