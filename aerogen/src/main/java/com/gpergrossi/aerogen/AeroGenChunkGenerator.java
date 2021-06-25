package com.gpergrossi.aerogen;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

public class AeroGenChunkGenerator extends ChunkGenerator
{
    // NOTE: this codec does not support separate populationSource and biomeSource.
    public static final Codec<AeroGenChunkGenerator> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("population_source").forGetter((noiseChunkGenerator) -> {
            return noiseChunkGenerator.populationSource;
        }), BiomeSource.CODEC.fieldOf("biome_source").forGetter((noiseChunkGenerator) -> {
            return noiseChunkGenerator.biomeSource;
        }), StructuresConfig.CODEC.fieldOf("structures_config").forGetter((noiseChunkGenerator) -> {
            return noiseChunkGenerator.structuresConfig;
        }), Codec.LONG.fieldOf("seed").stable().forGetter((noiseChunkGenerator) -> {
            return noiseChunkGenerator.seed;
        })).apply(instance, instance.stable(AeroGenChunkGenerator::new));
    });

    public static ChunkGenerator createAerogenGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed)
    {
        // We can safely ignore the ChunkGeneratorSettings registry because it does not apply to AeroGen.        
        BiomeSource biomeSource = new AeroGenBiomeSource(seed, biomeRegistry);
        StructuresConfig structuresConfig = new StructuresConfig(true);
        return new AeroGenChunkGenerator(biomeSource, biomeSource, structuresConfig, seed);
    }

    private static final BlockState[] EMPTY = new BlockState[0];

    private final long seed;
    private final StructuresConfig structuresConfig;

    /**
     * @param populationSource - This is a biome source that determines only feature placement. It is usually the same as
     *                         the regular biomeSource, but it can be different to make things more interesting.
     * @param biomeSource      - The biome source that determines the biomes and allows players to search for biomes.
     * @param structuresConfig - Config detailing which structures exist and their settings. Notably contains a count of how
     *                         many strongholds will be generated.
     * @param worldSeed        - The generation random seed.
     */
    public AeroGenChunkGenerator(BiomeSource populationSource, BiomeSource biomeSource, StructuresConfig structuresConfig, long worldSeed)
    {
        super(populationSource, biomeSource, structuresConfig, worldSeed);
        this.seed = worldSeed;
        this.structuresConfig = structuresConfig;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec()
    {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed)
    {
        return new AeroGenChunkGenerator(populationSource, biomeSource, structuresConfig, seed);
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk)
    {

    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk)
    {
        // Return the noise-unpopulated chunk
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getHeight(int x, int z, Type heightmap, HeightLimitView world)
    {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world)
    {
        return new VerticalBlockSample(0, EMPTY);
    }
}
