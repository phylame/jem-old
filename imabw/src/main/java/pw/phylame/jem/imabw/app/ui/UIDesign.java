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

package pw.phylame.jem.imabw.app.ui;

import pw.phylame.gaf.ixin.IMenuLabel;
import pw.phylame.gaf.ixin.IMenuModel;
import pw.phylame.jem.imabw.app.Constants;

public class UIDesign implements Constants {
    private static Object[] FILE_NEW_MENU = {
            new IMenuLabel("Menu.FileNew"),
            NEW_FILE, NEW_CHAPTER
    };

    private static Object[] FILE_RECENT_MENU = {
            new IMenuLabel("Menu.FileRecent"),
    };

    public static final int HISTORY_MENU_INDEX = 6;

    private static Object[] FILE_MENU = {
            new IMenuLabel("Menu.File"),
            FILE_NEW_MENU, OPEN_FILE,
            null,
            SAVE_FILE, SAVE_AS_FILE,
            null,
            FILE_RECENT_MENU,
            null,
            EDIT_SETTINGS,
            null,
            FILE_DETAILS,
            null,
            EXIT_APP
    };

    private static Object[] EDIT_FIND_MENU = {
            new IMenuLabel("Menu.EditFind"),
            FIND_CONTENT, REPLACE_TEXT, FIND_NEXT, FIND_PREV
    };

    private static Object[] EDIT_TRANSFORM_MENU = {
            new IMenuLabel("Menu.EditTransform"),
            EDIT_TO_UPPER, EDIT_TO_LOWER, EDIT_TO_CAPITALIZED, EDIT_TO_TITLED
    };

    private static Object[] EDIT_MENU = {
            new IMenuLabel("Menu.Edit"),
            EDIT_UNDO, EDIT_REDO,
            null,
            EDIT_CUT, EDIT_COPY, EDIT_PASTE, EDIT_DELETE,
            null,
            EDIT_FIND_MENU,
            EDIT_SELECT_ALL,
            null,
            GOTO_POSITION,
            null,
            EDIT_TRANSFORM_MENU,
            null,
            EDIT_JOIN_LINES,
    };

    private static Object[] VIEW_MENU = {
            new IMenuLabel("Menu.View"),
            new IMenuModel(VIEW_TOOL_BAR, IMenuModel.MenuType.CHECK),
            new IMenuModel(VIEW_STATUS_BAR, IMenuModel.MenuType.CHECK),
            new IMenuModel(VIEW_SIDE_BAR, IMenuModel.MenuType.CHECK),
    };

    private static Object[] BOOK_MENU = {
            new IMenuLabel("Menu.Book"),
            INSERT_CHAPTER,
            null,
            IMPORT_CHAPTER, EXPORT_CHAPTER,
            null,
            RENAME_CHAPTER,
            null,
            JOIN_CHAPTER,
            null,
            CHAPTER_PROPERTIES, BOOK_EXTRA
    };

    private static Object[] TOOLS_MENU = {
            new IMenuLabel("Menu.Tools"),
    };

    private static Object[] WINDOW_TABS_MENU = {
            new IMenuLabel("Menu.WindowTabs"),
            SELECT_NEXT_TAB, SELECT_PREVIOUS_TAB,
            null,
            CLOSE_ACTIVE_TAB, CLOSE_OTHER_TABS, CLOSE_ALL_TABS, CLOSE_UNMODIFIED_TABS
    };
    private static Object[] WINDOW_MENU      = {
            new IMenuLabel("Menu.Window"),
            WINDOW_TABS_MENU,
    };

    private static Object[] HELP_MENU = {
            new IMenuLabel("Menu.Help"),
            SHOW_HELP,
            ABOUT_APP
    };

    static Object[][] MENU_BAR_MODEL = {
            FILE_MENU,
            EDIT_MENU,
            VIEW_MENU,
            BOOK_MENU,
            TOOLS_MENU,
            WINDOW_MENU,
            HELP_MENU
    };

    static Object[] TOOL_BAR_MODEL = {
            NEW_FILE, OPEN_FILE, SAVE_FILE,
            null,
            EDIT_REDO, EDIT_UNDO,
            null,
            EDIT_CUT, EDIT_COPY, EDIT_PASTE,
            null,
            FIND_CONTENT, FIND_PREV, FIND_NEXT,
    };
}
