package dev.mortus.aerogen.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens;

public class BiomeProviderSky extends BiomeProvider {

	public BiomeProviderSky(World worldIn) {
		super(worldIn.getWorldInfo());
	}
	
    public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
        net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens event = new net.minecraftforge.event.terraingen.WorldTypeEvent.InitBiomeGens(worldType, seed, original);
        net.minecraftforge.common.MinecraftForge.TERRAIN_GEN_BUS.post(event);
        if (!event.isCanceled()) getSkyBiomeGenerators(event);
        return event.getNewBiomeGens();
    }

    /**
     * This does not subscribe to the event because I have no way of modifying "WorldType" as far as I can tell.
     * Without control over WorldType, I cannot know if I should be using the Sky generator or not. By overriding 
     * the getModdedBiomeGenerators, I am sure the call comes from the BiomeProviderSky. 
     */
	private void getSkyBiomeGenerators(InitBiomeGens event) {
		System.out.println("Creating a sky biome generator. (worldType == WorldTypeSky) = "+(event.getWorldType().getName().equals("aerogen_sky")));
		
		GenLayer original = new GenLayerSkyBiomes(event.getSeed());
		
        //GenLayer zoomedOut = new GenLayerVoronoiZoom(10L, original);
        
        original.initWorldGenSeed(event.getSeed());
        zoomedOut.initWorldGenSeed(event.getSeed());
		
		GenLayer[] layers = event.getNewBiomeGens();
		layers[0] = zoomedOut; // 4x4 area is now a 1x1 area, (0,0) to (3,3) becomes (0,0)
		layers[1] = original;
	}
	
	/**
	 * Returns the biome ID's for any requested range via getInts()
	 */
	public static class GenLayerSkyBiomes extends GenLayer {

		public GenLayerSkyBiomes(long baseSeed) {
			super(baseSeed);
		}

		@Override
		public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
			int[] biomeIDs = new int[areaWidth*areaHeight];
			
			return null;
		}
		
	}
	
}
