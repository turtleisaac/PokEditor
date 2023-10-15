//package io.github.turtleisaac.pokeditor.utilities;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//import org.json.JSONTokener;
//
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//public class TablePointers
//{
//    static Map<String, Map<String,TablePointer>> pointers= new HashMap<>();
//
//    static
//    {
//        InputStream inputStream = TablePointers.class.getResourceAsStream("/data/pointers.json");
//        JSONTokener tokener     = new JSONTokener(inputStream);
//        JSONObject pointers  = new JSONObject(tokener);
//        JSONArray games= pointers.getJSONArray("game");
//
//        for(int i= 0; i < games.length(); i++)
//        {
//            Map<String,TablePointer> gameMap= new HashMap<>();
//            JSONObject game= games.getJSONObject(i);
//            JSONArray tables= game.getJSONArray("tables");
//
//            for(int x= 0; x < tables.length(); x++)
//            {
//                JSONObject table= tables.getJSONObject(x);
//                gameMap.put(table.getString("name"),new TablePointer(table));
//            }
//
//            TablePointers.pointers.put(game.getString("code"),gameMap);
//        }
//
////        System.out.println("\n");
////        TablePointers.pointers.forEach((k, v) -> {
////            System.out.format("\n%s initialized\n",k);
////            v.forEach((x,y) -> System.out.format("\t%s initialized (pointerOffset 0x%s in file %s)\n",x,Long.toHexString(y.pointerOffset),y.file));
////        });
////        System.out.println("\n");
//    }
//
//    public static Map<String, Map<String,TablePointer>> getPointers()
//    {
//        return pointers;
//    }
//
//    public static final class TablePointer
//    {
//        protected String description;
//        protected String file;
//        protected long pointerOffset;
//        protected long countOffset;
//        protected int countPointerLength;
//
//        private TablePointer(JSONObject table)
//        {
//            description= table.getString("description");
//            file= table.getString("file");
//            pointerOffset= Long.parseLong(table.getString("pointerOffset").substring(2),16);
//            countOffset= Long.parseLong(table.getString("countOffset").substring(2),16);
//            countPointerLength= table.getInt("countPointerLength");
//        }
//    }
//}
