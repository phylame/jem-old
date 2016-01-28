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

package pw.phylame.jem.formats.util;

import java.io.Closeable;

import pw.phylame.jem.util.IOUtils;
import pw.phylame.jem.core.Chapter;

/**
 * Cleans input source of book.
 */
public class SourceCleaner implements Chapter.Cleanable {
    private final Closeable source;
    private final Runnable addon;

    public SourceCleaner(Closeable source) {
        this.source = source;
        addon = null;
    }

    public SourceCleaner(Closeable source, Runnable addon) {
        this.source = source;
        this.addon = addon;
    }

    @Override
    public void clean(Chapter chapter) {
        IOUtils.closeQuietly(source);
        if (addon != null) {
            addon.run();
        }
    }
}
