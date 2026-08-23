package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A column must refuse a value it cannot store.
 * <p>
 * The property is that {@code getCellValueRange} is a promise, not a hint: if a column says
 * it holds 0..511, then every value in that range must be storable and every value outside
 * it must be refused. A range that is merely advisory is worse than none, because the write
 * path narrows silently - a move of 512 was written back as move 0, which reads as "learns
 * nothing", and the user's only clue was the wrong data on reload.
 * <p>
 * These tests drive {@link FormatModel#prepareObjectForWriting(Object, CellTypes, int[])}
 * directly, because that is the one point every write funnels through: a cell editor, a
 * paste, and a programmatic write all reach it, whereas the editor-side check reached only
 * values typed into an INTEGER cell.
 */
class ValueRangeEnforcementTest
{
    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    /** the smallest thing that can answer prepareObjectForWriting; no Core types involved */
    private static final FormatModel<?, ?> MODEL = new E_RangeModel();

    private static Object prepare(Object value, CellTypes type, int[] range)
    {
        return MODEL.prepareObjectForWriting(value, type, range);
    }

    @Nested
    @DisplayName("numeric columns")
    class Numeric
    {
        @Test
        void everyValueInsideTheDeclaredRangeIsAccepted()
        {
            // the range is a promise about what can be stored. if a value inside it is
            // refused, the sheet is offering the user something it cannot deliver
            int[] range = {0, 511};
            for (int i = range[0]; i <= range[1]; i++)
                assertThat(prepare(i, CellTypes.INTEGER, range)).isEqualTo(i);
        }

        @Test
        void theBoundsThemselvesAreAccepted()
        {
            // inclusive means inclusive; an exclusive comparison shows up here first
            assertThatCode(() -> {
                prepare(0, CellTypes.INTEGER, new int[] {0, 511});
                prepare(511, CellTypes.INTEGER, new int[] {0, 511});
                prepare(-128, CellTypes.INTEGER, new int[] {-128, 127});
                prepare(127, CellTypes.INTEGER, new int[] {-128, 127});
            }).doesNotThrowAnyException();
        }

        @Test
        void oneStepOutsideEitherBoundIsRefused()
        {
            int[] range = {0, 511};
            assertThatThrownBy(() -> prepare(512, CellTypes.INTEGER, range))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> prepare(-1, CellTypes.INTEGER, range))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void aSignedColumnRefusesTheUnsignedValueThatWouldFit()
        {
            // move priority is a signed byte: 200 fits in eight bits but is not a legal
            // priority, and writing it stores -56. the range is what distinguishes them
            assertThatThrownBy(() -> prepare(200, CellTypes.INTEGER, new int[] {-128, 127}))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void theRefusalNamesTheValueAndTheRange()
        {
            // a message missing either one cannot be acted on
            assertThatThrownBy(() -> prepare(512, CellTypes.INTEGER, new int[] {0, 511}))
                    .hasMessageContaining("512")
                    .hasMessageContaining("511");
        }

        @Test
        void aStringIsCheckedAgainstTheRangeToo()
        {
            // a paste arrives as text, and the paste path is precisely the one that had no
            // check at all - converting without then checking would fix nothing
            assertThatThrownBy(() -> prepare("512", CellTypes.INTEGER, new int[] {0, 511}))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThat(prepare("511", CellTypes.INTEGER, new int[] {0, 511})).isEqualTo(511);
        }

        @Test
        void combinationColumnsAreCheckedNotJustIntegerOnes()
        {
            // COMBO_BOX and BITFIELD columns never had an editor-side range check, so they
            // are the ones this change actually protects
            for (CellTypes type : new CellTypes[] {CellTypes.COMBO_BOX, CellTypes.COLORED_COMBO_BOX,
                    CellTypes.BITFIELD_COMBO_BOX, CellTypes.INTEGER})
            {
                assertThatThrownBy(() -> prepare(999, type, new int[] {0, 18}))
                        .as("%s must honour its declared range", type)
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }

        @Test
        void aNullRangeSkipsTheCheckRatherThanFailing()
        {
            // columns where no numeric range applies must still be writable
            assertThatCode(() -> prepare(99999, CellTypes.INTEGER, null)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("non-numeric text in a numeric column")
    class NonNumeric
    {
        @Test
        void aNameIsRefusedWithAMessageThatExplainsWhatTheColumnWants()
        {
            // the sheet exports rendered text, so an exported column is full of names. pasting
            // it back in hits every combo box cell, and parseInt's own message ("For input
            // string") does not tell the user what to do about it
            assertThatThrownBy(() -> prepare("Bulbasaur", CellTypes.COMBO_BOX, new int[] {0, 511}))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bulbasaur")
                    .hasMessageContaining("number");
        }

        @Test
        void theOriginalParseFailureIsKeptAsTheCause()
        {
            // rewriting a message must not destroy the diagnosis underneath it
            assertThatThrownBy(() -> prepare("12abc", CellTypes.INTEGER, new int[] {0, 511}))
                    .hasCauseInstanceOf(NumberFormatException.class);
        }

        @Test
        void aStringColumnStillTakesText()
        {
            // names are stored as text and must not be run through the number parser
            assertThat(prepare("Bulbasaur", CellTypes.STRING, null)).isEqualTo("Bulbasaur");
        }
    }

    @Nested
    @DisplayName("checkbox columns")
    class Checkbox
    {
        @Test
        void theSpreadsheetSpellingsAreUnderstood()
        {
            // Boolean.parseBoolean answers false for anything it does not recognise, so a
            // pasted column of 1s and 0s silently cleared every box it touched. 1/0 is what a
            // spreadsheet actually produces, so it has to be understood, not guessed at
            assertThat(prepare("1", CellTypes.CHECKBOX, null)).isEqualTo(true);
            assertThat(prepare("0", CellTypes.CHECKBOX, null)).isEqualTo(false);
            assertThat(prepare("true", CellTypes.CHECKBOX, null)).isEqualTo(true);
            assertThat(prepare("false", CellTypes.CHECKBOX, null)).isEqualTo(false);
            assertThat(prepare("TRUE", CellTypes.CHECKBOX, null)).isEqualTo(true);
            assertThat(prepare("Yes", CellTypes.CHECKBOX, null)).isEqualTo(true);
            assertThat(prepare("no", CellTypes.CHECKBOX, null)).isEqualTo(false);
        }

        @Test
        void surroundingSpaceDoesNotChangeTheAnswer()
        {
            // a spreadsheet paste routinely carries it
            assertThat(prepare("  1  ", CellTypes.CHECKBOX, null)).isEqualTo(true);
        }

        @Test
        void somethingThatIsNotAYesOrNoIsRefusedRatherThanReadAsFalse()
        {
            // this is the whole point: an unrecognised value must not quietly become false,
            // because "every checkbox in the column is now clear" is indistinguishable from a
            // deliberate edit once it has happened
            assertThatThrownBy(() -> prepare("Bulbasaur", CellTypes.CHECKBOX, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Bulbasaur");
            assertThatThrownBy(() -> prepare("2", CellTypes.CHECKBOX, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void aBooleanPassesThroughUntouched()
        {
            assertThat(prepare(true, CellTypes.CHECKBOX, null)).isEqualTo(true);
            assertThat(prepare(false, CellTypes.CHECKBOX, null)).isEqualTo(false);
        }
    }

    /** a FormatModel with no columns; only prepareObjectForWriting is exercised */
    private static class E_RangeModel extends FormatModel<io.github.turtleisaac.pokeditor.formats.GenericFileData, CellTypes>
    {
        E_RangeModel()
        {
            super(java.util.List.of(), java.util.List.of());
        }

        @Override
        public String getColumnNameKey(int columnIndex)
        {
            return null;
        }

        @Override
        public int getColumnCount()
        {
            return 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            return null;
        }

        @Override
        public FormatModel<io.github.turtleisaac.pokeditor.formats.GenericFileData, CellTypes> getFrozenColumnModel()
        {
            return null;
        }

        @Override
        public Object getValueFor(int rowIdx, CellTypes property)
        {
            return null;
        }

        @Override
        public void setValueFor(Object aValue, int rowIdx, CellTypes property)
        {
        }
    }
}
