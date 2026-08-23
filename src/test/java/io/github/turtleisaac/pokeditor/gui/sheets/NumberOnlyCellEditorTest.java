package io.github.turtleisaac.pokeditor.gui.sheets;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.editors.NumberOnlyCellEditor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import java.awt.HeadlessException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A cell editor for a numeric column has two jobs, and they pull against each other: show the
 * value that is already in the cell without altering it, and refuse a value the column cannot
 * hold. Both have failed here in the same direction - quietly. A filter which stripped the minus
 * sign mangled a negative value on the way in, before the user typed anything; a range check
 * which could not fail let an out-of-range value through to be truncated to a byte on save.
 * <p>
 * The column's legal range is the {min, max} pair the sheet's model publishes for that column
 * (see {@code FormatModel.getCellValueRange}); move priority, written to the ROM as a signed
 * byte, is {-128, 127}, and Trick Room's priority is -7.
 */
class NumberOnlyCellEditorTest
{
    private static final int SIGNED_MIN = -128;
    private static final int SIGNED_MAX = 127;

    /** Seeds the editor as the table would when the user starts editing a cell. */
    private static JTextField seed(NumberOnlyCellEditor editor, Object cellValue)
    {
        return (JTextField) editor.getTableCellEditorComponent(null, cellValue, false, 0, 0);
    }

    /**
     * Whether the editor let the edit be committed.
     * <p>
     * Rejection is reported to the user through a modal dialog, which cannot be shown in a
     * headless JVM - so an attempt to open one is itself proof the value was refused. What
     * matters either way is that a refused value never comes back as {@code true}.
     */
    private static boolean commits(NumberOnlyCellEditor editor)
    {
        try {
            return editor.stopCellEditing();
        }
        catch (HeadlessException dialogWouldHaveBeenShown) {
            return false;
        }
    }

    private static int readBack(NumberOnlyCellEditor editor)
    {
        return Integer.parseInt(String.valueOf(editor.getCellEditorValue()).trim());
    }

    /**
     * Opening the editor on a cell must not change the cell. Every value the column admits has
     * to survive the trip in and straight back out - the user has not typed anything yet.
     */
    @Test
    @DisplayName("every value an unsigned column admits comes back unchanged from the editor")
    void seedingAnUnsignedColumnIsLossless()
    {
        for (int value = 0; value <= 255; value++)
        {
            NumberOnlyCellEditor editor = new NumberOnlyCellEditor(0, 255);

            seed(editor, value);
            assertThat(readBack(editor)).as("Integer %d seeded into a 0..255 column", value).isEqualTo(value);

            seed(editor, String.valueOf(value));
            assertThat(readBack(editor)).as("String \"%d\" seeded into a 0..255 column", value).isEqualTo(value);
        }
    }

    /**
     * The same for a signed column - and this is where the editor used to lose data before the
     * user could touch it, because setting the field's text runs through the same filter as
     * typing does, and the filter deleted the minus sign.
     */
    @Test
    @DisplayName("every value a signed column admits, negatives included, comes back unchanged from the editor")
    void seedingASignedColumnIsLossless()
    {
        for (int value = SIGNED_MIN; value <= SIGNED_MAX; value++)
        {
            NumberOnlyCellEditor editor = new NumberOnlyCellEditor(SIGNED_MIN, SIGNED_MAX);

            seed(editor, value);
            assertThat(readBack(editor)).as("Integer %d seeded into a -128..127 column", value).isEqualTo(value);

            seed(editor, String.valueOf(value));
            assertThat(readBack(editor)).as("String \"%d\" seeded into a -128..127 column", value).isEqualTo(value);
        }
    }

    /**
     * The concrete case: opening the priority cell of Trick Room and pressing escape must leave
     * -7 in it, and the field must be showing -7 while it is open, not 7.
     */
    @Test
    @DisplayName("opening a signed cell on a negative value shows that negative value")
    void negativeValueIsDisplayedAsNegative()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor(SIGNED_MIN, SIGNED_MAX);

        JTextField field = seed(editor, "-7");

        assertThat(field.getText()).as("what the user sees when the editor opens on -7").isEqualTo("-7");
        assertThat(readBack(editor)).isEqualTo(-7);
        assertThat(commits(editor)).as("committing an untouched cell holding a legal value").isTrue();
    }

    /**
     * The endpoints of the declared range are legal values, not near misses. An off-by-one that
     * excluded them would make the largest legal stat or the most negative priority unenterable.
     */
    @Test
    @DisplayName("the values at the minimum and the maximum are accepted")
    void boundsThemselvesAreAccepted()
    {
        for (int[] range : new int[][] {{0, 255}, {SIGNED_MIN, SIGNED_MAX}, {0, 65535}})
        {
            for (int value : new int[] {range[0], range[1]})
            {
                NumberOnlyCellEditor editor = new NumberOnlyCellEditor(range[0], range[1]);
                seed(editor, String.valueOf(value));

                assertThat(commits(editor))
                        .as("committing %d in a %d..%d column", value, range[0], range[1])
                        .isTrue();
                assertThat(readBack(editor)).isEqualTo(value);
            }
        }
    }

    /**
     * One past either end is not a legal value, and the editor is the only thing standing between
     * it and a write that truncates it into range. A range test which cannot be false - the
     * classic {@code v >= min || v <= max} - passes everything, so this is stated at both ends,
     * as the property that survives whichever defence catches it: the editor never commits a
     * value outside the range it declares, and never reports one either.
     */
    @Test
    @DisplayName("a value one past the minimum or one past the maximum is never committed")
    void valuesJustOutsideTheRangeAreRefused()
    {
        for (int[] range : new int[][] {{0, 255}, {SIGNED_MIN, SIGNED_MAX}, {0, 65535}})
        {
            for (int value : new int[] {range[0] - 1, range[1] + 1})
            {
                NumberOnlyCellEditor editor = new NumberOnlyCellEditor(range[0], range[1]);
                JTextField field = seed(editor, String.valueOf(range[0]));
                field.setText(String.valueOf(value));

                boolean committed = commits(editor);
                int reported = readBack(editor);

                assertThat(committed && reported == value)
                        .as("committed %d in a %d..%d column", value, range[0], range[1])
                        .isFalse();
                assertThat(reported)
                        .as("value reported by a %d..%d column after being handed %d", range[0], range[1], value)
                        .isBetween(range[0], range[1]);
            }
        }
    }

    /**
     * Refusing a value means the value does not get in - it does not mean quietly folding it into
     * range. 300 in a 0..255 column must not become 44, and 200 in a signed byte column must not
     * become -56, which is exactly what writing the value out as a byte would do.
     */
    @Test
    @DisplayName("an out-of-range value is refused rather than wrapped into range")
    void outOfRangeValueIsNotTruncated()
    {
        NumberOnlyCellEditor unsigned = new NumberOnlyCellEditor(0, 255);
        JTextField unsignedField = seed(unsigned, "10");
        unsignedField.setText("300");

        assertThat(commits(unsigned)).as("committing 300 in a 0..255 column").isFalse();
        assertThat(readBack(unsigned))
                .as("the value the editor hands back after refusing 300 - 300 truncated to a byte is 44")
                .isNotEqualTo(44)
                .isEqualTo(10);

        NumberOnlyCellEditor signed = new NumberOnlyCellEditor(SIGNED_MIN, SIGNED_MAX);
        JTextField signedField = seed(signed, "-7");
        signedField.setText("200");

        assertThat(commits(signed)).as("committing 200 in a -128..127 column").isFalse();
        assertThat(readBack(signed))
                .as("the value the editor hands back after refusing 200 - 200 as a signed byte is -56")
                .isNotEqualTo(-56)
                .isEqualTo(-7);
    }

    /**
     * Allowing the minus sign on signed columns must not have handed it to every column. An
     * unsigned column has no negative values, so one must never be able to reach the field -
     * neither by being typed nor by arriving as the cell's own value.
     */
    @Test
    @DisplayName("an unsigned column never lets a minus sign into the field")
    void unsignedColumnRejectsTheMinusSign()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor(0, 255);

        JTextField field = seed(editor, "-5");
        assertThat(field.getText()).as("an unsigned column seeded with -5").doesNotContain("-");

        seed(editor, "12");
        field.setText("-12");
        assertThat(field.getText()).as("an unsigned column asked to hold -12").doesNotContain("-");

        seed(editor, "12");
        field.setText("1-2");
        assertThat(field.getText()).as("a minus sign in the middle of a number").doesNotContain("-");
    }

    /**
     * A signed column takes the minus sign only where a minus sign belongs. "1-2" is not a number
     * in any column.
     */
    @Test
    @DisplayName("a signed column takes a leading minus sign but not one in the middle of a number")
    void signedColumnTakesOnlyALeadingMinus()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor(SIGNED_MIN, SIGNED_MAX);

        JTextField field = seed(editor, "0");
        field.setText("-42");
        assertThat(field.getText()).isEqualTo("-42");

        field.setText("1-2");
        assertThat(field.getText()).as("a minus sign in the middle of a number").doesNotContain("-");
    }

    /**
     * Letters are not numbers, and a cell left holding nothing is not zero. Either way the edit
     * must be refused rather than committed as some arbitrary value.
     */
    @Test
    @DisplayName("a cell left empty or containing no digits is refused")
    void nonNumericInputIsRefused()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor(0, 255);

        JTextField field = seed(editor, "7");
        field.setText("");
        assertThat(commits(editor)).as("committing an empty cell").isFalse();

        seed(editor, "7");
        field.setText("abc");
        assertThat(commits(editor)).as("committing letters").isFalse();
    }

    /**
     * The editor is reused across cells, so it must not carry the previous cell's text into a
     * cell which has no value - that would silently write the old cell's number into the new one.
     */
    @Test
    @DisplayName("opening the editor on a blank cell does not leave the previous cell's text behind")
    void blankCellDoesNotInheritThePreviousValue()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor(0, 255);

        seed(editor, "123");
        JTextField field = seed(editor, null);

        assertThat(field.getText()).isEmpty();
    }

    /** The default column is a single unsigned byte, and it enforces that range like any other. */
    @Test
    @DisplayName("the default editor enforces the unsigned byte range it declares")
    void defaultEditorEnforcesItsDeclaredRange()
    {
        NumberOnlyCellEditor editor = new NumberOnlyCellEditor();

        assertThat(editor.getMinimum()).isEqualTo(NumberOnlyCellEditor.DEFAULT_MINIMUM);
        assertThat(editor.getMaximum()).isEqualTo(NumberOnlyCellEditor.DEFAULT_MAXIMUM);

        JTextField field = seed(editor, "0");
        assertThat(commits(editor)).as("committing the minimum").isTrue();

        field.setText("256");
        assertThat(commits(editor)).as("committing one past the maximum").isFalse();
    }
}
