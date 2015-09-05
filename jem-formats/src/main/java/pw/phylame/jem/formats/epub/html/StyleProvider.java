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

import pw.phylame.jem.util.FileObject;

/**
 * Provides ePub HTML CSS content.
 */
public interface StyleProvider {
    /**
     * Gets the CSS source file.
     * @return the CSS file
     */
    FileObject getCssFile();

    // *********************
    // ** book cover page **
    // *********************
    String getBookCoverStyle();

    // *********************
    // ** book intro page **
    // *********************
    String getIntroTitleStyle();
    String getIntroContentStyle();

    // ********************
    // ** book info page **
    // ********************
    String getInfoTitleStyle();
    String getInfoContentStyle();

    // ************************
    // ** book contents page **
    // ************************
    String getTocTitleStyle();
    String getTocItemsStyle();

    // ************************
    // ** section cover page **
    // ************************
    String getSectionCoverStyle();

    // ***************************
    // ** section contents page **
    // ***************************
    String getSectionTitleStyle();
    String getSectionIntroStyle();
    String getSectionContentsStyle();

    // ************************
    // ** chapter cover page **
    // ************************
    String getChapterCoverStyle();

    // ***********************
    // ** chapter text page **
    // ***********************
    String getChapterTitleStyle();
    String getChapterIntroStyle();
    String getChapterTextStyle();
}
