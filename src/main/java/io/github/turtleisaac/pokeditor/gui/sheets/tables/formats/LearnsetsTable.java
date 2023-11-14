package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LearnsetsTable extends DefaultTable<LearnsetData, LearnsetsTable.LearnsetsColumn>
{
    public static final int[] columnWidths = new int[] {40, 100, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65};

    public LearnsetsTable(List<LearnsetData> data, List<TextBankData> textData)
    {
        super(new LearnsetsModel(data, textData), textData, columnWidths, null);
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();

        String[] moveNames = textData.get(TextFiles.MOVE_NAMES.getValue()).getStringList().toArray(String[]::new);

        for (int i = 0; i < 20; i++)
        {
            textSources.add(moveNames);
        }

        return textSources;
    }

    @Override
    public Class<LearnsetData> getDataClass()
    {
        return LearnsetData.class;
    }

    static class LearnsetsModel extends FormatModel<LearnsetData, LearnsetsColumn>
    {

        public LearnsetsModel(List<LearnsetData> data, List<TextBankData> textBankData)
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
            return LearnsetsColumn.getColumn(columnIndex).key;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0)
            {
                LearnsetsColumn c = LearnsetsColumn.getColumn(columnIndex % 2);
                c.repetition = columnIndex;
                setValueFor(aValue, rowIndex, c);
            }
            else
            {
                setValueFor(aValue, rowIndex, LearnsetsColumn.getColumn(columnIndex));
            }

        }

        @Override
        public void setValueFor(Object aValue, int rowIdx, LearnsetsColumn property)
        {
            LearnsetData learnset = getData().get(rowIdx);

            aValue = prepareObjectForWriting(aValue, property.cellType);

            if (property.idx >= 0)
            {
                int entryIdx = property.repetition / 2;
                while (entryIdx > learnset.size())
                {
                    learnset.add(new LearnsetData.LearnsetEntry());
                }
                LearnsetData.LearnsetEntry entry = learnset.get(entryIdx);

                switch (property) {
                    case MOVE -> entry.setMoveID((Integer) aValue);
                    case LEVEL -> entry.setLevel((Integer) aValue);
                }
            }
        }

        @Override
        public int getColumnCount()
        {
            return LearnsetData.MAX_NUM_ENTRIES * 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (columnIndex >= 0) {
                LearnsetsColumn c = LearnsetsColumn.getColumn(columnIndex % 2);
                c.repetition = columnIndex;
                return getValueFor(rowIndex, c);
            }
            return getValueFor(rowIndex, LearnsetsColumn.getColumn(columnIndex));
        }

        @Override
        public Object getValueFor(int rowIndex, LearnsetsColumn property)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            LearnsetData learnset = getData().get(rowIndex);

            if (property.idx >= 0)
            {
                int entryIdx = property.repetition / 2;
                while (entryIdx >= learnset.size())
                {
                    learnset.add(new LearnsetData.LearnsetEntry());
                }
                LearnsetData.LearnsetEntry entry = learnset.get(entryIdx);

                switch (property) {
                    case MOVE -> {
                        return entry.getMoveID();
                    }
                    case LEVEL -> {
                        return entry.getLevel();
                    }
                }
            }
            else if (property == LearnsetsColumn.ID)
            {
                return rowIndex;
            }
            else if (property == LearnsetsColumn.NAME)
            {
                if(rowIndex < speciesNames.size())
                    return speciesNames.get(rowIndex).getText();
                else
                    return "";
            }

            return null;
        }

        @Override
        protected CellTypes getCellType(int columnIndex)
        {
            if (columnIndex >= 0)
            {
                return LearnsetsColumn.getColumn(columnIndex % 2).cellType;
            }

            return LearnsetsColumn.getColumn(columnIndex).cellType;
        }

        @Override
        public FormatModel<LearnsetData, LearnsetsColumn> getFrozenColumnModel()
        {
            return new LearnsetsTable.LearnsetsModel(getData(), getTextBankData()) {
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

    enum LearnsetsColumn
    {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        MOVE(0, "move", CellTypes.COMBO_BOX),
        LEVEL(1, "level", CellTypes.INTEGER),
        NUMBER_OF_COLUMNS(2, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;
        int repetition;

        LearnsetsColumn(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static LearnsetsColumn getColumn(int idx)
        {
            for (LearnsetsColumn column : LearnsetsColumn.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }
}
