package dev.mortus.util.math.vectors;

public class Quaternion {

	public static Quaternion fromVector(Double3D vector) {
		return new Quaternion(0, vector.x(), vector.y(), vector.z());
	}
	
	public static Quaternion fromAxisRotation(Double3D axis, double theta) {
		double cos = Math.cos(theta/2.0);
		double sin = Math.sin(theta/2.0);
		Double3D axisNorm = axis.copy().normalize();
		return new Quaternion(cos, sin * axisNorm.x, sin * axisNorm.y, sin * axisNorm.z);
	}
	
	
	
	protected double r, i, j, k;
	
	public Quaternion(double r, double i, double j, double k) {
		this.r = r;
		this.i = i;
		this.j = j;
		this.k = k;
	}
	
	public Quaternion add(Quaternion q) {
		return new Quaternion(r+q.r, i+q.i, j+q.j, k+q.k);
	}
	
	public Quaternion multiply(Quaternion other) {
		Quaternion a = this, b = other;
		double r = (a.r * b.r) - (a.i * b.i) - (a.j * b.j) - (a.k * b.k);
		double i = (a.r * b.i) + (a.i * b.r) + (a.j * b.k) - (a.k * b.j);
		double j = (a.r * b.j) - (a.i * b.k) + (a.j * b.r) + (a.k * b.i);
		double k = (a.r * b.k) + (a.i * b.j) - (a.j * b.i) + (a.k * b.r);
		return new Quaternion(r, i, j, k);
	}
	
	public Quaternion conjugate() {
		return new Quaternion(r, -i, -j, -k);
	}
	
	/**
	 * Treating this quaternion as a unit quaternion, this method applies this quaternion's represented rotation to a 3D vector.
	 * This is equivalent to the following calls: <pre>
	 * this.multiply(Quaternion.fromVector(vector).multiply(this.conjugate()));
	 * </pre>
	 * @param vector - a vector to be rotated
	 * @return the original vector .redefine()ed. Mutable vectors will be modified, immutable vectors will be copied.
	 */
	public Double3D applyRotation(Double3D vector) {
		Quaternion conj = this.conjugate();
		Quaternion vecq = Quaternion.fromVector(vector);
		Quaternion result = this.multiply(vecq).multiply(conj);
		
		if (result.r < -0.0001 || result.r > 0.0001) {
			System.err.println("Rotation probably invalid. Real part of result nonzero: "+result.r);
		}
		
		return vector.redefine(result.i, result.j, result.k);
	}
	
	@Override
	public String toString() {
		return String.format("Quaternion(%.3f + %.3fi + %.3fj + %.3fk)", r, i, j, k);
	}
	
}
