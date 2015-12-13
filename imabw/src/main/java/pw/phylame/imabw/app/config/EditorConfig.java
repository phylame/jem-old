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

import java.awt.Font;
import java.awt.Color;
import java.util.Date;
import java.io.IOException;

import pw.phylame.gaf.ixin.ISettings;
import pw.phylame.imabw.app.Imabw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JTabbedPane;

/**
 * Provides styles of all text editors.
 */
public class EditorConfig extends ISettings {
    private static final Log LOG = LogFactory.getLog(EditorConfig.class);

    private static EditorConfig instance = null;

    public static EditorConfig sharedInstance() {
        if (instance == null) {
            try {
                instance = new EditorConfig();
            } catch (IOException e) {
                LOG.debug("cannot load editor config", e);
            }
        }
        return instance;
    }

    public static EditorConfig dumpedInstance() {
        try {
            EditorConfig config = new EditorConfig(0);    // no error occurred
            config.update(sharedInstance());
            return config;
        } catch (IOException e) {
            throw new AssertionError("BUG: IOException should't be raised here");
        }
    }

    private EditorConfig() throws IOException {
        super(true, Imabw.SETTINGS_HOME + "editor", true);
    }

    private EditorConfig(int unused) throws IOException {
        super(false, null, false);
    }

    @Override
    public void reset() {
        setComment("Text editor configurations\nCreated: " + new Date());

        setFont(null);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        setHighlight(Color.LIGHT_GRAY);
        setLineWarp(true);
        setWordWarp(true);
        setShowLineNumber(false);

        setTabLayout(JTabbedPane.SCROLL_TAB_LAYOUT);
        setTabPlacement(JTabbedPane.TOP);
    }

    public int getTabPlacement() {
        return getInteger("editor.tab.placement", JTabbedPane.TOP);
    }

    public void setTabPlacement(int placement) {
        setInteger("editor.tab.placement", placement);
    }

    public int getTabLayout() {
        return getInteger("editor.tab.layout", JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public void setTabLayout(int layout) {
        setInteger("editor.tab.layout", layout);
    }

    public Font getFont() {
        return getFont("editor.font", null);
    }

    public void setFont(Font font) {
        if (font != null) {
            setFont("editor.font", font);
        } else {
            setString("editor.font", "");
        }
    }

    public Color getBackground() {
        return getColor("editor.background", Color.WHITE);
    }

    public void setBackground(Color color) {
        setColor("editor.background", color);
    }

    public Color getForeground() {
        return getColor("editor.foreground", Color.BLACK);
    }

    public void setForeground(Color color) {
        setColor("editor.foreground", color);
    }

    public Color getHighlight() {
        return getColor("editor.highlight", Color.LIGHT_GRAY);
    }

    public void setHighlight(Color color) {
        setColor("editor.highlight", color);
    }

    public boolean isLineWarp() {
        return getBoolean("editor.lineWarp", true);
    }

    public void setLineWarp(boolean enable) {
        setBoolean("editor.lineWarp", enable);
    }

    public boolean isWordWarp() {
        return getBoolean("editor.wordWarp", true);
    }

    public void setWordWarp(boolean enable) {
        setBoolean("editor.wordWarp", enable);
    }

    public boolean isShowLineNumber() {
        return getBoolean("editor.showLineNumber", false);
    }

    public void setShowLineNumber(boolean visible) {
        setBoolean("editor.showLineNumber", visible);
    }
}
