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

package pw.pat.ixin;

import pw.pat.gaf.I18nSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.util.MissingResourceException;

/**
 * Action for IxIn components.
 * Like the {@code Action} but contains an unique ID (also: the command text).
 */
public abstract class IAction extends AbstractAction {
    private static Log LOG = LogFactory.getLog(IAction.class);

    /** Message shown in status bar */
    public static final String STATUS_TIP = "IxInStatusTip";

    /** Size of small icon */
    public static int smallIconSize = 16;

    /** Size of large icon */
    public static int largeIconSize = 24;

    // {command, key, [enable]} or {command, name, iconName, accelerator, tipText, [enable]}
    public IAction(Object[] model, I18nSupport i18n) {
        init(model, i18n);
    }

    public IAction(String key, I18nSupport i18n) {
        init(null, key, i18n);
    }

    public IAction(String command, String key, I18nSupport i18n) {
        init(command, key, i18n);
    }

    public IAction(String command, String name, String icon, KeyStroke accelerator,
                   String tipText) {
        init(command, name, icon, accelerator, tipText);
    }

    public IAction(String command, String text, Icon smallIcon, Icon largeIcon,
                   int mnemonic, KeyStroke accelerator,
                   String tipText) {
        init(command, text, smallIcon, largeIcon, mnemonic, accelerator, tipText);
    }

    /**
     * Initializes action from resource bundle.
     * @param command command of this action
     * @param key base key of action
     * @param i18n I18N provider
     */
    protected void init(String command, String key, I18nSupport i18n) {
        String name = i18n.getText(key);
        String icon = null;
        KeyStroke accelerator = null;
        String tipText = null;

        try {
            icon = i18n.getText(key + ".Icon");
        } catch (MissingResourceException e) {
            LOG.debug("not found field in bundle", e);
        }

        try {
            accelerator = IToolkit.getKeyStroke(i18n.getText(key + ".Shortcut"));
        } catch (MissingResourceException e) {
            LOG.debug("not found field in bundle", e);
        }

        try {
            tipText = i18n.getText(key + ".Tip");
        } catch (MissingResourceException e) {
            LOG.debug("not found field in bundle", e);
        }

        init(command, name, icon, accelerator, tipText);
    }

    protected void init(String command, String name, String icon,
                        KeyStroke accelerator, String tipText) {
        Object[] pair = IToolkit.parseTextMnemonic(name);
        String text = (String) pair[0];
        int mnemonic = (int) pair[1];
        Icon smallIcon = null, largeIcon = null;
        if (icon != null && icon.length() != 0) {
            smallIcon = IToolkit.createImageIcon(icon, smallIconSize);
            largeIcon = IToolkit.createImageIcon(icon, largeIconSize);
        }

        init(command, text, smallIcon, largeIcon, mnemonic, accelerator, tipText);
    }

    // 2: {command, key}
    // 3: {command, key, enable}
    // 5: {command, name, icon, accelerator, tipText}
    // 6: {command, name, icon, accelerator, tipText, enable}
    protected void init(Object[] model, I18nSupport i18n) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        if (model.length == 2) {
            init((String) model[0], (String) model[1], i18n);
            return;
        } else if (model.length == 3) {
            init((String) model[0], (String) model[1], i18n);
            setEnabled((boolean) model[2]);
            return;
        } else if (model.length < 5) {
            throw new IllegalArgumentException("model requires minimum 2-3 or 5-6 elements");
        }

        String command = (String) model[0], name = (String) model[1],
                icon = (String) model[2];

        KeyStroke accelerator = IToolkit.getKeyStroke(model[3]);

        String tipText = (String) model[4];

        init(command, name, icon, accelerator, tipText);

        if (model.length == 6) {
            setEnabled((boolean) model[5]);
        }
    }

    protected void init(String command, String text, Icon smallIcon, Icon largeIcon,
                        int mnemonic, KeyStroke accelerator,
                        String tipText) {
        putValue(ACTION_COMMAND_KEY, command);
        putValue(NAME, text);
        putValue(SMALL_ICON, smallIcon);
        putValue(LARGE_ICON_KEY, largeIcon);
        putValue(MNEMONIC_KEY, mnemonic);
        putValue(ACCELERATOR_KEY, accelerator);
        putValue(SHORT_DESCRIPTION, tipText);
        putValue(STATUS_TIP, tipText);
    }
}
