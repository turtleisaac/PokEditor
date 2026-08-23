package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.formdev.flatlaf.util.SystemInfo;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.BitfieldComboBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.CheckBoxEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.ComboBoxCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.renderers.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public abstract class DefaultTable<G extends GenericFileData, E extends Enum<E>> extends JTable
{
    final CellTypes[] cellTypes;
    private final int[] widths;
    private final List<TextBankData> textData;

    private final FormatModel<G, E> formatModel;

    private final CellTypes.CustomCellFunctionSupplier customCellSupplier;

    /**
     * while a paste is in progress the paste action reports failures itself (once, naming the
     * offending cell) rather than having {@link #setValueAt(Object, int, int)} raise a dialog
     * for every single rejected cell
     */
    private boolean pasteInProgress;

    public DefaultTable(FormatModel<G, E> model, List<TextBankData> textData, int[] widths, CellTypes.CustomCellFunctionSupplier customCellSupplier)
    {
        super(model);
        this.formatModel = model;

        cellTypes = new CellTypes[getColumnCount()];
        for (int i = 0; i < cellTypes.length; i++)
        {
            cellTypes[i] = model.getCellType(i);
        }
//        cellTypes = Arrays.copyOfRange(cellTypes, getNumFrozenColumns(), cellTypes.length);
        widths = Arrays.copyOfRange(widths, model.getNumFrozenColumns(), widths.length);

//        this.cellTypes = cellTypes;
        this.widths = widths;
        this.textData = textData;
        this.customCellSupplier = customCellSupplier;
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        setRowMargin(1);
        getColumnModel().setColumnMargin(1);
        setShowGrid(true);
        setGridColor(Color.black);
        setShowHorizontalLines(true);
        setShowVerticalLines(true);

//        setBackground(Color.WHITE);
//        setForeground(Color.black);

        loadCellRenderers(buildColumnTextSources());

        MultiLineTableHeaderRenderer renderer = new MultiLineTableHeaderRenderer();
        Enumeration<TableColumn> columns = getColumnModel().getColumns();
        while (columns.hasMoreElements())
        {
            columns.nextElement().setHeaderRenderer(renderer);
        }

        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        getTableHeader().setReorderingAllowed(false);
        setDragEnabled(false);

        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        PasteAction action = new PasteAction(this);

        KeyStroke stroke;
        if (!SystemInfo.isMacOS) {
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        } else {
            stroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.META_MASK, false);
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PASTE, 0), action);
        registerKeyboardAction(action, "Paste", stroke, JComponent.WHEN_FOCUSED);
//        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), new AbstractAction()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                System.out.println("moo");
//                editingCanceled(null);
//            }
//        });

//                inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");

//        addKeyListener(new KeyAdapter()
//        {
//            @Override
//            public void keyPressed(KeyEvent e)
//            {
////                super.keyPressed(e);
//                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
//                    clearSelection();
//            }
//        });
        setDefaultRenderer(Object.class, new DefaultSheetCellRenderer());
    }

    public abstract Queue<String[]> obtainTextSources(List<TextBankData> textData);

    public FormatModel<G, E> getFormatModel()
    {
        return formatModel;
    }

    public abstract Class<G> getDataClass();

//    public abstract int getNumFrozenColumns();

    /**
     * Walks the columns of this table once and maps each column which needs externally
     * supplied text to the {@code String[]}s it needs, consuming the positional queue
     * returned by {@link #obtainTextSources(List)} exactly once.
     * <p>
     * Keying by column index (rather than having every consumer re-walk the queue in the
     * same order) is what stops {@link #loadCellRenderers(Map)} and
     * {@link #resetIndexedCellRendererText()} from drifting apart and handing a column the
     * wrong list of names.
     * @return a map of column index to the text sources that column's editor/renderer needs
     */
    private Map<Integer, String[][]> buildColumnTextSources()
    {
        Queue<String[]> textSources = obtainTextSources(textData);
        Map<Integer, String[][]> result = new HashMap<>();

        boolean customConsumed = false;
        for (int i = 0; i < getColumnCount(); i++)
        {
            CellTypes c = cellTypes[i];

            if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX || c == CellTypes.BITFIELD_COMBO_BOX)
            {
                result.put(i, new String[][] {getTextFromSource(textSources)});
            }
            else if (c == CellTypes.CUSTOM && !customConsumed)
            {
                customConsumed = true;
                String[] speciesNames = getTextFromSource(textSources);
                String[] itemNames = getTextFromSource(textSources);
                String[] moveNames = getTextFromSource(textSources);
                result.put(i, new String[][] {speciesNames, itemNames, moveNames});
            }
        }

        return result;
    }

    public void loadCellRenderers(Map<Integer, String[][]> textSources)
    {
        TableCellEditor customEditor = null;
        TableCellRenderer customRenderer = null;

        for (int i = 0; i < getColumnCount(); i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            if (c == CellTypes.CHECKBOX)
            {
                col.setCellRenderer(new CheckBoxRenderer());
                col.setCellEditor(new CheckBoxEditor());
            }
            else if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX || c == CellTypes.BITFIELD_COMBO_BOX)
            {
                String[] text = textSources.get(i)[0];

                if (c != CellTypes.BITFIELD_COMBO_BOX) //normal and colored
                    col.setCellEditor(new ComboBoxCellEditor(text));
                else // bitfield combo box
                    col.setCellEditor(new BitfieldComboBoxEditor(text));

                if (c == CellTypes.COMBO_BOX)
                    col.setCellRenderer(new IndexedStringCellRenderer(text));
                else if (c == CellTypes.COLORED_COMBO_BOX)
                    col.setCellRenderer(new IndexedStringCellRenderer.ColoredIndexedStringCellRenderer(text, PokeditorManager.typeColors));
                else
                    col.setCellRenderer(new BitfieldStringCellRenderer(text));
            }
            else if (c == CellTypes.INTEGER)
            {
                int[] range = getFormatModel().getCellValueRange(i);
                col.setCellEditor(new NumberOnlyCellEditor(range[0], range[1]));
            }
            else if (c == CellTypes.CUSTOM)
            {
                if (customEditor == null || customRenderer == null)
                {
                    String[][] custom = textSources.get(i);

                    customEditor = customCellSupplier.getEditor(custom[0], custom[1], custom[2]);
                    customRenderer = customCellSupplier.getRenderer(custom[0], custom[1], custom[2]);
                }

                if (customEditor != null)
                    col.setCellEditor(customEditor);

                if (customRenderer != null)
                    col.setCellRenderer(customRenderer);
            }
        }
    }

    private String[] getTextFromSource(Queue<String[]> textSources)
    {
        String[] text = textSources.remove();
        if (text == null)
            text = new String[] {""};

        return text;
    }

    public void resetIndexedCellRendererText()
    {
        Map<Integer, String[][]> textSources = buildColumnTextSources();
        for (int i = 0; i < getColumnCount(); i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);

            if (c == CellTypes.COMBO_BOX || c == CellTypes.COLORED_COMBO_BOX || c == CellTypes.BITFIELD_COMBO_BOX)
            {
                String[][] entry = textSources.get(i);
                String[] text = (entry == null || entry.length == 0 || entry[0] == null) ? new String[] {""} : entry[0];
                ((ComboBoxCellEditor) col.getCellEditor()).setItems(text);
                ((IndexedStringCellRenderer) col.getCellRenderer()).setItems(text);
            }
        }
    }

    /**
     * Marks the data backing this sheet as dirty and reports validation failures thrown by
     * the underlying data classes to the user instead of letting them escape onto the EDT
     * where a user running a double-clicked jar would never see them.
     */
    @Override
    public void setValueAt(Object aValue, int row, int column)
    {
        try {
            super.setValueAt(aValue, row, column);
            DataManager.markDirty(getDataClass());
        }
        catch (RuntimeException e) {
            if (pasteInProgress)
                throw e;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    String.format("The value \"%s\" could not be applied to row %d, column \"%s\":\n%s",
                            aValue, row, getColumnName(column), e.getMessage()),
                    "Invalid Value", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String[][] exportClean()
    {
        FormatModel<G, E> frozenModel = getFormatModel().getFrozenColumnModel();
        int frozenColumnCount = frozenModel == null ? 0 : frozenModel.getColumnCount();

        String[][] output = new String[getModel().getRowCount()][frozenColumnCount + getColumnCount()];

        // the frozen ID/Name columns live in their own model, so they have to be pulled in
        // explicitly - iterating getColumnModel() alone leaves them out of the export entirely
        for (int colIdx = 0; colIdx < frozenColumnCount; colIdx++)
        {
            for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
            {
                output[rowIdx][colIdx] = String.valueOf(frozenModel.getValueAt(rowIdx, colIdx));
            }
        }

        for (int colIdx = 0; colIdx < getColumnCount(); colIdx++)
        {
            TableColumn column = getColumnModel().getColumn(colIdx);
            TableCellRenderer renderer = column.getCellRenderer();
            if (renderer != null && !(renderer instanceof CheckBoxRenderer))
            {
                for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
                {
                    output[rowIdx][frozenColumnCount + colIdx] = ((DefaultTableCellRenderer) prepareRenderer(renderer, rowIdx, colIdx)).getText();
                }
            }
            else
            {
                for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
                {
                    output[rowIdx][frozenColumnCount + colIdx] = String.valueOf(getValueAt(rowIdx, colIdx));
                }
            }

        }

        return output;
    }

    public String[][] exportEditable()
    {
        String[][] output = new String[getModel().getRowCount()][getColumnCount()];
        for (int colIdx = 0; colIdx < output[0].length; colIdx++)
        {
            for (int rowIdx = 0; rowIdx < output.length; rowIdx++)
            {
                output[rowIdx][colIdx] = String.valueOf(getValueAt(rowIdx, colIdx));
            }
        }

        return output;
    }

    public static String[] loadStringsFromKeys(String... keys)
    {
        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);
        String[] result = new String[keys.length];
        int idx = 0;
        for (String s : keys) {
            result[idx++] = bundle.getString(s);
        }
        return result;
    }

    public static class PasteAction extends AbstractAction {

        private final DefaultTable<? extends GenericFileData, ? extends Enum<?>> table;

        public PasteAction(DefaultTable<? extends GenericFileData, ? extends Enum<?>> table)
        {
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int[] rows = table.getSelectedRows();
            System.out.println("rows: " + Arrays.toString(rows));
            int[] cols = table.getSelectedColumns();

            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor))
            {
                try
                {
                    String value = (String) cb.getData(DataFlavor.stringFlavor);
                    String[] lines = value.split("\r?\n", -1);
                    String[][] pastedCells = new String[lines.length][];
                    int idx = 0;
                    for (String line : lines) {
                        pastedCells[idx++] = line.split("\t", -1);
                    }

                    if (rows.length == 0 || cols.length == 0 || pastedCells.length == 0)
                        return;

                    // integer floor, never zero - selecting a single cell and pasting several
                    // rows has to paste all of them, and selecting more rows than were copied
                    // must never write past the bottom of the selection
                    int numVerticalCopies = Math.max(1, rows.length / pastedCells.length);
                    int numHorizontalCopies = Math.max(1, cols.length / pastedCells[0].length);

                    int destRow = -1;
                    int destCol = -1;
                    table.pasteInProgress = true;
                    try
                    {
                        for (int verticalCopyIdx = 0; verticalCopyIdx < numVerticalCopies; verticalCopyIdx++)
                        {
                            for (int horizontalCopyIdx = 0; horizontalCopyIdx < numHorizontalCopies; horizontalCopyIdx++)
                            {
                                for (int rowIdx = 0; rowIdx < pastedCells.length; rowIdx++)
                                {
                                    for (int colIdx = 0; colIdx < pastedCells[rowIdx].length; colIdx++)
                                    {
                                        destRow = rows[0] + verticalCopyIdx * pastedCells.length + rowIdx;
                                        destCol = cols[0] + horizontalCopyIdx * pastedCells[0].length + colIdx;

                                        if (destRow >= table.getRowCount() || destCol >= table.getColumnCount())
                                            continue;

                                        table.setValueAt(pastedCells[rowIdx][colIdx], destRow, destCol);
                                        table.getFormatModel().fireTableCellUpdated(destRow, destCol);
                                    }
                                }
                            }
                        }
                    }
                    catch (RuntimeException ex)
                    {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(table,
                                String.format("The paste could not be completed - the value destined for row %d, column %d was rejected:%n%s",
                                        destRow, destCol, ex.getMessage()),
                                "Paste Error", JOptionPane.ERROR_MESSAGE);
                    }
                    finally
                    {
                        table.pasteInProgress = false;
                    }

//                    table.setValueAt(value, row, col);
                }
                catch (UnsupportedFlavorException | IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }
}
