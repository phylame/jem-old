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

package pw.phylame.jem.formats.util.text;

import java.util.List;
import java.util.LinkedList;

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.util.TextFactory;
import pw.phylame.jem.util.TextObject;

/**
 * Render book text with plain style.
 */
public final class TextRender {
    private TextRender() {
    }

    /**
     * Renders chapter of book to contents with one level.
     */
    public static void renderBook(Chapter book, TextWriter writer, TextConfig config)
            throws Exception {
        RenderHelper maker = new RenderHelper(writer, config);
        for (Chapter ch : book) {
            walkChapter(ch, maker);
        }
    }

    /**
     * Renders lines of text in <tt>TextObject</tt> to specified writer.
     *
     * @param text   the text source
     * @param writer the destination writer
     * @param config render config
     * @return number of written lines
     * @throws Exception if occurs error while rendering text
     */
    public static int renderLines(TextObject text, TextWriter writer, TextConfig config)
            throws Exception {
        return renderLines(text, writer, config, false);
    }

    private static int renderLines(TextObject text, TextWriter writer, TextConfig config,
                                   boolean prependNL) throws Exception {
        List<String> lines = TextUtils.plainLines(text, config.skipEmptyLine, config.textConverter);
        if (lines == null) {
            return 0;
        }
        int ix = 1, size = lines.size();
        if (prependNL && size > 0) {
            writer.writeText(config.lineSeparator);
        }
        for (String line : lines) {
            line = TextUtils.trimmed(line);
            writer.writeText(config.paragraphPrefix + line);
            if (ix++ != size) {
                writer.writeText(config.lineSeparator);
            }
        }
        return size;
    }

    public static String renderLines(TextObject text, TextConfig config) throws Exception {
        StringWriter writer = new StringWriter();
        renderLines(text, writer, config);
        return writer.toString();
    }

    /**
     * Renders text in <tt>TextObject</tt> to specified writer.
     *
     * @param text   the text source
     * @param writer the destination writer
     * @param config render config
     * @return written state, <tt>true</tt> if has text written, otherwise not
     * @throws Exception if occurs error while rendering text
     */
    public static boolean renderText(TextObject text, TextWriter writer, TextConfig config)
            throws Exception {
        return renderText(text, writer, config, false);
    }

    private static boolean renderText(TextObject text, TextWriter writer, TextConfig config,
                                      boolean prependLF) throws Exception {
        if (config.formatParagraph) {
            return renderLines(text, writer, config, prependLF) > 0;
        } else {
            String str = TextUtils.plainText(text, config.textConverter);
            if (!str.isEmpty()) {
                if (prependLF) {
                    writer.writeText(config.lineSeparator);
                }
                writer.writeText(str);
                return true;
            } else {
                return false;
            }
        }
    }

    public static String renderText(TextObject text, TextConfig config) throws Exception {
        if (config.formatParagraph) {
            return renderLines(text, config);
        } else {
            return TextUtils.plainText(text, config.textConverter);
        }
    }

    private static class StringWriter implements TextWriter {
        private final StringBuilder sb = new StringBuilder();

        @Override
        public String toString() {
            return sb.toString();
        }

        @Override
        public void startChapter(String title) throws Exception {
        }

        @Override
        public void writeText(String text) throws Exception {
            sb.append(text);
        }

        @Override
        public void endChapter() throws Exception {
        }
    }

    private static void walkChapter(Chapter chapter, RenderHelper maker) throws Exception {
        maker.beginItem(chapter);

        maker.writeText(chapter);

        for (Chapter c : chapter) {
            walkChapter(c, maker);
        }

        maker.endItem();
    }

    private static class RenderHelper {
        private final TextWriter writer;
        private final TextConfig config;

        private LinkedList<String> titleStack;

        private RenderHelper(TextWriter writer, TextConfig config) {
            this.config = config;
            this.writer = writer;
            if (config.joinTitles) {
                titleStack = new LinkedList<>();
            }
        }

        private void beginItem(Chapter chapter) {
            if (config.joinTitles) {
                titleStack.addLast(chapter.getTitle());
            }
        }

        private void writeText(Chapter chapter) throws Exception {
            String lineSeparator = config.lineSeparator;
            String title;
            if (config.joinTitles) {
                title = TextUtils.join(config.titleSeparator, titleStack);
            } else {
                title = chapter.getTitle();
            }
            writer.startChapter(title);

            // title
            boolean writtenTitle = false;
            if (config.writeTitle) {
                writer.writeText(title);
                writtenTitle = true;
            }
            // prefix
            if (TextUtils.isValid(config.prefixText)) {
                if (writtenTitle) {
                    writer.writeText(lineSeparator + config.prefixText);
                } else {
                    writer.writeText(config.prefixText);
                }
            }
            // intro
            if (config.writeIntro) {
                TextObject intro = chapter.getIntro();
                if (intro != null && renderText(intro, writer, config, true)) {
                    writer.writeText(lineSeparator + config.introSeparator);
                }
            }
            // content
            TextObject content = chapter.getContent();
            renderText(content != null ? content : TextFactory.emptyText(), writer, config, true);
            // suffix
            if (TextUtils.isValid(config.suffixText)) {
                writer.writeText(lineSeparator + config.suffixText);
            }
            // padding line
            if (config.paddingLine) {
                writer.writeText(lineSeparator);
            }
            writer.endChapter();
        }

        private void endItem() {
            if (config.joinTitles) {
                titleStack.removeLast();
            }
        }
    }
}
