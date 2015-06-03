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

package pw.phylame.imabw.ui;

import pw.pat.ixin.ISettings;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Dimension;

/**
 * Saves state of UI components.
 */
public class UIState extends ISettings {
    private static UIState instance = null;

    public static UIState getInstance() {
        if (instance == null) {
            instance = new UIState();
        }
        return instance;
    }

    private UIState() {
        super(true, "windows");
    }

    public Dimension getViewerSize() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        return getDimension("viewer.size", new Dimension((int) (d.getWidth() * 0.8), (int) (d.getWidth() * 0.45)));
    }

    public void setViewerSize(Dimension size) {
        setDimension("viewer.size", size, null);
    }

    public Point getViewerLocation() {
        return getPoint("viewer.location", null);
    }

    public void setViewerLocation(Point location) {
        setPoint("viewer.location", location, null);
    }

    public int getDividerLocation() {
        return getInteger("viewer.dividerLocation", -1);
    }

    public void setDividerLocation(int location) {
        setInteger("viewer.dividerLocation", location, null);
    }

    public int getDividerSize() {
        return getInteger("viewer.dividerSize", -1);
    }

    public void setDividerSize(int location) {
        setInteger("viewer.dividerSize", location, null);
    }
}
