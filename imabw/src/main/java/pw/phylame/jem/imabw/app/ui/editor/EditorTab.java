/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app.ui.editor;

import java.io.*;
import java.util.HashMap;

import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.util.FileObject;
import pw.phylame.jem.util.FileFactory;
import pw.phylame.jem.util.TextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EditorTab implements Chapter.Cleanable {
    private static Log LOG = LogFactory.getLog(EditorTab.class);
    private static Imabw app = Imabw.getInstance();

    private static final String CACHE_ENCODING = "UTF-16BE";
    private static HashMap<Chapter, File> cachedFiles = new HashMap<>();

    private Chapter chapter;
    private TextEditor editor;
    private File cache = null;

    private boolean modified = false;

    public EditorTab(Chapter chapter) {
        this.chapter = chapter;
        this.editor = new TextEditor(this, chapter.getSource().getText());
        // query if already cached
        cache = cachedFiles.get(chapter);
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
        app.getActiveTask().chapterTextModified(chapter, true);
        app.getActiveViewer().getNavigateTree().textModified(chapter);
    }

    private void createCacheFile() throws IOException {
        cache = File.createTempFile("imabw_chapter_", ".itf");
        chapter.registerCleanup(this);

        FileObject fb = FileFactory.fromFile(cache, null);
        chapter.setSource(TextFactory.fromFile(fb, CACHE_ENCODING));
        cachedFiles.put(chapter, cache);
    }

    public void cacheText() {
        BufferedWriter writer = null;
        try {
            if (cache == null) {
                createCacheFile();
            }
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache), CACHE_ENCODING));
            editor.getTextComponent().write(writer);
            modified = false;
        } catch (IOException e) {
            LOG.debug("cannot cache part content: " + chapter.stringAttribute(Chapter.TITLE), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
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
            if (! cache.delete()) {
                LOG.debug("cannot delete cached file: " + cache.getAbsolutePath());
            }
            // remove from cached map
            if (cachedFiles.remove(chapter) == null) {
                app.debug("cannot remove chapter {0} from cache list", chapter);
            }
        }
    }
}
