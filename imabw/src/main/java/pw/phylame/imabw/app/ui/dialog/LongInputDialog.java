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

package pw.phylame.imabw.app.ui.dialog;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IxinUtilities;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;

class LongInputDialog extends CommonDialog<String> {
    private JTextArea textArea;
    private JButton buttonOk;
    private String tipText;
    private String initText;
    private boolean requireChange;
    private boolean canEmpty;
    private boolean inputted = false;

    public LongInputDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.QUESTION_DIALOG);
    }

    public LongInputDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.QUESTION_DIALOG);
    }

    void setTipText(String tipText) {
        this.tipText = tipText;
    }

    void setInitText(String initText) {
        this.initText = initText;
    }

    void setRequireChange(boolean requireChange) {
        this.requireChange = requireChange;
    }

    void setCanEmpty(boolean canEmpty) {
        this.canEmpty = canEmpty;
    }

    @Override
    public String getResult() {
        return inputted ? textArea.getText() : null;
    }

    private void createTextArea() {
        textArea = new JTextArea(null, initText, 15, 26);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        buttonOk.setEnabled(initText != null && !initText.isEmpty() && !requireChange);
        final Document doc = textArea.getDocument();
        textArea.addCaretListener(e -> {
            if (doc.getLength() == 0) {
                buttonOk.setEnabled(canEmpty);
            } else if (requireChange) {
                if (initText != null && !initText.isEmpty()) {
                    buttonOk.setEnabled(!initText.equals(textArea.getText()));
                } else {
                    buttonOk.setEnabled(true);
                }
            } else {
                buttonOk.setEnabled(true);
            }
        });
    }

    @Override
    protected void createComponents(JPanel userPane) {
        JButton buttonLoad = new JButton(new IAction("dialog.input.buttonLoad") {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        buttonLoad.setEnabled(false);

        defaultButton = buttonOk = new JButton(new IAction(BUTTON_OK) {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputted = true;
                dispose();
            }
        });

        createTextArea();

        JLabel tipLabel = IxinUtilities.mnemonicLabel(tipText);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, margin, 0));
        tipLabel.setLabelFor(textArea);

        userPane.add(tipLabel, BorderLayout.PAGE_START);
        userPane.add(new JScrollPane(textArea), BorderLayout.CENTER);

        controlsPane = createControlsPane(-1, buttonLoad, Box.createHorizontalGlue(),
                buttonOk, createCloseButton(BUTTON_CANCEL));
    }

    private void loadFile() {

    }
}
