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
public interface Constants {
    /** Inner name of Imabw */
    String INNER_NAME = "imabw";

    /** Version of the Imabw */
    String VERSION = "2.0-SNAPSHOT";

    /** Readable version string */
    String RELEASE = "II";

    /** Name of language {@code ResourceBundle} file */
    String I18N_PATH = "res/i18n/imabw";

    /** User home of Imabw */
    String IMABW_HOME = String.format("%s/.imabw", System.getProperty("user.home"));

    /** Settings file */
    String SETTINGS_FILE = IMABW_HOME + "/settings.prop";

    /** Plugins directory */
    String PLUGINS_ROOT = IMABW_HOME + "/plugins";

    // **************************
    // ** Common menu commands **
    // **************************

    /* File */
    String NEW_FILE     = "new-file";
    String OPEN_FILE    = "open-file";
    String SAVE_FILE    = "save-file";
    String SAVE_AS_FILE = "save-as-file";
    String FILE_DETAILS = "file-details";
    String EXIT_APP     = "exit-app";

    String EDIT_PREFERENCE = "edit-settings";

    /* View */
    String SHOW_TOOLBAR   = "show-toolbar";
    String SHOW_STATUSBAR = "show-statusbar";
    String SHOW_SIDEBAR   = "show-sidebar";

    /* Search */
    String FIND_TEXT        = "find-text";
    String FIND_NEXT        = "find-next";
    String FIND_PREVIOUS    = "find-previous";
    String FIND_AND_REPLACE = "find-and-replace";
    String GO_TO_POSITION   = "goto-position";

    /* Tools */
    String BOOK_ATTRIBUTES = "book-attributes";
    String EXTRA_ITEMS     = "extra-items";

    /* Help */
    String SHOW_ABOUT = "show-about";

    // ****************************
    // ** Contents tree commands **
    // ****************************

    String VIEW_CHAPTER     = "view-chapter";
    String NEW_CHAPTER      = "new-chapter";
    String INSERT_CHAPTER   = "insert-chapter";
    String RENAME_CHAPTER   = "rename-chapter";
    String MOVE_CHAPTER     = "move-chapter";
    String DELETE_CHAPTER   = "delete-chapter";
    String MERGE_CHAPTER    = "merge-chapter";
    String TREE_PROPERTIES  = "tree-property";
    String SAVE_CHAPTER     = "save-chapter";
    String IMPORT_CHAPTER   = "import-chapter";
    String SEARCH_CHAPTER   = "search-chapter";
    String REFRESH_CONTENTS = "refresh-contents";
    String LOCK_CONTENTS    = "lock-contents";

    // ****************************
    // ** Tabbed editor commands **
    // ****************************

    String CLOSE_ACTIVE_TAB      = "close-active-tab";
    String CLOSE_OTHER_TABS      = "close-other-tabs";
    String CLOSE_ALL_TABS        = "close-all-tabs";
    String CLOSE_UNMODIFIED_TABS = "close-unmodified-tabs";
    String SELECT_NEXT_TAB       = "select-next-tab";
    String SELECT_PREVIOUS_TAB   = "select-previous-tab";
}
