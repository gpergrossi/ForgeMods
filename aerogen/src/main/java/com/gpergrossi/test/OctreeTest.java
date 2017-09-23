package com.gpergrossi.test;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.util.data.Octree;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int3DRange;
import com.gpergrossi.util.geom.vectors.Double3D;
import com.gpergrossi.util.geom.vectors.Int3D;

public class OctreeTest {

	private static class Point implements Octree.IEntry {

		Int3D point;
		Int3DRange range;
		
		public Point(int x, int y, int z) {
			this.point = new Int3D(x, y, z);
			this.range = new Int3DRange(point, point);
		}
		
		public Int3DRange getRange() {
			return range;
		}

		public double getDistanceTo(Double3D pt) {
			double dx = point.x() - pt.x();
			double dy = point.y() - pt.y();
			double dz = point.z() - pt.z();
			return Math.sqrt(dx*dx + dy*dy + dz*dz);
		}
		
		@Override
		public String toString() {
			return point.toString();
		}
		
	}
	
	static Octree<Point> octree;
	static List<Point> allPoints;
	static Int3DRange range = new Int3DRange(-32, -32, -32, 32, 32, 32);
	
	static int numNodes;
	static int numElements;
	
	public static void main(String[] args) {
		
		octree = new Octree<>(range);
		allPoints = new ArrayList<>();
		
		for (Int3D voxel : range.getAllMutable()) {
			if (Math.random() > 0.1) continue;
			Point point = new Point(voxel.x(), voxel.y(), voxel.z());
			octree.insert(point);
			allPoints.add(point);
		}

		numNodes = octree.numNodes();
		numElements = octree.size();
		
		System.out.println("Element Count: "+allPoints.size());
		System.out.println("Tree Size: "+numElements);
		System.out.println("Node Count: "+numNodes);
		
		for (Int3D voxel : range.getAllMutable()) {
			doClosestTest(voxel.x(), voxel.y(), voxel.z());
		}
		
		System.out.println("Searched "+nodesSearched+" / "+(numSearches*numElements)+" total nodes ("+((double)nodesSearched / (double)(numSearches*numElements))+")");
		
	}

	private static long nodesSearched;
	private static long numSearches;
	
	private static boolean doClosestTest(int i, int j, int k) {
		Double3D point = new Double3D(i, j, k);
		Tuple2<Point, Double> result = octree.getClosest(point);
		
		if (i == 0 && j == 0) System.out.println("Searched "+Octree.DEBUG_NODES_SEARCHED+" / "+numNodes+" nodes for "+point+" got "+result.first+" : "+result.second);
		nodesSearched += Octree.DEBUG_NODES_SEARCHED;
		numSearches++;
		
//		Tuple2<Point, Double> result2 = new Tuple2<>(null, Double.POSITIVE_INFINITY);
//		for (Point p : allPoints) {
//			double dist = p.getDistanceTo(point);
//			if (dist < result2.second) {
//				result2.first = p;
//				result2.second = dist;
//			}
//		}
//		
//		if (Math.abs(result.second - result2.second) > 0.1) {
//			System.out.println("===== NOT A MATCH =====");
//			System.out.println("Checking closest point to "+point+":");
//			System.out.println("Octree result: "+result.first+" : "+result.second);
//			System.out.println("List result: "+result2.first+" : "+result2.second);
//			return false;
//		}
		return true;
	}	
	
}
