//package io.github.turtleisaac.pokeditor.sheets;
//
//import com.google.api.client.auth.oauth2.Credential;
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.store.FileDataStoreFactory;
//import com.google.api.services.sheets.v4.Sheets;
//import com.google.api.services.sheets.v4.SheetsScopes;
//import com.google.api.services.sheets.v4.model.*;
//
//import java.io.*;
//import java.security.GeneralSecurityException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//public class GoogleSheetsAPI
//{
//    private final Sheets sheetsService;
//    private String APPLICATION_NAME= "PokEditor";
//    private final String SPREADSHEET_ID;
//    private final String SPREADSHEET_LINK;
//    private final String projectPath;
//
//    private final boolean local;
//
//    public GoogleSheetsAPI(String spreadsheetLink, String projectPath, boolean local) throws IOException, GeneralSecurityException
//    {
//        this.local= local;
//        this.projectPath= projectPath;
//
//        if(!local)
//        {
//            if(!spreadsheetLink.contains("https://"))
//                spreadsheetLink= "https://" + spreadsheetLink;
//
//            SPREADSHEET_LINK= spreadsheetLink;
//
//            SPREADSHEET_ID= spreadsheetLink.split("/")[5];
//            sheetsService= getSheetsService();
//        }
//        else
//        {
//            SPREADSHEET_ID = null;
//            SPREADSHEET_LINK = null;
//            sheetsService = null;
//        }
//    }
//
//    private Credential authorize() throws IOException, GeneralSecurityException
//    {
//        if(!local)
//        {
//            File userApiCredentials = new File(projectPath + File.separator + "credentials.json");
//            InputStream in;
//
//            System.out.println("Post - API Loader");
//            System.out.println(userApiCredentials.getAbsolutePath());
//            System.out.println("\tExists: " + userApiCredentials.exists());
//
//            if(userApiCredentials.exists())
//            {
//                System.out.println("Loaded user self-credentials");
//                in = new FileInputStream(userApiCredentials);
//            }
//            else
//            {
//                in= GoogleSheetsAPI.class.getResourceAsStream("/credentials.json");
//            }
//
//            GoogleClientSecrets clientSecrets= GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));
//
//            List<String> scopes= Arrays.asList(SheetsScopes.SPREADSHEETS);
//            GoogleAuthorizationCodeFlow flow= new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),GsonFactory.getDefaultInstance(),clientSecrets,scopes)
//                    .setDataStoreFactory(new FileDataStoreFactory(new File(projectPath + File.separator + "tokens")))
//                    .setAccessType("offline")
//                    .build();
//
//            Credential credential= new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
//
//            return credential;
//        }
//        else
//        {
//            return null;
//        }
//
//    }
//
//    public Sheets getSheetsService() throws IOException, GeneralSecurityException
//    {
//        Credential credential= authorize();
//        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),credential)
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//    }
//
//    public List<List<Object>> getResponse(String subSheet, String range) throws IOException
//    {
//        ValueRange response= sheetsService.spreadsheets().values().get(SPREADSHEET_ID,subSheet + "!" + range).execute();
//
//        List<List<Object>> values= response.getValues();
//
//        return values;
//    }
//
//    public List<List<Object>> getSpecifiedSheet(String subSheet) throws IOException
//    {
//        if(!local)
//        {
//            ValueRange response= sheetsService.spreadsheets().values().get(SPREADSHEET_ID,subSheet).execute();
//
//            List<List<Object>> values= response.getValues();
//
//            System.out.println("Downloading sheet: " + subSheet + " (" + values.size() + " rows and " + values.get(0).size() + " columns)\n");
//
//            return values;
//        }
//        else
//        {
//            List<List<Object>> values = Arrays.stream(SheetsInterceptor.interceptLoad(projectPath, subSheet)).map(row -> new ArrayList<>(Arrays.asList(row))).collect(Collectors.toList());
//            return values;
//        }
//
//    }
//
//    public Object[][] getSpecifiedSheetArr(String subSheet) throws IOException
//    {
//        if(!local)
//        {
//            List<List<Object>> values= getSpecifiedSheet(subSheet);
//
//            Object[][] ret= new Object[values.size()][];
//
//            for(int i= 0; i < values.size(); i++)
//                ret[i]= values.get(i).toArray(new Object[0]);
//
//            return ret;
//        }
//        else
//        {
//            return SheetsInterceptor.interceptLoad(projectPath, subSheet);
//        }
//    }
//
//    public void appendSheet(String range) throws IOException
//    {
//        ValueRange appendBody= new ValueRange()
//                .setValues(Arrays.asList(Arrays.asList("This","was","added","from","PokEditor!")));
//
//        AppendValuesResponse appendResult= sheetsService.spreadsheets().values()
//                .append(SPREADSHEET_ID,range,appendBody)
//                .setValueInputOption("USER_ENTERED")
//                .setInsertDataOption("INSERT_ROWS")
//                .setIncludeValuesInResponse(false)
//                .execute();
//    }
//
//    public void updateSheet(String range, List<List<Object>> values) throws IOException
//    {
//        if(!local)
//        {
//            ValueRange body= new ValueRange()
//                    .setValues(values);
//
//            UpdateValuesResponse result= sheetsService.spreadsheets().values()
//                    .update(SPREADSHEET_ID,range,body)
//                    .setValueInputOption("USER_ENTERED")
//                    .setIncludeValuesInResponse(false)
//                    .execute();
//        }
//        else
//        {
//            SheetsInterceptor.interceptWrite(values.stream().map(row -> row.toArray(new Object[0])).toArray(Object[][]::new), projectPath, range);
//        }
//    }
//
//    public void updateSheet(String range, Object[][] values) throws IOException
//    {
//        List<List<Object>> data= new ArrayList<>();
//
//        for(Object[] arr : values)
//        {
////            System.out.println(Arrays.toString(arr));
//            List<Object> row = new ArrayList<>(Arrays.asList(arr));
//            data.add(row);
//        }
//
//        updateSheet(range, data);
//    }
//
//    public void updateSheet(String range, Object[] values) throws IOException
//    {
//        List<Object> row = new ArrayList<>(Arrays.asList(values));
//        List<List<Object>> data= new ArrayList<>();
//        data.add(row);
//        updateSheet(range, data);
//    }
//
//    public void deleteRow() throws IOException
//    {
//        DeleteDimensionRequest deleteRequest= new DeleteDimensionRequest()
//                .setRange(
//                        new DimensionRange()
//                                .setSheetId(1880122989)
//                                .setDimension("ROWS")
//                                .setStartIndex(20)
//                );
//
//        List<Request> requests= new ArrayList<>();
//        requests.add(new Request().setDeleteDimension(deleteRequest));
//
//        BatchUpdateSpreadsheetRequest body= new BatchUpdateSpreadsheetRequest().setRequests(requests);
//        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID,body).execute();
//
//        System.out.println("Delete successful (hopefully)");
//    }
//
//    public String[] getSheetNames() throws IOException
//    {
//        if(!local)
//        {
//            Spreadsheet response1= sheetsService.spreadsheets().get(SPREADSHEET_ID)
//                    .setIncludeGridData(false)
//                    .execute();
//
//            List<Sheet> sheetList = response1.getSheets();
//
//            List<String> sheetNames= new ArrayList<>();
//
//            System.out.println("\n----Sheet Names----");
//            for (Sheet sheet : sheetList)
//            {
//                System.out.println(sheet.getProperties().getTitle());
//                sheetNames.add(sheet.getProperties().getTitle());
//            }
//            System.out.println("-------------------\n");
//
//            return sheetNames.toArray(new String[0]);
//        }
//        else
//        {
//            if(new File(projectPath + File.separator + "local").exists())
//            {
//                return Arrays.stream(Objects.requireNonNull(new File(projectPath + File.separator + "local").listFiles())).filter(s -> s.getName().contains(".csv")).map(f -> f.getName().substring(0, f.getName().indexOf("."))).toArray(String[]::new);
//            }
//            else
//            {
//                return new String[] {};
//            }
//
//        }
//
//    }
//
//
//    /**
//     * Gets the note contained in cell (0,0)
//     */
//    public String getPokeditorSheetType(int sheetId) throws IOException
//    {
//        return sheetsService.spreadsheets().get(SPREADSHEET_ID)
//                .setIncludeGridData(true)
//                .setFields("sheets/data/rowData/values/note")
//                .execute()
//                .getSheets()
//                .get(sheetId)
//                .getData()
//                .get(0)
//                .getRowData()
//                .get(0)
//                .getValues()
//                .get(0)
//                .getNote();
//    }
//
////    public void setPokeditorSheetType(String sheetName, String note)
////    {
////        try
////        {
////            List<Sheet> sheetList= sheetsService.spreadsheets().get(SPREADSHEET_ID).execute().getSheets();
////
////            int sheetIndex= indexOf(sheetList,sheetName);
////            Integer sheetId= sheetList.get(sheetIndex).getProperties().getSheetId();
////            System.out.println("Sheet ID: " + sheetId);
////
////            List<Request> requests= new ArrayList<>();
////            requests.add(buildRequestAddNoteCell(0,0,0,0,sheetId,note));
////
////            BatchUpdateSpreadsheetRequest body= new BatchUpdateSpreadsheetRequest().setRequests(requests);
////            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID,body).execute();
////        }
////        catch (IOException | NullPointerException exception)
////        {
////            exception.printStackTrace();
////        }
////
//////        System.out.println(sheetsService.spreadsheets().developerMetadata().);
////
////    }
//
//    /**
//     * Adds a note to cell(s)
//     * @return
//     */
//    private Request buildRequestAddNoteCell(int startRow, int endRow, int startColumn, int endColumn, int sheetId, String note) {
//        Request request = new Request();
//        request.setRepeatCell(new RepeatCellRequest()
//                .setCell(new CellData().setNote(note))
//                .setRange(new GridRange()
//                        .setSheetId(sheetId)
//                        .setStartRowIndex(startRow)
//                        .setEndRowIndex(endRow)
//                        .setStartColumnIndex(startColumn)
//                        .setEndColumnIndex(endColumn))
//                .setFields("note")
//        );
//        return request;
//    }
//
//    public List<List<java.awt.Color>> getSpecifiedSheetColors(int sheetIdx) throws IOException
//    {
//        if(sheetIdx != -1)
//        {
//            Sheet s= sheetsService.spreadsheets().get(SPREADSHEET_ID)
//                    .setIncludeGridData(true)
//                    .setFields("sheets/data/rowData/values/effectiveFormat/backgroundColor")
//                    .execute()
//                    .getSheets()
//                    .get(sheetIdx);
//
//
//            List<List<java.awt.Color>> colorList= new ArrayList<>();
//            if(s != null)
//            {
//                GridData gd= s.getData().get(0);
//
//                for(RowData row : gd.getRowData())
//                {
//                        List<java.awt.Color> colors= new ArrayList<>();
//                        for(CellData cell : row.getValues())
//                        {
//                            CellFormat fmt= cell.getEffectiveFormat();
//                            if(fmt != null)
//                            {
//                                colors.add(convertColor(fmt.getBackgroundColor()));
//                            }
//                            else if((fmt= cell.getUserEnteredFormat()) != null)
//                            {
//                                colors.add(convertColor(fmt.getBackgroundColor()));
//                            }
//                            else
//                            {
//                                colors.add(java.awt.Color.GRAY);
//                            }
//
//                        }
//                        colorList.add(colors);
//                }
//            }
//
//            return colorList;
//        }
//
//        return null;
//    }
//
////    public void pokeditorSheetMetadataUpdater(String[] expectedNames) throws IOException
////    {
////        List<Sheet> sheetList= sheetsService.spreadsheets().get(SPREADSHEET_ID).execute().getSheets();
////        List<Request> requests= new ArrayList<>();
////        String[] sheetNames= getSheetNames();
////        boolean dataStart= false;
////
////        for(int i= 0; i < sheetNames.length; i++)
////        {
////            String sheetName= sheetNames[i];
////            if(sheetName.equals("Personal"))
////                dataStart= true;
////            if(sheetName.equals("Formatting (DO NOT TOUCH)"))
////                break;
////
////            Integer sheetId= sheetList.get(i).getProperties().getSheetId();
////
////            if(dataStart)
////                requests.add(buildRequestAddNoteCell(0,1,0,1,sheetId,expectedNames[i]));
////        }
////
////        BatchUpdateSpreadsheetRequest body= new BatchUpdateSpreadsheetRequest().setRequests(requests);
////            sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID,body).execute();
////    }
//
////    public void getMetadata(int id) throws IOException
////    {
////        System.out.println(sheetsService.spreadsheets().developerMetadata().get(SPREADSHEET_ID,id).get("Personal"));
////    }
//
//    public String getAPPLICATION_NAME()
//    {
//        return APPLICATION_NAME;
//    }
//
//    public String getSPREADSHEET_ID()
//    {
//        return SPREADSHEET_ID;
//    }
//
//    public String getSPREADSHEET_LINK()
//    {
//        return SPREADSHEET_LINK;
//    }
//
//    public void setAPPLICATION_NAME(String APPLICATION_NAME)
//    {
//        this.APPLICATION_NAME = APPLICATION_NAME;
//    }
//
//    public boolean isLocal()
//    {
//        return local;
//    }
//
//    //    public static void main(String[] args) throws IOException, GeneralSecurityException
////    {
////        GoogleSheetsAPI googleSheets= new GoogleSheetsAPI("https://docs.google.com/spreadsheets/d/1XxVz-Y5kpStjsMtgrtALD7DMh1OAvbYsOJl5WtDPi2k/edit#gid=414420977");
////        String subSheet= "Personal";
////        String range= "A2:H20";
////
////        List<List<Object>> values= googleSheets.getResponse(subSheet, range);
////
////        if(values == null || values.isEmpty())
////        {
////            System.out.println("No data found.");
////        }
////        else
////        {
////            for(List row : values)
////            {
////                System.out.printf("Species %s with ID %s\n",row.get(1),row.get(0));
////            }
////        }
////
////
////
////    }
//
//    private int indexOf(String[] arr, String str)
//    {
//        for(int i= 0; i < arr.length; i++)
//        {
//            if(arr[i].equals(str))
//                return i;
//        }
//        return -1;
//    }
//
//    private int indexOf(List<Sheet> list, String str)
//    {
//        for(int i= 0; i < list.size(); i++)
//        {
//            if(list.get(i).getProperties().getTitle().equals(str))
//                return i;
//        }
//        return -1;
//    }
//
//    private java.awt.Color convertColor(Color color)
//    {
//        if(color != null)
//        {
//            int r= (int) (color.getRed() != null ? color.getRed()*255 : 0);
//            int g= (int) (color.getGreen() != null ? color.getGreen()*255 : 0);
//            int b= (int) (color.getBlue() != null ? color.getBlue()*255 : 0);
//
//            return new java.awt.Color(r,g,b);
//        }
//        else
//            return java.awt.Color.GRAY;
//
//    }
//}
