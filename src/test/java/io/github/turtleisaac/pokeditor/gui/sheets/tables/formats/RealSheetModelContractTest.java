package io.github.turtleisaac.pokeditor.gui.sheets.tables.formats;

import io.github.turtleisaac.pokeditor.formats.learnsets.LearnsetData;
import io.github.turtleisaac.pokeditor.gui.sheets.tables.FormatModelContract;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The same contract, pointed at the real sheet models.
 * <p>
 * Each model is built directly rather than through its {@code DefaultTable}, because the table
 * constructors need real move/type/item name banks pulled out of a ROM. The model is the part
 * which owns the properties being asserted here, so that is the part which is constructed.
 */
class RealSheetModelContractTest
{
    private static final int ROWS = 4;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    // =============================================================== learnsets

    @Nested
    @DisplayName("the level-up learnsets sheet")
    class Learnsets
    {
        /** every row is an empty, correctly terminated learnset - the whole grid is tail */
        private LearnsetsTable.LearnsetsModel emptyModel()
        {
            return new LearnsetsTable.LearnsetsModel(SheetFixtures.emptyLearnsets(ROWS), SheetFixtures.textBanks(ROWS));
        }

        private LearnsetsTable.LearnsetsModel populatedModel()
        {
            return new LearnsetsTable.LearnsetsModel(SheetFixtures.learnsets(ROWS, 3), SheetFixtures.textBanks(ROWS));
        }

        @Test
        @DisplayName("painting the sheet does not add moves to a Pokemon that has none")
        void readingAnEmptyLearnsetDoesNotFillItIn()
        {
            // this is the learnsets bug: getValueFor used to pad the learnset out to whichever
            // column was being painted, so scrolling injected move 0 at level 0 into every
            // species, and those junk entries serialise ahead of the 0xFFFF terminator
            FormatModelContract.assertReadsArePure(emptyModel());
        }

        @Test
        @DisplayName("painting a sheet of populated learnsets does not change them either")
        void readingAPopulatedLearnsetIsPure()
        {
            FormatModelContract.assertReadsArePure(populatedModel());
        }

        @Test
        @DisplayName("the entry a learnset column refers to does not depend on which cells were painted first")
        void readsAreIdempotent()
        {
            FormatModelContract.assertReadsAreIdempotent(populatedModel());
        }

        @Test
        @DisplayName("every cell of the declared learnsets grid can be painted")
        void everyCellIsReadable()
        {
            FormatModelContract.assertEveryCellIsReadable(emptyModel());
            FormatModelContract.assertEveryCellIsReadable(populatedModel());
        }

        @Test
        @DisplayName("editing one move of one learnset leaves every other cell alone")
        void writesAreLocal()
        {
            FormatModelContract.assertWritesAreLocal(populatedModel(), 1, 0, 25);
            FormatModelContract.assertWritesAreLocal(populatedModel(), 2, 3, 40);
            FormatModelContract.assertWritesAreLocal(populatedModel(), 0, 5, 100);
        }

        @Test
        @DisplayName("a move typed into a learnset is the move the sheet then shows")
        void writesRoundTrip()
        {
            FormatModelContract.assertWriteRoundTrips(populatedModel(), 1, 0, 25);
            FormatModelContract.assertWriteRoundTrips(populatedModel(), 1, 1, 33);
            FormatModelContract.assertWriteRoundTrips(populatedModel(), 3, 5, 12);
        }

        @Test
        @DisplayName("the first unused slot of a learnset can be filled in")
        void theFirstEmptySlotIsWritable()
        {
            // the boundary: the row's list has to grow by exactly one to reach the slot being
            // written. A bound of '>' rather than '>=' stops growing one entry short and then
            // indexes past the end, so the very first move anyone adds to a species throws.
            FormatModelContract.assertWriteRoundTrips(emptyModel(), 0, 0, 20);

            // and the same boundary partway along a learnset which already has entries
            LearnsetsTable.LearnsetsModel model = populatedModel();
            List<LearnsetData> data = model.getData();
            int firstFreeColumn = data.get(1).size() * 2;
            FormatModelContract.assertWriteRoundTrips(model, 1, firstFreeColumn, 77);
        }

        @Test
        @DisplayName("filling in an unused learnset slot lengthens that Pokemon's learnset and nobody else's")
        void growingOneRowDoesNotTouchTheOthers()
        {
            List<LearnsetData> data = SheetFixtures.learnsets(ROWS, 3);
            LearnsetsTable.LearnsetsModel model = new LearnsetsTable.LearnsetsModel(data, SheetFixtures.textBanks(ROWS));

            model.setValueAt(90, 1, 6); // the fourth move slot of row 1

            assertThat(data.get(1)).as("the edited row gained exactly the slot that was written").hasSize(4);
            for (int row = 0; row < ROWS; row++)
            {
                if (row == 1)
                    continue;
                assertThat(data.get(row))
                        .as("row %d was not edited and must not have grown", row)
                        .hasSize(3);
            }
        }

        @Test
        @DisplayName("the learnsets sheet can store every move id and level it advertises, and refuses the rest")
        void valueRangesAreHonest()
        {
            FormatModelContract.assertValueRangesAreHonest(emptyModel());
        }
    }

    // =============================================================== personal

    @Nested
    @DisplayName("the personal sheet")
    class Personal
    {
        private PersonalTable.PersonalModel model()
        {
            return new PersonalTable.PersonalModel(SheetFixtures.personals(ROWS), SheetFixtures.textBanks(ROWS));
        }

        @Test
        @DisplayName("painting the personal sheet does not change any Pokemon's stats")
        void readsArePure()
        {
            FormatModelContract.assertReadsArePure(model());
        }

        @Test
        @DisplayName("a personal cell reports the same value however much of the sheet is painted around it")
        void readsAreIdempotent()
        {
            FormatModelContract.assertReadsAreIdempotent(model());
        }

        @Test
        @DisplayName("every cell of the declared personal grid can be painted")
        void everyCellIsReadable()
        {
            FormatModelContract.assertEveryCellIsReadable(model());
        }

        @Test
        @DisplayName("editing one base stat leaves every other cell alone")
        void writesAreLocal()
        {
            FormatModelContract.assertWritesAreLocal(model(), 1, 0, 120);   // HP
            FormatModelContract.assertWritesAreLocal(model(), 2, 10, 2);    // HP EV yield
            FormatModelContract.assertWritesAreLocal(model(), 0, 28, true); // flip
            FormatModelContract.assertWritesAreLocal(model(), 3, -1, "Renamed");
        }

        @Test
        @DisplayName("a base stat typed into the personal sheet is the value it then shows")
        void writesRoundTrip()
        {
            FormatModelContract.assertWriteRoundTrips(model(), 1, 0, 120);
            FormatModelContract.assertWriteRoundTrips(model(), 1, 5, "77");
            FormatModelContract.assertWriteRoundTrips(model(), 2, 28, true);
            FormatModelContract.assertWriteRoundTrips(model(), 3, -1, "Renamed");
        }

        @Test
        @DisplayName("the personal sheet can store every value it advertises, and refuses the rest")
        void valueRangesAreHonest()
        {
            FormatModelContract.assertValueRangesAreHonest(model());
        }
    }

    // =============================================================== moves

    @Nested
    @DisplayName("the moves sheet")
    class Moves
    {
        private MovesTable.MovesModel model()
        {
            return new MovesTable.MovesModel(SheetFixtures.moves(ROWS), SheetFixtures.textBanks(ROWS));
        }

        @Test
        @DisplayName("painting the moves sheet does not change any move")
        void readsArePure()
        {
            FormatModelContract.assertReadsArePure(model());
        }

        @Test
        @DisplayName("a move cell reports the same value however much of the sheet is painted around it")
        void readsAreIdempotent()
        {
            FormatModelContract.assertReadsAreIdempotent(model());
        }

        @Test
        @DisplayName("every cell of the declared moves grid can be painted")
        void everyCellIsReadable()
        {
            FormatModelContract.assertEveryCellIsReadable(model());
        }

        @Test
        @DisplayName("editing one move's power leaves every other cell alone")
        void writesAreLocal()
        {
            FormatModelContract.assertWritesAreLocal(model(), 1, 2, 90);   // power
            FormatModelContract.assertWritesAreLocal(model(), 2, 9, true); // makes contact
            FormatModelContract.assertWritesAreLocal(model(), 0, -1, "Renamed");
        }

        @Test
        @DisplayName("a value typed into the moves sheet is the value it then shows")
        void writesRoundTrip()
        {
            FormatModelContract.assertWriteRoundTrips(model(), 1, 2, 90);
            FormatModelContract.assertWriteRoundTrips(model(), 1, 8, -6);  // priority, which is signed
            FormatModelContract.assertWriteRoundTrips(model(), 3, 16, true);
        }

        @Test
        @DisplayName("the moves sheet can store every value it advertises, and refuses the rest")
        void valueRangesAreHonest()
        {
            FormatModelContract.assertValueRangesAreHonest(model());
        }
    }

    // =============================================================== evolutions

    @Nested
    @DisplayName("the evolutions sheet")
    class Evolutions
    {
        private EvolutionsTable.EvolutionsModel model()
        {
            return new EvolutionsTable.EvolutionsModel(SheetFixtures.evolutionRows(ROWS, 1), SheetFixtures.textBanks(ROWS));
        }

        @Test
        @DisplayName("painting the evolutions sheet does not add evolutions to a Pokemon that has none")
        void readsArePure()
        {
            FormatModelContract.assertReadsArePure(model());
        }

        @Test
        @DisplayName("an evolution cell reports the same value however much of the sheet is painted around it")
        void readsAreIdempotent()
        {
            FormatModelContract.assertReadsAreIdempotent(model());
        }

        @Test
        @DisplayName("every cell of the declared evolutions grid can be painted")
        void everyCellIsReadable()
        {
            FormatModelContract.assertEveryCellIsReadable(model());
        }

        @Test
        @DisplayName("editing one evolution method leaves every other cell alone")
        void writesAreLocal()
        {
            FormatModelContract.assertWritesAreLocal(model(), 1, 0, 4);
            FormatModelContract.assertWritesAreLocal(model(), 2, 2, 25);
        }

        @Test
        @DisplayName("the first unused evolution slot can be filled in")
        void writesRoundTrip()
        {
            FormatModelContract.assertWriteRoundTrips(model(), 1, 0, 4);
            FormatModelContract.assertWriteRoundTrips(model(), 1, 3, 5); // the second evolution slot
        }

        @Test
        @DisplayName("the evolutions sheet can store every value it advertises, and refuses the rest")
        void valueRangesAreHonest()
        {
            FormatModelContract.assertValueRangesAreHonest(model());
        }
    }

    // =============================================================== tm compatibility

    @Nested
    @DisplayName("the TM compatibility sheet")
    class TmCompatibility
    {
        private TmCompatibilityTable.TmCompatibilityModel model()
        {
            return new TmCompatibilityTable.TmCompatibilityModel(SheetFixtures.personals(ROWS), SheetFixtures.textBanks(ROWS));
        }

        @Test
        @DisplayName("painting the TM sheet does not change any Pokemon's TM flags")
        void readsArePure()
        {
            FormatModelContract.assertReadsArePure(model());
        }

        @Test
        @DisplayName("a TM checkbox reports the same value however much of the sheet is painted around it")
        void readsAreIdempotent()
        {
            FormatModelContract.assertReadsAreIdempotent(model());
        }

        @Test
        @DisplayName("every cell of the declared TM grid can be painted")
        void everyCellIsReadable()
        {
            FormatModelContract.assertEveryCellIsReadable(model());
        }

        @Test
        @DisplayName("ticking one TM leaves every other TM of every Pokemon alone")
        void writesAreLocal()
        {
            FormatModelContract.assertWritesAreLocal(model(), 1, 0, true);
            FormatModelContract.assertWritesAreLocal(model(), 2, 63, true);
            FormatModelContract.assertWritesAreLocal(model(), 0, 99, true);
        }

        @Test
        @DisplayName("a ticked TM stays ticked")
        void writesRoundTrip()
        {
            FormatModelContract.assertWriteRoundTrips(model(), 1, 0, true);
            FormatModelContract.assertWriteRoundTrips(model(), 3, 99, "true");
        }

        @Test
        @DisplayName("the TM sheet declares a well formed value range for every column")
        void valueRangesAreHonest()
        {
            FormatModelContract.assertValueRangesAreHonest(model());
        }
    }
}
