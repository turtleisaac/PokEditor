package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MovesTable extends DefaultTable<MoveData>
{
    public static final int[] columnWidths = new int[] {40, 100, 500, 100, 65, 100, 65, 65, 120, 160, 65, 65, 80, 80, 80, 80, 80, 80, 80, 80, 65, 65};
    private static final String[] categoryKeys = new String[] {"category.physical", "category.special", "category.status"};
    private static final String[] targetKeys = new String[] {"target.selected", "target.automatic", "target.random", "target.bothFoes", "target.allExceptUser", "target.user", "target.userSide", "target.entireField", "target.foeSide", "target.ally", "target.userOrAlly", "target.MOVE_TARGET_ME_FIRST"};

    public MovesTable(List<MoveData> data, List<TextBankData> textData)
    {
        super(new MovesModel(data, textData), textData, columnWidths, null);
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

    static class MovesModel extends FormatModel<MoveData>
    {

        public MovesModel(List<MoveData> data, List<TextBankData> textBankData)
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
            return MovesColumn.getColumn(columnIndex).key;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            MoveData entry = getData().get(rowIndex);
            TextBankData moveNames = getTextBankData().get(TextFiles.MOVE_NAMES.getValue());

            aValue = prepareObjectForWriting(aValue, columnIndex);

            switch (MovesColumn.getColumn(columnIndex)) {
                case ID -> {}
                case NAME -> {
                    TextBankData.Message message = moveNames.get(rowIndex);
                    message.setText((String) aValue);
                }
                case EFFECT -> entry.setEffect((Integer) aValue);
                case CATEGORY -> entry.setCategory((Integer) aValue);
                case POWER -> entry.setPower((Integer) aValue);
                case TYPE -> entry.setType((Integer) aValue);
                case ACCURACY -> entry.setAccuracy((Integer) aValue);
                case PP -> entry.setPp((Integer) aValue);
                case EFFECT_CHANCE -> entry.setEffectChance((Integer) aValue);
                case TARGET -> entry.setTarget((Integer) aValue);
                case PRIORITY -> entry.setPriority((Integer) aValue);
                case MAKES_CONTACT, BLOCKED_BY_PROTECT, REFLECTED_BY_MAGIC_COAT, AFFECTED_BY_SNATCH, AFFECTED_BY_MIRROR_MOVE, TRIGGERS_KINGS_ROCK, HIDES_HP_BARS, REMOVE_TARGET_SHADOW -> entry.getFlags()[columnIndex - MovesColumn.MAKES_CONTACT.idx] = (Boolean) aValue;
                case CONTEST_EFFECT -> entry.setContestEffect((Integer) aValue);
                case CONTEST_TYPE -> entry.setContestType((Integer) aValue);
            }
        }

        @Override
        public int getColumnCount()
        {
            return MovesColumn.NUMBER_OF_COLUMNS.idx;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData moveNames = getTextBankData().get(TextFiles.MOVE_NAMES.getValue());
            MoveData entry = getData().get(rowIndex);

            switch (MovesColumn.getColumn(columnIndex)) {
                case ID -> {
                    return rowIndex;
                }
                case NAME -> {
                    if(rowIndex < moveNames.size())
                        return moveNames.get(rowIndex).getText();
                    else
                        return "";
                }
                case EFFECT -> {
                    return entry.getEffect();
                }
                case CATEGORY -> {
                    return entry.getCategory();
                }
                case POWER -> {
                    return entry.getPower();
                }
                case TYPE -> {
                    return entry.getType();
                }
                case ACCURACY -> {
                    return entry.getAccuracy();
                }
                case PP -> {
                    return entry.getPp();
                }
                case EFFECT_CHANCE -> {
                    return entry.getEffectChance();
                }
                case TARGET -> {
                    return entry.getTarget();
                }
                case PRIORITY -> {
                    return entry.getPriority();
                }
                case MAKES_CONTACT, BLOCKED_BY_PROTECT, REFLECTED_BY_MAGIC_COAT, AFFECTED_BY_SNATCH, AFFECTED_BY_MIRROR_MOVE, TRIGGERS_KINGS_ROCK, HIDES_HP_BARS, REMOVE_TARGET_SHADOW -> {
                    return entry.getFlags()[columnIndex - MovesColumn.MAKES_CONTACT.idx];
                }
                case CONTEST_EFFECT -> {
                    return entry.getContestEffect();
                }
                case CONTEST_TYPE -> {
                    return entry.getContestType();
                }
            }

            return null;
        }

        @Override
        protected CellTypes getCellType(int columnIndex)
        {
            return MovesColumn.getColumn(columnIndex).cellType;
        }

        @Override
        public FormatModel<MoveData> getFrozenColumnModel()
        {
            return new MovesModel(getData(), getTextBankData()) {
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
                protected CellTypes getCellType(int columnIndex)
                {
                    return super.getCellType(columnIndex - super.getNumFrozenColumns());
                }

                @Override
                public boolean isCellEditable(int rowIndex, int columnIndex)
                {
                    return columnIndex != 0;
                }
            };
        }
    }

    enum MovesColumn
    {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        EFFECT(0, "moves.effect", CellTypes.INTEGER),
        CATEGORY(1, "category", CellTypes.COMBO_BOX),
        POWER(2, "power", CellTypes.INTEGER),
        TYPE(3, "type", CellTypes.COLORED_COMBO_BOX),
        ACCURACY(4, "accuracy", CellTypes.INTEGER),
        PP(5, "pp", CellTypes.INTEGER),
        EFFECT_CHANCE(6, "moves.effectChance", CellTypes.INTEGER),
        TARGET(7, "target", CellTypes.BITFIELD_COMBO_BOX),
        PRIORITY(8, "priority", CellTypes.INTEGER),
        MAKES_CONTACT(9, "moves.makesContact", CellTypes.CHECKBOX),
        BLOCKED_BY_PROTECT(10, "moves.blockedByProtect", CellTypes.CHECKBOX),
        REFLECTED_BY_MAGIC_COAT(11, "moves.reflectedByMagicCoat", CellTypes.CHECKBOX),
        AFFECTED_BY_SNATCH(12, "moves.affectedBySnatch", CellTypes.CHECKBOX),
        AFFECTED_BY_MIRROR_MOVE(13, "moves.affectedByMirrorMove", CellTypes.CHECKBOX),
        TRIGGERS_KINGS_ROCK(14, "moves.triggersKingsRock", CellTypes.CHECKBOX),
        HIDES_HP_BARS(15, "moves.hidesHpBars", CellTypes.CHECKBOX),
        REMOVE_TARGET_SHADOW(16, "moves.removeTargetShadow", CellTypes.CHECKBOX),
        CONTEST_EFFECT(17, "moves.contestEffect", CellTypes.INTEGER),
        CONTEST_TYPE(18, "moves.contestType", CellTypes.INTEGER),
        NUMBER_OF_COLUMNS(19, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;

        MovesColumn(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static MovesColumn getColumn(int idx)
        {
            for (MovesColumn column : MovesColumn.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }
}
