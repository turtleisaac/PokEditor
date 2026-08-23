package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * CSV is a defined format (RFC 4180), so these are conformance tests, not descriptions of the
 * parser. The failures they guard all have the same consequence for a spreadsheet: a field is
 * split, merged or dropped, every column after it shifts by one, and the wrong numbers get
 * written into the ROM without anything looking obviously wrong on screen.
 */
class CsvReaderTest
{
    @TempDir
    Path temp;

    private String[][] parse(String content) throws IOException
    {
        return parse(content, 0, 0);
    }

    private String[][] parse(String content, int firstX, int firstY) throws IOException
    {
        Path file = temp.resolve("sheet-" + System.nanoTime() + ".csv");
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return new CsvReader(file.toString(), firstX, firstY).getCsv();
    }

    /**
     * A comma inside a quoted field belongs to the field. Move descriptions and item names
     * routinely contain one, and splitting there silently shifts every later column.
     */
    @Test
    @DisplayName("a comma inside a quoted field is part of the field, not a separator")
    void quotedCommaDoesNotSplitTheField() throws IOException
    {
        assertThat(parse("Growl,\"Lowers the target's Attack, sharply.\",40\n")[0])
                .containsExactly("Growl", "Lowers the target's Attack, sharply.", "40");
    }

    /**
     * Inside a quoted field, two double quotes stand for one literal double quote, and the
     * quotes that delimit the field are not part of it.
     */
    @Test
    @DisplayName("a doubled quote inside a quoted field reads back as one literal quote")
    void doubledQuoteReadsAsOneQuote() throws IOException
    {
        assertThat(parse("a,\"say \"\"hi\"\" now\",b\n")[0])
                .containsExactly("a", "say \"hi\" now", "b");
    }

    /**
     * Trailing empty fields are fields. {@code "a,b,,".split(",")} yields two elements, so a row
     * whose last columns are blank comes back short - and because sheet columns are read
     * positionally, every consumer downstream reads the wrong column or falls off the end.
     */
    @Test
    @DisplayName("trailing empty fields are preserved rather than dropped")
    void trailingEmptyFieldsSurvive() throws IOException
    {
        assertThat(parse("a,b,,\n")[0]).containsExactly("a", "b", "", "");
        assertThat(parse(",,,\n")[0]).containsExactly("", "", "", "");
        assertThat(parse("a,\n")[0]).containsExactly("a", "");
    }

    @Test
    @DisplayName("empty fields in the middle of a row are preserved in place")
    void interiorEmptyFieldsKeepTheirPosition() throws IOException
    {
        assertThat(parse("a,,c,,e\n")[0]).containsExactly("a", "", "c", "", "e");
    }

    /**
     * The exported files hold Pokemon names, and several of them are not ASCII. Reading (or
     * writing) with the platform charset turns Nidoran-female into "Nidoran?", which then gets
     * saved back over the real name.
     */
    @Test
    @DisplayName("non-ASCII text survives being read back")
    void nonAsciiSurvives() throws IOException
    {
        assertThat(parse("Nidoran♀,Nidoran♂,Flabébé,ピカチュウ\n")[0])
                .containsExactly("Nidoran♀", "Nidoran♂", "Flabébé", "ピカチュウ");
    }

    /**
     * A byte order mark is a byte order mark, not the first character of the first cell. A sheet
     * exported by Excel starts with one, and leaving it attached makes the first header cell
     * fail to match anything.
     */
    @Test
    @DisplayName("a UTF-8 byte order mark is not treated as part of the first field")
    void byteOrderMarkIsStripped() throws IOException
    {
        assertThat(parse("﻿id,name\n")[0]).containsExactly("id", "name");
    }

    /**
     * A field which contains a line break is one field of one record - RFC 4180 allows it inside
     * quotes precisely so that multi-line text can be carried. Reading record-by-record as
     * physical lines tears such a field in half and turns one row into two.
     */
    @Test
    @DisplayName("a line break inside a quoted field stays inside that field")
    void quotedLineBreakDoesNotStartANewRecord() throws IOException
    {
        String[][] parsed = parse("a,\"line one\nline two\",b\n");

        assertThat(parsed).as("a file holding a single record").hasNumberOfRows(1);
        assertThat(parsed[0]).containsExactly("a", "line one\nline two", "b");
    }

    @Test
    @DisplayName("records are returned in file order, one per row")
    void recordsKeepFileOrder() throws IOException
    {
        String[][] parsed = parse("r0a,r0b\nr1a,r1b\nr2a,r2b\n");

        assertThat(parsed).hasNumberOfRows(3);
        assertThat(parsed[0]).containsExactly("r0a", "r0b");
        assertThat(parsed[1]).containsExactly("r1a", "r1b");
        assertThat(parsed[2]).containsExactly("r2a", "r2b");
    }

    /**
     * next() walks the records once and then reports exhaustion, so a caller looping until null
     * terminates rather than reading past the end.
     */
    @Test
    @DisplayName("next walks every record once and then reports exhaustion")
    void nextWalksEveryRecordThenReturnsNull() throws IOException
    {
        Path file = temp.resolve("walk.csv");
        Files.write(file, "a,b\nc,d\n".getBytes(StandardCharsets.UTF_8));
        CsvReader reader = new CsvReader(file.toString(), 0, 0);

        assertThat(reader.length()).isEqualTo(2);
        assertThat(reader.next()).containsExactly("a", "b");
        assertThat(reader.next()).containsExactly("c", "d");
        assertThat(reader.next()).isNull();
    }

    /**
     * The firstX/firstY arguments drop a header block. What is left has to be the rest of the
     * grid, unshifted - an off-by-one in either axis silently re-labels every column.
     */
    @Test
    @DisplayName("firstX and firstY drop exactly the header rows and columns requested")
    void headerRowsAndColumnsAreDropped() throws IOException
    {
        String[][] parsed = parse("h0,h1,h2,h3\nx,y,a,b\nx,y,c,d\n", 2, 1);

        assertThat(parsed).hasNumberOfRows(2);
        assertThat(parsed[0]).containsExactly("a", "b");
        assertThat(parsed[1]).containsExactly("c", "d");
    }

    /**
     * A table is allowed to be wider than it is tall - a one row sheet with twenty columns is a
     * perfectly ordinary export. Sizing a per-column array by the number of rows and then
     * indexing it by column number throws the moment that stops being true.
     */
    @Test
    @DisplayName("a table with more columns than rows can be printed without throwing")
    void moreColumnsThanRowsDoesNotThrow() throws IOException
    {
        Path file = temp.resolve("wide.csv");
        Files.write(file, "a,b,c,d,e,f,g,h\n".getBytes(StandardCharsets.UTF_8));
        CsvReader reader = new CsvReader(file.toString(), 0, 0);

        assertThatCode(() -> printQuietly(reader)).doesNotThrowAnyException();
    }

    /**
     * Ragged rows are the normal case once trailing empties are preserved, so the same
     * per-column bookkeeping has to cope with rows of different widths.
     */
    @Test
    @DisplayName("a ragged table can be printed without throwing")
    void raggedTableDoesNotThrow() throws IOException
    {
        Path file = temp.resolve("ragged.csv");
        Files.write(file, "a\na,b,c,d,e\na,b\n".getBytes(StandardCharsets.UTF_8));
        CsvReader reader = new CsvReader(file.toString(), 0, 0);

        assertThatCode(() -> printQuietly(reader)).doesNotThrowAnyException();
    }

    /** print() writes to stdout; the test only cares whether it survives the table's shape. */
    private static void printQuietly(CsvReader reader)
    {
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
        try {
            reader.print();
        }
        finally {
            System.setOut(original);
        }
    }
}
