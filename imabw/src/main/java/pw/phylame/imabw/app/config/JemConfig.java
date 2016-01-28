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

package pw.phylame.imabw.app.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pw.phylame.gaf.core.Settings;
import pw.phylame.imabw.app.Imabw;

/**
 * Configurations for Jem.
 */
public class JemConfig extends Settings {
    private static JemConfig instance;

    public static JemConfig sharedInstance() {
        if (instance == null) {
            try {
                instance = new JemConfig();
            } catch (IOException e) {
                throw new RuntimeException("cannot load jem config", e);
            }
        }
        return instance;
    }

    public static JemConfig dumpedInstance() {
        try {
            JemConfig config = new JemConfig(0);    // no error occurred
            config.update(sharedInstance());
            return config;
        } catch (IOException e) {
            throw new AssertionError("BUG: IOException should't be raised here");
        }
    }

    private JemConfig() throws IOException {
        super(Imabw.SETTINGS_HOME + "jem", true, true);
    }

    private JemConfig(int unused) throws IOException {
        super(null, false, false);
    }

    @Override
    public void reset() {
        clear();
        setComment("Configurations for Jem");

        setGenres(getDefaultGenres());
        setStates(getDefaultStates());
    }

    public String getGenres() {
        return getString("jem.values.genres", getDefaultGenres());
    }

    public void setGenres(String genres) {
        setString("jem.values.genres", genres);
    }

    public String getStates() {
        return getString("jem.values.states", getDefaultStates());
    }

    public void setStates(String states) {
        setString("jem.values.states", states);
    }

    private static final String VALUE_PREFIX = "jem.attribute.";

    public String getAttribute(String key) {
        return getString(VALUE_PREFIX + key, null);
    }

    public void setAttribute(String key, String value) {
        setString(VALUE_PREFIX + key, value);
    }

    public String removeAttribute(String key) {
        return removeItem(VALUE_PREFIX + key);
    }

    public Map<String, String> defaultValues() {
        HashMap<String, String> values = new HashMap<>();
        for (Map.Entry<String, String> entry : itemEntries()) {
            String key = entry.getKey();
            if (key.startsWith(VALUE_PREFIX)) {
                values.put(key.substring(VALUE_PREFIX.length()), entry.getValue());
            }
        }
        return values;
    }

    private String getDefaultGenres() {
        return Imabw.sharedInstance().getText("jem.values.genres");
    }

    private String getDefaultStates() {
        return Imabw.sharedInstance().getText("jem.values.states");
    }
}
