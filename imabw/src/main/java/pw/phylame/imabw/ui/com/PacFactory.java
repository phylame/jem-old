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

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

public class PacFactory {
    private static PacProvider getPac(Frame owner, String title, String format) {
        switch (format) {
            case "txt":
                return new TxtPacProvider(owner, title);
            default:
                return null;
        }
    }

    private static PacProvider getPac(Dialog owner, String title, String format) {
        switch (format) {
            case "txt":
                return new TxtPacProvider(owner, title);
            default:
                return null;
        }
    }

    public static Map<String, Object> getArguments(Window parent, String title, String format) {
        PacProvider pac;
        if (parent instanceof Frame) {
            pac = getPac((Frame) parent, title, format);
        } else if (parent instanceof Dialog) {
            pac = getPac((Dialog) parent, title, format);
        } else {
            throw new IllegalArgumentException("parent required Frame or Dialog");
        }
        if (pac == null) {
            return new HashMap<>();     // no PAC returns default arguments (empty)
        }
        return pac.getArguments();
    }
}
