package dev.mortus.aerogen.world.gen.caves;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.octree.Octree;
import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.ranges.Int2DRange;
import dev.mortus.util.math.ranges.Int3DRange;
import dev.mortus.util.math.vectors.Double3D;
import dev.mortus.util.math.vectors.Int2D;
import dev.mortus.util.math.vectors.Int3D;

public class IslandCaves {

	private static double DEG2RAD = Math.PI / 180.0;
	
	private static double MIN_CAVE_RADIUS = 2.25;
	private static double MAX_CAVE_RADIUS = 3.75;
	
	/**
	 * Randomly steps up or down from 'original' by up to 'stepSize'. From -stepSize to stepSize.
	 * Then it constrains the result to be within the absolute minimum and maximum.
	 */
	private static double randomStep(double original, double stepSize, double absoluteMin, double absoluteMax, Random random) {
		return Math.min(absoluteMax, Math.max(absoluteMin, original + (random.nextDouble()-0.5)*2.0*stepSize));
	}
	
	private static boolean rollAllowedNearSurface(Random random) {	
		return random.nextDouble() < 0.2;
	}
	
	private static class CaveSegment implements Octree.IEntry {
			
		public double headingAngle;	// yaw compared to initial orientation of "upright (Y+), looking down the X+ axis"
		public double ascentAngle;	// pitch compared to initial orientation of "upright (Y+), looking down the X+ axis"
		
		public double curvature; // First derivative of headingAngle;
		public boolean allowedNearSurface = false;
		
		
		public final Double3D start;
		public final double startRadius;
		
		public final Double3D end;
		public final double endRadius;
		
		private final Double3D delta;
		private final double deltaLength2;
		private final Int3DRange range;
		
		
		private CaveSegment(Double3D start, double startRadius, double heading, double curve, double ascent, double length, double endRadius, boolean allowedNearSurface) {
			this.start = start.immutable();
			this.startRadius = startRadius;
			
			this.headingAngle = heading;
			this.curvature = curve;
			this.ascentAngle = ascent;
			
			this.allowedNearSurface = allowedNearSurface;
			
			Double3D offset = Double3D.X_AXIS;
			offset = offset.rotate(Double3D.Z_AXIS, ascentAngle);
			offset = offset.rotate(Double3D.Y_AXIS, headingAngle);
			
			this.end = start.add(offset.multiply(length));
			this.endRadius = endRadius;
			
			// Compute delta and range (Code duplication due to final members)
			this.delta = end.subtract(start);
			this.deltaLength2 = delta.lengthSquared();
			
			int minX = (int) Math.floor(Math.min((start.x() - startRadius), (end.x() - endRadius)));
			int minY = (int) Math.floor(Math.min((start.y() - startRadius), (end.y() - endRadius)));
			int minZ = (int) Math.floor(Math.min((start.z() - startRadius), (end.z() - endRadius)));
			
			int maxX = (int) Math.ceil(Math.max((start.x() + startRadius), (end.x() + endRadius)));
			int maxY = (int) Math.ceil(Math.max((start.y() + startRadius), (end.y() + endRadius)));
			int maxZ = (int) Math.ceil(Math.max((start.z() + startRadius), (end.z() + endRadius)));
			
			this.range = new Int3DRange(minX, minY, minZ, maxX, maxY, maxZ);
		}
		
		/**
		 * Creates a normal cave segment given a previous cave segment
		 */
		public static CaveSegment extend(IslandCaves caves, CaveSegment from, Random random) {
			Double3D start = from.end;
			double startRadius = from.endRadius;
			
			boolean allowedNearSurface = from.allowedNearSurface;
			
			double heading = randomStep(from.headingAngle, 15.0*DEG2RAD, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, random);
			heading = randomStep(heading, 15.0*DEG2RAD, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, random);
			
			double curve = randomStep(from.curvature, 6.0*DEG2RAD, -15.0*DEG2RAD, 15.0*DEG2RAD, random);
			heading += Math.pow(Math.abs(curve / (15.0*DEG2RAD)), 0.5) * (15.0*DEG2RAD) * Math.signum(curve);
			
			double ascent = randomStep(from.ascentAngle, 5.0*DEG2RAD, -75*DEG2RAD, 75*DEG2RAD, random);
			if (caves.getDepth(start) < 10) {
				if (allowedNearSurface) ascent = Math.min(75*DEG2RAD, ascent + 10.0*DEG2RAD);
				else ascent = Math.max(-75*DEG2RAD, ascent - 10.0*DEG2RAD);
			}
			if (Math.abs(ascent) > 15.0*DEG2RAD) ascent *= 0.9;
			
			double length = random.nextDouble() * 3.0 + 5;
			double endRadius = randomStep(startRadius, 0.3, MIN_CAVE_RADIUS, MAX_CAVE_RADIUS, random);

			return new CaveSegment(start, startRadius, heading, curve, ascent, length, endRadius, allowedNearSurface);
		}

		/**
		 * Creates a beginning cave segment given a position and radius
		 */
		public static CaveSegment begin(IslandCaves caves, Double3D pos, Random random) {
			Double3D start = pos.immutable();
			double startRadius = 2.25;

			boolean allowedNearSurface = rollAllowedNearSurface(random);
			
			double heading = random.nextDouble() * 360*DEG2RAD;
			double ascent = -random.nextDouble() * 30*DEG2RAD;
			
			double curve = (random.nextDouble()-0.5)*2.0 * 15.0*DEG2RAD;
			heading += curve;
						
			double length = random.nextDouble()*3.0 + 5.0;
			double endRadius = randomStep(startRadius, 0.3, MIN_CAVE_RADIUS, MAX_CAVE_RADIUS, random);

			return new CaveSegment(start, startRadius, heading, curve, ascent, length, endRadius, allowedNearSurface);
		}
		
		public static CaveSegment reverse(IslandCaves caves, CaveSegment toReverse) {
			Double3D start = toReverse.start;
			double startRadius = toReverse.startRadius;

			boolean allowedNearSurface = toReverse.allowedNearSurface;
			
			double heading = toReverse.headingAngle + Math.PI / 2.0;
			double ascent = -toReverse.ascentAngle;
			double curve = -toReverse.curvature;
			
			double length = Math.sqrt(toReverse.deltaLength2);
			double endRadius = toReverse.endRadius;

			return new CaveSegment(start, startRadius, heading, curve, ascent, length, endRadius, allowedNearSurface);
		}

		@Override
		public Int3DRange getRange() {
			return range;
		}
		
		@Override
		public double getDistanceTo(Double3D point) {
			Double3D.Mutable scratch = new Double3D.Mutable();
			
			// The t value for the intersect. Line represented as: v(t) = v0 + t*dv, for t on [0, 1]
			scratch = scratch.copy(point).subtract(start);
			double t = scratch.dot(delta) / deltaLength2;			
			t = Math.max(0, Math.min(1, t));
			
			// Closest point on line is v(t)
			scratch.copy(delta).multiply(t).add(start);
			
			// How thick is the cave segment at this t?
			double segmentRadius = startRadius * (1-t) + endRadius * (t);
			
			// Total distance = distance to center - radius at point
			double distance = scratch.distanceTo(point) - segmentRadius;
			
			/*  Possible concern:  ASCII art would be difficult so I will describe as simply as I can.
			 *  
			 *  Imagine a very large circle very close to a quite small circle both centered on a horizontal line.
			 *  In this case, the math above represents the shape created by connecting the top extents of both 
			 *  circles and the bottom extents of both circles. Instead of forming the shape one would expect 
			 *  -- that of a belt snugly wrapped around two wheels -- this forms a belt wrapped tightly around 
			 *  the two "opposing" half circles of the segment ends. That is, the half circles on the outside 
			 *  of the line segment.
			 *  
			 *  I believe this error is acceptable. It is much faster to compute than the "correct" result and
			 *  should cause no strange behavior in any situation the caves may create. Additionally, it is
			 *  mathematically similar to the correct result if the circles are similar in size and have a 
			 *  reasonable distance between them (compared to their radii).
			 */
			
			return distance;
		}
	}
	
	public static FractalNoise2D turbulenceX = new FractalNoise2D(87483211L, 1.0/32.0, 5);
	public static FractalNoise2D turbulenceY = new FractalNoise2D(-589043136L, 1.0/32.0, 5);
	public static FractalNoise2D turbulenceZ = new FractalNoise2D(1998491627635L, 1.0/32.0, 5);
	
	Island island;
	
	Int3DRange caveRange;
	Int3DRange comfortRange;
	Octree<CaveSegment> caves;
	
	int numSegments;
	int numBranches;
	int failedCaves;
	int targetNumSegments;
	
	List<CaveSegment> openEnds;
	List<CaveSegment> currentCave;
	
	public IslandCaves(Island island, int targetNumSegments, int minDepth) {
		this.island = island;
		
		Int2DRange islandRange = island.getShape().range;
		int minY = island.getHeightmap().getEstimateDeepestY();
		int maxY = island.getHeightmap().getEstimateHighestY();
		
		this.caveRange = new Int3DRange(islandRange.minX, minY, islandRange.minY, islandRange.maxX, maxY, islandRange.maxY);
		
		maxY = island.getAltitude() - minDepth - 4;
		this.comfortRange = new Int3DRange(islandRange.minX, maxY - (int) ((maxY - minY)*0.75), islandRange.minY, islandRange.maxX, maxY, islandRange.maxY);
		
		this.caves = new Octree<>(caveRange);
		
		this.numSegments = 0;
		this.failedCaves = 0;
		this.numBranches = 0;
		this.targetNumSegments = targetNumSegments;
		this.openEnds = new ArrayList<>();
		this.currentCave = new ArrayList<>();
	}

	public boolean generate(Random random) {
		
		if (openEnds.isEmpty()) {
			if (this.currentCave.size() > 5) {
				for (CaveSegment seg : currentCave) {
					caves.insert(seg);
					numSegments++;
				}
			} else {
				failedCaves++;
				if (failedCaves > 50) return false;
			}
			
			if (numSegments >= targetNumSegments) return false;
			if (comfortRange.size() == 0) return false;
			
			this.currentCave.clear();
			this.numBranches = 1;
			
			int tries = 0;
			Int3D tile = comfortRange.randomTile(random);
			while (!island.getShape().contains(tile.x(), tile.z()) && tries++ < 50) {
				tile = comfortRange.randomTile(random);
			}
			
			if (tries == 50) return false;
			
			CaveSegment newBeginning = CaveSegment.begin(this, tile.toDouble(), random);
			CaveSegment reverse = CaveSegment.reverse(this, newBeginning);
			openEnds.add(newBeginning);
			openEnds.add(reverse);
			currentCave.add(newBeginning);
			currentCave.add(reverse);
		}
		
		
		CaveSegment branch = openEnds.remove(random.nextInt(openEnds.size()));

		boolean split = (random.nextDouble() <= 0.2/numBranches);
		if (split) numBranches++;
		
		boolean corner = (random.nextDouble() <= 0.15);
		
		for (int i = 0; i < (split ? 2 : 1); i++) {
			CaveSegment seg = CaveSegment.extend(this, branch, random);
			
			if (corner || split) {
				seg.curvature += randomStep(seg.curvature, 6.0*DEG2RAD, -15*DEG2RAD, 15*DEG2RAD, random);
				seg.headingAngle += (random.nextDouble()-0.5)*2.0 * 30.0*DEG2RAD;
				seg.ascentAngle = randomStep(seg.ascentAngle, 30.0*DEG2RAD, -75*DEG2RAD, 75*DEG2RAD, random);
				if (split && i == 2) seg.allowedNearSurface = rollAllowedNearSurface(random); 
			}
			
			currentCave.add(seg);
				
			if (inIsland(seg.end)) {
				openEnds.add(seg);
			}
			
		}
		
		return true;
	}
	
	public boolean inIsland(Double3D pos) {
		int x = (int) Math.floor(pos.x()+0.5);
		int y = (int) Math.floor(pos.y()+0.5);
		int z = (int) Math.floor(pos.z()+0.5);		
		if (y < island.getHeightmap().getBottom(x, z)) return false;
		if (y > island.getHeightmap().getTop(x, z)) return false;
		return true;
	}

	public int getDepth(Double3D pos) {
		int x = (int) Math.floor(pos.x()+0.5);
		int y = (int) Math.floor(pos.y()+0.5);
		int z = (int) Math.floor(pos.z()+0.5);		
		return island.getHeightmap().getTop(x, z) - y;
	}

	public boolean carve(int x, int y, int z) {
		Double3D block = new Double3D(x + turbulenceX.getValue(x, z)*4, y + turbulenceY.getValue(x, z)*4, z + turbulenceZ.getValue(x, z)*4);
		return caves.getIntersects(block, null);
	}
	
}
