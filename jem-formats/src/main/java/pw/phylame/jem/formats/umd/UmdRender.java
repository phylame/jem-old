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

package pw.phylame.jem.formats.umd;

import pw.phylame.jem.formats.util.text.TextWriter;

import java.util.LinkedList;
import java.io.IOException;
import java.io.RandomAccessFile;

class UmdRender implements TextWriter {
    private final UmdMaker umdMaker;
    private final RandomAccessFile file;
    final LinkedList<Long> offsets;
    final LinkedList<String> titles;

    UmdRender(UmdMaker umdMaker, RandomAccessFile file) {
        this.umdMaker = umdMaker;
        this.file = file;
        this.offsets = new LinkedList<>();
        this.titles = new LinkedList<>();
    }

    @Override
    public void startChapter(String title) throws Exception {
        offsets.add(file.getFilePointer());
        titles.add(title);
    }

    @Override
    public void writeText(String text) throws IOException {
        umdMaker.writeString(file, text);
    }

    @Override
    public void endChapter() throws Exception {
    }
}
