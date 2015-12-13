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

import pw.phylame.gaf.ixin.ICommonDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Common dialog for Imabw.
 */
public abstract class CommonDialog<R> extends ICommonDialog {
    public static final String BUTTON_OK = "dialog.common.buttonOk";
    public static final String BUTTON_CANCEL = "dialog.common.buttonCancel";
    public static final String BUTTON_CLOSE = "dialog.common.buttonClose";

    public CommonDialog(Dialog owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    public CommonDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);
    }

    R getResult() {
        return null;
    }

    R makeShow(boolean resizable) {
        initialize(resizable);
        setVisible(true);
        return getResult();
    }

    @Override
    protected JPanel createControlsPane(int alignment, Component... components) {
        JPanel pane = super.createControlsPane(alignment, components);
        pane.setBackground(pane.getBackground().darker());
        return pane;
    }
}
