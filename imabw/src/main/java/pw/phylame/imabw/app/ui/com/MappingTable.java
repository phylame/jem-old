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

package pw.phylame.imabw.app.ui.com;

import pw.phylame.gaf.ixin.IAction;
import pw.phylame.gaf.ixin.IxinUtilities;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;

public abstract class MappingTable<K, V> extends JPanel {
    public static final String APPEND = "mappingTable.appendItem";
    public static final String REMOVE = "mappingTable.removeItem";
    public static final String MODIFY = "mappingTable.modifyItem";
    protected final List<String> commands = new ArrayList<>();

    {
        Collections.addAll(commands, APPEND, REMOVE, MODIFY);
    }

    private final int columns;
    protected int keyColumn = 0, valueColumn = 1;

    private ArrayList<K> keys = new ArrayList<>();
    private Map<K, V> map;

    protected MappingTable(int columns) {
        super(new BorderLayout());
        this.columns = columns;
    }

    protected void createComponents(String title, String comment) {
        if (title != null) {
            IxinUtilities.setTitleBorder(this, title);
        }
        for (String command : commands) {
            Action action = new IAction(command) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    commandPerformed(command);
                }
            };
            actions.put(command, action);
        }

        model = new TableModel();
        table = new JTable(model);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = table.getSelectedRow();
                    if (!isEditable(row, table.getSelectedColumn())) {
                        modifyRowAt(row);
                    }
                }
            }
        });
        table.getSelectionModel().addListSelectionListener(e ->
                        itemSelected(table.getSelectedRows())
        );

        toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.setBorderPainted(false);
        IxinUtilities.addToolItems(toolBar, commands.toArray(), actions, null);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(toolBar, BorderLayout.LINE_END);
        if (comment != null) {
            add(new JLabel(comment), BorderLayout.PAGE_END);
        }
    }

    public void setActionEnable(String key, boolean enable) {
        Action action = actions.get(key);
        if (action == null) {
            throw new RuntimeException("no such action: " + key);
        }
        action.setEnabled(enable);
    }

    protected abstract String nameOfColumn(int column);

    protected abstract Object valueOfCell(K key, int column);

    protected boolean isEditable(int row, int column) {
        return false;
    }

    protected abstract void createItem();

    protected void updateCell(int row, int column, K key, Object value) {

    }

    protected void itemSelected(int[] rows) {
        setActionEnable(REMOVE, rows.length > 0);
        setActionEnable(MODIFY, rows.length == 1);
    }

    private void modifyRowAt(int row) {
        K key = keyAt(row);
        V value = modifyValue(key, map.get(key));
        if (value != null) {
            map.put(key, value);
            model.fireTableCellUpdated(row, valueColumn);
            onModified();
        }
    }

    protected abstract V modifyValue(K key, Object oldValue);

    protected boolean commandPerformed(String command) {
        switch (command) {
            case REMOVE: {
                removeRows(table.getSelectedRows());
                break;
            }
            case MODIFY: {
                modifyRowAt(table.getSelectedRow());
                break;
            }
            case APPEND:
                createItem();
                break;
            default:
                return false;
        }
        return true;
    }

    public List<K> getKeys() {
        return keys;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public K keyAt(int row) {
        return keys.get(row);
    }

    public V valueFor(K key) {
        return map.get(key);
    }

    public V valueAt(int row) {
        return valueFor(keyAt(row));
    }

    protected void onModified() {

    }

    public void scrollToRow(int row) {
        table.setRowSelectionInterval(row, row);
        table.scrollRectToVisible(table.getCellRect(row, 0, true));
    }

    public void resetAll(Map<K, V> map, Set<K> ignoredKeys, Comparator<K> comparator) {
        this.map = map;
        HashSet<K> dumps = new HashSet<>(map.keySet());
        if (ignoredKeys != null) {
            dumps.removeAll(ignoredKeys);
        }
        keys.clear();
        keys.addAll(dumps);
        if (comparator != null) {
            Collections.sort(keys, comparator);
        }
        model.fireTableDataChanged();
        if (!keys.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        } else {
            setActionEnable(REMOVE, false);
            setActionEnable(MODIFY, false);
        }
    }

    public void appendItem(K key, V value) {
        keys.add(key);
        map.put(key, value);
        int count = keys.size();
        model.fireTableRowsInserted(count, count);
        scrollToRow(count - 1);
        onModified();
    }

    public void removeRows(int[] rows) {
        ArrayList<K> dumps = new ArrayList<>(keys);
        for (int row : rows) {
            K key = dumps.get(row);
            keys.remove(key);
            map.remove(key);
        }
        model.fireTableDataChanged();
        int row = rows[0];
        if (row == table.getRowCount()) {       // focus to the next row
            --row;
        }
        if (row > -1) {
            scrollToRow(row);
        } else {    // no more row
            setActionEnable(REMOVE, false);
        }
        onModified();
    }

    public void updateKey(int row, K oldKey, K newKey) {
        keys.set(row, newKey);
        map.put(newKey, map.get(oldKey));
        map.remove(oldKey);
    }

    private class TableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return keys.size();
        }

        @Override
        public int getColumnCount() {
            return columns;
        }

        @Override
        public String getColumnName(int column) {
            return nameOfColumn(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return valueOfCell(keyAt(rowIndex), columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return isEditable(rowIndex, columnIndex);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Object oldValue = getValueAt(rowIndex, columnIndex);
            if (Objects.equals(aValue, oldValue)) {
                return;
            }
            K key = keyAt(rowIndex);
            if (columnIndex == keyColumn) {
                updateKey(rowIndex, key, (K) aValue);
            } else if (columnIndex == valueColumn) {
                map.put(key, (V) aValue);
            } else {
                updateCell(rowIndex, columnIndex, keyAt(rowIndex), aValue);
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            onModified();
        }
    }

    protected JTable table;
    protected TableModel model;
    protected JToolBar toolBar;
    private Map<String, Action> actions = new HashMap<>();
}
