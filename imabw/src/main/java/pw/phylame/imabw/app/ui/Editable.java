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

import pw.phylame.imabw.app.Constants;

public interface Editable {
    Viewer getViewer();

    void undo();

    void redo();

    void cut();

    void copy();

    void paste();

    void delete();

    void selectAll();

    void find();

    void findNext();

    void findPrevious();

    void gotoPosition();

    default void updateEditActions(boolean enable) {
        Viewer viewer = getViewer();
        for (String command : Constants.EDIT_COMMANDS) {
            viewer.setActionEnable(command, enable);
        }
    }

    default void updateFindActions(boolean enable) {
        Viewer viewer = getViewer();
        for (String command : Constants.FIND_COMMANDS) {
            viewer.setActionEnable(command, enable);
        }
    }
}
