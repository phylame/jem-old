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

package pw.phylame.jem.imabw.app.ui.com;

import pw.phylame.gaf.ixin.IResource;
import pw.phylame.jem.imabw.app.Imabw;
import pw.phylame.jem.imabw.app.ui.editor.EditorTab;
import pw.phylame.jem.imabw.app.ui.Viewer;
import pw.phylame.jem.imabw.app.ui.editor.TextEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

public class StatusIndicator extends JPanel {
    private static Imabw app = Imabw.getInstance();

    private Viewer mViewer;

    private JLabel mlbRuler, mlbWords;
    private JLabel mlbReadonly;

    public StatusIndicator(Viewer viewer) {
        mViewer = viewer;

        initComps();

        setRuler(-1, -1, -1);
        setWords(-1);

        setEditorStatus(false);
    }

    private void initComps() {
        mlbRuler = new JLabel();
        mlbRuler.setToolTipText(app.getText("Frame.Status.Ruler.Tip"));
        mlbRuler.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!mlbRuler.isEnabled()) {
                    return;
                }
                if (!e.isMetaDown()) {
                    EditorTab tab = mViewer.getTabbedEditor().getActiveTab();
                    if (tab != null) {
                        tab.getEditor().gotoPosition();
                    }
                }
            }
        });

        mlbWords = new JLabel();
        mlbWords.setToolTipText(app.getText("Frame.Status.Words.Tip"));

        mlbReadonly = new JLabel(IResource.loadIcon("status/readwrite.png"));
        mlbReadonly.setToolTipText(app.getText("Frame.Status.Readonly.Tip"));
        mlbReadonly.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!mlbReadonly.isEnabled()) {
                    return;
                }
                if (e.isMetaDown()) {
                    return;
                }

                EditorTab tab = mViewer.getTabbedEditor().getActiveTab();
                if (tab != null) {
                    TextEditor editor = tab.getEditor();
                    editor.setReadonly(!editor.isReadonly());
                    setReadonly(editor.isReadonly());
                }
            }
        });

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        addComponents(new JSeparator(JSeparator.VERTICAL), mlbRuler,
                new JSeparator(JSeparator.VERTICAL), mlbWords,
                new JSeparator(JSeparator.VERTICAL), mlbReadonly);
    }

    private void addComponents(Component ...components) {
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
        mlbRuler.setText(sb.toString());
    }

    public void setWords(int n) {
        if (n < 0) {        // invalid
            mlbWords.setText("n/a");
        } else {
            mlbWords.setText(String.valueOf(n));
        }
    }

    public void setReadonly(boolean readonly) {
        if (readonly) {
            mlbReadonly.setIcon(IResource.loadIcon("status/readonly.png"));
        } else {
            mlbReadonly.setIcon(IResource.loadIcon("status/readwrite.png"));
        }
    }

    public void setEditorStatus(boolean enable) {
        if (! enable) {
            setRuler(-1, -1, -1);
            setWords(-1);
        }
        mlbRuler.setEnabled(enable);
        mlbWords.setEnabled(enable);
        mlbReadonly.setEnabled(enable);
    }
}
