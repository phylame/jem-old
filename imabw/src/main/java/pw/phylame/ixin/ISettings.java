/*
 * Copyright 2014-2015 Peng Wan <phylame@163.com>
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

package pw.phylame.ixin;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides common settings interface.
 */
public class ISettings extends HashMap<String, Object> {

    public ISettings() {
        super();
    }

    public ISettings(URL source) throws IOException {
        load(source);
    }

    public ISettings(Map<String, Object> m) {
        if (m != null) {
            putAll(m);
        }
    }

    public void load(URL source) throws IOException {
    }

    public void store(OutputStream out) throws IOException {

    }

    /**
     * Resets all value to the default.
     */
    public void reset() {

    }
}
