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

package pw.phylame.imabw.app.ui.dialog;

import java.awt.*;
import javax.swing.*;

import pw.phylame.imabw.app.Imabw;
import pw.phylame.gaf.ixin.IOptionDialog;

class MessageDialog extends IOptionDialog {
    private static final Imabw app = Imabw.sharedInstance();

    public MessageDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public MessageDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    @Override
    protected JPanel createControlsPane(int alignment, Component... components) {
        JPanel pane = super.createControlsPane(alignment, components);
        pane.setBackground(pane.getBackground().darker());
        return pane;
    }

    void setRightAlignedOptions(int defaultOption, Object... options) {
        setOptions(SwingConstants.RIGHT, defaultOption, options);
    }

    void addCloseButton(int alignment) {
        setOptions(alignment, 0, CommonDialog.BUTTON_CLOSE);
    }

    void setDecorationStyleIfNeed(int style) {
        if (isUndecorated()) {
            getRootPane().setWindowDecorationStyle(style);
        }
    }

    void setIconStyle(IconStyle style) {
        if (style == null) {
            setIcon(null);
            setDecorationStyleIfNeed(JRootPane.NONE);
        } else {
            setIcon(app.loadIcon("dialog/" + style.name + ".png"));
            setDecorationStyleIfNeed(style.decorationStyle);
        }
    }

    enum IconStyle {
        Alert("alert", JRootPane.ERROR_DIALOG),
        Information("information", JRootPane.INFORMATION_DIALOG),
        Prohibit("prohibit", JRootPane.ERROR_DIALOG),
        Question("question", JRootPane.QUESTION_DIALOG),
        Success("success", JRootPane.INFORMATION_DIALOG),
        Warning("warning", JRootPane.WARNING_DIALOG),
        Save("save", JRootPane.QUESTION_DIALOG);

        String name;
        int decorationStyle;

        IconStyle(String name, int decorationStyle) {
            this.name = name;
            this.decorationStyle = decorationStyle;
        }
    }
}
