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

import javax.swing.*;
import java.awt.*;

class WaitingDialog extends CommonDialog {
    private String tipText, waitingText;

    private JLabel tipLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;

    private WaitingWork work;
    private boolean cancelable, waiting;

    public WaitingDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public WaitingDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    void setTipText(String tipText) {
        this.tipText = tipText;
    }

    void setWaitingText(String waitingText) {
        this.waitingText = waitingText;
    }

    void setWork(WaitingWork work, boolean cancelable, boolean waiting) {
        if (work == null) {
            throw new NullPointerException("work");
        }
        this.work = work;
        work.setDialog(this);
        this.cancelable = cancelable;
        this.waiting = waiting;
    }

    @Override
    protected void createComponents(JPanel userPane) {
        tipLabel = new JLabel(tipText);

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString(waitingText);
        progressBar.setIndeterminate(waiting);

        userPane.setLayout(new GridLayout(2, 1));
        userPane.add(tipLabel);
        userPane.add(progressBar);

        cancelButton = createCloseButton("dialog.waiting.buttonCancel");
        cancelButton.setEnabled(cancelable);
        defaultButton = cancelButton;
        controlsPane = createControlsPane(SwingConstants.RIGHT, cancelButton);

        setPreferredSize(new Dimension(427, 118));
    }

    @Override
    protected void onCancel() {
        if (cancelButton.isEnabled()) {
            work.cancel(true);
        }
    }

    void updateTipText(String tipText) {
        tipLabel.setText(tipText);
    }

    void updateWaitingText(String waitingText) {
        progressBar.setString(waitingText);
    }

    void setProgress(int v) {
        progressBar.setValue(v);
    }
}
