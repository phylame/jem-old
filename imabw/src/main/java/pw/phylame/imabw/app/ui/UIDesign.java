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

package pw.phylame.imabw.app.ui;

import pw.phylame.gaf.ixin.IButtonType;
import pw.phylame.gaf.ixin.IActionModel;
import pw.phylame.imabw.app.Constants;

/**
 * Menu bar and tool bar models.
 */
class UIDesign implements Constants {
    private static Object[] newFile = {
            "menu.newFile",
            NEW_FILE, NEW_CHAPTER
    };
    private static Object[] openRecent = {
            "menu.openRecent",
            CLEAR_HISTORY
    };
    static int HISTORY_MENU_INDEX = 2;
    private static Object[] menuFile = {
            "menu.file",
            newFile, OPEN_FILE, openRecent,
            null,
            SAVE_FILE, SAVE_AS_FILE,
            null,
            APP_SETTINGS,
            null,
            FILE_DETAILS,
            null,
            EXIT_APP
    };

    private static Object[] findMenu = {
            "menu.find",
            FIND_CONTENT, REPLACE_TEXT, FIND_NEXT, FIND_PREVIOUS
    };

    private static Object[] transformationMenu = {
            "menu.transformations",
            TO_UPPER, TO_LOWER, TO_CAPITALIZED, TO_TITLED
    };
    private static Object[] menuEdit = {
            "menu.edit",
            EDIT_UNDO, EDIT_REDO,
            null,
            EDIT_CUT, EDIT_COPY, EDIT_PASTE, EDIT_DELETE,
            null,
            findMenu,
            EDIT_SELECT_ALL,
            null,
            GOTO_POSITION,
            null,
            transformationMenu,
            null,
            JOIN_LINES,
    };

    private static Object[] menuView = {
            "menu.view",
            new IActionModel(SHOW_TOOL_BAR, IButtonType.Check),
            new IActionModel(SHOW_STATUS_BAR, IButtonType.Check),
            new IActionModel(SHOW_SIDE_BAR, IButtonType.Check)
    };

    private static Object[] menuBook = {
            "menu.book",
            INSERT_CHAPTER,
            null,
            IMPORT_CHAPTER, EXPORT_CHAPTER,
            null,
            RENAME_CHAPTER,
            null,
            MERGE_CHAPTER,
            null,
            new IActionModel(LOCK_CONTENTS, IButtonType.Check),
            null,
            CHAPTER_PROPERTIES,
            BOOK_EXTENSIONS,
    };

    private static Object[] menuTools = {
            "menu.tools",
    };

    private static Object[] tabMenu = {
            "menu.tab",
            GOTO_NEXT_TAB, GOTO_PREVIOUS_TAB,
            null,
            CLOSE_ACTIVE_TAB, CLOSE_OTHER_TABS, CLOSE_ALL_TABS, CLOSE_UNMODIFIED_TABS
    };
    private static Object[] menuWindow = {
            "menu.window",
            tabMenu
    };

    private static Object[] menuHelp = {
            "menu.help",
            HELP_CONTENTS,
            ABOUT_APP
    };

    static Object[][] mainMenu = {
            menuFile, menuEdit, menuView, menuBook, menuTools, menuWindow,
            menuHelp
    };

    static final int FILE_MENU_INDEX = 0;
    static final int EDIT_MENU_INDEX = 0;
    static final int VIEW_MENU_INDEX = 0;
    static final int BOOK_MENU_INDEX = 0;
    static final int TOOLS_MENU_INDEX = 0;
    static final int WINDOW_MENU_INDEX = 0;
    static final int HELP_MENU_INDEX = 0;

    static Object[] toolBar = {
            NEW_FILE, OPEN_FILE, SAVE_FILE,
            null,
            EDIT_UNDO, EDIT_REDO,
            null,
            EDIT_CUT, EDIT_COPY, EDIT_PASTE,
            null,
            FIND_CONTENT, REPLACE_TEXT, FIND_NEXT, FIND_PREVIOUS
    };
}
