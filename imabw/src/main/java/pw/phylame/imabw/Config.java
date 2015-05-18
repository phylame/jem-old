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

package pw.phylame.imabw;

import pw.phylame.ixin.ISettings;
import pw.phylame.jem.formats.txt.TxtParser;

import java.awt.*;
import java.util.Locale;

/**
 * Configurations for Imabw.
 */
public class Config extends ISettings {
    public void reset() {
        clear();
        setAppLocale(getAppLocale());

        resetFace();

        resetWindows();

        resetEditor();

        resetTxtPa();
        resetPmabMa();
        resetTxtMa();

        setChanged(true);
    }

    public Locale getAppLocale() {
        return getLocal("app.locale", Locale.getDefault());
    }

    public void setAppLocale(Locale locale) {
        setLocale("app.locale", locale, "Imabw UI language");
    }

    public String getLafTheme() {
        return getString("ui.face.lafTheme", "com.jgoodies.looks.plastic.PlasticLookAndFeel");
    }

    public void setLafTheme(String lafClass) {
        setString("ui.face.lafTheme", lafClass, "SWING L&F style");
    }

    public boolean isDecoratedFrame() {
        return getBoolean("ui.face.decorateTitle", false);
    }

    public void setDecoratedFrame(boolean enable) {
        setBoolean("ui.face.decorateTitle", enable, "Decorate frame border");
    }

    public Font getGlobalFont() {
        return getFont("ui.font.global", new Font(Font.SANS_SERIF, Font.PLAIN, 14));
    }

    public void setGlobalFont(Font font) {
        setFont("ui.font.global", font, "Global UI font");
    }

    public boolean isAntiAliased() {
        return getBoolean("ui.font.aatext", true);
    }

    public void setAntiAliased(boolean enable) {
        setBoolean("ui.font.aatext", enable, "Enable font anti aliasing");
    }

    private void resetFace() {
        setLafTheme(getLafTheme());
        setDecoratedFrame(isDecoratedFrame());
        setGlobalFont(getGlobalFont());
        setAntiAliased(isAntiAliased());
    }

    // ***********************
    // ** Tools and windows **
    // ***********************

    public boolean isShowSidebar() {
        return getBoolean("ui.window.showSidebar", true);
    }

    public void setShowSidebar(boolean visible) {
        setBoolean("ui.window.showSidebar", visible, "Show/hide sidebar(contents tree)");
    }

    public boolean isShowToolbar() {
        return getBoolean("ui.window.showToolbar", true);
    }

    public void setShowToolbar(boolean visible) {
        setBoolean("ui.window.showToolbar", visible, "Show/hide toolbar");
    }

    public boolean isLockToolbar() {
        return getBoolean("ui.window.lockToolbar", true);
    }

    public void setLockToolbar(boolean locked) {
        setBoolean("ui.window.lockToolbar", locked, "Locked toolbar");
    }

    public boolean isShowStatusbar() {
        return getBoolean("ui.window.showStatusbar", true);
    }

    public void settShowStatusbar(boolean visible) {
        setBoolean("ui.window.showStatusbar", visible, "Show/hide statusbar");
    }

    private void resetWindows() {
        setShowToolbar(isShowToolbar());
        setLockToolbar(isLockToolbar());
        setShowSidebar(isShowSidebar());
        settShowStatusbar(isShowStatusbar());

    }

    // ******************
    // ** Editor style **
    // ******************

    public Font getEditorFont() {
        return getFont("editor.style.font", new Font(Font.SERIF, Font.PLAIN, 16));
    }

    public void setEditorFont(Font font) {
        setFont("editor.style.font", font, "Content editor font");
    }

    public Color getEditorBackground() {
        return getColor("editor.style.background", Color.WHITE);
    }

    public void setEditorBackground(Color color) {
        setColor("editor.style.background", color, "Content editor background color");
    }

    public Color getEditorForeground() {
        return getColor("editor.style.foreground", Color.BLACK);
    }

    public void setEditorForeground(Color color) {
        setColor("editor.style.foreground", color, "Content editor foreground color");
    }

    private void resetEditor() {
        setEditorFont(getEditorFont());
        setEditorBackground(getEditorBackground());
        setEditorForeground(getEditorForeground());
    }

    // ****************
    // ** TXT parser **
    // ****************

    public String getTxtPaChapterPattern() {
        return getString("jem.pa.txt.chapterPattern", TxtParser.DEFAULT_CHAPTER_PATTERN);
    }

    public void setTxtPaChapterPattern(String chapterPattern) {
        setString("jem.pa.txt.chapterPattern", chapterPattern,
                "Chapter title regex pattern for reading TXT book");
    }

    public String getTxtPaEncoding() {
        return getString("jem.pa.txt.encoding", System.getProperty("file.encoding"));
    }

    public void setTxtPaEncoding(String encoding) {
        setString("jem.pa.txt.encoding", encoding, "Encoding for reading TXT book");
    }

    private void resetTxtPa() {
        setTxtPaChapterPattern(getTxtPaChapterPattern());
        setTxtPaEncoding(getTxtPaEncoding());
    }

    // ****************
    // ** PMAB maker **
    // ****************

    public String getPmabMaTextEncoding() {
        return getString("jem.ma.pmab.textEncoding", System.getProperty("file.encoding"));
    }

    public void setPmabMaTextEncoding(String encoding) {
        setString("jem.ma.pmab.textEncoding", encoding, "Encoding for write PMAB text");
    }

    private void resetPmabMa() {
        setPmabMaTextEncoding(getPmabMaTextEncoding());
    }

    // ***************
    // ** TXT maker **
    // ***************

    public String getTxtMaEncoding() {
        return getString("jem.pa.txt.encoding", System.getProperty("file.encoding"));
    }

    public void setTxtMaEncoding(String encoding) {
        setString("jem.pa.txt.encoding", encoding, "Encoding for writing TXT book");
    }

    private static String transLineSeparator(String lineSeparator) {
        switch (lineSeparator) {
            case "\r\n":
                return "CRLF";
            case "\r":
                return "CR";
            default:
                return "LF";
        }
    }

    public String getTxtMaLineSeparator() {
        return getString("jem.pa.txt.lineSeparator",
                transLineSeparator(System.getProperty("line.separator")));
    }

    public void setTxtMaLineSeparator(String style) {
        setString("jem.pa.txt.lineSeparator", style, "Line separator for writing TXT book");
    }

    public String getTxtMaParagraphPrefix() {
        return getString("jem.ma.txt.paragraphPrefix", "\u3000\u3000"); // two chinese tabs
    }

    public void setTxtMaParagraphPrefix(String prefix) {
        setString("jem.ma.txt.paragraphPrefix", prefix, "Prefix of each paragraph for writing TXT book");
    }

    private void resetTxtMa() {
        setTxtMaEncoding(getTxtMaEncoding());
        setTxtMaLineSeparator(getTxtMaLineSeparator());
        setTxtMaParagraphPrefix(getTxtMaParagraphPrefix());
    }
}
