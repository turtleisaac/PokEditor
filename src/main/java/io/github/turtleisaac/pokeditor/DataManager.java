package io.github.turtleisaac.pokeditor;

import com.google.inject.*;
import com.google.inject.util.Types;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.binaries.MainCodeFile;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.GenericParser;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.JohtoEncounterParser;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterData;
import io.github.turtleisaac.pokeditor.formats.encounters.SinnohEncounterParser;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionParser;
import io.github.turtleisaac.pokeditor.formats.items.ItemData;
import io.github.turtleisaac.pokeditor.formats.items.ItemParser;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetParser;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveParser;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalParser;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteData;
import io.github.turtleisaac.pokeditor.formats.pokemon_sprites.PokemonSpriteParser;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankParser;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerParser;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditorPanel;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.pokemon_sprite.PokemonSpriteEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.DefaultSheetPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.formats.*;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager
{
    public static final String SHEET_STRINGS_PATH = "pokeditor/sheet_strings";

    public static DefaultSheetPanel<PersonalData, ?> createPersonalSheet(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<PersonalData> data = DataManager.getData(rom, PersonalData.class);
        return new DefaultSheetPanel<>(manager, new PersonalTable(data, textData));
    }

    public static DefaultSheetPanel<PersonalData, ?> createTmCompatibilitySheet(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<PersonalData> data = DataManager.getData(rom, PersonalData.class);
        DefaultSheetPanel<PersonalData, ?> sheetPanel = new DefaultSheetPanel<>(manager, new TmCompatibilityTable(data, textData));
//        String[] moveNames = textData.get(TextFiles.MOVE_NAMES.getValue()).getStringList().toArray(String[]::new);
//        sheetPanel.thing(new ComboBoxCellEditor(moveNames));
        return sheetPanel;
    }

    public static DefaultSheetPanel<EvolutionData, ?> createEvolutionSheet(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<EvolutionData> data = DataManager.getData(rom, EvolutionData.class);
        return new DefaultSheetPanel<>(manager, new EvolutionsTable(data, textData));
    }

    public static DefaultSheetPanel<LearnsetData, ?> createLearnsetSheet(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<LearnsetData> data = DataManager.getData(rom, LearnsetData.class);
        return new DefaultSheetPanel<>(manager, new LearnsetsTable(data, textData));
    }

    public static DefaultSheetPanel<MoveData, ?> createMoveSheet(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<MoveData> data = DataManager.getData(rom, MoveData.class);
        return new DefaultSheetPanel<>(manager, new MovesTable(data, textData));
    }

    public static DefaultDataEditorPanel<PokemonSpriteData, ?> createPokemonSpriteEditor(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<PokemonSpriteData> data = DataManager.getData(rom, PokemonSpriteData.class);
        return new DefaultDataEditorPanel<>(manager, new PokemonSpriteEditor(data, textData));
    }

    private static final Injector injector = Guice.createInjector(
            new PersonalModule(),
            new LearnsetsModule(),
            new EvolutionsModule(),
            new TrainersModule(),
            new MovesModule(),
            new SinnohEncountersModule(),
            new JohtoEncountersModule(),
            new ItemsModule(),
            new TextBankModule(),
            new PokemonSpriteModule());

    public static <E extends GenericFileData> GenericParser<E> getParser(Class<E> eClass)
    {
        ParameterizedType type = Types.newParameterizedType(GenericParser.class, eClass);
        return (GenericParser<E>) injector.getInstance(Key.get(TypeLiteral.get(type)));
    }

    private static final Map<Class<? extends GenericFileData>, List<? extends GenericFileData>> dataMap = new HashMap<>();
    private static final Map<GameCodeBinaries, CodeBinary> codeBinaries = new HashMap<>();

    public static <E extends GenericFileData> List<E> getData(NintendoDsRom rom, Class<E> eClass)
    {
        if (dataMap.containsKey(eClass))
            return (List<E>) dataMap.get(eClass);

        GenericParser<E> parser = DataManager.getParser(eClass);
        Map<GameFiles, Narc> input = new HashMap<>();
        for (GameFiles gameFile : parser.getRequirements())
        {
            input.put(gameFile, new Narc(rom.getFileByName(gameFile.getPath())));
        }

        List<E> data = parser.generateDataList(input, codeBinaries);
        dataMap.put(eClass, data);

        return data;
    }

    public static <E extends GenericFileData> void saveData(NintendoDsRom rom, Class<E> eClass)
    {
        if (!dataMap.containsKey(eClass))
            return;

        GenericParser<E> parser = DataManager.getParser(eClass);
        Map<GameFiles, Narc> map = parser.processDataList(getData(rom, eClass), codeBinaries);
        for (GameFiles gameFile : map.keySet())
        {
            rom.setFileByName(gameFile.getPath(), map.get(gameFile).save());
        }
    }

    public static <E extends GenericFileData> void resetData(NintendoDsRom rom, Class<E> eClass)
    {
        if (!dataMap.containsKey(eClass))
            return;

        List<E> list = (List<E>) dataMap.get(eClass);
        list.clear();
        dataMap.remove(eClass);
        List<E> newList = getData(rom, eClass);
        dataMap.remove(newList);

        list.addAll(newList);
        dataMap.put(eClass, list);
    }

    public static void codeBinarySetup(NintendoDsRom rom)
    {
        MainCodeFile arm9 = rom.loadArm9();
        codeBinaries.put(GameCodeBinaries.ARM9, arm9);
//        codeBinaries.put(GameCodeBinaries.ARM7, rom.loadArm7());

        MemBuf.MemBufWriter writer = arm9.getPhysicalAddressBuffer().writer();
        int pos = writer.getPosition();
        writer.setPosition(0xBB4); //todo account for DP if I ever add back support
        writer.writeInt(0);
        writer.setPosition(pos);
    }

    public static void saveCodeBinaries(NintendoDsRom rom, List<GameCodeBinaries> codeBinaries)
    {
        for (GameCodeBinaries codeBinary : codeBinaries)
        {
            CodeBinary binary = DataManager.codeBinaries.get(codeBinary);
            binary.lock();
            try {
                if(codeBinary == GameCodeBinaries.ARM9)
                {
                    rom.setArm9(binary.getData());
                }
            }
            finally {
                binary.unlock();
            }
        }
    }

    static class PersonalModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<PersonalData>>() {})
                    .to(PersonalParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class LearnsetsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<LearnsetData>>() {})
                    .to(LearnsetParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class EvolutionsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<EvolutionData>>() {})
                    .to(EvolutionParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class TrainersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<TrainerData>>() {})
                    .to(TrainerParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class MovesModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<MoveData>>() {})
                    .to(MoveParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class SinnohEncountersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<SinnohEncounterData>>() {})
                    .to(SinnohEncounterParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class JohtoEncountersModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<JohtoEncounterData>>() {})
                    .to(JohtoEncounterParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class ItemsModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<ItemData>>() {})
                    .to(ItemParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class TextBankModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<TextBankData>>() {})
                    .to(TextBankParser.class)
                    .in(Scopes.SINGLETON);
        }
    }

    static class PokemonSpriteModule extends AbstractModule {
        @Override
        protected void configure()
        {
            bind(new TypeLiteral<GenericParser<PokemonSpriteData>>() {})
                    .to(PokemonSpriteParser.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
