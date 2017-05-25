package dev.mortus.aerogen.world.islands.biomes;

import java.util.Random;

import dev.mortus.aerogen.world.islands.IslandShape;
import net.minecraft.world.biome.Biome;

public abstract class IslandBiome extends Biome {

	public IslandBiome(BiomeProperties properties) {
		super(properties);
	}

	public void generateShape(Random random, IslandShape shape) {
		shape.build(random);
	}
	
}
