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

package pw.phylame.imabw.ui.com.impl;

import pw.phylame.imabw.Application;
import pw.phylame.imabw.ui.com.PacProvider;
import pw.phylame.jem.formats.txt.TxtParser;
import pw.phylame.tools.StringUtils;

import javax.swing.*;
import java.util.Map;
import java.util.HashMap;

public class TxtPacProvider implements PacProvider {
    private JPanel     root;
    private JTextField tfChapterPattern;
    private JComboBox<String>  cbEncoding;
    private JLabel     lbChapterPattern;
    private JLabel     lbEncoding;

    public TxtPacProvider() {
        Application app = Application.getApplication();
        lbChapterPattern.setText(app.getText("Dialog.TxtPac.LabelCP"));
        lbEncoding.setText(app.getText("Dialog.TxtPac.LabelEncoding"));
        String o = (String) app.getSetting("jem.pa.txt.chapterPattern");
        if (StringUtils.isEmpty(o)) {
            o = TxtParser.DEFAULT_CHAPTER_PATTERN;
        }
        tfChapterPattern.setText(o);
        o = (String) app.getSetting("jem.pa.txt.encoding");
        if (o == null) {
            o = System.getProperty("file.encoding");
        }
        int index = -1, i = 0;
        for (String encoding: app.getWorker().getEncodings()) {
            cbEncoding.addItem(encoding);
            ++i;
            if (encoding.equals(o)) {   // contains in encodings
                index = i;
            }
        }
        if (index == -1) {  // not exists
            cbEncoding.addItem((String) o);
        }
        cbEncoding.setSelectedItem(o);
    }

    @Override
    public Map<String, Object> getArguments() {
        HashMap<String, Object> map = new HashMap<>();
        map.put(TxtParser.KEY_CHAPTER_PATTERN, tfChapterPattern.getText());
        map.put(TxtParser.KEY_TEXT_ENCODING, cbEncoding.getSelectedItem());
        return map;
    }

    @Override
    public void destroy() {

    }

    @Override
    public JPanel getPane() {
        return root;
    }
}
