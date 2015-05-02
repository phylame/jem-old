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

package pw.phylame.imabw.ui.com;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pw.phylame.imabw.Application;

import pw.phylame.ixin.IToolkit;
import pw.phylame.ixin.com.IPaneRender;


public class TreeOptionsPane implements IPaneRender {
    private JPanel root;
    private JButton buttonSearch;
    private JButton buttonRefresh;
    private JToggleButton buttonLock;

    public TreeOptionsPane() {
        final Application app = Application.getApplication();

        buttonSearch.setToolTipText(app.getText("Frame.Tree.Search.Tip"));
        buttonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getManager().onTreeAction(Application.SEARCH_CHAPTER);
            }
        });

        buttonRefresh.setToolTipText(app.getText("Frame.Tree.Refresh.Tip"));
        buttonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getManager().onTreeAction(Application.REFRESH_CONTENTS);
            }
        });

        buttonLock.setToolTipText(app.getText("Frame.Tree.Lock.Tip"));
        buttonLock.setSelectedIcon(IToolkit.createImageIcon(":/res/img/tree/contents-locked.png"));
        buttonLock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                app.getManager().onTreeAction(Application.LOCK_CONTENTS);
                if (buttonLock.isSelected()) {
                    buttonLock.setToolTipText(app.getText("Frame.Tree.Unlock.Tip"));
                } else {
                    buttonLock.setToolTipText(app.getText("Frame.Tree.Lock.Tip"));
                }
            }
        });
    }

    public void setButtonLock(boolean locked) {
        buttonLock.setSelected(locked);
    }

    @Override
    public void destroy() {

    }

    @Override
    public JPanel getPane() {
        return root;
    }
}
