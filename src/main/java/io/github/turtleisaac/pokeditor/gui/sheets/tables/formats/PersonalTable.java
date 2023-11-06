package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;

import java.util.*;

public class PersonalTable extends DefaultTable<PersonalData>
{
    public static final int[] columnWidths = new int[] {40, 100, 65, 65, 65, 65, 65, 65, 100, 100, 65, 65, 65, 65, 65, 65, 65, 65, 140, 140, 65, 65, 70, 120, 120, 120, 140, 140, 65, 65, 65};
    private static final String[] growthRateKeys = new String[] {"growthRate.mediumFast", "growthRate.erratic",  "growthRate.fluctuating", "growthRate.mediumSlow", "growthRate.fast", "growthRate.slow", "growthRate.mediumFast", "growthRate.mediumFast"};
    private static final String[] eggGroupKeys = new String[] {"eggGroup.null", "eggGroup.monster", "eggGroup.water1", "eggGroup.bug", "eggGroup.flying", "eggGroup.field", "eggGroup.fairy", "eggGroup.grass", "eggGroup.humanLike", "eggGroup.water3", "eggGroup.mineral", "eggGroup.amorphous", "eggGroup.water2", "eggGroup.ditto", "eggGroup.dragon", "eggGroup.undiscovered"};

    public PersonalTable(List<PersonalData> data, List<TextBankData> textData)
    {
        super(new PersonalModel(data, textData), textData, columnWidths, null);
    }

    @Override
    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
    {
        Queue<String[]> textSources = new LinkedList<>();
        String[] temp = textData.get(TextFiles.TYPE_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);

        temp = textData.get(TextFiles.ITEM_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);

        textSources.add(DefaultTable.loadStringsFromKeys(growthRateKeys));

        temp = DefaultTable.loadStringsFromKeys(eggGroupKeys);
        textSources.add(temp);
        textSources.add(temp);

        temp = textData.get(TextFiles.ABILITY_NAMES.getValue()).getStringList().toArray(String[]::new);
        textSources.add(temp);
        textSources.add(temp);
        textSources.add(temp);

        return textSources;
    }

    @Override
    public Class<PersonalData> getDataClass()
    {
        return PersonalData.class;
    }

    static class PersonalModel extends FormatModel<PersonalData>
    {

        public PersonalModel(List<PersonalData> data, List<TextBankData> textBankData)
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
            return PersonalColumns.getColumn(columnIndex).key;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            PersonalData entry = getData().get(rowIndex);
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());

            aValue = prepareObjectForWriting(aValue, columnIndex);

            switch (PersonalColumns.getColumn(columnIndex)) {
                case ID -> {}
                case NAME -> {
                    TextBankData.Message message = speciesNames.get(rowIndex);
                    message.setText((String) aValue);
                }
                case HP -> entry.setHp((Integer) aValue);
                case ATK -> entry.setAtk((Integer) aValue);
                case DEF -> entry.setDef((Integer) aValue);
                case SPEED -> entry.setSpeed((Integer) aValue);
                case SP_ATK -> entry.setSpAtk((Integer) aValue);
                case SP_DEF -> entry.setSpDef((Integer) aValue);
                case TYPE_1 -> entry.setType1((Integer) aValue);
                case TYPE_2 -> entry.setType2((Integer) aValue);
                case CATCH_RATE -> entry.setCatchRate((Integer) aValue);
                case EXP_DROP -> entry.setBaseExp((Integer) aValue);
                case HP_EV_YIELD -> entry.setHpEvYield((Integer) aValue);
                case ATK_EV_YIELD -> entry.setAtkEvYield((Integer) aValue);
                case DEF_EV_YIELD -> entry.setDefEvYield((Integer) aValue);
                case SPEED_EV_YIELD -> entry.setSpeedEvYield((Integer) aValue);
                case SP_ATK_EV_YIELD -> entry.setSpAtkEvYield((Integer) aValue);
                case SP_DEF_EV_YIELD -> entry.setSpDefEvYield((Integer) aValue);
                case UNCOMMON_HELD_ITEM -> entry.setUncommonItem((Integer) aValue);
                case RARE_HELD_ITEM -> entry.setRareItem((Integer) aValue);
                case GENDER_RATIO -> entry.setGenderRatio((Integer) aValue);
                case HATCH_MULTIPLIER -> entry.setHatchMultiplier((Integer) aValue);
                case BASE_HAPPINESS -> entry.setBaseHappiness((Integer) aValue);
                case GROWTH_RATE -> entry.setExpRate((Integer) aValue);
                case EGG_GROUP_1 -> entry.setEggGroup1((Integer) aValue);
                case EGG_GROUP_2 -> entry.setEggGroup2((Integer) aValue);
                case ABILITY_1 -> entry.setAbility1((Integer) aValue);
                case ABILITY_2 -> entry.setAbility2((Integer) aValue);
                case RUN_CHANCE -> entry.setRunChance((Integer) aValue);
                case COLOR -> entry.setDexColor((Integer) aValue);
                case FLIP -> entry.setFlip((Boolean) aValue);
            }
        }

        @Override
        public int getColumnCount()
        {
            return PersonalColumns.NUMBER_OF_COLUMNS.idx;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            TextBankData speciesNames = getTextBankData().get(TextFiles.SPECIES_NAMES.getValue());
            PersonalData entry = getData().get(rowIndex);

            switch (PersonalColumns.getColumn(columnIndex)) {
                case ID -> {
                    return rowIndex;
                }
                case NAME -> {
                    if(rowIndex < speciesNames.size())
                        return speciesNames.get(rowIndex).getText();
                    else
                        return "";
                }
                case HP -> {
                    return entry.getHp();
                }
                case ATK -> {
                    return entry.getAtk();
                }
                case DEF -> {
                    return entry.getDef();
                }
                case SPEED -> {
                    return entry.getSpeed();
                }
                case SP_ATK -> {
                    return entry.getSpAtk();
                }
                case SP_DEF -> {
                    return entry.getSpDef();
                }
                case TYPE_1 -> {
                    return entry.getType1();
                }
                case TYPE_2 -> {
                    return entry.getType2();
                }
                case CATCH_RATE -> {
                    return entry.getCatchRate();
                }
                case EXP_DROP -> {
                    return entry.getBaseExp();
                }
                case HP_EV_YIELD -> {
                    return entry.getHpEvYield();
                }
                case ATK_EV_YIELD -> {
                    return entry.getAtkEvYield();
                }
                case DEF_EV_YIELD -> {
                    return entry.getDefEvYield();
                }
                case SPEED_EV_YIELD -> {
                    return entry.getSpeedEvYield();
                }
                case SP_ATK_EV_YIELD -> {
                    return entry.getSpAtkEvYield();
                }
                case SP_DEF_EV_YIELD -> {
                    return entry.getSpDefEvYield();
                }
                case UNCOMMON_HELD_ITEM -> {
                    return entry.getUncommonItem();
                }
                case RARE_HELD_ITEM -> {
                    return entry.getRareItem();
                }
                case GENDER_RATIO -> {
                    return entry.getGenderRatio();
                }
                case HATCH_MULTIPLIER -> {
                    return entry.getHatchMultiplier();
                }
                case BASE_HAPPINESS -> {
                    return entry.getBaseHappiness();
                }
                case GROWTH_RATE -> {
                    return entry.getExpRate();
                }
                case EGG_GROUP_1-> {
                    return entry.getEggGroup1();
                }
                case EGG_GROUP_2 -> {
                    return entry.getEggGroup2();
                }
                case ABILITY_1 -> {
                    return entry.getAbility1();
                }
                case ABILITY_2 -> {
                    return entry.getAbility2();
                }
                case RUN_CHANCE -> {
                    return entry.getRunChance();
                }
                case COLOR -> {
                    return entry.getDexColor();
                }
                case FLIP -> {
                    return entry.isFlip();
                }
            }

            return null;
        }

        @Override
        protected CellTypes getCellType(int columnIndex)
        {
            return PersonalColumns.getColumn(columnIndex).cellType;
        }

        @Override
        public FormatModel<PersonalData> getFrozenColumnModel()
        {
            return new PersonalModel(getData(), getTextBankData()) {
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
                    return columnIndex != 0;
                }
            };
        }
    }

    enum PersonalColumns {
        ID(-2, "id", CellTypes.INTEGER),
        NAME(-1, "name", CellTypes.STRING),
        HP(0, "hp", CellTypes.INTEGER),
        ATK(1, "atk", CellTypes.INTEGER),
        DEF(2, "def", CellTypes.INTEGER),
        SPEED(3, "speed", CellTypes.INTEGER),
        SP_ATK(4, "spAtk", CellTypes.INTEGER),
        SP_DEF(5, "spDef", CellTypes.INTEGER),
        TYPE_1(6, "personal.type1", CellTypes.COLORED_COMBO_BOX),
        TYPE_2(7, "personal.type2", CellTypes.COLORED_COMBO_BOX),
        CATCH_RATE(8, "catchRate", CellTypes.INTEGER),
        EXP_DROP(9, "expDrop", CellTypes.INTEGER),
        HP_EV_YIELD(10, "hpEvYield", CellTypes.INTEGER),
        ATK_EV_YIELD(11, "atkEvYield", CellTypes.INTEGER),
        DEF_EV_YIELD(12, "defEvYield", CellTypes.INTEGER),
        SPEED_EV_YIELD(13, "speedEvYield", CellTypes.INTEGER),
        SP_ATK_EV_YIELD(14, "spAtkEvYield", CellTypes.INTEGER),
        SP_DEF_EV_YIELD(15, "spDefEvYield", CellTypes.INTEGER),
        UNCOMMON_HELD_ITEM(16, "uncommonHeldItem", CellTypes.COMBO_BOX),
        RARE_HELD_ITEM(17, "rareHeldItem", CellTypes.COMBO_BOX),
        GENDER_RATIO(18, "genderRatio", CellTypes.INTEGER),
        HATCH_MULTIPLIER(19, "hatchMultiplier", CellTypes.INTEGER),
        BASE_HAPPINESS(20, "baseHappiness", CellTypes.INTEGER),
        GROWTH_RATE(21, "growthRate", CellTypes.COMBO_BOX),
        EGG_GROUP_1(22, "personal.eggGroup1", CellTypes.COMBO_BOX),
        EGG_GROUP_2(23, "personal.eggGroup2", CellTypes.COMBO_BOX),
        ABILITY_1(24, "personal.ability1", CellTypes.COMBO_BOX),
        ABILITY_2(25, "personal.ability2", CellTypes.COMBO_BOX),
        RUN_CHANCE(26, "runChance", CellTypes.INTEGER),
        COLOR(27, "color", CellTypes.INTEGER),
        FLIP(28, "flip", CellTypes.CHECKBOX),
        NUMBER_OF_COLUMNS(29, null, null);

        private final int idx;
        private final String key;
        private final CellTypes cellType;

        PersonalColumns(int idx, String key, CellTypes cellType)
        {
            this.idx = idx;
            this.key = key;
            this.cellType = cellType;
        }

        static PersonalColumns getColumn(int idx)
        {
            for (PersonalColumns column : PersonalColumns.values())
            {
                if (column.idx == idx) {
                    return column;
                }
            }
            return NUMBER_OF_COLUMNS;
        }
    }
}
