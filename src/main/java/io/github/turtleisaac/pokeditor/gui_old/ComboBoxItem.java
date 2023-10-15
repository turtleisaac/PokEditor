package io.github.turtleisaac.pokeditor.gui_old;

public class ComboBoxItem
{
    private String str;

    public ComboBoxItem(String str)
    {
        this.str= str;
    }

    public ComboBoxItem(int num)
    {
        str= "" + num;
    }

    public void setName(String str) {this.str= str;}

    @Override
    public String toString()
    {
        return str;
    }
}
