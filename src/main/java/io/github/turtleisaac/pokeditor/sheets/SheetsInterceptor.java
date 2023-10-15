package io.github.turtleisaac.pokeditor.sheets;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SheetsInterceptor
{
    private static final String subDir = "local";
    private static boolean nullInsertionOccurred = false;

    public static void interceptWrite(Object[][] data, String projectPath, String output) throws IOException
    {
        output = output.replace("/", "_");
        if(!new File(projectPath + File.separator + subDir).exists())
        {
            new File(projectPath + File.separator + subDir).mkdir();
        }

        BufferedWriter writer= new BufferedWriter(new FileWriter(projectPath + File.separator + subDir + File.separator + output + ".csv"));
        for(Object[] row : data)
        {
            for(int col= 0; col < row.length; col++)
            {
                if(row[col] == null)
                {
                    System.currentTimeMillis();
                    row[col] = "";
                }
                writer.write((String) row[col]);
                if (col < row.length - 1)
                {
                    writer.write(", ");
                }
            }
            writer.write("\n");
        }
        writer.close();
    }

    public static Object[][] interceptLoad(String projectPath, String input) throws IOException
    {
        String[][] data = load(projectPath, input);

        if (input.equals("Trainer Data"))
        {
            System.currentTimeMillis();
        }

        if (input.equals("Formatting (DO NOT TOUCH)"))
        {
            System.currentTimeMillis();
        }

        HashMap<String, String[][]> resources = new HashMap<>();
        for(int row= 0; row < data.length; row++)
        {
            for(int col= 0; col < data[row].length; col++)
            {
                if(data[row][col].startsWith("=") && data[row][col].contains("!"))
                {
                    data[row][col] = obtainResourceCell(data[row][col], projectPath, resources);
                }
            }
        }

        return data;
    }

    public static String[][] load(String projectPath, String input) throws IOException
    {
        input = input.replace("/", "_");
        BufferedReader reader= new BufferedReader(new FileReader(projectPath + File.separator + subDir + File.separator + input + ".csv"));

        String[][] data = reader.lines().map(row -> Arrays.stream(row.split(",")).map(String::trim).toArray(String[]::new)).toArray(String[][]::new);
        reader.close();
        return data;
    }

    private static void loadResources(String projectPath, String input, HashMap<String, String[][]> resources) throws IOException
    {
        if(!resources.containsKey(input))
        {
            resources.put(input, load(projectPath, input));
        }
    }

    private static String obtainResourceCell(String cell, String projectPath, HashMap<String, String[][]> resources) throws IOException
    {
        if(cell.equals("=Evolutions!B444"))
            System.currentTimeMillis();

        if (cell.startsWith("=") && cell.contains("!"))
        {
            String resourcePath = cell.substring(0, cell.indexOf('!'));
            resourcePath = resourcePath.replace("=", "").replace("'", "");

            loadResources(projectPath, resourcePath, resources);
            CharSequence reference = cell.substring(cell.indexOf('!')+1).replace("$", "");

            String patternStr = "[0-9]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(reference);
            matcher.find();
            int numIndex = matcher.start();

            int refRowNum = Integer.parseInt(reference.toString().substring(numIndex));
            String refCol = reference.toString().substring(0, numIndex);
            int refColNum = 0;
            for(int i= refCol.length()-1; i >= 0; i--)
            {
                refColNum+= Math.pow(26, i) * (refCol.charAt(i)-'A');
            }

            if(refRowNum-1 >= resources.get(resourcePath).length)
            {
                if(!nullInsertionOccurred)
                    System.out.println("Sheet Intercept Null Insertion");
                nullInsertionOccurred = true;
                return "";
            }
            else if(refColNum >= resources.get(resourcePath)[refRowNum-1].length)
            {
                if(!nullInsertionOccurred)
                    System.out.println("Sheet Intercept Null Insertion");
                nullInsertionOccurred = true;
                return "";
            }

            cell = resources.get(resourcePath)[refRowNum-1][refColNum];
            return obtainResourceCell(cell, projectPath, resources);
        }
        return cell;
    }

    public static void main(String[] args) throws IOException
    {
        String projectPath = System.getProperty("user.dir");
        Object[][] data = interceptLoad(projectPath, "Personal");
        for(Object[] row : data)
        {
            for(Object cell : row)
            {
                System.out.print(cell + ", ");
            }
            System.out.println();
        }
    }
}
