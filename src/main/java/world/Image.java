package world;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;

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



    public ArrayList<Segment> openImage()
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

        return houghLines(filename);
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

    private ArrayList<Segment> houghLines(String filename)
    {
        CvSeq lines = new CvSeq();
        IplImage src = cvLoadImage(filename, 0);
        IplImage between;
        IplImage dst;
        CvMemStorage storage = cvCreateMemStorage(0);
        CanvasFrame edge = new CanvasFrame("Edge");
        CanvasFrame hough = new CanvasFrame("Lines");
        OpenCVFrameConverter.ToIplImage edgeConverter = new OpenCVFrameConverter.ToIplImage();
        OpenCVFrameConverter.ToIplImage houghConverter = new OpenCVFrameConverter.ToIplImage();

        ArrayList<Segment> segments = new ArrayList<>();

        between = cvCreateImage(cvGetSize(src), src.depth(), 1);
        dst = cvCreateImage(cvGetSize(src), src.depth(), 3);
        //colorDst = cvCreateImage(cvGetSize(src), src.depth(), 3);

        cvCanny(src, between, 50, 200, 3);
        //cvCvtColor(dst, colorDst, CV_GRAY2BGR); // Only needed when the image is not in grayscale

        System.out.println("Using the Probabilistic Hough Transform");
        // 3th and 4th last parameter can be tweaked (param1 and 2 for probabilistic Hough transform: http://docs.opencv.org/2.4/modules/imgproc/doc/feature_detection.html?highlight=houghlines#houghlines
        lines = cvHoughLines2(between, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 40, 5, 20, 0, CV_PI);
        for (int i = 0; i < lines.total(); i++) {
            Pointer line = cvGetSeqElem(lines, i);
            CvPoint pt1  = new CvPoint(line).position(0);
            CvPoint pt2  = new CvPoint(line).position(1);

            System.out.println("Line spotted: ");
            System.out.println("\t pt1: " + pt1);
            System.out.println("\t pt2: " + pt2);
            cvLine(dst, pt1, pt2, CV_RGB(255, 0, 0), 1, CV_AA, 0); // draw the segment on the image
            segments.add(new Segment(new double[]{pt1.x(), pt1.y()},new double[]{pt2.x(), pt2.y()}));
        }

        //Uncomment to see the edge detection and Hough transform result
        //edge.showImage(edgeConverter.convert(between));
        //hough.showImage(houghConverter.convert(dst));

        return segments;
    }

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
