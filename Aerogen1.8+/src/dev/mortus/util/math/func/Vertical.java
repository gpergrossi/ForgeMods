package dev.mortus.util.math.func;

import java.util.ArrayList;
import java.util.List;

public class Vertical extends Undefined {

	double verticalX;
	
	public Vertical(double x) {
		this.verticalX = x;
	}

	@Override
	public Function add(Function f) {
		if (f instanceof Vertical) return add((Vertical) f);
		return super.add(f);
	}
	
	public Function add(Vertical f) {
		if (f.verticalX == verticalX) return this;
		return new Undefined();
	}

	@Override
	public List<Double> zeros() {
		List<Double> zeros = new ArrayList<>(1);
		zeros.add(verticalX);
		return zeros;
	}

	public String toString() {
		return "Vertical[x="+verticalX+"]";
	}

	public double getX() {
		return verticalX;
	}
	
}
