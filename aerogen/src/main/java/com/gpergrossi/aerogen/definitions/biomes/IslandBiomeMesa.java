package com.gpergrossi.aerogen.definitions.biomes;

import java.util.Arrays;
import java.util.Random;
import com.gpergrossi.aerogen.generator.decorate.IslandDecorator;
import com.gpergrossi.aerogen.generator.decorate.features.FeatureSurfaceCluster;
import com.gpergrossi.aerogen.generator.decorate.placeables.Placeables;
import com.gpergrossi.aerogen.generator.decorate.placement.PlacementDesertCluster;
import com.gpergrossi.aerogen.generator.islands.Island;
import com.gpergrossi.aerogen.generator.islands.IslandHeightmap;
import com.gpergrossi.util.math.func2d.CombineOperation;
import com.gpergrossi.util.math.func2d.FractalNoise2D;
import com.gpergrossi.util.math.func2d.IFunction2D;
import com.gpergrossi.util.math.func2d.RemapOperation;
import com.gpergrossi.util.math.func3d.FractalNoise3D;
import com.gpergrossi.util.math.func3d.IFunction3D;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.biome.Biome;

public class IslandBiomeMesa extends IslandBiome {

    protected static final IBlockState STAINED_HARDENED_CLAY = Blocks.STAINED_HARDENED_CLAY.getDefaultState();
    
    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    protected static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    protected static final IBlockState STONE = Blocks.STONE.getDefaultState();
    
    protected static final IBlockState HARDENED_CLAY = Blocks.HARDENED_CLAY.getDefaultState();
    protected static final IBlockState BROWN_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.BROWN);
    protected static final IBlockState RED_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.RED);
    protected static final IBlockState ORANGE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE);
    protected static final IBlockState YELLOW_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW);
    protected static final IBlockState BLACK_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.BLACK);
    protected static final IBlockState SILVER_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.SILVER);
    protected static final IBlockState WHITE_STAINED_HARDENED_CLAY = STAINED_HARDENED_CLAY.withProperty(BlockColored.COLOR, EnumDyeColor.WHITE);
    
    protected static final IBlockState RED_SAND = Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND);
	
    protected static final IFunction3D perturbNoise = FractalNoise3D.builder().withSeed(978509432122L).withPeriod(64).withRange(-3, 3).build();
    
	public IslandBiomeMesa() {}
	
	@Override
	public Biome getMinecraftBiome() {
		return Biomes.MESA;
	}

	@Override
	public void prepare(Island island) {
		
        IBlockState[] clayBands = new IBlockState[128];
        Arrays.fill(clayBands, HARDENED_CLAY);
        Random random = new Random(island.getSeed());

        for (int i = 0; i < 128; i++) {
        	clayBands[i] = ORANGE_STAINED_HARDENED_CLAY;
            i += random.nextInt(5)+1;
        }

        int numBands = random.nextInt(8) + 4;
        for (int i = 0; i < numBands; i++) {
            int bandStart = random.nextInt(128);
            int bandSize = random.nextInt(3) + 1;
            for (int j = bandStart; j < bandStart+bandSize && j < 128; j++) {
                clayBands[j] = YELLOW_STAINED_HARDENED_CLAY;
            }
        }

        numBands = random.nextInt(8) + 4;
        for (int i = 0; i < numBands; i++) {
            int bandStart = random.nextInt(128);
            int bandSize = random.nextInt(3) + 2;
            for (int j = bandStart; j < bandStart+bandSize && j < 128; j++) {
                clayBands[j] = BROWN_STAINED_HARDENED_CLAY;
            }
        }

		numBands = random.nextInt(8) + 4;
		for (int i = 0; i < numBands; i++) {
			int bandStart = random.nextInt(128);
			int bandSize = random.nextInt(3) + 1;
			for (int j = bandStart; j < bandStart+bandSize && j < 128; j++) {
				clayBands[j] = RED_STAINED_HARDENED_CLAY;
			}
		}
				
		numBands = random.nextInt(6) + 6;
		int bandY = 0;
		for (int i = 0; i < numBands; i++) {
			bandY += random.nextInt(16) + 4;
			if (bandY > 127) break;
			clayBands[bandY] = WHITE_STAINED_HARDENED_CLAY;
			if (bandY > 1 && random.nextBoolean()) clayBands[bandY - 1] = SILVER_STAINED_HARDENED_CLAY;
			if (bandY < 127 && random.nextBoolean()) clayBands[bandY + 1] = SILVER_STAINED_HARDENED_CLAY;
		}
		
		numBands = random.nextInt(6) + 4;
		for (int i = 0; i < numBands; i++) {
			int bandStart = random.nextInt(50);
			int bandSize = random.nextInt(4) + 4;
			for (int j = bandStart; j < bandStart+bandSize && j < 128; j++) {
				clayBands[j] = STONE;
			}
		}
		
		island.setExtension("MesaLayers", clayBands);
	}
	
	@Override
	public IslandHeightmap createHeightMap(Island island) {
		return new IslandHeightmap(island) {
			
			long surfaceSeed;
			
			@Override
			public void initialize(Random random) {
				super.initialize(random);
				this.surfaceHeightMin = 0;
				this.surfaceHeightMax = 1;
				this.maxCliffTier = 4;
				this.cliffHeight = 12;
				this.surfaceSeed = random.nextLong();
			}
			
			@Override
			protected IFunction2D createCliffNoise(Random random) {
				final double breakpoint = 0.6;
				IFunction2D surface = FractalNoise2D.builder().withSeed(surfaceSeed).withPeriod(128).withOctaves(3, 0.3).withRange(0, 1).build();
				IFunction2D roof = FractalNoise2D.builder().withSeed(random.nextLong()).withPeriod(256).withOctaves(2, 0.5).withRange(12, 36).build();
				surface = new CombineOperation(surface, roof, (a, b) -> {
					if (a < breakpoint) return 0;
					int xShift = (int) (60 * breakpoint) - 1;
					return (60*a-xShift) / ((60*a-xShift) + 1) * b;
				});
				return surface;
			}
			
			@Override
			protected IFunction2D createSurfaceNoise(Random random) {
				final double breakpoint = 0.6;
				IFunction2D surface = FractalNoise2D.builder().withSeed(surfaceSeed).withPeriod(128).withOctaves(3, 0.3).withRange(0, 1).build();
				surface = new RemapOperation(surface, v -> {
					if (v < 0) return 0;
					return v * (0.3/breakpoint) * 16;
				});
				return surface;
			}
		};
	}

	@Override
	public IBlockState getBlockByDepth(Island island, int x, int y, int z) {		
		IslandHeightmap heightmap = island.getHeightmap();
		
		int surfaceY = heightmap.getTop(x, z);
		
		IBlockState block = Blocks.STONE.getDefaultState();
		if (surfaceY - island.getAltitude() < 12 && y == surfaceY) {
			block = RED_SAND;
		} else {
			IBlockState[] clayBands = (IBlockState[]) island.getExtension("MesaLayers");
			y = y - island.getAltitude();
			y += perturbNoise.getValue(x, y*2, z);
			block = clayBands[(y + 64) & 127];
		}
		return block;
	}
	
	@Override
	protected IslandDecorator createDecorator() {
		
		this.decorator = new IslandDecorator();
		
		addDefaultOres(decorator);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1.0f, Placeables.DEAD_BUSH)
			.withCluster(8, 4, 16)
			.withPlacement(
				new PlacementDesertCluster()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/2.0f)
			)
		);
		
		decorator.addFeature(
			new FeatureSurfaceCluster()
			.addPlacable(1, Placeables.CACTUS)
			.withCluster(8, 4, 16)
			.withPlacement(
				new PlacementDesertCluster()
				.withDesiredCount(0)
				.withChanceForExtra(1.0f/8.0f)
			)
		);
		
		return decorator;
	}
	
}
