package world;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static java.lang.Math.*;


/**
 * SDF created by Jan De Laet on 23/03/2017.
 */
public class SDF
{
    DocumentBuilder dBuilder;
    Document doc;
    Element model;

    public SDF(ArrayList<Segment> segments)
    {
        try
        {
            dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.newDocument();

            header();
            for(int i=0; i<segments.size();i++)
                convertSegment(segments.get(i),i);
            output();
        }
        catch (ParserConfigurationException | TransformerException e)
        {
            e.printStackTrace();
        }
    }

    private void header()
    {
        Element sdf = doc.createElement("sdf");
        doc.appendChild(sdf);
        addAttribute(sdf,"version","1.6");

        model = addChild(sdf,"model");
        addAttribute(model,"name","Worldsegments");

        Element STATIC = addChild(model,"static");
        addElementText(STATIC,"1");

        Element pose = addChild(model,"pose");
        addAttribute(pose,"frame","");
        addElementText(pose,"0 0 0 0 0 0"); // Center, no rotation
    }

    // casting to floats to limit the amount digits
    private void convertSegment(Segment s, int i)
    {
        double xd = s.direction[0]; // Difference of points on X-axis
        double yd = s.direction[1]; // Difference of points on Y-axis
        double xm = s.start[0]+xd/2; // X position half between points
        double ym = s.start[1]+yd/2; // Y position half between points
        float length = (float) Math.sqrt(xd*xd+yd*yd); // Pythagoras
        double width = 0.5; // Wallwidth = free to choose
        double height = 10; // Wallheight = free to choose
        float angle = (float) atan(yd/xd); // in radialen - TAN: overstaande / aanliggende zijde

        Element link = addChild(model,"link");
        addAttribute(link,"name","Wall_" + i);

        Element pose = addChild(link,"pose");
        addAttribute(pose,"frame","");
        addElementText(pose,xm + " " + ym + " " + height/2 + " 0 0 " + angle); // X Y Z Roll(X) Pitch(Y) Yaw(Z) (center of the box)

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

    private void output() throws TransformerException
    {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, new StreamResult(new File("worldmap.sdf")));
        transformer.transform(source, new StreamResult(System.out)); // Output to console for testing
    }

    private Element addChild(Element element, String name)
    {
        Element child = doc.createElement(name);
        element.appendChild(child);
        return child;
    }

    private void addAttribute(Element element, String name, String value)
    {
        Attr attribute = doc.createAttribute(name);
        attribute.setValue(value);
        element.setAttributeNode(attribute);
    }

    private void addElementText(Element element, String text)
    {
        element.appendChild(doc.createTextNode(text));
    }
}
