package dev.mortus.aerogen.world.gen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import dev.mortus.aerogen.world.islands.Island;
import dev.mortus.util.data.Int2D;

public class FeatureUnderwaterBlocks extends Feature {

	Block block;
	int radius;
	
	public FeatureUnderwaterBlocks(Block block, int radius) {
		this.block = block;
		this.radius = radius;
	}
	
	@Override
	public boolean generate(World world, Island island, BlockPos position, Random rand) {
        if (world.getBlockState(position).getMaterial() != Material.WATER) return false;

        int spread = rand.nextInt(this.radius - 2) + 2;

        for (int x = -spread; x <= spread; x++) {
            for (int z = -spread; z <= spread; z++) {
            	
                if (x*x + z*z > spread*spread) continue;
            
                for (int y = -2; y <= 2; y++) {
                    BlockPos blockpos = new BlockPos(x, y, z);
                    Block block = world.getBlockState(blockpos).getBlock();

                    if (block == Blocks.SAND || block == Blocks.GRAVEL || block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS) {
                        world.setBlockState(blockpos, this.block.getDefaultState(), 2);
                    }
                }
            }
        }

        return true;
	}
	
}
