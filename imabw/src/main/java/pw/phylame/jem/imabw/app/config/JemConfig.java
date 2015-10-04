/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.jem.imabw.app.config;

import java.util.Date;
import java.io.IOException;
import pw.phylame.gaf.core.Settings;
import pw.phylame.jem.imabw.app.Imabw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Configurations for Jem.
 */
public class JemConfig extends Settings {
    private static Log          LOG      = LogFactory.getLog(GUISnap.class);
    private static JemConfig    instance = null;

    public static JemConfig getInstance() {
        if (instance == null) {
            try {
                instance = new JemConfig();
            } catch (IOException e) {
                LOG.debug(e);
            }

            Imabw.getInstance().addExitHook(new Runnable() {
                @Override
                public void run() {
                    if (instance.isChanged()) {
                        try {
                            instance.sync();
                        } catch (IOException e) {
                            LOG.debug("cannot sync jem config", e);
                        }
                    }
                }
            });
        }

        return instance;
    }

    private JemConfig() throws IOException {
        super(true, Imabw.SETTINGS_HOME + "jem");

        setComment("Provides Jem configurations\nCreated: " + new Date());
    }

    public String getAttributeValue(String key) {
        String v = getString("jem.attribute."+key, null);
        if (v != null) {
            return v;
        }
        // save no mapped attribute
        setAttributeValue(key, "");
        return "";
    }
    public void setAttributeValue(String key, String value) {
        setString("jem.attribute."+key, value, "Default attribute "+key+" of chapter or book");
    }
}
