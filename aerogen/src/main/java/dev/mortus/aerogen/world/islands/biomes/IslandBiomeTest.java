package dev.mortus.aerogen.world.islands.biomes;

public class IslandBiomeTest extends IslandBiome {
	
	private static BiomeProperties getBiomeProperties() {
		BiomeProperties properties = new BiomeProperties("Test");
		properties.setWaterColor(0x0077FF);
		return properties;
	}
	
	public IslandBiomeTest() {
		super(getBiomeProperties());
	}
	
}
