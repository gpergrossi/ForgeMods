package com.gpergrossi.aerogen.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.gpergrossi.aerogen.AeroGenChunkGenerator;
import com.gpergrossi.aerogen.AeroGenDimensionOptions;
import com.gpergrossi.aerogen.AeroGenDimensionType;
import com.mojang.serialization.Lifecycle;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

@Mixin(DimensionType.class)
public abstract class DimensionTypeMixin
{
    /**
     * This mixin hooks into DimensionType.createDefaultDimensionOptions and allows
     * us to add more DimensionOptions into the list of default dimension options.
     * This will affect which dimensions are created when the server starts up.
     */
    @SuppressWarnings("unchecked")
    @Inject(method = "createDefaultDimensionOptions(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;Lnet/minecraft/util/registry/Registry;J)Lnet/minecraft/util/registry/SimpleRegistry;", 
            at = @At(value = "TAIL"), 
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void OnCreateDefaultDimensionOptions(Registry<DimensionType> dimensionRegistry, Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed, CallbackInfoReturnable<SimpleRegistry<?>> cir, SimpleRegistry<?> simpleRegistry)
    {
        SimpleRegistry<DimensionOptions> dimensionOptionsRegistry = (SimpleRegistry<DimensionOptions>) simpleRegistry;
        
        dimensionOptionsRegistry.add(AeroGenDimensionOptions.AEROGEN_SKY, 
            new DimensionOptions(() -> {
                    return (DimensionType) dimensionRegistry.getOrThrow(AeroGenDimensionType.AEROGEN_SKY_REGISTRY_KEY);
                }, 
                AeroGenChunkGenerator.createAerogenGenerator(biomeRegistry, chunkGeneratorSettingsRegistry, seed)
            ), 
            Lifecycle.stable()
        );
    }

    /**
     * This mixin hooks into DimensionType.addRegistryDefaults and allows
     * us to add more DimensionTypes to the default DimensionTypes registry.
     */
    @SuppressWarnings("unchecked")
    @Inject(method = "addRegistryDefaults(Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;)Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;", 
            at = @At(value = "TAIL"), 
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void OnAddRegistryDefaults(DynamicRegistryManager.Impl registryManager, CallbackInfoReturnable<DynamicRegistryManager.Impl> cir, MutableRegistry<?> mutableRegistry) {
        MutableRegistry<DimensionType> dimensionTypeRegistry = (MutableRegistry<DimensionType>) mutableRegistry;
        dimensionTypeRegistry.add(AeroGenDimensionType.AEROGEN_SKY_REGISTRY_KEY, AeroGenDimensionType.AEROGEN_SKY, Lifecycle.stable());
    }
    
}
