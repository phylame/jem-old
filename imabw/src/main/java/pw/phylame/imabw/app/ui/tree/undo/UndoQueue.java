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

import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.tree.TreePath;

import pw.phylame.imabw.app.ui.tree.ContentsTree;

/**
 * List of undo item.
 */
public class UndoQueue<ITEM> {
    private LinkedList<ITEM> items = new LinkedList<>();
    public ContentsTree contentsTree;
    public TreePath[] selections;

    public UndoQueue(ContentsTree contentsTree, TreePath[] selections) {
        this.contentsTree = contentsTree;
        this.selections = selections;
    }

    public UndoQueue(ContentsTree contentsTree, TreePath[] selections, ITEM item) {
        this.contentsTree = contentsTree;
        this.selections = selections;
        addItem(item);
    }

    public void addItem(ITEM item) {
        items.addLast(item);
    }

    public Iterator<ITEM> ascendingIterator() {
        return items.iterator();
    }

    public Iterator<ITEM> descendingIterator() {
        return items.descendingIterator();
    }

    public TreeUndoManager getUndoManager() {
        return contentsTree.getUndoManager();
    }

    public TreePath[] backupCurrentSelections() {
        return contentsTree.getTree().getSelectionPaths();
    }

    public void resetPreviousSelections() {
        contentsTree.getTree().setSelectionPaths(selections);
    }
}
