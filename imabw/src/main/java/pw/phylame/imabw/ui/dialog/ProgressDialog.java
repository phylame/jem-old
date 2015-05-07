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

import pw.phylame.imabw.Application;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ProgressDialog extends JDialog {
    private JPanel       contentPane;
    private JButton      buttonStop;
    private JProgressBar progressBar;
    private Thread thread = null;

    public ProgressDialog(Dialog owner, String title, String message) {
        super(owner, title, true);
        init(message);
    }

    public ProgressDialog(Frame owner, String title, String message) {
        super(owner, title, true);
        init(message);
    }

    private void init(String message) {
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonStop);

        Application app = Application.getApplication();

        progressBar.setString(message);

        buttonStop.setText(app.getText("Dialog.Progress.ButtonStop"));
        buttonStop.setToolTipText(app.getText("Dialog.Progress.ButtonStop.Tip"));

        buttonStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStop();
            }
        });

        // call onStop() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onStop();
            }
        });

        // call onStop() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
                                               public void actionPerformed(ActionEvent e) {
                                                   onStop();
                                               }
                                           }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
        setSize(getWidth() * 3, getHeight());
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void onStop() {
        stop();
        dispose();
    }

    public void start() {
        if (thread != null) {   // already started
            return;
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
        thread.start();
    }

    public void stop() {
        if (thread != null) {
            setVisible(false);
            thread.interrupt();
        }
    }
}
