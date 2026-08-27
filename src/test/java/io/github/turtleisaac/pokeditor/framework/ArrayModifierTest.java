package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * These helpers reshape sheet data. The defining property of every one of them is that they are
 * total over their input: whatever one row looks like has no bearing on what happens to the next.
 * A ragged row is the normal case for sheet data, not an error, and it must not be able to
 * silence the rows behind it.
 */
class ArrayModifierTest
{
    /**
     * The column of a table is one entry per row, in row order. A short row simply has nothing
     * to contribute at that column - it is not a signal to stop reading.
     * <p>
     * This is the exact shape of the {@code break}-where-{@code continue}-was-meant bug: the
     * first row too short to reach the requested column ended the loop, so every row below it
     * was silently blanked and whatever the caller was populating from that column lost its tail.
     */
    @Test
    @DisplayName("a short row leaves only its own cell blank and does not suppress the rows below it")
    void shortRowDoesNotTruncateTheColumn()
    {
        Object[][] table = {
                {"a0", "a1", "a2"},
                {"b0"},                       // too short to reach column 1
                {"c0", "c1", "c2"},
                {},                           // completely empty
                {"e0", "e1"},
        };

        Object[] column = ArrayModifier.getColumn(table, 1);

        assertThat(column).containsExactly("a1", "", "c1", "", "e1");
    }

    /**
     * The result has one entry per row no matter what, so callers can zip it against the row
     * index they already hold.
     */
    @Test
    @DisplayName("a column has exactly one entry per row, ragged or not")
    void columnHasOneEntryPerRow()
    {
        Object[][] table = {
                {"a"},
                {},
                {"c", "d", "e"},
        };

        for (int col = 0; col < 4; col++)
            assertThat(ArrayModifier.getColumn(table, col)).as("column %d", col).hasSize(table.length);
    }

    /**
     * Every value that exists at the requested column has to come back, whichever row it is in.
     * Stated as a quantifier over the whole table rather than one hand-picked column, so no
     * single early exit can hide inside it.
     */
    @Test
    @DisplayName("every cell present at column c is returned at its own row index")
    void columnReturnsEveryPresentCell()
    {
        Object[][] table = {
                {"r0c0", "r0c1", "r0c2", "r0c3"},
                {"r1c0"},
                {"r2c0", "r2c1"},
                {"r3c0", "r3c1", "r3c2"},
                {"r4c0", "r4c1", "r4c2", "r4c3"},
        };

        for (int col = 0; col < 4; col++)
        {
            Object[] extracted = ArrayModifier.getColumn(table, col);
            for (int row = 0; row < table.length; row++)
            {
                Object expected = col < table[row].length ? table[row][col] : "";
                assertThat(extracted[row]).as("row %d, column %d", row, col).isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("extracting a column does not disturb the table it was read from")
    void getColumnDoesNotMutateItsInput()
    {
        Object[][] table = {{"a", "b"}, {"c"}, {"d", "e"}};

        ArrayModifier.getColumn(table, 1);

        assertThat(table[0]).containsExactly("a", "b");
        assertThat(table[1]).containsExactly("c");
        assertThat(table[2]).containsExactly("d", "e");
    }

    /**
     * trim drops a header block: the first {@code rows} rows and the first {@code cols} cells of
     * every surviving row. Checked against the original coordinates so an off-by-one in either
     * axis is visible.
     */
    @Test
    @DisplayName("trim drops exactly the leading rows and columns asked for and keeps the rest in order")
    void trimRemovesOnlyTheRequestedHeaderBlock()
    {
        Object[][] table = new Object[4][5];
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < 5; col++)
                table[row][col] = row + ":" + col;

        Object[][] trimmed = ArrayModifier.trim(table, 1, 2);

        assertThat(trimmed).hasNumberOfRows(3);
        for (int row = 0; row < trimmed.length; row++)
        {
            assertThat(trimmed[row]).as("trimmed row %d", row).hasSize(3);
            for (int col = 0; col < trimmed[row].length; col++)
                assertThat(trimmed[row][col]).isEqualTo((row + 1) + ":" + (col + 2));
        }
    }

    /**
     * accommodateLength exists so a caller can index the result by position without a bounds
     * check. Two things follow: the length is exactly what was asked for, and no slot is null.
     * Existing entries must survive - a filler that overwrote real names would rename columns.
     */
    @Test
    @DisplayName("accommodateLength reaches the requested length without overwriting the entries already there")
    void accommodateLengthPadsWithoutOverwriting()
    {
        String[] padded = ArrayModifier.accommodateLength(new String[] {"first", "second"}, 5);

        assertThat(padded).hasSize(5);
        assertThat(padded[0]).isEqualTo("first");
        assertThat(padded[1]).isEqualTo("second");
        assertThat(padded).doesNotContainNull();
    }

    @Test
    @DisplayName("accommodateLength to a shorter length keeps the leading entries")
    void accommodateLengthTruncatesFromTheEnd()
    {
        String[] shortened = ArrayModifier.accommodateLength(new String[] {"a", "b", "c", "d"}, 2);

        assertThat(shortened).containsExactly("a", "b");
    }
}
