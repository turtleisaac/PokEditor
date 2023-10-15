package io.github.turtleisaac.pokeditor.framework;

import java.util.Arrays;

public class ArrayModifier
{
    public static Object[][] trim(Object[][] arr, int rows, int cols)
    {
        arr= Arrays.copyOfRange(arr,rows,arr.length);

        for(int i= 0; i < arr.length; i++)
        {
//            System.out.println(Arrays.toString(arr[i]));
            arr[i]= Arrays.copyOfRange(arr[i], cols, arr[i].length);
//            System.out.println(Arrays.toString(arr[i]) + "\n");
        }

        return arr;
    }

    public static Object[] getColumn(Object[][] arr, int col)
    {
        Object[] ret= new Object[arr.length];
        Arrays.fill(ret, "");
        for(int i= 0; i < arr.length; i++)
        {
            if (col >= arr[i].length)
                break;

            ret[i]= arr[i][col];
        }

        return ret;
    }

    public static String[] accommodateLength(String[] arr, int targetLength)
    {
        arr= Arrays.copyOf(arr,targetLength);

        for(int i= 0; i < arr.length; i++)
        {
            if(arr[i] == null)
                arr[i]= "" + i;
        }

        return arr;
    }
}
