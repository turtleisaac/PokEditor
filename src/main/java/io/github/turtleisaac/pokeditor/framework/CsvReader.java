package io.github.turtleisaac.pokeditor.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public class CsvReader
{
    public static void main(String[] args) throws IOException
    {
        CsvReader reader= new CsvReader(System.getProperty("user.dir") + File.separator + "personalData.csv",0,0);
        reader.print();
    }

    private final String[][] in;
    private int line;

    public CsvReader(String filePath) throws IOException
    {
        in= getData(filePath,2,1);
        line= 0;
    }

    /**
     *
     * @param filePath location of csv file
     * @param firstX how many columns (starting from index 0) should be removed when the file is read
     * @param firstY how many rows (starting from index 0) should be removed when the file is read
     * @throws IOException because BufferedReader
     */
    public CsvReader(String filePath, int firstX, int firstY) throws IOException
    {
        in= getData(filePath,firstX,firstY);
        line= 0;
    }

    /**
     *
     * @param filePath location of csv file
     * @param firstX how many columns (starting from index 0) should be removed when the file is read
     * @param firstY how many rows (starting from index 0) should be removed when the file is read
     * @return the csv in the form of a 2D array
     * @throws IOException because BufferedReader
     */
    private String[][] getData(String filePath, int firstX, int firstY) throws IOException
    {
        ArrayList<String> fileLines= new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8))
        {
            String line;
            boolean firstLine= true;
            while((line= reader.readLine()) != null)
            {
                if(firstLine)
                {
                    firstLine= false;
                    if(!line.isEmpty() && line.charAt(0) == '\ufeff') // strip the UTF-8 BOM
                        line= line.substring(1);
                }
                fileLines.add(line);
            }
        }
        for(; firstY != 0; firstY--)
        {
            fileLines.remove(0);
        }


        String[][] fileData= new String[fileLines.size()][];
        int x;
        for(int i= 0; i < fileLines.size(); i++)
        {
            x= firstX;
            String thisLine= fileLines.get(i);
            for(; x != 0; x--)
            {
                thisLine= thisLine.substring(thisLine.indexOf(",")+1);
            }
            thisLine= thisLine.replaceAll("×","x");
            fileData[i]= splitCsvLine(thisLine);
        }
        return fileData;
    }

    /**
     * Splits a single CSV record into fields as per RFC 4180 - commas inside a quoted field
     * are part of the field, a doubled quote inside a quoted field is a literal quote, and
     * trailing empty fields are preserved (which {@code split(",")} would silently drop,
     * shifting every downstream column).
     */
    static String[] splitCsvLine(String line)
    {
        ArrayList<String> fields= new ArrayList<>();
        StringBuilder current= new StringBuilder();
        boolean inQuotes= false;

        for(int i= 0; i < line.length(); i++)
        {
            char c= line.charAt(i);
            if(inQuotes)
            {
                if(c == '"')
                {
                    if(i + 1 < line.length() && line.charAt(i + 1) == '"')
                    {
                        current.append('"');
                        i++;
                    }
                    else
                    {
                        inQuotes= false;
                    }
                }
                else
                {
                    current.append(c);
                }
            }
            else if(c == '"')
            {
                inQuotes= true;
            }
            else if(c == ',')
            {
                fields.add(current.toString());
                current.setLength(0);
            }
            else
            {
                current.append(c);
            }
        }
        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }

    public String[] next()
    {
        if(line == in.length)
        {
            return null;
        }
        else
        {
            return in[line++];
        }
    }

    public int length()
    {
        return in.length;
    }

    public void skipLine() {
        next();
    }

    public String[][] getCsv()
    {
        return in;
    }

    public String getCell(int row, int col)
    {
        return in[row][col];
    }

    public void print()
    {
        String cyan= "\u001b[36m";
        String red= "\u001b[31m";
        String reset= "\u001b[0m";

        // sized by the widest ROW, because it is indexed by column below
        int maxColumns= 0;
        for (String[] strings : in)
        {
            maxColumns= Math.max(maxColumns, strings.length);
        }

        int[] columnWidths= new int[maxColumns];
        Arrays.fill(columnWidths,Integer.MIN_VALUE);

        for (String[] strings : in)
        {
            for (int y = 0; y < strings.length; y++)
            {
                if (columnWidths[y] < strings[y].length()) {
                    columnWidths[y]= strings[y].length();
                }
            }
        }

        for(int i= 0; i < columnWidths.length; i++)
        {
            columnWidths[i]= Math.min(columnWidths[i], 15);
        }

        StringBuilder line;
        for(int x= 0; x < in.length; x++)
        {
            line= new StringBuilder();
            for(int y= 0; y < in[x].length; y++)
            {
                line.append("│");
                if(in[x][y].length() <= 15)
                {
                    line.append(fixString(in[x][y],columnWidths[y]));
                }
                else
                {
                    line.append(fixString(in[x][y].substring(0,10) + "...",columnWidths[y]));
                }

                if(y == in[x].length-1)
                {
                    line.append("│");
                }
            }

            if(x == 0)
            {

                for(int i= 0; i < line.toString().length(); i++)
                {
                    if(line.toString().charAt(i) == '│')
                    {
                        System.out.print(cyan + "┼" + reset);
                    }
                    else
                    {
                        final String substring = line.toString().substring(i, i + 1);
                        if(!substring.equals(cyan) && !substring.equals(red) && !substring.equals(reset))
                        {
                            System.out.print(cyan + "-" + reset);
                        }
                    }
                }
                System.out.println();
            }

            System.out.println(line.toString());
            for(int i= 0; i < line.toString().length(); i++)
            {
                if(line.toString().charAt(i) == '│')
                {
                    System.out.print(cyan + "┼" + reset);
                }
                else
                {
                        System.out.print(cyan + "-" + reset);
                }
            }
            System.out.println();
        }
    }

    private String fixString(String str, int targetLength)
    {
        StringBuilder strBuilder = new StringBuilder(str);
        while(strBuilder.length() != targetLength){
            strBuilder.append(" ");
        }
        str = strBuilder.toString();
        return str;
    }
}
