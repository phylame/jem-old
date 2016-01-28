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

package pw.phylame.imabw.app.ui;

import java.io.IOException;

import pw.phylame.gaf.ixin.ISettings;
import pw.phylame.imabw.app.Constants;

/**
 * Stores state of UI components.
 */
public class UISnap extends ISettings {
    private static UISnap instance;

    public static UISnap sharedInstance() {
        if (instance == null) {
            try {
                instance = new UISnap();
            } catch (IOException e) {
                throw new RuntimeException("cannot load ui snap", e);
            }
        }
        return instance;
    }

    private UISnap() throws IOException {
        super(Constants.SETTINGS_HOME + "snap", true, true);
        setComment("Stores state of UI components");
    }
}
