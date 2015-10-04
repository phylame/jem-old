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

package pw.phylame.jem.imabw.app;

/**
 * Constants for Imabw.
 */
public interface Constants {
    // ***********************
    // ** Imabw information **
    // ***********************
    String NAME    = "imabw";
    String VERSION = "2.1.0";

    String SETTINGS_HOME = "config/";

    String I18N_NAME = "imabw";

    // ******************
    // ** Menu Actions **
    // ******************
    String ABOUT_APP     = "Help.About";
    String SHOW_HELP     = "Help.Help";
    String EXIT_APP      = "File.Exit";
    String EDIT_SETTINGS = "Edit.Settings";

    String NEW_FILE     = "File.New";
    String OPEN_FILE    = "File.Open";
    String SAVE_FILE    = "File.Save";
    String SAVE_AS_FILE = "File.SaveAs";
    String FILE_DETAILS = "File.Details";
    String CLEAR_HISTORY = "File.ClearHistory";

    String EDIT_UNDO       = "Edit.Undo";
    String EDIT_REDO       = "Edit.Redo";
    String EDIT_CUT        = "Edit.Cut";
    String EDIT_COPY       = "Edit.Copy";
    String EDIT_PASTE      = "Edit.Paste";
    String EDIT_DELETE     = "Edit.Delete";
    String EDIT_SELECT_ALL = "Edit.SelectAll";

    String[] EDIT_ACTIONS = {EDIT_UNDO, EDIT_REDO, EDIT_CUT, EDIT_COPY, EDIT_PASTE, EDIT_DELETE,
            EDIT_SELECT_ALL};

    String FIND_CONTENT  = "Search.Find";
    String FIND_NEXT     = "Search.Next";
    String FIND_PREV     = "Search.Prev";
    String REPLACE_TEXT  = "Search.Replace";
    String GOTO_POSITION = "Search.Goto";

    String EDIT_JOIN_LINES     = "Edit.JoinLines";
    String EDIT_TO_UPPER       = "Edit.ToUpper";
    String EDIT_TO_LOWER       = "Edit.ToLower";
    String EDIT_TO_CAPITALIZED = "Edit.ToCapitalized";
    String EDIT_TO_TITLED      = "Edit.ToTitled";

    String[] TEXT_ACTIONS = {REPLACE_TEXT, EDIT_JOIN_LINES, EDIT_TO_UPPER, EDIT_TO_LOWER,
            EDIT_TO_CAPITALIZED, EDIT_TO_TITLED};

    String VIEW_TOOL_BAR   = "View.ToolBar";
    String VIEW_STATUS_BAR = "View.StatusBar";
    String VIEW_SIDE_BAR   = "View.SideBar";

    String NEW_CHAPTER        = "Book.New";
    String INSERT_CHAPTER     = "Book.Insert";
    String EXPORT_CHAPTER     = "Book.Export";
    String IMPORT_CHAPTER     = "Book.Import";
    String RENAME_CHAPTER     = "Book.Rename";
    String JOIN_CHAPTER       = "Book.Join";
    String CHAPTER_PROPERTIES = "Book.Properties";
    String BOOK_EXTRA         = "Book.Extra";

    String[] TREE_ACTIONS = {NEW_CHAPTER, INSERT_CHAPTER, EXPORT_CHAPTER, IMPORT_CHAPTER,
            RENAME_CHAPTER, JOIN_CHAPTER, CHAPTER_PROPERTIES, BOOK_EXTRA};

    String CLOSE_ACTIVE_TAB      = "Tabs.CloseActive";
    String CLOSE_ALL_TABS        = "Tabs.CloseAll";
    String CLOSE_OTHER_TABS      = "Tabs.CloseOther";
    String CLOSE_UNMODIFIED_TABS = "Tabs.CloseUnmodified";
    String SELECT_NEXT_TAB       = "Tabs.NextTab";
    String SELECT_PREVIOUS_TAB   = "Tabs.PrevTab";

    String[] TAB_ACTIONS = {CLOSE_ACTIVE_TAB, CLOSE_ALL_TABS, CLOSE_OTHER_TABS, CLOSE_UNMODIFIED_TABS,
            SELECT_NEXT_TAB, SELECT_PREVIOUS_TAB};

}
