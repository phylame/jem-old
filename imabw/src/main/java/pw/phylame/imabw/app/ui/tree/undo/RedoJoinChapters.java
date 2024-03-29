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

package pw.phylame.imabw.app.ui.tree.undo;

import javax.swing.tree.TreePath;

public class RedoJoinChapters extends RedoContentsRemoving {
    private ContentsUndoItem insertedItem;

    public RedoJoinChapters(ContentsUndoItem insertedItem,
                            ContentsUndoQueue undoQueue, String message) {
        super(undoQueue, message);
        this.insertedItem = insertedItem;
    }

    @Override
    protected void redo() {
        TreePath[] currentSelections = undoQueue.backupCurrentSelections();
        undoQueue.insertItem(insertedItem, true);
        doRemoving();
        undoQueue.resetPreviousSelections();
        undoQueue.selections = currentSelections;
        undoQueue.getUndoManager().newUndoJoinChapters(insertedItem, undoQueue, getMessage());
    }
}
