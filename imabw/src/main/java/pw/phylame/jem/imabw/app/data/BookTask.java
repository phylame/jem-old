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

package pw.phylame.jem.imabw.app.data;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.BookHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import pw.phylame.jem.imabw.app.Imabw;

public class BookTask {
    private static Log LOG = LogFactory.getLog(BookTask.class);

    // e-book source file
    private File source = null;

    // book cache file
    private File cache = null;

    // auto delete cache file when clean task
    private boolean autoDelete = false;

    // format of current source
    private String format = null;

    // Jem Parser argument
    private Map<String, Object> paKw = null;

    // Jem Maker argument
    private Map<String, Object> maKw = null;

    // The book descriptor
    private Book book = null;

    private BookTask() {
    }

    public static BookTask fromNewBook(Book book) {
        BookTask task = new BookTask();
        task.setBook(book);
        return task;
    }

    public static BookTask fromOpenBook(Book book, ParserData data, File cache, boolean autoDelete) {
        BookTask task = new BookTask();
        task.setBook(book);
        task.setSource(data.file);
        if (BookHelper.hasMaker(data.format)) {
            task.setFormat(data.format);
        }
        task.setParserArguments(data.kw);
        task.setCache(cache, autoDelete);
        return task;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Map<String, Object> getParserArguments() {
        return paKw;
    }

    public void setParserArguments(Map<String, Object> kw) {
        paKw = kw;
    }

    public Map<String, Object> getMakerArguments() {
        return maKw;
    }

    public void setMakerArguments(Map<String, Object> kw) {
        maKw = kw;
    }

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    private void setCache(File cache, boolean autoDelete) {
        this.cache = cache;
        this.autoDelete = autoDelete;
    }

    public void cleanup() {
        book.cleanup();
        if (cache != null && autoDelete) {
            if (!cache.delete()) {
                LOG.debug("cannot delete cache file: " + cache.getAbsolutePath());
            }
        }
    }

    // modification state

    private HashMap<Chapter, ModifiedState> modifiedStates = new HashMap<>();

    private HashSet<Chapter> editedChapters = new HashSet<>();

    private ModifiedState getOrCreate(Chapter chapter) {
        ModifiedState state = modifiedStates.get(chapter);
        if (state == null) {
            state = new ModifiedState(chapter);
            modifiedStates.put(chapter, state);
        }
        return state;
    }

    private void checkModified(ModifiedState state) {
        if (state.isModified()) {
            editedChapters.add(state.chapter);
        } else {
            editedChapters.remove(state.chapter);
        }
        Imabw.getInstance().getActiveViewer().updateTitle();
    }

    public void chapterTextModified(Chapter chapter, boolean modified) {
        ModifiedState state = getOrCreate(chapter);
        state.setTextModified(modified);
        checkModified(state);
    }

    public void chapterAttributeModified(Chapter chapter, boolean modified) {
        ModifiedState state = getOrCreate(chapter);
        state.setAttrModified(modified);
        checkModified(state);
    }

    public void chapterChildrenModified(Chapter chapter, boolean modified) {
        ModifiedState state = getOrCreate(chapter);
        state.setSubModified(modified);
        checkModified(state);
    }

    public void bookSaved() {
        modifiedStates.clear();
        editedChapters.clear();
    }

    public boolean isChapterModified(Chapter chapter) {
        ModifiedState state = modifiedStates.get(chapter);
        return state != null && state.isModified();
    }

    public boolean isBookModified() {
        return !editedChapters.isEmpty();
    }

    // hold modification state of chapter
    private class ModifiedState {
        Chapter chapter;
        int textCount = 0;
        int attrCount = 0;
        int subCount = 0;

        ModifiedState(Chapter chapter) {
            this.chapter = chapter;
        }

        boolean isModified() {
            return textCount != 0 || attrCount != 0 || subCount != 0;
        }

        void setTextModified(boolean modified) {
            textCount += modified ? 1 : -1;

            System.err.println(chapter+" textCount: "+textCount);
        }

        void setAttrModified(boolean modified) {
            attrCount += modified ? 1 : -1;

            System.err.println(chapter+" attrCount: "+attrCount);
        }

        void setSubModified(boolean modified) {
            subCount += modified ? 1 : -1;

            System.err.println(chapter+" subCount: "+subCount);
        }
    }
}
