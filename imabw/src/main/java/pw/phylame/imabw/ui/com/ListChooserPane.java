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

import pw.phylame.ixin.com.IPaneRender;
import pw.phylame.jem.core.Part;

import javax.swing.*;
import java.util.List;

public class ListChooserPane implements IPaneRender {
    private JList<Part> list;
    private JPanel      root;
    private JLabel lbTip;

    public ListChooserPane(List<Part> parts, boolean multiple, String tip) {
        lbTip.setText(tip);
        list.setModel(new PartListModel(parts));
        list.setSelectionMode(multiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
                ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
    }

    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }

    @Override
    public void destroy() {

    }

    @Override
    public JPanel getPane() {
        return root;
    }

    private class PartListModel extends AbstractListModel<Part> {

        private List<Part> parts;

        public PartListModel(List<Part> parts) {
            this.parts = parts;
        }

        @Override
        public int getSize() {
            return parts.size();
        }

        @Override
        public Part getElementAt(int index) {
            return parts.get(index);
        }
    }
}
