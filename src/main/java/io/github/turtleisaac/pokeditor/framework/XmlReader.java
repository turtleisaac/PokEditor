package io.github.turtleisaac.pokeditor.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

public class XmlReader
{
    private String file;

    public XmlReader(String file)
    {
        this.file= file;
    }

    public XmlReader(File file)
    {
        this.file= file.toString();
    }

    public XmlReader(Path path)
    {
        this.file= path.toString();
    }

    public HashMap<String, String> readFile() throws IOException
    {
        HashMap<String, String> ret= new HashMap<>();

        BufferedReader reader= new BufferedReader(new FileReader(file));
        String line;
        boolean first= true;
        boolean second= true;

        while((line= reader.readLine()) != null)
        {
            if(first)
            {
                first= false;
            }
            else if(second)
            {
                ret.put("program",line.substring(1,line.length()-1));
                System.out.println("Program: " + line.substring(1,line.length()-1));
                second= false;
            }
            else
            {
                if(!line.equals("<\\" + ret.get("program") + ">"))
                {
                    ret.put(line.substring(3,line.indexOf('>')),line.substring(line.indexOf('>')+1,line.lastIndexOf('<')));
                    System.out.println(line.substring(3,line.indexOf('>')) + ": " + line.substring(line.indexOf('>')+1,line.lastIndexOf('<')));
                }

            }
        }

        return ret;
    }
}
