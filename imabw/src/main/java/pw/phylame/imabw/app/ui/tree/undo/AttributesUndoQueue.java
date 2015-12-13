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
import java.util.Map;
import javax.swing.tree.TreePath;

import pw.phylame.imabw.app.ui.tree.ContentsTree;
import pw.phylame.jem.core.Chapter;

public class AttributesUndoQueue extends UndoQueue<AttributesUndoItem> {
    public AttributesUndoQueue(ContentsTree contentsTree, TreePath[] selections) {
        super(contentsTree, selections);
    }

    public AttributesUndoQueue(ContentsTree contentsTree, TreePath[] selections,
                               AttributesUndoItem attributesUndoItem) {
        super(contentsTree, selections, attributesUndoItem);
    }

    public AttributesUndoItem newItem(Chapter chapter, Map<String, Object> oldAttributes,
                                      boolean removePresent) {
        AttributesUndoItem item = new AttributesUndoItem(chapter, oldAttributes, removePresent);
        addItem(item);
        return item;
    }

    public void doUpdating(boolean ascentOrder, boolean modified) {
        Iterator<AttributesUndoItem> iterator =
                ascentOrder ? ascendingIterator() : descendingIterator();
        while (iterator.hasNext()) {
            AttributesUndoItem item = iterator.next();
            item.attributes = contentsTree.updateChapterAttributes(
                    item.chapter, item.attributes, item.removePresent, modified);
        }
    }
}
