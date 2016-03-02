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

package pw.phylame.imabw.app.ui.editor;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.Viewer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Searcher bar for text editor.
 */
class SearchBar extends JSplitPane {
    private static Imabw app = Imabw.sharedInstance();
    private Viewer viewer;

    JTextField tfSource, tfTarget = null;
    private Component spacer = null;

    Action nextAction, prevAction;
    JButton btnReplace, btnReplaceAll;

    private JPanel jpReplace;

    private JPanel jpLeft, jpRight;

    private TextEditor editor;

    SearchBar(TextEditor editor) {
        this.editor = editor;
        viewer = app.getForm();
        createComponents();
    }

    private void createComponents() {
        tfSource = new JTextField();

        tfSource.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editor.findNext();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editor.findNext();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        nextAction = viewer.getMenuAction(Imabw.FIND_NEXT);
        prevAction = viewer.getMenuAction(Imabw.FIND_PREVIOUS);

        Action closeAction = new AbstractAction() {
            {
                putValue(SMALL_ICON, app.loadIcon("tab/close.png"));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.hideSearcher();
            }
        };

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
//        IToolkit.addButton(toolBar, prevAction, viewer).setIcon((Icon) prevAction.getValue(Action.SMALL_ICON));
//        IToolkit.addButton(toolBar, nextAction, viewer).setIcon((Icon) nextAction.getValue(Action.SMALL_ICON));
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(closeAction);

        jpLeft = new JPanel();
        jpLeft.setLayout(new BoxLayout(jpLeft, BoxLayout.PAGE_AXIS));
        jpLeft.add(tfSource);

        jpRight = new JPanel();
        jpRight.setLayout(new BoxLayout(jpRight, BoxLayout.PAGE_AXIS));
        jpRight.add(toolBar);

        setLeftComponent(jpLeft);
        setDividerLocation(200);
        setDividerSize(3);
        setRightComponent(jpRight);
    }

    void showReplacePane() {
        if (tfTarget != null) {
            return;
        }
        jpLeft.add((spacer = Box.createRigidArea(new Dimension(0, 3))));
        jpLeft.add((tfTarget = new JTextField()));

        btnReplace = new JButton(new IAction("editors.searcher.buttonReplace") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.replaceNext();
            }
        });
        btnReplace.setEnabled(false);
        btnReplaceAll = new JButton(new IAction("editors.searcher.buttonReplaceAll") {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.replaceAll();
            }
        });
        btnReplaceAll.setEnabled(false);
        jpReplace = new JPanel();
        jpReplace.setLayout(new BoxLayout(jpReplace, BoxLayout.LINE_AXIS));
        jpReplace.add(btnReplace);
        jpReplace.add(Box.createRigidArea(new Dimension(3, 0)));
        jpReplace.add(btnReplaceAll);
        jpReplace.add(Box.createHorizontalGlue());

        jpRight.add(jpReplace);
    }

    void hideReplacePane() {
        if (tfTarget == null) {
            return;
        }
        jpLeft.remove(spacer);
        jpLeft.remove(tfTarget);
        spacer = tfTarget = null;

        jpRight.remove(jpReplace);
        jpReplace = null;
        btnReplace = btnReplaceAll = null;
    }

    void setText(String text) {
        tfSource.setText(text);
        tfSource.selectAll();
        tfSource.requestFocus();
    }
}
