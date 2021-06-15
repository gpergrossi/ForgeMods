package com.gpergrossi.aerogen.generator.islands;

import java.util.Random;

import com.gpergrossi.util.geom.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.IFunction2D;

public class IslandHeightmap {

	protected Island island;
	protected IslandShape shape;
	
	public int surfaceHeightMin;
	public int surfaceHeightMax;
	public int bottomDepthMax;
	
	public double undersideSteepness;

	protected IFunction2D noiseSurface;
	protected IFunction2D noiseUnderside;
	
	protected int maxCliffTier;
	protected IFunction2D noiseCliff;
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
		
		int minY = island.getAltitude()-bottomDepthMax;
		int maxY = island.getAltitude()+surfaceHeightMax;
		if (minY < 0) minY = 0;
		if (maxY > 255) maxY = 255;
		bottomDepthMax = island.getAltitude()-minY;
		surfaceHeightMax = maxY-island.getAltitude();
		if (surfaceHeightMin > surfaceHeightMax) surfaceHeightMin = surfaceHeightMax;
		
		noiseUnderside = createUndersideNoise(random);
		noiseSurface = createSurfaceNoise(random);
		noiseCliff = createCliffNoise(random);
		
		undersideSteepness = 1.4;
		
		if (island.getAltitude() < 63) undersideSteepness += 0.001 * (63-island.getAltitude());
		undersideSteepness += 0.0005 * shape.maxEdgeDistance;
		
		this.bottom = new Int2DRange.Bytes(shape.range);
		this.top = new Int2DRange.Bytes(shape.range);
		this.defined = new Int2DRange.Bits(shape.range);
	}

	protected IFunction2D createCliffNoise(Random random) {
		return FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(64).withOctaves(3).withRange(0, 1).build();
	}

	protected IFunction2D createSurfaceNoise(Random random) {
		return FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(256).withOctaves(5, 0.4).withRange(0, 1).build();
	}

	protected IFunction2D createUndersideNoise(Random random) {
		return FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(128).withOctaves(4, 0.6).withRange(0, 2.22).build();
	}
	
	protected void generateHeights(int x, int z) {
		double edgeDistance = shape.getEdgeDistance(x, z);
		double edgeWeight = edgeDistance/shape.maxEdgeDistance;
		if (edgeWeight <= 0) {
			this.defined.set(x, z, true);
			return;
		}
		
		double noiseUnderside = getUndersideNoise(x, z);
		double noiseSurface = getSurfaceNoise(x, z);
		double noiseCliff = getCliffNoise(x, z);
		
		// Start surface
		double surface = ((surfaceHeightMax-surfaceHeightMin+1)*noiseSurface + surfaceHeightMin);
		
		// Cliffs
		double cliff = 0;
		if (maxCliffTier > 0) {
			double cliffValue = noiseCliff*Math.pow(edgeWeight, 0.5)*(maxCliffTier+1);
			if (cliffValue >= 1.0) {
				int cliffTier = (int) Math.min(Math.floor(cliffValue), maxCliffTier);
				double cliffBlend = cliffValue - cliffTier;
				
				int height = (int) cliffHeight * cliffTier;
				if (cliffBlend < 0.05) {
					int blendHeight = (int) (cliffHeight * (cliffBlend/0.05));
					height += blendHeight - cliffHeight;
				}
				if (surface < height) cliff = height - surface;
			}
		}
		surface += cliff;
		
		// River Head Lake
		double riverHeadDist = shape.riverHeadDist(x, z);
		if (riverHeadDist < 24) {
			double weight = 1.0 - riverHeadDist / 24.0;
			if (weight < 0) weight = 0;
			else weight = Math.pow(weight, 0.5) * (0.25*noiseSurface + 0.75);
			surface = Math.min(surface, -6.0) * weight + surface * (1.0 - weight);
		}
		
		// Edge smoothing
		double edgeSmoothing = 0;
		if (edgeDistance > 3) edgeSmoothing = 1.0 - 1.0/(0.03*Math.pow(edgeDistance-3, 2)+1); 
		
		// River calculation
		double riverDist = shape.getRiverDistance(x, z);
		double riverDepth = (noiseSurface*4+1);
		double riverWidth = (1-noiseSurface)*6+1;
		double riverBank = 1.2+noiseUnderside;
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
		double bottom = ((1.0 - edgeWeight) * (surface - cliff)) - (Math.pow(edgeWeight, 1.0/undersideSteepness) * bottomDepthMax * (noiseUnderside * 0.6 + 0.4));
		
		int bottomY = (int) (bottom);
		int carvedY = (int) (surface);
		if (bottomY+island.getAltitude() < 0) bottomY = -island.getAltitude();
		if (carvedY+island.getAltitude() > 255) carvedY = 255-island.getAltitude();
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
		return (1.0 - getSurfaceNoise(x, z))*6.0 + 1.0;
	}

	public int getTop(Int2D xz) {
		return getTop(xz.x(), xz.y());
	}
	
	public int getTop(int x, int z) {
		if (!shape.contains(x, z)) return island.getAltitude();
		if (!defined.get(x, z)) generateHeights(x, z);
		int topValue = top.get(x, z);
		if (topValue < -64) topValue += 256;
		return island.getAltitude() + topValue;
	}
	
	public int getBottom(Int2D xz) {
		return getBottom(xz.x(), xz.y());
	}
	
	public int getBottom(int x, int z) {
		if (!shape.contains(x, z)) return island.getAltitude();
		if (!defined.get(x, z)) generateHeights(x, z);
		int bottomValue = bottom.get(x, z);
		if (bottomValue > 64) bottomValue -= 256;
		return island.getAltitude() + bottomValue;
	}
	
	public int getEstimateDeepestY(int x, int z) {
		double edgeDistance = shape.getEdgeDistance(x, z);
		double edgeWeight = edgeDistance/shape.maxEdgeDistance;
		if (edgeWeight <= 0) {
			this.defined.set(x, z, true);
			return island.getAltitude();
		}
		
		double bottom = Math.pow(edgeWeight, 1.0/undersideSteepness) * bottomDepthMax * 0.75;
		return (int) (island.getAltitude() - bottom);
	}
	
	public int getEstimateDeepestY() {
		return (int) (island.getAltitude() - bottomDepthMax*0.75);
	}

	public int getEstimateHighestY() {
		return island.getAltitude() + surfaceHeightMax + (int) cliffHeight;
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
		
		boolean isCliff = (getCliffNoise(x, z) * Math.pow(edgeWeight, 0.5) > 1.0/maxCliffTier-0.05);
		if (isCliff) return (int) Math.floor(1.0 + 0.5*getUndersideNoise(x, z));
		
		double riverDist = shape.getRiverDistance(x, z);
		double riverWidth = (1-getSurfaceNoise(x, z))*6+1;
		if (riverDist < riverWidth*2) return (int) Math.floor(2.0 + 1.0*getUndersideNoise(x, z));
		
		return (int) Math.floor(1.0 + 2.0*edgeWeight + 2.0*getUndersideNoise(x, z));
	}

}
