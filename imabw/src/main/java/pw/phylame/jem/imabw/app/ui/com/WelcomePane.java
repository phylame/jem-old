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

package pw.phylame.jem.imabw.app.ui.com;

import pw.phylame.jem.imabw.app.Imabw;

import java.awt.*;
import javax.swing.*;

/**
 * This pane will be shown when no editor tab opened.
 */
public class WelcomePane extends JPanel {
    private static Imabw app = Imabw.getInstance();

    public WelcomePane() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        initComps();
    }

    private void initComps() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);

        label.setText(app.getText("Frame.Welcome.Text"));

        add(label, BorderLayout.PAGE_START);
    }
}
