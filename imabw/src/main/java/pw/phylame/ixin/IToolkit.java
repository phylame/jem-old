/*
 * Copyright 2015 Peng Wan <phylame@163.com>
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

package pw.phylame.ixin;

import pw.phylame.ixin.com.IAction;
import pw.phylame.ixin.com.IMenuLabel;
import pw.phylame.ixin.com.IMenuModel;
import pw.phylame.ixin.com.InvalidActionModelException;
import pw.phylame.ixin.event.IActionListener;
import pw.phylame.ixin.event.IStatusTipEvent;
import pw.phylame.ixin.event.IStatusTipListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The IxIn toolkit.
 */
public final class IToolkit {

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
        } else if ("motif".equalsIgnoreCase(name)) {
            lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        } else if ("nimbus".equalsIgnoreCase(name)) {
            lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
        } else {
            lookAndFeel = name;
        }
        return lookAndFeel;
    }

    public static ImageIcon createImageIcon(String path) {
        if (null == path || "".equals(path)) {
            return null;
        }
        URL url = null;
        if (path.startsWith(":") || path.startsWith("!")) {
            url = IToolkit.class.getResource(path.substring(1));
        } else {
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
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

    public static void addStatusTipListener(Component comp, final IAction action, final IStatusTipListener tipListener) {
        if (tipListener == null || action.getStatusTip() == null) {
            return;
        }
        comp.addMouseListener(new MouseAdapter() {
            public IStatusTipEvent createEvent(Object source) {
                return new IStatusTipEvent(source, action.getStatusTip());
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
        /* hide default tip note */
        action.setToolTip(null);
    }

    public static void addInputAction(JComponent comp, IAction action) {
        if (action == null) {
            return;
        }
        comp.getActionMap().put(action.getId(), action);
        comp.getInputMap().put(action.getAccelerator(), action.getId());
    }

    public static void addInputActions(JComponent comp, Collection<IAction> actions) {
        for (IAction action: actions) {
            addInputAction(comp, action);
        }
    }

    public static Map<Object, IAction> createActions(Object[][] actionModel, IActionListener actionListener) {
        Map<Object, IAction> actionMap = new HashMap<>();
        for (Object[] model: actionModel) {
            IAction action = null;
            try {
                action = IAction.createAction(model, actionListener);
            } catch (InvalidActionModelException e) {
                e.printStackTrace();
            }
            if (action != null) {
                actionMap.put(action.getId(), action);
            }
        }
        return actionMap;
    }

    /**
     * Create a {@code JMenuItem}
     * @param action the action for menu item, never {@code null}
     * @param menuModel model of this item, if {@code null} return {@code JMenuItem}.
     * @param tipListener tool tip action listener, if {@code null} do nothing
     * @return the menu item
     */
    public static JMenuItem createMenuItem(IAction action, IMenuModel menuModel, IStatusTipListener tipListener) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        JMenuItem menuItem;
        if (menuModel == null) {
            menuItem = new JMenuItem(action);
        } else {
            switch (menuModel.getMenuType()) {
                case RADIO:
                    JRadioButtonMenuItem rmi = new JRadioButtonMenuItem(action.getText(), menuModel.getState());
                    rmi.setAction(action);
                    menuItem = rmi;
                    break;
                case CHECK:
                    JCheckBoxMenuItem cmi = new JCheckBoxMenuItem(action);
                    cmi.setState(menuModel.getState());
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

    public static boolean addMenuItem(JComponent menu, Object[] model, Map<Object, IAction> actionMap,
                                      IStatusTipListener tipListener) {
        if (model == null || model.length == 0) {
            return false;
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
            if (key == null) {      // separator
                item = new JPopupMenu.Separator();
            } else if (key instanceof IMenuModel) {
                IMenuModel menuModel = (IMenuModel) key;
                IAction action = actionMap.get(menuModel.getActionId());
                if (action != null) {
                    item = createMenuItem(action, menuModel, tipListener);
                    // radio menu
                    if (menuModel.getMenuType() == IMenuModel.MenuType.RADIO) {
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
                addMenuItem(subMenu, (Object[]) key, actionMap, tipListener);
                item = subMenu;
            } else {        // action id
                IAction action = actionMap.get(key);
                if (action != null) {
                    item = createMenuItem(action, null, tipListener);
                }
            }
            if (item != null) {
                menu.add(item);
                ++n;
            }
        }
        return n != 0;
    }

    /**&
     * Create button by {@code IAction}.
     * @param action the action
     * @param tipListener tool tip action listener
     * @return {@code JButton}
     */
    public static JButton createButton(IAction action, IStatusTipListener tipListener) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        JButton button = new JButton(action);
        addStatusTipListener(button, action, tipListener);
        return button;
    }

    // {id or null}...
    public static boolean addButton(JToolBar toolBar, List<Object> actionIDs, Map<Object, IAction> actionMap,
                                    IStatusTipListener tipListener) {
        if (actionIDs == null || actionIDs.size() == 0) {
            return false;
        }
        int n = 0;
        for (Object id: actionIDs) {
            if (id == null) {   // separator
                toolBar.addSeparator();
                continue;
            }
            IAction action = actionMap.get(id);
            if (action == null) {   // not found
                continue;
            }
            JButton button = createButton(action, tipListener);
            /* hide button text */
            button.setText(null);
            toolBar.add(button);
            ++n;
        }
        return n != 0;
    }
}
