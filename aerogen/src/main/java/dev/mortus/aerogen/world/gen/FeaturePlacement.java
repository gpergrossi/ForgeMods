package dev.mortus.aerogen.world.gen;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.data.Int2D;

public abstract class FeaturePlacement {
	
	public abstract int getNumAttemptsPerChunk(Random random);
	public abstract int getMaxPlacementsPerChunk(Random random);
	
	public abstract int getMinY(World world, Island island, Int2D position);
	public abstract int getMaxY(World world, Island island, Int2D position);
	
	public abstract boolean canGenerate(World world, Island island, BlockPos position, Random random);

	public static class Surface extends FeaturePlacement {

		int desiredPlacementsPerChunk = 1;
		float chanceForExtraPlacement = 0;
		
		public Surface() {}
		
		public Surface withDesiredCount(int num) {
			this.desiredPlacementsPerChunk = num;
			return this;
		}
		
		public Surface withChanceForExtra(float chance) {
			this.chanceForExtraPlacement = chance;
			return this;
		}
		
		@Override
		public int getNumAttemptsPerChunk(Random random) {
			if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
				return (desiredPlacementsPerChunk+1)*2;
			}
			return desiredPlacementsPerChunk*2;
		}

		@Override
		public int getMaxPlacementsPerChunk(Random random) {
			if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
				return (desiredPlacementsPerChunk+1);
			}
			return desiredPlacementsPerChunk;
		}

		@Override
		public int getMinY(World world, Island island, Int2D position) {
			return island.getHeightmap().getTop(position.x, position.y)+1;
		}

		@Override
		public int getMaxY(World world, Island island, Int2D position) {
			return island.getHeightmap().getTop(position.x, position.y)+1;
		}

		@Override
		public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
			return true;
		}
		
	}
	
	public static class Interior extends FeaturePlacement {

		int desiredPlacementsPerChunk = 1;
		float chanceForExtraPlacement = 0;
		int minDepth = 0;
		int maxDepth = 256;
		
		public Interior() {}
		
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
		public int getNumAttemptsPerChunk(Random random) {
			if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
				return (desiredPlacementsPerChunk+1)*2;
			}
			return desiredPlacementsPerChunk*2;
		}

		@Override
		public int getMaxPlacementsPerChunk(Random random) {
			if (chanceForExtraPlacement > 0 && random.nextFloat() < chanceForExtraPlacement) {
				return (desiredPlacementsPerChunk+1);
			}
			return desiredPlacementsPerChunk;
		}

		@Override
		public int getMinY(World world, Island island, Int2D position) {
//			int top = island.getHeightmap().getTop(position.x, position.y);
//			int bottom = island.getHeightmap().getBottom(position.x, position.y);
//			return Math.max(bottom, top - maxDepth);
			
			return world.getHeight(position.x, position.y);
		}

		@Override
		public int getMaxY(World world, Island island, Int2D position) {
//			int top = island.getHeightmap().getTop(position.x, position.y);
//			return top - minDepth;

			return world.getHeight(position.x, position.y);
		}
		
		@Override
		public boolean canGenerate(World world, Island island, BlockPos position, Random random) {
			return true;
		}
		
	}
	
}
