package com.gpergrossi.aerogen.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gpergrossi.aerogen.generator.data.WorldPrimer;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.regions.Region;
import com.gpergrossi.aerogen.generator.regions.RegionManager;
import com.gpergrossi.util.data.Tuple2;
import com.gpergrossi.util.data.ranges.Int2DRange;
import com.gpergrossi.util.geom.vectors.Int2D;
import com.gpergrossi.aerogen.definitions.biomes.IslandBiomes;
import com.gpergrossi.viewframe.AeroGeneratorView;
import com.gpergrossi.viewframe.ViewerFrame;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class AeroGenerator {

	// ========================================
	// ============== Generators ==============
	// ========================================
	
	private static Map<World, AeroGenerator> generators = new HashMap<>();
	
	public static AeroGenerator getGeneratorForWorld(World world) {
		AeroGenerator generator = generators.get(world);
		if (generator == null) {
			generator = new AeroGenerator(world);
			generators.put(world, generator);
		}
		return generator;
	}
	
	public static Set<Map.Entry<World, AeroGenerator>> getGenerators() {
		return generators.entrySet();
	}

	
	
	// ========================================
	// =========== GUI Viewer Frame ===========
	// ========================================
	
	private static boolean guiEnabled = false;
	private static ViewerFrame viewFrame;
	
	public static void toggleGUI() {
		setGUIEnabled(!guiEnabled);
	}
	
	public static void setGUIEnabled(boolean enabled) {
		guiEnabled = enabled;
		if (enabled) {
			viewFrame = new ViewerFrame(new AeroGeneratorView());
			viewFrame.setVisible(true);
		} else {
			if (viewFrame != null) viewFrame.close();
		}
	}
	
	
	
	
	// ========================================
	// ============= Actual Class =============
	// ========================================
	
	private World world;
	private AeroGeneratorSettings settings;
	
	private WorldPrimer worldPrimer;
	private RegionManager regionManager;
	
	private AeroGenerator(World world) {
		this.world = world;
		this.settings = new AeroGeneratorSettings(this);
		this.settings.load();

		this.worldPrimer = new WorldPrimer(this);
		this.regionManager = new RegionManager(this);
		this.load();
	}
	
	/**
	 * Attempt to load save state from world AeroGeneratorSave file
	 */
	private void load() {		
	}
	
	public World getWorld() {
		return world;
	}

	public AeroGeneratorSettings getSettings() {
		return this.settings;
	}

	public RegionManager getRegionManager() {
		return regionManager;
	}
	
	public Tuple2<ChunkPrimer, Biome[]> requestChunk(int chunkX, int chunkZ) {
        int chunkMinX = chunkX * 16;
        int chunkMinZ = chunkZ * 16;
        Int2DRange chunkBounds = new Int2DRange(chunkMinX, chunkMinZ, chunkMinX+15, chunkMinZ+15);
        
		ChunkPrimer primer = new ChunkPrimer();
		Biome[] biomes = new Biome[16 * 16];

		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkBounds);

		for (Region region : regions) {
			for (Int2D.Mutable block : chunkBounds.getAllMutable()) {
				if (region.getRegionPolygon().contains(block.x(), block.y())) {
					int index = chunkBounds.indexFor(block);
					biomes[index] = region.getBiome().getVoidBiome();
				}
			}
			for (Island island : region.getIslands()) {
				island.provideChunk(primer, biomes, chunkBounds);
			}
		}
		
		return new Tuple2<ChunkPrimer, Biome[]>(primer, biomes);
	}

	public void getBiomeInts(Int2DRange.Integers returnIntsRange) {
		int voidID = Biome.getIdForBiome(IslandBiomes.VOID);
		for (Int2D.StoredInteger integer : returnIntsRange.getAllIntegers()) {
			integer.setValue(voidID);
		}
		
		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, returnIntsRange);
		for (Region region : regions) {
			
			// Assign void biome for region
			for (Int2D.StoredInteger integer : returnIntsRange.getAllIntegers()) {
				if (region.getRegionPolygon().contains(integer.x(), integer.y())) {
					integer.setValue(region.getBiome().getVoidBiome().getBiomeID());
				}
			}
			
			// Assign island biome for each island
			for (Island island : region.getIslands()) {
				island.provideBiomes(returnIntsRange, returnIntsRange.data);
			}
		}
	}


}
