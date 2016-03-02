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

package pw.phylame.imabw.app;

import pw.phylame.gaf.ixin.IxinUtilities;

/**
 * Constants for Imabw.
 */
public interface Constants {
    // meta data
    String NAME = "imabw";
    String VERSION = "2.2.1";
    String DOCUMENT = "https://git.oschina.net/phylame/pw-books";

    String SETTINGS_HOME = "config/";

    String RESOURCE_DIR = "pw/phylame/imabw/res/";
    String IMAGE_DIR = "gfx/";
    String I18N_DIR = "pw/phylame/imabw/res/i18n/";
    String I18N_NAME = I18N_DIR + "imabw";


    // default config
    int MAX_HISTORY_LIMITS = 28;
    int DEFAULT_HISTORY_LIMITS = 18;
    String DEFAULT_LAF_THEME = IxinUtilities.DEFAULT_LAF_NAME;
    String DEFAULT_ICON_SET = "default";

    // viewer operations
    String SHOW_TOOL_BAR = "showToolBar";
    String LOCK_TOOL_BAR = "lockToolBar";
    String SHOW_STATUS_BAR = "showStatusBar";
    String SHOW_SIDE_BAR = "showSideBar";

    // file operations
    String NEW_FILE = "newFile";
    String OPEN_FILE = "openFile";
    String SAVE_FILE = "saveFile";
    String SAVE_AS_FILE = "saveAsFile";
    String FILE_DETAILS = "fileDetails";
    String CLEAR_HISTORY = "clearHistory";

    // edit operations
    String EDIT_UNDO = "editUndo";
    String EDIT_REDO = "editRedo";
    String EDIT_CUT = "editCut";
    String EDIT_COPY = "editCopy";
    String EDIT_PASTE = "editPaste";
    String EDIT_DELETE = "editDelete";
    String EDIT_SELECT_ALL = "editSelectAll";

    String[] EDIT_COMMANDS = {
            EDIT_UNDO, EDIT_REDO, EDIT_CUT, EDIT_COPY, EDIT_PASTE, EDIT_DELETE,
            EDIT_SELECT_ALL
    };

    // find operations
    String FIND_CONTENT = "findContent";
    String FIND_NEXT = "findNext";
    String FIND_PREVIOUS = "findPrevious";
    String GOTO_POSITION = "gotoPosition";

    String[] FIND_COMMANDS = {
            FIND_CONTENT, FIND_NEXT, FIND_PREVIOUS, GOTO_POSITION
    };

    // text edit operations
    String REPLACE_TEXT = "replaceText";
    String TO_LOWER = "lowerText";
    String TO_UPPER = "upperText";
    String TO_TITLED = "titleText";
    String TO_CAPITALIZED = "capitalizeText";
    String JOIN_LINES = "joinLines";

    String[] TEXT_COMMANDS = {
            REPLACE_TEXT, TO_LOWER, TO_UPPER, TO_TITLED, TO_CAPITALIZED, JOIN_LINES
    };

    // book operations
    String NEW_CHAPTER = "newChapter";
    String INSERT_CHAPTER = "insertChapter";
    String IMPORT_CHAPTER = "importChapter";
    String EXPORT_CHAPTER = "exportChapter";
    String RENAME_CHAPTER = "renameChapter";
    String MERGE_CHAPTER = "mergeChapter";
    String LOCK_CONTENTS = "lockContents";
    String CHAPTER_PROPERTIES = "chapterProperties";
    String BOOK_EXTENSIONS = "bookExtensions";

    String[] TREE_COMMANDS = {
            NEW_CHAPTER, INSERT_CHAPTER, IMPORT_CHAPTER, EXPORT_CHAPTER,
            RENAME_CHAPTER, MERGE_CHAPTER, LOCK_CONTENTS, CHAPTER_PROPERTIES,
            BOOK_EXTENSIONS
    };

    // tab operations
    String GOTO_NEXT_TAB = "nextTab";
    String GOTO_PREVIOUS_TAB = "previousTab";
    String CLOSE_ACTIVE_TAB = "closeActiveTab";
    String CLOSE_OTHER_TABS = "closeOtherTabs";
    String CLOSE_ALL_TABS = "closeAllTabs";
    String CLOSE_UNMODIFIED_TABS = "closeUnmodifiedTabs";

    String[] TAB_COMMANDS = {
            GOTO_NEXT_TAB, GOTO_PREVIOUS_TAB, CLOSE_ACTIVE_TAB,
            CLOSE_OTHER_TABS, CLOSE_ALL_TABS, CLOSE_UNMODIFIED_TABS
    };

    // application operations
    String ABOUT_APP = "aboutApp";
    String HELP_CONTENTS = "helpContents";
    String APP_SETTINGS = "appSettings";
    String EXIT_APP = "exitApp";
}
