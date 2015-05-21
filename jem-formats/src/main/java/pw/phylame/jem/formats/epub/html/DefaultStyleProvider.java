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

package pw.phylame.jem.formats.epub.html;

import pw.phylame.tools.file.FileFactory;
import pw.phylame.tools.file.FileObject;

/**
 * Default ePub HTML style provider.
 */
public class DefaultStyleProvider implements StyleProvider {
    @Override
    public FileObject getCssFile() {
        return FileFactory.fromURL(DefaultStyleProvider.class.getResource("epub.css"), null);
    }

    @Override
    public String getBookCoverStyle() {
        return "cover";
    }

    @Override
    public String getIntroTitleStyle() {
        return "book_intro_title";
    }

    @Override
    public String getIntroContentStyle() {
        return "book_intro_content";
    }

    @Override
    public String getInfoTitleStyle() {
        return "book_info_title";
    }

    @Override
    public String getInfoContentStyle() {
        return "book_info_content";
    }

    @Override
    public String getTocTitleStyle() {
        return "toc_title";
    }

    @Override
    public String getTocItemsStyle() {
        return "toc_item";
    }

    @Override
    public String getSectionCoverStyle() {
        return "cover";
    }

    @Override
    public String getSectionTitleStyle() {
        return "toc_title";
    }

    @Override
    public String getSectionIntroStyle() {
        return "chapter_intro";
    }

    @Override
    public String getSectionContentsStyle() {
        return "toc_item";
    }

    @Override
    public String getChapterCoverStyle() {
        return "cover";
    }

    @Override
    public String getChapterTitleStyle() {
        return "chapter_title";
    }

    @Override
    public String getChapterIntroStyle() {
        return "chapter_intro";
    }

    @Override
    public String getChapterTextStyle() {
        return "chapter_text";
    }

}
