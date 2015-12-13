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

package pw.phylame.imabw.app.ui.tree;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import pw.phylame.jem.core.Chapter;
import pw.phylame.imabw.app.Imabw;

/**
 * Cell render for book tree.
 */
public class BookCellRender extends DefaultTreeCellRenderer {
    private static final Imabw app = Imabw.sharedInstance();

    private static final Color highlightColor = Color.BLUE;
    private static final Color copyColor = Color.LIGHT_GRAY;
    private static final Color cutColor = Color.DARK_GRAY;
    private final Color defaultColor = getForeground();

    private Icon bookIcon = app.loadIcon("tree/book.png");
    private Icon sectionIcon = app.loadIcon("tree/section.png");
    private Icon chapterIcon = app.loadIcon("tree/chapter.png");

    TreeClipboard clipboard;

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean selected,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        Chapter chapter = (Chapter) value;
        if (BookTreeModel.isRoot(chapter)) {   // book
            setIcon(bookIcon);
        } else if (leaf) {
            setIcon(chapterIcon);
        } else {
            setIcon(sectionIcon);
        }
        Color color;
        if (app.getManager().getActiveTask().isChapterModified(chapter)) {
            color = highlightColor;
        } else if (clipboard.contains(chapter)) {
            color = clipboard.isCopying() ? copyColor : cutColor;
        } else {
            color = defaultColor;
        }
        setForeground(color);
        return this;
    }

    public Icon getBookIcon() {
        return bookIcon;
    }

    public Icon getSectionIcon() {
        return sectionIcon;
    }

    public Icon getChapterIcon() {
        return chapterIcon;
    }
}
