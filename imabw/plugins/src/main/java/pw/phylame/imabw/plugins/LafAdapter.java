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

import java.util.Map;

import pw.phylame.gaf.core.Plugin;
import pw.phylame.imabw.app.config.UIConfig;

import javax.swing.*;

public class LafAdapter implements Plugin {

    @Override
    public Map<String, Object> properties() {
        return null;
    }

    @Override
    public void initialize() {
        UIConfig uiConfig = UIConfig.sharedInstance();
        if (uiConfig.getLafTheme().toLowerCase().contains("jgoodies")) {
            MyUtils.invokeStaticMethod("com.jgoodies.looks.plastic.PlasticLookAndFeel", "setTabStyle", "metal");
        }
    }

    @Override
    public void destroy() {

    }
}
