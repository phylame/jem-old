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

package pw.phylame.imabw.ui.com.impl;

import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.ui.Utils;
import pw.phylame.imabw.ui.com.MacProvider;
import pw.phylame.jem.formats.txt.TxtMaker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class TxtMacProvider extends JDialog implements MacProvider {
    private JPanel            contentPane;
    private JButton           buttonOK;
    private JButton           buttonCancel;
    private JComboBox<String> cbLS;
    private JTextField        tfPP;
    private JLabel            lbPP;
    private JLabel            lbLS;
    private JComboBox<String> cbEncodings;
    private JLabel lbEncoding;

    private boolean cancelled = false;

    private static Point     location = null;
    private static Dimension size     = null;

    public TxtMacProvider(Frame owner, String title) {
        super(owner, title, true);
        init();
    }

    public TxtMacProvider(Dialog owner, String title) {
        super(owner, title, true);
        init();
    }

    private void init() {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);

        Imabw app = Imabw.getInstance();

        lbEncoding.setText(app.getText("Dialog.TxtMac.LabelEncoding"));
        cbEncodings.setToolTipText(app.getText("Dialog.TxtMac.Encoding.Tip"));
        Utils.initEncodings(cbEncodings, app.getConfig().getTxtMaEncoding());

        lbLS.setText(app.getText("Dialog.TxtMac.LabelLS"));
        cbLS.setToolTipText(app.getText("Dialog.TxtMac.LineSeparator.Tip"));
        Utils.initLineSeparators(cbLS, app.getConfig().getTxtMaLineSeparator());

        lbPP.setText(app.getText("Dialog.TxtMac.LabelPP"));
        tfPP.setToolTipText(app.getText("Dialog.TxtMac.PP.Tip"));
        tfPP.setText(app.getConfig().getTxtMaParagraphPrefix());

        buttonOK.setText(app.getText("Dialog.ButtonOk"));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.setText(app.getText("Dialog.ButtonCancel"));
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

        if (location != null) {
            setLocation(location);
            setSize(size);
        } else {
            pack();
            setSize((int)(getWidth()*1.3), getHeight());
            setLocationRelativeTo(getOwner());
        }
    }

    private void onOK() {
        location = getLocation();
        size = getSize();
        dispose();
    }

    private void onCancel() {
        location = getLocation();
        size = getSize();
        cancelled = true;
        dispose();
    }

    @Override
    public Map<String, Object> getArguments() {
        setVisible(true);
        if (cancelled) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put(TxtMaker.KEY_TEXT_ENCODING, cbEncodings.getSelectedItem());
        map.put(TxtMaker.KEY_LINE_FEED, Utils.getLineSeparator(cbLS.getSelectedIndex()));
        map.put(TxtMaker.KEY_PARAGRAPH_PREFIX, tfPP.getText());
        return map;
    }
}
