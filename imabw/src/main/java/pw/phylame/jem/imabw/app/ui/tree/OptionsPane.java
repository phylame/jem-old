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

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.gaf.ixin.IToolkit;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.ui.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class OptionsPane extends JPanel {
    private static Imabw app = Imabw.getInstance();

    private NavigateTree    mTree;
    private JToolBar        mToolbar;
    private Action          mReadonlyAction;

    OptionsPane(NavigateTree tree) {
        mTree = tree;
        initComps();
    }

    private void initComps() {
        setLayout(new BorderLayout());

        JLabel label = new JLabel(app.getText("Frame.Contents.Title"),
                IResource.loadIcon(app.getText("Frame.Contents.Title.Icon")),
                JLabel.LEADING);

        mToolbar = new JToolBar();
        mToolbar.setOpaque(false);
        mToolbar.setRollover(true);
        mToolbar.setFloatable(false);
        mToolbar.setBorderPainted(false);
        addButtons();

        add(label, BorderLayout.LINE_START);
        add(mToolbar, BorderLayout.LINE_END);
    }

    private void addButtons() {
        Imabw app = Imabw.getInstance();

        mToolbar.addSeparator();

        Viewer viewer = mTree.getOwner();

        IToolkit.addButton(mToolbar, viewer.getMenuAction(Imabw.NEW_CHAPTER), viewer);
//
//        Action action = new IAction("Menu.Edit.Delete") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                mTree.delete();
//            }
//        };
//
//        // show the small icon
//        IToolkit.addButton(this, action, viewer).setIcon(
//                (Icon) action.getValue(Action.SMALL_ICON));

        Action action = viewer.getMenuAction(Imabw.CHAPTER_PROPERTIES);
        // show the small icon
        IToolkit.addButton(mToolbar, action, viewer).setIcon(
                (Icon) action.getValue(Action.SMALL_ICON));

        IToolkit.addButton(mToolbar, new IAction("Frame.Contents.Search") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // search contents
            }
        }, viewer);

//        IToolkit.addButton(this, new IAction("Frame.Contents.Refresh") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // refresh contents
//            }
//        }, viewer);

        String key = "Frame.Contents.Readonly";
        mReadonlyAction = new IAction(key) {
            {
                putValue(SELECTED_KEY, false);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                // lock contents
                mTree.updateActions();
            }
        };
        JToggleButton button = new JToggleButton(mReadonlyAction);
        button.setText("");
        button.setFocusable(false);
        button.setSelectedIcon(IResource.loadIcon(app.getText(key + ".Icon.Selected")));
        IToolkit.addStatusTipListener(button, mReadonlyAction, viewer);
        mToolbar.add(button);
    }

    boolean isReadonly() {
        return (boolean) mReadonlyAction.getValue(Action.SELECTED_KEY);
    }

    void setReadonly(boolean readonly) {
        mReadonlyAction.putValue(Action.SELECTED_KEY, readonly);
    }
}
