package dev.mortus.aerogen.world.islands;

import java.util.Random;

import dev.mortus.util.data.Int2DRange;
import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.func2d.Function2D;

public class IslandHeightmap {

	Island island;
	IslandShape shape;
	
	int surfaceHeightMin;
	int surfaceHeightMax;
	int bottomDepthMax;
	
	double undersideSteepness;

	Function2D noiseSurface;
	Function2D noiseUnderside;
	Function2D noiseCliff;
	
	Int2DRange.Bytes surfaceBeforeCarving;
	Int2DRange.Bytes bottom;
	Int2DRange.Bytes top;
	Int2DRange.Bits  defined;
	
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
		
		noiseUnderside = new FractalNoise2D(random.nextLong(), 1.0/64.0, 5, 0.6);
		noiseSurface = new FractalNoise2D(random.nextLong(), 1.0/256.0, 5, 0.4);
		noiseCliff = new FractalNoise2D(random.nextLong(), 1.0/64.0, 3);
		
		undersideSteepness = 1.4;
		
		if (island.altitude < 63) undersideSteepness += 0.001 * (63-island.altitude);
		undersideSteepness += 0.0005 * shape.maxEdgeDistance;
		
		this.surfaceBeforeCarving = new Int2DRange.Bytes(shape.range);
		this.bottom = new Int2DRange.Bytes(shape.range);
		this.top = new Int2DRange.Bytes(shape.range);
		this.defined = new Int2DRange.Bits(shape.range);
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
		if (island.getBiome().hasCliffs()) {
			double cliffValue = noiseC*Math.pow(edgeWeight, 0.5);
			if (cliffValue > 0.45) {
				int height = 6;
				int surfaceHeight = (int) Math.floor(surface);
				if (cliffValue < 0.5) height = (int) (6.0 * (cliffValue - 0.45)/0.05);
				if (surfaceHeight < height) cliff = height - surfaceHeight;
			}
		}
		surface += cliff;
		
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
		if (carvedSurface > surface) carvedSurface = surface;
		
		// Water & River+Edge smoothing
		if (surface < 0) surface *= edgeSmoothing;
		else surface *= riverSmoothing + edgeSmoothing*(1-riverSmoothing);
		if (carvedSurface < 0) carvedSurface *= edgeSmoothing;
		else carvedSurface *= riverSmoothing + edgeSmoothing*(1-riverSmoothing);

		// Finish surface
		double bottom = ((1.0 - edgeWeight) * (carvedSurface - cliff)) - (Math.pow(edgeWeight, 1.0/undersideSteepness) * bottomDepthMax * (noiseU * 0.6 + 0.4));
		
		int bottomY = (int) (bottom);
		int carvedY = (int) (carvedSurface);
		int surfaceY = (int) (surface);
		if (bottomY+island.altitude < 0) bottomY = -island.altitude;
		if (carvedY+island.altitude > 255) carvedY = 255-island.altitude;
		if (bottomY > carvedY) bottomY = carvedY;
		
		this.surfaceBeforeCarving.set(x, z, (byte) surfaceY);
		this.bottom.set(x, z, (byte) bottomY);
		this.top.set(x, z, (byte) carvedY);
		this.defined.set(x, z, true);
	}

	public double getSurfaceNoise(double x, double z) {
		return (noiseSurface.getValue(x, z)*0.5 + 0.5);
	}
	
	public double getUndersideNoise(double x, double z) {
		return (noiseUnderside.getValue(x, z)*0.5 + 0.5);
	}
	
	public double getCliffNoise(double x, double z) {
		return (noiseCliff.getValue(x, z)*0.5 + 0.5);
	}
	
	public int getSurfaceBeforeCarving(int x, int z) {
		if (!shape.contains(x, z)) return 0;
		if (!defined.get(x, z)) generateHeights(x, z);
		return island.altitude + surfaceBeforeCarving.get(x, z);
	}

	public int getTop(int x, int z) {
		if (!shape.contains(x, z)) return 0;
		if (!defined.get(x, z)) generateHeights(x, z);
		return island.altitude + top.get(x, z);
	}
	
	public int getBottom(int x, int z) {
		if (!shape.contains(x, z)) return 0;
		if (!defined.get(x, z)) generateHeights(x, z);
		return island.altitude + bottom.get(x, z);
	}
	
	public int getEstimateDeepestY() {
		return (int) (island.altitude + (surfaceHeightMin+surfaceHeightMax)/2 - bottomDepthMax*0.75);
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
		return (int) Math.floor(1.0 + 2.0*edgeWeight + 2.0*getUndersideNoise(x, z));
	}
	
}
