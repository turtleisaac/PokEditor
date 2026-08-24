package io.github.turtleisaac.pokeditor;

import com.google.inject.*;
import com.google.inject.util.Types;
import io.github.turtleisaac.nds4j.Narc;
import io.github.turtleisaac.nds4j.NintendoDsRom;
import io.github.turtleisaac.nds4j.binaries.CodeBinary;
import io.github.turtleisaac.nds4j.binaries.MainCodeFile;
import io.github.turtleisaac.nds4j.framework.BinaryWriter;
import io.github.turtleisaac.nds4j.framework.MemBuf;
import io.github.turtleisaac.nds4j.ui.Tool;
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
import io.github.turtleisaac.pokeditor.formats.scripts.FieldScriptParser;
import io.github.turtleisaac.pokeditor.formats.scripts.GenericScriptData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankData;
import io.github.turtleisaac.pokeditor.formats.text.TextBankParser;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerData;
import io.github.turtleisaac.pokeditor.formats.trainers.TrainerParser;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import io.github.turtleisaac.pokeditor.gamedata.GameFiles;
import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import io.github.turtleisaac.pokeditor.gui.editors.data.DefaultDataEditorPanel;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.field.FieldScriptEditor;
import io.github.turtleisaac.pokeditor.gui.editors.data.formats.pokemon_sprite.PokemonSpriteEditor;
import io.github.turtleisaac.pokeditor.gui.sheets.DefaultSheetPanel;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.formats.*;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Objects;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<MoveData> moves = DataManager.getData(rom, MoveData.class);
        DefaultSheetPanel<PersonalData, ?> sheetPanel = new DefaultSheetPanel<>(manager, new TmCompatibilityTable(data, textData, moves));
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

    public static DefaultDataEditorPanel<GenericScriptData, ?> createFieldScriptEditor(PokeditorManager manager, NintendoDsRom rom)
    {
        List<TextBankData> textData = DataManager.getData(rom, TextBankData.class);
        List<GenericScriptData> data = DataManager.getData(rom, GenericScriptData.class);
        return new DefaultDataEditorPanel<>(manager, new FieldScriptEditor(data, textData));
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

    @SuppressWarnings("unchecked")
    public static <E extends GenericFileData> GenericParser<E> getParser(Class<E> eClass)
    {
        if (eClass != GenericScriptData.class) {
            ParameterizedType type = Types.newParameterizedType(GenericParser.class, eClass);
            return (GenericParser<E>) injector.getInstance(Key.get(TypeLiteral.get(type)));
        } else {
            return (GenericParser<E>) new FieldScriptParser();
        }
    }

    private static final Map<Class<? extends GenericFileData>, List<? extends GenericFileData>> dataMap = new HashMap<>();
    private static final Map<GameCodeBinaries, CodeBinary> codeBinaries = new HashMap<>();

    /**
     * The ROM the two caches below hold data for.
     * <p>
     * They are keyed by data class alone, so nothing else distinguishes one ROM's parsed data
     * from another's. Nothing in the application can currently open a second ROM in one session
     * - the three menu entries that would are unimplemented stubs and closing the tool frame
     * exits the process - so this is a guard against a future capability rather than a bug
     * anyone has hit. It is kept because it costs four lines and the failure it prevents
     * (writing one ROM's tables into another) is silent and unrecoverable.
     * <p>
     * Compared by identity, not equality: two ROMs loaded from the same file are separate
     * objects with separate edits, and treating them as interchangeable is the same bug again.
     */
    private static NintendoDsRom cachedRom;

    /**
     * Points the caches at the given ROM, discarding anything held for a different one.
     * <p>
     * Deliberately does <em>not</em> clear the dirty flags. Whether unsaved work may be
     * discarded is a decision for whoever switches ROMs, taken in front of the user; a cache
     * coherency helper silently answering it means the exit prompt goes quiet and the edits
     * vanish with no warning.
     * <p>
     * Note that {@link #commitData} and {@link #saveCodeBinaries} both take a ROM and do not
     * come through here. They act on data already prepared for a specific ROM rather than
     * fetching any, so there is nothing for them to invalidate - but if ROM switching is ever
     * implemented, commitData is where one ROM's narcs could be written into another.
     */
    private static void useRom(NintendoDsRom rom)
    {
        if (cachedRom == rom)
            return;

        dataMap.clear();
        codeBinaries.clear();
        cachedRom = rom;
    }

    /**
     * Fails rather than treating a missing ROM as a ROM.
     * <p>
     * A null argument used to be harmless here, because the ROM was ignored whenever the cache
     * already held the class. Once the caches became ROM-scoped it stopped being harmless: null
     * read as "a different ROM", which discarded every loaded sheet and every code binary - and
     * since the binaries are only ever populated once, at startup, that left the session unable
     * to save anything again. Refusing loudly is the only version of this that cannot quietly
     * answer for the wrong ROM.
     */
    private static void requireRom(NintendoDsRom rom)
    {
        Objects.requireNonNull(rom, "A ROM must be provided - DataManager cannot resolve data "
                + "without knowing which ROM it belongs to.");
    }

    @SuppressWarnings("unchecked")
    public static <E extends GenericFileData> List<E> getData(NintendoDsRom rom, Class<E> eClass)
    {
        requireRom(rom);
        useRom(rom);
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

    private static final Set<Class<? extends GenericFileData>> dirtyClasses = new HashSet<>();

    /**
     * Records that the in-memory data for the provided class has been edited but not yet
     * written out, so the tool can prompt before discarding it on exit.
     */
    public static void markDirty(Class<? extends GenericFileData> eClass)
    {
        if (eClass != null)
            dirtyClasses.add(eClass);
    }

    public static void markClean(Class<? extends GenericFileData> eClass)
    {
        dirtyClasses.remove(eClass);
    }

    public static boolean hasUnsavedChanges()
    {
        return !dirtyClasses.isEmpty();
    }

    /**
     * Serialises the in-memory data for the provided class WITHOUT touching the ROM.
     * <p>
     * This is deliberately side effect free so callers can show the user which files a save
     * would write (and let them back out) before anything is actually modified - previously
     * this method mutated the ROM up front, so declining the confirmation only skipped the
     * write to disk while leaving the "cancelled" edits sitting in the in-memory ROM.
     * @return the files which would be written, or null if the class has never been loaded
     */
    public static <E extends GenericFileData> Map<GameFiles, Narc> prepareData(NintendoDsRom rom, Class<E> eClass)
    {
        requireRom(rom);
        useRom(rom);
        if (!dataMap.containsKey(eClass))
            return null;

        GenericParser<E> parser = DataManager.getParser(eClass);
        return parser.processDataList(getData(rom, eClass), codeBinaries);
    }

    /**
     * Whether the given class has been parsed and is available to save.
     * <p>
     * Answers the question {@link #prepareData} used to answer by returning null, but without
     * serialising anything - so a caller can decide whether a save is possible before doing the
     * work that has side effects.
     */
    public static <E extends GenericFileData> boolean isLoaded(Class<E> eClass)
    {
        return dataMap.containsKey(eClass);
    }

    /**
     * The game files a save of the given class will write.
     * <p>
     * Every parser's {@code getRequirements()} is exactly the key set of the map its
     * {@code processDataList} returns, so this is the same list the prepared data would have
     * yielded - available without running the preparation. That is what lets the confirmation
     * dialog name the files before anything is serialised.
     */
    public static <E extends GenericFileData> List<GameFiles> filesWrittenBy(Class<E> eClass)
    {
        return DataManager.getParser(eClass).getRequirements();
    }

    /**
     * Applies the result of {@link #prepareData(NintendoDsRom, Class)} to the in-memory ROM.
     */
    public static void commitData(NintendoDsRom rom, Map<GameFiles, Narc> map)
    {
        if (map == null)
            return;

        for (GameFiles gameFile : map.keySet())
        {
            rom.setFileByName(gameFile.getPath(), map.get(gameFile).save());
        }
    }

    /**
     * Discards the in-memory edits for the provided class and re-parses it from the ROM.
     * <p>
     * Nothing is prepared until the user has confirmed a save, so a cancelled save leaves the
     * ROM untouched and re-reading it here genuinely discards the unsaved edits.
     * <p>
     * Preparation itself is not free of side effects - PersonalParser writes the TM table into
     * the shared arm9 buffer - so a save that gets past the confirmation and then fails on a
     * value the data refuses can still leave arm9 partly written. That is a failure rather
     * than a cancellation, and re-reading does not undo it.
     */
    @SuppressWarnings("unchecked")
    public static <E extends GenericFileData> void resetData(NintendoDsRom rom, Class<E> eClass)
    {
        requireRom(rom);
        useRom(rom);
        if (!dataMap.containsKey(eClass))
            return;

        List<E> list = (List<E>) dataMap.get(eClass);
        list.clear();
        dataMap.remove(eClass);
        List<E> newList = getData(rom, eClass);
        // getData has now cached its own freshly parsed list under eClass. The caller still holds
        // the original list object, so the contents are moved into it and it is put back as the
        // cached one - otherwise every open sheet would keep rendering the discarded edits.
        // (This used to call dataMap.remove(newList), which passes a List to a Class-keyed map
        // and therefore removed nothing at all.)
        list.addAll(newList);
        dataMap.put(eClass, list);
        markClean(eClass);
    }

    public static void codeBinarySetup(NintendoDsRom rom)
    {
        requireRom(rom);
        useRom(rom);
        MainCodeFile arm9 = rom.loadArm9();
        codeBinaries.put(GameCodeBinaries.ARM9, arm9);
//        codeBinaries.put(GameCodeBinaries.ARM7, rom.loadArm7());

        // Through the lock, like every other arm9 write in the codebase. Not for mutual
        // exclusion - nothing is concurrent at startup - but because lock() and unlock() are
        // what maintain the buffer's cursors: unlock() extends the recorded size to whatever
        // was written and puts the write cursor back at the end. getData() returns the bytes
        // between the read and write cursors, so a hand-rolled restore that leaves the writer
        // short truncates arm9 on save. This used to save and restore the position itself,
        // which is a weaker copy of the same protocol.
        arm9.lock();
        try {
            MemBuf.MemBufWriter writer = arm9.getPhysicalAddressBuffer().writer();
            writer.setPosition(0xBB4); //todo account for DP if I ever add back support
            writer.writeInt(0);
        }
        finally {
            arm9.unlock();
        }
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
