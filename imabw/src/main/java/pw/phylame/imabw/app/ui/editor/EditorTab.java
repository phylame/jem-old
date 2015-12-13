/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Imabw.
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

package pw.phylame.imabw.app.ui.editor;

import java.io.*;
import java.util.HashMap;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.jem.core.Chapter;
import pw.phylame.imabw.app.Worker;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EditorTab implements Chapter.Cleanable {
    private static final Log LOG = LogFactory.getLog(EditorTab.class);
    private static final Imabw app = Imabw.sharedInstance();

    private static final String CACHE_ENCODING = "UTF-16BE";
    private static HashMap<Chapter, File> fileCaches = new HashMap<>();

    private Chapter chapter;
    private TextEditor editor;
    private File cache = null;

    private boolean modified = false;

    public EditorTab(Chapter chapter) {
        this.chapter = chapter;
        this.editor = new TextEditor(this, Worker.sharedInstance().contentOfChapter(chapter, ""));
        // query if already cached
        cache = fileCaches.get(chapter);
    }

    public Chapter getChapter() {
        return chapter;
    }

    public TextEditor getEditor() {
        return editor;
    }

    public boolean isModified() {
        return modified;
    }

    void textModified() {
        modified = true;
        app.getManager().getActiveTask().chapterTextModified(chapter, true);
        app.getForm().getContentsTree().fireTextModified(chapter);
    }

    private void createCacheFile() throws IOException {
        cache = File.createTempFile("imabw_chapter_", ".tmp");
        chapter.registerCleanup(this);

        FileObject fb = FileFactory.fromFile(cache, null);
        chapter.setContent(TextFactory.fromFile(fb, CACHE_ENCODING));
        fileCaches.put(chapter, cache);
    }

    public void cacheText() {
        Closeable dev = null;
        try {
            if (cache == null) {
                createCacheFile();
            }
            OutputStream os = new FileOutputStream(cache);
            dev = os;
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, CACHE_ENCODING));
            dev = writer;
            editor.getTextComponent().write(writer);
            modified = false;
        } catch (IOException e) {
            LOG.debug("cannot cache part content: " + chapter.getTitle(), e);
        } finally {
            if (dev != null) {
                try {
                    dev.close();
                } catch (IOException e) {
                    LOG.debug("cannot close cached file: " + cache.getAbsolutePath());
                }
            }
        }
    }

    public void cacheIfNeed() {
        if (modified) {
            cacheText();
        }
    }

    @Override
    public void clean(Chapter chapter) {
        if (cache != null) {
            if (!cache.delete()) {
                LOG.debug("cannot delete cached file: " + cache.getAbsolutePath());
            }
            // remove from cached map
            if (fileCaches.remove(chapter) == null) {
                LOG.debug("cannot remove chapter from cache list: " + chapter);
            }
        }
    }
}
