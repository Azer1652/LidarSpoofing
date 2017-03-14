package world;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Peter on 28/02/2017.
 */
public class Image extends JFrame
{
    BufferedImage img, image, binaryImage = null;

    public void Image() {}

    public int[][] openImage()
    {
        try
        {
            img = ImageIO.read(new File("basic_localization_stage_ground_truth.png"));
            System.out.println("image opened");
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        image = img.getSubimage(1400,1600,900,1000);
        binaryImage = image;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(900, 1000);
        this.setVisible(true);


        int[][] pixelData = new int[image.getWidth()][image.getHeight()];
        int[] rgb;
        int rgbTot = 0;


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

                if(i==0 && j==0 || i==300 && j==250)
                {
                    System.out.println("Red = "+rgb[0]+"\tGreen = "+rgb[1]+"\tBlue = "+rgb[2]);
                    System.out.println("Pixeldata: "+pixelData[i][j]);
                }

            }
        }

        return pixelData;
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
