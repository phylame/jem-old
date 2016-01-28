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

import javax.swing.JComboBox;
import java.util.function.Function;

public class NamedComboBox<E> extends JComboBox<String> {
    private E[] items;

    public NamedComboBox(E[] items, Function<E, String> converter) {
        this.items = items;
        for (E item : items) {
            addItem(converter.apply(item));
        }
    }

    public final E itemAt(int index) {
        return items[index];
    }

    public final E currentItem() {
        int index = getSelectedIndex();
        return index != -1 ? itemAt(index) : null;
    }

    public final void activateItem(E item) {
        for (int ix = 0; ix < items.length; ++ix) {
            if (items[ix].equals(item)) {
                setSelectedIndex(ix);
                break;
            }
        }
    }
}
