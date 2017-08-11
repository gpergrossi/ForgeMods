package dev.mortus.aerogen.world.islands;

import java.util.Random;

import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.func2d.Function2D;
import dev.mortus.util.math.func2d.RemapOperation;
import dev.mortus.util.math.ranges.Int2DRange;
import dev.mortus.util.math.vectors.Int2D;

public class IslandHeightmap {

	protected Island island;
	protected IslandShape shape;
	
	public int surfaceHeightMin;
	public int surfaceHeightMax;
	public int bottomDepthMax;
	
	public double undersideSteepness;

	protected Function2D noiseSurface;
	protected Function2D noiseUnderside;
	
	protected boolean hasCliffs;
	protected Function2D noiseCliff;
	protected double cliffHeight = 6;
	
	protected Int2DRange.Bytes bottom;
	protected Int2DRange.Bytes top;
	protected Int2DRange.Bits  defined;
	
	public IslandHeightmap(Island island) {
		this.island = island;
		this.shape = island.getShape();
	}
	
	public void initialize(Random random) {		
		bottomDepthMax = (int) (shape.maxEdgeDistance*1.5);
		surfaceHeightMin = -bottomDepthMax/8;
		surfaceHeightMax = bottomDepthMax/4;
		
		int minY = island.altitude-bottomDepthMax;
		int maxY = island.altitude+surfaceHeightMax;
		if (minY < 0) minY = 0;
		if (maxY > 255) maxY = 255;
		bottomDepthMax = island.altitude-minY;
		surfaceHeightMax = maxY-island.altitude;
		if (surfaceHeightMin > surfaceHeightMax) surfaceHeightMin = surfaceHeightMax;
		
		noiseUnderside = createUndersideNoise(random);
		noiseSurface = createSurfaceNoise(random);
		noiseCliff = createCliffNoise(random);
		
		undersideSteepness = 1.4;
		
		if (island.altitude < 63) undersideSteepness += 0.001 * (63-island.altitude);
		undersideSteepness += 0.0005 * shape.maxEdgeDistance;
		
		this.bottom = new Int2DRange.Bytes(shape.range);
		this.top = new Int2DRange.Bytes(shape.range);
		this.defined = new Int2DRange.Bits(shape.range);
	}

	protected Function2D createCliffNoise(Random random) {
		Function2D func = new FractalNoise2D(random.nextLong(), 1.0/64.0, 3); 
		return new RemapOperation(func, v -> v*0.5+0.5);
	}

	protected Function2D createSurfaceNoise(Random random) {
		Function2D func = new FractalNoise2D(random.nextLong(), 1.0/256.0, 5, 0.4);
		return new RemapOperation(func, v -> v*0.5+0.5);
	}

	protected Function2D createUndersideNoise(Random random) {
		Function2D func = new FractalNoise2D(random.nextLong(), 1.0/64.0, 5, 0.6);
		return new RemapOperation(func, v -> v*0.5+0.5);
	}
	
	protected void generateHeights(int x, int z) {
		double edgeDistance = shape.getEdgeDistance(x, z);
		double edgeWeight = edgeDistance/shape.maxEdgeDistance;
		if (edgeWeight <= 0) {
			this.defined.set(x, z, true);
			return;
		}
		
		double noiseU = getUndersideNoise(x, z);
		double noiseS = getSurfaceNoise(x, z);
		double noiseC = getCliffNoise(x, z);
		
		// Start surface
		double surface = ((surfaceHeightMax-surfaceHeightMin+1)*noiseS + surfaceHeightMin);
		
		// Cliffs
		double cliff = 0;
		if (hasCliffs) {
			double cliffValue = noiseC*Math.pow(edgeWeight, 0.5);
			if (cliffValue > 0.45) {
				int height = (int) cliffHeight;
				int surfaceHeight = (int) Math.floor(surface);
				if (cliffValue < 0.5) height = (int) (cliffHeight * (cliffValue - 0.45)/0.05);
				if (surfaceHeight < height) cliff = height - surfaceHeight;
			}
		}
		surface += cliff;
		
		// River Head Lake
		double riverHeadDist = shape.riverHeadDist(x, z);
		if (riverHeadDist < 24) {
			double weight = 1.0 - riverHeadDist / 24.0;
			if (weight < 0) weight = 0;
			else weight = Math.pow(weight, 0.5) * (0.25*noiseS + 0.75);
			surface = Math.min(surface, -6.0) * weight + surface * (1.0 - weight);
		}
		
		// Edge smoothing
		double edgeSmoothing = 0;
		if (edgeDistance > 3) edgeSmoothing = 1.0 - 1.0/(0.03*Math.pow(edgeDistance-3, 2)+1); 
		
		// River calculation
		double riverDist = shape.getRiverDistance(x, z);
		double riverDepth = (noiseS*4+1);
		double riverWidth = (1-noiseS)*6+1;
		double riverBank = 1.2+noiseU;
		double river = Math.pow(riverDist/riverWidth, riverBank);
		double riverSmoothing = 0.0;
		if (river > 1.0) riverSmoothing = 1.0 - 1.0/(0.01*river+1);
		
		// River carving / smoothing
		double carvedSurface = (river-1)*riverDepth-1;
		if (surface > carvedSurface) surface = carvedSurface;
		
		// Water & River+Edge smoothing
		if (surface < 0) surface *= edgeSmoothing;
		else surface *= riverSmoothing + edgeSmoothing*(1-riverSmoothing);

		// Finish surface
		double bottom = ((1.0 - edgeWeight) * (surface - cliff)) - (Math.pow(edgeWeight, 1.0/undersideSteepness) * bottomDepthMax * (noiseU * 0.6 + 0.4));
		
		int bottomY = (int) (bottom);
		int carvedY = (int) (surface);
		if (bottomY+island.altitude < 0) bottomY = -island.altitude;
		if (carvedY+island.altitude > 255) carvedY = 255-island.altitude;
		if (bottomY > carvedY) bottomY = carvedY;
		
		this.bottom.set(x, z, (byte) bottomY);
		this.top.set(x, z, (byte) carvedY);
		this.defined.set(x, z, true);
	}

	public double getSurfaceNoise(double x, double z) {
		return noiseSurface.getValue(x, z);
	}
	
	public double getUndersideNoise(double x, double z) {
		return noiseUnderside.getValue(x, z);
	}
	
	public double getCliffNoise(double x, double z) {
		return noiseCliff.getValue(x, z);
	}
	
	public double getRiverWidth(double x, double z) {
		return (1.0-getSurfaceNoise(x, z))*6.0+1.0;
	}

	public int getTop(Int2D xz) {
		return getTop(xz.x(), xz.y());
	}
	
	public int getTop(int x, int z) {
		if (!shape.contains(x, z)) return island.altitude;
		if (!defined.get(x, z)) generateHeights(x, z);
		return island.altitude + top.get(x, z);
	}
	
	public int getBottom(Int2D xz) {
		return getBottom(xz.x(), xz.y());
	}
	
	public int getBottom(int x, int z) {
		if (!shape.contains(x, z)) return island.altitude;
		if (!defined.get(x, z)) generateHeights(x, z);
		return island.altitude + bottom.get(x, z);
	}
	
	public int getEstimateDeepestY(int x, int z) {
		double edgeDistance = shape.getEdgeDistance(x, z);
		double edgeWeight = edgeDistance/shape.maxEdgeDistance;
		if (edgeWeight <= 0) {
			this.defined.set(x, z, true);
			return island.altitude;
		}
		
		double bottom = Math.pow(edgeWeight, 1.0/undersideSteepness) * bottomDepthMax * 0.75;
		return (int) (island.altitude - bottom);
	}
	
	public int getEstimateDeepestY() {
		return (int) (island.altitude - bottomDepthMax*0.75);
	}

	public int getEstimateHighestY() {
		return island.altitude + surfaceHeightMax + (int) cliffHeight;
	}

	public boolean isVisibleUnderside(int x, int y, int z) {
		if (getBottom(x, z) >= y) return true;
		if (getBottom(x-1, z) > y) return true;
		if (getBottom(x+1, z) > y) return true;
		if (getBottom(x, z-1) > y) return true;
		if (getBottom(x, z+1) > y) return true;
		return false;
	}

	public int getDirtLayerDepth(int x, int z) {
		float edgeWeight = shape.getEdgeDistance(x, z) / shape.maxEdgeDistance;
		
		boolean isCliff = (getCliffNoise(x, z) * Math.pow(edgeWeight, 0.5) > 0.45);
		if (isCliff) return (int) Math.floor(1.0 + 1.0*getUndersideNoise(x, z));
		
		double riverDist = shape.getRiverDistance(x, z);
		double riverWidth = (1-getSurfaceNoise(x, z))*6+1;
		if (riverDist < riverWidth*2) return (int) Math.floor(2.0 + 1.0*getUndersideNoise(x, z));
		
		return (int) Math.floor(1.0 + 2.0*edgeWeight + 2.0*getUndersideNoise(x, z));
	}

}
