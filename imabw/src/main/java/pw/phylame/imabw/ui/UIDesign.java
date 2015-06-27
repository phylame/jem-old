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

package pw.phylame.imabw.ui;

import pw.phylame.pat.ixin.IMenuLabel;
import pw.phylame.pat.ixin.IMenuModel;

import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.Constants;

import pw.phylame.imabw.ui.com.ITextEdit;

/**
 * UI design for Imabw.
 */
public class UIDesign implements Constants {

    private static Imabw app = Imabw.getInstance();

    // *******************
    // ** Menu actions  **
    // *******************
    // {name, icon, mnemonic, accelerator, command, toolTip} or {null} is separator

    /* File */
    public static Object[][] FILE_MENU_ACTIONS = {
            {NEW_FILE, "Menu.File.New"},
            {OPEN_FILE, "Menu.File.Open"},
            {SAVE_FILE, "Menu.File.Save"},
            {SAVE_AS_FILE, "Menu.File.SaveAs"},
            {FILE_DETAILS, "Menu.File.Details", false},
            {EXIT_APP, "Menu.File.Exit"}
    };
    public static Object[]   FILE_MENU_MODEL   = {
            new IMenuLabel("Menu.File", app),
            NEW_FILE, OPEN_FILE,
            null,
            SAVE_FILE, SAVE_AS_FILE,
            null,
            FILE_DETAILS,
            null,
            EXIT_APP
    };

    /* Edit */
    public static Object[][] EDIT_MENU_ACTIONS = {
            {EDIT_PREFERENCE, "Menu.Edit.Preference"}
    };
    public static Object[]   EDIT_MENU_MODEL   = {
            new IMenuLabel("Menu.Edit", app),
            ITextEdit.UNDO,
            ITextEdit.REDO,
            null,
            ITextEdit.CUT,
            ITextEdit.COPY,
            ITextEdit.PASTE,
            ITextEdit.DELETE,
            null,
            ITextEdit.SELECT_ALL,
            null,
            EDIT_PREFERENCE
    };

    /* View */
    public static Object[][] VIEW_MENU_ACTIONS = {
            {SHOW_SIDEBAR, "Menu.View.ShowSidebar"}
    };
    public static Object[]   VIEW_MENU_MODEL   = {
            new IMenuLabel("Menu.View", app),
            new IMenuModel(SHOW_TOOLBAR, IMenuModel.MenuType.CHECK, true),
            new IMenuModel(SHOW_STATUSBAR, IMenuModel.MenuType.CHECK, true),
            new IMenuModel(SHOW_SIDEBAR, IMenuModel.MenuType.CHECK, true),
    };

    /* Search */
    public static Object[][] SEARCH_MENU_ACTIONS = {
            {FIND_TEXT, "Menu.Search.Find", false},
            {FIND_NEXT, "Menu.Search.Next", false},
            {FIND_PREVIOUS, "Menu.Search.Previous", false},
            {FIND_AND_REPLACE, "Menu.Search.Replace", false},
            {GO_TO_POSITION, "Menu.Search.Goto", false}
    };
    public static Object[]   SEARCH_MENU_MODEL   = {
            new IMenuLabel("Menu.Search", app),
            FIND_TEXT, FIND_NEXT, FIND_PREVIOUS,
            null,
            FIND_AND_REPLACE,
            null,
            GO_TO_POSITION
    };

    /* Tools */
    public static Object[][] TOOLS_MENU_ACTIONS = {
            {BOOK_ATTRIBUTES, "Menu.Tools.Meta"},
            {EXTRA_ITEMS, "Menu.Tools.Extra"}
    };
    public static Object[]   TOOLS_MENU_MODEL   = {
            new IMenuLabel("Menu.Tools", app),
            BOOK_ATTRIBUTES,
//            EXTRA_ITEMS
    };

    /* Help */
    public static Object[][] HELP_MENU_ACTIONS = {
            {DO_ACTION, "Menu.Help.Action"},
            {SHOW_ABOUT, "Menu.Help.About"}
    };
    public static Object[]   HELP_MENU_MODEL   = {
            new IMenuLabel("Menu.Help", app),
//            DO_ACTION,
            SHOW_ABOUT
    };

    public static Object[][][] MENU_ACTIONS = {
            FILE_MENU_ACTIONS, EDIT_MENU_ACTIONS, VIEW_MENU_ACTIONS, SEARCH_MENU_ACTIONS,
            TOOLS_MENU_ACTIONS, HELP_MENU_ACTIONS
    };

    /* Menu bar */
    public static Object[][] MENU_BAR_MODEL = {
            FILE_MENU_MODEL, EDIT_MENU_MODEL, VIEW_MENU_MODEL, SEARCH_MENU_MODEL, TOOLS_MENU_MODEL,
            HELP_MENU_MODEL
    };

    /* Tool bar */
    public static String[] TOOL_BAR_MODEL = {
            NEW_FILE, OPEN_FILE, SAVE_FILE,
            null,
            ITextEdit.UNDO, ITextEdit.REDO, null, ITextEdit.CUT, ITextEdit.COPY, ITextEdit.PASTE,
            null,
            FIND_TEXT, FIND_AND_REPLACE, GO_TO_POSITION,
            null,
            BOOK_ATTRIBUTES
    };

    /* Tab control actions */
    public static Object[][] TAB_POPUP_MENU_ACTIONS = {
            {CLOSE_ACTIVE_TAB, "Frame.Tab.Menu.Close"},
            {CLOSE_OTHER_TABS, "Frame.Tab.Menu.CloseOthers"},
            {CLOSE_ALL_TABS, "Frame.Tab.Menu.CloseAll"},
            {CLOSE_UNMODIFIED_TABS, "Frame.Tab.Menu.CloseUnmodified"},
            {SELECT_NEXT_TAB, "Frame.Tab.Menu.SelectNext"},
            {SELECT_PREVIOUS_TAB, "Frame.Tab.Menu.SelectPrevious"}
    };

    public static Object[] TAB_POPUP_MENU_MODEL = {
            CLOSE_ACTIVE_TAB, CLOSE_OTHER_TABS, CLOSE_ALL_TABS, CLOSE_UNMODIFIED_TABS,
            null,
            SELECT_NEXT_TAB,
            SELECT_PREVIOUS_TAB
    };

    /* Tree options actions */
    public static Object[][] TREE_POPUP_MENU_ACTIONS = {
            {NEW_CHAPTER, "Frame.Tree.Menu.New"},
            {INSERT_CHAPTER, "Frame.Tree.Menu.Insert"},
            {RENAME_CHAPTER, "Frame.Tree.Menu.Rename"},
            {MOVE_CHAPTER, "Frame.Tree.Menu.Move"},
            {DELETE_CHAPTER, "Frame.Tree.Menu.Delete"},
            {MERGE_CHAPTER, "Frame.Tree.Menu.Merge"},
            {TREE_PROPERTIES, "Frame.Tree.Menu.Properties"},
            {SAVE_CHAPTER, "Frame.Tree.Menu.SaveAs"},
//            {IMPORT_CHAPTER, "Frame.Tree.Menu.Import"}
    };

    public static Object[] TREE_POPUP_MENU_MODEL = {
            NEW_CHAPTER,
            INSERT_CHAPTER,
            null,
//            IMPORT_CHAPTER,
            SAVE_CHAPTER,
            null,
            RENAME_CHAPTER, MOVE_CHAPTER,
            null,
            DELETE_CHAPTER,
            null,
            MERGE_CHAPTER,
            null,
            TREE_PROPERTIES
    };
}
