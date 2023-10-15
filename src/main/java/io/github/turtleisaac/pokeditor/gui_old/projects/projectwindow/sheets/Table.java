package io.github.turtleisaac.pokeditor.gui_old.projects.projectwindow.sheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Table
{
    public static void main(String[] args)
    {
        Object[][] arr= new Object[][]{
                {"ID", "Name", "Message"},
                {"0","Turtle","Mr. Mime"},
                {"1","Jay","AAAHHHH!"},
                {"2","Vendor","Slowpoke"},
                {"3","0zone","Baguette"}
        };

        Table table= new Table(arr,1,0);

        IntColumn idColumn= new IntColumn(table,"ID");
        StringColumn nameColumn= new StringColumn(table,"Name");
        StringColumn messageColumn= new StringColumn(table,"Message");

        for(int i= 0; i < table.getSize(); i++)
        {
            System.out.println(idColumn.getValue(i) + ", " + nameColumn.getValue(i) + ", " + messageColumn.getValue(i));
        }
    }

    private Object[][] table;
    private ArrayList<HashMap<String, Object>> recordList;

    public Table(Object[][] arr, int rowStartIndex, int colStartIndex)
    {
        setTable(arr, rowStartIndex, colStartIndex);
    }

    public Table()
    {
        recordList= new ArrayList<>();
        table= null;
    }

    public HashMap<String, Object> getRecord(int idx)
    {
        if(recordList != null)
        {
            if(recordList.size() > idx)
            {
                return recordList.get(idx);
            }
        }

        return null;
    }

    public HashMap<String, Object> removeRecord(int idx)
    {
        return recordList.remove(idx);
    }

    public boolean removeRecord(HashMap<String, Object> record)
    {
        return recordList.remove(record);
    }

    public void addRecord(HashMap<String, Object> record)
    {
        recordList.add(record);
    }

    @SafeVarargs
    public final void addRecords(HashMap<String, Object>... records)
    {
        recordList.addAll(Arrays.asList(records));
    }

    public void setRecord(int idx, HashMap<String, Object> record)
    {
        recordList.set(idx, record);
    }

    public Object getField(int idx, String field)
    {
        if(recordList != null)
        {
            if(recordList.size() > idx)
            {
                if(recordList.get(idx).containsKey(field))
                {
                    return recordList.get(idx).get(field);
                }
            }
        }

        return null;
    }

    public void setField(int idx, String field, Object entry)
    {
        if(recordList != null)
        {
            if(recordList.size() > idx)
            {
                if(recordList.get(idx).containsKey(field))
                {
                    recordList.get(idx).remove(field);
                    recordList.get(idx).put(field,entry);
                }
            }
        }
    }

    public int getSize()
    {
        return recordList.size();
    }




    public ArrayList<HashMap<String, Object>> getRecordList()
    {
        return recordList;
    }

    public Object[][] getTable()
    {
        return table;
    }

    public void setTable(Object[][] arr)
    {
        setTable(arr,0,0);
    }

    public void setTable(Object[][] arr, int rowStartIndex, int colStartIndex)
    {
        table= arr;
        recordList= new ArrayList<>();

        for(int row= rowStartIndex; row < table.length; row++)
        {
            HashMap<String, Object> record= new HashMap<>();
            for(int col= colStartIndex; col < table[row].length; col++)
            {
                record.put((String) table[0][col],arr[row][col]);
            }
            recordList.add(record);
        }
    }









    public static class Column<T extends Comparable<T>>
    {
        private String field;
        private Table table;

        public Column(Table table, String field)
        {
            this.table= table;
            this.field= field;
        }

        protected Column()
        {
        }

        public T getValue(int idx)
        {
                return (T) table.getField(idx,field);
        }
    }


    public static class StringColumn extends Column<String>
    {
        public StringColumn(Table table, String field)
        {
            super(table,field);
        }

        public String getValue(int idx)
        {
            return super.getValue(idx);
        }
    }




    public static class IntColumn
    {
        private final StringColumn column;

        public IntColumn(Table table, String field)
        {
            column= new StringColumn(table,field);
        }

        public int getValue(int idx)
        {
            String value= column.getValue(idx);

            if(value.startsWith("0x"))
            {
                return Integer.parseInt(column.getValue(idx).substring(2),16);
            }
            else if(value.startsWith("0b"))
            {
                return Integer.parseInt(column.getValue(idx).substring(2),2);
            }
            else
            {
                return Integer.parseInt(column.getValue(idx));
            }

        }
    }

    public static class ShortColumn
    {
        private final StringColumn column;

        public ShortColumn(Table table, String field)
        {
            column= new StringColumn(table,field);
        }

        public short getValue(int idx)
        {
            return Short.parseShort(column.getValue(idx));
        }
    }

    public static class ByteColumn
    {
        private final StringColumn column;

        public ByteColumn(Table table, String field)
        {
            column= new StringColumn(table,field);
        }

        public byte getValue(int idx)
        {
            return Byte.parseByte(column.getValue(idx));
        }
    }

    public static class LongColumn
    {
        private final StringColumn column;

        public LongColumn(Table table, String field)
        {
            column= new StringColumn(table,field);
        }

        public long getValue(int idx)
        {
            return Long.parseLong(column.getValue(idx));
        }
    }

    public static class BooleanColumn
    {
        private final StringColumn column;

        public BooleanColumn(Table table, String field)
        {
            column= new StringColumn(table,field);
        }

        public boolean getValue(int idx)
        {
            return Boolean.parseBoolean(column.getValue(idx));
        }
    }
}
