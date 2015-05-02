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

package pw.phylame.imabw.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import pw.phylame.jem.core.Part;
import pw.phylame.imabw.Application;

public class PartPropertiesDialog extends JDialog {
    private static Application app = Application.getApplication();

    private JPanel    contentPane;
    private JButton   buttonOK;
    private JButton   buttonCancel;
    private JButton   buttonOpen;
    private JButton   buttonRemove;
    private JButton   buttonSave;
    private JButton   button1;
    private JButton   button2;
    private JButton   button3;
    private JTextArea textArea1;
    private JTable    table1;

    private Part part;

    public PartPropertiesDialog(Frame owner, String title, Part part) {
        super(owner, title, true);
        this.part = part;
        init();
    }

    public PartPropertiesDialog(Dialog owner, String title, Part part) {
        super(owner, title, true);
        this.part = part;
        init();
    }

    private void init() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSize(900, 547);
        setLocationRelativeTo(getOwner());
    }

    private void onCancel() {
        dispose();
    }

    public static void viewProperties(Frame owner, Part part) {
        PartPropertiesDialog dialog = new PartPropertiesDialog(owner,
                app.getText("Dialog.PartProperties.Title", part.getTitle()), part);
        dialog.setVisible(true);
    }

    public static void viewProperties(Dialog owner, Part part) {
        PartPropertiesDialog dialog = new PartPropertiesDialog(owner,
                app.getText("Dialog.PartProperties.Title", part.getTitle()), part);
        dialog.setVisible(true);
    }
}
