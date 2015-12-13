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

public class ContentsUndoItem {
    public Chapter parent;
    public Chapter[] chapters;
    public int[] indices;

    public ContentsUndoItem(Chapter parent, Chapter[] chapters, int[] indices) {
        this.parent = parent;
        this.chapters = chapters;
        this.indices = indices;
    }

    public ContentsUndoItem(Chapter parent, Chapter chapter, int index) {
        this(parent, new Chapter[]{chapter}, new int[]{index});
    }
}
