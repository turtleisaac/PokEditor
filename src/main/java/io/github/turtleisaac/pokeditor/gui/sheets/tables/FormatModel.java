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

    public abstract FormatModel<G, E> getFrozenColumnModel();

    public Object prepareObjectForWriting(Object aValue, CellTypes cellType)
    {
        if (aValue instanceof String)
        {
            if (cellType == CellTypes.CHECKBOX)
                aValue = Boolean.parseBoolean(((String) aValue).trim());
            else if (cellType != CellTypes.STRING)
                aValue = Integer.parseInt(((String) aValue).trim());
        }
        return aValue;
    }
}
