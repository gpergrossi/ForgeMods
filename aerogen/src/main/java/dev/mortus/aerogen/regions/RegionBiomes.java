package dev.mortus.aerogen.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class RegionBiomes {
	
	public static RegionBiome randomBiome(Random random) {
		return ALL.get(random.nextInt(ALL.size()));
	}
	
	public static final RegionBiome FOREST = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Forest";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.FOREST);
			biomes.add(Biomes.FOREST_HILLS);
			biomes.add(Biomes.BIRCH_FOREST);
			biomes.add(Biomes.BIRCH_FOREST_HILLS);
			biomes.add(Biomes.ROOFED_FOREST);
			biomes.add(Biomes.TAIGA);
			biomes.add(Biomes.TAIGA_HILLS);
			
			biomes.add(Biomes.MUTATED_BIRCH_FOREST);
			biomes.add(Biomes.MUTATED_BIRCH_FOREST_HILLS);
			biomes.add(Biomes.MUTATED_FOREST);
			biomes.add(Biomes.MUTATED_ROOFED_FOREST);
			biomes.add(Biomes.MUTATED_TAIGA);
			
			return biomes;
		}
		
	};
	
	public static final RegionBiome DESERT = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Desert";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.DESERT);
			biomes.add(Biomes.DESERT_HILLS);
			biomes.add(Biomes.MUTATED_DESERT);
			biomes.add(Biomes.MESA_ROCK);
			
			return biomes;
		}
		
	};
	
	public static final RegionBiome SAVANNA = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Savanna";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.SAVANNA);
			biomes.add(Biomes.SAVANNA_PLATEAU);
			biomes.add(Biomes.MUTATED_SAVANNA);
			biomes.add(Biomes.MUTATED_SAVANNA_ROCK);
			
			return biomes;
		}
		
	};
	
	public static final RegionBiome SWAMP = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Swamp";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.SWAMPLAND);
			biomes.add(Biomes.MUTATED_SWAMPLAND);
			
			return biomes;
		}
		
	};
	
	public static final RegionBiome JUNGLE = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Jungle";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.JUNGLE);
			biomes.add(Biomes.JUNGLE_EDGE);
			biomes.add(Biomes.JUNGLE_HILLS);
			biomes.add(Biomes.MUTATED_JUNGLE);
			biomes.add(Biomes.MUTATED_JUNGLE_EDGE);
			
			return biomes;
		}

	};
	
	public static final RegionBiome TAIGA = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Taiga";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.TAIGA);
			biomes.add(Biomes.TAIGA_HILLS);
			biomes.add(Biomes.COLD_TAIGA);
			biomes.add(Biomes.COLD_TAIGA_HILLS);
			biomes.add(Biomes.MUTATED_REDWOOD_TAIGA);
			biomes.add(Biomes.MUTATED_REDWOOD_TAIGA_HILLS);
			biomes.add(Biomes.MUTATED_TAIGA);
			biomes.add(Biomes.MUTATED_TAIGA_COLD);
			biomes.add(Biomes.REDWOOD_TAIGA);
			biomes.add(Biomes.REDWOOD_TAIGA_HILLS);
			
			return biomes;
		}

	};
	
	public static final RegionBiome EXTREME_HILLS = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Extreme Hills";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.EXTREME_HILLS);
			biomes.add(Biomes.EXTREME_HILLS_EDGE);
			biomes.add(Biomes.EXTREME_HILLS_WITH_TREES);
			biomes.add(Biomes.MUTATED_EXTREME_HILLS);
			biomes.add(Biomes.MUTATED_EXTREME_HILLS_WITH_TREES);
			
			return biomes;
		}

	};
	
	public static final RegionBiome FROSTLAND = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Frostland";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.ICE_PLAINS);
			biomes.add(Biomes.MUTATED_ICE_FLATS);
			biomes.add(Biomes.ICE_MOUNTAINS);
			biomes.add(Biomes.COLD_BEACH);
			biomes.add(Biomes.COLD_TAIGA);
			
			return biomes;
		}

	};
	
	public static final RegionBiome MESA = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Mesa";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.MESA);
			biomes.add(Biomes.MESA_CLEAR_ROCK);
			biomes.add(Biomes.MESA_ROCK);
			biomes.add(Biomes.MUTATED_MESA);
			biomes.add(Biomes.MUTATED_MESA_CLEAR_ROCK);
			biomes.add(Biomes.MUTATED_MESA_ROCK);
			biomes.add(Biomes.MUTATED_SAVANNA_ROCK);
			biomes.add(Biomes.MUTATED_SAVANNA);
			biomes.add(Biomes.SAVANNA_PLATEAU);
			
			return biomes;
		}

	};
	
	public static final RegionBiome MUSHROOM_LAND = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Mushroom Land";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.MUSHROOM_ISLAND);
			biomes.add(Biomes.MUSHROOM_ISLAND_SHORE);
			biomes.add(Biomes.ROOFED_FOREST);
			biomes.add(Biomes.MUTATED_ROOFED_FOREST);
			
			return biomes;
		}

	};
	
	public static final RegionBiome OCEAN = new RegionBiome() {
		
		@Override
		public String getName() {
			return "Ocean";
		}

		@Override
		public List<Biome> getPossibleIslandBiomes() {
			List<Biome> biomes = new ArrayList<>();
			
			biomes.add(Biomes.OCEAN);
			biomes.add(Biomes.DEEP_OCEAN);
			
			return biomes;
		}

	};
	
	private static List<RegionBiome> ALL = new ArrayList<>();
	static {
		ALL.add(FOREST);
		ALL.add(DESERT);
		ALL.add(SAVANNA);
		ALL.add(SWAMP);
		ALL.add(JUNGLE);
		ALL.add(TAIGA);
		ALL.add(EXTREME_HILLS);
		ALL.add(FROSTLAND);
		ALL.add(MESA);
		ALL.add(MUSHROOM_LAND);
		ALL.add(OCEAN);
	}
	
}
