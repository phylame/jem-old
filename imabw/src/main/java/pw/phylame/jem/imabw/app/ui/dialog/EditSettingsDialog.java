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

package pw.phylame.jem.imabw.app.ui.dialog;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.jem.imabw.app.Imabw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog for editing imabw preferences.
 */
class EditSettingsDialog extends DialogFactory.CommonDialog {
    private static Imabw app = Imabw.getInstance();

    private JPanel jpGeneral, jpGUI, jpEditor, jpJem;

    public EditSettingsDialog(Frame owner) {
        super(owner, app.getText("Dialog.Settings.Title"), true);

        init(true);
    }

    @Override
    protected void createComponents(JPanel topPane) {

        jpGeneral = new JPanel();
        jpGUI = new JPanel();
        jpEditor = new JPanel();
        jpJem = new JPanel();

        JTabbedPane tabbedPane = new JTabbedPane();

        topPane.add(tabbedPane, BorderLayout.CENTER);

        createButtons();
    }

    private void createButtons() {
        JButton btnReset = new JButton(new IAction("Dialog.Settings.ButtonReset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onReset();
            }
        });

        JButton btnSave = new JButton(new IAction("Dialog.Settings.ButtonSave") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });

        JButton btnCancel = new JButton(new IAction("Dialog.Settings.ButtonCancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnReset, btnSave, btnCancel);
        btnDefault = btnSave;
    }

    private void onReset() {

    }

    @Override
    protected void onCancel() {
        dispose();
    }

    private void sync() {

    }

    private void onOk() {
        dispose();
        sync();
    }
}
