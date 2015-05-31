/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
 *
 * This file is part of Jem.
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

package pw.phylame.jem.formats.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * I18N for book parsers and makers.
 */
public final class I18N {
    public static final String I18N_PATH = "pw/phylame/jem/formats/messages";
    private static ResourceBundle bundle = null;

    private I18N() {
    }

    public static String getText(String key) {
        return bundle.getString(key);
    }

    public static String getText(String key, Object... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }

    private static void loadBundle() {
        bundle = ResourceBundle.getBundle(I18N_PATH);
    }

    static {
        loadBundle();
    }
}
