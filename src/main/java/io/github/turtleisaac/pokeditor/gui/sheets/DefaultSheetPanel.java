/*
 * Created by JFormDesigner on Wed Dec 15 18:04:50 CST 2021
 */

package io.github.turtleisaac.pokeditor.gui.sheets;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FrozenColumnTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.renderers.MultiLineTableHeaderRenderer;
import net.miginfocom.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author turtleisaac
 */
public class DefaultSheetPanel<E extends GenericFileData> extends JPanel
{
    private Dimension lastSize;

    private final PokeditorManager manager;

    private final DefaultTable<E> table;
    private final FrozenColumnTable<E> frozenColumns;

    public DefaultSheetPanel(PokeditorManager manager, DefaultTable<E> table) {
        initComponents();
        this.manager = manager;
        this.table = table;
//        table.setColumnSelectionAllowed(true);
        scrollPane1.setViewportView(table);
//        resizeColumnWidth(table1);
        setIcons();

        frozenColumns = new FrozenColumnTable<>(table.getFormatModel().getFrozenColumnModel());
        scrollPane1.setRowHeaderView(frozenColumns);
        resizeColumnWidth(frozenColumns);
        scrollPane1.getRowHeader().setMaximumSize(new Dimension(frozenColumns.getPreferredSize().width, scrollPane1.getRowHeader().getMaximumSize().height));
        scrollPane1.getRowHeader().setPreferredSize(new Dimension(frozenColumns.getPreferredSize().width, scrollPane1.getRowHeader().getMaximumSize().height));

        scrollPane1.setCorner(JScrollPane.UPPER_LEFT_CORNER, frozenColumns.getCornerTableHeader());

        table.getSelectionModel().addListSelectionListener(e -> frozenColumns.clearSelection());
        frozenColumns.getSelectionModel().addListSelectionListener(e -> table.clearSelection());

        linkTableSelectionIndicators(table, frozenColumns);
        linkTableSelectionIndicators(frozenColumns, table);

        frozenColumns.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                String property = evt.getPropertyName();
                if (frozenColumns.getSelectedColumn() == 1 && (property.equals("tableCellEditor") || property.equals("selectionBackground")))
                    manager.resetAllIndexedCellRendererText();
            }
        });
    }

    private void setIcons()
    {
        zoomOutButton.setIcon(ThemeUtils.zoomOutIcon);
        zoomInButton.setIcon(ThemeUtils.zoomInIcon);
        saveSheetButton.setIcon(ThemeUtils.saveIcon);
        reloadSheetButton.setIcon(ThemeUtils.reloadIcon);
        exportSheetButton.setIcon(PokeditorManager.sheetExportIcon);
        importSheetButton.setIcon(PokeditorManager.sheetImportIcon);
        addRowButton.setIcon(PokeditorManager.rowInsertIcon);
        deleteRowButton.setIcon(PokeditorManager.rowRemoveIcon);
        findButton.setIcon(PokeditorManager.searchIcon);
        copyModeButton.setIcon(PokeditorManager.clipboardIcon);
    }

    public DefaultTable<E> getTable()
    {
        return table;
    }

    private void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++)
        {
            int width = 40;
            for (int row = 0; row < table.getRowCount(); row++)
            {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
                width = Math.max(width, table.getColumnModel().getColumn(column).getPreferredWidth());
            }
            if (width > 300)
                width = 300;
            columnModel.getColumn(column).setPreferredWidth(width + 5);
        }
    }

    private void linkTableSelectionIndicators(JTable table, JTable otherTable)
    {
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(Color.black);
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else if (table.getSelectedRow() == row || otherTable.getSelectedRow() == row) {
                    c.setBackground(table.getSelectionBackground());
                }
                else {
                    if (row % 2 == 0)
                        c.setBackground(table.getBackground());
                    else
                        c.setBackground(new Color(248, 221, 231));
                }
                c.validate();
                c.repaint();
                return c;
            }
        });
    }

    private void zoomOutButtonPressed(ActionEvent e) {
        Font oldFont = this.table.getFont();
//        table1.setRowHeight(table1.getRowHeight() - 1);
        this.table.setFont(oldFont.deriveFont(Font.PLAIN, oldFont.getSize() - 1));

//        Font oldHeaderFont = table1.getTableHeader().getFont();
//        table1.getTableHeader().setFont(oldHeaderFont.deriveFont(Font.BOLD, oldFont.getSize() - 1));
        resizeColumnWidth(this.table);
    }

    private void zoomInButtonPressed(ActionEvent e) {
        Font oldFont = this.table.getFont();
//        table1.setRowHeight(table1.getRowHeight() + 1);
        this.table.setFont(oldFont.deriveFont(Font.PLAIN, oldFont.getSize() + 1));

//        Font oldHeaderFont = table1.getTableHeader().getFont();
//        table1.getTableHeader().setFont(oldHeaderFont.deriveFont(Font.BOLD, oldFont.getSize() + 1));
        resizeColumnWidth(this.table);
    }

    private void addRowButtonPressed(ActionEvent e) {
        List<GenericFileData> data = (List<GenericFileData>) table.getFormatModel().getData();
        try
        {
            GenericFileData v = data.get(0).getClass().getDeclaredConstructor().newInstance();
            data.add(v);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void deleteRowButtonPressed(ActionEvent e) {
        // todo figure out how I'm going to remove the name of the species when a row is deleted (because right now the name just stays at that index and the data moves up)
        table.getFormatModel().getData().remove(table.getSelectedRow());
    }

    private void exportSheetButtonPressed(ActionEvent e) {
        // TODO add your code here
        manager.writeSheet(table.exportClean());
    }

    private void importSheetButtonPressed(ActionEvent e) {
        // TODO add your code here
        JOptionPane.showMessageDialog(this, "Lol you wish", "Nope", JOptionPane.ERROR_MESSAGE);
    }

    private void saveSheetButtonPressed(ActionEvent e) {
        manager.saveData(table.getDataClass());
    }

    private void reloadSheetButtonPressed(ActionEvent e) {
        manager.resetData(table.getDataClass());
        table.getFormatModel().fireTableDataChanged();
    }

    private void findButtonPressed(ActionEvent e) {
        // TODO add your code here
        FindDialog findDialog = new FindDialog(this);
    }

    private void copyModeButtonPressed(ActionEvent e) {
        // TODO add your code here
        table.getFormatModel().toggleCopyPasteMode(copyModeButton.isSelected());
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        ResourceBundle bundle = ResourceBundle.getBundle("pokeditor.sheet_panel");
        toolBar1 = new JToolBar();
        saveSheetButton = new JButton();
        reloadSheetButton = new JButton();
        addRowButton = new JButton();
        deleteRowButton = new JButton();
        findButton = new JButton();
        copyModeButton = new JToggleButton();
        exportSheetButton = new JButton();
        importSheetButton = new JButton();
        hSpacer1 = new JPanel(null);
        zoomOutButton = new JButton();
        zoomInButton = new JButton();
        scrollPane1 = new JScrollPane();

        //======== this ========
        setLayout(new MigLayout(
            "hidemode 3",
            // columns
            "[grow,fill]",
            // rows
            "[grow,fill]"));

        //======== toolBar1 ========
        {

            //---- saveSheetButton ----
            saveSheetButton.setText(bundle.getString("DefaultSheetPanel.saveSheetButton.text"));
            saveSheetButton.addActionListener(e -> saveSheetButtonPressed(e));
            toolBar1.add(saveSheetButton);
            toolBar1.addSeparator();

            //---- reloadSheetButton ----
            reloadSheetButton.setText(bundle.getString("DefaultSheetPanel.reloadSheetButton.text"));
            reloadSheetButton.addActionListener(e -> reloadSheetButtonPressed(e));
            toolBar1.add(reloadSheetButton);
            toolBar1.addSeparator();

            //---- addRowButton ----
            addRowButton.setText(bundle.getString("DefaultSheetPanel.addRowButton.text"));
            addRowButton.addActionListener(e -> addRowButtonPressed(e));
            toolBar1.add(addRowButton);
            toolBar1.addSeparator();

            //---- deleteRowButton ----
            deleteRowButton.setText(bundle.getString("DefaultSheetPanel.deleteRowButton.text"));
            deleteRowButton.addActionListener(e -> deleteRowButtonPressed(e));
            toolBar1.add(deleteRowButton);
            toolBar1.addSeparator();

            //---- findButton ----
            findButton.setText(bundle.getString("DefaultSheetPanel.findButton.text"));
            findButton.addActionListener(e -> findButtonPressed(e));
            toolBar1.add(findButton);
            toolBar1.addSeparator();

            //---- copyModeButton ----
            copyModeButton.setText(bundle.getString("DefaultSheetPanel.copyModeButton.text"));
            copyModeButton.addActionListener(e -> copyModeButtonPressed(e));
            toolBar1.add(copyModeButton);
            toolBar1.addSeparator();

            //---- exportSheetButton ----
            exportSheetButton.setText(bundle.getString("DefaultSheetPanel.exportSheetButton.text"));
            exportSheetButton.addActionListener(e -> exportSheetButtonPressed(e));
            toolBar1.add(exportSheetButton);
            toolBar1.addSeparator();

            //---- importSheetButton ----
            importSheetButton.setText(bundle.getString("DefaultSheetPanel.importSheetButton.text"));
            importSheetButton.addActionListener(e -> importSheetButtonPressed(e));
            toolBar1.add(importSheetButton);
            toolBar1.addSeparator();
            toolBar1.add(hSpacer1);

            //---- zoomOutButton ----
            zoomOutButton.setIcon(UIManager.getIcon("InternalFrame.minimizeIcon"));
            zoomOutButton.addActionListener(e -> zoomOutButtonPressed(e));
            toolBar1.add(zoomOutButton);

            //---- zoomInButton ----
            zoomInButton.setIcon(UIManager.getIcon("InternalFrame.maximizeIcon"));
            zoomInButton.addActionListener(e -> zoomInButtonPressed(e));
            toolBar1.add(zoomInButton);
        }
        add(toolBar1, "north");

        //======== scrollPane1 ========
        {
            scrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        }
        add(scrollPane1, "cell 0 0,grow");
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JToolBar toolBar1;
    private JButton saveSheetButton;
    private JButton reloadSheetButton;
    private JButton addRowButton;
    private JButton deleteRowButton;
    private JButton findButton;
    private JToggleButton copyModeButton;
    private JButton exportSheetButton;
    private JButton importSheetButton;
    private JPanel hSpacer1;
    private JButton zoomOutButton;
    private JButton zoomInButton;
    private JScrollPane scrollPane1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
