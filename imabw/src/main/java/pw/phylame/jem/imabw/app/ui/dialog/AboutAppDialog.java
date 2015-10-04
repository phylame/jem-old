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
import pw.phylame.gaf.ixin.IResource;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.formats.util.Version;
import pw.phylame.jem.imabw.app.Imabw;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

class AboutAppDialog extends DialogFactory.CommonDialog {
    private static Imabw app = Imabw.getInstance();

    AboutAppDialog(Frame owner) {
        super(owner, app.getText("Dialog.AboutApp.Title"), true);
        init();
    }

    @Override
    protected void createComponents(JPanel topPane) {
        // app icon label
        JLabel iconLabel = new JLabel(IResource.loadIcon(app.getText("App.Icon")));

        // information text
        String str = app.getText("Dialog.AboutApp.Content",
                app.getText("App.Name"), Imabw.VERSION,
                System.getProperty("os.name"), Jem.VERSION, Version.VERSION);

        JLabel textLabel = new JLabel(str, null, JLabel.LEADING);
        textLabel.setBorder(BorderFactory.createEmptyBorder(0, MARGIN, 0, 0));

        JLabel licenseLabel = new JLabel(app.getText("Dialog.AboutApp.License", app.getText("App.License")));
        licenseLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(app.getText("App.License")));
                } catch (Exception ex) {
                    app.debug("cannot open license URL", ex);
                }
            }
        });
        JLabel homeLabel = new JLabel(app.getText("Dialog.AboutApp.Home", app.getText("App.Home")));
        homeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(app.getText("App.Home")));
                } catch (Exception ex) {
                    app.debug("cannot open home URL", ex);
                }
            }
        });

        JButton btnClose = new JButton(new IAction("Dialog.AboutApp.ButtonClose") {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // link label
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        linkPanel.add(licenseLabel);
        linkPanel.add(homeLabel);

        // app information panel
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);
        textPanel.add(linkPanel, BorderLayout.PAGE_END);

        JPanel messagePane = new JPanel();
        messagePane.setLayout(new BoxLayout(messagePane, BoxLayout.LINE_AXIS));
        messagePane.add(iconLabel);
        messagePane.add(textPanel);

        topPane.add(messagePane, BorderLayout.CENTER);

        buttonPane = makeButtonPane(SwingConstants.RIGHT, btnClose);
        btnDefault = btnClose;

        setPreferredSize(new Dimension(420, 200));
    }

    @Override
    protected void onCancel() {
        dispose();
    }
}
