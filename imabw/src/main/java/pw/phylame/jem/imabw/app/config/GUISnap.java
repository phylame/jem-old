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

import java.awt.Point;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.SwingConstants;
import pw.phylame.gaf.ixin.ISettings;
import pw.phylame.jem.imabw.app.Imabw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Keeps state for Imabw GUI components.
 */
public class GUISnap extends ISettings {
    private static Log     LOG      = LogFactory.getLog(GUISnap.class);
    private static GUISnap instance = null;

    public static GUISnap getInstance() {
        if (instance == null) {
            try {
                instance = new GUISnap();
            } catch (IOException e) {
                LOG.debug(e);
            }

            Imabw.getInstance().addExitHook(new Runnable() {
                @Override
                public void run() {
                    try {
                        instance.sync();
                    } catch (IOException e) {
                        LOG.debug("cannot sync ui state", e);
                    }
                }
            });
        }

        return instance;
    }

    private GUISnap() throws IOException {
        super(true, Imabw.SETTINGS_HOME + "gui");

        setComment("UI state recorder for Imabw");
    }

    public Dimension getFrameSize() {
        return getDimension("frame.size", new Dimension(978, 571));
    }
    public void setFrameSize(Dimension size) {
        setDimension("frame.size", size, "Size of main frame");
    }

    public Point getFrameLocation() {
        return getPoint("frame.location", null);
    }
    public void setFrameLocation(Point location) {
        if (location != null) {
            setPoint("frame.location", location, "Location of main frame");
        }
    }

    public boolean isShowToolBar() {
        return getBoolean("frame.toolbar.shown", true);
    }
    public void setShowToolBar(boolean visible) {
        setBoolean("frame.toolbar.shown", visible, "Show/hide the main toolbar");
    }

    public boolean isShowStatusBar() {
        return getBoolean("frame.statusbar.shown", true);
    }
    public void setShowStatusBar(boolean visible) {
        setBoolean("frame.statusbar.shown", visible, "Show/hide the status bar");
    }

    public boolean isShowSideBar() {
        return getBoolean("frame.sidebar.shown", true);
    }
    public void setShowSideBar(boolean visible) {
        setBoolean("frame.sidebar.shown", visible, "Show/hide the side bar");
    }

    public int getFrameDividerSize() {
        return getInteger("frame.divider.size", 7);
    }
    public void setFrameDividerSize(int size) {
        setInteger("frame.divider.size", size, "Size of split pane divider");
    }

    public int getFrameDividerLocation() {
        return getInteger("frame.divider.location", 171);
    }
    public void setFrameDividerLocation(int location) {
        setInteger("frame.divider.location", location, "Location of split pane divider");
    }

    public int getFrameTabPlacement() {
        return getInteger("frame.tab.placement", SwingConstants.TOP);
    }
    public void setFrameTabPlacement(int placement) {
        setInteger("frame.tab.placement", placement,
                "Tab placement of TabbedEditor, TOP=1, LEFT=2, BOTTOM=3, RIGHT=4");
    }

    public Dimension getAttributeDialogSize() {
        return getDimension("dialog.attributes.size", new Dimension(878, 514));
    }
    public void setAttributeDialogSize(Dimension size) {
        setDimension("dialog.attributes.size", size, "Size of chapter attribute dialog");
    }

    public Dimension getAttributeDividerLocation() {
        return getDimension("dialog.attributes.divider", new Dimension(256, 254));
    }
    public void setAttributeDividerLocation(Dimension dividers) {
        setDimension("dialog.attributes.divider", dividers,
                "Divider location in chapter attribute dialog, first: cover and right, second: table and intro");
    }
}
