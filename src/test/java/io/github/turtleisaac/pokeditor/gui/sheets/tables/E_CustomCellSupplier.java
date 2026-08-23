package io.github.turtleisaac.pokeditor.gui.sheets.tables;

import io.github.turtleisaac.pokeditor.gui.sheets.tables.cells.CellTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link CellTypes.CustomCellFunctionSupplier} whose products remember, verbatim, the three
 * arrays they were handed. That is what makes the text-source consumption bijection observable
 * for {@code CellTypes.CUSTOM} columns, which - unlike combo box columns - expose no
 * {@code setItems} of their own.
 */
public class E_CustomCellSupplier implements CellTypes.CustomCellFunctionSupplier
{
    private final List<String[][]> editorCalls = new ArrayList<>();
    private final List<String[][]> rendererCalls = new ArrayList<>();

    @Override
    public TableCellEditor getEditor(String[]... strings)
    {
        editorCalls.add(strings);
        return new E_CustomEditor(strings);
    }

    @Override
    public TableCellRenderer getRenderer(String[]... strings)
    {
        rendererCalls.add(strings);
        return new E_CustomRenderer(strings);
    }

    /** every argument list {@link #getEditor} has ever been called with, in call order */
    public List<String[][]> editorCalls()
    {
        return editorCalls;
    }

    /** every argument list {@link #getRenderer} has ever been called with, in call order */
    public List<String[][]> rendererCalls()
    {
        return rendererCalls;
    }

    public static class E_CustomEditor extends AbstractCellEditor implements TableCellEditor
    {
        private final String[][] given;

        E_CustomEditor(String[][] given)
        {
            this.given = given;
        }

        public String[][] given()
        {
            return given;
        }

        @Override
        public Object getCellEditorValue()
        {
            return 0;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            return new JLabel();
        }
    }

    /**
     * Shaped like the real custom renderers (a {@link DefaultTableCellRenderer} which returns
     * {@code this}), because {@code DefaultTable.exportClean} casts the prepared component to
     * {@code DefaultTableCellRenderer} and reads its text.
     */
    public static class E_CustomRenderer extends DefaultTableCellRenderer
    {
        private final String[][] given;

        E_CustomRenderer(String[][] given)
        {
            this.given = given;
        }

        public String[][] given()
        {
            return given;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setText("custom(" + given[0][0] + "," + given[1][0] + "," + given[2][0] + ")=" + value);
            return this;
        }
    }
}
