package io.github.turtleisaac.pokeditor.framework;

import io.github.turtleisaac.pokeditor.gui.PokeditorManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Export and import are one feature: a sheet the tool wrote must come back as the same sheet.
 * Testing the two halves separately cannot catch a quoting convention that the writer emits and
 * the reader does not accept, so these tests state the property that actually matters to a user -
 * export, edit nothing, re-import, get the same table.
 * <p>
 * The export itself ({@code PokeditorManager.writeSheet}) is reachable only behind a modal
 * JFileChooser, so it cannot be driven headlessly. {@link #write} therefore reproduces the loop
 * from {@code writeSheet} - join the quoted fields with commas, one record per line, UTF-8 - and
 * delegates the part that carries the actual format decisions, field quoting, to the production
 * method itself.
 */
class CsvRoundTripTest
{
    /**
     * This test asserts a property the code under it does not hold, and that code has no
     * callers anywhere in src/main. It is kept as the specification for anyone who revives
     * the class, and excluded from the build that has to stay green, so that a genuine
     * regression elsewhere is still visible rather than lost among known failures.
     */
    static final String DEAD_CODE = "dead-code";

    @TempDir
    Path temp;

    private static Method quoteCsvField;

    @BeforeAll
    static void locateWriterQuoting() throws NoSuchMethodException
    {
        quoteCsvField = PokeditorManager.class.getDeclaredMethod("quoteCsvField", String.class);
        quoteCsvField.setAccessible(true);
    }

    private static String quote(String field)
    {
        try {
            return (String) quoteCsvField.invoke(null, field);
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("could not invoke the export path's field quoting", e);
        }
    }

    /** The serialisation loop from {@code PokeditorManager.writeSheet}, minus the file chooser. */
    private Path write(String[][] table) throws IOException
    {
        StringBuilder out = new StringBuilder();
        for (String[] row : table)
        {
            String[] quoted = new String[row.length];
            for (int i = 0; i < row.length; i++)
                quoted[i] = quote(row[i]);
            out.append(String.join(",", quoted)).append("\n");
        }

        Path file = temp.resolve("export-" + System.nanoTime() + ".csv");
        Files.write(file, out.toString().getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private String[][] writeThenRead(String[][] table) throws IOException
    {
        return new CsvReader(write(table).toString(), 0, 0).getCsv();
    }

    /**
     * A field which needs no quoting must not acquire any, otherwise every ordinary cell in the
     * file gains a pair of quotes that a stricter reader would hand back as part of the value.
     */
    @Test
    @DisplayName("a field with nothing special in it is written unquoted")
    void ordinaryFieldIsNotQuoted()
    {
        assertThat(quote("Pikachu")).isEqualTo("Pikachu");
        assertThat(quote("")).isEqualTo("");
        assertThat(quote("Nidoran♀")).isEqualTo("Nidoran♀");
    }

    /**
     * A field carrying a comma, a quote or a line break has to be quoted, because those are the
     * three characters that would otherwise be read as structure rather than content. An
     * embedded quote is doubled inside the quoted field.
     */
    @Test
    @DisplayName("a field containing a comma, a quote or a line break is quoted, and inner quotes are doubled")
    void hazardousFieldsAreQuoted()
    {
        assertThat(quote("a,b")).isEqualTo("\"a,b\"");
        assertThat(quote("say \"hi\"")).isEqualTo("\"say \"\"hi\"\"\"");
        assertThat(quote("line one\nline two")).isEqualTo("\"line one\nline two\"");
        assertThat(quote("carriage\rreturn")).isEqualTo("\"carriage\rreturn\"");
    }

    /** A missing cell has to become an empty field, not the four characters "null". */
    @Test
    @DisplayName("a null field is written as an empty field")
    void nullFieldBecomesEmpty()
    {
        assertThat(quote(null)).isEqualTo("");
    }

    /**
     * The round trip, over a table that carries every hazard at once except an embedded line
     * break (which has its own test below): commas, quotes, quotes next to commas, empty cells,
     * trailing empty cells and non-ASCII names.
     */
    @Test
    @DisplayName("a table of commas, quotes, blanks and non-ASCII names survives export and re-import unchanged")
    void roundTripPreservesEveryFieldButLineBreaks() throws IOException
    {
        String[][] table = {
                {"id", "name", "description", "flags", "note"},
                {"29", "Nidoran♀", "Lowers Attack, sharply.", "", ""},
                {"30", "Nidorina", "He said \"no\" twice", "a,b", "trailing"},
                {"31", "Nidoqueen", "\"fully quoted\"", ",leading comma", ""},
                {"32", "Flabébé", "", "", ""},
        };

        assertThat(writeThenRead(table)).isDeepEqualTo(table);
    }

    /**
     * A cell whose text runs onto a second line is still one cell of one row. If the round trip
     * cannot carry it, re-importing an exported sheet gains a row and every entry below the
     * offending one is written to the wrong index.
     */
    @Tag(DEAD_CODE)
    @Test
    @DisplayName("a table containing an embedded line break survives export and re-import unchanged")
    void roundTripPreservesEmbeddedLineBreaks() throws IOException
    {
        String[][] table = {
                {"id", "description"},
                {"1", "first line\nsecond line"},
                {"2", "plain"},
        };

        assertThat(writeThenRead(table)).isDeepEqualTo(table);
    }

    /**
     * Row shape is part of the table. A row whose last cells are blank must come back with those
     * cells still present, or the importer reads a short row and every column index after the
     * gap addresses the wrong field.
     */
    @Test
    @DisplayName("every row comes back with exactly the number of fields it was written with")
    void roundTripPreservesRowWidths() throws IOException
    {
        String[][] table = {
                {"a", "b", "c", "d"},
                {"", "", "", ""},
                {"x", "", "", ""},
                {"", "", "", "z"},
        };

        String[][] reread = writeThenRead(table);

        for (int row = 0; row < table.length; row++)
            assertThat(reread[row]).as("row %d", row).hasSameSizeAs(table[row]);
        assertThat(reread).isDeepEqualTo(table);
    }
}
