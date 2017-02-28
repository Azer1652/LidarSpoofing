package tracing;

import world.Segment;

public class Ray {
	
	private double direction[];
	private double location[];

	public Ray(){
		this.location = new double[]{0,0};
		this.direction = new double[]{0,0};
	}

	public Ray(double angle){
		this.location = new double[]{0,0};
		this.direction = new double[]{0,0};
		this.direction[0] = Math.cos(angle);
		this.direction[1] = Math.sin(angle);
	}

	public Ray(double[] location, double[] direction){
		this.location = location;
		this.direction = direction;
	}

	public void setLocation(double[] location){
	    this.location[0] = location[0];
        this.location[1] = location[1];
    }

    public void setLocation(double x, double y){
        this.location[0] = x;
        this.location[1] = y;
    }

	public void setDirection(double[] direction){
		this.direction[0] = direction[0];
		this.direction[1] = direction[1];
	}

	public void setDirection(double x, double y){
		this.direction[0] = x;
		this.direction[1] = y;
	}

	public Hit hit(Segment segment){
		if(this.direction == segment.direction)
			return new Hit(null, 30000);

		double T2 = (direction[0]*(segment.start[1]-location[1]) + direction[1]*(location[0]-segment.start[0]))/(segment.direction[0]*direction[1] - segment.direction[1]*direction[0]);
		double T1 = (segment.start[0]+segment.direction[0]*T2-location[0])/direction[0];

		// Must be within parametic whatevers for RAY/SEGMENT
		if(T1<0) return new Hit(null, 30000);
		if(T2<0 || T2>1.0000000001) return new Hit(null, 30000);

		// Return the POINT OF INTERSECTION
		return new Hit(new double[]{location[0]+direction[0]*T1, location[1]+direction[1]*T1}, T1);
	}
}