package io.github.turtleisaac.pokeditor;

import io.github.turtleisaac.pokeditor.formats.GenericFileData;
import io.github.turtleisaac.pokeditor.formats.evolutions.EvolutionData;
import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.formats.moves.MoveData;
import io.github.turtleisaac.pokeditor.formats.personal.PersonalData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Property-based tests for {@link DataManager}'s dirty-tracking subsystem.
 *
 * <p>THEORY. Dirty tracking is a finite set with two commands and one query:
 * {@code markDirty} is set insertion, {@code markClean} is set removal, {@code hasUnsavedChanges}
 * is the emptiness test. The laws follow from set algebra:
 * <ul>
 *   <li><b>Idempotence.</b> {@code S u {x} u {x} == S u {x}} - marking twice is marking once, so
 *       one {@code markClean} always suffices. A counter-based implementation would violate this
 *       and strand data as permanently dirty.</li>
 *   <li><b>Locality.</b> Inserting or removing <i>x</i> leaves the membership of every <i>y != x</i>
 *       unchanged. Losing this is the data-loss bug: a type the user edited stops being reported
 *       because an unrelated type was saved.</li>
 *   <li><b>Command/query separation.</b> The query is pure: asking whether there are unsaved
 *       changes cannot clear them. This is the same law the learnsets defect broke.</li>
 * </ul>
 *
 * <p>NOTE ON TESTABILITY. The state is {@code private static final} with no public reset, so the
 * suite has to clear it reflectively in {@code @BeforeEach}; and there is no per-class query, so
 * membership of a single class is established behaviourally (mark it, clean everything else, ask).
 */
public class DataManagerDirtyStateTest
{
    private static final Class<? extends GenericFileData> A = PersonalData.class;
    private static final Class<? extends GenericFileData> B = LearnsetData.class;
    private static final Class<? extends GenericFileData> C = MoveData.class;
    private static final Class<? extends GenericFileData> D = EvolutionData.class;

    private static final List<Class<? extends GenericFileData>> ALL = List.of(A, B, C, D);

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void resetStaticState() throws Exception
    {
        Field field = DataManager.class.getDeclaredField("dirtyClasses");
        field.setAccessible(true);
        ((Set<Class<? extends GenericFileData>>) field.get(null)).clear();
        // Precondition for every test below: the shared static state starts empty.
        assertThat(DataManager.hasUnsavedChanges()).isFalse();
    }

    /**
     * Behavioural membership test, since no per-class query exists: temporarily clean every other
     * known class and ask the global query, then restore the set exactly as it was found.
     */
    private static boolean isDirty(Class<? extends GenericFileData> target)
    {
        Set<Class<? extends GenericFileData>> snapshot = new HashSet<>(currentSet());
        for (Class<? extends GenericFileData> other : ALL)
        {
            if (other != target)
                DataManager.markClean(other);
        }
        boolean result = DataManager.hasUnsavedChanges();
        for (Class<? extends GenericFileData> restored : snapshot)
            DataManager.markDirty(restored);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<? extends GenericFileData>> currentSet()
    {
        try
        {
            Field field = DataManager.class.getDeclaredField("dirtyClasses");
            field.setAccessible(true);
            return (Set<Class<? extends GenericFileData>>) field.get(null);
        }
        catch (ReflectiveOperationException e)
        {
            throw new AssertionError("dirty-tracking state is not reachable for verification", e);
        }
    }

    @Test
    @DisplayName("marking a type makes it dirty; the empty set reports no unsaved changes")
    void markingMakesDirty()
    {
        assertThat(DataManager.hasUnsavedChanges()).isFalse();
        DataManager.markDirty(A);
        // Insertion into an empty set makes it non-empty: the user is now at risk of losing work.
        assertThat(DataManager.hasUnsavedChanges()).isTrue();
        assertThat(isDirty(A)).isTrue();
    }

    @Test
    @DisplayName("marking one type does not mark another (locality of insertion)")
    void markingIsLocal()
    {
        DataManager.markDirty(A);
        // Set insertion touches exactly one element; B was never edited, so it must not be dirty.
        assertThat(isDirty(B)).isFalse();
        assertThat(isDirty(C)).isFalse();
        assertThat(isDirty(A)).isTrue();
    }

    @Test
    @DisplayName("marking is idempotent: two marks need only one clean")
    void markingIsIdempotent()
    {
        DataManager.markDirty(A);
        DataManager.markDirty(A);
        DataManager.markDirty(A);
        DataManager.markClean(A);
        // S u {x} u {x} == S u {x}: a counting implementation would leave A dirty forever, so the
        // user would be prompted about changes that no longer exist and could never clear them.
        assertThat(DataManager.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("cleaning one type clears only that type (locality of removal)")
    void cleaningIsLocal()
    {
        DataManager.markDirty(A);
        DataManager.markDirty(B);

        DataManager.markClean(A);
        // Removing A may not remove B: silently forgetting B is exactly how an edited file gets
        // dropped without a save prompt.
        assertThat(DataManager.hasUnsavedChanges()).isTrue();
        assertThat(isDirty(B)).isTrue();
        assertThat(isDirty(A)).isFalse();

        DataManager.markClean(B);
        assertThat(DataManager.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("cleaning a type that was never dirty is a no-op, not a reset")
    void cleaningAnUnmarkedTypeIsANoOp()
    {
        DataManager.markDirty(A);
        DataManager.markClean(B);
        DataManager.markClean(C);
        // Removing an absent element from a set leaves the set unchanged.
        assertThat(isDirty(A)).isTrue();
    }

    @Test
    @DisplayName("the query is pure: asking about unsaved changes never clears them")
    void queryIsPure()
    {
        DataManager.markDirty(A);
        for (int i = 0; i < 5; i++)
            assertThat(DataManager.hasUnsavedChanges()).as("query #%d", i).isTrue();
        // Command/query separation: repeated observation is stable, so a confirmation dialog that
        // asks twice cannot lose the answer between the two calls.
        assertThat(isDirty(A)).isTrue();
    }

    @Test
    @DisplayName("a marked type stays dirty until it is explicitly cleaned - the safety property")
    void markedTypesSurviveEveryOtherOperation()
    {
        DataManager.markDirty(A);

        DataManager.markDirty(B);
        DataManager.markDirty(B);
        DataManager.markClean(B);
        DataManager.markClean(C);
        DataManager.markDirty(null);
        DataManager.hasUnsavedChanges();
        DataManager.markDirty(D);
        DataManager.markClean(D);

        // No read, no query and no operation on another type may clear A: the whole purpose of the
        // subsystem is that an edit the user made is never silently dropped on save/exit.
        assertThat(isDirty(A)).isTrue();
        assertThat(DataManager.hasUnsavedChanges()).isTrue();
    }

    @Test
    @DisplayName("markDirty(null) is a no-op and does not corrupt the set")
    void nullIsANoOp()
    {
        // The implementation documents null as ignored; the requirement either way is that it
        // cannot poison the set - a null member would make every later query unreliable.
        assertThatCode(() -> DataManager.markDirty(null)).doesNotThrowAnyException();
        assertThat(DataManager.hasUnsavedChanges()).isFalse();

        DataManager.markDirty(A);
        DataManager.markDirty(null);
        assertThatCode(() -> DataManager.markClean(null)).doesNotThrowAnyException();
        assertThat(isDirty(A)).isTrue();
        DataManager.markClean(A);
        assertThat(DataManager.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("model check: the tracker agrees with a plain HashSet after every operation")
    void agreesWithASetModelUnderARandomOperationSequence()
    {
        Random random = new Random(20260823L);
        Set<Class<? extends GenericFileData>> model = new HashSet<>();
        List<String> history = new ArrayList<>();

        for (int step = 0; step < 400; step++)
        {
            Class<? extends GenericFileData> target = ALL.get(random.nextInt(ALL.size()));
            boolean mark = random.nextBoolean();
            if (mark)
            {
                DataManager.markDirty(target);
                model.add(target);
            }
            else
            {
                DataManager.markClean(target);
                model.remove(target);
            }
            history.add((mark ? "markDirty(" : "markClean(") + target.getSimpleName() + ")");

            // The tracker is a set; refinement against the reference implementation must hold at
            // every step, not merely at the end of the sequence.
            assertThat(DataManager.hasUnsavedChanges())
                    .as("after step %d %s; history=%s", step, history.get(step), history)
                    .isEqualTo(!model.isEmpty());
            assertThat(currentSet())
                    .as("membership after step %d %s", step, history.get(step))
                    .containsExactlyInAnyOrderElementsOf(model);
        }
    }
}
