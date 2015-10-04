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

class WaitingDialog extends DialogFactory.CommonDialog {
    private String tipText, waitingText;

    private JLabel          lbTip;
    private JProgressBar    progressBar;
    private JButton         btnCancel;

    private WaitingWork     work;

    WaitingDialog(Frame owner, String title, String tipText, String waitingText) {
        super(owner, title, true);
        this.tipText = tipText;
        this.waitingText = waitingText;
        init();
    }

    WaitingDialog(Dialog owner, String title, String tipText, String waitingText) {
        super(owner, title, true);
        this.tipText = tipText;
        this.waitingText = waitingText;
        init();
    }

    void setWork(WaitingWork work, boolean cancelable, boolean waiting) {
        if (work == null) {
            throw new NullPointerException("work");
        }
        this.work = work;
        work.setDialog(this);
        progressBar.setIndeterminate(waiting);
        btnCancel.setEnabled(cancelable);
    }

    @Override
    protected void createComponents(JPanel topPane) {
        lbTip = new JLabel(tipText);
        lbTip.setBorder(BorderFactory.createEmptyBorder(0, 0, MARGIN, 0));

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString(waitingText);

        JPanel pane = new JPanel(new GridLayout(2, 1));
        pane.add(lbTip);
        pane.add(progressBar);

        topPane.add(pane, BorderLayout.CENTER);

        btnCancel = new JButton(new IAction("Dialog.Waiting.ButtonCancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        btnCancel.setEnabled(false);

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnCancel);
        btnDefault = btnCancel;

        setPreferredSize(new Dimension(427, 118));
    }

    @Override
    protected void onCancel() {
        if (btnCancel.isEnabled()) {
            work.cancel(true);
        }
    }

    void setTipText(String tipText) {
        lbTip.setText(tipText);
    }

    void setWaitingText(String waitingText) {
        progressBar.setString(waitingText);
    }

    void setProgress(int v) {
        progressBar.setValue(v);
    }
}
