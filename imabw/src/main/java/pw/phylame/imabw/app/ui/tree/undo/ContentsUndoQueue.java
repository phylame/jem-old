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

import java.util.Iterator;

import pw.phylame.jem.core.Chapter;
import pw.phylame.imabw.app.ui.tree.ContentsTree;

public class ContentsUndoQueue extends UndoQueue<ContentsUndoItem> {
    public ContentsUndoQueue(ContentsTree contentsTree, TreePath[] selections) {
        super(contentsTree, selections);
    }

    public ContentsUndoQueue(ContentsTree contentsTree, TreePath[] selections,
                             ContentsUndoItem contentsUndoItem) {
        super(contentsTree, selections, contentsUndoItem);
    }

    public void newItem(Chapter parent, Chapter[] chapters, int[] indices) {
        addItem(new ContentsUndoItem(parent, chapters, indices));
    }

    public void removeItem(ContentsUndoItem item, boolean modified) {
        contentsTree.removeChaptersFrom(
                item.chapters, item.parent, item.indices, modified);
    }

    public void doRemoving(boolean ascentOrder, boolean modified) {
        Iterator<ContentsUndoItem> iterator =
                ascentOrder ? ascendingIterator() : descendingIterator();
        while (iterator.hasNext()) {
            removeItem(iterator.next(), modified);
        }
    }

    public void insertItem(ContentsUndoItem item, boolean modified) {
        contentsTree.insertChaptersInto(
                item.chapters, item.parent, item.indices, modified);
    }

    public void doInserting(boolean ascentOrder, boolean modified) {
        Iterator<ContentsUndoItem> iterator =
                ascentOrder ? ascendingIterator() : descendingIterator();
        while (iterator.hasNext()) {
            insertItem(iterator.next(), modified);
        }
    }
}
