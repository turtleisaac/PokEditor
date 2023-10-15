package io.github.turtleisaac.pokeditor.framework;

import java.io.File;
import java.util.Objects;

public class Directory extends File
{
    public Directory(String pathname)
    {
        super(pathname);
    }

    @Override
    public boolean delete()
    {
        clearDirectory(this);
        return true;
    }

    private void clearDirectory(File directory)
    {
        for(File subfile : Objects.requireNonNull(directory.listFiles()))
        {
            if(subfile.isDirectory())
            {
                clearDirectory(subfile);
            }
            else
            {
                subfile.delete();
            }
        }
        directory.delete();
    }
}
