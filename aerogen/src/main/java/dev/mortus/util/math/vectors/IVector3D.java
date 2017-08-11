package dev.mortus.util.math.vectors;

public interface IVector3D<T extends IVector3D<T>> extends IVector<T> {

	public T cross(T vector);
	
	public T rotate(T axis, double angle);
	
}
