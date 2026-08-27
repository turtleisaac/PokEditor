package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.Interval;
import io.github.turtleisaac.variabletracker.ScriptVariable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.text.BadLocationException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Properties of the range set a highlighting pass produces.
 *
 * <p>The range set is consumed by {@link ScriptPane} on every mouse move and every ctrl-click, and
 * it is fed straight back into {@code setCharacterAttributes} and {@code String.substring}. So the
 * things asserted here - ranges lie inside the document, ranges do not fight over a character,
 * running the highlighter again changes nothing - are the preconditions of not crashing while the
 * user types.</p>
 */
class ScriptDocumentHighlightingTest
{
    /** Fixtures chosen to exercise different visitor branches, not to be valid Pokemon scripts. */
    static Stream<String> scriptFixtures()
    {
        return Stream.of(
                "",
                "\n",
                "End\n",
                "End",
                "WaitTime 5 0\nEnd\n",
                "_0001:\n\tEnd\n",
                "_0001:\n\tEnd\n_0001:\n\tEnd\n",
                "script(1):\n\tEnd\n",
                "script(0):\n\tEnd\n",
                "script():\n\tEnd\n",
                "Bogus 1\nEnd\n",
                "End 1 2 3\n",
                "WaitTime\n",
                "\t\t\n   \n",
                "endTable\n",
                "Overworld(1)\nEnd\n"
        );
    }

    private static ScriptDocument highlighted(String text)
    {
        ScriptDocument document = newQuiescedDocument();
        try
        {
            document.insertString(0, text, null);
            document.setSyntaxAttributes();
        }
        catch (BadLocationException e)
        {
            throw new AssertionError("highlighting a freshly inserted document must be legal", e);
        }
        return document;
    }

    static void assertRangesLieInsideDocument(ScriptDocument document)
    {
        int length = document.getLength();
        for (Interval interval : intervalsOf(document))
        {
            assertThat(interval.min())
                    .as("%s starts before the document", interval)
                    .isGreaterThanOrEqualTo(0);
            assertThat(interval.min())
                    .as("%s is empty or inverted", interval)
                    .isLessThan(interval.maxExclusive());
            assertThat(interval.maxExclusive())
                    .as("%s ends past the end of a %d character document", interval, length)
                    .isLessThanOrEqualTo(length);
        }
    }

    static void assertRangesDoNotCross(ScriptDocument document)
    {
        List<Interval> intervals = intervalsOf(document);
        for (int i = 0; i < intervals.size(); i++)
        {
            for (int j = i + 1; j < intervals.size(); j++)
            {
                Interval a = intervals.get(i);
                Interval b = intervals.get(j);
                assertThat(a.disjointFrom(b) || a.encloses(b) || b.encloses(a))
                        .as("%s and %s overlap without one containing the other, so the characters "
                                + "they share belong to two elements at once", a, b)
                        .isTrue();
            }
        }
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("every range produced lies inside the document")
    void rangesLieInsideTheDocument(String text)
    {
        // A range that ends past getLength() is a BadLocationException the next time anything
        // styles it or slices the text with it.
        assertRangesLieInsideDocument(highlighted(text));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("no two ranges partially overlap: they are disjoint or one encloses the other")
    void rangesDoNotPartiallyOverlap(String text)
    {
        // Nesting is deliberate - a parameter's tooltip sits inside its command's - and the lookup
        // resolves it by returning the innermost. Ranges that merely cross have no such resolution:
        // whichever style was written last silently wins on the shared characters.
        assertRangesDoNotCross(highlighted(text));
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("the element under an offset is the smallest element covering it")
    void lookupReturnsTheSmallestCoveringRange(String text)
    {
        ScriptDocument document = highlighted(text);
        List<Interval> intervals = intervalsOf(document);

        for (int offset = 0; offset < document.getLength(); offset++)
        {
            ScriptDocument.ElementRange found = document.getScriptElementList().find(offset);
            int smallest = Integer.MAX_VALUE;
            for (Interval interval : intervals)
            {
                if (offset >= interval.min() && offset < interval.maxExclusive())
                    smallest = Math.min(smallest, interval.length());
            }

            if (smallest == Integer.MAX_VALUE)
            {
                assertThat(found).as("no range covers offset %d", offset).isNull();
            }
            else
            {
                assertThat(found).as("some range covers offset %d", offset).isNotNull();
                assertThat(found.getLength())
                        .as("offset %d is covered by a %d character element but lookup returned a "
                                + "%d character one", offset, smallest, found.getLength())
                        .isEqualTo(smallest);
            }
        }
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("highlighting an unchanged document twice produces the same ranges")
    void highlightingIsIdempotent(String text) throws BadLocationException
    {
        // A highlighter whose output depends on how many times it has run is carrying state across
        // passes, and the pass count is driven by the user's typing speed.
        ScriptDocument document = highlighted(text);
        List<Interval> first = intervalsOf(document);

        document.setSyntaxAttributes();
        List<Interval> second = intervalsOf(document);

        document.setSyntaxAttributes();
        List<Interval> third = intervalsOf(document);

        assertThat(second).isEqualTo(first);
        assertThat(third).isEqualTo(first);
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("highlighting leaves the document's characters untouched")
    void highlightingDoesNotChangeTheText(String text) throws BadLocationException
    {
        ScriptDocument document = newQuiescedDocument();
        document.insertString(0, text, null);

        document.setSyntaxAttributes();
        assertThat(textOf(document)).isEqualTo(text);

        document.setSyntaxAttributes();
        assertThat(textOf(document)).isEqualTo(text);
    }

    @ParameterizedTest(name = "[{0}]")
    @MethodSource("scriptFixtures")
    @DisplayName("querying a document changes neither its text nor its ranges")
    void queryingDoesNotMutate(String text)
    {
        // Command/query separation: reading a tooltip while the mouse moves must not be able to
        // disturb what a later highlighting pass sees.
        ScriptDocument document = highlighted(text);
        String textBefore = textOf(document);
        List<Interval> rangesBefore = intervalsOf(document);

        for (int offset = -1; offset <= document.getLength() + 1; offset++)
            document.getScriptElementList().find(offset);
        document.getStyle("command");
        document.getStyle("label");
        document.getScriptElementList();
        textOf(document);

        assertThat(textOf(document)).isEqualTo(textBefore);
        assertThat(intervalsOf(document)).isEqualTo(rangesBefore);
    }

    @Test
    @DisplayName("a variable parameter nests inside its command instead of crossing it")
    void variableParameterNestsInsideItsCommand() throws BadLocationException
    {
        ScriptDocument document = newQuiescedDocument();
        document.setVariableList(List.of(new ScriptVariable("MyVar", 0x4000)));
        document.insertString(0, "WaitTime MyVar 0\nEnd\n", null);
        document.setSyntaxAttributes();

        assertRangesLieInsideDocument(document);
        assertRangesDoNotCross(document);

        // The narrower parameter element must be what a hover at its offsets resolves to.
        ScriptDocument.ElementRange atParameter = document.getScriptElementList().find(10);
        assertThat(atParameter).isNotNull();
        assertThat(atParameter.getLength()).isEqualTo("MyVar".length());
    }

    @Test
    @DisplayName("text is preserved exactly, including non-ASCII and tokenizer-significant characters")
    void textIsPreservedExactly() throws BadLocationException
    {
        String text = "End\n\t'quoted' \"double\" {braces} \\backslash\\ ; comment\n"
                + "é中😀 café\n:()@#$%^&*\n";

        ScriptDocument document = newQuiescedDocument();
        document.insertString(0, text, null);

        assertThat(document.getLength()).isEqualTo(text.length());
        assertThat(textOf(document)).isEqualTo(text);

        document.setSyntaxAttributes();

        assertThat(textOf(document)).isEqualTo(text);
        assertRangesLieInsideDocument(document);
        assertRangesDoNotCross(document);
    }

    @Test
    @DisplayName("text inserted piece by piece is preserved exactly")
    void textAccumulatedPieceByPieceIsPreserved() throws BadLocationException
    {
        ScriptDocument document = newQuiescedDocument();
        StringBuilder expected = new StringBuilder();

        for (String piece : new String[]{"End", "\n", "\tWaitTime", " 5", " 0", "\n", "café\n"})
        {
            document.insertString(document.getLength(), piece, null);
            expected.append(piece);
            document.setSyntaxAttributes();
            assertThat(textOf(document)).isEqualTo(expected.toString());
        }
    }

    // ---------------------------------------------------------------------------------------
    // Tokenizer termination. A tokenizer that fails to advance does not fail loudly - it spins
    // on the EDT and the whole application stops repainting, which is the worst failure mode a
    // text editor has.
    // ---------------------------------------------------------------------------------------

    static Stream<Arguments> adversarialInputs()
    {
        StringBuilder tenThousandLines = new StringBuilder();
        for (int i = 0; i < 10_000; i++)
            tenThousandLines.append("WaitTime 5 0\n");

        return Stream.of(
                Arguments.of("one very long token", "A".repeat(50_000)),
                Arguments.of("one very long number", "1".repeat(50_000)),
                Arguments.of("unterminated quote", "End 'abc\nEnd\n"),
                Arguments.of("only an opening quote", "'"),
                Arguments.of("unbalanced braces", "{".repeat(5_000)),
                Arguments.of("mismatched braces", "}{}{}}{".repeat(1_000)),
                Arguments.of("unbalanced parentheses", "script(".repeat(2_000)),
                Arguments.of("two unterminated script headers", "script(script("),
                Arguments.of("only whitespace", " \t\n".repeat(5_000)),
                Arguments.of("a lone backslash", "\\"),
                Arguments.of("many backslashes", "\\".repeat(5_000)),
                Arguments.of("characters with no rule", "@#$%^&~`|".repeat(3_000)),
                Arguments.of("a lone colon", ":"),
                Arguments.of("nothing but colons", ":".repeat(5_000)),
                Arguments.of("a NUL character", "\u0000End\n"),
                Arguments.of("ten thousand lines", tenThousandLines.toString())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("adversarialInputs")
    @DisplayName("highlighting terminates and does not throw on adversarial input")
    void highlightingTerminates(String description, String text)
    {
        ScriptDocument document = assertTimeoutPreemptively(Duration.ofSeconds(30),
                () -> highlighted(text),
                () -> "highlighting did not finish for " + description);

        assertThat(textOf(document)).isEqualTo(text);
        assertRangesLieInsideDocument(document);
        assertRangesDoNotCross(document);
    }
}
