package dev.mortus.aerogen.world.gen.placeables;

import java.util.Random;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceableDoublePlant implements IPlaceable {

	final BlockDoublePlant.EnumPlantType plantType;

	public PlaceableDoublePlant(BlockDoublePlant.EnumPlantType plantType) {
		this.plantType = plantType;
	}

	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		if (Blocks.DOUBLE_PLANT.canPlaceBlockAt(world, pos)) {
			Blocks.DOUBLE_PLANT.placeAt(world, pos, this.plantType, 2);
			return true;
		}
		return false;
	}

}
