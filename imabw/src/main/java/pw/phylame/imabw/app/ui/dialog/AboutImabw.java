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

import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.com.LinkedLabel;
import pw.phylame.jem.core.Jem;
import pw.phylame.jem.formats.util.Versions;

/**
 * Dialog to show information of Imabw.
 */
class AboutImabw extends CommonDialog {
    private static final Imabw app = Imabw.sharedInstance();

    AboutImabw(Frame owner) {
        super(owner, app.getText("d.aboutApp.title"), true);
        initialize(false);
        setDecorationStyleIfNeed(JRootPane.INFORMATION_DIALOG);
    }

    @Override
    protected void createComponents(JPanel userPane) {
        // app icon label
        JLabel iconLabel = new JLabel(app.localizedIcon("app.icon"));

        // information text
        String str = app.getText("d.aboutApp.content", app.getText("app.name"),
                Imabw.VERSION, System.getProperty("os.name"), Jem.VERSION, Versions.VERSION);

        JLabel textLabel = new JLabel(str, null, JLabel.LEADING);

        JLabel licenseLabel = LinkedLabel.fromAction("d.aboutApp.license", "app.license");
        JLabel homeLabel = LinkedLabel.fromAction("d.aboutApp.home", "app.home");
        JLabel contactLabel = LinkedLabel.fromAction("d.aboutApp.contact", "app.email");

        // link label
        JPanel linkPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        linkPane.add(licenseLabel);
        linkPane.add(homeLabel);
        linkPane.add(contactLabel);

        // app information panel
        JPanel textPane = new JPanel(new BorderLayout());
        textPane.add(textLabel, BorderLayout.CENTER);
        textPane.add(linkPane, BorderLayout.PAGE_END);

        JPanel infoPane = new JPanel();
        infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.LINE_AXIS));
        infoPane.add(iconLabel);
        infoPane.add(textPane);

        userPane.add(infoPane, BorderLayout.CENTER);

        defaultButton = createCloseButton(BUTTON_CLOSE);
        controlsPane = createControlsPane(SwingConstants.RIGHT, defaultButton);
    }
}
