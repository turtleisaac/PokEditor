package io.github.turtleisaac.pokeditor.utilities.images;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Hashtable;

public class CenterImage extends BufferedImage
{
    public CenterImage(int width, int height, int imageType)
    {
        super(width, height, imageType);
    }

    public CenterImage(int width, int height, int imageType, IndexColorModel cm)
    {
        super(width, height, imageType, cm);
    }

    public CenterImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties)
    {
        super(cm, raster, isRasterPremultiplied, properties);
    }

    @Override
    public synchronized void setRGB(int x, int y, int rgb)
    {
        x+= getWidth()/2;
        y= -y;
        y+= getHeight()/2;

        super.setRGB(x, y, rgb);
    }

    @Override
    public int getRGB(int x, int y)
    {
        x+= getWidth()/2;
        y= -y;
        y+= getHeight()/2;

        return super.getRGB(x, y);
    }

    public synchronized void setSection(int x, int y, BufferedImage image)
    {
        x+= getWidth()/2;
        y= -y;
        y+= getHeight()/2;

        System.out.println("(" + x + ", " + y + ")");

        ArrayList<Integer> list= new ArrayList<>();

        for(int row= 0; row < image.getHeight(); row++)
        {
            for(int col= 0; col < image.getWidth(); col++)
            {
                list.add(image.getRGB(col,row));
            }
        }

        int[] rgbArr= list.stream().mapToInt(Integer::intValue).toArray();
        super.setRGB(x,y,image.getWidth(),image.getHeight(),rgbArr,0,1);
    }
}
