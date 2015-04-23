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

package pw.phylame.imabw;

/**
 * Constants for Imabw.
 */
public final class Constants {
    /** Version of the Imabw */
    public static final String VERSION = "1.8.1";

    /** Name of {@code ResourceBundle} file */
    public static final String I18N_PATH = "res/i18n/imabw";

    /** User home of Imabw */
    public static final String IMABW_HOME = String.format("%s/.imabw", System.getProperty("user.home"));

    // *******************
    // ** Menu commands **
    // *******************

    /* Book file operation */
    public static final String NEW_FILE = "new-file";
    public static final String OPEN_FILE = "open-file";
    public static final String SAVE_FILE = "save-file";
    public static final String SAVE_AS_FILE = "save-as-file";
    public static final String EXIT_APP = "exit-app";

    public static final String EDIT_PREFERENCE = "edit-settings";

    /* View */
    public static final String SHOW_TOOLBAR = "show-toolbar";
    public static final String SHOW_STATUSBAR = "show-statusbar";
    public static final String SHOW_SIDEBAR = "show-sidebar";

    /* Search */
    public static final String FIND_TEXT = "find-text";
    public static final String FIND_NEXT = "find-next";
    public static final String FIND_PREVIOUS = "find-previous";
    public static final String FIND_AND_REPLACE = "find-and-replace";
    public static final String GO_TO_POSITION = "goto-position";

    /* Tools */
    public static final String EDIT_META = "edit-meta";
    public static final String EDIT_EXTRA = "edit-extra";

    /* Help */
    public static final String SHOW_ABOUT = "show-about";

    /* Tree options */
    public static final String TREE_NEW = "tree-new";
    public static final String TREE_INSERT = "tree-insert";
    public static final String TREE_RENAME = "tree-rename";
    public static final String TREE_MOVE = "tree-move";
    public static final String TREE_DELETE = "tree-delete";
    public static final String TREE_MERGE = "tree-merge";
    public static final String TREE_PROPERTIES = "tree-property";
    public static final String TREE_SAVE_AS = "tree-save-as";
    public static final String TREE_IMPORT = "tree-import";

    /* Tab control */
    public static final String TAB_CLOSE = "close-tab";
    public static final String TAB_CLOSE_OTHERS = "close-other-tabs";
    public static final String TAB_CLOSE_ALL = "close-all-tabs";
    public static final String TAB_CLOSE_UNMODIFIED = "close-unmodified-tabs";
    public static final String TAB_SELECT_NEXT = "select-next-tab";
    public static final String TAB_SELECT_PREVIOUS = "select-previous-tab";
}
