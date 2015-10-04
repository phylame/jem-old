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
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.ParseException;

class SimpleInputDialog extends DialogFactory.CommonDialog {
    private JTextField                              textField;
    private JButton                                 btnOk;
    private Object                                  initValue;
    private boolean                                 requireChange;
    private boolean                                 canEmpty;
    private JFormattedTextField.AbstractFormatter   formatter;
    private boolean                                 inputted = false;

    private String tipText;

    SimpleInputDialog(Frame owner, String title, String tipText,
                      Object initValue, boolean requireChange, boolean canEmpty,
                      JFormattedTextField.AbstractFormatter formatter) {
        super(owner, title, true);
        this.tipText = tipText;
        this.initValue = initValue;
        this.requireChange = requireChange;
        this.canEmpty = canEmpty;
        this.formatter = formatter;
        init();
    }

    SimpleInputDialog(Dialog owner, String title, String tipText,
                      Object initValue, boolean requireChange, boolean canEmpty,
                      JFormattedTextField.AbstractFormatter formatter) {
        super(owner, title, true);
        this.tipText = tipText;
        this.initValue = initValue;
        this.requireChange = requireChange;
        this.canEmpty = canEmpty;
        this.formatter = formatter;
        init();
    }

    @Override
    protected void createComponents(JPanel topPane) {
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

        createTextField(formatter);

        JLabel tipLabel = IToolkit.labelWithMnemonic(tipText);
        tipLabel.setLabelFor(textField);

        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.LINE_AXIS));
        inputPane.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        inputPane.add(tipLabel);
        inputPane.add(Box.createRigidArea(new Dimension(5, 0)));
        inputPane.add(textField);
        inputPane.add(Box.createHorizontalGlue());

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnOk, btnCancel);
        btnDefault = btnOk;

        topPane.add(inputPane, BorderLayout.PAGE_START);

        setPreferredSize(new Dimension(360, 100));
    }

    private void createTextField(JFormattedTextField.AbstractFormatter formatter) {
        if (formatter == null) {
            textField = new JTextField();
            if (initValue != null) {
                textField.setText(initValue.toString());
            }
            textField.selectAll();
            btnOk.setEnabled(initValue != null && !initValue.equals("")
                    && !requireChange);
        } else {
            JFormattedTextField formattedTextField = new JFormattedTextField(initValue);
            formattedTextField.setFormatterFactory(new DefaultFormatterFactory(formatter));
            textField = formattedTextField;
            this.formatter = formatter;
        }

        final Document doc = textField.getDocument();
        textField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (doc.getLength() == 0) {
                    btnOk.setEnabled(canEmpty);
                } else if (requireChange) {
                    if (initValue != null) {
                        btnOk.setEnabled(isChanged());
                    } else {
                        btnOk.setEnabled(true);
                    }
                } else {
                    btnOk.setEnabled(true);
                }
            }
        });
    }

    private boolean isChanged() {
        if (formatter == null) {
            return ! initValue.equals(textField.getText());
        } else {
            try {
                return ! initValue.equals(formatter.stringToValue(textField.getText()));
            } catch (ParseException e) {
                // invalid input
                return false;
            }
        }
    }

    Object getValue() {
        if (! inputted) {
            return null;
        } else if (formatter != null) {
            return ((JFormattedTextField) textField).getValue();
        } else {
            return textField.getText();
        }
    }

    @Override
    protected void onCancel() {
        dispose();
    }
}
