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

import pw.phylame.imabw.ui.com.impl.EpubMacProvider;
import pw.phylame.imabw.ui.com.impl.PmabMacProvider;
import pw.phylame.imabw.ui.com.impl.TxtMacProvider;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MacFactory {
    public static MacProvider getMac(Frame owner, String title, String format) {
        switch (format) {
            case "pmab":
                return new PmabMacProvider(owner, title);
            case "epub":
                return new EpubMacProvider(owner, title);
            case "txt":
                return new TxtMacProvider(owner, title);
            default:
                return null;
        }
    }

    public static MacProvider getMac(Dialog owner, String title, String format) {
        switch (format) {
            case "pmab":
                return new PmabMacProvider(owner, title);
            case "epub":
                return new EpubMacProvider(owner, title);
            case "txt":
                return new TxtMacProvider(owner, title);
            default:
                return null;
        }
    }

    public static Map<String, Object> getArguments(Window parent, String title, String format) {
        MacProvider mac;
        if (parent instanceof Frame) {
            mac = getMac((Frame) parent, title, format);
        } else if (parent instanceof Dialog) {
            mac = getMac((Dialog) parent, title, format);
        } else {
            throw new IllegalArgumentException("parent required Frame or Dialog");
        }
        if (mac == null) {
            return new HashMap<>();     // no MAC returns default arguments (empty)
        }
        return mac.getArguments();
    }
}
