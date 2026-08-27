package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import com.formdev.flatlaf.util.SystemInfo;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.TableCellComponents;
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

        PasteAction<G, E> action = new PasteAction<>(this);

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
        // the custom columns of a sheet all show the same kind of thing, so they share one
        // editor and one renderer. the pair is built once, from the first custom column's
        // text, because buildColumnTextSources only assigns the triple to that column -
        // asking for it again on a later column would hand TableCellComponents a null.
        TableCellComponents.Pair customPair = null;

        for (int i = 0; i < getColumnCount(); i++)
        {
            CellTypes c = cellTypes[i];
            TableColumn col = getColumnModel().getColumn(i);
            col.setWidth(widths[i]);
            col.setPreferredWidth(widths[i]);

            TableCellComponents.Pair pair;
            if (c == CellTypes.CUSTOM)
            {
                if (customPair == null)
                {
                    customPair = TableCellComponents.forType(c, textSources.get(i),
                            getFormatModel().getCellValueRange(i), customCellSupplier);
                }
                pair = customPair;
            }
            else {
                pair = TableCellComponents.forType(c, textSources.get(i),
                        getFormatModel().getCellValueRange(i), customCellSupplier);
            }

            if (pair.renderer() != null)
                col.setCellRenderer(pair.renderer());

            if (pair.editor() != null)
                col.setCellEditor(pair.editor());
        }
    }

    private String[] getTextFromSource(Queue<String[]> textSources)
    {
        if (textSources.isEmpty())
        {
            // remove() would throw NoSuchElementException with a null message, naming neither
            // the sheet nor the column that went unserved
            throw new IllegalStateException(String.format(
                    "%s supplied fewer text sources than its columns need. Every combo box column "
                            + "takes one list and a custom column takes three, so obtainTextSources "
                            + "must return that many.", getClass().getSimpleName()));
        }

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
        // bounded by the column count, not by the width of row 0 - an empty sheet has no row 0,
        // and asking for its width threw rather than returning the empty export it should
        for (int colIdx = 0; colIdx < getColumnCount(); colIdx++)
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

    public static class PasteAction<G extends GenericFileData, E extends Enum<E>> extends AbstractAction {

        /** how many rejected cells to name individually before falling back to a count */
        private static final int MAX_REPORTED_REJECTIONS = 8;

        /**
         * An empty cell in a pasted block means "there is nothing here", not "write nothing here".
         * A spreadsheet produces them for genuinely blank cells and for the ragged right-hand end
         * of a copied range, and no numeric, checkbox or combo box column has a value that spelling
         * denotes - parsing it either throws or, for a checkbox, silently clears the flag.
         * <p>
         * Text columns are the exception: clearing a name is a real edit, so an empty string is a
         * value there and is written through.
         */
        private static boolean isBlankCell(String value, CellTypes cellType)
        {
            return value != null && value.isEmpty() && cellType != CellTypes.STRING;
        }

        private final DefaultTable<G, E> table;

        public PasteAction(DefaultTable<G, E> table)
        {
            this.table = table;
        }

        /**
         * Reports a paste failure to the user, or to the console when there is no display.
         * Calling JOptionPane headlessly throws HeadlessException from inside the error path,
         * which replaces the problem being reported with a different one - and makes the paste
         * path untestable, which is why this defect reached a release in the first place.
         */
        private static void report(Component parent, String message)
        {
            if (GraphicsEnvironment.isHeadless())
            {
                System.err.println(message);
                return;
            }
            JOptionPane.showMessageDialog(parent, message, "Paste Error", JOptionPane.ERROR_MESSAGE);
        }

        /**
         * The clipboard this action pastes from.
         * <p>
         * Reached through a method rather than called inline so that a test can supply its own.
         * The system clipboard needs a display, so without this seam the paste path can only be
         * exercised by reflectively replacing the AWT toolkit - which is how a regression that
         * refused every spreadsheet paste came to ship untested.
         *
         * @return the clipboard to read, or null if there is none to read
         */
        protected Clipboard getClipboard()
        {
            try {
                return Toolkit.getDefaultToolkit().getSystemClipboard();
            }
            catch (HeadlessException | SecurityException e) {
                // no display, or a sandbox that will not hand one over. nothing to paste from.
                return null;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int[] rows = table.getSelectedRows();
            System.out.println("rows: " + Arrays.toString(rows));
            int[] cols = table.getSelectedColumns();

            Clipboard cb = getClipboard();
            if (cb == null)
                return;

            if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor))
            {
                try
                {
                    String value = (String) cb.getData(DataFlavor.stringFlavor);
                    String[] lines = value.split("\r?\n", -1);

                    // Every spreadsheet terminates the last row of a copied range with a newline,
                    // so splitting with -1 leaves a trailing empty line. Counting it as a row of
                    // data makes a 2x2 copy look like three rows: it inflates numVerticalCopies
                    // below, and its single empty cell is not a value any numeric column can take.
                    int lineCount = lines.length;
                    if (lineCount > 1 && lines[lineCount - 1].isEmpty())
                        lineCount--;

                    String[][] pastedCells = new String[lineCount][];
                    for (int idx = 0; idx < lineCount; idx++) {
                        pastedCells[idx] = lines[idx].split("\t", -1);
                    }

                    if (rows.length == 0 || cols.length == 0 || pastedCells.length == 0)
                        return;

                    // integer floor, never zero - selecting a single cell and pasting several
                    // rows has to paste all of them, and selecting more rows than were copied
                    // must never write past the bottom of the selection
                    int numVerticalCopies = Math.max(1, rows.length / pastedCells.length);
                    int numHorizontalCopies = Math.max(1, cols.length / pastedCells[0].length);

                    // Check the whole rectangle before writing any of it. The write loop below
                    // cannot roll back - there is no undo - so a value rejected part way through
                    // would leave the sheet holding some of the paste and not the rest, with no
                    // way to tell which. prepareObjectForWriting only converts and validates, so
                    // running it here costs nothing and changes nothing.
                    List<String> rejections = new ArrayList<>();
                    FormatModel<G, E> model = table.getFormatModel();
                    for (int verticalCopyIdx = 0; verticalCopyIdx < numVerticalCopies; verticalCopyIdx++)
                    {
                        for (int horizontalCopyIdx = 0; horizontalCopyIdx < numHorizontalCopies; horizontalCopyIdx++)
                        {
                            for (int rowIdx = 0; rowIdx < pastedCells.length; rowIdx++)
                            {
                                for (int colIdx = 0; colIdx < pastedCells[rowIdx].length; colIdx++)
                                {
                                    int checkRow = rows[0] + verticalCopyIdx * pastedCells.length + rowIdx;
                                    int checkCol = cols[0] + horizontalCopyIdx * pastedCells[0].length + colIdx;

                                    if (checkRow >= table.getRowCount() || checkCol >= table.getColumnCount())
                                        continue;

                                    if (isBlankCell(pastedCells[rowIdx][colIdx], table.cellTypes[checkCol]))
                                        continue;

                                    try {
                                        model.prepareObjectForWriting(pastedCells[rowIdx][colIdx],
                                                table.cellTypes[checkCol], model.getCellValueRange(checkCol));
                                    }
                                    catch (RuntimeException ex) {
                                        if (rejections.size() < MAX_REPORTED_REJECTIONS)
                                        {
                                            rejections.add(String.format("row %d, \"%s\": %s",
                                                    checkRow, table.getColumnName(checkCol), ex.getMessage()));
                                        }
                                        else {
                                            rejections.add(null); // counted, not listed
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!rejections.isEmpty())
                    {
                        StringBuilder message = new StringBuilder(rejections.size() == 1
                                ? "This value can't be pasted, so nothing was changed:\n\n"
                                : rejections.size() + " values can't be pasted, so nothing was changed:\n\n");
                        int listed = 0;
                        for (String rejection : rejections)
                        {
                            if (rejection == null)
                                continue;
                            message.append("  \u2022 ").append(rejection).append('\n');
                            listed++;
                        }
                        if (rejections.size() > listed)
                            message.append("  \u2022 ").append(rejections.size() - listed).append(" more\n");

                        report(table, message.toString());
                        return;
                    }

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

                                        // must skip exactly what the dry run skipped, or the two
                                        // passes disagree and a value reaches setValueAt unchecked
                                        if (isBlankCell(pastedCells[rowIdx][colIdx], table.cellTypes[destCol]))
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
                        // the dry run above passed, so reaching here means the write path rejected
                        // something the validation did not know about. name the cell rather than
                        // leaving the user to guess which of the pasted values was the problem.
                        ex.printStackTrace();
                        report(table, String.format(
                                "The paste stopped at row %d, column \"%s\":%n%s%n%n"
                                        + "Cells before this one have already been changed.",
                                destRow, table.getColumnName(destCol), ex.getMessage()));
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
