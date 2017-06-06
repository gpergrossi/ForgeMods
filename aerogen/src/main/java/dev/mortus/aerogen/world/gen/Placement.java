package dev.mortus.aerogen.world.gen;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.data.Int2D;

public abstract class Placement {
	
	public abstract int getNumAttemptsPerChunk(Random random);
	public abstract int getMaxPlacementsPerChunk(Random random);
	
	public abstract int getMinY(World world, Island island, Int2D position);
	public abstract int getMaxY(World world, Island island, Int2D position);
	
	public abstract boolean canGenerate(World world, Island island, BlockPos position, Random random);

	public static abstract class ConfigurablePlacement extends Placement {
		int desiredPlacementsPerChunk = 1;
		float chanceForExtraPlacement = 0;
		
		public ConfigurablePlacement() {}
		
		public ConfigurablePlacement withDesiredCount(int num) {
			this.desiredPlacementsPerChunk = num;
			return this;
		}
		
		public ConfigurablePlacement withChanceForExtra(float chance) {
			this.chanceForExtraPlacement = chance;
			return this;
		}
		
		@Override
		public int getNumAttemptsPerChunk(Random random) {
			return getMaxPlacementsPerChunk(random)*2;
		}

		@Override
		public int getMaxPlacementsPerChunk(Random random) {
			if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
				return (desiredPlacementsPerChunk+1);
			}
			return desiredPlacementsPerChunk;
		}
		
		@Override
		public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
			return true;
		}
	}
	
	public static class WaterSurface extends ConfigurablePlacement {
		
		public WaterSurface withDesiredCount(int num) {
			this.desiredPlacementsPerChunk = num;
			return this;
		}
		
		public WaterSurface withChanceForExtra(float chance) {
			this.chanceForExtraPlacement = chance;
			return this;
		}
		
		@Override
		public int getMinY(World world, Island island, Int2D position) {
			return world.getTopSolidOrLiquidBlock(new BlockPos(position.x, 0, position.y)).getY();
		}

		@Override
		public int getMaxY(World world, Island island, Int2D position) {
			return world.getTopSolidOrLiquidBlock(new BlockPos(position.x, 0, position.y)).getY();
		}
		
		@Override
		public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
			return (position.getY() > 0);
		}
	}
	
	public static class HighestBlock extends ConfigurablePlacement {
		
		public HighestBlock withDesiredCount(int num) {
			this.desiredPlacementsPerChunk = num;
			return this;
		}
		
		public HighestBlock withChanceForExtra(float chance) {
			this.chanceForExtraPlacement = chance;
			return this;
		}

		@Override
		public int getMinY(World world, Island island, Int2D position) {
			return world.getHeight(position.x, position.y);
		}

		@Override
		public int getMaxY(World world, Island island, Int2D position) {
			return world.getHeight(position.x, position.y);
		}

		@Override
		public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
			return (position.getY() > 0);
		}
	}
	
	public static class Interior extends ConfigurablePlacement {
		
		int minDepth = 0;
		int maxDepth = 256;
		
		public Interior withDesiredCount(int num) {
			this.desiredPlacementsPerChunk = num;
			return this;
		}
		
		public Interior withChanceForExtra(float chance) {
			this.chanceForExtraPlacement = chance;
			return this;
		}
		
		public Interior withMinDepth(int depth) {
			this.minDepth = depth;
			return this;
		}
		
		public Interior withMaxDepth(int depth) {
			this.maxDepth = depth;
			return this;
		}

		@Override
		public int getMinY(World world, Island island, Int2D position) {
			int top = island.getHeightmap().getTop(position.x, position.y);
			int bottom = island.getHeightmap().getBottom(position.x, position.y);
			return Math.max(bottom, top - maxDepth);
		}

		@Override
		public int getMaxY(World world, Island island, Int2D position) {
			int top = island.getHeightmap().getTop(position.x, position.y);
			return top - minDepth;
		}
	}
	
}
