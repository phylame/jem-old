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

import pw.phylame.jem.core.Chapter;
import pw.phylame.gaf.undo.UndoManager;

public class TreeUndoManager extends UndoManager {
    public void chaptersRemoved(ContentsUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoRemoveChapters(undoQueue, message));
    }

    void newUndoRemoveChapters(ContentsUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoRemoveChapters(undoQueue, message), false);
    }

    public void chaptersInserted(ContentsUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoInsertChapters(undoQueue, message));
    }

    void newUndoInsertChapters(ContentsUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoInsertChapters(undoQueue, message), false);
    }

    public void attributesUpdated(AttributesUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoAttributesUpdating(undoQueue, message));
    }

    void newUndoUpdateAttributes(AttributesUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoAttributesUpdating(undoQueue, message), false);
    }

    public void chaptersJoined(Chapter chapter, int index,
                               ContentsUndoQueue undoQueue, String message) {
        insertUndoTask(new UndoJoinChapters(
                new ContentsUndoItem(chapter.getParent(), chapter, index),
                undoQueue, message));
    }

    void newUndoJoinChapters(ContentsUndoItem item, ContentsUndoQueue undoQueue,
                             String message) {
        insertUndoTask(new UndoJoinChapters(item, undoQueue, message), false);
    }
}
