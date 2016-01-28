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

package pw.phylame.imabw.app.ui.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.util.LinkedList;

class SearchHelper {
    private static final int MAX_COUNT = 20;
    static LinkedList<String> sources = new LinkedList<>();
    static LinkedList<String> targets = new LinkedList<>();

    private TextEditor editor;

    private int position = 0;

    SearchHelper(TextEditor editor) {
        this.editor = editor;
        editor.getTextComponent().addCaretListener(e -> position = e.getDot());
    }

    private String getMatchText() {
        String match;
        if (editor.searchBar != null) {
            match = editor.searchBar.tfSource.getText();
            if (match.isEmpty()) {
                editor.searchBar.nextAction.setEnabled(false);
                editor.searchBar.prevAction.setEnabled(false);
            } else {
                editor.searchBar.nextAction.setEnabled(true);
                editor.searchBar.prevAction.setEnabled(true);
                if (sources.size() <= MAX_COUNT) {
                    sources.push(match);
                }
            }
        } else {
            match = sources.getFirst();
        }
        return match;
    }

    int findNext() {
        String match = getMatchText();
        if (match.isEmpty()) {
            return -1;
        }
        JTextComponent comp = editor.getTextComponent();
        String text = comp.getText();
        int cursor = text.indexOf(match, position);
        if (editor.searchBar != null) {
            editor.searchBar.nextAction.setEnabled(cursor != -1 || position != 0);
            editor.searchBar.prevAction.setEnabled(editor.searchBar.nextAction.isEnabled());
            editor.searchBar.tfSource.setBackground(
                    editor.searchBar.nextAction.isEnabled() ? Color.WHITE : Color.PINK);

            if (editor.searchBar.tfTarget != null) {
                editor.searchBar.btnReplace.setEnabled(cursor != -1);
                editor.searchBar.btnReplaceAll.setEnabled(cursor != -1);
            }
        }
        if (cursor != -1) {     // found
            comp.setSelectionStart(cursor);
            comp.setSelectionEnd(cursor + match.length());
            position = cursor + match.length();
        } else {
            position = 0;
        }
        text = null;
        return cursor;
    }

    void findPrevious() {
        String match = getMatchText();
        if (match.isEmpty()) {
            return;
        }
        JTextComponent comp = editor.getTextComponent();
        String text = comp.getText();
        int cursor = text.lastIndexOf(match, position);
        if (editor.searchBar != null) {
            editor.searchBar.prevAction.setEnabled(cursor != -1 || position != text.length());
            editor.searchBar.nextAction.setEnabled(editor.searchBar.prevAction.isEnabled());
            editor.searchBar.tfSource.setBackground(
                    editor.searchBar.prevAction.isEnabled() ? Color.WHITE : Color.PINK);
        }
        if (cursor != -1) {
            comp.setSelectionStart(cursor);
            comp.setSelectionEnd(cursor + match.length());
            position = cursor - match.length();
        } else {
            position = text.length();
        }
        text = null;
    }

    void replaceNext() {
        String target = editor.searchBar.tfTarget.getText();
        int cursor = findNext();
        if (cursor != -1) {
            Document doc = editor.getTextComponent().getDocument();
            try {
                doc.remove(cursor, sources.getFirst().length());
                doc.insertString(cursor, target, null);
                position = cursor + target.length();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    void replaceAll() {
        System.out.println("replace all");
    }
}
