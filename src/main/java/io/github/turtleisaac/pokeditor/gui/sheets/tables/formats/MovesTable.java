package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MovesTable extends DefaultTable<MoveData>
{
    private static final int NUM_FROZEN_COLUMNS = 2;
    public static final int[] columnWidths = new int[] {40, 100, 500, 100, 65, 100, 65, 65, 120, 160, 65, 65, 80, 80, 80, 80, 80, 80, 80, 80, 65, 65};
    private static final String[] categoryKeys = new String[] {"category.physical", "category.special", "category.status"};
    private static final String[] targetKeys = new String[] {"target.selected", "target.automatic", "target.random", "target.bothFoes", "target.allExceptUser", "target.user", "target.userSide", "target.entireField", "target.foeSide", "target.ally", "target.userOrAlly", "target.MOVE_TARGET_ME_FIRST"};

    public MovesTable(List<MoveData> data, List<TextBankData> textData)
    {
        super(MovesModel.movesClasses, new MovesModel(data, textData), textData, columnWidths, null);
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();

        String[] categories = DefaultTable.loadStringsFromKeys(categoryKeys);

        String[] typeNames = textData.get(TextFiles.TYPE_NAMES.getValue()).getStringList().toArray(String[]::new);

        String[] targets = DefaultTable.loadStringsFromKeys(targetKeys);

//        String[] arr = new String[500];
//        Arrays.fill(arr, "Moo");

        textSources.add(categories);
        textSources.add(typeNames);
        textSources.add(targets);
//        textSources.add(arr);

        return textSources;
    }

    @Override
    public Class<MoveData> getDataClass()
    {
        return MoveData.class;
    }

    @Override
    public int getNumFrozenColumns()
    {
        return NUM_FROZEN_COLUMNS;
    }

    static class MovesModel extends FormatModel<MoveData>
    {
        static final CellTypes[] movesClasses = new CellTypes[] {CellTypes.INTEGER, CellTypes.STRING, /*CellTypes.COMBO_BOX*/ CellTypes.INTEGER, CellTypes.COMBO_BOX, CellTypes.INTEGER, CellTypes.COLORED_COMBO_BOX, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.INTEGER, CellTypes.BITFIELD_COMBO_BOX, CellTypes.INTEGER, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.CHECKBOX, CellTypes.INTEGER, CellTypes.INTEGER};
        private static final String[] columnKeys = new String[] {"id", "name", "moves.effect", "category", "power", "type", "accuracy", "pp", "moves.effectChance", "target", "priority", "moves.makesContact", "moves.blockedByProtect", "moves.reflectedByMagicCoat", "moves.affectedBySnatch", "moves.affectedByMirrorMove", "moves.triggersKingsRock", "moves.hidesHpBars", "moves.removeTargetShadow", "moves.contestEffect", "moves.contestType"};

        public MovesModel(List<MoveData> data, List<TextBankData> textBankData)
        {
            super(columnKeys, data, textBankData, NUM_FROZEN_COLUMNS);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            MoveData entry = getData().get(rowIndex);
            TextBankData moveNames = getTextBankData().get(TextFiles.MOVE_NAMES.getValue());

            if (aValue instanceof String)
            {
                CellTypes cellType = movesClasses[columnIndex + NUM_FROZEN_COLUMNS];
                if (cellType == CellTypes.CHECKBOX)
                    aValue = Boolean.parseBoolean(((String) aValue).trim());
                else if (cellType != CellTypes.STRING)
                    aValue = Integer.parseInt(((String) aValue).trim());
            }


            switch (columnIndex + NUM_FROZEN_COLUMNS) {
                case 0 -> {}
                case 1 -> {
                    TextBankData.Message message = moveNames.get(rowIndex);
                    message.setText((String) aValue);
                }
                case 2 -> entry.setEffect((Integer) aValue);
                case 3 -> entry.setCategory((Integer) aValue);
                case 4 -> entry.setPower((Integer) aValue);
                case 5 -> entry.setType((Integer) aValue);
                case 6 -> entry.setAccuracy((Integer) aValue);
                case 7 -> entry.setPp((Integer) aValue);
                case 8 -> entry.setEffectChance((Integer) aValue);
                case 9 -> {
                    entry.setTarget((Integer) aValue);
                }
                case 10 -> entry.setPriority((Integer) aValue);
                case 11, 12, 13, 14, 15, 16, 17, 18 -> entry.getFlags()[columnIndex - 11 + NUM_FROZEN_COLUMNS] = (Boolean) aValue;
                case 19 -> entry.setContestEffect((Integer) aValue);
                case 20 -> entry.setContestType((Integer) aValue);
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData moveNames = getTextBankData().get(TextFiles.MOVE_NAMES.getValue());
            MoveData entry = getData().get(rowIndex);

            switch(columnIndex + NUM_FROZEN_COLUMNS) {
                case 0 -> {
                    return rowIndex;
                }
                case 1 -> {
                    if(rowIndex < moveNames.size())
                        return moveNames.get(rowIndex).getText();
                    else
                        return "";
                }
                case 2 -> {
                    return entry.getEffect();
                }
                case 3 -> {
                    return entry.getCategory();
                }
                case 4 -> {
                    return entry.getPower();
                }
                case 5 -> {
                    return entry.getType();
                }
                case 6 -> {
                    return entry.getAccuracy();
                }
                case 7 -> {
                    return entry.getPp();
                }
                case 8 -> {
                    return entry.getEffectChance();
                }
                case 9 -> {
                    return entry.getTarget();
                }
                case 10 -> {
                    return entry.getPriority();
                }
                case 11, 12, 13, 14, 15, 16, 17, 18 -> {
                    return entry.getFlags()[columnIndex - 11 + NUM_FROZEN_COLUMNS];
                }
                case 19 -> {
                    return entry.getContestEffect();
                }
                case 20 -> {
                    return entry.getContestType();
                }
            }

            return null;
        }

        @Override
        public FormatModel<MoveData> getFrozenColumnModel()
        {
            return new MovesTable.MovesModel(getData(), getTextBankData()) {
                @Override
                public int getColumnCount()
                {
                    return NUM_FROZEN_COLUMNS;
                }

                @Override
                public Object getValueAt(int rowIndex, int columnIndex)
                {
                    return super.getValueAt(rowIndex, columnIndex - 2);
                }

                @Override
                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                {
                    super.setValueAt(aValue, rowIndex, columnIndex - 2);
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return columnIndex != 0;
                }
            };
        }
    }
}
