package dev.mortus.util.math.func;

import java.util.List;

public class Polynomial extends Function {

	protected double multiplier;
	protected int startingPower;
	protected double[] coefficients;
	
	private static double getCoef(int select, int start, double[] coef) {
		if (select < start) return 0;
		if (select - start >= coef.length) return 0;
		return coef[select - start];
	}
	
	protected static Polynomial create(int startingPower, double... coefficients) {
		int length = coefficients.length;
		while (length > 0 && coefficients[length-1] == 0) length--;
		
		int first = 0;
		while (first < length && coefficients[first] == 0) first++;
		if (first > 0) {
			startingPower = startingPower + first;
			length -= first;
		}
		
		double[] coef = new double[length];
		if (length > 0) System.arraycopy(coefficients, first, coef, 0, length);
		
		int highestPower = startingPower + length - 1;
		if (length == 0) highestPower = 0;
				
		switch (highestPower) {
		case 0: {
			double c = getCoef(0, startingPower, coef);
			return new Constant(c);
		}
		case 1: {
			double m = getCoef(1, startingPower, coef);
			double b = getCoef(0, startingPower, coef);
			return new Linear(m, b);
		}
		case 2: {
			double a = getCoef(2, startingPower, coef);
			double b = getCoef(1, startingPower, coef);
			double c = getCoef(0, startingPower, coef);
			return new Quadratic(a, b, c);
		}
		default:
			return new Polynomial(1.0, startingPower, coef);
		}
	}
	
	protected Polynomial (double multiplier, int startingPower, double... coefficients) {
		this.startingPower = startingPower;
		this.coefficients = coefficients;
		this.multiplier = multiplier;
	}
	
	public double getValue(double x) {
		int low = getLowestPower();
		int high = getHighestPower();
		double y = 0;
		for (int i = low; i <= high; i++) {
			y += getCoef(i) * Math.pow(x, i);
		}
		return y;
	}

	public double getCoef(int power) {
		if (power < startingPower) return 0;
		if (power - startingPower >= coefficients.length) return 0;
		return multiplier * coefficients[power - startingPower];
	}
	
	public int getLowestPower() {
		return startingPower;
	}
	
	public int getHighestPower() {
		return startingPower + coefficients.length - 1;
	}
	
	public Function add(Function f) {
		if (f == null) return null;
		
		// Implemented here?
		if (f instanceof Polynomial) return this.add((Polynomial) f);
		
		// Does other class have an add(Polynomial p) method?
		try {
			return f.add(this);
		} catch (Throwable e) {
			System.err.println("No addition implemented between "+this.getClass().getName()+" and "+f.getClass().getName());
		}
		
		// Otherwise, not supported
		throw new UnsupportedOperationException();		
	}
	
	public Function add(Polynomial p) {
		int low = getLowestPower();
		int pLow = p.getLowestPower(); 
		if (pLow < low) low = pLow;

		int high = getHighestPower();
		int pHigh = p.getHighestPower(); 
		if (pHigh > high) high = pHigh;
		
		double[] add = new double[high-low+1];
		for (int i = low; i <= high; i++) {
			add[i-low] = getCoef(i) + p.getCoef(i);
		}
		return create(low, add);
	}

	public Function negate() {
		return new Polynomial(multiplier * -1, startingPower, coefficients);
	}

	public Function multiply(Function f) {
		throw new UnsupportedOperationException();
	}

	public Function inverse() {
		throw new UnsupportedOperationException();
	}

	public Function derivative() {
		int low = getLowestPower();
		int high = getHighestPower();		
		
		double[] der = new double[coefficients.length];
		for (int i = low; i <= high; i++) {
			der[i-low] = i*getCoef(i);
		}
		return create(startingPower-1, der);
	}
	
	public List<Double> zeros() {
		throw new UnsupportedOperationException();
	}
	
}
