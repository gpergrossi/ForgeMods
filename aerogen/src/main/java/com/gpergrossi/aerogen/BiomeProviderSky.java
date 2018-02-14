package com.gpergrossi.aerogen;

import java.util.List;

import com.google.common.collect.Lists;
import com.gpergrossi.util.geom.ranges.Int2DRange;

import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class BiomeProviderSky extends BiomeProvider {

	AeroGenerator generator;
	List<Biome> spawnBiomes;
	GenLayer[] genLayers;
	
	protected BiomeProviderSky(World world) {
		super(world.getWorldInfo());		
		generator = AeroGenerator.getGeneratorForWorld(world);
		this.spawnBiomes = Lists.newArrayList(Biomes.FOREST);
	}

    /**
     * Gets the list of valid biomes for the player to spawn in.
     */
    public List<Biome> getBiomesToSpawnIn() {
        return this.spawnBiomes;
    }
	
    @Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] originalLayers) {
    	if (genLayers == null) {
    		GenLayer original = new GenLayerSkyBiomes();
    		GenLayer zoomedOut = new GenLayerUnzoom(original);
    		zoomedOut = new GenLayerUnzoom(zoomedOut);
			this.genLayers = new GenLayer[2];
			this.genLayers[0] = zoomedOut; // 4x4 area is now a 1x1 area, (0,0) to (3,3) becomes (0,0)
			this.genLayers[1] = original;
    	}
		
		return genLayers;
	}

	/**
	 * Returns the biome ID's for any requested range via getInts()
	 */
	public class GenLayerSkyBiomes extends GenLayer {
				
		public GenLayerSkyBiomes() {
			super(0l);
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int[] biomeIDs = IntCache.getIntCache(areaWidth * areaHeight);
			Int2DRange range = new Int2DRange(areaX, areaY, areaX+areaWidth-1, areaY+areaHeight-1);
			Int2DRange.Integers integers = range.createIntegers();
			
			if (generator != null) generator.getBiomeInts(integers);
			return biomeIDs;
		}
		
	}

	public static class GenLayerUnzoom extends GenLayer {
		
		public GenLayerUnzoom(GenLayer parent) {
			super(0l);
			this.parent = parent;
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int[] parentInts = this.parent.getInts(areaX << 1, areaY << 1, areaWidth << 1, areaHeight << 1);
			int[] thisInts = IntCache.getIntCache(areaWidth * areaHeight);

			int parentScansize = areaWidth << 1;

			for (int j = 0; j < areaHeight; j++) {
				for (int i = 0; i < areaWidth; i++) {
					this.initChunkSeed((long) (areaX + i), (long) (areaY + j));
					int parentIndex = (j << 1) * parentScansize + (i << 1);
					int i00 = parentInts[parentIndex];
					int i01 = parentInts[parentIndex + 1];
					int i10 = parentInts[parentIndex + parentScansize];
					int i11 = parentInts[parentIndex + parentScansize + 1];
					thisInts[j * areaWidth + i] = this.selectModeOrRandom(i00, i01, i10, i11);
				}
			}

			return thisInts;
		}
	    
	}
	
}
