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

import pw.phylame.imabw.ui.com.impl.TxtPacProvider;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PacFactory {
    public static PacProvider getPac(String format) {
        switch (format) {
            case "txt":
                return new TxtPacProvider();
            default:
                return null;
        }
    }

    public static Map<String, Object> getArguments(Component parent, String title, String format) {
        PacProvider pac = getPac(format);
        if (pac == null) {
            return new HashMap<>();     // no PAC returns default arguments (empty)
        }
        int opt = JOptionPane.showOptionDialog(parent, pac.getPane(), title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (opt != JOptionPane.OK_OPTION) {
            return null;
        }
        return pac.getArguments();
    }
}
