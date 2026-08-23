package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class CellTypesTest
{
    /**
     * The one cell type deliberately left to the JTable defaults: a STRING column needs no
     * special editor or renderer. Every other constant must be dispatched explicitly.
     */
    private static final Set<CellTypes> INTENTIONALLY_UNDISPATCHED = EnumSet.of(CellTypes.STRING);

    private static final String DISPATCHER = "src/main/java/io/github/turtleisaac/pokeditor/gui/sheets/tables/"
            + "DefaultTable.java";

    @Test
    @DisplayName("every cell type is dispatched to an editor and renderer, or is explicitly a default-rendered type")
    void everyCellTypeIsDispatched() throws IOException
    {
        // The dispatch is a chain of if/else comparisons, not a switch, so the compiler never
        // checks it for exhaustiveness. A constant added without a matching branch silently
        // falls through and that column gets the plain JTable editor - a species column that
        // accepts free text, a flag column with no checkbox. Reaching the dispatch at runtime
        // means constructing the whole data stack, so the coverage is checked on the source.
        String dispatch = methodBody(Files.readString(dispatcherSource(), StandardCharsets.UTF_8), "loadCellRenderers");

        List<CellTypes> undispatched = new ArrayList<>();
        for (CellTypes type : CellTypes.values())
        {
            if (!dispatch.contains("CellTypes." + type.name()))
                undispatched.add(type);
        }

        assertThat(undispatched)
                .as("cell types with no branch in DefaultTable.loadCellRenderers")
                .containsExactlyInAnyOrderElementsOf(INTENTIONALLY_UNDISPATCHED);
    }

    @Test
    @DisplayName("the dispatcher wires up every renderer and editor the cells package provides")
    void everyCellComponentIsWiredUp() throws IOException
    {
        // A column type is only as correct as the pair it is given. A renderer or editor that
        // exists but is never wired up means some column is silently falling back to the plain
        // JTable default - a flag column with no checkbox, or an index column showing raw numbers.
        // In particular the bitfield pair must both be used: pairing a bitfield renderer with the
        // plain combo editor is exactly how a picked flag gets stored as a different flag.
        String dispatch = methodBody(Files.readString(dispatcherSource(), StandardCharsets.UTF_8), "loadCellRenderers");

        List<String> missing = new ArrayList<>();
        for (String component : List.of(
                "CheckBoxRenderer", "CheckBoxEditor",
                "ComboBoxCellEditor", "BitfieldComboBoxEditor",
                "IndexedStringCellRenderer", "ColoredIndexedStringCellRenderer", "BitfieldStringCellRenderer",
                "NumberOnlyCellEditor"))
        {
            // matches both `new Foo(` and `new Outer.Foo(`
            if (!Pattern.compile("new\\s+(?:[A-Za-z0-9_]+\\.)*" + component + "\\s*\\(").matcher(dispatch).find())
                missing.add(component);
        }

        assertThat(missing).as("cell components never constructed by DefaultTable.loadCellRenderers").isEmpty();
    }

    @Test
    @DisplayName("the custom cell supplier must be able to produce both an editor and a renderer")
    void customSupplierProvidesBothHalves()
    {
        // CUSTOM columns get their pair from a supplier rather than from the dispatch. If the
        // interface ever lost one half, CUSTOM columns would quietly fall back to the defaults.
        List<String> methods = new ArrayList<>();
        for (Method m : CellTypes.CustomCellFunctionSupplier.class.getMethods())
            methods.add(m.getName());

        assertThat(methods).contains("getEditor", "getRenderer");
    }

    @Test
    @DisplayName("the set of cell types is the set the dispatchers were written against")
    void constantSetIsTheKnownOne()
    {
        // A tripwire: adding a cell type is a change that has to be made in more than one place,
        // and this test is where the author is told so.
        assertThat(Arrays.stream(CellTypes.values()).map(Enum::name))
                .containsExactlyInAnyOrder("INTEGER", "STRING", "COMBO_BOX", "COLORED_COMBO_BOX",
                        "BITFIELD_COMBO_BOX", "CHECKBOX", "CUSTOM");
    }

    private static Path dispatcherSource()
    {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null)
        {
            Path candidate = dir.resolve(DISPATCHER);
            if (Files.exists(candidate))
                return candidate;
            dir = dir.getParent();
        }
        throw new AssertionError("could not locate " + DISPATCHER + " from " + Paths.get("").toAbsolutePath()
                + " - if the dispatcher moved, this test has to move with it rather than be deleted");
    }

    /** The text of the named method, brace matched from its declaration. */
    private static String methodBody(String source, String methodName)
    {
        Matcher declaration = Pattern.compile(
                        "(?m)^[ \\t]*(?:public|protected|private)[^;=\\n]*\\b" + methodName + "\\s*\\([^;{]*\\)[^;{]*$")
                .matcher(source);
        assertThat(declaration.find()).as("declaration of %s()", methodName).isTrue();

        int open = source.indexOf('{', declaration.end());
        int depth = 0;
        for (int i = open; i < source.length(); i++)
        {
            char c = source.charAt(i);
            if (c == '{')
                depth++;
            else if (c == '}' && --depth == 0)
                return source.substring(open, i + 1);
        }
        throw new AssertionError("unbalanced braces in " + methodName + "()");
    }

}
