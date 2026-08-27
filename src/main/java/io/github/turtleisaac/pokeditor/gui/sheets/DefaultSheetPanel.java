/*
 * Created by JFormDesigner on Wed Dec 15 18:04:50 CST 2021
 */

package io.github.turtleisaac.pokeditor.gui.sheets;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import io.github.turtleisaac.nds4j.ui.ThemeUtils;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FrozenColumnTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.IndexedStringCellRenderer;
import net.miginfocom.swing.*;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author turtleisaac
 */
public class DefaultSheetPanel<G extends GenericFileData, E extends Enum<E>> extends JPanel
{
    private Dimension lastSize;

    private final PokeditorManager manager;

    private final DefaultTable<G, E> table;
    private final FrozenColumnTable<G> frozenColumns;

    public DefaultSheetPanel(PokeditorManager manager, DefaultTable<G, E> table) {
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

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        frozenColumns.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        table.getSelectionModel().addListSelectionListener(e -> frozenColumns.clearSelection());
        frozenColumns.getSelectionModel().addListSelectionListener(e -> table.clearSelection());

//        linkTableSelectionIndicators(table, frozenColumns);
//        linkTableSelectionIndicators(frozenColumns, table);

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

    public DefaultTable<G, E> getTable()
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

    /**
     * Terminates any in-progress cell edit on both tables so the value the user just typed is
     * committed to the model before it is read. Without this, typing a value and clicking Save
     * writes the OLD value while the editor still displays the new one.
     * @return false if an editor refused to stop (i.e. the typed value failed validation)
     */
    private boolean stopEditing()
    {
        boolean stopped = true;

        if (table.isEditing())
            stopped = table.getCellEditor().stopCellEditing();

        if (frozenColumns.isEditing())
            stopped &= frozenColumns.getCellEditor().stopCellEditing();

        return stopped;
    }

    /**
     * @return the selected row, resolved from whichever of the two tables actually has a
     * selection (they clear each other's), or -1 if neither does
     */
    private int getSelectedModelRow()
    {
        int row = table.getSelectedRow();
        if (row < 0)
            row = frozenColumns.getSelectedRow();
        return row;
    }

    private void addRowButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        List<GenericFileData> data = (List<GenericFileData>) table.getFormatModel().getData();
        if (data.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "This sheet has no rows to base a new row on.", "PokEditor", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GenericFileData v;
        try
        {
            v = data.get(0).getClass().getDeclaredConstructor().newInstance();
        }
        catch (NoSuchMethodException ex) {
            /*
                TODO: EvolutionData and LearnsetData (both owned by PokEditor-Core) declare only
                 a (BytesDataContainer) constructor, and GenericParser exposes no "create blank
                 entry" factory, so there is no way to build a blank row for those sheets from
                 here. Once Core gains a public no-arg constructor (or the parser gains a
                 factory method), route creation through it and drop this branch.
             */
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Rows cannot be added to this sheet yet - the underlying data format (" + data.get(0).getClass().getSimpleName() + ")\ndoes not provide a way to create a blank entry.",
                    "Unsupported", JOptionPane.ERROR_MESSAGE);
            return;
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "A new row could not be created:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int newRow = data.size();
        data.add(v);

        // keep the parallel name bank the same length, otherwise typing a name into the new
        // row throws
        TextBankData nameBank = table.getFormatModel().getNameTextBank();
        if (nameBank != null)
        {
            while (nameBank.size() <= newRow)
                nameBank.add(new TextBankData.Message(""));
        }

        table.getFormatModel().fireTableRowsInserted(newRow, newRow);
        if (frozenColumns.getModel() instanceof AbstractTableModel frozenModel)
            frozenModel.fireTableRowsInserted(newRow, newRow);

        // other open sheets read the same name bank and have just had it grow underneath them
        manager.nameBankRowsChanged(nameBank, this);
        manager.markSheetDirty(table.getDataClass());
    }

    private void deleteRowButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        int row = getSelectedModelRow();
        if (row < 0)
        {
            JOptionPane.showMessageDialog(this, "Select the row you would like to delete first.", "PokEditor", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<?> data = table.getFormatModel().getData();
        if (row >= data.size())
            return;

        data.remove(row);

        // the name bank has to be shifted along with the data, otherwise every name below the
        // deleted row labels the wrong entry
        TextBankData nameBank = table.getFormatModel().getNameTextBank();
        if (nameBank != null && row < nameBank.size())
            nameBank.remove(row);

        table.getFormatModel().fireTableRowsDeleted(row, row);
        if (frozenColumns.getModel() instanceof AbstractTableModel frozenModel)
            frozenModel.fireTableRowsDeleted(row, row);

        // Personal, TM compatibility, Evolutions and Learnsets all read the same species name
        // bank across three different data classes, so the sheets that are not being edited
        // have just had every name below this row shifted under them.
        manager.nameBankRowsChanged(nameBank, this);
        manager.markSheetDirty(table.getDataClass());
    }

    private void exportSheetButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        manager.writeSheet(table.exportClean());
    }

    private void importSheetButtonPressed(ActionEvent e) {
        // TODO add your code here
        JOptionPane.showMessageDialog(this, "Lol you wish", "Nope", JOptionPane.ERROR_MESSAGE);
    }

    private void saveSheetButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        manager.saveData(table.getDataClass());
    }

    private void reloadSheetButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        manager.resetData(table.getDataClass());
        table.getFormatModel().fireTableDataChanged();
    }

    private void findButtonPressed(ActionEvent e) {
        if (!stopEditing())
            return;

        new FindDialog(this).setVisible(true);
    }

    /**
     * Moves the selection to (and scrolls to) the provided cell. Used by {@link FindDialog}.
     */
    public void selectCell(int row, int column)
    {
        table.clearSelection();
        frozenColumns.clearSelection();

        if (column < 0) // a hit in one of the frozen ID/Name columns
        {
            frozenColumns.setRowSelectionInterval(row, row);
            frozenColumns.scrollRectToVisible(frozenColumns.getCellRect(row, 0, true));
            return;
        }

        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
        table.scrollRectToVisible(table.getCellRect(row, column, true));
    }

    public FrozenColumnTable<G> getFrozenColumns()
    {
        return frozenColumns;
    }

    /**
     * @return how many frozen (ID/Name) columns precede column 0 of the main table
     */
    public int getFrozenColumnCount()
    {
        return frozenColumns.getColumnCount();
    }

    /**
     * The text a search should match against for the given cell - the text the user can
     * actually see, so searching for a move or species name works.
     * @param column the main table's column index, or a negative index into the frozen columns
     */
    private String getSearchableText(int row, int column)
    {
        if (column < 0)
            return String.valueOf(frozenColumns.getValueAt(row, column + getFrozenColumnCount()));

        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Component component = table.prepareRenderer(renderer, row, column);
        if (component instanceof JLabel label)
            return String.valueOf(label.getText());

        return String.valueOf(table.getValueAt(row, column));
    }

    /**
     * Searches the sheet for the provided text, starting just after the provided cell and
     * wrapping around. There is no RowSorter in this project, so view and model coordinates
     * are the same.
     * @param fromColumn the column of the previous hit (may be negative for a frozen column);
     *                   pass {@code -getFrozenColumnCount() - 1} to start at the very beginning
     * @return {row, column} of the next match, or null if there is none
     */
    public int[] findNext(String query, boolean matchCase, boolean matchEntireContents, int fromRow, int fromColumn)
    {
        int frozenCount = getFrozenColumnCount();
        int rowCount = table.getRowCount();
        int columnCount = table.getColumnCount();
        int totalColumns = frozenCount + columnCount;

        if (query == null || query.isEmpty() || rowCount == 0 || totalColumns == 0)
            return null;

        String needle = matchCase ? query : query.toLowerCase(Locale.ROOT);
        int totalCells = rowCount * totalColumns;

        int startIdx = Math.max(0, fromRow) * totalColumns + (fromColumn + frozenCount) + 1;

        for (int offset = 0; offset < totalCells; offset++)
        {
            int idx = Math.floorMod(startIdx + offset, totalCells);
            int row = idx / totalColumns;
            int column = (idx % totalColumns) - frozenCount;

            String cell = getSearchableText(row, column);
            if (cell == null)
                continue;

            String haystack = matchCase ? cell : cell.toLowerCase(Locale.ROOT);

            if (matchEntireContents ? haystack.equals(needle) : haystack.contains(needle))
                return new int[] {row, column};
        }

        return null;
    }

    private void copyModeButtonPressed(ActionEvent e) {
        // TODO add your code here
        table.getFormatModel().toggleCopyPasteMode(copyModeButton.isSelected());
    }

    public void thing(TableCellEditor editor)
    {

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
            "insets 0,hidemode 3",
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
