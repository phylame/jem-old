/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app.ui.tree;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.jem.core.Chapter;
import pw.phylame.jem.imabw.app.Imabw;

/**
 * Cell render for book tree.
 */
public class BookCellRender extends DefaultTreeCellRenderer {
    private static Imabw app = Imabw.getInstance();

    private static final Color HIGHLIGHT_COLOR = Color.BLUE;
    private Color defaultColor = getForeground();

    private Icon bookIcon    = IResource.loadIcon(app.getText("Frame.Contents.BookIcon"));
    private Icon sectionIcon = IResource.loadIcon(app.getText("Frame.Contents.SectionIcon"));
    private Icon chapterIcon = IResource.loadIcon(app.getText("Frame.Contents.ChapterIcon"));

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
        if (leaf) {
            setIcon(chapterIcon);
        } else if (BookTreeModel.isRoot(chapter)) {   // book
            setIcon(bookIcon);
        } else {
            setIcon(sectionIcon);
        }
        if (app.getActiveTask().isChapterModified(chapter)) {
            setForeground(HIGHLIGHT_COLOR);
        } else {
            setForeground(defaultColor);
        }
        return this;
    }
}
