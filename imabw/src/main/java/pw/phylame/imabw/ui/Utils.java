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

package pw.phylame.imabw.ui;

import pw.phylame.imabw.Config;
import pw.phylame.imabw.Imabw;
import pw.phylame.imabw.ui.com.ListChooserPane;
import pw.phylame.jem.core.Part;

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;

import java.util.List;

public class Utils {
    private static Imabw app = Imabw.getInstance();

    public static void initEncodings(JComboBox<String> comboBox, String initEncoding) {
        int ix = -1, i = 0;
        for (String encoding: app.getWorker().getEncodings()) {
            comboBox.addItem(encoding);
            if (encoding.equals(initEncoding)) {
                ix = i;
            }
            ++i;
        }
        if (ix == -1) {
            comboBox.addItem(initEncoding);
            ix = comboBox.getItemCount()-1;
        }
        comboBox.setSelectedIndex(ix);
    }

    public static String getLineSeparator(int index) {
        switch (index) {
            case 0:
                return "\r\n";
            case 1:
                return "\n";
            case 2:
                return "\r";
            default:
                return "\r\n";
        }
    }

    public static void initLineSeparators(JComboBox<String> comboBox, String initLS) {
        String[] items = {"CRLF - (Windows \r\n)", "LF - (UNIX & OS X \n)",
                "CR - (Classic Mac \r)"};
        comboBox.setModel(new DefaultComboBoxModel<>(items));
        if (Config.isEmpty(initLS)) {
            comboBox.setSelectedIndex(0);
            return;
        }
        switch (initLS) {
            case "\r\n":
                comboBox.setSelectedIndex(0);
                break;
            case "\n":
                comboBox.setSelectedIndex(1);
                break;
            case "\r":
                comboBox.setSelectedIndex(2);
                break;
            default:
                comboBox.setSelectedIndex(0);
                break;
        }
    }

    public static Part choosePart(Component parent, String title, List<Part> parts, String tip) {
        ListChooserPane pane = new ListChooserPane(parts, false, tip);
        int opt = JOptionPane.showOptionDialog(parent, pane.getPane(), title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (opt != JOptionPane.OK_OPTION) {
            return null;
        }
        return parts.get(pane.getSelectedIndex());
    }
}
