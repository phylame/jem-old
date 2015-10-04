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

import java.awt.Font;
import java.awt.Color;
import java.util.Date;
import java.io.IOException;
import pw.phylame.gaf.ixin.ISettings;
import pw.phylame.jem.imabw.app.Imabw;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides styles of all text editors.
 */
public class EditorStyle extends ISettings {
    private static Log              LOG      = LogFactory.getLog(GUISnap.class);
    private static EditorStyle      instance = null;

    public static EditorStyle getInstance() {
        if (instance == null) {
            try {
                instance = new EditorStyle();
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
                            LOG.debug("cannot sync editor style", e);
                        }
                    }
                }
            });
        }

        return instance;
    }

    private EditorStyle() throws IOException {
        super(true, Imabw.SETTINGS_HOME + "editor");
    }

    @Override
    public void reset() {
        setComment("Text editor configurations\nCreated: " + new Date());

        setFont(getFont());
        setBackground(getBackground());
        setForeground(getForeground());
        setHighlight(getHighlight());
        setLineWarp(isLineWarp());
        setWordWarp(isWordWarp());
        setShowLineNumber(isShowLineNumber());
    }

    public Font getFont() {
        return getFont("editor.font", null);
    }

    public void setFont(Font font) {
        if (font != null) {
            setFont("editor.font", font, "Chapter text editor font");
        } else {
            setProperty("editor.font", "", "Chapter text editor font");
        }
    }

    public Color getBackground() {
        return getColor("editor.background", Color.WHITE);
    }

    public void setBackground(Color color) {
        setColor("editor.background", color, "Chapter text editor background color");
    }

    public Color getForeground() {
        return getColor("editor.foreground", Color.BLACK);
    }

    public void setForeground(Color color) {
        setColor("editor.foreground", color, "Chapter text editor foreground color");
    }

    public Color getHighlight() {
        return getColor("editor.highlight", Color.LIGHT_GRAY);
    }

    public void setHighlight(Color color) {
        setColor("editor.highlight", color, "Chapter text editor highlight color");
    }

    public boolean isLineWarp() {
        return getBoolean("editor.lineWarp", true);
    }

    public void setLineWarp(boolean enable) {
        setBoolean("editor.lineWarp", enable, "Warp line if content is too long");
    }

    public boolean isWordWarp() {
        return getBoolean("editor.wordWarp", true);
    }

    public void setWordWarp(boolean enable) {
        setBoolean("editor.wordWarp", enable, "Warp line if a word is too long");
    }

    public boolean isShowLineNumber() {
        return getBoolean("editor.showLineNumber", false);
    }

    public void setShowLineNumber(boolean visible) {
        setBoolean("editor.showLineNumber", visible, "Show line number in editor");
    }
}
