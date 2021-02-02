package com.turtleisaac.pokeditor.utilities.ncgr;

public interface NcgrData
{
    Header getHeader();
    CharacterData getCharacterData();
    CharacterPosition getCharacterPosition();

    interface Header
    {
        String getMagicId();
        int getEndianness();
        int getConstant();
        long getFileSize();
        int getHeaderSize();
        int getNumSections();
    }

    /**
     * Section 1 - CHAR - Character Data
     */
    interface CharacterData
    {
        String getMagicId();
        long getSectionSize();
        int getNumTiles_Y();
        int getNumTiles_X();
        long getTileBitDepth();
        short getUnknown1();
        short getUnknown2();
        TileFormat getTileOrder(); //read four bytes
        long getTileDataSize();
        int getUnknown3();
    }

    /**
     * Section 2 - CPOS - Character Position
     */
    interface CharacterPosition
    {
        String getMagicId();
        long getSectionSize();
        int getUnknown1();
        int getCharacterSize();
        int getNumCharacters();
    }
}
