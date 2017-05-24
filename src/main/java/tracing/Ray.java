package tracing;

import world.Segment;

public class Ray {
	
	private double direction[];
	private double location[];
	private double angle;

	/**
	 * New Ray
	 */
	public Ray(){
		this.location = new double[]{0,0};
		this.direction = new double[]{0,0};
	}

	/**
	 * new Ray
	 * @param angle
	 */
	public Ray(double angle){
		this.angle=angle;
		this.location = new double[]{0,0};
		this.direction = new double[]{0,0};
		this.direction[0] = Math.cos(angle);
		this.direction[1] = Math.sin(angle);
	}

	/**
	 * New Ray
	 * @param location
	 * @param angle
	 */
	public Ray(double[] location, double angle){
		this.location = location;
		this.angle = angle;
	}

	/**
	 * new Ray
	 * @param location
	 * @param direction
	 */
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

	/**
	 * Perform Pixel Tracing with this ray on pixelData
	 * @param pixelData
	 * @return
	 */
	public Hit hitPixel(int[][] pixelData) {
		double i, j;
		double dx = Math.cos(angle);
		double dy = Math.sin(angle);
		double rico = dy / dx;
		double inverseRico = dx / dy;

		if (angle <= Math.PI / 4 && angle >= -Math.PI / 4 || angle >= Math.PI * 7 / 4 && angle <= Math.PI * 2) {
			i = 0;
			while (i < 100) {
				j = Math.floor(rico * i);
				int locationX = (int) (location[0] + i);
				int locationY = (int) (location[1] + j);

				if (locationX <= 0 || locationX >= 900 || locationY <= 0 || locationY >= 1000) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				} else if (pixelData[(int) location[0]][(int) location[1]] != pixelData[locationX][locationY]) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				}
				i++;
			}
		} else if (angle <= Math.PI * 5 / 4 && angle >= Math.PI * 3 / 4 || angle >= -Math.PI && angle < -Math.PI * 3 / 4) {
			i = 0;
			while (i > -100) {
				j = Math.floor(rico * i);
				int locationX = (int) (location[0] + i);
				int locationY = (int) (location[1] + j);

				if (locationX <= 0 || locationX >= 900 || locationY <= 0 || locationY >= 1000) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				} else if (pixelData[(int) location[0]][(int) location[1]] != pixelData[locationX][locationY]) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				}
				i--;
			}
		} else if (angle > Math.PI / 4 && angle < Math.PI * 3 / 4) {
			j = 0;
			while (j < 100) {
				i = Math.floor(inverseRico * j);
				int locationX = (int) (location[0] + i);
				int locationY = (int) (location[1] + j);

				if (locationX <= 0 || locationX >= 900 || locationY <= 0 || locationY >= 1000) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				} else if (pixelData[(int) location[0]][(int) location[1]] != pixelData[locationX][locationY]) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				}
				j++;
			}
		} else if (angle < -Math.PI / 4 && angle > -Math.PI * 3 / 4 || angle < Math.PI * 7 / 4 && angle > Math.PI * 5 / 4) {
			j = 0;
			while (j > -100) {
				i = Math.floor(inverseRico * j);
				int locationX = (int) (location[0] + i);
				int locationY = (int) (location[1] + j);

				if (locationX <= 0 || locationX >= 900 || locationY <= 0 || locationY >= 1000) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				} else if (pixelData[(int) location[0]][(int) location[1]] != pixelData[locationX][locationY]) {
					return new Hit(new double[]{locationX, locationY}, Math.sqrt(i * i + j * j));
				}
				j--;
			}
		}

		return new Hit(null, 100);
	}

	/**
	 * Perform normal raytracing between two lines
	 * @param segment
	 * @return
	 */
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