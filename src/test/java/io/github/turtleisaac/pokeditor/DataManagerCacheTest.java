package io.github.turtleisaac.pokeditor;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import io.github.turtleisaac.pokeditor.gamedata.GameCodeBinaries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A missing ROM must never cost the user their work.
 * <p>
 * The parsed-data caches had no tests. That is how a change to scope them per-ROM shipped a
 * regression in which one call passing a null ROM was read as "a different ROM": it discarded
 * every open sheet's parsed data, every unsaved-change flag, <em>and</em> the code binaries -
 * and since the binaries are populated exactly once at startup, the session could never save
 * again. Everything below is about that: not the exception, but what survives it.
 * <p>
 * The ROM-switching property itself is deliberately absent. Exercising it needs two
 * {@link io.github.turtleisaac.nds4j.NintendoDsRom} instances, which cannot be built without
 * two real ROM files, and a fixture assembled to look like one would only prove the cache
 * agrees with the fixture. It is also unreachable in the shipped application - the three menu
 * entries that would open a second ROM are unimplemented, and closing the tool frame ends the
 * process - so the scoping is a guard against a future capability, not a tested behaviour.
 * Saying so is more useful than a test that pretends otherwise.
 */
class DataManagerCacheTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @SuppressWarnings("unchecked")
    private static <T> T field(String name)
    {
        try {
            Field f = DataManager.class.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(null);
        }
        catch (ReflectiveOperationException e) {
            throw new AssertionError("DataManager." + name + " could not be read", e);
        }
    }

    private static Map<Class<? extends GenericFileData>, Object> dataMap() { return field("dataMap"); }
    private static Map<GameCodeBinaries, Object> codeBinaries() { return field("codeBinaries"); }
    private static Set<Class<? extends GenericFileData>> dirtyClasses() { return field("dirtyClasses"); }

    /**
     * Puts the caches into the state a running session is in: sheets parsed, one of them edited,
     * the arm9 binary loaded. Reflection because there is no reset hook - itself worth noting,
     * since state with process lifetime and no way to clear it is hard to test deliberately.
     */
    @BeforeEach
    void seedCaches()
    {
        dataMap().clear();
        codeBinaries().clear();
        dirtyClasses().clear();

        dataMap().put(PersonalData.class, new ArrayList<>());
        dataMap().put(MoveData.class, new ArrayList<>());
        dirtyClasses().add(PersonalData.class);
        codeBinaries().put(GameCodeBinaries.ARM9, new Object());
    }

    @Test
    @DisplayName("a null ROM is refused before any cache is touched")
    void nullRomIsRefusedWithoutSideEffects()
    {
        // The exception is not the point; what survives it is. A missing ROM is a caller
        // mistake, and the cost of one must not be the user's unsaved work. Nor may it be
        // answered from whatever happens to be cached - that quietly returns another ROM's
        // data, which is the very failure the per-ROM scoping exists to prevent.
        assertThatThrownBy(() -> DataManager.getData(null, MoveData.class))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ROM");

        assertThat(dataMap()).as("a refused call must not discard parsed data")
                .containsKeys(PersonalData.class, MoveData.class);
        assertThat(codeBinaries()).as("a refused call must not discard the code binaries - "
                        + "nothing repopulates them after startup, so losing them ends the session")
                .isNotEmpty();
        assertThat(dirtyClasses()).as("a refused call must not discard unsaved-change flags")
                .contains(PersonalData.class);
    }

    @Test
    @DisplayName("every entry point taking a ROM refuses null the same way")
    void everyRomEntryPointRefusesNull()
    {
        // one guarded and the rest not is the same bug with a smaller blast radius, and the
        // unguarded one is the one a future caller will reach for
        assertThatThrownBy(() -> DataManager.getData(null, MoveData.class))
                .as("getData").isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataManager.prepareData(null, MoveData.class))
                .as("prepareData").isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataManager.resetData(null, MoveData.class))
                .as("resetData").isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataManager.codeBinarySetup(null))
                .as("codeBinarySetup").isInstanceOf(NullPointerException.class);

        assertThat(dataMap()).containsKeys(PersonalData.class, MoveData.class);
        assertThat(codeBinaries()).isNotEmpty();
        assertThat(dirtyClasses()).contains(PersonalData.class);
    }

    @Test
    @DisplayName("the files a save writes can be listed without preparing anything")
    void theFileListIsAvailableWithoutSideEffects()
    {
        // The save confirmation names the files it is about to write. It used to get that list
        // by preparing the data first - and preparing writes the TM table straight into the
        // shared arm9 buffer, so declining the confirmation left the ROM already modified.
        // Asking the parser what it writes has no side effects, which is what lets both
        // confirmations happen before anything is touched.
        assertThat(DataManager.filesWrittenBy(PersonalData.class))
                .as("a parser must be able to name its outputs without producing them")
                .isNotEmpty();

        assertThat(dataMap()).containsKeys(PersonalData.class, MoveData.class);
        assertThat(dirtyClasses()).contains(PersonalData.class);
    }

    @Test
    @DisplayName("isLoaded answers without parsing")
    void isLoadedIsAQuery()
    {
        assertThat(DataManager.isLoaded(PersonalData.class)).isTrue();

        dataMap().remove(PersonalData.class);
        assertThat(DataManager.isLoaded(PersonalData.class)).isFalse();

        // asking must not have gone and fetched it
        assertThat(dataMap()).doesNotContainKey(PersonalData.class);
    }
}
