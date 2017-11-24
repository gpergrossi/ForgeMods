package com.gpergrossi.aerogen.world;

import java.util.List;

import com.google.common.collect.Lists;
import com.gpergrossi.aerogen.definitions.biomes.IslandBiomes;
import com.gpergrossi.aerogen.generator.AeroGenerator;
import com.gpergrossi.util.data.ranges.Int2DRange;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens;

public class BiomeProviderSky extends BiomeProvider {

	AeroGenerator generator;
	List<Biome> spawnBiomes;
	
	public BiomeProviderSky(World worldIn) {
		super(worldIn.getWorldInfo());

		generator = AeroGenerator.getGeneratorForWorld(worldIn);
		this.spawnBiomes = Lists.newArrayList(Biomes.FOREST);
	}

    /**
     * Gets the list of valid biomes for the player to spawn in.
     */
    public List<Biome> getBiomesToSpawnIn() {
        return this.spawnBiomes;
    }
	
    @Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
		net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens event = new net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens(worldType, seed, original);
		net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
		if (!event.isCanceled()) getSkyBiomeGenerators(event);
		return event.getNewBiomeGens();
	}
	
	/**
	 * This does not subscribe to the event because I have no way of modifying
	 * "WorldType" as far as I can tell. Without control over WorldType, I
	 * cannot know if I should be using the Sky generator or not. By overriding
	 * the getModdedBiomeGenerators, I am sure the call comes from the
	 * BiomeProviderSky.
	 */
	private void getSkyBiomeGenerators(InitBiomeGens event) {
		System.out.println("Creating a sky biome generator. (worldType == WorldTypeSky) = "	+ (event.getWorldType().getName().equals("aerogen_sky")));
		
		GenLayer original = new GenLayerSkyBiomes(event.getSeed());
		GenLayer zoomedOut = new GenLayerUnzoom(event.getSeed(), original);
		zoomedOut = new GenLayerUnzoom(event.getSeed(), zoomedOut);

		original.initWorldGenSeed(event.getSeed());
		zoomedOut.initWorldGenSeed(event.getSeed());

		GenLayer[] layers = event.getNewBiomeGens();
		layers[0] = zoomedOut; // 4x4 area is now a 1x1 area, (0,0) to (3,3) becomes (0,0)
		layers[1] = original;
	}

	/**
	 * Returns the biome ID's for any requested range via getInts()
	 */
	public class GenLayerSkyBiomes extends GenLayer {
				
		public GenLayerSkyBiomes(long baseSeed) {
			super(baseSeed);
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int[] biomeIDs = IntCache.getIntCache(areaWidth * areaHeight);
			Int2DRange range = new Int2DRange(areaX, areaY, areaX+areaWidth-1, areaY+areaHeight-1);
			Int2DRange.Integers integers = range.createIntegers();
			
			generator.getBiomeInts(integers);
			return biomeIDs;
		}
		
	}

	public static class GenLayerUnzoom extends GenLayer {
		
		public GenLayerUnzoom(long seed, GenLayer parent) {
			super(seed);
			super.parent = parent;
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
		
	    protected int selectModeOrRandom(int a, int b, int c, int d) {
	    	// VOID biomes must always 'win' so that the spawn locator code won't ever accept a spawn area over the void
	    	if (IslandBiomes.isVoid(a)) return a;
	    	if (IslandBiomes.isVoid(b)) return b;
	    	if (IslandBiomes.isVoid(c)) return c;
	    	if (IslandBiomes.isVoid(d)) return d;
	        return (b == c && c == d) ? b 
	        	:( (a == b && a == c) ? a 
	        	:( (a == b && a == d) ? a 
	        	:( (a == c && a == d) ? a 
	        	:( (a == b && c != d) ? a 
	        	:( (a == c && b != d) ? a 
	        	:( (a == d && b != c) ? a 
	        	:( (b == c && a != d) ? b 
	        	:( (b == d && a != c) ? b 
	        	:( (c == d && a != b) ? c 
	        	: super.selectRandom(new int[] {a, b, c, d})
	        	))) ))) )));
	    }
	    
	}
	
}
