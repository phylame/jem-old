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

import pw.phylame.imabw.Application;

import java.util.Map;

public class SettingsDialog extends JDialog {
    private JPanel       contentPane;
    private JButton      buttonOK;
    private JButton      buttonCancel;
    private JTabbedPane  tabbedPane1;
    private JButton      buttonReset;
    private JComboBox    comboBox1;
    private JComboBox    comboBox2;
    private JCheckBox    toolbarCheckBox;
    private JCheckBox    statusBarCheckBox;
    private JCheckBox    sideBarCheckBox;
    private JRadioButton plainRadioButton;
    private JRadioButton boldRadioButton;
    private JRadioButton itallicRadioButton;
    private JSlider      slider1;
    private JComboBox    comboBox3;
    private JCheckBox    showLineNumberCheckBox;
    private JCheckBox    lockToolbarCheckBox;
    private JComboBox    comboBox4;
    private JComboBox    comboBox5;
    private JComboBox    comboBox6;
    private JComboBox    comboBox7;
    private JComboBox    comboBox8;
    private JTextField   textField1;
    private JTable       table1;

    private Application app = Application.getApplication();

    public SettingsDialog(Frame parent) {
        super(parent, true);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

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

        setTitle(app.getText("Dialog.Settings.Title"));
        setSize(700, 394);
        setLocationRelativeTo(getOwner());
    }

    private void setSettings(Map<String, Object> settings) {

    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void editSettings(Frame parent, Map<String, Object> setting) {
        SettingsDialog dialog = new SettingsDialog(parent);
        dialog.setSettings(setting);
        dialog.setVisible(true);
    }
}