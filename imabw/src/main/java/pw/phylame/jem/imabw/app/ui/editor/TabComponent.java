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

package pw.phylame.jem.imabw.app.ui.editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.jem.imabw.app.Imabw;

class TabComponent extends JPanel {
    private JLabel lbTitle, lbClose;

    TabComponent(Icon icon, String title, final EditorTab tab, final TabbedEditor tabbedEditor) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setOpaque(false);

        lbTitle = new JLabel(title, icon, JLabel.LEADING);
        add(lbTitle);

        add(Box.createRigidArea(new Dimension(4, 0)));

        lbClose = new JLabel(IResource.loadIcon("tab/close.png"));
        lbClose.setToolTipText(Imabw.getInstance().getText("Frame.Editors.Tab.CloseTip"));
        lbClose.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!e.isMetaDown()) {
                    tabbedEditor.closeTab(tab);
                }
            }
        });
        add(lbClose);
    }

    void setTitle(String title) {
        lbTitle.setText(title);
    }

    void setIcon(Icon icon) {
        lbClose.setIcon(icon);
    }
}
