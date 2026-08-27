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
        // super.delete(), not directory.delete(): for the top level call `directory` IS this
        // Directory, so a virtual call would dispatch straight back into the override above and
        // recurse until the stack ran out. Subdirectories arrive as plain File from listFiles(),
        // so only the outermost call was ever affected - which is to say every single call.
        boolean removed = (directory == this) ? super.delete() : directory.delete();
        return removed && success;
    }
}
