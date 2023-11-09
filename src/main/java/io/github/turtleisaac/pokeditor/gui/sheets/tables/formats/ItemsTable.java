//package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;
//
//import io.github.turtleisaac.pokeditor.formats.items.ItemData;
//import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
//import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
//import io.github.turtleisaac.pokeditor.gamedata.TextFiles;
//import io.github.turtleisaac.pokeditor.gui.sheets.tables.CellTypes;
//import io.github.turtleisaac.pokeditor.gui.sheets.tables.DefaultTable;
//import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModel;
//
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Queue;
//
//public class ItemsTable extends DefaultTable<ItemData>
//{
//    private static final int NUM_FROZEN_COLUMNS = 2;
//    public static final int[] columnWidths = new int[] {40, 100, 500, 100, 65, 100, 65, 65, 120, 160, 65, 65, 80, 80, 80, 80, 80, 80, 80, 80, 65, 65};
//
//    public ItemsTable(List<ItemData> data, List<TextBankData> textData)
//    {
//        super(new ItemsModel(data, textData), textData, columnWidths, null);
//    }
//
//    @Override
//    public Queue<String[]> obtainTextSources(List<TextBankData> textData)
//    {
//        Queue<String[]> textSources = new LinkedList<>();
//
////        String[] categories = DefaultTable.loadStringsFromKeys(categoryKeys);
//
//        String[] typeNames = textData.get(TextFiles.TYPE_NAMES.getValue()).getStringList().toArray(String[]::new);
//
////        String[] targets = DefaultTable.loadStringsFromKeys(targetKeys);
//
//        String[] arr = new String[500];
//        Arrays.fill(arr, "Moo");
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//        textSources.add(arr);
//
//
////        textSources.add(categories);
////        textSources.add(typeNames);
////        textSources.add(targets);
////        textSources.add(arr);
//
//        return textSources;
//    }
//
//    @Override
//    public Class<ItemData> getDataClass()
//    {
//        return ItemData.class;
//    }
//
//    static class ItemsModel extends FormatModel<ItemData>
//    {
//        private static final String[] columnKeys = new String[] {"id", "name", "moves.effect", "category", "power", "type", "accuracy", "pp", "moves.effectChance", "target", "priority", "moves.makesContact", "moves.blockedByProtect", "moves.reflectedByMagicCoat", "moves.affectedBySnatch", "moves.affectedByMirrorMove", "moves.triggersKingsRock", "moves.hidesHpBars", "moves.removeTargetShadow", "moves.contestEffect", "moves.contestType"};
//
//        public ItemsModel(List<ItemData> data, List<TextBankData> textBankData)
//        {
//            super(columnKeys, data, textBankData, NUM_FROZEN_COLUMNS);
//        }
//
//        @Override
//        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
//        {
//            ItemData entry = getData().get(rowIndex);
//            TextBankData itemNames = getTextBankData().get(TextFiles.ITEM_NAMES.getValue());
//
////            aValue = prepareObjectForWriting(aValue, columnIndex);
////
////
////            switch (columnIndex + NUM_FROZEN_COLUMNS) {
////                case 0 -> {}
////                case 1 -> {
////                    TextBankData.Message message = moveNames.get(rowIndex);
////                    message.setText((String) aValue);
////                }
////                case 2 -> entry.setEffect((Integer) aValue);
////                case 3 -> entry.setCategory((Integer) aValue);
////                case 4 -> entry.setPower((Integer) aValue);
////                case 5 -> entry.setType((Integer) aValue);
////                case 6 -> entry.setAccuracy((Integer) aValue);
////                case 7 -> entry.setPp((Integer) aValue);
////                case 8 -> entry.setEffectChance((Integer) aValue);
////                case 9 -> {
////                    entry.setTarget((Integer) aValue);
////                }
////                case 10 -> entry.setPriority((Integer) aValue);
////                case 11, 12, 13, 14, 15, 16, 17, 18 -> entry.getFlags()[columnIndex - 11 + NUM_FROZEN_COLUMNS] = (Boolean) aValue;
////                case 19 -> entry.setContestEffect((Integer) aValue);
////                case 20 -> entry.setContestType((Integer) aValue);
////            }
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex)
//        {
//            TextBankData itemNames = getTextBankData().get(TextFiles.ITEM_NAMES.getValue());
//            ItemData entry = getData().get(rowIndex);
//
//            switch (columnIndex + NUM_FROZEN_COLUMNS) {
//                case 0 -> {
//                    return rowIndex;
//                }
//                case 1 -> {
//                    if(rowIndex < itemNames.size())
//                        return itemNames.get(rowIndex).getText();
//                    else
//                        return "";
//                }
//                case 2 -> {
//                    return entry.getPrice();
//                }
//                case 3 -> {
//                    return entry.getEquipmentEffect();
//                }
//                case 4 -> {
//                    return entry.getPower();
//                }
//                case 5 -> {
//                    return entry.getPluckEffect();
//                }
//                case 6 -> {
//                    return entry.getFlingEffect();
//                }
//                case 7 -> {
//                    return entry.getNaturalGiftPower();
//                }
//                case 8 -> {
//                    return entry.getNaturalGiftType();
//                }
//                case 9 -> {
//                    return entry.isUnableToDiscard();
//                }
//                case 10 -> {
//                    return entry.isCanRegister();
//                }
//                case 11 -> {
//                    return entry.getFieldBag();
//                }
//                case 12 -> {
//                    return entry.getBattleBag();
//                }
//                case 13 -> {
//                    return entry.getFieldFunction();
//                }
//                case 14 -> {
//                    return entry.getBattleFunction();
//                }
//                case 15 -> {
//                    return entry.getWorkType();
//                }
//                case 16, 17, 18, 19, 20, 21, 22, 23 -> {
//                    return entry.getStatusRecoveries()[columnIndex - 16 + NUM_FROZEN_COLUMNS];
//                }
//                case 24, 25, 26, 27 -> {
//                    return entry.getUtilities()[columnIndex - 24 + NUM_FROZEN_COLUMNS];
//                }
//                case 28, 29, 30, 31, 32, 33, 34 -> {
//                    return entry.getStatBoosts()[columnIndex - 28 + NUM_FROZEN_COLUMNS];
//                }
//                case 35, 36 -> {
//                    return entry.getPpUpEffects()[columnIndex - 35 + NUM_FROZEN_COLUMNS];
//                }
//                case 37, 38, 39 -> {
//                    return entry.getRecoveryToggles()[columnIndex - 37 + NUM_FROZEN_COLUMNS];
//                }
//                case 40, 41, 42, 43, 44, 45 -> {
//                    return entry.getEvYieldToggles()[columnIndex - 40 + NUM_FROZEN_COLUMNS];
//                }
//                case 46, 47, 48 -> {
//                    return entry.getFriendshipChangeToggles()[columnIndex - 46 + NUM_FROZEN_COLUMNS];
//                }
//                case 49, 50, 51, 52, 53, 54 -> {
//                    return entry.getEvYields()[columnIndex - 49 + NUM_FROZEN_COLUMNS];
//                }
//                case 55 -> {
//                    return entry.getHpRecoveryAmount();
//                }
//                case 56 -> {
//                    return entry.getPpRecoveryAmount();
//                }
//                case 57, 58, 59 -> {
//                    return entry.getFriendshipChangeAmounts()[columnIndex - 57 + NUM_FROZEN_COLUMNS];
//                }
//            }
//
//            return null;
//        }
//
//        @Override
//        protected CellTypes getCellType(int columnIndex)
//        {
//            return switch(columnIndex + NUM_FROZEN_COLUMNS) {
//                case 0, 2, 3, 4, 7, 8, 29, 30, 31, 32, 33, 34, 35, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 ->
//                        CellTypes.INTEGER;
//                case 5, 6, 14, 15 -> CellTypes.COMBO_BOX;
//                case 9 -> CellTypes.COLORED_COMBO_BOX;
//                case 10, 11, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 ->
//                        CellTypes.CHECKBOX;
//                case 12, 13 -> CellTypes.BITFIELD_COMBO_BOX;
//                default -> super.getCellType(columnIndex);
//            };
//        }
//
//        @Override
//        public FormatModel<ItemData> getFrozenColumnModel()
//        {
//            return new ItemsModel(getData(), getTextBankData()) {
//                @Override
//                public int getColumnCount()
//                {
//                    return NUM_FROZEN_COLUMNS;
//                }
//
//                @Override
//                public Object getValueAt(int rowIndex, int columnIndex)
//                {
//                    return super.getValueAt(rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
//                }
//
//                @Override
//                public void setValueAt(Object aValue, int rowIndex, int columnIndex)
//                {
//                    super.setValueAt(aValue, rowIndex, columnIndex - NUM_FROZEN_COLUMNS);
//                }
//
//                @Override
//                public boolean isCellEditable(int rowIndex, int columnIndex)
//                {
//                    return columnIndex != 0;
//                }
//            };
//        }
//    }
//
//    enum ItemsColumn
//    {
//        ID(-2, "id", CellTypes.INTEGER),
//        NAME(-1, "name", CellTypes.STRING),
//        PRICE(0, "moves.effect", CellTypes.INTEGER),
//        CATEGORY(1, "category", CellTypes.COMBO_BOX),
//        POWER(2, "power", CellTypes.INTEGER),
//        TYPE(3, "type", CellTypes.COLORED_COMBO_BOX),
//        ACCURACY(4, "accuracy", CellTypes.INTEGER),
//        PP(5, "pp", CellTypes.INTEGER),
//        EFFECT_CHANCE(6, "moves.effectChance", CellTypes.INTEGER),
//        TARGET(7, "target", CellTypes.BITFIELD_COMBO_BOX),
//        PRIORITY(8, "priority", CellTypes.INTEGER),
//        MAKES_CONTACT(9, "moves.makesContact", CellTypes.CHECKBOX),
//        BLOCKED_BY_PROTECT(10, "moves.blockedByProtect", CellTypes.CHECKBOX),
//        REFLECTED_BY_MAGIC_COAT(11, "moves.reflectedByMagicCoat", CellTypes.CHECKBOX),
//        AFFECTED_BY_SNATCH(12, "moves.affectedBySnatch", CellTypes.CHECKBOX),
//        AFFECTED_BY_MIRROR_MOVE(13, "moves.affectedByMirrorMove", CellTypes.CHECKBOX),
//        TRIGGERS_KINGS_ROCK(14, "moves.triggersKingsRock", CellTypes.CHECKBOX),
//        HIDES_HP_BARS(15, "moves.hidesHpBars", CellTypes.CHECKBOX),
//        REMOVE_TARGET_SHADOW(16, "moves.removeTargetShadow", CellTypes.CHECKBOX),
//        CONTEST_EFFECT(17, "moves.contestEffect", CellTypes.INTEGER),
//        CONTEST_TYPE(18, "moves.contestType", CellTypes.INTEGER),
//        NUMBER_OF_COLUMNS(19, null, null);
//
//        private final int idx;
//        private final String key;
//        private final CellTypes cellType;
//
//        ItemsColumn(int idx, String key, CellTypes cellType)
//        {
//            this.idx = idx;
//            this.key = key;
//            this.cellType = cellType;
//        }
//
//        static ItemsColumn getColumn(int idx)
//        {
//            for (ItemsColumn column : ItemsColumn.values())
//            {
//                if (column.idx == idx) {
//                    return column;
//                }
//            }
//            return NUMBER_OF_COLUMNS;
//        }
//    }
//}
