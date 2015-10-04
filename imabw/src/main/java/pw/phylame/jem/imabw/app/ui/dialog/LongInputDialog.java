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
import pw.phylame.gaf.ixin.IToolkit;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;

class LongInputDialog extends DialogFactory.CommonDialog {
    private JTextArea   textArea;
    private JButton     btnOk;
    private String      tipText;
    private String      initText;
    private boolean     requireChange;
    private boolean     canEmpty;
    private boolean     inputted = false;

    LongInputDialog(Frame owner, String title, String tipText, String initText,
                    boolean requireChange, boolean canEmpty) {
        super(owner, title, true);
        this.tipText = tipText;
        this.initText = initText;
        this.requireChange = requireChange;
        this.canEmpty = canEmpty;
        init();
    }

    LongInputDialog(Dialog owner, String title, String tipText, String initText,
                    boolean requireChange, boolean canEmpty) {
        super(owner, title, true);
        this.tipText = tipText;
        this.initText = initText;
        this.requireChange = requireChange;
        this.canEmpty = canEmpty;
        init();
    }

    private void createTextArea() {
        textArea = new JTextArea(initText);
        btnOk.setEnabled(initText != null && !initText.isEmpty() && !requireChange);
        final Document doc = textArea.getDocument();
        textArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (doc.getLength() == 0) {
                    btnOk.setEnabled(canEmpty);
                } else if (requireChange) {
                    if (initText != null && !initText.isEmpty()) {
                        btnOk.setEnabled(!initText.equals(textArea.getText()));
                    } else {
                        btnOk.setEnabled(true);
                    }
                } else {
                    btnOk.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void createComponents(JPanel topPane) {
        JButton btnLoad = new JButton(new IAction("Dialog.Input.ButtonLoad") {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        btnLoad.setEnabled(false);

        btnOk = new JButton(new IAction("Dialog.Input.ButtonOk") {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputted = true;
                dispose();
            }
        });

        JButton btnCancel = new JButton(new IAction("Dialog.Input.ButtonCancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        createTextArea();

        JLabel tipLabel = IToolkit.labelWithMnemonic(tipText);
        tipLabel.setLabelFor(textArea);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, MARGIN, 0));

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnLoad, btnOk, btnCancel);
        btnDefault = btnOk;

        topPane.add(tipLabel, BorderLayout.PAGE_START);
        topPane.add(new JScrollPane(textArea), BorderLayout.CENTER);

        setPreferredSize(new Dimension(265, 363));
    }

    private void loadFile() {

    }

    @Override
    protected void onCancel() {
        dispose();
    }

    String getText() {
        return inputted ? textArea.getText() : null;
    }

}
