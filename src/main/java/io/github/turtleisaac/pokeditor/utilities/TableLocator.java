//package io.github.turtleisaac.pokeditor.utilities;
//
//import io.github.turtleisaac.pokeditor.editors.overlays.OverlayData;
//import io.github.turtleisaac.pokeditor.project.Project;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * Used for locating relocated tables for things such as types, trainer class prize money, legendary encounter music, trainer class genders, and more
// *
// * Order of Operations:
// * 1. Goes to the pointer offset in the arm9/ overlays that contains the four-byte pointer to the location of the table in memory
// * 2. Reads the table offset in little endian (least significant byte to most significant byte) (right to left)
// * 3. Compares the table offset to the starting offset of the arm9, overlays, and (when applicable) synthetic overlays in RAM (ARM9 is 0x02000000, Overlays are read from the ROM, Synthetic Overlay is 0x023C8000)
// * 4. Finds the arm9/ overlay/ synthetic overlay that the table offset is within by seeing if the pointer is greater than the starting offset of the file and less than the ending offset of the file
// * 5. Subtracts the starting offset of the file the table is in (see step 5) from the table offset to produce the offset of the pointer within the file
// * 6. Reads the table from the file and returns its contents
// */
//public class TableLocator
//{
//    private Project project;
//
//    private ArrayList<OverlayData> overlayEntries;
//    String romDataPath;
//
//    public TableLocator(Project project)
//    {
//        this.project= project;
//        romDataPath= new File(project.getDataPath()).getParent() + File.separator;
//        readOverlays();
//    }
//
//    /**
//     * Obtains the table
//     * @param pointerLocation a representation of the file where the table pointer is located and the offset in the file where the table pointer is located
//     * @param numEntries (if applicable) the number of entries to read from the file
//     * @param entryLength (if applicable) the length of each entry
//     * @return a temp File containing the table
//     */
//    public File obtainTable(TablePointers.TablePointer pointerLocation, int numEntries, int entryLength) throws IOException
//    {
//        byte[] arr= obtainTableArr(pointerLocation,numEntries,entryLength);
//
//        File ret=  File.createTempFile("LocatedTable",null);
//        BinaryWriter writer= new BinaryWriter(ret);
//        writer.write(arr);
//
//        return ret;
//    }
//
//    /**
//     * Obtains the table
//     * @param pointerLocation a representation of the file where the table pointer is located and the offset in the file where the table pointer is located
//     * @param numEntries (if applicable) the number of entries to read from the file
//     * @param entryLength (if applicable) the length of each entry
//     * @return a byte[] containing the table
//     */
//    public byte[] obtainTableArr(TablePointers.TablePointer pointerLocation, int numEntries, int entryLength)
//    {
//        //TODO add more specialized support for arm9/ overlay decompression
//
//        String pointerFilePath= romDataPath + pointerLocation.file;
//
//        boolean compressed_pointer_source= false;
//
//        if(pointerFilePath.contains("overlay_"))
//        {
//            String pointerFileIdString= pointerFilePath.substring(pointerFilePath.indexOf("overlay_"));
//            pointerFileIdString= pointerFileIdString.substring(0,pointerFileIdString.indexOf(".bin"));
//
//            int pointerFileId= Integer.parseInt(pointerFileIdString);
//
//            compressed_pointer_source= overlayEntries.get(pointerFileId).getReserved() == 0;
//        }
//        else if (pointerFilePath.contains("arm9"))
//        {
//            Buffer arm9Buffer = new Buffer(pointerFilePath);
//            switch (Project.parseBaseRom(project.getBaseRomGameCode()))
//            {
//                case Platinum:
//                case HeartGold:
//                case SoulSilver:
//                    arm9Buffer.skipTo(0xbb4);
//                    compressed_pointer_source = arm9Buffer.readInt() != 0;
//                    break;
//
//                case Diamond:
//                case Pearl:
//                    arm9Buffer.skipTo(0xb7c);
//                    compressed_pointer_source = arm9Buffer.readInt() != 0;
//                    break;
//            }
//        }
//
//
//        if(compressed_pointer_source) //decompresses the file where the pointer is located
//        {
//            BLZCoder coder= new BLZCoder(null);
//            byte[] decoded= coder.BLZ_DecodePub(Buffer.readFile(pointerFilePath),"file");
//
//            try
//            {
//                File tempDir = new File(project.getProjectPath().getAbsolutePath() + File.separator + "temp");
//                tempDir.mkdir();
//                File tempFile= File.createTempFile("pokeditor_file_",".bin", tempDir);
//                tempFile.deleteOnExit();
//                BinaryWriter.writeFile(tempFile,decoded);
//                pointerFilePath= tempFile.getAbsolutePath();
//            }
//            catch (IOException exception)
//            {
//                exception.printStackTrace();
//            }
//        }
//
//        Buffer buffer= new Buffer(pointerFilePath);
//        buffer.skipTo(pointerLocation.pointerOffset);
//        long offset= buffer.readUInt32();
//
//        int bytesToRead= 0;
//        if(pointerLocation.countOffset != 0)
//        {
//            buffer= new Buffer(pointerFilePath);
//            buffer.skipTo(pointerLocation.countOffset);
//            switch (pointerLocation.countPointerLength)
//            {
//                case 1:
//                    bytesToRead= buffer.readByte();
//                    break;
//
//                case 2:
//                    bytesToRead= buffer.readUInt16();
//                    break;
//
//                case 4:
//                    bytesToRead= buffer.readInt();
//                    break;
//            }
//        }
//
//        if(bytesToRead == 0)
//            bytesToRead = numEntries*entryLength;
//
//        TableLocation tableLocation= locateContainerFile(offset);
////        System.out.println("File: " + tableLocation.getFile());
////        System.out.println("Table Offset: 0x" + Long.toHexString(tableLocation.getOffset()));
////        System.out.println("Bytes/ Entries to read: " + bytesToRead);
//
//
//        String tableFilePath= tableLocation.getFile();
//
//
//
//        boolean compressed_table_source= false;
//
//        if(tableFilePath.contains("overlay_"))
//        {
//            String tableFileIdString= tableFilePath.substring(tableFilePath.indexOf("overlay_"));
//            tableFileIdString= tableFileIdString.substring(0,tableFileIdString.indexOf(".bin"));
//
//            int pointerFileId= Integer.parseInt(tableFileIdString);
//
//            compressed_table_source= overlayEntries.get(pointerFileId).getReserved() == 0;
//        }
//        else if (tableFilePath.contains("arm9"))
//        {
//            Buffer arm9Buffer = new Buffer(tableFilePath);
//            switch (Project.parseBaseRom(project.getBaseRomGameCode()))
//            {
//                case Platinum:
//                case HeartGold:
//                case SoulSilver:
//                    arm9Buffer.skipTo(0xbb4);
//                    compressed_table_source = arm9Buffer.readInt() != 0;
//                    break;
//
//                case Diamond:
//                case Pearl:
//                    arm9Buffer.skipTo(0xb7c);
//                    compressed_table_source = arm9Buffer.readInt() != 0;
//                    break;
//            }
//        }
//
//
//        if(compressed_table_source) //decompresses the file where the table is located
//        {
//            BLZCoder coder= new BLZCoder(null);
//            byte[] decoded= coder.BLZ_DecodePub(Buffer.readFile(tableFilePath),"file");
//
//            try
//            {
//                File tempDir = new File(project.getProjectPath().getAbsolutePath() + File.separator + "temp");
//                tempDir.mkdir();
//                File tempFile= File.createTempFile("pokeditor_file_",".bin", tempDir);
//                tempFile.deleteOnExit();
//                BinaryWriter.writeFile(tempFile,decoded);
//                tableFilePath= tempFile.getAbsolutePath();
//            }
//            catch (IOException exception)
//            {
//                exception.printStackTrace();
//            }
//        }
//
//        buffer= new Buffer(tableFilePath);
//        buffer.skipTo(tableLocation.getOffset());
//        byte[] ret= buffer.readBytes(bytesToRead);
//
//        return ret;
//    }
//
//    /**
//     * Reads all of the overlays in the ROM to obtain their starting and ending offsets, among other things
//     */
//    private void readOverlays()
//    {
//        overlayEntries= new ArrayList<>();
//        Buffer buffer= new Buffer(romDataPath + "arm9ovltable.bin");
//
//        int numOverlays= buffer.getLength()/32;
////        System.out.println("Number of Overlays: " + numOverlays + "\n");
//
//        for(int i= 0; i < numOverlays; i++)
//        {
//            long overlayId= buffer.readUInt32();
//
//            long ramAddress= buffer.readUInt32();
//            long ramSize= buffer.readUInt32();
//
//            long bssSize= buffer.readUInt32();
//            long staticInitStart= buffer.readUInt32();
//            long staticInitEnd= buffer.readUInt32();
//            long fileId= buffer.readUInt32();
//            int reserved= buffer.readInt();
//
////            System.out.println("Overlay ID: " + overlayId);
////            System.out.println("Ram Address: " + ramAddress);
////            System.out.println("Ram Size: " + ramSize);
////            System.out.println("BSS Size: " + bssSize);
////            System.out.println("Static Initializer Start Address: " + staticInitStart);
////            System.out.println("Static Initializer End Address: " + staticInitStart);
////            System.out.println("File ID: " + fileId + "\n");
////            System.out.println("Reserved: " + Integer.toHexString(reserved));
//
//            overlayEntries.add(new OverlayData()
//            {
//                @Override
//                public long getOverlayId()
//                {
//                    return overlayId;
//                }
//
//                @Override
//                public long getRamAddress()
//                {
//                    return ramAddress;
//                }
//
//                @Override
//                public long getRamSize()
//                {
//                    return ramSize;
//                }
//
//                @Override
//                public long getBssSize()
//                {
//                    return bssSize;
//                }
//
//                @Override
//                public long getStaticInitStart()
//                {
//                    return staticInitStart;
//                }
//
//                @Override
//                public long getStaticInitEnd()
//                {
//                    return staticInitEnd;
//                }
//
//                @Override
//                public long getFileId()
//                {
//                    return fileId;
//                }
//
//                @Override
//                public int getReserved()
//                {
//                    return reserved;
//                }
//            });
//        }
//
//    }
//
//
//    /**
//     * Locates the file containing the table and the offset of the table in that file
//     * @param offset the RAM address of the table
//     * @return a TableLocation object representing the file containing the table and the offset of that table in the file
//     */
//    private TableLocation locateContainerFile(long offset)
//    {
//        for(int i= 0; i < overlayEntries.size(); i++)
//        {
//            long start= overlayEntries.get(i).getRamAddress();
//            long end= start + overlayEntries.get(i).getRamSize();
//
//            if(offset > start && offset < end)
//            {
//                int finalI = i;
//                return new TableLocation()
//                {
//                    @Override
//                    public String getFile()
//                    {
//                        return romDataPath + "overlay_" + File.separator + fixNum(finalI) + ".bin";
//                    }
//
//                    @Override
//                    public long getOffset()
//                    {
//                        return offset - start;
//                    }
//                };
//            }
//        }
//
//        if(offset > 0x023C8000)
//        {
//            return new TableLocation()
//            {
//                @Override
//                public String getFile()
//                {
//                    return romDataPath + "data" + File.separator + "weather_sys" + File.separator + "9.rlcn";
//                }
//
//                @Override
//                public long getOffset()
//                {
//                    return offset - 0x023C8000;
//                }
//            };
//        }
//        else if(offset > 0x02000000)
//        {
//            return new TableLocation()
//            {
//                @Override
//                public String getFile()
//                {
//                    return romDataPath + "arm9.bin";
//                }
//
//                @Override
//                public long getOffset()
//                {
//                    return offset - 0x02000000;
//                }
//            };
//        }
//        else
//            throw new RuntimeException("Invalid RAM address");
//    }
//
//
//    /**
//     * Prepends a integer value with 0's for use in identifying the overlay files output by jNdstool
//     * @param num the integer value to prepend with 0's
//     * @return a String of length 4 representing the integer
//     */
//    private String fixNum(int num)
//    {
//        StringBuilder strBuilder = new StringBuilder(num);
//        while(strBuilder.length() != 4)
//        {
//            strBuilder.insert(0, "0");
//        }
//        return strBuilder.toString();
//    }
//}
