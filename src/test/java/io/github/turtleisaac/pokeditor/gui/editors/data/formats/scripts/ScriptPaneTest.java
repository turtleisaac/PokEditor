package io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts;

import io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.Fixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;

import static io.github.turtleisaac.pokeditor.gui.editors.data.formats.scripts.ScriptTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * The pane's own arithmetic: which line an offset is on, and which characters get restyled when a
 * label is followed. Both are places a +1 has historically crept in - the pane used to style
 * {@code max - min + 1} characters, painting one character beyond every label it highlighted.
 */
class ScriptPaneTest
{
    private static final String SCRIPT = "WaitTime 5 0\nEnd\nGoTo _0001\nEnd\n";

    private Fixture wiredFixture() throws BadLocationException
    {
        Fixture fixture = newQuiescedFixture();
        fixture.pane().setScriptDocument(fixture.document());
        fixture.document().insertString(0, SCRIPT, null);
        fixture.document().setSyntaxAttributes();
        return fixture;
    }

    @Test
    @DisplayName("the first line of a document is line 1, never line 0")
    void lineNumbersAreOneBased()
    {
        JTextPane pane = new JTextPane();
        pane.setText("alpha\nbeta\ngamma");

        assertThat(ScriptPane.getLineAtOffset(pane, 0)).isEqualTo(1);
        assertThat(ScriptPane.getLineAtOffset(pane, 4)).isEqualTo(1);
        assertThat(ScriptPane.getLineAtOffset(pane, 6)).isEqualTo(2);
        assertThat(ScriptPane.getLineAtOffset(pane, 11)).isEqualTo(3);
    }

    @Test
    @DisplayName("the line reported for an offset is the line the newlines before it put it on")
    void lineNumberAgreesWithTheNewlinesBeforeTheOffset()
    {
        String text = "a\nbb\n\nccc\ndddd";
        JTextPane pane = new JTextPane();
        pane.setText(text);

        for (int offset = 0; offset < text.length(); offset++)
        {
            int newlinesBefore = 0;
            for (int i = 0; i < offset; i++)
            {
                if (text.charAt(i) == '\n')
                    newlinesBefore++;
            }

            assertThat(ScriptPane.getLineAtOffset(pane, offset))
                    .as("offset %d follows %d newlines", offset, newlinesBefore)
                    .isEqualTo(newlinesBefore + 1);
        }
    }

    @Test
    @DisplayName("going to line n leaves the caret on line n")
    void gotoStartOfLineIsTheInverseOfGetLineAtOffset()
    {
        JTextPane pane = new JTextPane();
        pane.setText("alpha\nbeta\ngamma\ndelta");

        for (int line = 1; line <= 4; line++)
        {
            ScriptPane.gotoStartOfLine(pane, line);
            assertThat(ScriptPane.getLineAtCaret(pane)).as("after going to line %d", line).isEqualTo(line);
            assertThat(ScriptPane.getLineAtOffset(pane, pane.getCaretPosition())).isEqualTo(line);
        }
    }

    @Test
    @DisplayName("going to line 1 puts the caret at offset 0")
    void gotoFirstLine()
    {
        JTextPane pane = new JTextPane();
        pane.setText("alpha\nbeta");

        ScriptPane.gotoStartOfLine(pane, 1);

        assertThat(pane.getCaretPosition()).isZero();
    }

    @Test
    @DisplayName("going to a line outside the document clamps to a real line instead of throwing")
    void gotoLineOutsideTheDocumentIsClamped()
    {
        JTextPane pane = new JTextPane();
        pane.setText("alpha\nbeta\ngamma");

        assertThatCode(() -> ScriptPane.gotoStartOfLine(pane, 0)).doesNotThrowAnyException();
        assertThat(ScriptPane.getLineAtCaret(pane)).isEqualTo(1);

        assertThatCode(() -> ScriptPane.gotoStartOfLine(pane, 9_999)).doesNotThrowAnyException();
        assertThat(ScriptPane.getLineAtCaret(pane)).isEqualTo(3);

        assertThatCode(() -> ScriptPane.gotoStartOfLine(pane, -5)).doesNotThrowAnyException();
        assertThat(ScriptPane.getLineAtCaret(pane)).isEqualTo(1);
    }

    @Test
    @DisplayName("following a label restyles exactly the label's own characters")
    void highlightStylesExactlyTheRangeAndNoMore() throws BadLocationException
    {
        // The label element covers "_0001" in "GoTo _0001". Styling min..maxExclusive-1 is the
        // whole range; styling one more would bleed the goto styling onto the following newline,
        // and styling one fewer would leave the label's last character behind.
        Fixture fixture = wiredFixture();
        ScriptDocument document = fixture.document();

        int min = SCRIPT.indexOf("_0001");
        int maxExclusive = min + "_0001".length();
        document.getScriptElementList().add(
                document.new ElementRange(min, maxExclusive, "label", ScriptDocument.ElementType.LABEL));

        paneHighlight(fixture.pane(), min + 1);

        for (int offset = min; offset < maxExclusive; offset++)
        {
            assertThat(StyleConstants.isUnderline(document.getCharacterElement(offset).getAttributes()))
                    .as("character %d ('%s') is inside the label and must be styled",
                            offset, SCRIPT.charAt(offset))
                    .isTrue();
        }

        assertThat(StyleConstants.isUnderline(document.getCharacterElement(min - 1).getAttributes()))
                .as("the character before the label must not be styled")
                .isFalse();
        assertThat(StyleConstants.isUnderline(document.getCharacterElement(maxExclusive).getAttributes()))
                .as("the character after the label must not be styled")
                .isFalse();
    }

    @Test
    @DisplayName("following a label leaves the document's characters untouched")
    void highlightDoesNotChangeTheText() throws BadLocationException
    {
        Fixture fixture = wiredFixture();
        ScriptDocument document = fixture.document();

        int min = SCRIPT.indexOf("_0001");
        document.getScriptElementList().add(
                document.new ElementRange(min, min + 5, "label", ScriptDocument.ElementType.LABEL));

        paneHighlight(fixture.pane(), min);

        assertThat(textOf(document)).isEqualTo(SCRIPT);
    }

    @Test
    @DisplayName("a pane with no script document answers tooltip queries with null instead of throwing")
    void tooltipWithoutADocument()
    {
        ScriptPane pane = new ScriptPane();

        assertThat(pane.getScriptDocument()).isNull();
        assertThatCode(() -> paneHighlight(pane, 0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("installing a gutter empties it and hands it back")
    void installingAGutter() throws BadLocationException
    {
        ScriptPane pane = new ScriptPane();
        JTextPane gutter = new JTextPane();
        gutter.setText("stale\ncontent\n");

        pane.setLineNumberPane(gutter);

        assertThat(pane.getLineNumberPane()).isSameAs(gutter);
        assertThat(gutter.getDocument().getLength()).isZero();
        assertThat(gutter.isEditable()).isFalse();
    }

    @Test
    @DisplayName("the pane reports back the script document installed on it")
    void installingAScriptDocument()
    {
        Fixture fixture = newQuiescedFixture();

        fixture.pane().setScriptDocument(fixture.document());

        assertThat(fixture.pane().getScriptDocument()).isSameAs(fixture.document());
        assertThat(fixture.pane().getStyledDocument()).isSameAs(fixture.document());
    }
}
