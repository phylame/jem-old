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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

class ExceptionTraceDialog extends DialogFactory.CommonDialog {
    private String          tipText;
    private Throwable       ex;
    // if the text area is shown
    private boolean         shown = false;
    private JScrollPane     textView = null;

    ExceptionTraceDialog(Frame owner, String title, String tipText, Throwable e) {
        super(owner, title, true);
        this.tipText = tipText;
        this.ex = e;
        init();
        btnDefault.requestFocus();
    }

    ExceptionTraceDialog(Dialog owner, String title, String tipText, Throwable e) {
        super(owner, title, true);
        this.tipText = tipText;
        this.ex = e;

        init();
        btnDefault.requestFocus();
    }

    @Override
    protected void createComponents(final JPanel topPane) {
        JLabel tipLabel = new JLabel(tipText);
        tipLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, MARGIN, 0));

        final Dimension originSize = new Dimension(460, 138);
        final Dimension detailsSize = new Dimension(480, 260);

        JButton btnDetails = new JButton(new IAction("Dialog.Exception.ButtonDetails") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (shown) {
                    textView.setVisible(false);
                    setPreferredSize(originSize);
                } else {
                    textView = new JScrollPane(createTextArea(ex));
                    topPane.add(textView, BorderLayout.CENTER);
                    setPreferredSize(detailsSize);
                }
                shown = !shown;
                pack();
                setLocationRelativeTo(getOwner());
            }
        });

        JButton btnClose = new JButton(new IAction("Dialog.Exception.ButtonClose") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnDetails, btnClose);
        btnDefault = btnClose;

        topPane.add(tipLabel, BorderLayout.PAGE_START);

        setPreferredSize(originSize);
    }

    private JTextArea createTextArea(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();

        JTextArea textArea = new JTextArea();
        textArea.setText(sw.toString());
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        return textArea;
    }

    @Override
    protected void onCancel() {
        dispose();
    }
}
