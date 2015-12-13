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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import pw.phylame.imabw.app.Imabw;

class TabComponent extends JPanel {
    private JLabel titleLabel, closeLabel;

    TabComponent(Icon icon, String title, final EditorTab tab, final TabbedEditor tabbedEditor) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);

        add(titleLabel = new JLabel(title, icon, JLabel.LEADING));

        add(Box.createRigidArea(new Dimension(4, 0)));

        closeLabel = new JLabel(Imabw.sharedInstance().loadIcon("tab/close.png"));
        closeLabel.setToolTipText(Imabw.sharedInstance().getText("editors.tab.closeTip"));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isMetaDown()) {
                    tabbedEditor.closeTab(tab);
                }
            }
        });
        add(closeLabel);
    }

    void setTitle(String title) {
        titleLabel.setText(title);
    }

    void setIcon(Icon icon) {
        closeLabel.setIcon(icon);
    }
}
