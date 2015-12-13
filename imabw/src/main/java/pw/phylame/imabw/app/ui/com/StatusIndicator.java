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

package pw.phylame.imabw.app.ui.com;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.Viewer;
import pw.phylame.imabw.app.ui.dialog.DialogFactory;
import pw.phylame.imabw.app.ui.editor.EditorTab;
import pw.phylame.imabw.app.ui.editor.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The indicator for main form.
 */
public class StatusIndicator extends JPanel {
    private static final Imabw app = Imabw.sharedInstance();

    private Viewer viewer;

    private JLabel rulerLabel, wordsLabel;
    private JLabel readonlyLabel;

    public StatusIndicator(Viewer viewer) {
        this.viewer = viewer;
        createComponents();
        setEditorStatus(false);
    }

    private void createComponents() {
        rulerLabel = new JLabel();
        rulerLabel.setToolTipText(app.getText("status.ruler.tip"));
        rulerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!rulerLabel.isEnabled()) {
                    return;
                }
                if (!e.isMetaDown()) {
                    EditorTab tab = viewer.getTabbedEditor().getActiveTab();
                    if (tab != null) {
                        tab.getEditor().gotoPosition();
                    }
                }
            }
        });

        wordsLabel = new JLabel();
        wordsLabel.setToolTipText(app.getText("status.words.tip"));

        readonlyLabel = new JLabel(app.loadIcon("status/readwrite.png"));
        readonlyLabel.setToolTipText(app.getText("status.readonly.tip"));
        readonlyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!readonlyLabel.isEnabled()) {
                    return;
                }
                if (e.isMetaDown()) {
                    return;
                }

                EditorTab tab = viewer.getTabbedEditor().getActiveTab();
                if (tab != null) {
                    TextEditor editor = tab.getEditor();
                    editor.setReadonly(!editor.isReadonly());
                    setReadonly(editor.isReadonly());
                }
            }
        });
        JLabel messageLabel = new JLabel(app.loadIcon("status/message.png"));
        messageLabel.setToolTipText(app.getText("status.message.tip"));
        messageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DialogFactory.featureDeveloping(viewer);
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        addComponents(new JSeparator(JSeparator.VERTICAL), rulerLabel,
                new JSeparator(JSeparator.VERTICAL), wordsLabel,
                new JSeparator(JSeparator.VERTICAL), readonlyLabel,
                new JSeparator(JSeparator.VERTICAL), messageLabel);
    }

    private void addComponents(Component... components) {
        for (Component c : components) {
            add(c);
            add(Box.createRigidArea(new Dimension(5, 0)));
        }
    }

    public void setRuler(int row, int column, int selected) {
        StringBuilder sb = new StringBuilder();
        if (row < 0) {      // invalid
            sb.append("n/a");
        } else {
            sb.append(row).append(":").append(column);
            if (selected > 0) {
                sb.append("/").append(selected);
            }
        }
        rulerLabel.setText(sb.toString());
    }

    public void setWords(int n) {
        if (n < 0) {        // invalid
            wordsLabel.setText("n/a");
        } else {
            wordsLabel.setText(String.valueOf(n));
        }
    }

    public void setReadonly(boolean readonly) {
        if (readonly) {
            readonlyLabel.setIcon(app.loadIcon("status/readonly.png"));
        } else {
            readonlyLabel.setIcon(app.loadIcon("status/readwrite.png"));
        }
    }

    public void setEditorStatus(boolean enable) {
        if (!enable) {
            setRuler(-1, -1, -1);
            setWords(-1);
        }
        rulerLabel.setEnabled(enable);
        wordsLabel.setEnabled(enable);
        readonlyLabel.setEnabled(enable);
    }

}
