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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

class ErrorTracer extends CommonDialog {
    private String tipText;
    private Throwable error;
    // if the text area is shown
    private boolean shown = false;
    private JScrollPane textView = null;

    public ErrorTracer(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.ERROR_DIALOG);
    }

    public ErrorTracer(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
        setDecorationStyleIfNeed(JRootPane.ERROR_DIALOG);
    }

    void setTipText(String tipText) {
        this.tipText = tipText;
    }

    void setError(Throwable error) {
        this.error = error;
    }

    @Override
    protected void createComponents(final JPanel userPane) {
        JLabel tipLabel = new JLabel(tipText);

        final Dimension originSize = new Dimension(460, 138);
        final Dimension detailsSize = new Dimension(480, 260);

        JButton detailsButton = new JButton(
                new IAction("dialog.error.buttonDetails") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (shown) {
                            textView.setVisible(false);
                            setPreferredSize(originSize);
                        } else {
                            textView = new JScrollPane(createTextArea(error));
                            userPane.add(textView, BorderLayout.CENTER);
                            setPreferredSize(detailsSize);
                        }
                        shown = !shown;
                        pack();
                        setLocationRelativeTo(getOwner());
                    }
                });

        controlsPane = createControlsPane(SwingConstants.RIGHT,
                detailsButton, createCloseButton(BUTTON_CLOSE));

        userPane.add(tipLabel, BorderLayout.PAGE_START);

        setPreferredSize(originSize);
    }

    private JTextArea createTextArea(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();

        JTextArea textArea = new JTextArea();
        textArea.setText(sw.toString());
        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        return textArea;
    }
}
