/*
 * Copyright 2015 Peng Wan <phylame@163.com>
 *
 * This file is part of PAT Core.
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

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.util.MissingResourceException;

/**
 * Action for IxIn components.
 * Like the {@code Action} and {@code IAction} contains an unique ID and and tip text.
 */
public abstract class IAction extends AbstractAction {
    private static Log LOG = LogFactory.getLog(IAction.class);

    public static final String STATUS_TIP = "IxInStatusTip";

    /** Size of small icon */
    public static int smallIconSize = 16;

    /** Size of large icon */
    public static int largeIconSize = 24;

    // {command, key} or {command, name, iconName, accelerator, toolTip}
    public IAction(Object[] model, I18nSupport i18n) {
        init(model, i18n);
    }

    public IAction(String command, String key, I18nSupport i18n) {
        init(command, key, i18n);
    }

    public IAction(String command, String name, String icon, KeyStroke accelerator, String toolTip) {
        init(command, name, icon, accelerator, toolTip);
    }

    public IAction(String command, String text, Icon icon, int mnemonic, KeyStroke accelerator, String toolTip) {
        init(command, text, icon, null, mnemonic, accelerator, toolTip);
    }

    public IAction(String command, String text, Icon smallIcon, Icon largeIcon, int mnemonic, KeyStroke accelerator,
                   String toolTip) {
        init(command, text, smallIcon, largeIcon, mnemonic, accelerator, toolTip);
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
        String toolTip = null;

        try {
            icon = i18n.getText(key + ".Icon");
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }

        try {
            accelerator = IToolkit.getKeyStroke(i18n.getText(key + ".Shortcut"));
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }

        try {
            toolTip = i18n.getText(key + ".Tip");
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }

        init(command, name, icon, accelerator, toolTip);
    }

    protected void init(String command, String name, String icon, KeyStroke accelerator, String toolTip) {
        Object[] pair = IToolkit.parseTextMnemonic(name);
        String text = (String) pair[0];
        int mnemonic = (int) pair[1];
        Icon smallIcon = null, largeIcon = null;
        if (icon != null) {
            smallIcon = IToolkit.createImageIcon(icon, smallIconSize);
            largeIcon = IToolkit.createImageIcon(icon, largeIconSize);
        }

        init(command, text, smallIcon, largeIcon, mnemonic, accelerator, toolTip);
    }

    protected void init(String command, String text, Icon smallIcon, Icon largeIcon, int mnemonic, KeyStroke accelerator,
                      String toolTip) {
        setCommand(command);
        setText(text);
        setSmallIcon(smallIcon);
        setLargeIcon(largeIcon);
        setMnemonic(mnemonic);
        setAccelerator(accelerator);
        setToolTip(toolTip);
        setStatusTip(toolTip);
    }

    protected void init(Object[] model, I18nSupport i18n) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        if (model.length == 2) {
            init((String) model[0], (String) model[1], i18n);
            return;
        } else if (model.length != 5) {
            throw new IllegalArgumentException("model requires minimum 2 or 5 elements");
        }

        String command = (String) model[0], name = (String) model[1], icon = (String) model[2];

        KeyStroke accelerator = IToolkit.getKeyStroke(model[3]);

        String toolTip = (String) model[4];

        init(command, name, icon, accelerator, toolTip);
    }

    public String getText() {
        return (String) getValue(NAME);
    }

    public void setText(String text) {
        putValue(NAME, text);
    }

    public String getCommand() {
        return (String) getValue(ACTION_COMMAND_KEY);
    }

    public void setCommand(Object id) {
        putValue(ACTION_COMMAND_KEY, id);
    }

    public Icon getSmallIcon() {
        return (Icon) getValue(SMALL_ICON);
    }

    public void setSmallIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
    }

    public void setLargeIcon(Icon icon) {
        putValue(LARGE_ICON_KEY, icon);
    }

    public Icon getLargeIcon() {
        return (Icon) getValue(LARGE_ICON_KEY);
    }

    public int getMnemonic() {
        return (int) getValue(MNEMONIC_KEY);
    }

    public void setMnemonic(int mnemonic) {
        putValue(MNEMONIC_KEY, mnemonic < 0 ? 0 : mnemonic);
    }

    public KeyStroke getAccelerator() {
        return (KeyStroke) getValue(ACCELERATOR_KEY);
    }

    public void setAccelerator(KeyStroke accelerator) {
        putValue(ACCELERATOR_KEY, accelerator);
    }

    public String getToolTip() {
        return (String) getValue(SHORT_DESCRIPTION);
    }

    public void setToolTip(String statusTip) {
        putValue(SHORT_DESCRIPTION, statusTip);
    }

    public void setStatusTip(String toolTip) {
        putValue(STATUS_TIP, toolTip);
    }

    public String getStatusTip() {
        return (String) getValue(STATUS_TIP);
    }
}
