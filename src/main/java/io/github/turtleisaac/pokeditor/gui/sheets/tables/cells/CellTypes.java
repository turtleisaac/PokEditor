package io.github.turtleisaac.pokeditor.gui.sheets.tables.cells;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public enum CellTypes
{
    INTEGER,
    STRING,
    COMBO_BOX,
    COLORED_COMBO_BOX,
    BITFIELD_COMBO_BOX,
    CHECKBOX,
    CUSTOM;


    public interface CustomCellFunctionSupplier {

        TableCellEditor getEditor(String[]... strings);
        TableCellRenderer getRenderer(String[]... strings);
    }
}
