package com.gpergrossi.aerogen.generator.decorate.placeables;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class PlaceableMinecraftGeneric implements IPlaceable {

	WorldGenerator worldGen;
	
	public PlaceableMinecraftGeneric(WorldGenerator worldGen) {
		this.worldGen = worldGen;
	}

	@Override
	public boolean place(World world, BlockPos pos, Random rand) {
		return worldGen.generate(world, rand, pos);
	}

}
