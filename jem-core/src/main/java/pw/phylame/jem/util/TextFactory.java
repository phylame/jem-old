/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedList;

import pw.phylame.jem.core.Jem;

/**
 * Factory class for creating <tt>TextObject</tt>.
 */
public class TextFactory {
    /**
     * Returns list of lines split from text content in this object.
     *
     * @param cs        the input string
     * @param skipEmpty <tt>true</tt> to skip empty line
     * @return list of lines, never <tt>null</tt>
     * @throws NullPointerException if the <tt>cs</tt> is <tt>null</tt>
     */
    public static List<String> splitLines(CharSequence cs, boolean skipEmpty) {
        if (cs == null) {
            throw new NullPointerException("cs");
        }
        List<String> lines = new LinkedList<>();
        int ix, begin = 0, length = cs.length();
        CharSequence sub;
        for (ix = 0; ix < length; ) {
            char ch = cs.charAt(ix);
            if ('\n' == ch) {   // \n
                sub = cs.subSequence(begin, ix);
                if (sub.length() > 0 || !skipEmpty) {
                    lines.add(sub.toString());
                }
                begin = ++ix;
            } else if ('\r' == ch) {
                sub = cs.subSequence(begin, ix);
                if (sub.length() > 0 || !skipEmpty) {
                    lines.add(sub.toString());
                }
                if (ix + 1 < length && '\n' == cs.charAt(ix + 1)) {   // \r\n
                    begin = ix += 2;
                } else {    // \r
                    begin = ++ix;
                }
            } else {
                ++ix;
            }
        }
        if (ix >= begin) {
            sub = cs.subSequence(begin, cs.length());
            if (sub.length() > 0 || !skipEmpty) {
                lines.add(sub.toString());
            }
        }
        return lines;
    }

    private static class RawText extends AbstractText {
        static {
            Jem.mapVariantType(RawText.class, Jem.TEXT);
        }

        private final CharSequence text;

        private RawText(CharSequence str, String type) {
            super(type);
            if (str == null) {
                throw new NullPointerException("str");
            }
            this.text = str;
        }

        @Override
        public String getText() throws Exception {
            return text.toString();
        }
    }

    private static class FileText extends AbstractText {
        static {
            Jem.mapVariantType(FileText.class, Jem.TEXT);
        }

        private final FileObject file;
        private final String encoding;

        private FileText(FileObject file, String encoding, String type) {
            super(type);
            if (file == null) {
                throw new NullPointerException("file");
            }
            this.file = file;
            // if null, using platform default encoding
            this.encoding = encoding;
        }

        @Override
        public String getText() throws Exception {
            try (InputStream stream = file.openStream()) {
                return IOUtils.toString(stream, encoding);
            }
        }

        @Override
        public List<String> getLines(boolean skipEmpty) throws Exception {
            try (InputStream stream = file.openStream()) {
                return IOUtils.toLines(stream, encoding, skipEmpty);
            }
        }

        @Override
        public int writeTo(Writer writer) throws IOException {
            try (Reader reader = IOUtils.openReader(file.openStream(), encoding)) {
                return IOUtils.copy(reader, writer, -1);
            }
        }
    }

    private static TextObject EMPTY_TEXT;

    public static synchronized TextObject emptyText() {
        if (EMPTY_TEXT == null) {
            EMPTY_TEXT = new RawText("", TextObject.PLAIN);
        }
        return EMPTY_TEXT;
    }

    public static TextObject forString(CharSequence str) {
        return forString(str, TextObject.PLAIN);
    }

    public static TextObject forString(CharSequence str, String type) {
        return new RawText(str, type);
    }

    public static TextObject forFile(FileObject file) {
        return forFile(file, null, TextObject.PLAIN);
    }

    public static TextObject forFile(FileObject file, String encoding) {
        return forFile(file, encoding, TextObject.PLAIN);
    }

    public static TextObject forFile(FileObject file, String encoding, String type) {
        return new FileText(file, encoding, type);
    }
}
