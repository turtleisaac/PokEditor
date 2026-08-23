package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.swing.text.BadLocationException;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.newQuiescedDocument;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link ScriptDocument.ElementRange} is a half-open interval [min, maxExclusive) over document
 * offsets. Every syntax colour, every tooltip and every ctrl-click target is expressed as one, so
 * an off-by-one in this class is an off-by-one everywhere in the editor at once. These tests state
 * the definition of a half-open interval and nothing else.
 */
class ElementRangeTest
{
    private ScriptDocument document;

    @BeforeEach
    void setUp()
    {
        // ElementRange is an inner class; it needs an enclosing document, but none of the interval
        // arithmetic below depends on that document's contents.
        document = newQuiescedDocument();
    }

    @ParameterizedTest(name = "[{0},{1})")
    @CsvSource({"0, 1", "0, 5", "3, 4", "7, 20", "100, 101"})
    @DisplayName("min is inside the range and min - 1 is outside it")
    void lowerBoundIsInclusive(int min, int maxExclusive)
    {
        ScriptDocument.ElementRange range = document.new ElementRange(min, maxExclusive, null);

        assertThat(range.contains(min))
                .as("%d is the first offset of [%d,%d)", min, min, maxExclusive)
                .isTrue();
        assertThat(range.contains(min - 1))
                .as("%d is before [%d,%d)", min - 1, min, maxExclusive)
                .isFalse();
    }

    @ParameterizedTest(name = "[{0},{1})")
    @CsvSource({"0, 1", "0, 5", "3, 4", "7, 20", "100, 101"})
    @DisplayName("maxExclusive - 1 is the last offset inside the range and maxExclusive is outside it")
    void upperBoundIsExclusive(int min, int maxExclusive)
    {
        ScriptDocument.ElementRange range = document.new ElementRange(min, maxExclusive, null);

        // This is the assertion that the historical `value < maxExclusive - 1` bug broke: it
        // dropped the final character of every token, so the last letter of a command never got
        // its tooltip and never got styled with the rest of the word.
        assertThat(range.contains(maxExclusive - 1))
                .as("%d is the last offset of [%d,%d)", maxExclusive - 1, min, maxExclusive)
                .isTrue();
        assertThat(range.contains(maxExclusive))
                .as("%d is one past the end of [%d,%d)", maxExclusive, min, maxExclusive)
                .isFalse();
    }

    @ParameterizedTest(name = "[{0},{1})")
    @CsvSource({"0, 1", "0, 5", "3, 4", "7, 20", "100, 101"})
    @DisplayName("every offset from min to maxExclusive - 1 inclusive is contained, and no other")
    void containsExactlyItsOwnOffsets(int min, int maxExclusive)
    {
        ScriptDocument.ElementRange range = document.new ElementRange(min, maxExclusive, null);

        for (int offset = Math.max(0, min - 3); offset < maxExclusive + 3; offset++)
        {
            boolean expected = offset >= min && offset < maxExclusive;
            assertThat(range.contains(offset))
                    .as("[%d,%d).contains(%d)", min, maxExclusive, offset)
                    .isEqualTo(expected);
        }
    }

    @ParameterizedTest(name = "[{0},{1}) has length {2}")
    @CsvSource({"0, 1, 1", "0, 5, 5", "3, 4, 1", "7, 20, 13", "100, 101, 1"})
    @DisplayName("length is exactly maxExclusive - min")
    void lengthIsTheWidthOfTheInterval(int min, int maxExclusive, int expectedLength)
    {
        // The document styles `getLength()` characters starting at `getMin()`. If length were
        // maxExclusive - min + 1 the highlighter would paint one character beyond every token -
        // which is exactly the bug ScriptPane used to carry.
        assertThat(document.new ElementRange(min, maxExclusive, null).getLength())
                .isEqualTo(expectedLength);
    }

    @Test
    @DisplayName("length counts exactly the offsets the range contains")
    void lengthAgreesWithContains()
    {
        ScriptDocument.ElementRange range = document.new ElementRange(4, 11, null);

        int contained = 0;
        for (int offset = 0; offset < 40; offset++)
        {
            if (range.contains(offset))
                contained++;
        }

        assertThat(contained).isEqualTo(range.getLength());
    }

    @ParameterizedTest(name = "[{0},{1})")
    @CsvSource({"0, 0", "5, 5", "7, 3", "0, -1"})
    @DisplayName("a range that contains nothing cannot be constructed at all")
    void emptyOrInvertedRangesAreRejected(int min, int maxExclusive)
    {
        // A zero-width range contains no offset, so styling or tooltipping it is meaningless.
        // The class enforces that by refusing to exist, which is a stronger guarantee than
        // "contains() returns false" - it means no such range can ever reach the range set.
        assertThatThrownBy(() -> document.new ElementRange(min, maxExclusive, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("must be greater than");
    }

    @Test
    @DisplayName("a range encloses itself and any sub-range of itself")
    void containsNestedRanges()
    {
        ScriptDocument.ElementRange outer = document.new ElementRange(10, 20, null);

        assertThat(outer.contains(document.new ElementRange(10, 20, null)))
                .as("a range encloses itself").isTrue();
        assertThat(outer.contains(document.new ElementRange(12, 15, null)))
                .as("strictly inside").isTrue();
        assertThat(outer.contains(document.new ElementRange(10, 15, null)))
                .as("flush with the start").isTrue();
        assertThat(outer.contains(document.new ElementRange(15, 20, null)))
                .as("flush with the end").isTrue();
    }

    @Test
    @DisplayName("a range does not enclose a range that crosses or exceeds its bounds")
    void doesNotContainCrossingRanges()
    {
        ScriptDocument.ElementRange outer = document.new ElementRange(10, 20, null);

        assertThat(outer.contains(document.new ElementRange(5, 15, null)))
                .as("overlaps the start").isFalse();
        assertThat(outer.contains(document.new ElementRange(15, 25, null)))
                .as("overlaps the end").isFalse();
        assertThat(outer.contains(document.new ElementRange(5, 25, null)))
                .as("strictly larger").isFalse();
        assertThat(outer.contains(document.new ElementRange(20, 30, null)))
                .as("starts where this one ends").isFalse();
        assertThat(outer.contains(document.new ElementRange(1, 9, null)))
                .as("entirely before").isFalse();
    }

    @Test
    @DisplayName("a range's string form is exactly the document text it covers")
    void toStringIsTheCoveredText() throws BadLocationException
    {
        // toString() is how a range identifies itself in a debugger, a log line or an error
        // message; if it reports text the range does not cover, every such report misleads.
        document.insertString(0, "WaitTime 5 0\nEnd\n", null);

        ScriptDocument.ElementRange range = document.new ElementRange(0, 8, null);

        assertThat(range.toString()).isEqualTo("WaitTime");
    }

    @Test
    @DisplayName("a range that reaches the end of the document has a string form")
    void toStringAtEndOfDocument() throws BadLocationException
    {
        document.insertString(0, "End", null);

        ScriptDocument.ElementRange range = document.new ElementRange(0, document.getLength(), null);

        assertThat(range.toString()).isEqualTo("End");
    }
}
