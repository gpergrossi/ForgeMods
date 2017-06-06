package dev.mortus.aerogen.world.islands.biomes;

import java.util.Random;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.aerogen.world.islands.IslandDecorator;
import dev.mortus.aerogen.world.islands.IslandErosion;
import dev.mortus.aerogen.world.islands.IslandHeightmap;
import dev.mortus.aerogen.world.islands.IslandShape;
import dev.mortus.util.data.Int2DRange;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public abstract class IslandBiome extends Biome {

	protected int biomeID;
	IslandDecorator decorator;
	
	public IslandBiome(BiomeProperties properties) {
		super(properties);
	}

	public void generateShape(IslandShape shape, Random random) {
		shape.erode(new IslandErosion(), random);
	}

	public void decorate(World world, Island island, Int2DRange chunkRange, Int2DRange overlapRange, Random random) {
		IslandDecorator decorator = this.getDecorator();
		if (decorator == null) {
			System.out.println("Null decorator");
			return;
		}
        decorator.decorate(world, island, chunkRange, overlapRange, random);
	}

	protected IslandDecorator getDecorator() {
		if (decorator == null) decorator = createDecorator();
		return decorator;
	}
	
	protected abstract IslandDecorator createDecorator();

	public static IBlockState getBlockByDepthStandard(Island island, int x, int y, int z, IBlockState grassLayer, IBlockState dirtLayer, IBlockState stoneLayer) {
		IslandHeightmap heightmap = island.getHeightmap();
		
		int stopY = heightmap.getTop(x, z);
		int surfaceY = heightmap.getSurfaceBeforeCarving(x, z);
		int dirtDepth = heightmap.getDirtLayerDepth(x, z);
		
		IBlockState block = stoneLayer;
		if (y > surfaceY-dirtDepth) block = dirtLayer;
		if (y == stopY) {
			if (stopY < island.getAltitude()) block = Blocks.SAND.getDefaultState();
			else if (block == dirtLayer) block = grassLayer;
		}
		return block;
	}

	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {
		return getBlockByDepthStandard(island, x, y, z, Blocks.GRASS.getDefaultState(), Blocks.DIRT.getDefaultState(), Blocks.STONE.getDefaultState());
	}

	private static final IBlockState WATER = Blocks.WATER.getDefaultState();
	
	public IBlockState getWater() {
		return WATER;
	}
	
	public boolean isVoid() {
		return false;
	}
	
	public int getBiomeID() {
		return biomeID;
	}

	public boolean hasCliffs() {
		return false;
	}
	
}
