package world;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import java.io.File;
import java.util.ArrayList;
import static java.lang.Math.*;

/**
 * This class coverts a 3D world of walls to an SDF file that is readable by Gazebo
 * SDF created by Jan De Laet on 23/03/2017.
 */
public class SDF
{
    private Document doc;
    private Element model;

    // When LidarSpoof is in Random mode

    /**
     *
     * @param segments
     * @param randomRange
     */
    public SDF(ArrayList<Segment> segments, int randomRange)
    {
        try
        {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.newDocument();

            header();
            int i;
            for(i=0; i<segments.size();i++)
                convertSegment(segments.get(i),i,0);
            randomBorder(randomRange+1,i);
            output();
        }
        catch (ParserConfigurationException | TransformerException e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param segments
     * @param origin
     */
    public SDF(ArrayList<Segment> segments, double origin)
    {
        try
        {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.newDocument();

            header();
            int i;
            for(i=0; i<segments.size();i++)
                convertSegment(segments.get(i),i,origin);
            output();
        }
        catch (ParserConfigurationException | TransformerException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Add static header for SDF format
     */
    private void header()
    {
        Element sdf = doc.createElement("sdf");
        doc.appendChild(sdf);
        addAttribute(sdf,"version","1.6");

        Element world = addChild(sdf,"world");
        addAttribute(world,"name","default");

        Element include1 = addChild(world,"include");
        Element uri1 = addChild(include1,"uri");
        addElementText(uri1,"model://ground_plane");

        Element include2 = addChild(world,"include");
        Element uri2 = addChild(include2,"uri");
        addElementText(uri2,"model://sun");

        Element include3 = addChild(world,"include");
        Element uri3 = addChild(include3,"uri");
        addElementText(uri3,"/home/ros/catkin_ws/src/F1_Gazebo/worlds/");
        Element pose1 = addChild(include3,"pose");
        addElementText(pose1,"0 0 0 0 0 3.14");

        model = addChild(world,"model");
        addAttribute(model,"name","Worldsegments");

        Element STATIC = addChild(model,"static");
        addElementText(STATIC,"1");

        Element pose2 = addChild(model,"pose");
        addAttribute(pose2,"frame","");
        addElementText(pose2,"0 0 0 0 0 0"); // Center, no rotation
    }

    /**
     * casting to floats to limit the amount digits
     */
    private void convertSegment(Segment s, int i, double origin)
    {
        double xd = s.direction[0]; // Difference of points on X-axis
        double yd = s.direction[1]; // Difference of points on Y-axis
        double xm = s.start[0]+xd/2; // X position half between points
        double ym = s.start[1]+yd/2; // Y position half between points
        float length = (float) Math.sqrt(xd*xd+yd*yd); // Pythagoras
        double width = 0.1; // Wallwidth = free to choose (10 cm binnenmuur ongeveer)
        double height = 2.5; // Wallheight = free to choose (250 cm hoogte binnenhuis)
        float angle = (float) atan(yd/xd); // in radialen - TAN: overstaande / aanliggende zijde

        Element link = addChild(model,"link");
        addAttribute(link,"name","Wall_" + i);

        Element pose = addChild(link,"pose");
        addAttribute(pose,"frame","");
        addElementText(pose,(xm+origin) + " " + (ym+origin) + " " + height/2 + " 0 0 " + angle); // X Y Z Roll(X) Pitch(Y) Yaw(Z) (center of the box)

        Element collision = addChild(link,"collision");
        addAttribute(collision,"name","Wall_" + i + "_Collision");

        Element visual = addChild(link,"visual");
        addAttribute(visual,"name","Wall_" + i + "_Visual");

        Element geometry,box,size;
        for(int j=0; j<2; j++)
        {
            if(j == 0)
                geometry = addChild(collision, "geometry");
            else
                geometry = addChild(visual,"geometry");

            box = addChild(geometry,"box");
            size = addChild(box,"size");
            addElementText(size,length + " " + width + " " + height); // Length Width Height (distance)
        }
    }

    /**
     *
     * @param range
     * @param i
     */
    private void randomBorder(int range, int i)
    {
        convertSegment(new Segment(new double[]{-range, range},new double[]{range, range}),i+1,0);
        convertSegment(new Segment(new double[]{-range, -range},new double[]{range, -range}),i+2,0);
        convertSegment(new Segment(new double[]{-range, -range},new double[]{-range, range}),i+3,0);
        convertSegment(new Segment(new double[]{range, -range},new double[]{range, range}),i+4,0);
    }

    /**
     * Write SDF format to a file called worldmap.world
     * @throws TransformerException
     */
    private void output() throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, new StreamResult(new File("worldmap.world")));
        transformer.transform(source, new StreamResult(System.out)); // Output to console for testing
    }

    /**
     * In xml format: add child of an element
     * @param element
     * @param name
     * @return
     */
    private Element addChild(Element element, String name)
    {
        Element child = doc.createElement(name);
        element.appendChild(child);
        return child;
    }

    /**
     * In xml format: add attribute (name-value pair)
     * @param element
     * @param name
     * @param value
     */
    private void addAttribute(Element element, String name, String value)
    {
        Attr attribute = doc.createAttribute(name);
        attribute.setValue(value);
        element.setAttributeNode(attribute);
    }

    /**
     * In xml format: add element tag
     * @param element
     * @param text
     */
    private void addElementText(Element element, String text)
    {
        element.appendChild(doc.createTextNode(text));
    }
}
