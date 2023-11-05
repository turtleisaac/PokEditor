package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class LearnsetsTable extends DefaultTable<LearnsetData>
{
    private static final int NUM_FROZEN_COLUMNS = 2;
    public static final int[] columnWidths = new int[] {40, 100, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65, 140, 65};

    public LearnsetsTable(List<LearnsetData> data, List<TextBankData> textData)
    {
        super(LearnsetsModel.learnsetsClasses, new LearnsetsModel(data, textData), textData, columnWidths, null);
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

    @Override
    public int getNumFrozenColumns()
    {
        return NUM_FROZEN_COLUMNS;
    }

    static class LearnsetsModel extends FormatModel<LearnsetData>
    {
        static final CellTypes[] learnsetsClasses = new CellTypes[] {CellTypes.INTEGER, CellTypes.STRING, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER};
        private static final String[] columnKeys = new String[] {"id", "name", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level", "move", "level"};

        public LearnsetsModel(List<LearnsetData> data, List<TextBankData> textBankData)
        {
            super(columnKeys, data, textBankData, NUM_FROZEN_COLUMNS);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            LearnsetData learnset = getData().get(rowIndex);

            if (aValue instanceof String)
            {
                CellTypes cellType = learnsetsClasses[columnIndex + NUM_FROZEN_COLUMNS];
                if (cellType == CellTypes.CHECKBOX)
                    aValue = Boolean.parseBoolean(((String) aValue).trim());
                else if (cellType != CellTypes.STRING)
                    aValue = Integer.parseInt(((String) aValue).trim());
            }

            if (columnIndex >= 0)
            {
                int entryIdx = columnIndex / 2;
                while (entryIdx > learnset.size())
                {
                    learnset.add(new LearnsetData.LearnsetEntry());
                }
                LearnsetData.LearnsetEntry entry = learnset.get(entryIdx);

                switch (columnIndex % 2) {
                    case 0 -> entry.setMoveID((Integer) aValue);
                    case 1 -> entry.setLevel((Integer) aValue);
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            LearnsetData learnset = getData().get(rowIndex);

            if (columnIndex >= 0)
            {
                int entryIdx = columnIndex / 2;
                while (entryIdx >= learnset.size())
                {
                    learnset.add(new LearnsetData.LearnsetEntry());
                }
                LearnsetData.LearnsetEntry entry = learnset.get(entryIdx);

                switch (columnIndex % 2) {
                    case 0 -> {
                        return entry.getMoveID();
                    }
                    case 1 -> {
                        return entry.getLevel();
                    }
                }
            }
            else if (columnIndex == -2)
            {
                return rowIndex;
            }
            else if (columnIndex == -1)
            {
                if(rowIndex < speciesNames.size())
                    return speciesNames.get(rowIndex).getText();
                else
                    return "";
            }

            return null;
        }

        @Override
        public FormatModel<LearnsetData> getFrozenColumnModel()
        {
            return new LearnsetsTable.LearnsetsModel(getData(), getTextBankData()) {
                @Override
                public int getColumnCount()
                {
                    return NUM_FROZEN_COLUMNS;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    return super.getValueAt(rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                {
                    super.setValueAt(aValue, rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return false;
                }
            };
        }
    }
}
