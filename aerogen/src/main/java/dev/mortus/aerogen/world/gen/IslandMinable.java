package dev.mortus.aerogen.world.gen;

import java.util.Random;

import com.google.common.base.Predicate;

import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.data.Int2D;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class IslandMinable extends IslandFeature {
	
	protected IBlockState oreBlock;
	protected int maxVeinSize;
	protected Predicate<IBlockState> predicateCanReplace;
	protected boolean allowUndersideVisible;

	public IslandMinable() {
		this.oreBlock = null;
		this.maxVeinSize = 0;
		this.predicateCanReplace = new StonePredicate();
		this.allowUndersideVisible = true;
	}

	public IslandMinable withOre(IBlockState block) {
		this.oreBlock = block;
		return this;
	}
	
	public IslandMinable withVeinSize(int size) {
		this.maxVeinSize = size;
		return this;
	}
	
	public IslandMinable allowUndersideVisible(boolean visible) {
		this.allowUndersideVisible = visible;
		return this;
	}
	
	public IslandMinable withReplacePredicate(Predicate<IBlockState> canReplace) {
		this.predicateCanReplace = canReplace;
		return this;
	}
	
	public boolean generate(World worldIn, Island island, BlockPos position, Random rand) {
		if (maxVeinSize == 0 || oreBlock == null) return false; 
		
		float angle = rand.nextFloat() * (float) Math.PI;
		float radius = (float) this.maxVeinSize / 8.0F;

		float clumpMinX = (float) position.getX() - MathHelper.sin(angle) * radius;
		float clumpMaxX = (float) position.getX() + MathHelper.sin(angle) * radius;

		float clumpMinZ = (float) position.getZ() - MathHelper.cos(angle) * radius;
		float clumpMaxZ = (float) position.getZ() + MathHelper.cos(angle) * radius;

		float clumpMinY = position.getY() + rand.nextInt(3) - 2;
		float clumpMaxY = position.getY() + rand.nextInt(3) - 2;

		for (int i = 0; i < this.maxVeinSize; i++) {
			float t = (float) i / (float) this.maxVeinSize;
			float lineX = clumpMinX + (clumpMaxX - clumpMinX) * t;
			float lineY = clumpMinY + (clumpMaxY - clumpMinY) * t;
			float lineZ = clumpMinZ + (clumpMaxZ - clumpMinZ) * t;
			
			float fuzzRadius = rand.nextFloat() * (radius / 2.0f);
			float fuzz = (MathHelper.sin((float) Math.PI * t) + 1.0f) * fuzzRadius + 1.0f;
			float fuzzX = fuzz;
			float fuzzY = fuzz;
			float fuzzZ = fuzz;
			
			int minX = MathHelper.floor(lineX - fuzzX / 2.0f);
			int minY = MathHelper.floor(lineY - fuzzY / 2.0f);
			int minZ = MathHelper.floor(lineZ - fuzzZ / 2.0f);
			int maxX = MathHelper.floor(lineX + fuzzX / 2.0f);
			int maxY = MathHelper.floor(lineY + fuzzY / 2.0f);
			int maxZ = MathHelper.floor(lineZ + fuzzZ / 2.0f);

			for (int x = minX; x <= maxX; x++) {
				float xdist = (x + 0.5f - lineX) / (fuzzX / 2.0f);
				xdist = xdist * xdist;
				if (xdist >= 1.0f) continue;
				
				for (int y = minY; y <= maxY; y++) {
					float ydist = (y + 0.5f - lineY) / (fuzzY / 2.0f);
					ydist = ydist * ydist;
					if (xdist + ydist >= 1.0f) continue;
					
					for (int z = minZ; z <= maxZ; z++) {
						float zdist = (z + 0.5f - lineZ) / (fuzzZ / 2.0f);
						zdist = zdist * zdist;
						if (xdist + ydist + zdist >= 1.0f) continue;
						
						BlockPos blockpos = new BlockPos(x, y, z);

						// Is showing on island underside?
						if (!allowUndersideVisible && island.getHeightmap().isVisibleUnderside(x, y, z)) continue;
						
						// Can replace existing block?
						IBlockState state = worldIn.getBlockState(blockpos);
						if (!state.getBlock().isReplaceableOreGen(state, worldIn, blockpos, this.predicateCanReplace)) continue;
						
						worldIn.setBlockState(blockpos, this.oreBlock, 2);
					}
				}
			}
		}

		return true;
	}

	static class StonePredicate implements Predicate<IBlockState> {
		private StonePredicate() {}
		public boolean apply(IBlockState blockState) {
			if (blockState != null && blockState.getBlock() == Blocks.STONE) {
				BlockStone.EnumType blockType = (BlockStone.EnumType) blockState.getValue(BlockStone.VARIANT);
				return blockType.isNatural();
			} else {
				return false;
			}
		}
	}

}
