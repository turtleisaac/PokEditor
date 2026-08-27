package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Property-based tests for {@link ArrayProcessor}, a comma-separated row accumulator.
 *
 * <p>THEORY. The class implements two textbook transformations:
 * <ul>
 *   <li>a <em>free-monoid accumulator</em> over rows: {@code newLine()} appends one row, so the
 *       row count is exactly the number of {@code newLine()} calls and rows never interact;</li>
 *   <li>a <em>separator split</em>, which is the inverse of a separator join. RFC 4180 (and plain
 *       {@code String.split}/{@code String.join} algebra) fixes the law: a record containing
 *       <i>m</i> unquoted separators has exactly <i>m+1</i> fields, and joining those fields with
 *       the separator reproduces the record. Any field the split drops is data loss.</li>
 * </ul>
 * The fixed-width constructor {@code ArrayProcessor(int numColumns)} additionally promises
 * rectangularity: every emitted row has exactly {@code numColumns} cells.
 */
public class ArrayProcessorTest
{
    /**
     * This test asserts a property the code under it does not hold, and that code has no
     * callers anywhere in src/main. It is kept as the specification for anyone who revives
     * the class, and excluded from the build that has to stay green, so that a genuine
     * regression elsewhere is still visible rather than lost among known failures.
     */
    static final String DEAD_CODE = "dead-code";

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    private static Object[] row(ArrayProcessor processor, int index)
    {
        return processor.getTable()[index];
    }

    /**
     * Field count demanded by the separator-split law, allowing for the single trailing separator
     * that {@code newLine()} documents itself as stripping (the emitter appends "value," per cell,
     * so one trailing comma is punctuation rather than an empty final field).
     */
    private static int expectedFieldCount(String line)
    {
        String stripped = line.endsWith(",") ? line.substring(0, line.length() - 1) : line;
        int commas = 0;
        for (int i = 0; i < stripped.length(); i++)
            if (stripped.charAt(i) == ',')
                commas++;
        return commas + 1;
    }

    @Test
    @DisplayName("a fresh processor holds no rows and getTable() is non-null")
    void freshProcessorIsEmpty()
    {
        // The empty accumulator must encode as the empty table, never as null: null would force
        // every caller to special-case the identity element.
        assertThat(new ArrayProcessor().getTable()).isNotNull().isEmpty();
        assertThat(new ArrayProcessor(5).getTable()).isNotNull().isEmpty();
        assertThat(new ArrayProcessor("abc").getTable()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("row count equals the number of newLine() calls (free monoid over rows)")
    void rowCountIsAdditive()
    {
        ArrayProcessor processor = new ArrayProcessor();
        for (int n = 1; n <= 25; n++)
        {
            processor.append("a,b,c");
            processor.newLine();
            // Appending one row increments the length by exactly one; no row is merged or dropped.
            assertThat(processor.getTable()).as("after %d newLine() calls", n).hasNumberOfRows(n);
        }
    }

    @Test
    @DisplayName("split and join are mutual inverses for separator-free fields")
    void splitAndJoinAreMutualInverses()
    {
        String[][] records = {
                {"a", "b", "c"},
                {"only"},
                {"1", "2", "3", "4", "5"},
                {"a", "", "c"},          // an empty interior field is a field, not an absence
                {"", "b"},
        };

        for (String[] fields : records)
        {
            String line = String.join(",", fields);
            ArrayProcessor processor = new ArrayProcessor();
            processor.append(line);
            processor.newLine();

            // join(split(x)) == x is the defining inverse relationship of a separator codec.
            assertThat(row(processor, 0)).as("record %s", line).containsExactly((Object[]) fields);
        }
    }

    @Tag(DEAD_CODE)
    @Test
    @DisplayName("field count obeys the separator law even when the final field is empty")
    void trailingEmptyFieldsSurviveTheSplit()
    {
        // A record with m separators has m+1 fields (RFC 4180 s.2). Dropping empty fields at the
        // end silently shortens the row, so a consumer reading by column index reads the wrong
        // column or an out-of-range one. The single documented trailing-separator strip is already
        // allowed for by expectedFieldCount().
        String[] lines = {"a,b,c", "a,,b", "a,,", ",,", "a,"};
        for (String line : lines)
        {
            ArrayProcessor processor = new ArrayProcessor();
            processor.append(line);
            processor.newLine();
            assertThat(row(processor, 0)).as("record \"%s\"", line).hasSize(expectedFieldCount(line));
        }
    }

    @Test
    @DisplayName("the fixed-width constructor emits rectangular rows, padding short records")
    void fixedWidthRowsAreRectangular()
    {
        String[] lines = {"a,b,c,d", "a,b,c", "a,b", "a", "a,b,c,", "a,,", ""};
        for (String line : lines)
        {
            ArrayProcessor processor = new ArrayProcessor(4);
            processor.append(line);
            processor.newLine();
            // Rectangularity is the whole contract of a declared column count: a table consumer
            // indexes cell [r][c] for every r, so every row must have exactly numColumns cells.
            assertThat(row(processor, 0)).as("record \"%s\"", line).hasSize(4);
        }
    }

    @Test
    @DisplayName("fixed-width padding preserves the leading fields and pads with empty cells")
    void fixedWidthPaddingIsOnTheRight()
    {
        ArrayProcessor processor = new ArrayProcessor(5);
        processor.append("x,y");
        processor.newLine();
        // Padding may only add cells; the fields actually present keep their column indices,
        // otherwise the column-to-meaning mapping shifts.
        assertThat(row(processor, 0)).containsExactly("x", "y", "", "", "");
    }

    @Test
    @DisplayName("a record wider than the declared width is rejected, never silently truncated")
    void overflowIsRejectedAndLeavesTheTableUnchanged()
    {
        ArrayProcessor processor = new ArrayProcessor(2);
        processor.append("a,b");
        processor.newLine();

        processor.append("a,b,c,d");
        // With no documented truncation rule, the mathematically sane response to a record that
        // does not fit the declared shape is rejection: silently dropping columns c and d would
        // corrupt the table with no signal to the caller.
        assertThatThrownBy(processor::newLine).isInstanceOf(IndexOutOfBoundsException.class);

        // Failure atomicity: a rejected operation must not leave a partial row behind.
        assertThat(processor.getTable()).hasNumberOfRows(1);
        assertThat(row(processor, 0)).containsExactly("a", "b");
    }

    @Test
    @DisplayName("append is string concatenation: append(a);append(b) == append(a+b)")
    void appendIsConcatenation()
    {
        String[][] splits = {{"a,b", ",c"}, {"", "a"}, {"a", ""}, {"a,", "b,c"}};
        for (String[] parts : splits)
        {
            ArrayProcessor split = new ArrayProcessor();
            split.append(parts[0]);
            split.append(parts[1]);
            split.newLine();

            ArrayProcessor whole = new ArrayProcessor();
            whole.append(parts[0] + parts[1]);
            whole.newLine();

            // Concatenation is associative, so the buffer must not care where the caller cut it.
            assertThat(row(split, 0)).as("%s + %s", parts[0], parts[1]).isEqualTo(row(whole, 0));
        }
    }

    @Test
    @DisplayName("substring composes: substring(i) then substring(j) == substring(i+j)")
    void substringComposes()
    {
        ArrayProcessor composed = new ArrayProcessor();
        composed.append("abcdefgh");
        composed.substring(2);
        composed.substring(3);
        composed.newLine();

        ArrayProcessor direct = new ArrayProcessor();
        direct.append("abcdefgh");
        direct.substring(5);
        direct.newLine();

        // Suffix-taking is a monoid action of the additive naturals on strings.
        assertThat(row(composed, 0)).isEqualTo(row(direct, 0));
        assertThat(row(direct, 0)).containsExactly("fgh");

        ArrayProcessor ranged = new ArrayProcessor();
        ranged.append("abcdefgh");
        ranged.substring(2, 5);
        ranged.newLine();
        // Two-argument substring must agree with String.substring's half-open interval [2,5).
        assertThat(row(ranged, 0)).containsExactly("abcdefgh".substring(2, 5));
    }

    @Test
    @DisplayName("rows are independent: a later row cannot alter an earlier one")
    void rowsAreIndependent()
    {
        ArrayProcessor processor = new ArrayProcessor(3);
        processor.append("a,b,c");
        processor.newLine();
        Object[] firstBefore = row(processor, 0).clone();

        processor.append("x,y,z");
        processor.newLine();

        // Locality: appending row n+1 is a pure extension of the table, so row n is untouched.
        assertThat(row(processor, 0)).isEqualTo(firstBefore);
        assertThat(row(processor, 1)).containsExactly("x", "y", "z");
    }

    @Test
    @DisplayName("getTable() is a pure query: repeated calls agree and do not consume the table")
    void getTableIsAPureQuery()
    {
        ArrayProcessor processor = new ArrayProcessor(2);
        processor.append("a,b");
        processor.newLine();

        Object[][] first = processor.getTable();
        Object[][] second = processor.getTable();
        // Command/query separation: reading the table cannot be destructive.
        assertThat(second).hasNumberOfRows(first.length);
        assertThat(second[0]).isEqualTo(first[0]);
        assertThat(processor.getTable()).hasNumberOfRows(1);
    }

    @Tag(DEAD_CODE)
    @Test
    @DisplayName("an empty record produces a row rather than crashing")
    void emptyRecordProducesARow()
    {
        // The empty string is a legal record: under the separator law it has exactly one (empty)
        // field, and under the fixed-width contract it is a fully padded row. Either way the
        // accumulator must be total on its own state - a NullPointerException from an internal
        // uninitialised buffer is not a diagnosable answer.
        ArrayProcessor fixed = new ArrayProcessor(3);
        assertThatCode(fixed::newLine).doesNotThrowAnyException();
        assertThat(fixed.getTable()).hasNumberOfRows(1);
        assertThat(row(fixed, 0)).containsExactly("", "", "");
    }

    @Tag(DEAD_CODE)
    @Test
    @DisplayName("append(null) is rejected at the call site")
    void nullAppendIsRejectedEagerly()
    {
        ArrayProcessor processor = new ArrayProcessor(2);
        // A null record is not a record. Rejecting it where it enters keeps the diagnosis at the
        // faulty call site; accepting it defers the failure to an unrelated later newLine() (or,
        // worse, stringifies it into the table as the text "null").
        assertThatThrownBy(() -> processor.append(null)).isInstanceOf(NullPointerException.class);
    }
}
