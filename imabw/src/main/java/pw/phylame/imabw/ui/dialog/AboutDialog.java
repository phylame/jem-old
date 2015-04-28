/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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
import pw.phylame.imabw.Constants;
import pw.phylame.jem.core.Jem;

import javax.swing.*;
import java.awt.event.*;

public class AboutDialog extends JDialog {
    private JPanel contentPane;
    private JButton btnClose;
    private JLabel lbInfo;
    private JLabel lbRights;
    private JLabel lbHome;
    private JLabel lbLicense;
    private JLabel lbJem;

    public AboutDialog(JFrame owner) {
        super(owner);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(btnClose);
        final Application app = Application.getApplication();

        btnClose.setText(app.getText("Dialog.About.ButtonClose"));
        btnClose.setToolTipText(app.getText("Dialog.About.ButtonClose.Tip"));
        btnClose.addActionListener(new ActionListener() {
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

        lbInfo.setText(String.format("%s %s on %s (%s)", app.getText("App.Name"), Constants.VERSION,
                System.getProperty("os.name"), System.getProperty("os.arch")));
        lbJem.setText(String.format("Jem core: %s by %s", Jem.VERSION, Jem.VENDOR));
        lbRights.setText(app.getText("App.Rights"));

        lbLicense.setText(String.format("<html><a href=\"%s\">%s</a></html>",
                app.getText("App.LicenseURL"), app.getText("Dialog.About.LabelLicense")));
        lbLicense.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(
                            app.getText("App.LicenseURL")));
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });

        lbHome.setText(String.format("<html><a href=\"%s\">%s</a></html>",
                app.getText("App.HomeURL"), app.getText("Dialog.About.LabelHome")));
        lbHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(
                            app.getText("App.HomeURL")));
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        });

        setTitle(app.getText("Dialog.About.Title"));
        pack();
        setResizable(false);
        setLocationRelativeTo(getOwner());
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void showAbout(JFrame owner) {
        AboutDialog dialog = new AboutDialog(owner);
        dialog.setVisible(true);
    }
}
