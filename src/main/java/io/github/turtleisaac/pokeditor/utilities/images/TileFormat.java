package io.github.turtleisaac.pokeditor.utilities.images;

public enum TileFormat
{
    Horizontal(0),
    Linear(1),
    Vertical(3)
    ;

    public final int value;

    TileFormat(int value)
    {
        this.value= value;
    }
}
