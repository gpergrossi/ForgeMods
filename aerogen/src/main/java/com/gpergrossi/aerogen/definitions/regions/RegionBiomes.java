package com.gpergrossi.aerogen.definitions.regions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gpergrossi.aerogen.definitions.biomes.IslandBiome;
import com.gpergrossi.aerogen.definitions.biomes.IslandBiomes;

public class RegionBiomes {
	
	public static RegionBiome randomBiome(Random random) {
		return ALL.get(random.nextInt(ALL.size()));
	}
	
	public static final RegionBiome REGION_FOREST = new RegionBiome() {
		@Override
		public String getName() {
			return "Forest";
		}
		
		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.FOREST);
			biomes.add(IslandBiomes.FOREST_CLEARING);
			biomes.add(IslandBiomes.FOREST_BIRCH);
			return biomes;
		}
		
		@Override
		public int getRandomNumberOfRivers(Random random) {
			double roll = random.nextDouble();
			if (roll < 0.75) return 1;
			return 2;
		}
		
	};
	
	public static final RegionBiome START_AREA = REGION_FOREST;
	
	public static final RegionBiome REGION_DESERT = new RegionBiome() {
		@Override
		public String getName() {
			return "Desert";
		}

		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.DESERT);
			biomes.add(IslandBiomes.DESERT_DUNES);
			return biomes;
		}
		
		@Override
		public double getIslandCellGatherPercentage() {
			return 0.75;
		}
		
		@Override
		public double getCellSizeMultiplier() {
			return 1.5;
		}
		
		@Override
		public int getRandomIslandAltitude(Random random, int minHeight, int maxHeight) {
			int altitude = random.nextInt(maxHeight-minHeight+1)+minHeight;
			altitude = Math.min(altitude, random.nextInt(maxHeight-minHeight+1)+minHeight);
			return altitude;
		}

	};
	
	public static final RegionBiome REGION_JUNGLE = new RegionBiome() {
		@Override
		public String getName() {
			return "Jungle";
		}

		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.JUNGLE);
			return biomes;
		}
		
		@Override
		public int getRandomNumberOfRivers(Random random) {
			return 2;
		}
		
		@Override
		public double getIslandCellGatherPercentage() {
			return 0.25;
		}
		
		@Override
		public double getCellSizeMultiplier() {
			return 1.5;
		}
		
		@Override
		public int getIslandMinAltitude() {
			return 52;
		}
		
		@Override
		public int getIslandMaxAltitude() {
			return 132;
		}
		
		@Override
		public int getRandomIslandAltitude(Random random, int minHeight, int maxHeight) {
			int altitude = random.nextInt(maxHeight-minHeight+1)+minHeight;
			return altitude;
		}
		
	};
	
	public static final RegionBiome REGION_SAVANNAH = new RegionBiome() {
		@Override
		public String getName() {
			return "Savannah";
		}

		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.SAVANNAH);
			return biomes;
		}
		
		@Override
		public int getRandomNumberOfRivers(Random random) {
			return 2;
		}
		
		@Override
		public double getIslandCellGatherPercentage() {
			return 0.75;
		}
		
		@Override
		public double getCellSizeMultiplier() {
			return 1.5;
		}
		
		@Override
		public int getRandomIslandAltitude(Random random, int minHeight, int maxHeight) {
			int altitude = random.nextInt(maxHeight-minHeight+1)+minHeight;
			altitude = Math.min(altitude, random.nextInt(maxHeight-minHeight+1)+minHeight);
			return altitude;
		}

	};
	
	public static final RegionBiome REGION_SNOWY = new RegionBiome() {
		@Override
		public String getName() {
			return "Snowy";
		}
		
		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.COLD_TAIGA);
			return biomes;
		}
		
		@Override
		public int getRandomNumberOfRivers(Random random) {
			double roll = random.nextDouble();
			if (roll < 0.75) return 1;
			return 2;
		}
		
	};
	
	public static final RegionBiome REGION_DEEP_FOREST = new RegionBiome() {
		@Override
		public String getName() {
			return "Deep Forest";
		}
		
		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.ROOFED_FOREST);
			return biomes;
		}
		
		@Override
		public int getRandomNumberOfRivers(Random random) {
			double roll = random.nextDouble();
			if (roll < 0.75) return 1;
			return 2;
		}
		
	};
	
	public static final RegionBiome REGION_MESA = new RegionBiome() {
		@Override
		public String getName() {
			return "Mesa";
		}

		@Override
		public List<IslandBiome> getPossibleIslandBiomes() {
			List<IslandBiome> biomes = new ArrayList<>();
			biomes.add(IslandBiomes.MESA);
			return biomes;
		}
		
		@Override
		public double getIslandCellGatherPercentage() {
			return 0.75;
		}
		
		@Override
		public double getCellSizeMultiplier() {
			return 1.5;
		}
		
		@Override
		public int getRandomIslandAltitude(Random random, int minHeight, int maxHeight) {
			int altitude = random.nextInt(maxHeight-minHeight+1)+minHeight;
			altitude = Math.min(altitude, random.nextInt(maxHeight-minHeight+1)+minHeight);
			return altitude;
		}

	};
	
//	public static final RegionBiome SWAMP = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Swamp";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.SWAMPLAND);
//			biomes.add(Biomes.MUTATED_SWAMPLAND);
//			
//			return biomes;
//		}
//		
//	};
//	
//	
//	public static final RegionBiome EXTREME_HILLS = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Extreme Hills";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.EXTREME_HILLS);
//			biomes.add(Biomes.EXTREME_HILLS_EDGE);
//			biomes.add(Biomes.EXTREME_HILLS_WITH_TREES);
//			biomes.add(Biomes.MUTATED_EXTREME_HILLS);
//			biomes.add(Biomes.MUTATED_EXTREME_HILLS_WITH_TREES);
//			
//			return biomes;
//		}
//
//	};
//	
//	public static final RegionBiome FROSTLAND = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Frostland";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.ICE_PLAINS);
//			biomes.add(Biomes.MUTATED_ICE_FLATS);
//			biomes.add(Biomes.ICE_MOUNTAINS);
//			biomes.add(Biomes.COLD_BEACH);
//			biomes.add(Biomes.COLD_TAIGA);
//			
//			return biomes;
//		}
//
//	};
//	
//	public static final RegionBiome MESA = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Mesa";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.MESA);
//			biomes.add(Biomes.MESA_CLEAR_ROCK);
//			biomes.add(Biomes.MESA_ROCK);
//			biomes.add(Biomes.MUTATED_MESA);
//			biomes.add(Biomes.MUTATED_MESA_CLEAR_ROCK);
//			biomes.add(Biomes.MUTATED_MESA_ROCK);
//			biomes.add(Biomes.MUTATED_SAVANNA_ROCK);
//			biomes.add(Biomes.MUTATED_SAVANNA);
//			biomes.add(Biomes.SAVANNA_PLATEAU);
//			
//			return biomes;
//		}
//
//	};
//	
//	public static final RegionBiome MUSHROOM_LAND = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Mushroom Land";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.MUSHROOM_ISLAND);
//			biomes.add(Biomes.MUSHROOM_ISLAND_SHORE);
//			biomes.add(Biomes.ROOFED_FOREST);
//			biomes.add(Biomes.MUTATED_ROOFED_FOREST);
//			
//			return biomes;
//		}
//
//	};
//	
//	public static final RegionBiome OCEAN = new RegionBiome() {
//		
//		@Override
//		public String getName() {
//			return "Ocean";
//		}
//
//		@Override
//		public List<Biome> getPossibleIslandBiomes() {
//			List<Biome> biomes = new ArrayList<>();
//			
//			biomes.add(Biomes.OCEAN);
//			biomes.add(Biomes.DEEP_OCEAN);
//			
//			return biomes;
//		}
//
//	};
//	
	private static List<RegionBiome> ALL = new ArrayList<>();
	static {
		ALL.add(REGION_DEEP_FOREST);
		ALL.add(REGION_SNOWY);
		ALL.add(REGION_SAVANNAH);
		ALL.add(REGION_FOREST);
		ALL.add(REGION_DESERT);
		ALL.add(REGION_MESA);
		ALL.add(REGION_JUNGLE);
	}
	
}
