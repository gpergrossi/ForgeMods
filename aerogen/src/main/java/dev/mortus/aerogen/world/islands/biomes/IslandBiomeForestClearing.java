package dev.mortus.aerogen.world.islands.biomes;

import dev.mortus.aerogen.world.islands.IslandDecorator;

public class IslandBiomeForestClearing extends IslandBiome {

	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Forest");
		properties.setWaterColor(0x0077FF);
		return properties;
	}

	public IslandBiomeForestClearing(BiomeProperties properties) {
		super(getBiomeProperties());
	}

	@Override
	protected IslandDecorator createDecorator() {
		return null;
	}
	
}
