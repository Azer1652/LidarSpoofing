package world;

import tracing.Hit;
import tracing.Ray;

import java.awt.event.KeyEvent;
import java.util.*;

public class World {

    //This value is supposed to be divided by -pi/4, but it generates an offset if you don't substract 0.1
    private final static double angleStartRad = +Math.toRadians(135); //-0.28;
    //private final static double angleEndRad = Math.PI-angleStartRad;
    private final static double angleDiffRad = Math.toRadians(270)/(1080);

    private int[][] pixelData = new int[4000][4000];

	private List<Segment> segments = new ArrayList<>();
    //List<double[]> uniquePoints = new ArrayList<>();
	private double data[] = new double[1080];
    private StringBuilder dataString = new StringBuilder();

    private double[] carLocation = new double[]{300,250};
    //private double[] carLocation = new double[]{0,0};
    private double currentCarAngleRad = 0;

    //public double speed = 50;
    public int speed = 1; //in pixels
    public double turnSpeed = 0.05;

    private boolean moveLikeCar = true;
    private boolean up, down, left, right;

	public World(){
	    up = false;
        down = false;
        left = false;
        right = false;
		//buildWorld();
        getWorldFromImage();
		//generatePoints();
		updateWorld();
		encodeData();
	}
	
	public String getDataString(){
	    updateWorld();
	    encodeData();
	    return dataString.toString();
    }

    private Hit tracePixel(double angle){
        //set direction
        Ray ray = new Ray(angle+currentCarAngleRad);
        ray.setLocation(carLocation);
        Hit hit = ray.hitPixel(pixelData);
        return hit;
    }

    private Hit trace(double angle){
        //todo remove cos and sin by something simpler
        double dx = Math.cos(angle+currentCarAngleRad);
        double dy = Math.sin(angle+currentCarAngleRad);

        //set direction
        Ray ray = new Ray();
        ray.setLocation(carLocation);
        ray.setDirection(dx, dy);

        //find closest intersection
        Hit bestHit = null;
        for(Segment s: segments){
            Hit hit = ray.hit(s);
            if(hit != null) {
                if (bestHit == null || (hit.getTime() > 0 && hit.getTime() < bestHit.getTime())) {
                    bestHit = hit;
                }
            }
        }
        return bestHit;
    }

    public double getCarHeadingDeg(){
        return currentCarAngleRad*180/Math.PI;
    }

    public double[] getCarLocation(){
        return carLocation;
    }

    public List<Segment> getSegments(){
        return segments;
    }

	synchronized private void updateWorld(){
        double current = angleStartRad;

        if(!moveLikeCar) {
            if (up)
                carLocation[1] += speed;
            if (down)
                carLocation[1] -= speed;
            if (left)
                carLocation[0] -= speed;
            if (right)
                carLocation[0] += speed;
        }else{
            if (up) {
                //move in direction
                carLocation[0] += Math.cos(currentCarAngleRad) * speed;
                carLocation[1] += Math.sin(currentCarAngleRad) * speed;
            }
            if (down) {
                carLocation[0] -= Math.cos(currentCarAngleRad) * speed;
                carLocation[1] -= Math.sin(currentCarAngleRad) * speed;
            }
            if (left){
                currentCarAngleRad -= turnSpeed;
            }
            if (right) {
                currentCarAngleRad += turnSpeed;
            }
        }

        int i = 0;

        while (i < 1080) {
            //calculate an intersect for each angle
            //Hit hit = trace(current);
            Hit hit = tracePixel(current);
            data[i]=hit.getTime();
            current -= angleDiffRad;
            i++;
        }
    }

	synchronized public void move(KeyEvent e){
        /*
	    switch(e.getKeyChar()){
            case 'z':
                carLocation[1] += 100;
                break;
            case 's':
                carLocation[1] -= 100;
                break;
            case 'q':
                carLocation[0] -= 100;
                break;
            case 'd':
                carLocation[0] += 100;
                break;
        }*/
    }

    synchronized public void setKey(KeyEvent e){
        switch(e.getKeyChar()){
            case 'z':
                up = true;
                break;
            case 's':
                down = true;
                break;
            case 'q':
                left = true;
                break;
            case 'd':
                right = true;
                break;
        }
    }

    synchronized public void removeKey(KeyEvent e){
        switch(e.getKeyChar()){
            case 'z':
                up = false;
                break;
            case 's':
                down = false;
                break;
            case 'q':
                left = false;
                break;
            case 'd':
                right = false;
                break;
        }
    }

    private void getWorldFromImage(){
        Image image = new Image();

        pixelData = image.openImage();
    }

	private void buildWorld()
    {

        segments.add(new Segment(new double[]{-10000, 10000},new double[]{ -10000, -10000}));
        segments.add(new Segment(new double[]{-10000, -10000},new double[]{ 10000, -10000}));
        segments.add(new Segment(new double[]{10000, -10000},new double[]{ 10000, 10000}));
        segments.add(new Segment(new double[]{10000, 10000},new double[]{ -10000, 10000}));

        segments.add(new Segment(new double[]{-6000, 6000},new double[]{ -6000, 2000}));
        segments.add(new Segment(new double[]{-6000, 2000},new double[]{ -2000, 2000}));
        segments.add(new Segment(new double[]{-2000, 2000},new double[]{ -2000, 6000}));
        segments.add(new Segment(new double[]{-2000, 6000},new double[]{ -6000, 6000}));

        segments.add(new Segment(new double[]{-4000, -3000},new double[]{ -5000, -4000}));
        segments.add(new Segment(new double[]{-5000, -4000},new double[]{ -4000, -5000}));
        segments.add(new Segment(new double[]{-4000, -5000},new double[]{ -3000, -4000}));
        segments.add(new Segment(new double[]{-3000, -4000},new double[]{ -4000, -3000}));

        segments.add(new Segment(new double[]{6000, 7000},new double[]{ 3000, 0}));
        segments.add(new Segment(new double[]{3000, 0},new double[]{ 6000, -7000}));
        segments.add(new Segment(new double[]{6000, -7000},new double[]{ 6000, 7000}));
	}
	
	synchronized public String encodeData(){
	    dataString = new StringBuilder();
		for(int j=0; j<1080; j++){
			int value = (int) this.data[j];
            //int value =(int) ((j+300) >> 6);
			char firstChar = (char) (((value & 0x3F000) >> 12) + 0x30);
			char secondChar = (char) (((value & 0xFC0) >> 6) + 0x30);
			char thirdChar =  (char) ((value & 0x3F) + 0x30);
			dataString.append(firstChar);
			dataString.append(secondChar);
			dataString.append(thirdChar);
		}
        return dataString.toString();
	}
}
