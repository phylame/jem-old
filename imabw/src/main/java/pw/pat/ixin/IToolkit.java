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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pw.pat.gaf.I18nSupport;
import pw.pat.ixin.event.IStatusTipEvent;
import pw.pat.ixin.event.IStatusTipListener;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.*;

/**
 * The IxIn toolkit.
 */
public final class IToolkit {
    private static Log LOG = LogFactory.getLog(IToolkit.class);

    public static final char MNEMONIC_PREFIX = '&';

    private static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private static <T> boolean isEmpty(T[] ary) {
        return ary == null || ary.length == 0;
    }

    private static <E> boolean isEmpty(Collection<E> col) {
        return col == null || col.size() == 0;
    }

    /** Get L&F class name by short index name. */
    public static String getLookAndFeel(String name) {
        String lookAndFeel;
        if (name == null) {
            lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        } else if ("java".equalsIgnoreCase(name)) {
            lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        } else if ("system".equalsIgnoreCase(name)) {
            lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        } else if ("metal".equalsIgnoreCase(name)) {
            lookAndFeel = "javax.swing.plaf.metal.MetalLookAndFeel";
        } else if ("motif".equalsIgnoreCase(name) ||
                "cde/motif".equalsIgnoreCase(name)) {
            lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        } else if ("nimbus".equalsIgnoreCase(name)) {
            lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
        } else {
            lookAndFeel = name;
        }
        return lookAndFeel;
    }

    private static int indexParent(String path) {
        int index = -1;
        for (int i = path.length()-1; i >= 0; --i) {
            char ch = path.charAt(i);
            if (ch == '/' || ch == '\\') {
                index = i;
                break;
            }
        }
        return index;
    }

    // URL: [parent_dir]/[size]x/[base_name]
    public static ImageIcon createImageIcon(String path, int size) {
        int index = indexParent(path);
        if (index == -1) {  // no parent
            path = Integer.toString(size) + "x/" + path;
        } else {
            path = path.substring(0, index) + "/" + size + "x/" +
                    path.substring(index+1);
        }
        return createImageIcon(path);
    }

    public static ImageIcon createImageIcon(String path) {
        if (isEmpty(path)) {
            return null;
        }
        URL url = null;
        if (path.startsWith(":") || path.startsWith("!")) {
            url = IToolkit.class.getResource(path.substring(1));
        } else {
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                LOG.debug("Malformed icon URL: " + path, e);
            }
        }
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }

    public static Image createImage(Object source) {
        ImageIcon imageIcon;
        if (source instanceof Image) {
            return (Image) source;
        } else if (source instanceof ImageIcon) {
            imageIcon = (ImageIcon) source;
        } else if (source instanceof String) {
            imageIcon = createImageIcon((String) source);
        } else {
            imageIcon = null;
        }
        return imageIcon == null ? null : imageIcon.getImage();
    }

    public static Icon getIcon(Object source) {
        if (source instanceof Icon) {
            return (Icon) source;
        } else if (source instanceof String) {
            return createImageIcon((String) source);
        } else {
            return null;
        }
    }

    public static int getMnemonic(Object source) {
        if (source instanceof Integer) {
            return (int) source;
        } else if (source instanceof Character) {
            return (char) source;
        } else if (source instanceof String) {
            String s = (String) source;
            return s.length() == 0 ? 0 : s.charAt(0);
        } else {
            return 0;
        }
    }

    public static KeyStroke getKeyStroke(Object source) {
        if (source instanceof KeyStroke) {
            return (KeyStroke) source;
        } else if (source instanceof String) {
            return KeyStroke.getKeyStroke((String) source);
        } else {
            return null;
        }
    }

    /**
     * Parses text and mnemonic from name.
     * @param name the name
     * @return array of text, mnemonic
     */
    public static Object[] parseTextMnemonic(String name) {
        // get mnemonic from name
        String text = name;
        int mnemonic = 0;

        int index = name.indexOf(MNEMONIC_PREFIX);
        if (index >= 0 && index < name.length()) {
            char next = name.charAt(index+1);
            if (Character.isLetter(next)) {     // has mnemonic
                mnemonic = next;
                text = name.substring(0, index) + name.substring(index+1);
            }
        }
        return new Object[]{text, mnemonic};
    }

    public static void addKeyboardAction(JComponent comp, Action action, int condition) {
        comp.registerKeyboardAction(action,
                (String) action.getValue(Action.ACTION_COMMAND_KEY),
                (KeyStroke) action.getValue(Action.ACCELERATOR_KEY), condition);
    }

    public static void addKeyboardActions(JComponent comp, Collection<Action> actions,
                                          int condition) {
        for (Action action: actions) {
            addKeyboardAction(comp, action, condition);
        }
    }

    public static void addStatusTipListener(JComponent comp, Action action,
                                            IStatusTipListener tipListener) {
        addStatusTipListener(comp, (String) action.getValue(IAction.STATUS_TIP), tipListener);
    }

    public static void addStatusTipListener(JComponent comp, final String text,
                                            final IStatusTipListener tipListener) {
        if (tipListener== null) {
            throw new NullPointerException("tipListener");
        }

        if (isEmpty(text)) {
            return;
        }

        comp.addMouseListener(new MouseAdapter() {
            private IStatusTipEvent createEvent(Object source) {
                return new IStatusTipEvent(source, text);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tipListener.showingTip(createEvent(e));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tipListener.closingTip(createEvent(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                tipListener.closingTip(createEvent(e));
            }
        });

        comp.setToolTipText(null);
    }

    // key: action base key in resource bundle
    public static IAction createAction(String command, String key,
                                       final ActionListener listener, I18nSupport i18n) {
        return new IAction(command, key, i18n) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
    }

    // command, name, iconName, accelerator, toolTip
    public static IAction createAction(Object[] model, final ActionListener listener,
                                       I18nSupport i18n) {
        return new IAction(model, i18n) {
            @Override
            public void actionPerformed(ActionEvent e) {
                listener.actionPerformed(e);
            }
        };
    }

    public static void updateAction(Action action, String key, I18nSupport i18n) {
        String name = i18n.getText(key);
        Object[] pair = IToolkit.parseTextMnemonic(name);
        String text = (String) pair[0];
        int mnemonic = (int) pair[1];

        action.putValue(Action.NAME, text);
        action.putValue(Action.MNEMONIC_KEY, mnemonic);

        String icon = null;
        try {
            icon = i18n.getText(key + ".Icon");
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }
        Icon smallIcon = null, largeIcon = null;
        if (icon != null && icon.length() != 0) {
            smallIcon = IToolkit.createImageIcon(icon, IAction.smallIconSize);
            largeIcon = IToolkit.createImageIcon(icon, IAction.largeIconSize);
        }
        action.putValue(Action.SMALL_ICON, smallIcon);
        action.putValue(Action.LARGE_ICON_KEY, largeIcon);

        KeyStroke accelerator = null;
        try {
            accelerator = IToolkit.getKeyStroke(i18n.getText(key + ".Shortcut"));
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }
        action.putValue(Action.ACCELERATOR_KEY, accelerator);

        String tipText = null;
        try {
            tipText = i18n.getText(key + ".Tip");
        } catch (MissingResourceException e) {
            LOG.debug("not found field for frame in bundle", e);
        }
        action.putValue(Action.SHORT_DESCRIPTION, tipText);
        action.putValue(IAction.STATUS_TIP, tipText);
    }

    public static Map<String, Action> createActions(Object[][] models,
                                                    ActionListener listener, I18nSupport i18n) {
        Map<String, Action> actionMap = new HashMap<>();
        for (Object[] model: models) {
            Action action = createAction(model, listener, i18n);
            actionMap.put((String) action.getValue(Action.ACTION_COMMAND_KEY), action);
        }
        return actionMap;
    }

    /**
     * Creates menu item with action.
     * @param action the action for menu item, never {@code null}
     * @param menuModel model of this item, if {@code null} return {@code JMenuItem}.
     * @param tipListener tool tip action listener, if {@code null} do nothing
     * @return the menu item
     */
    public static JMenuItem createMenuItem(Action action, IMenuModel menuModel,
                                           IStatusTipListener tipListener) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        JMenuItem menuItem;
        if (menuModel == null) {
            menuItem = new JMenuItem(action);
        } else {
            switch (menuModel.getType()) {
                case RADIO:
                    JRadioButtonMenuItem rmi = new JRadioButtonMenuItem(action);
                    rmi.setSelected(menuModel.getState());
                    menuItem = rmi;
                    break;
                case CHECK:
                    JCheckBoxMenuItem cmi = new JCheckBoxMenuItem(action);
                    cmi.setSelected(menuModel.getState());
                    menuItem = cmi;
                    break;
                default:
                    menuItem = new JMenuItem(action);
                    break;
            }
        }
        addStatusTipListener(menuItem, action, tipListener);
        return menuItem;
    }

    public static void setMenuLabel(JMenu menu, IMenuLabel menuLabel) {
        menu.setText(menuLabel.getText());
        menu.setIcon(menuLabel.getIcon());
        menu.setMnemonic(menuLabel.getMnemonic());
    }

    public static int addMenuItems(JComponent menu, Object[] model,
                                   Map<String, Action> actionMap,
                                   IStatusTipListener tipListener) {
        if (isEmpty(model)) {
            return 0;
        }
        if (! (menu instanceof JMenu) && ! (menu instanceof JPopupMenu)) {
            throw new IllegalArgumentException("menu must be JMenu or JPopupMenu");
        }

        int i = 0;
        /* JMenu label */
        if (menu instanceof JMenu && model[i] instanceof IMenuLabel) {
            setMenuLabel((JMenu) menu, (IMenuLabel) model[i]);
            ++i;
        }

        int n = 0;
        ButtonGroup group = null;
        for (; i<model.length; ++i) {
            Object key = model[i];
            JComponent item = null;
            if (key == null) {                      // separator
                item = new JPopupMenu.Separator();
            } else if (key instanceof IMenuModel) { // menu model
                IMenuModel menuModel = (IMenuModel) key;
                Action action = actionMap.get(menuModel.getID());
                if (action != null) {
                    item = createMenuItem(action, menuModel, tipListener);
                    // radio menu
                    if (menuModel.getType() == IMenuModel.MenuType.RADIO) {
                        if (group == null) {
                            group = new ButtonGroup();
                        }
                        group.add((JMenuItem) item);
                    } else if (group != null) { // not radio and previous is radio
                        group = null;
                    }
                }
            } else if (key instanceof Object[]) {   // sub menu
                JMenu subMenu = new JMenu();
                addMenuItems(subMenu, (Object[]) key, actionMap, tipListener);
                item = subMenu;
            } else {                                // menu item
                Action action = actionMap.get(key);
                if (action != null) {
                    item = createMenuItem(action, null, tipListener);
                }
            }
            if (item != null) {
                menu.add(item);
                ++n;
            }
        }
        return n;
    }

    /**
     * Creates button with action.
     * @param action the action
     * @param tipListener tool tip action listener
     * @return the {@code JButton} object
     */
    public static JButton createButton(Action action, IStatusTipListener tipListener) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        JButton button = new JButton(action);
        addStatusTipListener(button, action, tipListener);
        return button;
    }

    public static void addButton(JToolBar toolBar, Action action,
                                 IStatusTipListener tipListener) {
        if (action == null) {
            throw new NullPointerException("action");
        }

        JButton button = createButton(action, tipListener);
        if (button.getIcon() != null) {
            button.setText(null);   // hide button text
        }
        toolBar.add(button);
    }

    /**
     * Gets buttons from actions and added to toolbar.
     * @param toolBar the toolbar
     * @param actionIDs ID of all added button action ID
     * @param actionMap action map
     * @param tipListener status tip listener
     * @return number of added buttons
     */
    public static int addButtons(JToolBar toolBar, List<String> actionIDs,
                                 Map<String, Action> actionMap,
                                 IStatusTipListener tipListener) {
        if (isEmpty(actionIDs)) {
            return 0;
        }
        int n = 0;
        for (String id: actionIDs) {
            if (id == null) {   // separator
                toolBar.addSeparator();
                continue;
            }
            Action action = actionMap.get(id);
            if (action == null) {   // not found
                continue;
            }
            addButton(toolBar, action, tipListener);
            ++n;
        }
        return n;
    }
}
