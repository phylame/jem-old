/*
 * Copyright 2014 Peng Wan <phylame@163.com> <minexiac@gmail.com>
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

import pw.phylame.imabw.Application;
import pw.phylame.ixin.ITextEdit;
import pw.phylame.ixin.IToolkit;
import pw.phylame.ixin.com.IAction;
import pw.phylame.ixin.com.IMenuModel;
import pw.phylame.ixin.com.IMenuLabel;

import static pw.phylame.imabw.Constants.*;

/**
 * UI design for Imabw.
 */
public class UIDesign {

    private static Application app = Application.getApplication();

    // *******************
    // ** Menu actions  **
    // *******************
    // {name, icon, mnemonic, accelerator, command, toolTip} or {null} is separator

    /* File */
    public static Object[][] FILE_MENU_ACTIONS = {
            {NEW_FILE,
                    app.getText("Menu.File.New"), app.getText("Menu.File.New.Icon"),
                    app.getText("Menu.File.New.Mnemonic"), app.getText("Menu.File.New.Shortcut"),
                    app.getText("Menu.File.New.Tip")},
            {OPEN_FILE,
                    app.getText("Menu.File.Open"), app.getText("Menu.File.Open.Icon"),
                    app.getText("Menu.File.Open.Mnemonic"), app.getText("Menu.File.Open.Shortcut"),
                    app.getText("Menu.File.Open.Tip")},
            {SAVE_FILE,
                    app.getText("Menu.File.Save"), app.getText("Menu.File.Save.Icon"),
                    app.getText("Menu.File.Save.Mnemonic"), app.getText("Menu.File.Save.Shortcut"),
                    app.getText("Menu.File.Save.Tip")},
            {SAVE_AS_FILE,
                    app.getText("Menu.File.SaveAs"), app.getText("Menu.File.SaveAs.Icon"),
                    app.getText("Menu.File.SaveAs.Mnemonic"), app.getText("Menu.File.SaveAs.Shortcut"),
                    app.getText("Menu.File.SaveAs.Tip")},
            {FILE_DETAILS,
                    app.getText("Menu.File.Details"), app.getText("Menu.File.Details.Icon"),
                    app.getText("Menu.File.Details.Mnemonic"), app.getText("Menu.File.Details.Shortcut"),
                    app.getText("Menu.File.Details.Tip"), false},
            {EXIT_APP,
                    app.getText("Menu.File.Exit"), app.getText("Menu.File.Exit.Icon"),
                    app.getText("Menu.File.Exit.Mnemonic"), app.getText("Menu.File.Exit.Shortcut"),
                    app.getText("Menu.File.Exit.Tip")}
    };

    /* Edit */
    public static Object[][] EDIT_MENU_ACTIONS = {
            {EDIT_PREFERENCE,
                    app.getText("Menu.Edit.Preference"), app.getText("Menu.Edit.Preference.Icon"),
                    app.getText("Menu.Edit.Preference.Mnemonic"), app.getText("Menu.Edit.Preference.Shortcut"),
                    app.getText("Menu.Edit.Preference.Tip")}
    };

    /* View */
    public static Object[][] VIEW_MENU_ACTIONS = {
            {SHOW_TOOLBAR,
                    app.getText("Menu.View.ShowToolbar"), app.getText("Menu.View.ShowToolbar.Icon"),
                    app.getText("Menu.View.ShowToolbar.Mnemonic"), app.getText("Menu.View.ShowToolbar.Shortcut"),
                    app.getText("Menu.View.ShowToolbar.Tip")},
            {SHOW_STATUSBAR,
                    app.getText("Menu.View.ShowStatusbar"), app.getText("Menu.View.ShowStatusbar.Icon"),
                    app.getText("Menu.View.ShowStatusbar.Mnemonic"), app.getText("Menu.View.ShowStatusbar.Shortcut"),
                    app.getText("Menu.View.ShowStatusbar.Tip")},
            {SHOW_SIDEBAR,
                    app.getText("Menu.View.ShowSidebar"), app.getText("Menu.View.ShowSidebar.Icon"),
                    app.getText("Menu.View.ShowSidebar.Mnemonic"), app.getText("Menu.View.ShowSidebar.Shortcut"),
                    app.getText("Menu.View.ShowSidebar.Tip")}
    };

    /* Search */
    public static Object[][] SEARCH_MENU_ACTIONS = {
            {FIND_TEXT,
                    app.getText("Menu.Search.Find"), app.getText("Menu.Search.Find.Icon"),
                    app.getText("Menu.Search.Find.Mnemonic"), app.getText("Menu.Search.Find.Shortcut"),
                    app.getText("Menu.Search.Find.Tip"), false},
            {FIND_NEXT,
                    app.getText("Menu.Search.Next"), app.getText("Menu.Search.Next.Icon"),
                    app.getText("Menu.Search.Next.Mnemonic"), app.getText("Menu.Search.Next.Shortcut"),
                    app.getText("Menu.Search.Next.Tip"), false},
            {FIND_PREVIOUS,
                    app.getText("Menu.Search.Previous"), app.getText("Menu.Search.Previous.Icon"),
                    app.getText("Menu.Search.Previous.Mnemonic"), app.getText("Menu.Search.Previous.Shortcut"),
                    app.getText("Menu.Search.Previous.Tip"), false},
            {FIND_AND_REPLACE,
                    app.getText("Menu.Search.Replace"), app.getText("Menu.Search.Replace.Icon"),
                    app.getText("Menu.Search.Replace.Mnemonic"), app.getText("Menu.Search.Replace.Shortcut"),
                    app.getText("Menu.Search.Replace.Tip"), false},
            {GO_TO_POSITION,
                    app.getText("Menu.Search.Goto"), app.getText("Menu.Search.Goto.Icon"),
                    app.getText("Menu.Search.Goto.Mnemonic"), app.getText("Menu.Search.Goto.Shortcut"),
                    app.getText("Menu.Search.Goto.Tip"), false}
    };

    /* Tools */
    public static Object[][] TOOLS_MENU_ACTIONS = {
            {EDIT_META,
                    app.getText("Menu.Tools.Meta"), app.getText("Menu.Tools.Meta.Icon"),
                    app.getText("Menu.Tools.Meta.Mnemonic"), app.getText("Menu.Tools.Meta.Shortcut"),
                    app.getText("Menu.Tools.Meta.Tip")}
    };

    /* Help */
    public static Object[][] HELP_MENU_ACTIONS = {
            {SHOW_ABOUT,
                    app.getText("Menu.Help.About"), app.getText("Menu.Help.About.Icon"),
                    app.getText("Menu.Help.About.Mnemonic"), app.getText("Menu.Help.About.Shortcut"),
                    app.getText("Menu.Help.About.Tip")}
    };

    public static Object[][][] MENU_ACTIONS = {
            FILE_MENU_ACTIONS, EDIT_MENU_ACTIONS, VIEW_MENU_ACTIONS, SEARCH_MENU_ACTIONS,
            TOOLS_MENU_ACTIONS, HELP_MENU_ACTIONS
    };

    // ******************
    // ** Menu model   **
    // ******************

    /* File */
    public static Object[] FILE_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.File"), null, app.getText("Menu.File.Mnemonic")),
            NEW_FILE, OPEN_FILE,
            null,
            SAVE_FILE, SAVE_AS_FILE,
            null,
            FILE_DETAILS,
            null,
            EXIT_APP
    };

    /* Edit */
    public static Object[] EDIT_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.Edit"), null, app.getText("Menu.Edit.Mnemonic")),
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
    public static Object[] VIEW_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.View"), null, app.getText("Menu.View.Mnemonic")),
            new IMenuModel(SHOW_TOOLBAR, IMenuModel.MenuType.CHECK, true),
            new IMenuModel(SHOW_STATUSBAR, IMenuModel.MenuType.CHECK, true),
            new IMenuModel(SHOW_SIDEBAR, IMenuModel.MenuType.CHECK, true),
    };

    /* Search */
    public static Object[] SEARCH_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.Search"), null, app.getText("Menu.Search.Mnemonic")),
            FIND_TEXT, FIND_NEXT, FIND_PREVIOUS,
            null,
            FIND_AND_REPLACE,
            null,
            GO_TO_POSITION
    };

    /* Tools */
    public static Object[] TOOLS_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.Tools"), null, app.getText("Menu.Tools.Mnemonic")),
            EDIT_META
    };

    /* Help */
    public static Object[] HELP_MENU_MODEL = {
            new IMenuLabel(app.getText("Menu.Help"), null, app.getText("Menu.Help.Mnemonic")),
            SHOW_ABOUT
    };

    /* Menu bar */
    public static Object[][] MENU_BAR_MODEL = {
            FILE_MENU_MODEL, EDIT_MENU_MODEL, VIEW_MENU_MODEL, SEARCH_MENU_MODEL, TOOLS_MENU_MODEL,
            HELP_MENU_MODEL
    };

    /* Tool bar */
    public static Object[] TOOL_BAR_MODEL = {
            NEW_FILE, OPEN_FILE, SAVE_FILE,
            null,
            ITextEdit.UNDO, ITextEdit.REDO, null, ITextEdit.CUT, ITextEdit.COPY, ITextEdit.PASTE,
            null,
            FIND_TEXT, FIND_AND_REPLACE, GO_TO_POSITION,
            null,
            EDIT_META
    };

    /* Tab control */
    public static Object[][] TAB_POPUP_MENU_ACTIONS = {
            {TAB_CLOSE,
                    app.getText("Frame.Tab.Menu.Close"),
                    app.getText("Frame.Tab.Menu.Close.Icon"),
                    app.getText("Frame.Tab.Menu.Close.Mnemonic"),
                    app.getText("Frame.Tab.Menu.Close.Shortcut"),
                    app.getText("Frame.Tab.Menu.Close.Tip")},
            {TAB_CLOSE_OTHERS,
                    app.getText("Frame.Tab.Menu.CloseOthers"),
                    app.getText("Frame.Tab.Menu.CloseOthers.Icon"),
                    app.getText("Frame.Tab.Menu.CloseOthers.Mnemonic"),
                    app.getText("Frame.Tab.Menu.CloseOthers.Shortcut"),
                    app.getText("Frame.Tab.Menu.CloseOthers.Tip")},
            {TAB_CLOSE_ALL,
                    app.getText("Frame.Tab.Menu.CloseAll"),
                    app.getText("Frame.Tab.Menu.CloseAll.Icon"),
                    app.getText("Frame.Tab.Menu.CloseAll.Mnemonic"),
                    app.getText("Frame.Tab.Menu.CloseAll.Shortcut"),
                    app.getText("Frame.Tab.Menu.CloseAll.Tip")},
            {TAB_CLOSE_UNMODIFIED,
                    app.getText("Frame.Tab.Menu.CloseUnmodified"),
                    app.getText("Frame.Tab.Menu.CloseUnmodified.Icon"),
                    app.getText("Frame.Tab.Menu.CloseUnmodified.Mnemonic"),
                    app.getText("Frame.Tab.Menu.CloseUnmodified.Shortcut"),
                    app.getText("Frame.Tab.Menu.CloseUnmodified.Tip")},
            {TAB_SELECT_NEXT,
                    app.getText("Frame.Tab.Menu.SelectNext"),
                    app.getText("Frame.Tab.Menu.SelectNext.Icon"),
                    app.getText("Frame.Tab.Menu.SelectNext.Mnemonic"),
                    app.getText("Frame.Tab.Menu.SelectNext.Shortcut"),
                    app.getText("Frame.Tab.Menu.SelectNext.Tip")},
            {TAB_SELECT_PREVIOUS,
                    app.getText("Frame.Tab.Menu.SelectPrevious"),
                    app.getText("Frame.Tab.Menu.SelectPrevious.Icon"),
                    app.getText("Frame.Tab.Menu.SelectPrevious.Mnemonic"),
                    app.getText("Frame.Tab.Menu.SelectPrevious.Shortcut"),
                    app.getText("Frame.Tab.Menu.SelectPrevious.Tip")}
    };

    /* Tree options */
    public static Object[][] TREE_POPUP_MENU_ACTIONS = {
            {TREE_NEW,
                    app.getText("Frame.Tree.Menu.New"),
                    app.getText("Frame.Tree.Menu.New.Icon"),
                    app.getText("Frame.Tree.Menu.New.Mnemonic"),
                    app.getText("Frame.Tree.Menu.New.Shortcut"),
                    app.getText("Frame.Tree.Menu.New.Tip")},
            {TREE_INSERT,
                    app.getText("Frame.Tree.Menu.Insert"),
                    app.getText("Frame.Tree.Menu.Insert.Icon"),
                    app.getText("Frame.Tree.Menu.Insert.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Insert.Shortcut"),
                    app.getText("Frame.Tree.Menu.Insert.Tip")},
            {TREE_RENAME,
                    app.getText("Frame.Tree.Menu.Rename"),
                    app.getText("Frame.Tree.Menu.Rename.Icon"),
                    app.getText("Frame.Tree.Menu.Rename.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Rename.Shortcut"),
                    app.getText("Frame.Tree.Menu.Rename.Tip")},
            {TREE_MOVE,
                    app.getText("Frame.Tree.Menu.Move"),
                    app.getText("Frame.Tree.Menu.Move.Icon"),
                    app.getText("Frame.Tree.Menu.Move.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Move.Shortcut"),
                    app.getText("Frame.Tree.Menu.Move.Tip")},
            {TREE_DELETE,
                    app.getText("Frame.Tree.Menu.Delete"),
                    app.getText("Frame.Tree.Menu.Delete.Icon"),
                    app.getText("Frame.Tree.Menu.Delete.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Delete.Shortcut"),
                    app.getText("Frame.Tree.Menu.Delete.Tip")},
            {TREE_MERGE,
                    app.getText("Frame.Tree.Menu.Merge"),
                    app.getText("Frame.Tree.Menu.Merge.Icon"),
                    app.getText("Frame.Tree.Menu.Merge.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Merge.Shortcut"),
                    app.getText("Frame.Tree.Menu.Merge.Tip")},
            {TREE_PROPERTIES,
                    app.getText("Frame.Tree.Menu.Properties"),
                    app.getText("Frame.Tree.Menu.Properties.Icon"),
                    app.getText("Frame.Tree.Menu.Properties.Mnemonic"),
                    app.getText("Frame.Tree.Menu.Properties.Shortcut"),
                    app.getText("Frame.Tree.Menu.Properties.Tip")},
            {TREE_SAVE_AS,
                    app.getText("Frame.Tree.Menu.SaveAs"),
                    app.getText("Frame.Tree.Menu.SaveAs.Icon"),
                    app.getText("Frame.Tree.Menu.SaveAs.Mnemonic"),
                    app.getText("Frame.Tree.Menu.SaveAs.Shortcut"),
                    app.getText("Frame.Tree.Menu.SaveAs.Tip")},
//            {TREE_IMPORT,
//                    app.getText("Frame.Tree.Menu.Import"),
//                    app.getText("Frame.Tree.Menu.Import.Icon"),
//                    app.getText("Frame.Tree.Menu.Import.Mnemonic"),
//                    app.getText("Frame.Tree.Menu.Import.Shortcut"),
//                    app.getText("Frame.Tree.Menu.Import.Tip")}
    };


    /* Tab control */
    public static Object[] TAB_POPUP_MENU_MODEL = {
            TAB_CLOSE, TAB_CLOSE_OTHERS, TAB_CLOSE_ALL, TAB_CLOSE_UNMODIFIED,
            null,
            TAB_SELECT_NEXT,
            TAB_SELECT_PREVIOUS
    };

    /* Tree option */
    public static Object[] TREE_POPUP_MENU_MODEL = {
            TREE_NEW,
            TREE_INSERT,
            null,
//            TREE_IMPORT,
            TREE_SAVE_AS,
            null,
            TREE_RENAME, TREE_MOVE,
            null,
            TREE_DELETE,
            null,
            TREE_MERGE,
            null,
            TREE_PROPERTIES
    };

    /* set ITextEdit text and icons */
    static {
        Object[][] editModel = new Object[][] {
                {ITextEdit.UNDO, "Undo"},
                {ITextEdit.REDO, "Redo"},
                {ITextEdit.CUT, "Cut"},
                {ITextEdit.COPY, "Copy"},
                {ITextEdit.PASTE, "Paste"},
                {ITextEdit.DELETE, "Delete"},
                {ITextEdit.SELECT_ALL, "SelectAll"},
        };
        for (Object[] model: editModel) {
            IAction action = ITextEdit.getEditAction(model[0]);
            String key = "Editor.Menu."+model[1];
            action.setText(app.getText(key));
            action.setIcon(pw.phylame.ixin.IToolkit.createImageIcon(app.getText(key + ".Icon")));
            action.setMnemonic(IToolkit.getMnemonic(app.getText(key + ".Mnemonic")));
            action.setAccelerator(IToolkit.getKeyStroke(app.getText(key + ".Shortcut")));
            action.setStatusTip(app.getText(key + ".Tip"));
        }
    }
}
