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

import javax.swing.*;
import java.awt.*;

import pw.phylame.gaf.ixin.IButtonType;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.Viewer;

/**
 * Provides control options for <tt>ContentsTree</tt>
 */
public class ControlsPane extends JPanel {
    private static final Imabw app = Imabw.sharedInstance();

    private ContentsTree contentsTree;
    private JToolBar toolBar;

    ControlsPane(ContentsTree contentsTree) {
        super(new BorderLayout());
        this.contentsTree = contentsTree;
        createComponents();
    }

    private void createComponents() {
        JLabel titleLabel = new JLabel(app.getText("contents.title"),
                app.loadIcon("tree/contents.png"), JLabel.LEADING);

        toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        addToolItems();

        add(titleLabel, BorderLayout.LINE_START);
        add(toolBar, BorderLayout.LINE_END);
    }

    private void addToolItems() {
        toolBar.addSeparator();

        addControlButton(Imabw.NEW_CHAPTER, null);
        addControlButton(Imabw.CHAPTER_PROPERTIES, null);
        addControlButton(Imabw.LOCK_CONTENTS, IButtonType.Toggle);
    }

    private void addControlButton(String actionKey, IButtonType type) {
        Viewer viewer = contentsTree.getViewer();
        Action action = viewer.getMenuAction(actionKey);
        AbstractButton button = IxinUtilities.addToolButton(toolBar, action,
                type, viewer);
        button.setIcon((Icon) action.getValue(Action.SMALL_ICON));
    }

    public JToolBar getToolBar() {
        return toolBar;
    }
}
