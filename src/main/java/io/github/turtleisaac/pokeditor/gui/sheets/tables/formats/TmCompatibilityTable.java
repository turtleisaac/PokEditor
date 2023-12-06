package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import com.formdev.flatlaf.extras.components.FlatPopupMenu;
import io.github.turtleisaac.pokeditor.DataManager;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.EditorComboBox;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class TmCompatibilityTable extends DefaultTable<PersonalData, TmCompatibilityTable.TmCompatibilityColumns>
{
    //todo 0xF0BFC is address of TMs table
    static final int[] columnWidths = new int[102];
    static {
        Arrays.fill(columnWidths, 120);
    }

    public TmCompatibilityTable(List<PersonalData> data, List<TextBankData> textData)
    {
        super(new TmCompatibilityModel(data, textData), textData, columnWidths, null);
        String[] moveNames = textData.get(TextFiles.MOVE_NAMES.getValue()).getStringList().toArray(String[]::new);

        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value != null)
                {
                    if (value instanceof Integer val)
                    {
                        if (val < moveNames.length)
                        {
                            this.setText(moveNames[val]);
                        }
                    }
                    else if (value instanceof String s)
                    {
                        int val = Integer.parseInt(s);
                        if (val < moveNames.length)
                        {
                            this.setText(moveNames[val]);
                        }
                    }
                }

                return this;
            }
        };

        Enumeration<TableColumn> columns = getColumnModel().getColumns();
        while (columns.hasMoreElements())
        {
            columns.nextElement().setHeaderRenderer(renderer);
        }
        setupEditableHeader();
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();
        return textSources;
    }

    @Override
    public Class<PersonalData> getDataClass()
    {
        return PersonalData.class;
    }

    public static class TmCompatibilityModel extends FormatModel<PersonalData, TmCompatibilityColumns>
    {

        public TmCompatibilityModel(List<PersonalData> data, List<TextBankData> textBankData)
        {
            super(data, textBankData);
        }

        @Override
        public int getNumFrozenColumns()
        {
            return 2;
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return TmCompatibilityColumns.getColumn(columnIndex).key;
        }

        @Override
        public String getColumnName(int column)
        {
            if (column < 0)
                return super.getColumnName(column);
            return "" + PersonalParser.tmMoveIdNumbers[column];
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0) {
                TmCompatibilityColumns c = TmCompatibilityColumns.getColumn(0);
                c.repetition = columnIndex;
                setValueFor(aValue, rowIndex, c);
            }
        }

        @Override
        public void setValueFor(Object aValue, int entryIdx, TmCompatibilityColumns property)
        {
            PersonalData entry = getData().get(entryIdx);

            if (property == TmCompatibilityColumns.TM)
            {
                aValue = prepareObjectForWriting(aValue, property.cellType);
                entry.getTmCompatibility()[property.repetition] = (boolean) aValue;
            }
        }

        @Override
        public int getColumnCount()
        {
            return 100;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0) {
                TmCompatibilityColumns c = TmCompatibilityColumns.getColumn(0);
                c.repetition = columnIndex;
                return getValueFor(rowIndex, c);
            }
            return getValueFor(rowIndex, TmCompatibilityColumns.getColumn(columnIndex));
        }

        @Override
        public Object getValueFor(int entryIdx, TmCompatibilityColumns property)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            PersonalData entry = getData().get(entryIdx);

            switch (property)
            {
                case ID -> {
                    return entryIdx;
                }
                case NAME -> {
                    if(entryIdx < speciesNames.size())
                        return speciesNames.get(entryIdx).getText();
                    else
                        return "";
                }
                case TM -> {
                    return entry.getTmCompatibility()[property.repetition];
                }
            }
            return null;
        }

        @Override
        protected CellTypes getCellType(int columnIndex)
        {
            return CellTypes.CHECKBOX;
        }

        @Override
        public FormatModel<PersonalData, TmCompatibilityColumns> getFrozenColumnModel()
        {
            return new TmCompatibilityModel(getData(), getTextBankData()) {
                @Override
                public int getColumnCount()
                {
                    return super.getNumFrozenColumns();
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    return super.getValueAt(rowIndex, columnIndex - super.getNumFrozenColumns());
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                {
                    super.setValueAt(aValue, rowIndex, columnIndex - super.getNumFrozenColumns());
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            };
        }
    }

    public enum TmCompatibilityColumns {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        TM(0, "name", CellTypes.CHECKBOX),
        NUMBER_OF_COLUMNS(1, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;
        int repetition;

        TmCompatibilityColumns(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static TmCompatibilityColumns getColumn(int idx)
        {
            for (TmCompatibilityColumns column : TmCompatibilityColumns.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }

    public void setupEditableHeader()
    {
        String[] moveNames = getFormatModel().getTextBankData().get(TextFiles.MOVE_NAMES.getValue()).getStringList().toArray(String[]::new);
        tableHeader.setEnabled(true);

        tableHeader.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent event)
            {
                if (event.getClickCount() == 2)
                {
                    int columnIndex = tableHeader.columnAtPoint(event.getPoint());

                    if (columnIndex >= 0)
                    {
                        TableColumn column = tableHeader.getColumnModel().getColumn(columnIndex);

                        Rectangle rect = tableHeader.getHeaderRect(columnIndex);

                        JPopupMenu popupMenu = new JPopupMenu();

                        DefaultListModel<EditorComboBox.ComboBoxItem> items = new DefaultListModel<>();
                        for (String moveName : moveNames) {
                            items.addElement(new EditorComboBox.ComboBoxItem(moveName));
                        }
                        JList<EditorComboBox.ComboBoxItem> list = new JList<>(items);
                        list.setSelectedIndex(PersonalParser.tmMoveIdNumbers[columnIndex]);

                        list.addListSelectionListener(e -> {
                            PersonalParser.updateTmType(columnIndex, list.getSelectedIndex(), DataManager.getData(null, MoveData.class));
                            column.setHeaderValue(list.getSelectedIndex());
                        });

                        popupMenu.setPreferredSize(
                                new Dimension(rect.width, rect.height * 5));

                        popupMenu.add(new JScrollPane(list));
                        list.scrollRectToVisible(list.getCellBounds(list.getSelectedIndex(), list.getSelectedIndex()));
                        popupMenu.show(tableHeader, rect.x, 0);
                    }
                }
            }
        });
    }
}
