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
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;

class SimpleInputDialog extends CommonDialog<Object> {
    private JTextField textField;
    private JButton buttonOk;
    private Object initValue;
    private boolean requireChange;
    private boolean canEmpty;
    private JFormattedTextField.AbstractFormatter formatter;
    private boolean inputted = false;

    private String tipText;

    public SimpleInputDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.QUESTION_DIALOG);
    }

    public SimpleInputDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.QUESTION_DIALOG);
    }

    void setInitValue(Object initValue) {
        this.initValue = initValue;
    }

    void setRequireChange(boolean requireChange) {
        this.requireChange = requireChange;
    }

    void setCanEmpty(boolean canEmpty) {
        this.canEmpty = canEmpty;
    }

    void setFormatter(JFormattedTextField.AbstractFormatter formatter) {
        this.formatter = formatter;
    }

    void setTipText(String tipText) {
        this.tipText = tipText;
    }

    @Override
    public Object getResult() {
        if (!inputted) {
            return null;
        } else if (formatter != null) {
            return ((JFormattedTextField) textField).getValue();
        } else {
            return textField.getText();
        }
    }

    @Override
    protected void createComponents(JPanel userPane) {
        Action okAction = new IAction(BUTTON_OK) {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputted = true;
                dispose();
            }
        };
        defaultButton = buttonOk = new JButton(okAction);

        createTextField(formatter);

        JLabel tipLabel = IxinUtilities.mnemonicLabel(tipText);
        tipLabel.setLabelFor(textField);

        userPane.add(tipLabel, BorderLayout.CENTER);
        userPane.add(textField, BorderLayout.PAGE_END);

        controlsPane = createControlsPane(SwingConstants.RIGHT, buttonOk,
                createCloseButton(BUTTON_CANCEL));
    }

    private void createTextField(JFormattedTextField.AbstractFormatter formatter) {
        if (formatter == null) {
            textField = new JTextField();
            if (initValue != null) {
                textField.setText(initValue.toString());
            }
            buttonOk.setEnabled(initValue != null && !initValue.equals("")
                    && !requireChange);
        } else {
            JFormattedTextField formattedTextField = new JFormattedTextField(initValue);
            formattedTextField.setFormatterFactory(new DefaultFormatterFactory(formatter));
            textField = formattedTextField;
            this.formatter = formatter;
        }
        textField.setColumns(32);
        textField.selectAll();
        buttonOk.setEnabled(!requireChange && (canEmpty || !"".equals(initValue)));

        final Document doc = textField.getDocument();
        textField.addCaretListener(e -> {
            if (doc.getLength() == 0) { // empty
                buttonOk.setEnabled(canEmpty && (!requireChange || !"".equals(initValue)));
            } else if (requireChange) { // not empty
                buttonOk.setEnabled(initValue == null || isChanged());
            } else {
                buttonOk.setEnabled(true);
            }
        });
    }

    private boolean isChanged() {
        if (formatter == null) {
            return !initValue.equals(textField.getText());
        } else {
            try {
                return !initValue.equals(formatter.stringToValue(textField.getText()));
            } catch (ParseException e) {
                // invalid input
                return false;
            }
        }
    }
}
