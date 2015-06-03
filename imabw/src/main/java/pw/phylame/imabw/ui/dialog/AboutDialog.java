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

import java.awt.Frame;
import java.awt.event.*;
import javax.swing.*;

import pw.pat.ixin.IAction;
import pw.pat.ixin.IToolkit;
import pw.phylame.imabw.Imabw;
import pw.phylame.jem.core.Jem;

public class AboutDialog extends JDialog {
    private JPanel contentPane;
    private JLabel labelMessage;
    private JLabel labelJem;
    private JLabel labelRights;
    private JLabel labelLicense, labelHome;
    private JButton buttonClose;

    public AboutDialog(Frame owner) {
        super(owner, true);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonClose);

        final Imabw app = Imabw.getInstance();

        IAction closeAction = new IAction(null, "Dialog.About.ButtonClose", app) {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        buttonClose.setAction(closeAction);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(closeAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        labelMessage.setText(String.format("%s v%s on %s (%s)", app.getText("App.Name"),
                Imabw.VERSION,
                System.getProperty("os.name"), System.getProperty("os.arch")));
        labelJem.setText(String.format("Jem core: %s by %s", Jem.VERSION, Jem.VENDOR));
        labelRights.setText(app.getText("App.Rights"));

        labelLicense.setText(String.format("<html><a href=\"%s\">%s</a></html>",
                app.getText("App.LicenseURL"), app.getText("Dialog.About.LabelLicense")));
        labelLicense.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(app.getText("App.LicenseURL")));
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });

        labelHome.setText(String.format("<html><a href=\"%s\">%s</a></html>",
                app.getText("App.HomeURL"), app.getText("Dialog.About.LabelHome")));
        labelHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(app.getText("App.HomeURL")));
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });

        setTitle(app.getText("Dialog.About.Title", app.getText("App.Name")));
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    public static void showAbout(Frame owner) {
        AboutDialog dialog = new AboutDialog(owner);
        dialog.setVisible(true);
    }
}
