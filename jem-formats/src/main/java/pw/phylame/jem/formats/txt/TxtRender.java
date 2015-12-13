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

package pw.phylame.jem.formats.txt;

import java.io.Writer;

import pw.phylame.jem.formats.util.text.NormalWriter;

class TxtRender extends NormalWriter {
    private final Writer writer;
    private final boolean additionLine;
    private final String lineSeparator;

    TxtRender(Writer writer, boolean additionLine, String lineSeparator) {
        this.lineSeparator = lineSeparator;
        this.additionLine = additionLine;
        this.writer = writer;
    }

    @Override
    public void writeText(String text) throws Exception {
        writer.write(text);
    }

    @Override
    public void endChapter() throws Exception {
        if (additionLine) {
            writer.write(lineSeparator);
        }
    }
}
