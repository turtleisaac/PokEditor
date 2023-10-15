package io.github.turtleisaac.pokeditor.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayProcessor
{
    private List<Object[]> data;
    private String line;
    private int numColumns;

    public ArrayProcessor()
    {
        data= new ArrayList<>();
        numColumns= 0;
    }

    public ArrayProcessor(String str)
    {
        data= new ArrayList<>();
        line= str;
        numColumns= 0;
    }

    public ArrayProcessor(int numColumns)
    {
        data= new ArrayList<>();
        this.numColumns= numColumns;
    }

    public void append(String str)
    {
        if(line == null)
            line= str;
        else
            line+= str;
    }

    public void substring(int start, int end)
    {
        line= line.substring(start,end);
    }

    public void substring(int start)
    {
        line= line.substring(start);
    }

    public void newLine()
    {
        String[] arr;
//        System.out.println(Arrays.toString(line.split(",")));
        if(line.endsWith(","))
            line= line.substring(0,line.length()-1);

        if(numColumns != 0)
        {
            arr= new String[numColumns];
            Arrays.fill(arr,"");
            System.arraycopy(line.split(","),0,arr,0,line.split(",").length);
        }
        else
            arr= line.split(",");

        data.add(arr);
        line= "";
    }

    public Object[][] getTable()
    {
        Object[][] table= new Object[data.size()][];

        for(int i= 0; i < data.size(); i++)
        {
            table[i]= data.get(i);
        }

        return table;
    }
}
