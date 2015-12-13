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

package pw.phylame.imabw.app.ui.com;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.imabw.app.ui.dialog.DialogFactory;

/**
 * Label with URI that can be visited.
 */
public class LinkedLabel extends JLabel {
    private String uri;

    public LinkedLabel(String text, String uri) {
        super(makeHtml(text, uri));
        this.uri = uri;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                DialogFactory.browseURI(uri);
            }
        });
    }

    public String getUri() {
        return uri;
    }

    public static LinkedLabel fromAction(String actionKey, String uriKey) {
        Imabw app = Imabw.sharedInstance();
        LinkedLabel label = new LinkedLabel(app.getText(actionKey), app.getText(uriKey));
        label.setToolTipText(app.getText(actionKey + IAction.tipKeySuffix));
        return label;
    }

    private static String makeHtml(String text, String uri) {
        return "<html><a href=\"" + uri + "\">" + text + "</a></html>";
    }
}
