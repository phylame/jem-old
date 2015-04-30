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
    /** Version of the Imabw */
    public static final String VERSION = "2.0-SNAPSHOT";

    /** Name of {@code ResourceBundle} file */
    public static final String I18N_PATH = "res/i18n/imabw";

    /** User home of Imabw */
    public static final String IMABW_HOME = String.format("%s/.imabw", System.getProperty("user.home"));

    /** Settings file */
    public static final String SETTINGS_FILE = IMABW_HOME + "/" + "settings.prop";

    // *******************
    // ** Menu commands **
    // *******************

    /* Book file operation */
    public static final String NEW_FILE = "new-file";
    public static final String OPEN_FILE = "open-file";
    public static final String SAVE_FILE = "save-file";
    public static final String SAVE_AS_FILE = "save-as-file";
    public static final String FILE_DETAILS = "file-details";
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
    public static final String BOOK_ATTRIBUTES = "book-attributes";
    public static final String EXTRA_ITEMS = "extra-items";

    /* Help */
    public static final String SHOW_ABOUT = "show-about";

    /* Tree options */
    public static final String NEW_CHAPTER = "new-chapter";
    public static final String INSERT_CHAPTER = "insert-chapter";
    public static final String RENAME_CHAPTER = "rename-chapter";
    public static final String MOVE_CHAPTER = "move-chapter";
    public static final String DELETE_CHAPTER = "delete-chapter";
    public static final String MERGE_CHAPTER = "merge-chapter";
    public static final String TREE_PROPERTIES = "tree-property";
    public static final String SAVE_CHAPTER = "save-chapter";
    public static final String IMPORT_CHAPTER = "import-chapter";
    public static final String SEARCH_CHAPTER = "search-chapter";
    public static final String REFRESH_CONTENTS = "refresh-contents";
    public static final String LOCK_CONTENTS = "lock-contents";

    /* Tab control */
    public static final String CLOSE_TAB = "close-tab";
    public static final String CLOSE_OTHER_TABS = "close-other-tabs";
    public static final String CLOSE_ALL_TABS = "close-all-tabs";
    public static final String CLOSE_UNMODIFIED_TABS = "close-unmodified-tabs";
    public static final String SELECT_NEXT_TAB = "select-next-tab";
    public static final String SELECT_PREVIOUS_TAB = "select-previous-tab";
}
