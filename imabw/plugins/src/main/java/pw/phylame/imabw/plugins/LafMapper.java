/*
 * Copyright 2014-2016 Peng Wan <phylame@163.com>
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

package pw.phylame.imabw.plugins;

import pw.phylame.gaf.core.GafUtils;
import pw.phylame.gaf.core.Plugin;
import pw.phylame.gaf.ixin.IxinUtilities;
import pw.phylame.imabw.app.Constants;
import pw.phylame.imabw.app.Imabw;
import pw.phylame.jem.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class LafMapper implements Plugin {
    @Override
    public Map<String, Object> properties() {
        return null;
    }

    @Override
    public void initialize() {
        try {
            loadLafs();
        } catch (IOException e) {
            Imabw.sharedInstance().error(e, "cannot load user laf");
        }
    }

    private void loadLafs() throws IOException {
        Imabw app = Imabw.sharedInstance();
        File file = new File(GafUtils.pathInHome(Constants.SETTINGS_HOME), "userlafs");
        try (FileInputStream fis = new FileInputStream(file)) {
            for (String line : IOUtils.toLines(fis, "UTF-8", true)) {
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    app.error("user laf map require name:className");
                    continue;
                }
                IxinUtilities.mapLafPath(parts[0].trim(), parts[1].trim());
            }
        }
    }

    @Override
    public void destroy() {

    }
}
