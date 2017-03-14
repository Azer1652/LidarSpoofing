package world;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

/**
 * Created by Peter on 28/02/2017.
 */
public class Image extends JFrame
{
    BufferedImage img, image, binaryImage = null;
    int[][] pixelData;

    public void Image() {}

    public int[][] openImage()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(1900, 1000);


        String filename = "hector_slam_map_14-18-36.tiff";
        String s = filename.substring(filename.lastIndexOf(".") + 1);
        System.out.println(s);

        if(Objects.equals(s, "png"))
        {
            openPNG(filename);
        }

        else if(Objects.equals(s,"tiff"))
        {
            openTIFF(filename);
        }

        System.out.println("image opened");

        this.setVisible(true);

        return pixelData;
    }

    private void openPNG(String filename)
    {
        try
        {
            img = ImageIO.read(new File(filename));

            image = img.getSubimage(1400,1600,900,1000);
            binaryImage = image;
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        pixelData = new int[image.getWidth()][image.getHeight()];
        int[] rgb;
        int rgbTot;


        //Create binary image: white = path, black = wall
        for(int j = 0; j <= image.getHeight()-1; j++){
            for(int i = 0; i <= image.getWidth()-1; i++){
                rgb = getPixelData(image, i, j);



                rgbTot = rgb[0] + rgb[1] + rgb[2];
                if (rgbTot<=700)
                {
                    binaryImage.setRGB(i, j, Color.black.getRGB());
                    pixelData[i][j] = 0;
                }
                else
                {
                    binaryImage.setRGB(i, j, Color.white.getRGB());
                    pixelData[i][j] = 1;
                }
            }
        }
    }

    private void openTIFF(String filename)
    {
        try{
            ImageInputStream input = null;
            input = ImageIO.createImageInputStream(new File(filename));
            try
            {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if(!readers.hasNext())
                {
                    throw new IllegalArgumentException("No reader for: "+filename);
                }

                ImageReader reader = readers.next();

                try
                {
                    reader.setInput(input);
                    ImageReadParam param = reader.getDefaultReadParam();
                    image = reader.read(0,param);
                    binaryImage = image;
                }
                finally
                {
                    reader.dispose();
                }
            }
            finally
            {

                input.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        pixelData = new int[image.getWidth()][image.getHeight()];
        int[] rgb;

        for(int j = 0; j<=image.getHeight()-1; j++)
            for(int i=0; i<=image.getWidth()-1; i++)
            {
                rgb = getPixelData(image, i, j);

                if(rgb[0] == 0 && rgb[1] == 40 && rgb[2] == 120)
                {
                    binaryImage.setRGB(i, j, Color.black.getRGB());
                    pixelData[i][j] = 0;
                }
                else
                {
                    binaryImage.setRGB(i, j, Color.white.getRGB());
                    pixelData[i][j] = 1;
                }
            }
    }


    @Override
    public void paint(Graphics g){
        super.paint(g);
//        g.drawImage(image,0,0,null);
        g.drawImage(binaryImage,0,0,null);
    }

    public int[] getPixelData(BufferedImage img, int x, int y)
    {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[] {
                (argb >> 16) & 0xff, //red
                (argb >>  8) & 0xff, //green
                (argb      ) & 0xff  //blue
        };

//        System.out.println("rgb: " + rgb[0] + " " + rgb[1] + " " + rgb[2]);
        return rgb;
    }
}
