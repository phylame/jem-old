/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.core;

import pw.phylame.tools.TextObject;
import pw.phylame.tools.file.FileObject;

/**
 * Common chapter in book.
 */
public class Chapter extends Part {
    /** Key name for chapter cover.*/
    public static final String COVER = "cover";

    /** Key name for chapter intro.*/
    public static final String INTRO = "intro";

    public Chapter() {
        this("", new TextObject(), null, null);
    }

    public Chapter(String title) {
        this(title, new TextObject(), null, null);
    }

    public Chapter(String title, String text) {
        this(title, new TextObject(text), null, null);
    }

    public Chapter(String title, String text, FileObject cover, TextObject intro) {
        this(title, new TextObject(text), cover, intro);
    }

    public Chapter(String title, FileObject file, String encoding) {
        this(title, new TextObject(file, encoding), null, null);
    }

    public Chapter(String title, FileObject file, String encoding, FileObject cover, TextObject intro) {
        this(title, new TextObject(file, encoding), cover, intro);
    }

    public Chapter(String title, TextObject content) {
        this(title, content, null, null);
    }

    public Chapter(String title, TextObject content, FileObject cover, TextObject intro) {
        super(title, content);
        setCover(cover);
        setIntro(intro);
    }

    /**
     * Returns cover of the chapter.
     * @return cover file or <tt>null</tt> if not present
     */
    public FileObject getCover() {
        Object o = getAttribute(COVER, null);
        if (o instanceof FileObject) {
            return (FileObject) o;
        }
        return null;
    }

    /**
     * Replaces chapter cover with specified cover.
     * @param cover cover provider
     */
    public void setCover(FileObject cover) {
        setAttribute(COVER, cover);
    }

    /**
     * Returns intro content of the chapter.
     * @return intro provider or <tt>null</tt> if not present.
     */
    public TextObject getIntro() {
        Object o = getAttribute(INTRO, null);
        if (o instanceof TextObject) {
            return (TextObject) o;
        }
        return null;
    }

    /**
     * Replaces chapter intro with specified intro provider.
     * @param intro intro content provider
     */
    public void setIntro(TextObject intro) {
        setAttribute(INTRO, intro);
    }

    /**
     * Replaces chapter intro with specified intro text.
     * @param intro intro text
     */
    public void setIntro(String intro) {
        setIntro(new TextObject(intro));
    }

    public Chapter newChapter(String title) {
        return newChapter(title, new TextObject(), null, null);
    }

    public Chapter newChapter(String title, String text) {
        return newChapter(title, new TextObject(text), null, null);
    }

    public Chapter newChapter(String title, String text, FileObject cover, TextObject intro) {
        return newChapter(title, new TextObject(text), cover, intro);
    }

    public Chapter newChapter(String title, FileObject file, String encoding) {
        return newChapter(title, new TextObject(file, encoding), null, null);
    }

    public Chapter newChapter(String title, FileObject file, String encoding, FileObject cover, TextObject intro) {
        return newChapter(title, new TextObject(file, encoding), cover, intro);
    }

    public Chapter newChapter(String title, TextObject content) {
        return newChapter(title, content, null, null);
    }

    public Chapter newChapter(String title, TextObject content, FileObject cover, TextObject intro) {
        Chapter sub = new Chapter(title, content, cover, intro);
        append(sub);
        return sub;
    }
}
