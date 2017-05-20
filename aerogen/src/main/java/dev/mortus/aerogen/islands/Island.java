package dev.mortus.aerogen.islands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.mortus.aerogen.regions.Region;
import dev.mortus.util.math.func2d.FractalNoise2D;
import dev.mortus.util.math.func2d.Function2D;
import dev.mortus.util.math.geom.Polygon;
import dev.mortus.util.math.geom.Rect;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

public class Island {

	Region region;
	int id;
	long seed;
	Random random;
	List<Polygon> cells;
	boolean constructed = false;
	boolean built = false;
	
	Rect boundsXZ;
	int minX, maxX, width;
	int minZ, maxZ, depth;
	int altitude;
	
	float[] edgeDistance;
	double maxEdgeDistance;
	
//	Function2D edgeNoise;
//	float edgeNoiseGain;
//	float edgeNoiseThreshold;
//	float[] boundaryDistance;
	
	Biome biome;

	int surfaceHeightMin;
	int surfaceHeightMax;
	int extendYDown;
	Function2D noiseD;
	Function2D noiseS;
	double undersideSteepness;
	
	public Island(Region region, int regionIsleID, long seed) {
		this.region = region;
		this.id = regionIsleID;
		this.seed = seed;
		this.random = new Random(seed);
		this.cells = new ArrayList<>();
	}

	public void grant(Polygon cell) {
		if (cells.contains(cell)) return;
		cells.add(cell);
	}

	public void finishGrant() {
		calcXZBounds();
		constructed = true;
	}

	private void calcXZBounds() {
		boundsXZ = null;
		for (Polygon cell : cells) {
			Rect r = cell.getBounds();
			if (boundsXZ == null) boundsXZ = r;
			else boundsXZ.union(r);
		}
		
		minX = (int) Math.floor(boundsXZ.minX());
		maxX = (int) Math.ceil(boundsXZ.maxX());
		width = maxX - minX + 1;
		
		minZ = (int) Math.floor(boundsXZ.minY());
		maxZ = (int) Math.ceil(boundsXZ.maxY());
		depth = maxZ - minZ + 1;
	}
	
	public void build() {
		if (!constructed) throw new IllegalStateException("Island needs to finishGrant() before being initialized");
		if (built) return;
		built = true;
				
		generateShape();
		if (Region.DEBUG_VIEW) return;
		
		biome = region.getBiome().getRandomIslandBiome(random);
		
		int minHeight = 64;
		int maxHeight = 128;
		
		altitude = 0;
		altitude += random.nextInt(maxHeight-minHeight+1)+minHeight;
		altitude += random.nextInt(maxHeight-minHeight+1)+minHeight;
		altitude = altitude/3;

		extendYDown = (int) (Math.sqrt(maxEdgeDistance)*7);
		surfaceHeightMin = 0;
		surfaceHeightMax = extendYDown/2;
		
		int minY = altitude-extendYDown;
		int maxY = altitude+surfaceHeightMax;
		if (minY < 0) minY = 0;
		if (maxY > 255) maxY = 255;
		extendYDown = altitude-minY;
		surfaceHeightMax = maxY-altitude;
		if (surfaceHeightMin > surfaceHeightMax) surfaceHeightMin = surfaceHeightMax;
		
		noiseD = new FractalNoise2D(random.nextLong(), 1.0/64.0, 5, 0.6);
		noiseS = new FractalNoise2D(random.nextLong(), 1.0/128.0, 4, 0.3);
		undersideSteepness = 1.4;
		
		if (altitude < 63) undersideSteepness += 0.001 * (63-altitude);
		undersideSteepness += 0.0005 * maxEdgeDistance;
	}

	/**
	 * Generate the top-down view shape of the island.
	 * Inputs: this.width, this.depth, this.cells
	 * Outputs: this.edgeDistance[], this.maxEdgeDistance
	 */
	private void generateShape() {		
		// The operations needed to compute edge distance are extremely computation intense.
		// In order to make the outline computation as efficient as possible, the code has
		// become somewhat complex. Follow the comments. Basically I am doing am generating
		// a matrix of "distance to the edge of the island from this spot" values. I do this
		// by "smearing" distance values over a matrix in 4 diagonal passes. I use a small
		// model of the true island matrix to compute distance values. Then I use estimates
		// and math to refine the small matrix into a reasonably accurate full-scale matrix
		
		// Through testing, I have seen that 256x256 is a good grid size for quality and speed
		int targetNumMatrixCells = 256*256;
		
		// This multiplier will be used in conversions between the real island size and the smaller representation
		double multiplier = Math.ceil((double)(width*depth)/targetNumMatrixCells);
		if (multiplier < 1.0) multiplier = 1.0; // no need to scale down, if the island is small, cool!
		
		// Create the small matrix representation of this island
		int smallScale = (int) multiplier;
		int padding = 1;
		int smallWidth = (int) Math.ceil(width/multiplier)+2*padding;
		int smallDepth = (int) Math.ceil(depth/multiplier)+2*padding;
		float[] smallDist = new float[smallWidth*smallDepth]; // Full size, used later
		
		// Set tiles inside the island to Float.Infinity and outside the island to Zero.
		// Note that there is also a padding of 1 tile around the outside, this makes neighbor checks faster.
		for (int z = 0; z < smallDepth; z++) {
			for (int x = 0; x < smallWidth; x++) {
				int index = z*smallWidth+x;
				smallDist[index] = 0;
				
				// If in the padding region, we are done. All padding tiles should be 0
				if (x == 0 || z == 0 || x == width-1 || z == depth-1) continue;
				
				// Otherwise, check if the cells are in one of the island polygons
				for (Polygon cell : cells) {
					double trueX = minX + (x-padding)*smallScale;
					double trueZ = minZ + (z-padding)*smallScale;
					if (cell.contains(trueX, trueZ)) {
						smallDist[index] = Float.POSITIVE_INFINITY;
						break;
					}
				}
			}
			Thread.yield(); // Not sure if this is necessary, but MineCraft might appreciate it
		}
		
		// Perform the distanceTransform to acquire the "distance to island boundary" matrix
		float maxDistance = distanceTransform(smallDist, smallWidth, smallDepth);

		// If we are not working at full scale, we need to remember
		// the boundary distances to estimate the full size matrix later
		float[] boundDist = null;
		if (smallScale > 1) {
			boundDist = new float[smallWidth*smallDepth];
			System.arraycopy(smallDist, 0, boundDist, 0, boundDist.length);
		}
		
		// Next, we erode the available island tiles with a Fractal Simplex ("Perlin") noise function
		// Below are the settings used to compute erosion. It took a massive amount of tuning to get good results
		int    erodeOctaves = 6;
		double erodeWavelength = 64;
		double erodePersistence = 0.45;
		
		// These two parameters scale with the island's size true size
		maxDistance *= smallScale;
		float erodeGain = maxDistance/2f + 16f;					
		float erodeCutoff = 0.7f*erodeGain+0.1f*maxDistance;
		
		Function2D erodeNoise = new FractalNoise2D(random.nextLong(), 1.0/erodeWavelength, erodeOctaves, erodePersistence);
		
		// Remove some tiles from the island, this uses the distanceTransform from earlier
		for (int z = 0; z < smallDepth; z++) {
			for (int x = 0; x < smallWidth; x++) {
				int index = z*smallWidth+x;
				double edgeDist = smallDist[index] * smallScale;
				
				// Some tiles are already outside the island boundary, no need to erode them
				if (edgeDist == 0f) continue; 

				// For the rest we apply a noise function and a threshold cut-off
				double trueX = (x-padding)*smallScale;
				double trueZ = (z-padding)*smallScale;
				float noiseVal = (float) erodeNoise.getValue(trueX, trueZ)*0.5f+0.5f; // FractalNoise2D returns (-1,1) we want (0,1)				

				if (erodeGain*noiseVal + edgeDist < erodeCutoff) smallDist[index] = 0f;
			}
			Thread.yield();
		}

		// Update the distance transform to include the new erosion information
		maxDistance = distanceTransform(smallDist, smallWidth, smallDepth) * smallScale;
		
		// If the smallScale representation is full size, we just need to fix the padding
		if (smallScale == 1) {
			float[] fullSize = new float[width*depth];
			for (int z = 0; z < depth; z++) {
				for (int x = 0; x < width; x++) {
					fullSize[z*width+x] = smallDist[(z+1)*smallWidth+(x+1)];
				}
			}
			this.edgeDistance = fullSize;
			this.maxEdgeDistance = maxDistance;
			return;
		}
		
		// Otherwise, we need to do a bit of magic to up-scale the values
		float[] fullSize = new float[width*depth];
		for (int z = 0; z < depth; z++) {
			for (int x = 0; x < width; x++) {
				int index = (z/smallScale + padding)*smallWidth + (x/smallScale + padding);	
				float lerpX = ((float) (x % smallScale)) / ((float) smallScale);
				float lerpZ = ((float) (z % smallScale)) / ((float) smallScale);
				
				float edgeDist = lerp2dMatrix(lerpX, lerpZ, smallDist, index, smallWidth);
				
				// All small matrix zeros correspond to true matrix zeros
				if (edgeDist <= 0) {
					fullSize[z*width+x] = 0;
					continue;
				};
				
				// If the edgeDist <= 1, the tile is on the edge of the island and
				// a simple linear interpolation will not be good enough
				if (edgeDist <= 1f) {
					// We apply the erosion function from earlier
					float borderDist = lerp2dMatrix(lerpX, lerpZ, boundDist, index, smallWidth) * smallScale;
					float noiseVal = (float) erodeNoise.getValue(x, z)*0.5f+0.5f;
					
					// If eroded, the true matrix distance is 0
					if (erodeGain*noiseVal + borderDist <= erodeCutoff) {
						fullSize[z*width+x] = 0;
						continue;
					}
				}
				
				// In all other cases, we only need to scale up the small distance estimate
				fullSize[z*width+x] = edgeDist * smallScale;
			}
		}
		this.edgeDistance = fullSize;
		this.maxEdgeDistance = maxDistance;
	}
	
	/**
	 * Performs a distance transform on the given array, which is interpreted as
	 * a two dimensional array in y*width+x order. The array must already be initialized
	 * for a distance transform. Values are pulled down to the lowest neighboring value
	 * plus 1 for cardinal and 1.414f for diagonal. Also returns the greatest distance found.
	 */
	private float distanceTransform(float[] array, int width, int height) {
		float dist;
		for (int y = 1; y < height; y++) {
			for (int x = 1; x < width; x++) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index-width]+1, dist);
				dist = Math.min(array[index-1]+1, dist);
				dist = Math.min(array[index-width-1]+1.414f, dist);
				array[index] = dist;
			}
			Thread.yield();
			for (int x = width-2; x >= 0; x--) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+1]+1, dist);
				dist = Math.min(array[index-width+1]+1.414f, dist);
				array[index] = dist;
			}
			Thread.yield();
		}
		float maxDistance = 0;
		for (int y = height-2; y >= 0; y--) {
			for (int x = 1; x < width; x++) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+width]+1, dist);
				dist = Math.min(array[index-1]+1, dist);
				dist = Math.min(array[index+width-1]+1.414f, dist);
				array[index] = dist;
			}
			Thread.yield();
			for (int x = width-2; x >= 0; x--) {
				int index = y*width+x;
				if (array[index] == 0) continue;
				dist = array[index];
				dist = Math.min(array[index+1]+1, dist);
				dist = Math.min(array[index+width+1]+1.414f, dist);
				array[index] = dist;
				maxDistance = Math.max(maxDistance, array[index]);
			}
			Thread.yield();
		}
		return maxDistance;
	}
	
	/**
	 * Apply a 2d lerp using the matrix stored in the float[] arr. The existence of neighbors
	 * in arr[index], arr[index+1], arr[index+scansize], and arr[index+scansize+1] is assumed.
	 */
	private float lerp2dMatrix(float x, float y, float[] arr, int index, int scansize) {
		return lerp2d(x, y, arr[index], arr[index+scansize], arr[index+1], arr[index+scansize+1]);
	}
	
	/**
	 * Two-dimensional linear interpolation. (x, y) in the range of (0, 0) to (1, 1)
	 */
	private float lerp2d(float x, float y, float lowXlowY, float lowXhighY, float highXlowY, float highXhighY) {
		float lowX  = lerp(y, lowXlowY, lowXhighY);
		float highX = lerp(y, highXlowY, highXhighY);
		return lerp(x, lowX, highX);
	}
	
	/**
	 * Linear interpolation between lowX and highX as x moves between 0 and 1
	 */
	private float lerp(float x, float lowX, float highX) {
		return (1f-x)*lowX + x*highX;
	}
	
	public boolean provideChunk(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
		int chunkMinX = chunkX*16;
		int chunkMinZ = chunkZ*16;
		int chunkMaxX = chunkMinX+15;
		int chunkMaxZ = chunkMinZ+15;
		if (chunkMaxX < minX || chunkMinX > maxX) return false;
		if (chunkMaxZ < minZ || chunkMinZ > maxZ) return false;

		this.build();
		
		int startX = Math.max(chunkMinX, minX);
		int stopX = Math.min(chunkMaxX, maxX);
		int startZ = Math.max(chunkMinZ, minZ);
		int stopZ = Math.min(chunkMaxZ, maxZ);

		for (int z = startZ; z <= stopZ; z++) {
			for (int x = startX; x <= stopX; x++) {
				double edgeWeight = getEdgeDistance(x, z)/maxEdgeDistance;
				if (edgeWeight <= 0) continue;
				
				double d = 0.95*Math.pow(edgeWeight, 1.0/undersideSteepness) * (noiseD.getValue(x, z)*0.3 + 0.7); // .40 to 1.0
				double s = (0.5 + 0.5*Math.pow(edgeWeight, 0.4)) * (noiseS.getValue(x, z)*0.5 + 0.5);

				double surface = (altitude + (surfaceHeightMax-surfaceHeightMin)*s + surfaceHeightMin);
				double smoothEdge = (altitude - surface) * Math.pow(Math.cos(edgeWeight*Math.PI)*0.5+0.5, 0.25);
				
				int stopY = (int) (surface + smoothEdge);
				int startY = (int) (altitude - extendYDown*d);
				if (startY < 0) startY = 0;
				if (stopY > 255) stopY = 255;
				if (stopY < startY+3) stopY = startY+3;
				
				boolean water = stopY < altitude;
				boolean edge = water && (!isIsland(x-1,z) || !isIsland(x+1,z) || !isIsland(x,z-1) || !isIsland(x,z+1));
				if (edge) stopY = altitude-1;
				
				for (int y = startY; y <= stopY; y++) {
					IBlockState block = Blocks.STONE.getDefaultState();
					if (y == stopY) block = Blocks.GRASS.getDefaultState();
					primer.setBlockState(x-chunkMinX, y, z-chunkMinZ, block);
				}
				
				biomes[(z-chunkMinZ)*16 + (x-chunkMinX)] = biome;
				
			}
		}
		
		return true;
	}

	public void replaceBiomeBlocks(IChunkGenerator generator, World world, int chunkX, int chunkZ, ChunkPrimer primer) {
        if (!net.minecraftforge.event.ForgeEventFactory.onReplaceBiomeBlocks(generator, chunkX, chunkZ, primer, world)) return;

		int chunkMinX = chunkX*16;
		int chunkMinZ = chunkZ*16;
		int chunkMaxX = chunkMinX+15;
		int chunkMaxZ = chunkMinZ+15;
		if (chunkMaxX < minX || chunkMinX > maxX) return;
		if (chunkMaxZ < minZ || chunkMinZ > maxZ) return;

		int startX = Math.max(chunkMinX, minX);
		int stopX = Math.min(chunkMaxX, maxX);
		int startZ = Math.max(chunkMinZ, minZ);
		int stopZ = Math.min(chunkMaxZ, maxZ);

		IBlockState[] removeBedrock = new IBlockState[8];
		for (int z = startZ; z <= stopZ; z++) {
			for (int x = startX; x <= stopX; x++) {
				double edgeWeight = getEdgeDistance(x, z)/maxEdgeDistance;
				if (edgeWeight <= 0) continue;
				
				double d = 0.95*Math.pow(edgeWeight, 1.0/undersideSteepness) * (noiseD.getValue(x, z)*0.3 + 0.7); // .40 to 1.0
				double s = (0.5 + 0.5*Math.pow(edgeWeight, 0.4)) * (noiseS.getValue(x, z)*0.5 + 0.5);

				double surface = (altitude + (surfaceHeightMax-surfaceHeightMin)*s + surfaceHeightMin);
				double smoothEdge = (0.5 + altitude - surface) * Math.pow(Math.cos(edgeWeight*Math.PI)*0.5+0.5, 0.25);
				
				int stopY = (int) (surface + smoothEdge);
				int startY = (int) (altitude - extendYDown*d);
				if (startY < 0) startY = 0;
				if (stopY > 255) stopY = 255;
				if (stopY < startY+3) stopY = startY+3;
				
				boolean water = (stopY < altitude);
				boolean edge = water && (!isIsland(x-1,z) || !isIsland(x+1,z) || !isIsland(x,z-1) || !isIsland(x+1,z));
				if (edge) stopY = altitude-1;
				
                for (int y = 0; y < 8; y++) removeBedrock[y] = primer.getBlockState(x-chunkMinX, y, z-chunkMinZ);
                
                // Replace blocks with biome style
                biome.genTerrainBlocks(world, random, primer, z, x, s);
                
				if (water) primer.setBlockState(x-chunkMinX, startY, z-chunkMinZ, Blocks.SANDSTONE.getDefaultState());
            	primer.setBlockState(x-chunkMinX, stopY, z-chunkMinZ, Blocks.AIR.getDefaultState());
                
            	// Fix bedrock spawns
                for (int y = 0; y < 8; y++) {
                	primer.setBlockState(x-chunkMinX, y, z-chunkMinZ, removeBedrock[y]);
                }
			}
		}
    }

	@Override
	public String toString() {
		return "Island ("+region.getCoord().x+", "+region.getCoord().y+"):"+id+" (seed: "+seed+")";
	}

	public long getSeed() {
		return seed;
	}
	
	public List<Polygon> getPolygons() {
		return cells;
	}

	public int getAltitude() {
		return altitude;
	}

	public int minX() { return minX; }
	public int maxX() { return maxX; }
	public int width() { return width; }
	
	public int minZ() { return minZ; }
	public int maxZ() { return maxZ; }
	public int depth() { return depth; }

	public boolean isIsland(int x, int z) {
		return getEdgeDistance(x, z) > 0;
	}
	
	public float getEdgeDistance(int x, int z) {
		int index = (z-minZ)*width + (x-minX);
		return edgeDistance[index];
	}

	public double getMaxEdgeDistance() {
		return maxEdgeDistance;
	}

	public Rect getBoundingBox() {
		return boundsXZ;
	}

	public boolean isComplete() {
		return built;
	}
	
}
