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

import pw.phylame.jem.core.Chapter;

import java.io.Closeable;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Cleans input source of book.
 */
public class SourceCleaner implements Chapter.Cleanable {
    private static Log LOG = LogFactory.getLog(SourceCleaner.class);

    protected final Closeable dev;
    protected final String name;
    protected final Runnable extTask;

    public SourceCleaner(Closeable dev, String name) {
        this(dev, name, null);
    }

    public SourceCleaner(Closeable dev, String name, Runnable extTask) {
        this.dev = dev;
        this.name = name;
        this.extTask = extTask;
    }

    @Override
    public void clean(Chapter chapter) {
        try {
            dev.close();
        } catch (IOException e) {
            LOG.debug("cannot close source file: " + name, e);
        } finally {
            if (extTask != null) {
                extTask.run();
            }
        }
    }
}
