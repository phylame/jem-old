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

package pw.phylame.imabw.app.model;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.jem.core.Book;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.core.BookHelper;

public class BookTask {
    // The book descriptor
    private Book book = null;

    // e-book source file
    private File source = null;

    // format of current source
    private String format = null;

    // Jem Maker argument
    private Map<String, Object> makerArguments = null;

    private BookTask(Book book, File source, String format) {
        this.book = book;
        this.source = source;
        this.format = format;
    }

    public static BookTask fromNewBook(Book book) {
        return new BookTask(book, null, null);
    }

    public static BookTask fromOpenBook(ParseResult pr) {
        BookTask task = new BookTask(pr.book, pr.pd.file, null);
        if (BookHelper.hasMaker(pr.pd.format)) {
            task.format = pr.pd.format;
        }
        return task;
    }

    public Book getBook() {
        return book;
    }

    public File getSource() {
        return source;
    }

    public String getFormat() {
        return format;
    }

    public Map<String, Object> getMakerArguments() {
        return makerArguments;
    }

    public void cleanup() {
        bookClosed();
        book.cleanup();
    }

    // modification state

    private HashMap<Chapter, ModifiedState> modifiedStates = new HashMap<>();

    private boolean extensionModified = false;
    private HashSet<Chapter> editedChapters = new HashSet<>();

    private ModifiedState getOrCreate(Chapter chapter) {
        ModifiedState state = modifiedStates.get(chapter);
        if (state == null) {
            modifiedStates.put(chapter, state = new ModifiedState(chapter));
        }
        return state;
    }

    private void checkModified(ModifiedState state) {
        if (state.isModified()) {
            editedChapters.add(state.chapter);
        } else {
            editedChapters.remove(state.chapter);
        }
        Imabw.sharedInstance().getForm().updateTitle();
    }

    public void bookExtensionModified(boolean modified) {
        ModifiedState state = getOrCreate(book);
        state.setExtModified(modified);
        checkModified(state);
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

    public void chapterRemoved(Chapter chapter) {
        editedChapters.remove(chapter);
        modifiedStates.remove(chapter);
    }

    public void bookSaved(MakerData md) {
        modifiedStates.clear();
        editedChapters.clear();
        source = md.file;
        format = md.format;
        makerArguments = md.arguments;
    }

    private void bookClosed() {
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
        private Chapter chapter;
        private int textCount = 0;
        private int attrCount = 0;
        private int subCount = 0;
        private int extCount = 0;

        private ModifiedState(Chapter chapter) {
            this.chapter = chapter;
        }

        private boolean isModified() {
            return textCount != 0 || attrCount != 0 || subCount != 0 | extCount != 0;
        }

        private void setTextModified(boolean modified) {
            textCount += modified ? 1 : -1;
            System.err.println(chapter.getTitle() + " textCount: " + textCount);
        }

        private void setAttrModified(boolean modified) {
            attrCount += modified ? 1 : -1;
            System.err.println(chapter.getTitle() + " attrCount: " + attrCount);
        }

        private void setSubModified(boolean modified) {
            subCount += modified ? 1 : -1;
            System.err.println(chapter.getTitle() + " subCount: " + subCount);
        }

        private void setExtModified(boolean modified) {
            extCount += modified ? 1 : -1;
            System.err.println(book.getTitle() + " extCount: " + extCount);
        }
    }
}
