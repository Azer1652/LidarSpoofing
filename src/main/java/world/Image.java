package world;

import com.sun.javafx.geom.Vec2f;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import static org.opencv.core.Core.line;
import static org.opencv.highgui.Highgui.*;
import static org.opencv.imgproc.Imgproc.*;


/**
 * Created by Peter on 28/02/2017.
 */
public class Image extends JFrame
{
    BufferedImage img, image, binaryImage = null;
    int[][] pixelData;
    double[] location = new double[]{0,0};
    double x, y, oldX = 0, oldY = 0;
    boolean mouseClicked = false;

    public List<double[]> vertex = new ArrayList<>();

    public void Image() {}



    public int[][] openImage()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(1000, 1000);


        String filename = "basic_localization_stage_ground_truth.png";
        String s = filename.substring(filename.lastIndexOf(".") + 1);
        System.out.println(s);

        if (Objects.equals(s, "png"))
        {
            openPNG(filename);
        } else if (Objects.equals(s, "tiff"))
        {
            openTIFF(filename);
        } else if (Objects.equals(s, "pgm"))
        {
            openPGM(filename);
        }

        System.out.println("image opened");

        System.out.println("Detecting edges...");
//        detectEdges();

        this.setVisible(true);

        return pixelData;
    }



    private void openPNG(String filename)
    {
        try
        {
            img = ImageIO.read(new File(filename));


        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        image = img.getSubimage(1400,1600,900,1000);
        binaryImage = image;

        pixelData = new int[image.getWidth()][image.getHeight()];
        int[] rgb;
        int rgbTot;


        //Create binary image: white = path, black = wall
        for (int j = 0; j < image.getHeight(); j++)
        {
            for (int i = 0; i < image.getWidth(); i++)
            {
                rgb = getPixelData(image, i, j);


                rgbTot = rgb[0] + rgb[1] + rgb[2];
                if (rgbTot <= 200)
                {
                    binaryImage.setRGB(i, j, Color.black.getRGB());
                    pixelData[i][j] = 0;
                    vertex.add(new double[]{i,0d,j});
                    vertex.add(new double[]{i,10d,j});
                } else
                {
                    binaryImage.setRGB(i, j, Color.white.getRGB());
                    pixelData[i][j] = 1;
                }
            }
        }
    }

    private void openTIFF(String filename)
    {
        try
        {
            ImageInputStream input = null;
            input = ImageIO.createImageInputStream(new File(filename));
            try
            {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext())
                {
                    throw new IllegalArgumentException("No reader for: " + filename);
                }

                ImageReader reader = readers.next();

                try
                {
                    reader.setInput(input);
                    ImageReadParam param = reader.getDefaultReadParam();
                    image = reader.read(0, param);
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

        for (int j = 0; j < image.getHeight(); j++)
            for (int i = 0; i < image.getWidth(); i++)
            {
                rgb = getPixelData(image, i, j);

                if (rgb[0] == 0 && rgb[1] == 40 && rgb[2] == 120)
                {
                    binaryImage.setRGB(i, j, Color.black.getRGB());
                    pixelData[i][j] = 0;
                    vertex.add(new double[]{i,0d,j});
                    vertex.add(new double[]{i,1000d,j});
                } else
                {
                    binaryImage.setRGB(i, j, Color.white.getRGB());
                    pixelData[i][j] = 1;
                }
            }
    }

    private void openPGM(String filename)
    {
        try
        {
            FileInputStream fis = new FileInputStream(filename);
            Scanner scan = new Scanner(fis);
            // Discard the magic number
            scan.nextLine();
            // Discard the comment line
            scan.nextLine();
            // Read pic width, height and max value
            int picWidth = scan.nextInt();
            int picHeight = scan.nextInt();
            int maxvalue = scan.nextInt();


            fis.close();

            // Now parse the file as binary data
            fis = new FileInputStream(filename);
            DataInputStream dis = new DataInputStream(fis);

            // look for 4 lines (i.e.: the header) and discard them
            int numnewlines = 4;
            while (numnewlines > 0)
            {
                char c;
                do
                {
                    c = (char) (dis.readUnsignedByte());
                } while (c != '\n');
                numnewlines--;
            }

            int[][] data2D = new int[picWidth][picHeight];
            pixelData = new int[picWidth][picHeight];

            binaryImage = new BufferedImage(4000, 4000, BufferedImage.TYPE_INT_ARGB);

            // read the image data
            for (int j = 0; j < picHeight; j++)
                for (int i = 0; i < picWidth; i++)
                {
                    data2D[i][j] = dis.readUnsignedByte();
                    if (data2D[i][j] > 205)
                    {
                        pixelData[i][j] = 1;
                        binaryImage.setRGB(i, j, Color.white.getRGB());
                    } else if (data2D[i][j] < 205)
                    {
                        pixelData[i][j] = 0;
                        binaryImage.setRGB(i, j, Color.black.getRGB());
                        vertex.add(new double[]{i,0d,j});
                        vertex.add(new double[]{i,1000d,j});
                    } else
                    {
                        pixelData[i][j] = 0;
                        binaryImage.setRGB(i, j, Color.gray.getRGB());
                    }
                }


        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


//    private void detectEdges()
//    {
//        // load the image
//        Mat img = imread("basic_localization_stage_ground_truth.png");
//
//        // generate gray scale and blur
//        Mat gray = new Mat();
//        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.blur(gray, gray, new Size(3, 3));
//
//        // detect the edges
//        Mat edges = new Mat();
//        int lowThreshold = 50;
//        int ratio = 3;
//        Imgproc.Canny(gray, edges, lowThreshold, lowThreshold * ratio);
//
//        Mat lines = new Mat();
//        Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180, 50, 50, 10);
//
//        for(int i = 0; i < lines.cols(); i++)
//        {
//            double[] val = lines.get(0, i);
//            Core.line(img, new Point(val[0], val[1]), new Point(val[2], val[3]), new Scalar(0, 0, 255), 2);
//        }
//
//        System.out.println(lines.get(0,0)[0] + " - " + lines.get(0,0)[1]);
//
//        System.out.println();



//
//        Mat src = imread("basic_localization_stage_ground_truth.png",0);
//        Mat dst = new Mat();
//        Mat cdst = new Mat();
//
//        ArrayList<Vec2f> lines = null;
//        Imgproc.HoughLines(src, lines, 1, Math.PI/180,100,0,0);
//
//        for( int i = 0; i < lines.size(); i++ )
//        {
//            float rho = lines[i][0], theta = lines[i][1];
//            Point pt1 = null, pt2 = null;
//            double a = Math.cos(theta), b = Math.sin(theta);
//            double x0 = a*rho, y0 = b*rho;
//            pt1.x = (int) Math.round(x0 + 1000*(-b));
//            pt1.y = (int) Math.round(y0 + 1000*(a));
//            pt2.x = (int) Math.round(x0 - 1000*(-b));
//            pt2.y = (int) Math.round(y0 - 1000*(a));
//            line( cdst, pt1, pt2, Scalar(0,0,255), 3, CV_AA);
//        }
//    }


    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
//        g.drawImage(image,0,0,null);
        g.drawImage(binaryImage, 0, 0, 1000, 1000, null);
    }

    public int[] getPixelData(BufferedImage img, int x, int y)
    {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[]{
                (argb >> 16) & 0xff, //red
                (argb >> 8) & 0xff, //green
                (argb) & 0xff  //blue
        };

//        System.out.println("rgb: " + rgb[0] + " " + rgb[1] + " " + rgb[2]);
        return rgb;
    }

    public void getMouse()
    {
            this.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    double x = e.getX();
                    double y = e.getY();
                    setLocationFromMouse(x,y);
                    System.out.println("Click!");
                }
            });
    }

    private void setLocationFromMouse(double tempX, double tempY)
    {
        mouseClicked = true;
        oldX = x;
        oldY = y;
        x = tempX;
        y = tempY;
        System.out.println("x: "+x+" y: "+y+" oldX: "+oldX+" oldY: "+oldY);
    }

    public double[] getLocationFromMouse()
    {

        return new double[]{x,y};
    }

    public boolean checkMouseClicked()
    {
        if(mouseClicked || oldX != x && oldY != y)
        {
            mouseClicked = false;
            oldX = x;
            oldY = y;
            return true;

        }
        return false;
    }

    public List<double[]> getVertex(){
        return vertex;
    }
}
