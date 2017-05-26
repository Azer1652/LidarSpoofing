package world;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

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

    public Image() {}

    /**
     *
     * @return arraylist of Segments
     */
    public ArrayList<Segment> openImage()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.setSize(1000, 1000);

//        String filename = "gangmap.png";
//       String filename = "gangmap.pgm";
//        String filename = "hector_slam_map_14-00-31.tiff";
//        String filename = "hector_slam_map_14-18-36.tiff";
        String filename = "zbuilding.pgm";
        String s = filename.substring(filename.lastIndexOf(".") + 1);
        System.out.println(s);
        int imageScale = 1;

        if (Objects.equals(s, "png"))
        {
            openPNG(filename);
            imageScale = 20; // 1m per 20 pixels
        } else if (Objects.equals(s, "tiff"))
        {
            openTIFF(filename);
            imageScale = 60; // 1m per 60 pixels
        } else if (Objects.equals(s, "pgm"))
        {
            openPGM(filename);
            imageScale = 20; // 1m per 20 pixels
        }

        System.out.println("image opened");

        System.out.println("Detecting edges...");
//        detectEdges();

        this.setVisible(true);

        return houghLines(imageScale);
    }

    /**
     *
     * @param filename
     */
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

        //image = img.getSubimage(1400,1600,900,1000);
        image = img;
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
                if (rgbTot <= 700)
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

    /**
     *
     * @param filename
     */
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

                if (rgb[0] == 0 && rgb[1] == 40 && rgb[2] == 120 ||
                    rgb[0] == 226 && rgb[1] == 226 && rgb[2] == 227 ||
                    rgb[0] == 237 && rgb[1] == 237 && rgb[2] == 238)
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

        for(int a = 0; a<300; a++)
            for(int b = 0; b<135; b++)
                binaryImage.setRGB(a, b, Color.black.getRGB());
        BufferedImage convertedImage = new BufferedImage(binaryImage.getWidth(),binaryImage.getHeight(),10); // PNG TYPE 10!
        convertedImage.getGraphics().drawImage(binaryImage,0,0,null);
        binaryImage = convertedImage;
}

    /**
     *
     * @param filename
     */
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
            for (int j = picHeight-1; j >= 0; j--)
                for (int i = 0; i < picHeight; i++)
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

            BufferedImage convertedImage = new BufferedImage(binaryImage.getWidth(),binaryImage.getHeight(),10); // PNG TYPE 10!
            convertedImage.getGraphics().drawImage(binaryImage,0,0,null);
            binaryImage = convertedImage;

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param imageScale
     * @return
     */
    private ArrayList<Segment> houghLines(int imageScale)
    {
        CvSeq lines;
        OpenCVFrameConverter.ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
        CvMemStorage storage = cvCreateMemStorage(0);
        CanvasFrame C1 = new CanvasFrame("Edge");
        CanvasFrame C2 = new CanvasFrame("Dilation");
        CanvasFrame C3 = new CanvasFrame("Lines");
        OpenCVFrameConverter.ToIplImage C1Converter = new OpenCVFrameConverter.ToIplImage();
        OpenCVFrameConverter.ToIplImage C2Converter = new OpenCVFrameConverter.ToIplImage();
        OpenCVFrameConverter.ToIplImage C3Converter = new OpenCVFrameConverter.ToIplImage();
        ArrayList<Segment> segments = new ArrayList<>();
        IplImage src = iplConverter.convert(java2DFrameConverter.convert(binaryImage)); // Convert from binaryImage format
        IplImage dst = cvCreateImage(cvGetSize(src), src.depth(), 3); // Destination image

        cvNot(src,src); // Invert image: Black to white, White to black
        cvCanny(src, src, 50, 200, 3); // Canny edge detection
        C1.showImage(C1Converter.convert(src));
        IplConvKernel element = cvCreateStructuringElementEx(2,2,0,0,CV_SHAPE_RECT); // rectangle, 2x2 size
        cvDilate(src,src,element,1); // Dilate once to thicken the pixels for better houghlines recognition
        C2.showImage(C2Converter.convert(src));

        // Using Hough Probabilistic transform: http://docs.opencv.org/2.4/modules/imgproc/doc/feature_detection.html?highlight=houghlines#houghlines
        // Param. 6: Threshold (20), Param. 7: Minimum Line Length (1, lower has no effect), Param. 8: Max Line Gap (10)
        lines = cvHoughLines2(src, storage, CV_HOUGH_PROBABILISTIC, 1, Math.PI / 180, 20, 1, 10, 0, CV_PI);
        for (int i = 0; i < lines.total(); i++) {
            Pointer line = cvGetSeqElem(lines, i);
            CvPoint pt1  = new CvPoint(line).position(0);
            CvPoint pt2  = new CvPoint(line).position(1);
            cvLine(dst, pt1, pt2, CV_RGB(255, 0, 0), 1, CV_AA, 0); // draw the segment on the image (for own check)
            segments.add(new Segment(new double[]{(double) pt1.x()/imageScale,(double) pt1.y()/imageScale},new double[]{(double) pt2.x()/imageScale,(double) pt2.y()/imageScale})); // Adding segments to list, rescaling if needed
        }
        C3.showImage(C3Converter.convert(dst));

        return segments;
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        g.drawImage(binaryImage, 0, 0, 1000, 1000, null);
    }

    /**
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
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

    /**
     *
     * @param X
     * @param Y
     */
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
