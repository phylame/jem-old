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

import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.ui.com.ITextEdit;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class LongInputDialog extends JDialog {
    private JPanel  contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel  labelTip;
    private JPanel mainPane;
    private ITextEdit textEdit;

    private boolean isCancelled;

    public LongInputDialog(Frame owner, String title, String tip, String initValue) {
        super(owner, title, true);
        initComp(tip, initValue);
    }

    public LongInputDialog(Dialog owner, String title, String tip, String initValue) {
        super(owner, title, true);
        initComp(tip, initValue);
    }

    private void initComp(String tip, String initValue) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        Imabw app = Imabw.getInstance();

        labelTip.setText(tip);

        if (initValue != null) {    // prohibit undo manager works
            textEdit = new ITextEdit(initValue, null);
            buttonOK.setEnabled(true);
        } else {
            textEdit = new ITextEdit();
        }
        textEdit.getTextEditor().setLineWrap(true);
        textEdit.getTextEditor().setWrapStyleWord(true);
        textEdit.addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (textEdit.getText().length() == 0) {     // no input
                    buttonOK.setEnabled(false);
                } else {
                    buttonOK.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (textEdit.getText().length() == 0) {     // no input
                    buttonOK.setEnabled(false);
                } else {
                    buttonOK.setEnabled(true);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        mainPane.add(textEdit, BorderLayout.CENTER);

        buttonOK.setText(app.getText("Dialog.ButtonOk"));
        buttonOK.setToolTipText(app.getText("Dialog.Input.ButtonOk.Tip"));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.setText(app.getText("Dialog.ButtonCancel"));
        buttonCancel.setToolTipText(app.getText("Dialog.Input.ButtonCancel.Tip"));
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

        setSize(360, 390);
        setLocationRelativeTo(getOwner());
    }

    private void onOK() {
        isCancelled = false;
        dispose();
    }

    private void onCancel() {
        isCancelled = true;
        dispose();
    }

    public static String inputText(Frame owner, String title, String tip, String initValue) {
        LongInputDialog dialog = new LongInputDialog(owner, title, tip, initValue);
        dialog.setVisible(true);
        if (dialog.isCancelled) {
            return null;
        }
        return dialog.textEdit.getText();
    }

    public static String inputText(Dialog owner, String title, String tip, String initValue) {
        LongInputDialog dialog = new LongInputDialog(owner, title, tip, initValue);
        dialog.setVisible(true);
        if (dialog.isCancelled) {
            return null;
        }
        return dialog.textEdit.getText();
    }
}
