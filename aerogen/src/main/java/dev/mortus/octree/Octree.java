package dev.mortus.octree;

import java.util.ArrayList;
import java.util.List;

import dev.mortus.util.data.Tuple2;
import dev.mortus.util.math.ranges.Int3DRange;
import dev.mortus.util.math.vectors.Double3D;
import dev.mortus.octree.Octree.IEntry;

public class Octree<T extends IEntry> {
	
	public static interface IEntry {
		public Int3DRange getRange();
		public double getDistanceTo(Double3D pt);
	}
	
	public static enum NodeType {
		LEAF {
			public int getSplitValue(Int3DRange range) {
				return 0;
			}
			public int compare(Double3D elem, int splitValue) {
				return 0;
			}
			public int compare(Int3DRange elem, int splitValue) {
				return 0;
			}
			public Int3DRange chop(Int3DRange range, boolean greater) {
				return null;
			}
		}, 
		
		X_SPLIT {
			public int getSplitValue(Int3DRange range) {
				return Math.floorDiv(range.minX + range.maxX, 2);
			}
			public int compare(Double3D elem, int splitValue) {
				return (int) Math.signum(elem.x() - splitValue);
			}
			public int compare(Int3DRange elem, int splitValue) {
				return (elem.maxX <= splitValue) ? -1 : ((elem.minX > splitValue) ? 1 : 0);
			}
			public Int3DRange chop(Int3DRange range, boolean greater) {
				int splitValue = this.getSplitValue(range);
				if (greater) {
					return range.resize(splitValue+1, range.minY, range.minZ, range.maxX, range.maxY, range.maxZ);
				} else {
					return range.resize(range.minX, range.minY, range.minZ, splitValue, range.maxY, range.maxZ);
				}
			}
		}, 
		
		Y_SPLIT {
			public int getSplitValue(Int3DRange range) {
				return Math.floorDiv(range.minY + range.maxY, 2);
			}
			public int compare(Double3D elem, int splitValue) {
				return (int) Math.signum(elem.y() - splitValue);
			}
			public int compare(Int3DRange elem, int splitValue) {
				return (elem.maxY <= splitValue) ? -1 : ((elem.minY > splitValue) ? 1 : 0);
			}
			public Int3DRange chop(Int3DRange range, boolean greater) {
				int splitValue = this.getSplitValue(range);
				if (greater) {
					return range.resize(range.minX, splitValue+1, range.minZ, range.maxX, range.maxY, range.maxZ);
				} else {
					return range.resize(range.minX, range.minY, range.minZ, range.maxX, splitValue, range.maxZ);
				}
			}
		},  
		
		Z_SPLIT {
			public int getSplitValue(Int3DRange range) {
				return Math.floorDiv(range.minZ + range.maxZ, 2);
			}
			public int compare(Double3D elem, int splitValue) {
				return (int) Math.signum(elem.z() - splitValue);
			}
			public int compare(Int3DRange elem, int splitValue) {
				return (elem.maxZ <= splitValue) ? -1 : ((elem.minZ > splitValue) ? 1 : 0);
			}
			public Int3DRange chop(Int3DRange range, boolean greater) {
				int splitValue = this.getSplitValue(range);
				if (greater) {
					return range.resize(range.minX, range.minY, splitValue+1, range.maxX, range.maxY, range.maxZ);
				} else {
					return range.resize(range.minX, range.minY, range.minZ, range.maxX, range.maxY, splitValue);
				}
			}
		};

		public abstract int getSplitValue(Int3DRange range);
		public abstract int compare(Double3D elem, int splitValue);
		public abstract int compare(Int3DRange elem, int splitValue);
		public abstract Int3DRange chop(Int3DRange range, boolean greater);
	}

	protected Octree<T> parent = null;
	
	protected Int3DRange range;
	protected NodeType split;
	protected int splitValue;
	
	protected List<T> items;
	protected Octree<T> lesserChild;
	protected Octree<T> greaterChild;
	
	public Octree(Int3DRange range) {
		this.range = range.copy();
		this.items = new ArrayList<T>();
		this.split = getSplitType();
		this.splitValue = split.getSplitValue(range);
	}
	
	protected Octree(Octree<T> parent, Int3DRange range) {
		this(range);
		this.parent = parent;
	}
	
	protected NodeType getSplitType() {
		if (range.width == 1 && range.height == 1 && range.depth == 1) return NodeType.LEAF;
		if (range.width > range.height) {
			if (range.width > range.depth) {
				return NodeType.X_SPLIT;
			} else {
				return NodeType.Z_SPLIT;
			}
		} else {
			if (range.height > range.depth) {
				return NodeType.Y_SPLIT;
			} else {
				return NodeType.Z_SPLIT;
			}
		}
	}
	
	public int size() {
		int size = this.items.size();
		if (hasLesserChild()) size += getLesserChild().size();
		if (hasGreaterChild()) size += getGreaterChild().size();
		return size;
	}
	
	public int numNodes() {
		int size = 1;
		if (hasLesserChild()) size += getLesserChild().numNodes();
		if (hasGreaterChild()) size += getGreaterChild().numNodes();
		return size;
	}
	
	public void insert(T obj) {
		Int3DRange objRange = obj.getRange();
		int compare = split.compare(objRange, splitValue);
		
		if (compare == 0) {
			items.add(obj);
		} else if (compare > 0) {
			getGreaterChild().insert(obj);
		} else if (compare < 0) {
			getLesserChild().insert(obj);
		}
	}

	protected static int nodesSearched = 0;
	
	protected Tuple2<T, Double> getClosest(Double3D point, Tuple2<T, Double> currentBest) {
		nodesSearched++;
		
		int compare = split.compare(point, splitValue);
		if (compare <= 0) {
			if (hasLesserChild()) currentBest = getLesserChild().getClosest(point, currentBest);
			if (-compare < currentBest.second) {
				if (hasGreaterChild()) currentBest = getGreaterChild().getClosest(point, currentBest);
			}
		} else {
			if (hasGreaterChild()) currentBest = getGreaterChild().getClosest(point, currentBest);
			if (compare-1 < currentBest.second) {
				if (hasLesserChild()) currentBest = getLesserChild().getClosest(point, currentBest);
			}
		}
		
		for (T item : items) {
			double dist = item.getDistanceTo(point);
			if (dist < currentBest.second) {
				currentBest.first = item;
				currentBest.second = dist;
			}
		}
		
		return currentBest;
	}

	public Tuple2<T, Double> getClosest(Double3D point) {
		nodesSearched = 0;
		Tuple2<T, Double> result = this.getClosest(point, new Tuple2<>(null, Double.POSITIVE_INFINITY));
		return result;
	}

	/**
	 * Get the objects in the OctTree that intersect with the given input point.
	 * @param point - point on which to check for intersects
	 * @return List of intersecting objects
	 * @see getIntersects(Int3D, List&lt;Tuple2&lt;T, Double&gt;&gt;) if you wish to provide a list object or do not need the list results.
	 */
	public List<Tuple2<T, Double>> getIntersects(Double3D point) {
		nodesSearched = 0;
		List<Tuple2<T, Double>> intersects = new ArrayList<>();
		this.getIntersects(point, intersects);
		return intersects;
	}
	
	/**
	 * Get the objects in the OctTree that intersect with the given input point.
	 * @param point - point on which to check for intersects
	 * @param output - the list to add hits to, or null if you don't need the list. (A list will NOT be created)
	 * @return true if there was an intersect, false otherwise
	 */
	public boolean getIntersects(Double3D point, List<Tuple2<T, Double>> output) {
		nodesSearched++;
		boolean added = false;
		
		int compare = split.compare(point, splitValue);
		if (compare <= 0) {
			if (hasLesserChild()) added |= getLesserChild().getIntersects(point, output);
		} else {
			if (hasGreaterChild()) added |= getGreaterChild().getIntersects(point, output);
		}
		
		if (output == null && added == true) return true;
		
		for (T item : items) {
			double dist = item.getDistanceTo(point);
			if (dist > 0) continue;
			
			if (output != null) {
				added = true;
				output.add(new Tuple2<T, Double>(item, dist));
			} else {
				return true;
			}
		}
		
		return added;
	}
	
	protected boolean hasLesserChild() {
		return (lesserChild != null);
	}
	
	protected Octree<T> getLesserChild() {
		if (lesserChild == null) lesserChild = new Octree<>(this, split.chop(range, false));
		return lesserChild;
	}

	protected boolean hasGreaterChild() {
		return (greaterChild != null);
	}
	
	protected Octree<T> getGreaterChild() {
		if (greaterChild == null) greaterChild = new Octree<>(this, split.chop(range, true));
		return greaterChild;
	}
	
}
