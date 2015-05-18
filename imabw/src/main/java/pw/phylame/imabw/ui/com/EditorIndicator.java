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

package pw.phylame.imabw.ui.com;

import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import pw.phylame.imabw.Imabw;

import pw.phylame.ixin.com.IPaneRender;
import pw.phylame.tools.DateUtils;

import java.util.Date;

public class EditorIndicator implements IPaneRender {
    private JPanel rootPanel;

    private JLabel ruler;
    private JLabel encoding;
    private JLabel words;
    private JLabel time;

    public EditorIndicator() {
        Imabw app = Imabw.getApplication();
        ruler.setToolTipText(app.getText("Frame.Statusbar.Ruler"));
        encoding.setToolTipText(app.getText("Frame.Statusbar.Encoding"));
        words.setToolTipText(app.getText("Frame.Statusbar.Words"));
        time.setToolTipText(app.getText("Frame.Statusbar.Time"));

        setRuler(-1, -1, 0);
        setEncoding(null);
        setWords(-1);

        // per 30 seconds
        time.setText(DateUtils.formatDate(new Date(), "H:mm "));
        new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                time.setText(DateUtils.formatDate(new Date(), "H:mm "));
            }
        }).start();
    }

    @Override
    public void destroy() {
    }

    @Override
    public JPanel getPane() {
        return rootPanel;
    }

    public void setRuler(int row, int column, int selected) {
        StringBuilder sb = new StringBuilder();
        if (row < 0) {
            sb.append("n/a");
        } else {
            sb.append(row).append(",").append(column);
            if (selected > 0) {
                sb.append("/").append(selected);
            }
        }
        ruler.setText(sb.toString());
    }

    public void setEncoding(String codec) {
        if (codec == null) {
            encoding.setText("n/a");
        } else {
            encoding.setText(codec);
        }
    }

    public void setWords(int n) {
        if (n < 0) {
            words.setText("n/a");
        } else {
            words.setText(String.valueOf(n));
        }
    }
}
