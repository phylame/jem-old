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

package pw.phylame.imabw.ui.com;

import pw.phylame.imabw.Imabw;
import pw.phylame.ixin.com.IPaneRender;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

public class NewAttributePane implements IPaneRender {
    private JPanel            root;
    private JComboBox<String> cbName;
    private JTextField        tfName;
    private JComboBox<String> cbType;
    private JLabel            labelCName;
    private JLabel            labelType;
    private JLabel            labelName;
    private JPanel cusPane;

    private boolean customized = false;

    public NewAttributePane(ArrayList<String> names, ArrayList<String> types) {
        Imabw app = Imabw.getApplication();

        labelName.setText(app.getText("Dialog.Properties.Attributes.Add.LabelName"));
        for (String name : names) {
            cbName.addItem(name);
        }
        if (cbName.getItemCount() == 1) {   // customer
            tfName.setEditable(true);
            cbType.setEnabled(true);
            customized = true;
        }
        cbName.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (cbName.getSelectedIndex() == cbName.getItemCount() - 1) {   // lase item, customer
                    tfName.setEditable(true);
                    cbType.setEnabled(true);
                    customized = true;
                } else {
                    tfName.setEditable(false);
                    cbType.setEnabled(false);
                    customized = false;
                }
            }
        });

        ((TitledBorder)cusPane.getBorder()).setTitle(
                app.getText("Dialog.Properties.Attributes.Add.CustomizedPane"));

                labelCName.setText(labelName.getText());
        labelType.setText(app.getText("Dialog.Properties.Attributes.Add.LabelType"));
        for (String type : types) {
            cbType.addItem(type);
        }
    }

    public boolean isCustomized() {
        return customized;
    }

    public int getName() {
        return cbName.getSelectedIndex();
    }

    public int getType() {
        return cbType.getSelectedIndex();
    }

    public String getInput() {
        return tfName.getText();
    }

    @Override
    public void destroy() {
    }

    @Override
    public JPanel getPane() {
        return root;
    }
}
