package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gui.editors.data.EditorDataModel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ResourceBundle;

public abstract class FormatModel<G extends GenericFileData, E extends Enum<E>> extends AbstractTableModel implements EditorDataModel<E>
{
    private final List<G> data;
    private final List<TextBankData> textBankData;
    private final String[] columnNames;

    private boolean copyPasteModeEnabled;

    public static final int[] DEFAULT_VALUE_RANGE = new int[] {0, 255};

    public FormatModel(List<G> data, List<TextBankData> textBankData)
    {
        this.data = data;
        this.textBankData = textBankData;
        this.columnNames = new String[getColumnCount() + getNumFrozenColumns()];

        ResourceBundle bundle = ResourceBundle.getBundle(DataManager.SHEET_STRINGS_PATH);

        int lastValid = 0;
        for (int idx = 0; idx < columnNames.length; idx++)
        {
            int adjusted = idx-getNumFrozenColumns();
            String columnNameKey = getColumnNameKey(adjusted);
            if (columnNameKey == null)
            {
                columnNames[idx] = bundle.getString(getColumnNameKey(adjusted % (lastValid + 1))); // this will cause columns to repeat as much as needed for the sheets which need them
            }
            else {
                columnNames[idx] = bundle.getString(columnNameKey);
                lastValid = adjusted;
            }

        }

        copyPasteModeEnabled = false;
    }

    public int getNumFrozenColumns() {
        return 0;
    }

    public abstract String getColumnNameKey(int columnIndex);

    @Override
    public String getColumnName(int column)
    {
        return columnNames[column + getNumFrozenColumns()];
    }

    public void toggleCopyPasteMode(boolean state)
    {
        copyPasteModeEnabled = state;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return !copyPasteModeEnabled;
    }

    @Override
    public int getRowCount()
    {
        return getEntryCount();
    }

    @Override
    public int getEntryCount()
    {
        return data.size();
    }

    @Override
    public String getEntryName(int entryIdx)
    {
        return "" + entryIdx;
    }

    public List<G> getData()
    {
        return data;
    }

    public List<TextBankData> getTextBankData()
    {
        return textBankData;
    }

    protected CellTypes getCellType(int columnIndex)
    {
        return CellTypes.STRING;
    }

    /**
     * The inclusive {min, max} range which the provided column's underlying storage can hold.
     * Sheets which have columns wider (or narrower, or signed) than a single unsigned byte
     * override this so the cell editors can reject out-of-range input instead of silently
     * truncating it when the data is written back out to the ROM.
     * @param columnIndex the column index, using the same numbering as {@link #getCellType(int)}
     * @return an array of length 2, {minimum, maximum}
     */
    public int[] getCellValueRange(int columnIndex)
    {
        return DEFAULT_VALUE_RANGE;
    }

    /**
     * The text bank which backs the frozen "Name" column of this sheet, if it has one.
     * Rows added to or removed from a sheet have to be mirrored into it, otherwise every
     * name below the affected row labels the wrong entry.
     * @return the name bank, or null if this sheet has no parallel name bank
     */
    public TextBankData getNameTextBank()
    {
        return null;
    }

    public abstract FormatModel<G, E> getFrozenColumnModel();

    public Object prepareObjectForWriting(Object aValue, CellTypes cellType)
    {
        return prepareObjectForWriting(aValue, cellType, null);
    }

    /**
     * Converts an incoming cell value to the type the column stores, and refuses it if the
     * column cannot hold it.
     * <p>
     * The range check has to live here rather than in the cell editor. {@link
     * io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor}
     * was the only thing consulting {@link #getCellValueRange(int)}, and it is reached only
     * by typing into an {@link CellTypes#INTEGER} cell - a paste goes straight to
     * {@code setValueAt}, and combo box columns never had an editor-side check at all. So a
     * pasted value simply landed in the data and was narrowed to something else when the
     * file was written.
     *
     * @param valueRange the inclusive {min, max} this column can store, or null to skip the
     *                   check (for columns where no numeric range applies)
     */
    public Object prepareObjectForWriting(Object aValue, CellTypes cellType, int[] valueRange)
    {
        if (aValue instanceof String text)
        {
            text = text.trim();

            if (cellType == CellTypes.CHECKBOX)
            {
                // Boolean.parseBoolean answers false for everything it does not recognise, so
                // pasting a spreadsheet column of 1s and 0s silently cleared every checkbox
                aValue = parseCheckbox(text);
            }
            else if (cellType != CellTypes.STRING)
            {
                try {
                    aValue = Integer.parseInt(text);
                }
                catch (NumberFormatException e) {
                    // the sheet exports rendered text - names, not indices - so pasting an
                    // exported column back in lands here for every combo box cell. say what
                    // the column actually wants rather than repeating parseInt's message
                    throw new IllegalArgumentException(String.format(
                            "\"%s\" is not a number. This column stores a number%s, so a name "
                                    + "cannot be pasted into it - use the number that names it.",
                            text, valueRange == null ? ""
                                    : " between " + valueRange[0] + " and " + valueRange[1]), e);
                }
            }
        }

        if (valueRange != null && aValue instanceof Integer value
                && (value < valueRange[0] || value > valueRange[1]))
        {
            throw new IllegalArgumentException(String.format(
                    "%d is outside the range %d to %d that this column can store. Saving it "
                            + "would write a different value than the one entered.",
                    value, valueRange[0], valueRange[1]));
        }

        return aValue;
    }

    /**
     * Accepts the spellings a checkbox column can actually receive - the true/false a cell
     * editor produces and the 1/0 a spreadsheet paste produces - and refuses anything else
     * rather than quietly reading it as false.
     */
    private static boolean parseCheckbox(String text)
    {
        switch (text.toLowerCase())
        {
            case "true": case "1": case "yes": case "y":
                return true;
            case "false": case "0": case "no": case "n": case "":
                return false;
            default:
                throw new IllegalArgumentException(String.format(
                        "\"%s\" is not a yes or no value. This column is a checkbox; it accepts "
                                + "true/false or 1/0.", text));
        }
    }
}
