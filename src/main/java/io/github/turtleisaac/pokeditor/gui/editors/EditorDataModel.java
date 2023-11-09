package io.github.turtleisaac.pokeditor.gui.editors;

public interface EditorDataModel<E extends Enum<E>>
{
    Object getValueFor(int entryIdx, E property);
    void setValueFor(Object aValue, int entryIdx, E property);
    int getEntryCount();
    String getEntryName(int entryIdx);
}
