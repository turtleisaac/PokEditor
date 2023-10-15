package io.github.turtleisaac.pokeditor.gui.sheets;

import io.github.turtleisaac.pokeditor.sheets.JohtoSheets;
import io.github.turtleisaac.pokeditor.sheets.SinnohSheets;

public interface Sheet
{
    public Object[][] getData();
    public SinnohSheets getSheetType();
    public JohtoSheets getAltSheetType();
}
