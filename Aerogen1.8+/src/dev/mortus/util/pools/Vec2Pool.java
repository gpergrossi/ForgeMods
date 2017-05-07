package dev.mortus.util.pools;

import java.awt.geom.Point2D;

import dev.mortus.util.math.geom.Vec2;

public class Vec2Pool {

	Slot[] availableSlots;
	int nextAvailable;
	int numAvailable;
	
	int minAvailable;
	
	double[] data;
	
	public Vec2Pool(int capacity) {
		this.availableSlots = new Slot[capacity];
		this.data = new double[capacity*2];
		for (int i = 0; i < capacity; i++) {
			this.availableSlots[i] = new Slot(i);
		}
		this.numAvailable = capacity;
		this.nextAvailable = 0;
		this.minAvailable = numAvailable;
	}
	
	public Slot allocate() {
		if (numAvailable <= 0) throw new RuntimeException("Out of capacity!");
		Slot ret = availableSlots[nextAvailable];
		
		nextAvailable = (nextAvailable + 1) % availableSlots.length;
		numAvailable--;
		minAvailable = Math.min(minAvailable, numAvailable);
		
		ret.inUse = true;
		return ret;
	}
	
	public void free(Slot slot) {
		if (numAvailable == availableSlots.length || !slot.inUse) throw new RuntimeException("Invalid free");
		int index = (nextAvailable+numAvailable) % availableSlots.length;
		availableSlots[index] = slot;
		numAvailable++;
		slot.inUse = false;
	}
	
	public class Slot implements Vec2 {

		boolean inUse;
		int index;
		
		public Slot(int index) {
			this.index = index*2;
		}
		
		@Override
		public double getX() {
			return data[index];
		}

		@Override
		public double getY() {
			return data[index+1];
		}

		public void set(double x, double y) {
			data[index] = x;
			data[index+1] = y;
		}
		
		public Point2D toPoint2D() {
			return new Point2D.Double(data[index], data[index+1]);
		}
		
		public Vec2 multiply(double s) {
			data[index] *= s;
			data[index+1] *= s;
			return this;
		}
		
		public Vec2 divide(double s) {
			data[index] /= s;
			data[index+1] /= s;
			return this;
		}
		
		public Vec2 add(Vec2 other) {
			data[index] += other.getX();
			data[index+1] += other.getY();
			return this;
		}
		
		public Vec2 subtract(Vec2 other) {
			data[index] -= other.getX();
			data[index+1] -= other.getY();
			return this;
		}
		
		public double cross(Vec2 other) {
			return data[index]*other.getY() - data[index+1]*other.getX();
		}
		
		public double dot(Vec2 other) {
			return data[index]*other.getX() + data[index+1]*other.getY();
		}

		public double angle() {
			return Math.atan2(data[index+1], data[index]);
		}
		
		public double length() {
			double x = data[index];
			double y = data[index+1];
			return Math.sqrt(x*x + y*y);
		}
		
		public Vec2 normalize() {
			if (length() == 1.0) return this;
			return this.divide(length());
		}
		
		@Override
		public String toString() {
			return "Vec2Pool[slot="+(index/2)+", x="+data[index]+", y="+data[index+1]+"]";
		}

		@Override
		public int compareTo(Vec2 other) {
			int dy = (int) Math.signum(data[index+1] - other.getY());
			if (dy != 0) return dy;

			int dx = (int) Math.signum(data[index] - other.getX());
			if (dx != 0) return dx;
			
			return Integer.compare(this.hashCode(), other.hashCode());
		}

		@Override
		public boolean equals(Vec2 other) {
			double dx = data[index] - other.getX();
			double dy = data[index+1] - other.getY();
			if (dx*dx + dy*dy < EPSILON2) return true;
			return false;
		}
		
	}
	
}
