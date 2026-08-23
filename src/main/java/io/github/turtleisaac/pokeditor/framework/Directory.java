package io.github.turtleisaac.pokeditor.framework;

import java.io.File;

public class Directory extends File
{
    public Directory(String pathname)
    {
        super(pathname);
    }

    @Override
    public boolean delete()
    {
        return clearDirectory(this);
    }

    private boolean clearDirectory(File directory)
    {
        File[] subfiles = directory.listFiles();
        if (subfiles == null) // not a directory, or unreadable
            return false;

        boolean success = true;
        for(File subfile : subfiles)
        {
            if(subfile.isDirectory())
            {
                success &= clearDirectory(subfile);
            }
            else
            {
                success &= subfile.delete();
            }
        }
        return directory.delete() && success;
    }
}
