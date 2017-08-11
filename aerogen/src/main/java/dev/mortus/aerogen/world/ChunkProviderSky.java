package dev.mortus.aerogen.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.regions.Region;
import dev.mortus.aerogen.world.regions.RegionManager;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.gen.ChunkProviderSettings;

public class ChunkProviderSky implements IChunkGenerator {

	World world;
	RegionManager regionManager;
	ChunkProviderSettings settings;
	
	public ChunkProviderSky(World world, long seed, String settingsJSON) {
		this.world = world;
		this.regionManager = RegionManager.instanceFor(world);
		this.settings = ChunkProviderSettings.Factory.jsonToFactory(settingsJSON).build();
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		ChunkPrimer primer = new ChunkPrimer();

		Biome[] biomes = new Biome[16 * 16];

		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, x * 16, z * 16, x * 16 + 16, z * 16 + 16);

		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				island.provideChunk(x, z, primer, biomes);
			}
		}
		
		Chunk chunk = new Chunk(world, primer, x, z);

		// Biomes
		byte[] chunkBiomes = chunk.getBiomeArray();
		for (int i = 0; i < 256; i++) {
			chunkBiomes[i] = (byte) Biome.getIdForBiome(biomes[i]);
		}

		// Lighting
		chunk.generateSkylightMap();

		return chunk;
	}

	public void populate(int chunkX, int chunkZ) {
//		BlockFalling.fallInstantly = false;
		Random random = new Random(this.world.getSeed());
		long k = random.nextLong() / 2L * 2L + 1L;
		long l = random.nextLong() / 2L * 2L + 1L;
		long seed = (long) chunkX * k + (long) chunkZ * l ^ this.world.getSeed();

		List<Region> regions = new ArrayList<>();
		regionManager.getAll(regions, chunkX * 16 + 8, chunkZ * 16 + 8,  chunkX * 16 + 24,  chunkZ * 16 + 24);

		for (Region region : regions) {
			for (Island island : region.getIslands()) {
				random.setSeed(seed);
				island.populateChunk(world, chunkX, chunkZ, random);
			}
		}
	}

	public boolean generateStructures(Chunk chunkIn, int x, int z) {
		return false;
	}

	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
		Biome biome = this.world.getBiome(pos);
		return biome.getSpawnableList(creatureType);
	}

	@Nullable
	public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
		return null;
	}

	public void recreateStructures(Chunk chunkIn, int x, int z) {

	}

}
